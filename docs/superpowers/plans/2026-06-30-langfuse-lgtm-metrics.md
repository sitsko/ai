# Langfuse + LGTM Metrics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire Langfuse LLM traces and LGTM latency metrics into the `ai-agent` Quarkus module so every booking workflow invocation produces full OTEL spans in Langfuse and `ai_agent.call.duration` timers in Grafana.

**Architecture:** Add `quarkus-opentelemetry`; Quarkus DevService auto-configures the primary OTLP exporter → LGTM/Tempo. A CDI `@Produces SpanProcessor` bean (Quarkus collects all `SpanProcessor` beans via `@All`) adds a second `BatchSpanProcessor` → Langfuse's OTLP endpoint. Agent call latency is tracked in `MicrometerTokenExporter` by pairing `AiServiceStartedEvent` / `AiServiceResponseReceivedEvent` on `invocationContext().invocationId()` (UUID).

**Tech Stack:** Quarkus 3.36.2, quarkus-langchain4j 1.10.0, langchain4j-core 1.14.1-beta24, `opentelemetry-exporter-otlp` 1.60.1 (transitive), Micrometer, Docker Compose, Langfuse v3, Postgres 18.4.

## Global Constraints

- All work is in `ai-agent/` only — do not touch `ai-tool-calling/`
- Java 21, Maven wrapper (`./mvnw`) — never use system `mvn`
- Run all compile/test commands from inside `ai-agent/` directory
- Do not add any dependency not already in `quarkus-bom` or `quarkus-langchain4j-bom` 3.36.2 unless noted; `opentelemetry-exporter-otlp` is already a transitive dep of `quarkus-opentelemetry` — do NOT add it explicitly
- Lombok is on the classpath — use `@RequiredArgsConstructor` where it simplifies injection
- Test framework: JUnit 5 + AssertJ (`assertThat`) — follow the existing test style in `MicrometerTokenExporterTest`
- Commit after each task using `git commit -m "feat: <short description>"`

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `pom.xml` | Modify | Add `quarkus-opentelemetry` dependency |
| `docker-compose.yml` | Create | Langfuse + Postgres 18.4 services |
| `.env` | Modify | Add `LANGFUSE_*` keys |
| `src/main/resources/application.properties` | Modify | OTEL endpoint, tracing content flags, Langfuse config |
| `src/main/java/.../observability/LangfuseSpanProcessor.java` | Create | `@Produces SpanProcessor` CDI bean → Langfuse OTLP |
| `src/test/java/.../observability/LangfuseSpanProcessorTest.java` | Create | Unit tests for LangfuseSpanProcessor |
| `src/main/java/.../observability/MicrometerTokenExporter.java` | Modify | Add start-event observers + `ai_agent.call.duration` Timer |
| `src/test/java/.../observability/MicrometerTokenExporterTest.java` | Modify | Add Timer tests |

---

### Task 1: Infrastructure — Docker Compose + OTEL dependency + config

**Files:**
- Modify: `ai-agent/pom.xml`
- Create: `ai-agent/docker-compose.yml`
- Modify: `ai-agent/.env`
- Modify: `ai-agent/src/main/resources/application.properties`

**Interfaces:**
- Produces: Langfuse running at `http://localhost:3000`; `quarkus-opentelemetry` on classpath; config properties `langfuse.base-url`, `langfuse.public-key`, `langfuse.secret-key` available to CDI beans

- [ ] **Step 1: Add `quarkus-opentelemetry` to pom.xml**

Open `ai-agent/pom.xml`. In the `<dependencies>` section, after the `<!-- Observability -->` comment block (around line 75), add:

```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-opentelemetry</artifactId>
    </dependency>
```

- [ ] **Step 2: Create `ai-agent/docker-compose.yml`**

