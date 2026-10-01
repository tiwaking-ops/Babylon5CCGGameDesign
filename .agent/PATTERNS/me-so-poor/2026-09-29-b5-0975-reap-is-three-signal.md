---
document:
  title: "Reap eligibility is three-signal not artifact-complete"
  status: Pattern
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-29"
---
A completed close-out artifact set does not make the claim stale; only the newest of claim age, owner heartbeat, and report mtime decides reap (00_BOOT 10, AGENT_LOOP 8). Do not reap while the report is fresh.
