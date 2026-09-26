---
document:
  title: "Pattern: assert the invariant per mutation, not once at the end"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# assert-the-invariant-per-mutation

**Trigger:** A repair pass performs several sequential structural mutations
(merging cells, splitting fused cells, truncating fragments) against a
shared structural invariant (e.g. every ledger row has exactly 7 pipes).

**Response:** Re-assert the invariant after EACH mutation and repair any
overshoot/undershoot immediately, before the next mutation. Verifying once
at the end can leave an intermediate row structurally broken with no
recorded intermediate state to diff against.

**Evidence:** B5-0545 (2026-09-26): rebuilding the fused B5-0543 row
overshot (8 pipes) and undershot (6 pipes) before landing on 7; each was
caught and fixed only because the census ran per mutation.
