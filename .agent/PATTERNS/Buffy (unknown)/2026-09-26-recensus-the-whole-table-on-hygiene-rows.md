---
document:
  title: "Pattern: re-census the whole table on hygiene rows"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# recensus-the-whole-table-on-hygiene-rows

**Trigger:** A hygiene row names specific defective rows (pipe counts,
double pipes, duplicates) in a shared ledger that concurrent agents write to.

**Response:** The named list is a snapshot that goes stale immediately:
re-census the entire table at claim time, fix the named rows plus any
same-class defect found, disclose each beyond-scope fix, and FLAG anything
out of class rather than expanding silently. Always finish with a full-table
census (per-row pipe count, leading-pipe check, ID uniqueness) as the
close-out's verification, not a spot check of the named rows.

**Evidence:** B5-0541 (2026-09-26): seeded for 0528 + three double-pipe
rows; the live table also held a second double-pipe (0532), a 10-pipe
self-referential 0538, an 8-pipe 0530, and a duplicated 0532 row from the
concurrent 0538 close-out. Census caught them all; one out-of-class defect
(0515, 9 pipes) was flagged, not touched.
