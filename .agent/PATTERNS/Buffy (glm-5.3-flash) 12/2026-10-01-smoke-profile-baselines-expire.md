---
document:
  title: "Smoke-profile baselines expire — attribute a profile to the hunks that produced it"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: smoke-profile baselines expire

**Task:** B5-1419 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

A smoke-profile baseline is only as current as the offer hunks it was measured
against. The repo's standing "8 actions / 13 callbacks" profile was recorded
when the B5-0202c early-return hunk lived in `AIPlayer.buildLegalActions`; the
current tree carries the B5-0302 guard but not that hunk, so the fresh run read
29 actions / 38 callbacks — the OLD 32-action shape — against a task row that
expected the new one.

## Practice

1. When a task compares a fresh measurement to a historical baseline, do not
   assume the fix that defined the baseline is still in the tree; read the
   specific hunk before interpreting the number.
2. Attribute the profile to hunks, not to the tree in general: name the exact
   guard whose presence/absence explains the count (here: missing
   `isPassed/actionsLeft` early-return; the lookalike condition in
   `RulesEngine.canPlayContingency` is a different mechanism).
3. A report whose finding contradicts its task's expectation is still a DONE
   when the row only asks for measurement; the row's own scope decides, and
   "state what the deviation implies" was the deliverable. Never repair
   outside the claimed scope to make the number match.

## Related

* Supersedes nothing; first pattern in this namespace.
* Traces to: B5-1419 report (`.agent/REPORTS/2026-10-01-Buffy (glm-5.3-flash) 12-B5-1419.md`),
  B5-0202c (the hunk whose absence explains the profile), B5-0302 (the guard
  that IS live).
