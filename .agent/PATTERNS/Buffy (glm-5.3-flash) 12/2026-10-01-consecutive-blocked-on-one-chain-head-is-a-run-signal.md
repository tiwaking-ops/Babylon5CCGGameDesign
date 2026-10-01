---
document:
  title: "Consecutive BLOCKED results on one gate chain head are a run-level signal, not bad luck"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: consecutive BLOCKED results on one chain head are a run-level signal

**Task:** B5-1335 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

Three consecutive BLOCKED results this run (B5-1333, B5-1433, B5-1335) all
traced to the *same* unresolved chain head: B5-1045 BLOCKED → B5-1047 (whose
claim file itself carries a ~29h/future-skew history) → every downstream
"verify the exclusion" row. The loop's stop rule ("3 consecutive BLOCKED")
exists precisely so a headless runner surfaces a stuck subgraph instead of
shuffling between its leaves forever. Hitting the threshold is not a failure
of the run; ignoring what the three results have in common would be.

## Practice

1. When a BLOCKED result lands, note *which* gate row failed; when a second
   lands on the same name, treat that row as the run's actual blocker; a third
   confirms it — stop and report the chain, not just the item.
2. In each BLOCKED close-out, carry forward only measured corrections for the
   eventual re-run (here: fixed side is 50 not 60; the stderr "fixed ids
   missing" line is the proven instrument), so the row's owner inherits a
   sharper probe, not just a red stamp.
3. Distinguish "cannot run the probe" (BLOCKED) from "ran the probe and it
   failed" (regressed): probing a feature that was never built produces
   verdicts about a fiction.

## Related

* Supersedes nothing; sixth pattern in this namespace.
* Traces to: B5-1335 report; B5-1333 and B5-1433 (the other two results on the
  same chain); B5-1167/B5-1145 precedents (queue offer does not waive a prose
  gate); the B5-1047/B5-1045/B5-1043 family this run left for its owners.
