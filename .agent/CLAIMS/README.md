---
document:
  title: "Claims directory"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free — row-status precondition added, human-approved 2026-09-27 (B5-0622)"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-27", note: "edit: three-signal liveness rule added to the claims paragraph, human-approved 2026-09-27 (B5-0659). Appended rather than merged into the B5-0622 entry above because that entry's version field has prose fused into it, so identity cannot be matched on name+version without rewriting a field the compaction rule forbids rewriting; the earlier pass is named here so the history is not lost."}
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-27"
---

# CLAIMS

Active locks live here as `<task-id>.json`. Creating the file IS the claim —
check existence first; if present, the task is taken. Never overwrite, edit, or
delete another agent's file. Delete only your own on finish.

**Liveness is three signals, never one** (human-approved 2026-09-27, B5-0659;
canonical text in `../HEARTBEATS/README.md` § *Liveness: three signals, never one*).
A task is live when the **newest** of three timestamps falls inside the TTL:

1. the claim file's `started_utc`, or its mtime when `started_utc` is unparseable;
2. the **owner's** heartbeat file mtime — required, so its absence is `UNKNOWN`;
3. the task's report file mtime in `../REPORTS/` — contributing only, because a
   report is written at close-out and its absence mid-task is the normal case.

Verdicts: `LIVE` = at least one signal found and within TTL; `STALE` = at least one
found and none within TTL; `UNKNOWN` = a required signal absent or unparseable.
A claim may be reaped only when **no** signal is fresh, and only after the evidence
is recorded in the reap note itself (`.agent/TASK_LEDGER.md`).

The rule that keeps biting: **an absent signal is `UNKNOWN`, never `LIVE` and never
`STALE`.** A lookup that matches nothing must return `UNKNOWN` with its reason and
never an age of `-1` or `0`, because both compare as younger than any TTL. That
single inversion is the B5-0609 defect class, and reading a missing heartbeat as
"fall back to the claim age" is the same bug wearing a different hat.

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
