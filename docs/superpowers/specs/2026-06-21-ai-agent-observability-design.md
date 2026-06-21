# ai-agent Observability Design

**Date:** 2026-06-21
**Scope:** `ai-agent` module only
**Signals:** Metrics only (traces and logs are out of scope for this iteration)

---

## Goals

- Auto-start a full LGTM observability stack (Grafana, Prometheus, Loki, Tempo) in dev mode with zero manual setup
- Expose built-in LangChain4J token usage and AI service call metrics via Prometheus
- Add custom Micrometer Gauges for token counts tagged by both `model` and `agent`, backed by the existing in-memory collector so the reset REST endpoint continues to work

---

## Dependencies

Three additions to `ai-agent/pom.xml` (all managed by `quarkus-bom`, no explicit versions):

| Artifact | Purpose |
|---|---|
| `quarkus-micrometer-opentelemetry` | Activates built-in `MetricsChatModelListener`; bridges OTel metrics to Micrometer |
| `quarkus-micrometer-registry-prometheus` | Exposes `/q/metrics` for Prometheus scraping |
| `quarkus-observability-devservices-lgtm` (scope: `provided`) | Auto-starts Grafana + Prometheus + Loki + Tempo in `quarkus:dev` only |

---

## Configuration

Additions to `ai-agent/src/main/resources/application.properties`:

```properties
quarkus.otel.metrics.enabled=true
quarkus.otel.traces.enabled=true
quarkus.otel.logs.enabled=true

quarkus.observability.enabled=true
quarkus.observability.dev-resources=true
```

The Grafana UI is accessible from the Quarkus Dev UI at `/q/dev-ui` with no manual configuration.

---

## Built-in Metrics (zero code)

Once dependencies are added, the following metrics appear automatically in `/q/metrics`:

| Metric | Type | Tags | Description |
|---|---|---|---|
| `gen_ai.client.token.usage` | Counter | `model`, `provider`, token type (`input`/`output`) | Token counts per model, GenAI semantic convention |
| `langchain4j.aiservices.timed` | Timer | `service`, `method`, `exception`, `result` | Per-agent call duration |
| `langchain4j.aiservices.counted` | Counter | `service`, `method`, `exception`, `result` | Per-agent call count |
| `gen_ai.client.estimated_cost` | Counter | `model`, `provider` | Cost estimate (requires a `CostEstimator` bean; not implemented in this iteration) |

---

## Custom Gauge Layer

The built-in `gen_ai.client.token.usage` is a Counter (cannot be reset). To preserve the existing reset semantics and add agent-level token breakdown, a thin custom layer is added.

### `ModelAgentKey` (new record)

```
dev.sitsko.ai.observability.ModelAgentKey
```

Replaces the plain `String model` key in `TokenMetricCollector`:

```java
record ModelAgentKey(String model, String agent) {}
```

### `TokenMetricCollector` (modified)

- Change `Map<String, AtomicInteger> inputTokens` → `Map<ModelAgentKey, AtomicInteger> inputTokens`
- Change `Map<String, AtomicInteger> outputTokens` → `Map<ModelAgentKey, AtomicInteger> outputTokens`
- The existing `@Observes AiServiceResponseReceivedEvent onResponse(...)` method is **removed** from `TokenMetricCollector`; the collector no longer observes CDI events directly. Instead a new `record(ModelAgentKey key, TokenUsage usage)` method is added, called by `MicrometerTokenExporter`
- The `clearTokenStatistics` / `flushTokens` logic is unchanged
- `MetricResource` (REST) adapts to the new key; the response shape gains an `agent` field per entry

### `MicrometerTokenExporter` (new)

```
dev.sitsko.ai.observability.MicrometerTokenExporter
```

`@ApplicationScoped` bean. Responsibilities:
1. Observe `AiServiceResponseReceivedEvent` per named agent using `@AiServiceSelector` qualifier
2. Delegate to `TokenMetricCollector.record(key, usage)` to update in-memory state
3. Register a Micrometer `Gauge` for each new `ModelAgentKey` seen, backed directly by the `AtomicInteger` in the collector — so resetting the collector also zeroes the gauge value in Prometheus

**Observer methods** (one per agent, calls shared `record(agentName, event)` helper):

| Observer qualifier | Agent name tag value |
|---|---|
| `@AiServiceSelector(ParserAgent.class)` | `"ParserAgent"` |
| `@AiServiceSelector(ScheduleAgent.class)` | `"ScheduleAgent"` |
| `@AiServiceSelector(SecurityAgent.class)` | `"SecurityAgent"` |
| `@AiServiceSelector(DataLeakAgent.class)` | `"DataLeakAgent"` |

Adding a new agent requires adding one observer method here.

**Gauge registration** (called once per new key, idempotent on repeat):

```java
Gauge.builder("ai_agent.token.input", collector.getInputTokens(),
        map -> map.getOrDefault(key, new AtomicInteger(0)).doubleValue())
    .tag("model", key.model())
    .tag("agent", key.agent())
    .register(registry);
```

**Resulting Prometheus metric names:**

| Metric | Tags |
|---|---|
| `ai_agent_token_input` | `model`, `agent` |
| `ai_agent_token_output` | `model`, `agent` |

---

## Data Flow

```
LLM response
  │
  ├─► MetricsChatModelListener (built-in)
  │     └─► gen_ai.client.token.usage (Counter, by model+provider)
  │         langchain4j.aiservices.timed (Timer, by agent service+method)
  │
  └─► MicrometerTokenExporter (new, @AiServiceSelector per agent)
        ├─► TokenMetricCollector.record(ModelAgentKey, usage)
        │     └─► AtomicInteger maps (backing store for REST + Gauges)
        └─► MeterRegistry.Gauge (ai_agent.token.input/output, by model+agent)

REST endpoint (existing)
  GET /api/metrics/tokens  ← reads TokenMetricCollector maps
  DELETE /api/metrics/tokens ← clears AtomicIntegers → Gauges drop to 0
```

---

## What Is Not Changing

- `SecurityService` (deterministic keyword checks) — no metrics added
- `BookingResource` timeout / fault tolerance — no changes
- The REST endpoint path `/api/metrics/tokens` stays the same; response shape gains an `agent` field per token entry
- No traces, no Loki log shipping, no cost estimation in this iteration

---

## Open Implementation Question

The `@AiServiceSelector` qualifier API needs to be verified against the actual quarkus-langchain4j version in use (1.10.0 via Quarkus 3.36.2). If the qualifier name or usage differs, observer methods may need adjustment. Verify by checking `io.quarkiverse.langchain4j.observability.AiServiceSelector` in the classpath before writing the exporter.
