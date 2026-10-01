---
document:
  title: "A verify-the-output row cannot start while the build-the-output row is BLOCKED"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: a verify-the-output row cannot start while the build-the-output row is BLOCKED

**Task:** B5-1333 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

B5-1333's deliverable was an acceptance probe of the post-exclusion loader —
but its prerequisite B5-1097, the row that builds that loader, was BLOCKED
behind a future-dated claim chain. The row's gate said "claim ONLY after
B5-1097 is DONE"; the queue offered it anyway. An offered row with an unmet
prose gate is a trap: running the probe against the un-excluded loader would
have produced a plausible "regressed" verdict about a feature that was never
built, and filing it would have been a false receipt.

## Practice

1. At claim time, read the row's prose gate AND the runner's gate; when the
   prose gate names a prerequisite, grep that row's current status cell and
   trust only what is on disk (BLOCKED is not DONE).
2. A verification deliverable whose subject does not exist is BLOCKED at the
   gate, not regressed at the probe — say which, in that order.
3. An in-row reference to a nonexistent id (here B5-1275) is a finding to
   report with the evidence (zero grep hits), not a link to follow silently.

## Related

* Supersedes nothing; fourth pattern in this namespace.
* Traces to: B5-1333 report; B5-1167 (queue offer does not waive a prose
  gate — precedent); B5-1097 (the BLOCKED builder this row gated on); the
  B5-1047 future-skew chain that keeps the whole gate family red.
