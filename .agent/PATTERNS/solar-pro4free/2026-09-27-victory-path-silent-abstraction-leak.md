---
document:
  title: "Victory-path surfacing silent abstraction leak"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Victory-path surfacing silent abstraction leak

**Reusable lesson:** a `checkVictory` that returns `Player`-only is a silent abstraction leak —
every downstream consumer that wants to know WHY the game ended (UI, log, AI post-mortem,
reporting tiebreak) must re-derive the path from the same state the engine already evaluated,
which invites divergence between the engine's actual path and the consumer's reconstructed path.

**Context:** B5-0641 audit of `RulesEngine.checkVictory` (returns `Player` only) and
`MainWindow.java:1432` (game-over status shows winner name only). Five distinct victory paths
(last-standing, station condition 2, agenda condition, Major Victory, Standard Victory) all render
identically to the player. The engine computed the path internally but never exposed it.

**Observation:** the engine's `checkVictory` evaluated the path — it walked the per-path checks in
order and returned the first match — but its return type (`Player`) discarded that information. The
UI then had no way to render a path label without re-deriving it from the winner's state, which is
exactly the divergence risk (the engine's ordering, eligibility rules, and Shadow-War guards are the
authoritative path; a UI-side reconstruction could disagree).

**Applies to:** any engine query that answers a question with a scalar (winner, selected card, final
state) and discards the reasoning path that got there, when downstream consumers need the reasoning
for display, audit, or decision-making.

**Avoid:** returning the bare result and expecting consumers to reconstruct the path from the same
inputs the engine already saw. Expose the path as a structured label, enum, or value object alongside
the result — even if today every consumer only renders the label, the structure is what keeps them
from diverging.

**Corroborating record:** `.agent/REPORTS/2026-09-27-solar-pro4-free-B5-0641.md`