```yaml
services:
  postgres:
    image: postgres:18.4
    environment:
      POSTGRES_USER: langfuse
      POSTGRES_PASSWORD: langfuse
      POSTGRES_DB: langfuse
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U langfuse"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - langfuse-network

  langfuse:
    image: langfuse/langfuse:3
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "3000:3000"
    environment:
      DATABASE_URL: postgresql://langfuse:langfuse@postgres:5432/langfuse
      NEXTAUTH_SECRET: dev-nextauth-secret-change-in-prod
      SALT: dev-salt-change-in-prod
      NEXTAUTH_URL: http://localhost:3000
      LANGFUSE_INIT_ORG_ID: dev-org
      LANGFUSE_INIT_ORG_NAME: Dev
      LANGFUSE_INIT_PROJECT_ID: ai-agent-project
      LANGFUSE_INIT_PROJECT_NAME: ai-agent
      LANGFUSE_INIT_PROJECT_PUBLIC_KEY: pk-lf-dev-public-key-1234567890
      LANGFUSE_INIT_PROJECT_SECRET_KEY: sk-lf-dev-secret-key-1234567890
      LANGFUSE_INIT_USER_EMAIL: dev@sitsko.dev
      LANGFUSE_INIT_USER_PASSWORD: devpassword123
      LANGFUSE_INIT_USER_NAME: Dev User
    networks:
      - langfuse-network

networks:
  langfuse-network:
    driver: bridge
```

- [ ] **Step 3: Add Langfuse env vars to `ai-agent/.env`**

Append to the existing `.env` file:

```
LANGFUSE_BASE_URL=http://localhost:3000
LANGFUSE_PUBLIC_KEY=pk-lf-dev-public-key-1234567890
LANGFUSE_SECRET_KEY=sk-lf-dev-secret-key-1234567890
```

The key values must match `LANGFUSE_INIT_PROJECT_PUBLIC_KEY` / `LANGFUSE_INIT_PROJECT_SECRET_KEY` in `docker-compose.yml`.

- [ ] **Step 4: Update `application.properties`**

Append to `ai-agent/src/main/resources/application.properties`:

```properties
# OTEL — primary exporter auto-configured by LGTM DevService
# Langfuse config (picked up by LangfuseSpanProcessor)
langfuse.base-url=${LANGFUSE_BASE_URL:http://localhost:3000}
langfuse.public-key=${LANGFUSE_PUBLIC_KEY:dummy}
langfuse.secret-key=${LANGFUSE_SECRET_KEY:dummy}

# Include prompt text and LLM response in OTEL spans
quarkus.langchain4j.tracing.include-prompt=true
quarkus.langchain4j.tracing.include-completion=true
```

- [ ] **Step 5: Verify the project compiles**

```bash
cd ai-agent
./mvnw compile -q
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 6: Start Langfuse and verify it's healthy**

```bash
cd ai-agent
docker compose up -d
```

Wait ~20 seconds, then:

```bash
curl -s http://localhost:3000/api/public/health
```

Expected: `{"status":"ok"}` (or similar JSON with status OK).

Open `http://localhost:3000` in a browser and log in with `dev@sitsko.dev` / `devpassword123`. The `ai-agent` project should be visible.

- [ ] **Step 7: Commit**

```bash
git add pom.xml docker-compose.yml .env src/main/resources/application.properties
git commit -m "feat: add quarkus-opentelemetry and Langfuse docker-compose"
```

---

### Task 2: LangfuseSpanProcessor — second OTLP span exporter

**Files:**
- Create: `ai-agent/src/main/java/dev/sitsko/ai/observability/LangfuseSpanProcessor.java`
- Create: `ai-agent/src/test/java/dev/sitsko/ai/observability/LangfuseSpanProcessorTest.java`

**Interfaces:**
- Consumes: config properties `langfuse.base-url`, `langfuse.public-key`, `langfuse.secret-key`
- Produces: CDI `SpanProcessor` bean named `"langfuse"` — Quarkus's `TracerProviderCustomizer` collects all `SpanProcessor` beans via `@All List<SpanProcessor>` and adds them to the tracer provider automatically (see `AutoConfiguredOpenTelemetrySdkBuilderCustomizer.TracerProviderCustomizer` in `quarkus-opentelemetry-3.36.2-sources.jar`)

- [ ] **Step 1: Write the failing test**

The class has `@ConfigProperty` fields injected by CDI, so tests call the package-private static factory method `buildProcessor` directly — no CDI container required.

