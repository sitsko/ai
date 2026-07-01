---
name: flow-diagrams
description: Use when creating or updating a Mermaid flow diagram (.mmd) documenting a runtime call flow (agent workflow, REST endpoint, guardrail pipeline) in this repo — covers file location, dark-theme classDef styling, and keeping diagrams in sync with the Java classes they depict
---

# Flow Diagrams (Mermaid)

## Overview

Project convention for `.mmd` flow diagrams that document runtime call flow in this repo (`ai-tool-calling`, `ai-agent`). For general Mermaid syntax — node shapes, edge types, the dark-theme init directive — see the `generating-mermaid-diagrams` skill; this skill covers only what's specific to this project.

## When to Use

- Documenting a new agent workflow, REST endpoint, or guardrail pipeline as a flow diagram
- Updating an existing flow diagram after the source classes it depicts changed

## Location & Naming

| What | Convention | Example |
|---|---|---|
| Folder | `docs/architecture/diagram/flow/` | — |
| File name | `{kebab-case-process-name}.mmd` | `booking-proposal.mmd` |
| One file per process | Don't merge unrelated flows into one file | ✅ |

## Required Header

Every file starts with the dark-theme init directive, then a `sources` comment block — the fully-qualified Java classes the diagram is derived from. This is the sync contract: when a listed class is renamed, moved, or its logic changes, the diagram must be updated (see below).

```
%%{init: {'theme': 'dark'}}%%
%% sources:
%%   dev.sitsko.ai.booking.BookingResource
%%   dev.sitsko.ai.booking.BookingWorkflow
flowchart TD
```

## Styling — `classDef`/`class`, not per-node `style`

Reuse the same four semantic classes across every diagram in this repo instead of one-off `style` lines per node:

```
classDef stage fill:#1e2a3a,stroke:#5b8dbf,color:#e6edf3;
classDef error fill:#3a1e22,stroke:#d16969,color:#f5c2c7;
classDef success fill:#1e3a24,stroke:#5bbf7a,color:#c6f6d5;
classDef metrics fill:#2a1e3a,stroke:#9b7ed6,color:#e0d6f5;

class NodeA,NodeB stage;
class ErrorNode error;
```

| Class | Use for |
|---|---|
| `stage` | Normal processing step (service/agent call, REST entry point) |
| `error` | Exception type or failure path |
| `success` | Terminal success response |
| `metrics` | Side-channel observability node, connected via dotted edges (`-.->`) |

## Keeping Diagrams in Sync with Code

| Code change | Diagram update |
|---|---|
| Class renamed/moved | Update every node label referencing it + the `sources` list. Keep node IDs unchanged. |
| Method renamed | Update only the label text of the affected node. |
| Branch added/removed (`if`, `switch`) | Add/remove the diamond decision node and its edges; add/remove its `class` assignment. |
| New service/agent call added | Add a node with the next unused ID, wire the edge, assign it a `class`. |
| New lifecycle phase/stage | Wrap it in a new `subgraph`; add its sources to the header. |
| Class deleted | Remove its nodes, reconnect or drop the edges, drop it from `sources` and any `class` line. |

## What NOT to Show

- Constructor injection wiring — show runtime call flow only
- Lombok-generated getters/setters
- Exception stack traces — collapse to one node if the error path matters
- Private helper methods with no branching logic — fold into the caller node
- Framework internals (CDI proxies, Panache SQL generation)

## Example

`docs/architecture/diagram/flow/booking-proposal.mmd` is the canonical example — the `ai-agent` booking-proposal request flow, including guardrail branches and the token-metrics side channel, styled per the conventions above.

## Checklist Before Committing

- [ ] `sources` header lists every Java class visible in the diagram, and only those
- [ ] Every node label matches the current class/method name in code
- [ ] All nodes use `class` assignments from the four semantic `classDef`s — no orphaned per-node `style` lines
- [ ] File is named and located per the table above
