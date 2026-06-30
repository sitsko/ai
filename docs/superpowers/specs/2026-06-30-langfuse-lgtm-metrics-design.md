# Langfuse + LGTM Metrics Integration for ai-agent

**Date:** 2026-06-30  
**Module:** `ai-agent`  
**Branch:** `langfuse`

---

## Goal

Add full AI observability to the `ai-agent` module:
- **Langfuse** — per-request LLM traces (prompts, responses, token usage, cost) for ParserAgent, ScheduleAgent, SecurityAgent, DataLeakAgent
- **LGTM/Grafana** — aggregate metrics: token counts (existing) + new per-agent call latency timers

Run everything locally: Langfuse via Docker Compose, LGTM stack via Quarkus DevServices.

---

## Architecture

```
[Quarkus ai-agent app]
  ├── Micrometer (existing) ──────────────────────→ LGTM DevService (Mimir → Grafana)
  └── OTEL SDK (quarkus-opentelemetry, NEW)
        ├── Primary OTLP exporter ────────────────→ LGTM DevService (Tempo)
        └── LangfuseTracerCustomizer (NEW) ───────→ Langfuse localhost:3000

[docker-compose.yml — NEW, in ai-agent/]
  ├── langfuse     (port 3000, pre-seeded with API keys via LANGFUSE_INIT_* vars)
  └── postgres     (internal, no exposed port)

[Quarkus LGTM DevService — existing, auto-started in dev mode]
  ├── Grafana      (dashboards)
  ├── Tempo        (distributed traces)
  ├── Mimir        (Prometheus metrics)
  └── Loki         (logs)
```

**Key design decision:** No standalone OTEL Collector container. Instead, a `LangfuseTracerCustomizer` bean implements Quarkus's `TracerProviderCustomizer` SPI to register a second `BatchSpanProcessor` pointing to Langfuse. The DevService's primary OTLP exporter (→ Tempo) is untouched.

---

## Components

### New Files

#### `ai-agent/docker-compose.yml`

Starts Langfuse and its Postgres backend. The `LANGFUSE_INIT_*` environment variables pre-seed the project and API keys so no manual UI setup is needed after first `docker compose up`.

Services:
- `langfuse` — `langfuse/langfuse:3`, port 3000
- `postgres` — `postgres:18.4`, internal-only

#### `LangfuseTracerCustomizer.java`

```
dev.sitsko.ai.observability.LangfuseTracerCustomizer
```

Implements `io.quarkus.opentelemetry.runtime.tracing.TracerProviderCustomizer`. In `customize()`:
1. Reads Langfuse base URL and API keys from config
2. Builds an `OtlpHttpSpanExporter` targeting `{langfuse.base-url}/api/public/otel/v1/traces` with `Authorization: Basic <base64(publicKey:secretKey)>` header
3. Adds a `BatchSpanProcessor` wrapping that exporter to `SdkTracerProviderBuilder`

No changes to agent interfaces — LangChain4J Quarkus emits `gen_ai.*` spans automatically when `quarkus-opentelemetry` is on the classpath.

### Modified Files

#### `pom.xml`

Add dependency:
```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-opentelemetry</artifactId>
</dependency>
```

#### `application.properties`

Add:
```properties
# Langfuse OTLP configuration
langfuse.base-url=${LANGFUSE_BASE_URL:http://localhost:3000}
langfuse.public-key=${LANGFUSE_PUBLIC_KEY:dummy}
langfuse.secret-key=${LANGFUSE_SECRET_KEY:dummy}

# Include prompt and response content in OTEL spans
quarkus.langchain4j.tracing.include-prompt=true
quarkus.langchain4j.tracing.include-completion=true
```

#### `.env`

Add:
```
LANGFUSE_BASE_URL=http://localhost:3000
LANGFUSE_PUBLIC_KEY=<from docker-compose LANGFUSE_INIT_PROJECT_PUBLIC_KEY>
LANGFUSE_SECRET_KEY=<from docker-compose LANGFUSE_INIT_PROJECT_SECRET_KEY>
```

