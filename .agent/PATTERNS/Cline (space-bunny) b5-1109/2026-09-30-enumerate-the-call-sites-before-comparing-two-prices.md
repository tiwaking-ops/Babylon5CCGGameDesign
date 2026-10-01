---
document:
  title: "Enumerate the call sites before comparing two prices"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Enumerate the call sites before comparing two prices

**Reusable lesson.** A gate that nothing calls is not a mispriced gate — it is a
gate-shaped hole. Measure the call sites before comparing prices: enumerate who
invokes the predicate, because a divergence "between a gate and a charge" is only
meaningful once both are on the same path.

B5-1109 asked whether `canPlayCard` (raw `card.getCost()`) and the
`RECRUIT_CHARACTER` charge (`sponsorCost`) read two different costs for the same
card. Both prices were real and both were named in the row. But `canPlayCard` had
**zero production callers** — only the conformance suite and a headless probe —
so there was no path on which the two could ever disagree. The row's own fallback
hypothesis ("a gate that does not cover the path it appears to cover") was the
one that held.

The same pass found the reachable defect one level over, on a path the row never
named: the generic `PLAY_CARD` route charged raw cost and *discarded* a character
instead of seating it.

**Why the ordering matters.** Comparing two numbers is cheap and feels rigorous.
Grepping the two definitions is also cheap. Neither one can distinguish "these
disagree" from "these never meet". The call-site census is the only step that
separates them, and it is the step a report that leads with a table of measured
prices tends to skip — the table looks like the evidence, so the question of
whether the columns are ever compared on one line stops being asked.

**How to apply it.** Before writing "X and Y disagree", grep both symbols across
`src/` and sort the hits into production versus test/harness. If one side has no
production hit, the finding is not a price disagreement; it is an unreachable or
unwired gate, and the report should say so in its first paragraph rather than
burying it under measurements.

First instance: `.agent/REPORTS/2026-09-30-Cline (space-bunny) b5-1109-B5-1109.md`.
