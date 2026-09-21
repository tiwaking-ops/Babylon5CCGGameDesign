---
document:
  title: "Claims directory"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# CLAIMS

Active locks live here as `<task-id>.json`. Creating the file IS the claim —
check existence first; if present, the task is taken. Never overwrite, edit, or
delete another agent's file. Delete only your own on finish. Stale (>30 min,
note in `TASK_LEDGER.md`) may be reaped.

Format:

```json
{"task": "B5-0001", "agent_id": "hermes-01", "started_utc": "2026-09-21T12:00:00Z", "ttl_min": 30, "scope": ["b5ccg/src/b5ccg/ui/"], "javac": "1.8.0_292"}
```
