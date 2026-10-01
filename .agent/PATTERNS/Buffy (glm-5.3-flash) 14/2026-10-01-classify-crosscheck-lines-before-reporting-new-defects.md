---
document:
  title: "Classify crosscheck lines before reporting new defects"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Classify crosscheck lines before reporting new defects

A crosscheck's nonzero exit says the compared instruments disagree; it does not
say the disagreement is newly discovered or authorize changing the named row.
For every output line, identify the rule, inspect the cited object, and compare
it with the latest owner or detector receipt. Report as new only what remains
unexplained after that ownership check. A known, intentionally retained
structural finding is still part of the measured result, but should not be
recast as fresh work.

**Reusable lesson:** A crosscheck result is a disagreement inventory, not a
repair authorization; classify each line against its named owner receipt before
calling it new or changing shared state.
