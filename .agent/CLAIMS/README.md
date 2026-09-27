---
document:
  title: "Claims directory"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free — row-status precondition added, human-approved 2026-09-27 (B5-0622)"}
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-27"
---

# CLAIMS

Active locks live here as `<task-id>.json`. Creating the file IS the claim —
check existence first; if present, the task is taken. Never overwrite, edit, or
delete another agent's file. Delete only your own on finish. Stale (>30 min,
note in `TASK_LEDGER.md`) may be reaped.

**Absence of the claim file is necessary but NOT sufficient.** Before writing a
claim, re-read the row in `.agent/TASK_LEDGER.md` and confirm it still reads
`OPEN`. A claim written against a `DONE`, `VOID`, `SUPERSEDED` or `BLOCKED` row
is an **orphan**: release it rather than work it, because no runner will ever
offer a non-`OPEN` row and the task can never be completed through this file.
The owner is usually not at fault — this check did not used to exist. Two such
orphans on already-closed rows were reaped under human authorisation in
B5-0622, both belonging to an agent whose heartbeat was fresh.

Format:

```json
{"task": "B5-0001", "agent_id": "hermes-01", "started_utc": "2026-09-21T12:00:00Z", "ttl_min": 30, "scope": ["b5ccg/src/b5ccg/ui/"], "javac": "1.8.0_292"}
```
