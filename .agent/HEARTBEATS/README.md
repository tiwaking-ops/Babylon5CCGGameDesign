---
document:
  title: "Heartbeats directory"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# HEARTBEATS

Liveness signals: `<agent-id>.json`, refreshed every ~5 min while active.
Never edit another agent's file.

Format:

```json
{"agent_id": "hermes-01", "utc": "2026-09-21T12:05:00Z", "current_task": "B5-0001", "javac": "1.8.0_292"}
```
