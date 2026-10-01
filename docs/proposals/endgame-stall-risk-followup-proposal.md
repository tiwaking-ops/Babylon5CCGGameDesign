---
document:
  title: "Endgame stall-risk follow-up: do surrender, computed Power and Civil War change the 20-20 tie?"
  status: "Proposal"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Endgame stall-risk follow-up (B5-0705)

**Proposal only. No `b5ccg/src` or `b5ccg/resources` file was touched.**
Toolchain measured: `javac 1.8.0_292`. Date: 2026-09-27.

Follow-up to `docs/proposals/tiebreak-agenda-victory-design-proposal.md`
(B5-0332), which sized the 20–20 stall when the only win paths were standard
and agenda. Three mechanics have landed since: **unconditional surrender**
(B5-0661), the **computed Power seam** (B5-0677) and **Civil War** (B5-0691).
This document re-measures rather than re-derives: every premise below was read
off the current tree, not quoted from the earlier design.

## 1. The stall, restated against the code as it is now

A 20–20 tie still produces no winner, and the reason is unchanged in kind from
B5-0332. Measured on `RulesEngine` today:

| Path | Predicate | Tie behaviour |
|---|---|---|
| Last standing | `remaining == 1` | n/a |
| Station condition 2 | `stationVictory` → `strictlyLeads(state, p, true)` | tie crowns nobody |
| Agenda condition | `agenda.isConditionMet(...)` | `INFLUENCE_20` uses `>=`, so it **does** break ties |
| Major | `majorVictory` — every other player `<= p - 10` | a 20–20 tie fails the 10-point margin |
| Standard | `standardVictory` → `strictlyLeads(state, p, false)` | tie crowns nobody |

So the residual stall is the same shape as before: **two or more players at 20+
with no satisfied agenda win and no 10-point margin.** B5-0332 identified this
as rulebook-faithful (there is no tiebreak rule in the rulebook) and that
reading is unchanged — nothing here proposes overriding the rulebook.

## 2. Does the new mechanics change the probability? Measured, not assumed.

**Unconditional surrender (B5-0661): weakly anti-stall, and not a tiebreak.**
`executeSurrender` sets `hasSurrendered` and grants the target +3 influence.
Two consequences for the stall, both measured from the predicates above:

- It **removes a player from every comparison** — `strictlyLeads` (:802) and
  `majorVictory` (:791) both skip `q.hasSurrendered()`. A 20–20 tie broken by
  one player surrendering is therefore *not* crowned: the survivor is now
  strictly leading and wins on Standard or Major. Surrender is a genuine
  tie-escape hatch.
- But it is **not reachable at 20–20 by default**. `canSurrender` (:822)
  requires `GamePhase.DRAW`, an ambassador in play, and a faction the player is
  **at war** with. A 20–20 endgame standoff is typically not a war state, so the
  hatch is usually closed exactly when the stall is live. It should not be
  counted as an anti-stall mechanism for this purpose.

**Computed Power (B5-0677): no effect on the stall today, and this is the
load-bearing finding.** `getPower()` is defined as
`getInfluence() + POWER-tagged StatBonus total`, but the call-site audit over
production `engine/` and `ai/` returns exactly **one** hit:

```
RulesEngine.java:922   return target.getPower() >= target.getInfluence();
```

That is the B5-0667 negative-Power protection gate (`canAffectTarget`), not a
victory path. **No victory predicate reads `getPower()`.** `standardVictory`
(:771), `majorVictory` (:788) and `checkVictoryPath` (:653) all read
`getInfluence()`. So the rulebook's own wording — Major Victory is "20 **Power**"
(:182) — is still evaluated on influence, and since no card in either set
carries a POWER stat the two are currently equal by construction. **Computed
Power therefore cannot change the stall probability today, and will not until a
Power-bearing card exists.** This is the honest answer to the row's question, and
it is a null result worth recording rather than a hedge.


## 3. Which B5-0332 option still holds

Re-measured against the tree:

- **Option A (station condition 2) — LANDED and holding.** `state.getStation()`
  exists, `Babylon5Station.CONDITION_2_THRESHOLD = 20`, and `stationVictory` is
  path 2 in `checkVictory`. It still crowns nobody on a tie (`strictlyLeads`),
  so it does **not** solve the 20–20 case; it solves the *single leader at
  station 20+* case, which is a different gap.
- **Option B (agenda points) — still on the table, still data-gated.** The
  `INFLUENCE_20` agenda already breaks ties via `>=`. Extending the vocabulary
  is a **card-data** change and is explicitly out of scope here.
- **Option C (reporting-only tiebreak) — still the only tiebreak-shaped
  option, and still a human ruling.** This document does not make that call; it
  records that the question is unresolved and that the B5-0332 sequencing
  (C → A → B) has effectively been overtaken by A landing first.

**Net: the 20–20 stall is still unmitigated.** Option A closed a different gap.

## 4. What evidence would settle it

A **synthetic-fixture** probe, not a soak, because the question is whether a
20–20 multi-way tie can *persist* to a natural round cap, and that is a
predicate question:

1. Construct 4 players, no agendas, station below 20, not at war.
2. Drive all four to exactly 20 influence.
3. Assert `checkVictory` returns `null` **and** that no further scoring can
   distinguish them — i.e. the state is terminal-but-unresolvable.
4. Then re-run with one player holding a `>= 20` agenda, and with one player
   surrendered, asserting each *does* resolve.

That probe is implementable with no card-data change and no new mechanic, which
is what makes it the right next slice rather than another soak. The soak
question — how *often* the tie occurs in real play — stays open and is a
separate, human-scheduled measurement.

## 5. Proposed slice (at most one)

**Add a 4-assertion synthetic stall probe to the conformance suite.** No engine
change, no model change, no card data. It pins the current behaviour so that any
future tiebreak work has a red-to-green target, and it settles the
"is 20–20 terminal" half of the question on synthetic fixtures.

**Explicitly ruled out, and why:**

- **Any card-data change** (new POWER-bearing cards, new agenda win conditions) —
  ruled out by the row, and independently it is human-gated data work.
- **Switching victory predicates to `getPower()`** — not merely out of scope but
  *premature*: with zero POWER sources it is a no-op refactor today, and doing
  it before a Power-bearing card exists would be the exact "half-applied split"
  the B5-0667 ruling warns against.
- **Any tiebreak rule** — that is Option C and a human decision.

## Supersedes

Nothing. This document re-measures the B5-0332 design against landed mechanics;
it does not replace `tiebreak-agenda-victory-design-proposal.md`, which remains
the record of the original options.

**Civil War (B5-0691): narrows the field, which is indirectly anti-stall.**
Civil War splits a race into factions; surrendered players and forfeited
players drop out of comparisons, and a race reduced to one active faction exits
the state. Fewer active players means fewer simultaneous 20+ candidates, which
shrinks the tie window. The magnitude is **not quantified here** — it would
require a soak, which this docs-only row does not run.
