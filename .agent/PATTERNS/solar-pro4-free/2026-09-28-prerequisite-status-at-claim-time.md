---
document:
  title: "B5-0735 reusable lesson — record prerequisite on-disk status at claim time"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-28"
---

# B5-0735 — record prerequisite on-disk status at claim time

A docs-only refresh that names a prerequisite as DONE should still record the
actual on-disk status of that prerequisite at claim time, because a seeded row's
gate text and the live ledger can diverge between seed and claim. When the
prerequisite is OPEN at claim time but the user directs the work anyway, document
the deviation in the report and use the prerequisite's source close-outs as the
guide's ground truth rather than assuming the prerequisite's own report exists.

Filed under `.agent/PATTERNS/solar-pro4-free/`.
