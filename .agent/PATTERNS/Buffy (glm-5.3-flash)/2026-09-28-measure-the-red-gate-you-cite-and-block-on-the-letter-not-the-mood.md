---
document:
  title: "Measure the gate you cite as red; block on the row's letter, not on an unmeasured mood"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# From B5-0699: the third identical block, and what finally distinguished the sessions

## 1. A red gate must be measured, never quoted

Two prior sessions blocked B5-0699 on "out-of-scope edits make the gate red" without running
the gate (one said so explicitly: compile "NOT executed"). This session ran it against the
same dirty tree: compile exit 0, conformance 643/643, smoke PASSED. The premise was false;
the standing guidance already said so ("modified-files-exist is not a red gate"). The cost of
a quoted gate: a deliverable stall repeated across three sessions, a falsely-blamed dirty
tree, and reconciliation work nobody needed. A gate result is a measurement or it is an
opinion; opinions do not flip ledger cells.

## 2. Sweep-scoped rows are structurally unrunnable while any sibling is live — and that is the honest block

Even with the gate green, committing "everything except claims/heartbeats" necessarily
captures whatever a live sibling holds (here: B5-0939 over `investigations/` +
`docs/reports/`). Three sessions independently resolved the same verdict. The lesson is not
"be faster than your siblings" — it is that a row whose scope is the tree inherits every
live claim in the repo as a blocker, and the truthful close-out is BLOCKED with the live
claim **named**, not a quiet DROP, not a force-commit, not a re-scope invented unilaterally.
If the deliverable matters, the fix is a re-scope that stages exactly its own bytes (the
B5-0921 Phase-1 pattern) or a fleet-agreed quiet window — an overseer decision.

## Cross-reference

* v3's independent verdict: `.agent/REPORTS/2026-09-28-me-so-poor-B5-0737.md` (B5-0737 row).
* The measured-gate guidance: DECISIONS, B5-0937 restoration entry (human ruling Option A).
