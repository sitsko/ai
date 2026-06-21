# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-module Maven project showcasing **Quarkus + LangChain4J** AI patterns:

- **`ai-tool-calling/`** — OpenAI function calling, guardrails, conversation memory, observability
- **`ai-agent/`** — Declarative multi-agent workflow (`@SequenceAgent`) with multi-LLM support (OpenAI, Anthropic, Gemini), agentic security guardrails, and token observability

Domain: maritime container shipping (vessel schedules, booking proposals).

## Commands

Run from the module directory (`ai-tool-calling/` or `ai-agent/`):

```bash
# Dev mode with live reload
./mvnw quarkus:dev

# Build JVM JAR
./mvnw package

# Build native image
./mvnw package -Pnative

# Run all tests
./mvnw test

# Run single test class
./mvnw test -Dtest=MyTestClass

# Run integration tests
./mvnw verify
```

HTTP request examples are in `ai-tool-calling/script/http/`.

---

## Module: ai-tool-calling

### Setup

Create `ai-tool-calling/.env`:
```
AI_APP_PORT=9080
AI_MODEL_PROVIDER=openai
OPEN_AI_TOKEN=<your-openai-token>
```

Supports switching to Ollama via `AI_MODEL_PROVIDER=ollama`.

### Package Structure

```
me.sitsko.ai/
├── vessel/     # Maritime vessel domain — tool calling + guardrails demo
└── customer/   # Customer generation domain — memory + structured output demo
```

### LangChain4J Patterns Used

- **`@RegisterAiService`** — Interface-based AI service auto-implemented by LangChain4J
- **`@Tool`** on methods in `*Tool.java` classes — Functions the LLM can call (simulated external data)
- **`@SystemMessage` / `@UserMessage`** — Prompt templates on service methods
- **`@InputGuardrails` / `@OutputGuardrails`** — Validation before/after LLM calls
- **`@MemoryId`** — Per-session conversation memory (used in `CustomerAiService`)

### Key Design Decisions

- AI services are interfaces only; LangChain4J generates implementations at build time
- `*Tool.java` classes mock external service calls (vessel database)
- `@RunOnVirtualThread` + `@Timeout(60)` (SmallRye Fault Tolerance) handle AI call latency
- Provider switching (OpenAI ↔ Ollama) via `AI_MODEL_PROVIDER` env var

### REST Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/vessels/{owner}/heavy` | Largest vessel by owner (tool calling) |
| `GET /api/vessels/{owner}/count` | Count vessels by owner (tool calling) |
| `GET /api/vessels/forecast?years=N` | AI-generated forecast (with timeout) |
| `GET /api/lists/{listId}` | Generate 3 random persons (memory demo) |
| `GET /api/lists/{listId}/customers/{id}` | Retrieve person from memory by ID |

### Observability

Dev mode starts a local LGTM stack (Loki, Grafana, Tempo, Mimir) via `quarkus-observability-devservices-lgtm`. Prometheus metrics via Micrometer; endpoints annotated with `@Timed` / `@Counted`.

### Key Dependencies

- **Quarkus 3.23.4** with `quarkus-rest` + `quarkus-rest-jackson`
- **LangChain4J 1.1.0** (`quarkus-langchain4j-openai` / `quarkus-langchain4j-ollama`)
- **SmallRye Fault Tolerance** for `@Timeout`
- **Lombok** for constructor injection

---

## Module: ai-agent

### Setup

Create `ai-agent/.env`:
```
AI_APP_PORT=<port>
OPEN_AI_TOKEN=<openai-token>
ANTHROPIC_AI_TOKEN=<anthropic-token>
GOOGLE_AI_TOKEN=<google-token>
```

Switch providers in `application.properties` by changing `quarkus.langchain4j.chat-model.provider` to `openai`, `anthropic`, or `ai-gemini`.

Voyage data is read from `../data/voyages.csv` (relative to the module root).

### Package Structure

```
dev.sitsko.ai/
├── booking/        # REST endpoint + declarative agent workflow orchestration
├── parser/         # ParserAgent — extracts structured booking request from natural language
├── schedule/       # ScheduleAgent — queries voyage data and ranks matching voyages
├── security/       # SecurityAgent, InputGuardRailService, OutputGuardrailService, DataLeakAgent
├── observability/  # Token usage metrics (input/output per model call)
└── exception/      # Custom exceptions (ProhibitedContextException, PromptInjectionException, DataLeakException, VoyageDataSourceException)
```

### LangChain4J Agentic Patterns Used

- **`@SequenceAgent`** (`BookingWorkflow`) — declarative pipeline that chains `ParserAgent → ScheduleAgent`; output composed via `@Output` static method
- **`@Agent`** on interface methods — defines named agents with role descriptions; `outputKey` controls how results are passed between agents in the sequence
- **`@ToolBox`** (`ScheduleAgent`) — binds `ScheduleService` as a callable tool for the LLM
- **`@InputGuardrails`** (`ParserAgent`) — runs `InputGuardRailService` before the LLM call: deterministic keyword check (`SecurityService`) + non-deterministic injection score via `SecurityAgent` (threshold 0.7)
- **`@OutputGuardrails`** (`ScheduleAgent`) — runs `OutputGuardrailService` after the LLM call: data-leak score via `DataLeakAgent` (threshold 0.7)
- **`{current_date}`** in `@UserMessage` — built-in LangChain4J variable injected at call time

### Key Design Decisions

- All agent interfaces are LangChain4J-implemented; no manual CDI beans for agent logic
- Two-layer input security: deterministic (banned words) + non-deterministic (LLM injection scorer `SecurityAgent`)
- `@SequenceAgent` passes outputs between sub-agents by name via `outputKey`; the `@Output` static method assembles the final `BookingResponse` and includes the active LLM provider name from config
- Multi-LLM support via commented config blocks; switch provider by changing a single property
- `TokenMetricCollector` tracks cumulative input/output token counts per model, exposed and clearable via REST

### REST Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /api/booking/proposal` | Submit a natural-language booking request; returns ranked voyage proposals (600 s timeout) |
| `GET /api/metrics/tokens` | Cumulative token usage statistics (input + output) |
| `DELETE /api/metrics/tokens` | Reset token statistics |

### Key Dependencies

- **Quarkus 3.36.2** with `quarkus-rest` + `quarkus-rest-jackson`
- **`quarkus-langchain4j-agentic`** — `@SequenceAgent`, `@Agent`, `@Output`
- **`quarkus-langchain4j-openai`** / **`quarkus-langchain4j-anthropic`** / **`quarkus-langchain4j-ai-gemini`** — multi-provider LLM support
- **SmallRye Fault Tolerance** for `@Timeout` on the booking endpoint
- **Lombok** for constructor injection

Requires Java 21.
