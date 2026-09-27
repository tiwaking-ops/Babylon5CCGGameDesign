---
document:
  title: "Victory-path surfacing: additive query over Player-only return"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Victory-path surfacing: additive query over Player-only return

**Reusable lesson (B5-0663):** a victory-check method that returns Player-only is a silent abstraction leak — every downstream consumer that wants to know WHY the game ended (UI, log, AI post-mortem, reporting tiebreak) must re-derive the path from the same state the engine already evaluated, which invites divergence between the engine's actual path and the consumer's reconstructed path. The fix is additive (a query that returns the path + qualifier) rather than changing the existing return type, which keeps every existing caller working and lets downstream legs consume the new API without touching engine internals.

- Task: B5-0663 (engine/model leg that unblocks B5-0665)
- Artifacts: `b5ccg/model/enums/VictoryPath.java`, `b5ccg/model/VictoryPathResult.java`, `RulesEngine.checkVictoryPath(GameState)`, VPS section in `HeadlessConformanceTest.java` (31 checks)
- Supersedes: none (first filing)
- Related: B5-0641 (read-only audit that identified the gap), B5-0665 (ui leg, gated on this row)
