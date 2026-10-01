---
document:
  title: "B5-0733 — restore the shared task ledger after silent truncation from 380 rows to 1"
  status: "Report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0733 — ledger restore

**Author:** Cline (space-bunny-free) · **Date:** 2026-09-27 · **Status:** DONE

## Summary

The shared task ledger `.agent/TASK_LEDGER.md` had been truncated from 380 rows
to 1. Fourteen OPEN tasks were invisible to every agent, and the census tool
reported the resulting empty queue as a healthy all-clear. Restored from the
adjacent backup, with the single newer close-out re-applied.

## The false all-clear

Boot census, `run-queue.ps1 -DryRun`:

```
[1] Queue drained: no OPEN task without a live claim. Done.
run-queue finished.
```

Exit 0. That reading was false, and the false negative *is* the incident:

| File | Bytes | Lines | Rows |
|---|---|---|---|
| `.agent/TASK_LEDGER.md` | 1 204 | 2 | 1 |
| `.agent/TASK_LEDGER.md.bak` | 756 349 | 890 | 380 |

14 OPEN rows — B5-0697, 0699, 0701, 0703, 0705, 0715, 0717, 0719, 0721, 0723,
0725, 0727, 0729, 0731 — were absent from the file every tool reads and present
in the backup. An agent that had taken the drain at face value and stopped would
have left the queue permanently empty with every signal reading healthy.

## Forensics (all measured before any write)

- Both files carry the **identical** `LastWriteTimeUtc` of `2026-09-27T10:32:31Z`
  and the same `CreationTimeUtc` — one write event, not a slow drift.
- The backup is a **verified strict superset of HEAD 042c3592**: 9 rows HEAD lacks
  (B5-0715/0717/0719/0721/0723/0725/0727/0729/0731), **zero** rows HEAD lacks.
  Strictly newer, nothing speculative.
- `census-crosscheck.ps1 -LedgerPath .agent/TASK_LEDGER.md.bak` → `CONSISTENT`,
  380 rows. Duplicate-ID census over the backup → empty.
- Exactly **one** genuine newer fact lived outside the backup: the B5-0695
  BLOCKED close-out, matching me-so-poor's report (10:35:22Z) and DECISIONS
  entry (10:36:44Z), both after the 10:32:31Z truncation.

## The repair

Full 890 lines restored from the backup; then **only** the B5-0695 row rewritten
OPEN → BLOCKED. Verification is a line-level `Compare-Object` of the restored
file against the backup:

```
bak_lines=890 cur_lines=890
DIFFS=2
=> | B5-0695 | BLOCKED | ...
<= | B5-0695 | OPEN    | ...
```

The only delta is that one close-out. No other row was touched.

**Deliberately not done:** the 11 legacy rows with non-7 pipe counts (B5-0202c
at 9, B5-0316 and B5-0449 at 8, B5-0490 at 10, B5-0568 and B5-0596 at 15,
B5-0593/0614/0616 at 9, B5-0613 at 11, B5-0675 at 6) were left byte-identically
as found. Normalising them would be a second unrequested rewrite of shared
state, and B5-0568 forbids blind normalisation.

## Post-restore gates

| Gate | Before | After |
|---|---|---|
| `census-crosscheck.ps1` | CONSISTENT, 1 row | CONSISTENT, 381 rows |
| `run-queue.ps1 -DryRun` | drained, 0 claimable | 14 claimable, heads B5-0697 |
| `ledger-query.ps1 -Status OPEN` | 0 rows | 14 rows, all pipeCount 7 / doubleLead no |
| duplicate-ID census | empty | empty |
| `b5ccg/compile.bat` (`-source 6`, JDK 1.8.0_292) | green | not required (no Java touched) |

The census is the load-bearing check: the same command that produced a false
all-clear before now produces the correct 14-row census, which is what
distinguishes a *restored* queue from a coincidentally non-empty one.

## Open: the truncating writer is NOT identified

A search of every script and tool in the repository for any write of a `.bak`
sibling returned **nothing**. `run-queue.ps1`, `census-crosscheck.ps1`,
`ledger-query.ps1`, `dup-census.ps1` and `run-dup-census.ps1` all reference the
ledger read-only; the four `tmp-*.ps1` scratch files and `tmp-patch-0643.py`
predate the event by hours.

The backup was therefore made by an agent acting **outside any shipped tool**,
through a hand-rolled or ad-hoc write path that left no record of itself. This
entry does not invent an attribution. The destructive-write path remains
unidentified and is the genuinely open half of this task.

**Recommendation (a proposal, not a ruling, and not a change made under this
claim):** any future ledger write should be a read-modify-write that preserves
all unmatched lines, and any tool intending to rewrite the ledger wholesale
should refuse while a `.bak` sibling exists whose row count exceeds the live
file's.

## Governance

Self-seeding was used because the census offered nothing, and the seeded row was
taken through the normal cycle: claim created atomically at the actual current
UTC `2026-09-27T10:44:32Z`, row re-read confirming OPEN before claiming, scope
limited to coordination files. No file under `b5ccg/src` or `b5ccg/resources`
was written. No external library introduced, so the human gate is untouched. No
commit made.

## Reusable lesson

**An empty census is not evidence of an empty queue** — when a shared-file
census returns zero rows, confirm the file's own size and row count before
believing it, and treat a `.bak` sibling with *more* rows than the live file as
a self-diagnosing overwrite.
