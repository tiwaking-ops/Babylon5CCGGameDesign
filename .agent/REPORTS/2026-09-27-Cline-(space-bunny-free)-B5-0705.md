---
document:
  title: "B5-0705 — endgame stall-risk follow-up proposal"
  status: "Report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0705 — endgame stall-risk follow-up

**Author:** Cline (space-bunny-free) · **Date:** 2026-09-27 · **Status:** DONE

Deliverable: `docs/proposals/endgame-stall-risk-followup-proposal.md` (145 lines).
**No `b5ccg/src` or `b5ccg/resources` byte touched.**

## The headline is a negative result

The row asked whether unconditional surrender, the computed Power seam and
Civil War change the 20–20 tie stall probability. Answer: **surrender
conditionally, Civil War indirectly, and computed Power not at all.**

## Computed Power cannot change the stall — today

A call-site audit over **production** `engine/` and `ai/` (tests and probes
excluded) for `getPower()` returns **exactly one** hit:

```
RulesEngine.java:922   return target.getPower() >= target.getInfluence();
```

That is the B5-0667 negative-Power **protection gate**, not a victory path.
`standardVictory` (:771), `majorVictory` (:788) and `checkVictoryPath` (:653)
all read `getInfluence()`. The rulebook's "20 **Power**" (:182) is still
evaluated on influence, and with zero POWER sources in either card set the two
are equal by construction.

**Consequence, and it drove a ruling:** switching the victory predicates to
`getPower()` would be a **no-op refactor today** — exactly the "half-applied
split" the B5-0667 ruling warns against. Explicitly ruled out, not merely out of
scope.

## Surrender: measured both ways

- **It does escape a tie.** `strictlyLeads` (:802) and `majorVictory` (:791)
  both skip `q.hasSurrendered()`, so a 20–20 tie broken by one surrender leaves
  the survivor strictly leading and crowned.
- **But it is usually unreachable in a 20–20 standoff.** `canSurrender` (:822)
  requires `GamePhase.DRAW`, an ambassador in play, and a faction you are **at
  war** with. A 20–20 endgame standoff is typically not a war state — the hatch
  is shut exactly when the stall is live. Not counted as anti-stall.

## Civil War: indirect, not quantified

Splits races into factions; surrendered/forfeited players drop out of
comparisons. Fewer simultaneous 20+ candidates narrows the tie window.
Magnitude **not quantified** — that needs a soak, which this docs-only row does
not run.

## Option status, re-measured

| Option | Status |
|---|---|
| A — station condition 2 | **LANDED** (`getStation()`, threshold 20, path 2) — but routes through `strictlyLeads`, so it crowns nobody on a tie. Closed a *different* gap. |
| B — agenda points | Data-gated; `INFLUENCE_20` already breaks ties via `>=` |
| C — reporting-only tiebreak | **Human ruling. Not decided here.** |

**Net: the 20–20 stall is still unmitigated.** B5-0332's sequencing (C→A→B) was
overtaken by A landing first. Its reading that the tie is rulebook-faithful is
unchanged.

## The one proposed slice

A **4-assertion synthetic-fixture probe** — four players at exactly 20
influence, no agendas, station below 20, not at war; assert `checkVictory`
returns null and the state is terminal-but-unresolvable; then re-run with a
`>= 20` agenda and with one surrendered player, asserting each **does** resolve.

A probe rather than a soak because *"is 20–20 terminal"* is a predicate question
answerable on fixtures. *"How often does it happen"* stays open as separate,
human-scheduled measurement.

**Ruled out:** card-data changes (row boundary + human-gated); switching victory
predicates to `getPower()` (premature no-op); any tiebreak rule (Option C).

## Scope held

`git diff --stat -- b5ccg/` shows only the two `ui/` files from the earlier
B5-0701 task, unchanged here. No compile required or run — no Java touched.

## Reusable lesson

**A null result is a result — record it as one.** When measurement shows a
landed feature cannot affect the thing it was expected to affect, say so
plainly and explain the mechanism; a hedged "may help" seeds a follow-up row
that re-derives the same null.