#### `MicrometerTokenExporter.java`

Extend existing class to also record per-agent call latency:
- Observe `AiServiceStartedEvent` per agent — record `Instant.now()` in a `ConcurrentHashMap<ModelAgentKey, Instant>` keyed by current thread or event correlation
- On `AiServiceResponseReceivedEvent` — compute elapsed time, record to `Timer` tagged `model` + `agent`
- New metric: `ai_agent.call.duration` (histogram, seconds)

---

## Data Flow

### Trace tree in Langfuse per booking request

```
POST /api/booking/proposal                    (HTTP root span)
  └── BookingWorkflow.reservationData         (sequence agent span)
        ├── ParserAgent                       (LLM call span)
        │     gen_ai.system = openai
        │     gen_ai.model = gpt-4o-mini
        │     gen_ai.usage.input_tokens = ~312
        │     gen_ai.usage.output_tokens = ~89
        │     [events: prompt, completion text]
        │
        ├── SecurityAgent                     (guardrail LLM span)
        │     gen_ai.usage.input_tokens = ~45
        │     gen_ai.usage.output_tokens = ~12
        │
        ├── ScheduleAgent                     (LLM call + tool call span)
        │     gen_ai.usage.input_tokens = ~891
        │     gen_ai.usage.output_tokens = ~213
        │     [tool call: ScheduleService]
        │
        └── DataLeakAgent                     (output guardrail LLM span)
              gen_ai.usage.input_tokens = ~67
              gen_ai.usage.output_tokens = ~8
```

Langfuse maps `gen_ai.usage.*` token attributes to its Observation token fields and computes cost using its built-in model pricing table.

The same spans go to Tempo via the primary OTLP exporter for Grafana trace correlation.

### Metrics in Grafana

| Metric | Type | Tags | Source |
|--------|------|------|--------|
| `ai_agent.token.input` | Gauge | model, agent | existing |
| `ai_agent.token.output` | Gauge | model, agent | existing |
| `ai_agent.call.duration` | Timer/Histogram | model, agent | new |

---

## Dev Setup

### First run

```bash
# 1. Start Langfuse + Postgres
cd ai-agent
docker compose up -d

# 2. Start Quarkus (starts LGTM DevService automatically)
./mvnw quarkus:dev
```

### Langfuse UI

`http://localhost:3000` — log in with credentials from `LANGFUSE_INIT_USER_*` env vars in docker-compose.yml.

### API Keys

The `LANGFUSE_INIT_PROJECT_PUBLIC_KEY` and `LANGFUSE_INIT_PROJECT_SECRET_KEY` values in docker-compose.yml must match what's in `.env`. Choose fixed values at design time and keep them consistent.

---

## Constraints & Decisions

- **No standalone OTEL Collector:** Avoided to keep Docker Compose minimal. The secondary exporter pattern (via `TracerProviderCustomizer`) achieves fan-out at the app level.
- **LGTM DevService retained:** The devservice handles Tempo, Mimir, Loki, and Grafana. Docker Compose only adds what the devservice doesn't provide (Langfuse).
- **Prompt content in spans:** `quarkus.langchain4j.tracing.include-prompt=true` and `quarkus.langchain4j.tracing.include-completion=true` send prompt and response text into span events (from `TracingConfig` in quarkus-langchain4j-core 1.10.0). Disable in production to avoid shipping sensitive data.
- **Cost tracking:** Langfuse computes cost from `gen_ai.model` and token counts using its internal pricing table — no custom implementation needed.
- **Latency correlation:** Start/response event pairing in `MicrometerTokenExporter` uses a `ConcurrentHashMap` keyed by a correlation identifier from the event. Implementation detail: verify what correlation field is available on `AiServiceStartedEvent` and `AiServiceResponseReceivedEvent` before implementing.
