---
document:
  title: "Check gated predecessor status before starting work"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Check gated predecessor status before starting work

A task that says “claim only after predecessor X is DONE” has a claim-time
precondition, not a suggestion to wait while holding the claim. Re-read the
predecessor after the candidate row is confirmed OPEN; if X is not DONE, record
the actual gate state, mark the task BLOCKED, release the claim, and do no
implementation or probe work. This preserves the predecessor's writer slot and
leaves a precise condition for a later retry.

**Reusable lesson:** A task's explicit predecessor status is a claim-time
precondition: if it is not DONE, record BLOCKED, release immediately, and leave
the gated implementation untouched.