Create `ai-agent/src/test/java/dev/sitsko/ai/observability/LangfuseSpanProcessorTest.java`:

```java
package dev.sitsko.ai.observability;

import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LangfuseSpanProcessorTest {

    @Test
    void buildProcessor_returnsNonNullSpanProcessor() {
        SpanProcessor processor = LangfuseSpanProcessor.buildProcessor(
                "http://localhost:3000", "pk-lf-test-key", "sk-lf-test-key");

        assertThat(processor).isNotNull();
    }

    @Test
    void buildProcessor_withDummyKeysDoesNotThrow() {
        assertThat(LangfuseSpanProcessor.buildProcessor(
                "http://localhost:3000", "dummy", "dummy")).isNotNull();
    }
}
```

- [ ] **Step 2: Run the test — expect it to fail**

```bash
cd ai-agent
./mvnw test -Dtest=LangfuseSpanProcessorTest -q
```

Expected: COMPILATION ERROR — `LangfuseSpanProcessor` does not exist yet.

- [ ] **Step 3: Implement `LangfuseSpanProcessor`**

Use field injection (non-final fields) for `@ConfigProperty` — CDI injects these at startup. The static `buildProcessor` method separates construction logic so tests don't need a CDI container.

Create `ai-agent/src/main/java/dev/sitsko/ai/observability/LangfuseSpanProcessor.java`:

```java
package dev.sitsko.ai.observability;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class LangfuseSpanProcessor {

    @ConfigProperty(name = "langfuse.base-url", defaultValue = "http://localhost:3000")
    String langfuseBaseUrl;

    @ConfigProperty(name = "langfuse.public-key", defaultValue = "dummy")
    String publicKey;

    @ConfigProperty(name = "langfuse.secret-key", defaultValue = "dummy")
    String secretKey;

    @Produces
    @ApplicationScoped
    @Named("langfuse")
    public SpanProcessor langfuseSpanProcessor() {
        return buildProcessor(langfuseBaseUrl, publicKey, secretKey);
    }

    static SpanProcessor buildProcessor(String baseUrl, String publicKey, String secretKey) {
        String endpoint = baseUrl + "/api/public/otel/v1/traces";
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((publicKey + ":" + secretKey).getBytes(StandardCharsets.UTF_8));
        return BatchSpanProcessor.builder(
                OtlpHttpSpanExporter.builder()
                        .setEndpoint(endpoint)
                        .addHeader("Authorization", auth)
                        .build())
                .build();
    }
}
```

- [ ] **Step 4: Run the test — expect it to pass**

```bash
cd ai-agent
./mvnw test -Dtest=LangfuseSpanProcessorTest -q
```

Expected:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 5: Run the full test suite**

```bash
cd ai-agent
./mvnw test -q
```

Expected: BUILD SUCCESS — all existing tests still pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sitsko/ai/observability/LangfuseSpanProcessor.java \
        src/test/java/dev/sitsko/ai/observability/LangfuseSpanProcessorTest.java
git commit -m "feat: add LangfuseSpanProcessor — second OTLP exporter to Langfuse"
```

---

### Task 3: Agent call latency timers in MicrometerTokenExporter

**Files:**
- Modify: `ai-agent/src/main/java/dev/sitsko/ai/observability/MicrometerTokenExporter.java`
- Modify: `ai-agent/src/test/java/dev/sitsko/ai/observability/MicrometerTokenExporterTest.java`

**Interfaces:**
- Consumes:
  - `AiServiceStartedEvent` from `dev.langchain4j.observability.api.event` — has `invocationContext().invocationId()` (UUID)
  - `AiServiceResponseReceivedEvent` (already used) — has `invocationContext().invocationId()` (UUID, same correlation key as started event)
  - `@AiServiceSelector` qualifier from `io.quarkiverse.langchain4j.observability` (already imported)
- Produces: Micrometer `Timer` named `ai_agent.call.duration` tagged `model` + `agent`

**Key design:** A `ConcurrentHashMap<UUID, Instant>` stores start times keyed by `invocationId`. On `AiServiceStartedEvent`, store `Instant.now()`. On `AiServiceResponseReceivedEvent`, look up the start time, compute the duration, record it to the Timer, then remove the entry.

- [ ] **Step 1: Write the failing tests**

Add these test methods to `MicrometerTokenExporterTest`. The test file already has `setUp()` creating a `SimpleMeterRegistry`, `TokenMetricCollector`, and `MicrometerTokenExporter`. Add:

```java
// Add these imports at the top of the existing test file:
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

