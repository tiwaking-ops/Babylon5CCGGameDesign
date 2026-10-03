---
document:
  title: "Claims directory"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free — row-status precondition added, human-approved 2026-09-27 (B5-0622)"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-27", note: "edit: three-signal liveness rule added to the claims paragraph, human-approved 2026-09-27 (B5-0659). Appended rather than merged into the B5-0622 entry above because that entry's version field has prose fused into it, so identity cannot be matched on name+version without rewriting a field the compaction rule forbids rewriting; the earlier pass is named here so the history is not lost."}
    - {name: "opencode (space-bunny-free) 10", version: "space-bunny-free", passes: 1, last_pass: "2026-10-01", note: "edit (B5-1931): the opening claim-creation paragraph now names new-claim.ps1 as the only write path with the measured UTC+13 hand-stamp evidence, and the bottom JSON block is relabelled the writer's emitted shape rather than a hand-fillable template"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 10", version: "space-bunny-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-10-01"
---

# CLAIMS

Active locks live here as `<task-id>.json`. Creating the file IS the claim —
check existence first; if present, the task is taken. Never overwrite, edit, or
delete another agent's file. Delete only your own on finish.

**Create claims with the shipped writer, never by hand (B5-1931).** The
existence of the file is the claim, but the *timestamp inside it* is the
weakest link in the whole liveness design, because a model can emit a plausible
ISO string without ever reading a clock:

```
powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/new-claim.ps1 -TaskId B5-NNNN -AgentId "<agent-id>" -Scope "<scope>" -Javac 1.8.0_292
```

It stamps `started_utc` from `[DateTimeOffset]::UtcNow` at the moment of
writing, refuses an existing claim path, refuses an orphan claim against a
non-`OPEN` row, and accepts the write only after the payload is verified **not
ahead of the real clock** and in agreement with the new file's own mtime. Treat
a non-zero exit as *no claim*: record the explicit error and stop that item.

**Why hand-authoring fails on this host (measured, B5-1931).** There are two
distinct ways a hand-stamped `started_utc` lands in the future, and they are
not the same bug.

*Cause (a) — local time stamped as `Z`.* This host runs **UTC+13**. Stamping
local time and appending `Z` — the obvious hand-author mistake — puts
`started_utc` ~13 h *ahead*. Live claim `B5-1827` measured exactly this:
**779.9 min ahead of its own file mtime**, which is this host's offset to the
decimal, so the cause is identified rather than guessed.

*Cause (b) — a value no clock ever read.* Live claim `B5-1803` measures **351.8
min ahead** of its own mtime, which is *not* the host offset, so no local-stamp
mistake explains it: the number was invented or rounded. The `B5-1913`
diagnostic in `new-claim.ps1` names both causes for this reason.

Either way the age is then negative, and a negative age compares as younger than
any TTL, so the claim reads `LIVE` forever: the task is unofferable (the
runner's B5-0952 guard refuses it) and unrepairable by anyone but the owner.

A second, quieter failure of the same hand: naming the field `task_id` (or
`claimed_utc`) instead of `task` / `started_utc`. That file parses as JSON and
carries a real timestamp, so a reader looking for `started_utc` finds nothing
and the three-signal liveness join **cannot see the claim at all** — it is not
malformed, it is invisible. The writer above emits the contract fields; use it.

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

**Field contract.** `task`, `agent_id`, `started_utc`, `ttl_min`, `scope`,
`javac`. The shape below is **what the writer emits, for reference** — it is
not a template to fill in by hand, because a hand-typed `started_utc` is the
UTC+13 defect named above:

```json
{"task": "B5-0001", "agent_id": "hermes-01", "started_utc": "2026-09-21T12:00:00Z", "ttl_min": 30, "scope": ["b5ccg/src/b5ccg/ui/"], "javac": "1.8.0_292"}
```

One more tell, measured on B5-1931: the writer emits UTF-8 **with** a BOM
(`EF BB BF` as the first three bytes), because it writes through
`[IO.File]::WriteAllText` with `System.Text.Encoding.UTF8`. A claim file that
starts `7B 0D 0A` (`{`, CR, LF) did **not** come from the writer. The BOM is
not a formatting preference — it is a free forensic marker separating a
tool-stamped stamp from an invented one, and it is what identified B5-1827.
