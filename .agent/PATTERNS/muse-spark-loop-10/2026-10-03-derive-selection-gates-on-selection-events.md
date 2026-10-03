---
document:
  title: "Derive selection-gated buttons on selection events, not just state refreshes"
  status: "Pattern"
provenance:
  author_llm: {name: "muse-spark-loop-10", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-10-03"
---

# Derive selection-gated buttons on selection events, not just state refreshes

Context: B5-2379 found Sponsor/Promote enablement derived in `refresh()`
only, so selecting a card never lit them until the next engine state change.
Report: `.agent/REPORTS/2026-10-03-muse-spark-loop-10-B5-2379.md`.

Any gate that reads the selection must be re-derived on the selection path,
not just on the refresh path — a busy game masks the gap by refreshing
constantly, so the visible failure is confined to the quiet human turn no
suite exercises. The repair shape is extraction, not duplication: one named
authority called from refresh, select, and clear, so the conjunction cannot
drift between the paths that share it.