// Add these test methods:

@Test
void recordDuration_registersTimerForKey() {
    var key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");

    exporter.recordDuration(key, Duration.ofMillis(500));

    Timer timer = registry.find("ai_agent.call.duration")
            .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").timer();

    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
}

@Test
void recordDuration_accumulatesMultipleCalls() {
    var key = new ModelAgentKey("gpt-4o-mini", "ParserAgent");

    exporter.recordDuration(key, Duration.ofMillis(300));
    exporter.recordDuration(key, Duration.ofMillis(700));

    Timer timer = registry.find("ai_agent.call.duration")
            .tag("model", "gpt-4o-mini").tag("agent", "ParserAgent").timer();

    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(2);
    assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(1000.0);
}

@Test
void recordDuration_registersDistinctTimersPerKey() {
    exporter.recordDuration(new ModelAgentKey("gpt-4o-mini", "ParserAgent"), Duration.ofMillis(100));
    exporter.recordDuration(new ModelAgentKey("claude-sonnet-4-6", "ScheduleAgent"), Duration.ofMillis(200));

    assertThat(registry.find("ai_agent.call.duration").tag("agent", "ParserAgent").timer()).isNotNull();
    assertThat(registry.find("ai_agent.call.duration").tag("agent", "ScheduleAgent").timer()).isNotNull();
}
```

- [ ] **Step 2: Run the failing tests**

```bash
cd ai-agent
./mvnw test -Dtest=MicrometerTokenExporterTest -q
```

Expected: COMPILATION ERROR — `recordDuration` method does not exist yet.

- [ ] **Step 3: Implement latency tracking in `MicrometerTokenExporter`**

Replace the contents of `MicrometerTokenExporter.java` with:

```java
package dev.sitsko.ai.observability;

import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkiverse.langchain4j.observability.AiServiceSelector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Gauge;

@ApplicationScoped
@RequiredArgsConstructor
public class MicrometerTokenExporter {

    private final MeterRegistry registry;
    private final TokenMetricCollector collector;
    private final Set<ModelAgentKey> registeredKeys = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, Instant> startTimes = new ConcurrentHashMap<>();

    // --- Start event observers ---

    public void onParserStarted(@Observes @AiServiceSelector(dev.sitsko.ai.parser.ParserAgent.class) AiServiceStartedEvent event) {
        startTimes.put(event.invocationContext().invocationId(), Instant.now());
    }

    public void onScheduleStarted(@Observes @AiServiceSelector(dev.sitsko.ai.schedule.ScheduleAgent.class) AiServiceStartedEvent event) {
        startTimes.put(event.invocationContext().invocationId(), Instant.now());
    }

    public void onSecurityStarted(@Observes @AiServiceSelector(dev.sitsko.ai.security.SecurityAgent.class) AiServiceStartedEvent event) {
        startTimes.put(event.invocationContext().invocationId(), Instant.now());
    }

    public void onDataLeakStarted(@Observes @AiServiceSelector(dev.sitsko.ai.security.DataLeakAgent.class) AiServiceStartedEvent event) {
        startTimes.put(event.invocationContext().invocationId(), Instant.now());
    }

    // --- Response event observers ---

