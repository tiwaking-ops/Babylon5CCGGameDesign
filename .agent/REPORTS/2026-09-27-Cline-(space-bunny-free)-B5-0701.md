---
document:
  title: "B5-0701 — UI surrender plus computed-Power readout"
  status: "Report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0701 — UI surrender + Power readout

**Author:** Cline (space-bunny-free) · **Date:** 2026-09-27 · **Status:** DONE

## Summary

Two engine capabilities that already existed and the UI never showed. This row
surfaces landed law; it invents none. Scope: `b5ccg/src/b5ccg/ui/` only.

## Grounding, measured not cited

| Query over `b5ccg/src/b5ccg/ui/` | Hits |
|---|---|
| `surrender` | **0** |
| `getPower` | **0** |

Meanwhile, already landed before this task:

- `GameAction.surrender(Player)` — `GameAction.java:183`
- `RulesEngine.canSurrender(Player, Player, GameState)` — `RulesEngine.java:821`
- `Player.getPower()` / `getPowerBonusTotal()` — B5-0677

## 1. Surrender readout

Target-selector + commit button + hint label, in the toolbar beside the existing
B5-0407-style target+button pairs.

**Target-first is forced by the mechanic.** Surrender needs a target player, so a
bare button would have had nothing legal to submit.

**The engine is the only authority.** `refreshSurrenderControl` calls
`rules.canSurrender` once per candidate and renders the verdict — no part of the
legality rule is re-derived. The commit handler re-checks at the submit point,
exactly as sponsor/promote/build-influence do, so a stale enabled button cannot
submit an illegal action. This is the **B5-0423** lesson: the recorded cause of
that fix was a hand-rolled partial predicate drifting from the engine and
disabling a *legal* move.

**An empty list IS the "unavailable" signal**, so the readout cannot show a
stale "available". Empty → `"(surrender unavailable)"` + disabled.

**B5-0452 guard applied**: repopulation runs under `surrenderSelectorPopulating`,
because a `JComboBox` auto-selects its first item and fires the listener — which
is exactly how B5-0452 set a target with no user action.

**Direction stated explicitly.** The readout says *"Delenn gains +3 inf"*, not
"you gain 3" — rulebook :817 grants the influence to the **target** while the
surrendering player goes out of the game. A readout that got that backwards
would be worse than no readout. The tooltip shows the target's projected total.

## 2. Power readout (`GameBoardPanel.drawZone`)

Shown **only when `getPower() != getInfluence()`**, so a board with no
Power-bearing card in play carries no redundant number and the reader is never
asked which of two equal figures is real. When it differs, the label decomposes
it: `Power: N (inf M ± K)`.

Two placement bugs caught by reading surrounding code rather than assuming:

1. The left column below `y+58` is the **ambassador mini-card**. An initial
   left-aligned draw at `y+61` would have overlapped it → moved to the right of
   the name row.
2. A POWER bonus may be **negative**; `"+ " + negative` prints a double sign →
   explicit sign + `Math.abs`.

## Verification (exact working tree)

| Gate | Result |
|---|---|
| `compile.bat` `-source 6`, JDK 1.8.0_292 | **green** |
| `HeadlessConformanceTest` | **643/643 PASS** (incl. SUR, PWR, CWR) |
| `HeadlessSmokeTest` | **PASS** |
| `MainWindowAttackControlTest` | **10/10 PASS** |
| Java 6 grep on added lines | clean\* |
| Duplicate-ID census | empty |
| B5-0701 row | `pipeCount 7` / `doubleLead no` |

\* Exactly one hit, a `->` inside a tooltip **string literal**, not a language
construct.

`java.util.List`/`ArrayList` fully qualified, matching the file's existing
style. `src-java8-archive/` untouched.

## Scope held

`GameBoardPanel.java` (+18), `MainWindow.java` (+164), nothing else in
`b5ccg/src`. No engine/model/ai edit, no resources byte. The **suite file was
deliberately not edited** — this row makes no suite claim, so the existing
SUR/PWR sections stand rather than being extended by a writer who does not own
that file. No external library, human gate untouched. No commit.

## Reusable lesson

**Measure the gap by searching the layer you are allowed to touch** — before
writing a "missing feature", grep the layer you may edit for the capability's
name; zero hits there plus a landed engine API is the signature of an
unsurfaced capability, and it tells you the work is wiring, not design.
