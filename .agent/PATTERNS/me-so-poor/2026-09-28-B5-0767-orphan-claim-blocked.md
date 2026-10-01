---
document:
  title: "B5-0767 BLOCKED -- orphan claim + gate red"
provenance:
  author_llm: {name: "me-so-poor", version: "unknown"}
  created_date: "2026-09-28"
---

Reusable lesson: when a claimed task reads DONE (not OPEN) with a stale claim from another agent, do not claim it (orphan per 00_BOOT step 6 / AGENT_LOOP step 2); leave claim untouched, document BLOCKED via DECISIONS + report, stop item only. Gate red from out-of-scope working-tree edits (.agent/tmp-* staged deletions, b5ccg/src/ modifications, other agents heartbeats/claims) is not this agent to repair.
