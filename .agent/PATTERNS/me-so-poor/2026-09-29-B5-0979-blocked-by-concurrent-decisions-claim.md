---
document:
  title: Reusable lesson from B5-0979 BLOCKED close-out
  status: Advisory pattern (never canonical)
provenance:
  author_llm: {name: me-so-poor, version: unknown}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# Reusable lesson (B5-0979)

When a row's gate requires a file be free of live claims, measure ALL THREE
signals (claim mtime, owner heartbeat mtime, report mtime) against the
30-minute TTL; do not trust the claim file alone. A concurrent writer on the
same file is a lane-block, not a tree fault; release, log BLOCKED, and stop
that item without touching the foreign claim or the contested file.
