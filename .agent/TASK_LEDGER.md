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
| B5-0201 | DONE | Headless smoke test: drive DeckLoader + GameState + one full AI round with no GUI; fail loudly on exception | `b5ccg/src/b5ccg/engine/` (new test harness file only, no game-logic changes) | hermes-solar-pro4 | 2026-09-21: PASS — `java -cp out b5ccg.engine.HeadlessSmokeTest` → exit 0; DeckLoader.loadBothSets() loaded 829 cards; 4 all-AI players built; one full round completed in ~19.3–20.7s across two runs; 32–34 AI actions, 37–39 UI callbacks, 66–68 log lines; all 4 AIPlayer.chooseAction() calls legal. Note: `compile.sh` has a Windows+MSYS path bug (pre-existing, works around with direct javac); `compile.bat` (native Windows gate) exits 0. Prior agent freebuff-01 added the file + compiled; hermes-solar-pro4 ran it, wrote report, closed governance. |
| B5-0202 | DONE | Audit `ai/` turn quality vs rulebook: report illegal or no-op AI moves, fix the smallest set | `b5ccg/src/b5ccg/ai/` | hermes-solar-pro4 | 2026-09-21: audit complete (7 findings). Fix #1 applied: AIPlayer.buildLegalActions() skips INITIATE_CONFLICT generation when state.getActiveConflict() != null, enforcing rulebook §IV one-conflict-per-turn. compile + smoke test pass (exit 0, ~19.3s round, 32 AI actions, 4/4 legal). 6 findings deferred: missing Build Influence (high), engine re-initiation gap (high), no influence-cost scoring (med), no IC promotion (med), buildLegalActions skips isPassed/actionsLeft (low), EASY 30% pass bias (low/design). |
| B5-0203 | DONE | Rulebook-conformance audit of `model/` card effects: report-only, list each deviation with rulebook section | `b5ccg/src/b5ccg/model/` | freebuff-01 | 2026-09-21: report-only audit complete, no model/ file modified; gate green (34 files, `-source 6`), smoke exit 0. 8 areas conformant; 15 deviations (D1–D15) logged with rulebook sections + smallest-fix suggestions in `.agent/REPORTS/2026-09-21-freebuff-01-B5-0203.md`. Key: aftermath Won/Lost from wrong player's perspective (engine site), PSI-conflict type hole in AftermathCard.isEligible, Leadership double-count in Player.conflictTotal, deck-out penalty missing in Player.drawCards, standard-victory tie + major-agenda rules ignored in AgendaCard.isConditionMet, NON_ALIGNED universally playable in Faction.isPlayableBy, CardEffect interface dead (no implementers/callers) — effect application is the root engine gap; recommend follow-up task before fixing model-side items. |

| B5-0301 | OPEN | Implement Build Influence action (rulebook §V): BUILD_INFLUENCE GameAction type + factory, canBuildInfluence + processAction branch, AI offers it at rating ≤ 9 | `b5ccg/src/b5ccg/model/`, `b5ccg/src/b5ccg/engine/`, `b5ccg/src/b5ccg/ai/` | none | — |
| B5-0302 | OPEN | Enforce one-conflict-per-faction-per-turn in engine (B5-0202 Finding 5): track conflicts initiated this turn per player in GameState, check in canInitiateConflict | `b5ccg/src/b5ccg/engine/`, `b5ccg/src/b5ccg/model/` | none | — |
| B5-0303 | OPEN | Aftermath Won/Lost from initiator's perspective (D1): pass won=(winner==initiator) at play site | `b5ccg/src/b5ccg/engine/` | none | — |
| B5-0304 | OPEN | Deck-out penalty (D8): empty deck → discard non-ambassador Inner Circle character, else signal forfeit for victory check | `b5ccg/src/b5ccg/model/` (+ `b5ccg/src/b5ccg/engine/` victory check if needed) | none | — |
| B5-0305 | OPEN | Model fixes batch: PSI aftermath-type branch in isEligible (D3), NON_ALIGNED as normal race in isPlayableBy (D13), standard-victory strictly-greatest + major-agenda block (D12) | `b5ccg/src/b5ccg/model/` (+ `b5ccg/src/b5ccg/engine/` checkVictory if needed) | none | — |
| B5-0306 | OPEN | Fix compile.sh Windows+MSYS path bug (pre-existing; agents currently work around with direct javac) | `b5ccg/compile.sh` | none | — |

Rules: take the highest `OPEN` row with no live claim file. B5-0001–B5-0203 are
DONE and the `-source 6` gate is green — build on it, do not regress it.
Reap stale claims (>30 min) only with a note here. One writer per scope at a
time: B5-0301–B5-0305 overlap in engine//model/, so second agent must pick a
task whose scope has no live claim; B5-0306 (script only) always runs parallel.