    public void onParserResponse(@Observes @AiServiceSelector(dev.sitsko.ai.parser.ParserAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("ParserAgent", event);
    }

    public void onScheduleResponse(@Observes @AiServiceSelector(dev.sitsko.ai.schedule.ScheduleAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("ScheduleAgent", event);
    }

    public void onSecurityResponse(@Observes @AiServiceSelector(dev.sitsko.ai.security.SecurityAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("SecurityAgent", event);
    }

    public void onDataLeakResponse(@Observes @AiServiceSelector(dev.sitsko.ai.security.DataLeakAgent.class) AiServiceResponseReceivedEvent event) {
        handleEvent("DataLeakAgent", event);
    }

    void record(ModelAgentKey key, TokenUsage usage) {
        collector.record(key, usage);
        if (registeredKeys.add(key)) {
            registerGauges(key);
        }
    }

    void recordDuration(ModelAgentKey key, Duration duration) {
        Timer.builder("ai_agent.call.duration")
                .tag("model", key.model())
                .tag("agent", key.agent())
                .register(registry)
                .record(duration);
    }

    private void handleEvent(String agentName, AiServiceResponseReceivedEvent event) {
        TokenUsage usage = event.response().tokenUsage();
        if (usage == null) return;
        String model = event.response().modelName();
        ModelAgentKey key = new ModelAgentKey(model, agentName);
        record(key, usage);

        Instant start = startTimes.remove(event.invocationContext().invocationId());
        if (start != null) {
            recordDuration(key, Duration.between(start, Instant.now()));
        }
    }

    private void registerGauges(ModelAgentKey key) {
        Gauge.builder("ai_agent.token.input", collector.getInputTokens(),
                        map -> map.getOrDefault(key, new AtomicInteger(0)).doubleValue())
                .tag("model", key.model())
                .tag("agent", key.agent())
                .register(registry);

        Gauge.builder("ai_agent.token.output", collector.getOutputTokens(),
                        map -> map.getOrDefault(key, new AtomicInteger(0)).doubleValue())
                .tag("model", key.model())
                .tag("agent", key.agent())
                .register(registry);
    }
}
```

- [ ] **Step 4: Run the new tests — expect them to pass**

```bash
cd ai-agent
./mvnw test -Dtest=MicrometerTokenExporterTest -q
```

Expected:
```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

(5 existing tests + 3 new timer tests)

- [ ] **Step 5: Run the full test suite**

```bash
cd ai-agent
./mvnw test -q
```

Expected: BUILD SUCCESS — all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/sitsko/ai/observability/MicrometerTokenExporter.java \
        src/test/java/dev/sitsko/ai/observability/MicrometerTokenExporterTest.java
git commit -m "feat: add ai_agent.call.duration timer to MicrometerTokenExporter"
```

---

### Task 4: Smoke test — verify traces appear in Langfuse

**Files:** No code changes — this task is manual verification.

- [ ] **Step 1: Ensure Langfuse is running**

```bash
cd ai-agent
docker compose ps
```

Expected: both `langfuse` and `postgres` containers are `Up`.

- [ ] **Step 2: Start Quarkus dev mode**

```bash
cd ai-agent
./mvnw quarkus:dev
```

Wait for `Listening on: http://0.0.0.0:<AI_APP_PORT>` in the output. The LGTM DevService also starts (look for Grafana/Tempo container startup).

- [ ] **Step 3: Send a booking request**

```bash
curl -s -X POST http://localhost:${AI_APP_PORT}/api/booking/proposal \
  -H "Content-Type: application/json" \
  -d '{"userPrompt": "I need to ship 2 containers from Shanghai to Rotterdam in July"}' \
  | jq .
```

Expected: JSON response with `voyageData` containing ranked voyages.

- [ ] **Step 4: Verify traces in Langfuse**

Open `http://localhost:3000` → log in → navigate to the `ai-agent` project → **Traces**.

Expected: a new trace entry for the booking request, containing child spans for `ParserAgent`, `SecurityAgent`, `ScheduleAgent`, and `DataLeakAgent` with token counts visible.

- [ ] **Step 5: Verify latency metrics in Grafana**

In the Quarkus dev console or LGTM Grafana UI (port shown in startup logs), run a Prometheus query:

```
ai_agent_call_duration_seconds_count
```

Expected: non-zero counts for each agent.

- [ ] **Step 6: Commit the updated spec**

```bash
git add docs/superpowers/specs/2026-06-30-langfuse-lgtm-metrics-design.md
git commit -m "docs: fix langfuse tracing property names in design spec"
```
