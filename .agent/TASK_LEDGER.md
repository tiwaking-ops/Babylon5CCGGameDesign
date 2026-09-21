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
| B5-0001 | DONE | Fix MainWindow init (pre-existing: `statusLabel`/`playCardButton` might not have been initialized, fails even on `-source 8`) | `b5ccg/src/b5ccg/ui/` | hermes-solar-pro4 | 2026-09-21: compile gate green at `-source 6`; also fixed HandPanel Consumer/method-ref, GameBoardPanel method-ref, MainWindow.stream in playSelected() |
| B5-0101 | DONE | Convert `model/` to Java 6 (lambdas, streams, method refs, `computeIfAbsent`, `@FunctionalInterface`) | `b5ccg/src/b5ccg/model/` | hermes-solar-pro4 | 2026-09-21: all 23 model files compile clean at `-source 6`; diamonds→explicit, streams→for-loops, `computeIfAbsent`/`putIfAbsent`/`getOrDefault`→manual, `@FunctionalInterface` removed, strings-in-switch→if-else |
| B5-0102 | DONE | Convert `engine/` + `ai/` to Java 6 (`try-with-resources` in DeckLoader, streams/lambdas in GameController/AIPlayer) | `b5ccg/src/b5ccg/engine/`, `b5ccg/src/b5ccg/ai/` | hermes-solar-pro4 | 2026-09-21: all engine/ai files Java 6-clean; try-with-resources→try/finally in DeckLoader; Consumer→GameStateCallback interface in GameController; streams→for-loops in GameController.getAI() + AIPlayer.leadingPlayer(); diamonds→explicit; getOrDefault→manual containsKey guards; AICard.buildCard() getOrDefault calls→own helper; strings-in-switch already fixed in AgendaCard (B5-0101) |
| B5-0103 | DONE | Convert `ui/` + `util/` + `Main.java` to Java 6 (lambdas, method refs, diamond) | `b5ccg/src/b5ccg/ui/`, `b5ccg/src/b5ccg/util/`, `b5ccg/src/b5ccg/Main.java` | hermes-solar-pro4 | 2026-09-21: full project compiles clean at `-source 6`; ui/ already clean from B5-0001; util/ImageCache.java: ConcurrentHashMap.computeIfAbsent+methodref→manual get/put, diamonds→explicit; Main.java: lambdas→anonymous Runnable (invokeLater, GameController callback, Thread launch), diamonds→explicit; Java 6 inner-class final-capture fix (fController); public GameStateCallback→own GameStateCallback.java file |

Rules: take the highest `OPEN` row with no live claim file. Fix B5-0001 first —
nothing compiles until it does. Reap stale claims (>30 min) only with a note here.
