---
document:
  title: "Task ledger (shared live files — claims are authority)"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
    - {name: "Buffy", version: "deepseek-v4-flash"}
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
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
|| B5-0201 | DONE | Headless smoke test: drive DeckLoader + GameState + one full AI round with no GUI; fail loudly on exception | `b5ccg/src/b5ccg/engine/` (new test harness file only, no game-logic changes) | hermes-solar-pro4 | 2026-09-21: PASS — `java -cp out b5ccg.engine.HeadlessSmokeTest` → exit 0; DeckLoader.loadBothSets() loaded 829 cards; 4 all-AI players built; one full round completed in ~19.3–20.7s across two runs; 32–34 AI actions, 37–39 UI callbacks, 66–68 log lines; all 4 AIPlayer.chooseAction() calls legal. Note: `compile.sh` has a Windows+MSYS path bug (pre-existing, works around with direct javac); `compile.bat` (native Windows gate) exits 0. Prior agent freebuff-01 added the file + compiled; hermes-solar-pro4 ran it, wrote report, closed governance. |
| B5-0202 | DONE | Audit `ai/` turn quality vs rulebook: report illegal or no-op AI moves, fix the smallest set | `b5ccg/src/b5ccg/ai/` | hermes-solar-pro4 | 2026-09-21: audit complete (5 findings); Fix #1 applied — `AIPlayer.buildLegalActions()` now skips `INITIATE_CONFLICT` generation when `state.getActiveConflict() != null`, enforcing the rulebook's "one conflict per turn" rule; compile + smoke test pass; 4 quality observations deferred (EASY pass bias, missing Build Influence action, no play-cost checks, static MEDIUM recruitment score — all require model/engine changes or are quality-only) |
| B5-0203 | OPEN | Rulebook-conformance audit of `model/` card effects: report-only, list each deviation with rulebook section | `b5ccg/src/b5ccg/model/` | none | — |

Rules: take the highest `OPEN` row with no live claim file. B5-0001–B5-0103 are
DONE and the `-source 6` gate is green — build on it, do not regress it.
Reap stale claims (>30 min) only with a note here. One writer per scope:
B5-0201, B5-0202, B5-0203 are in disjoint scopes and may run in parallel.
