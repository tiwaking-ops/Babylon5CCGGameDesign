---
document:
  title: "Pattern: dispatch on picker selection, not post-submit state"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# dispatch-on-picker-selection-not-post-submit-state

**Trigger:** A UI handler must choose between a targeted path and a
fallback, where the targeted path's precondition (an explicit target seam)
is set by the handler's own delegate immediately before submit.

**Response:** Route on the picker's current selection, never on the
precondition the play path will set afterwards — that test is constant
against a fresh hand card and silently diverts every targeted click to the
fallback. Mirror the enablement gate (button dark without a selection) so
the null branch is unreachable in practice.

**Evidence:** B5-0522 (2026-09-26): the B5-0487 fleet handler tested
`enh.hasExplicitTarget()`, but the seam is set inside
`playCensureWithTarget` right before submit, so every targeted click fell
through to the self-target path — the exact human-unreachable class
B5-0522 was seeded to close. Same-class fix applied to the fleet handler
under the B5-0435/0490 precedent; the new CHARACTER handler was written
correctly from the start.
