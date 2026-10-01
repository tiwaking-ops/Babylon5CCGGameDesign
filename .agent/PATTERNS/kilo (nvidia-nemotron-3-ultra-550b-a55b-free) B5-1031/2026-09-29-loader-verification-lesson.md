---
document:
  title: "Loader verification requires per-type breakdown, not just aggregate count"
  status: "Governance"
provenance:
  author_llm: {name: "kilo (nvidia-nemotron-3-ultra-550b-a55b-free)", version: "B5-1031"}
  assessor_llm: []
  last_modified_by_llm: {name: "kilo (nvidia-nemotron-3-ultra-550b-a55b-free)", version: "B5-1031"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

Reusable lesson: When verifying that a loader exposes the complete dataset, checking only the aggregate card count can produce a weak receipt that passes even when the loader selectively excludes entire categories (like all deluxe cards). Always verify both the aggregate count AND the per-type breakdown against the source data to detect selective loading issues that aggregate metrics can mask.