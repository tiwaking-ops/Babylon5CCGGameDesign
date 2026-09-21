---
document:
  title: "Task ledger (shared live files — claims are authority)"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# TASK_LEDGER

One writer per task. Claim via `.agent/CLAIMS/<task-id>.json` before editing
(see `00_BOOT.md`). Status: `OPEN` / `CLAIMED` / `DONE` / `BLOCKED`.

| ID | Status | Task | Scope | Claim | Verified |
|---|---|---|---|---|---|
| B5-0001 | OPEN | Fix MainWindow init (pre-existing: `statusLabel`/`playCardButton` might not have been initialized, fails even on `-source 8`) | `b5ccg/src/b5ccg/ui/` | none | — |
| B5-0101 | OPEN | Convert `model/` to Java 6 (lambdas, streams, method refs, `computeIfAbsent`, `@FunctionalInterface`) | `b5ccg/src/b5ccg/model/` | none | — |
| B5-0102 | OPEN | Convert `engine/` + `ai/` to Java 6 (`try-with-resources` in DeckLoader, streams/lambdas in GameController/AIPlayer) | `b5ccg/src/b5ccg/engine/`, `b5ccg/src/b5ccg/ai/` | none | — |
| B5-0103 | OPEN | Convert `ui/` + `util/` + `Main.java` to Java 6 (lambdas, method refs, diamond) | `b5ccg/src/b5ccg/ui/`, `b5ccg/src/b5ccg/util/`, `b5ccg/src/b5ccg/Main.java` | none | — |

Rules: take the highest `OPEN` row with no live claim file. Fix B5-0001 first —
nothing compiles until it does. Reap stale claims (>30 min) only with a note here.
