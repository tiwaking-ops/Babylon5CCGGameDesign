---
document:
  title: "A sweep is a table, or it is nothing"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A sweep is a table, or it is nothing

**Source task:** B5-0683 (post-chain harness re-sweep). **Record:**
2026-09-27, Buffy (glm-5.3-flash).

## The lesson

The re-sweep row demands a "pass fail table naming every probe" — and the
demand is the point. A re-sweep after a change chain exists to answer two
questions the chain's own green runs cannot: did the new code perturb
*standalone* harnesses that RUN_TESTS never executes, and did a numeric
invariant (a designed band, a baseline count) drift? Both answers are only
useful as a per-probe table with actual numbers — "still green" is
unauditable, and an unnamed gap in the sweep is invisible to the next agent.

## The transferable rule

For any post-chain re-sweep:

1. **Enumerate the sweep set before running** (by header-tagged task id, not
   by memory), so a missing probe is visible in the table as an absence.
2. **Record the actual numbers** — check counts, rates, baselines — next to
   each PASS, with the prior reading beside it when one exists; a band check
   that passes at 0.42 today and 0.457 last month is a trend, not a constant.
3. **State the gate reading** when the row's gate names a row whose status
   changed shape (DONE-via-supersession), rather than silently assuming the
   condition is met.

## Anti-patterns this heads off

- Summarizing a sweep as "all pass" — an unverifiable claim that robs the
  next sweep of its comparison baseline.
- Skipping standalone probes because RUN_TESTS is green — the two harness
  sets overlap partially; the difference is exactly where regressions hide.
- Treating a numeric band as a boolean — the rate is the datum; the pass is
  derived.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0683.md`.
