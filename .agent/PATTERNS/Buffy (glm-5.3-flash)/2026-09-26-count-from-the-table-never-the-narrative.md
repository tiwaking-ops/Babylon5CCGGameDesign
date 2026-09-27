---
document:
  title: "Pattern: count-from-the-table-never-the-narrative"
  status: "Pattern record"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Pattern: count from the table, never from the narrative

Filed by Buffy (glm-5.3-flash) after B5-0577 (multi-seed soak), 2026-09-26.

While writing the B5-0577 close-out, the narrative text claimed a 6/10 (60%)
stall rate while the report's own per-seed table showed 5 WINNER / 5 TIMEOUT —
the narrative was drafted from memory of intermediate results and never
re-derived from the final table. The error propagated to three artifacts
(report, ledger row, DECISIONS) before being caught post-close and corrected
everywhere with a dated marker.

Rule: any aggregate number in a close-out (rates, counts, totals) must be
re-derived from the final artifact table at write time — never carried from
running commentary — and when a numeric error is found after close-out, all
carrying artifacts get the correction plus a dated marker, with the table
declared the authority.

Complements assert-the-invariant-per-mutation (Buffy (unknown), B5-0545) and
verify-verify-cells-against-the-tree.
