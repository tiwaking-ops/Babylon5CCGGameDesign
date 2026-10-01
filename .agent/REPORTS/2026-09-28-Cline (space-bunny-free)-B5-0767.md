---
document:
  title: "B5-0767 close-out - attribution census of five scratch diagnostics, no deletions"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0767 - DONE (Cline (space-bunny-free))

## Verdict

**DONE.** Census only. The five files were read from git (four of them are already
gone from the working tree), attributed, mapped to the landed work each verifies,
and classified for B5-0737 to act on. **No deletions, no moves, no edits to any of
the five**, and no foreign claim, heartbeat, report, or row touched.

## Claim, gate, identity

- `agent_id` `Cline (space-bunny-free)`; the sanitised form is **unchanged** (no `:`
  or `/` in it), so heartbeat, report and pattern filenames all carry the true id
  verbatim, per AGENT_LOOP's IDENTITY AND FILENAMES rule.
- `.agent/CLAIMS/B5-0767.json` verified **absent** by the runner and re-verified by
  me (`Test-Path` False) before I created my own; the directory held B5-0481,
  B5-0699, B5-0737, B5-0761, B5-0765 - none mine.
- `started_utc` = `2026-09-28T05:45:31Z`, the real current UTC from
  `(Get-Date).ToUniversalTime()`, not a placeholder (B5-0653).
- Row re-read immediately before the write: ledger line 916 still `OPEN`.
- **Gate green.** The row reads "gated- none, claimable immediately", so there is
  no prerequisite to falsify and BLOCKED would have written a false red.
- `run-queue.ps1 -DryRun` offered B5-0767 in lanes 1-10 consistently, so the shared
  census tool agrees the row was claimable and unsuppressed.

## The premise was wrong, and the correction is the point

The row says the five files were "swept into history by checkpoint 042c3592".
Measured, that is backwards:

```
git show 042c3592 --stat   ->  .agent/tmp-b5660-diag.ps1    |  14 +
                             .agent/tmp-b5660-harness.ps1  | 104 +
                             .agent/tmp-diag2.ps1          |  13 +
                             .agent/tmp-diag3.ps1          |  14 +
git ls-tree HEAD -- .agent ->  all five still present in 02716363
```

042c3592 is the commit that **added** them. They were committed into history, not
swept out of the tree - and they are in the **current tip**, so the content B5-0737
needs is one `git show` away, not lost.

| File | Header | In HEAD | Working tree | Index |
|---|---|---|---|---|
| `tmp-b5660-harness.ps1` | present | yes | absent | staged `D` (uncommitted) |
| `tmp-b5660-diag.ps1` | absent | yes | absent | staged `D` (uncommitted) |
| `tmp-diag2.ps1` | absent | yes | absent | staged `D` (uncommitted) |
| `tmp-diag3.ps1` | absent | yes | absent | staged `D` (uncommitted) |
| `tmp-patch-0643.py` | absent | yes | **present** | clean |

Four staged deletions exist, made by another writer and **not committed**. They sit
inside B5-0737's git-only scope. I did not stage, unstage, or commit anything.

## Per-file census

**`tmp-b5660-harness.ps1`, 104 lines - header present.** Reads
`# B5-0660 acceptance harness (Buffy (glm-5.3-flash), 2026-09-27).` The only one of
the five with a header. Its V1/V2/V3 cases are the **B5-0660 row's VERIFY clause
verbatim**: V1 fresh report + stale claim + stale heartbeat stays LIVE; V2 a missing
heartbeat reads UNKNOWN and is warned; V3 an in-flight task with no report yet is
still offered. V4 (unparseable claim => UNKNOWN, never LIVE) is the **B5-0653**
rule, not B5-0660's - so the file straddles two rows.
Two defects B5-0737 should correct on any move: the header names Buffy
(glm-5.3-flash) while the row's close-out report is
`2026-09-27-me-so-poor-B5-0660.md`, and lines 66-69 claim the sandbox `REPORTS` dir
"sits at repo root (sibling of .agent), matching the live layout" - which
`run-queue.ps1` lines 139-143 record as the **B5-0771** defect, since fixed to
`.agent/REPORTS`. Re-run as written today, V1 and V3 no longer exercise the live
report signal. **Class: incident evidence to KEEP** (the assertions), comments
corrected.

**`tmp-patch-0643.py` - no header.** Its
`assert content.count("| B5-0643 | OPEN |") == 1` is the assert-before-write that
DECISIONS records as having **stopped the B5-0643 double-run duplicate ledger
write**, and its `assert len(cells) == 8` plus its post-write `row.count("|")` and
duplicate-ID print are the **B5-0621** and **B5-0618** gates as runnable code.
**Class: incident evidence to KEEP.**

**`tmp-diag3.ps1`, 14 lines - no header.** The only diagnostic that **dot-sources**
the sandbox runner copy in-process (`*>&1` into `Select-String` for
`UNPARSE|heartbeat UNKNOWN`) rather than capturing a child process. That is what
made the runner's warning strings observable; the child-process harness does not
reproduce them. **Class: candidate to MOVE under a tracked evidence directory.**

**`tmp-b5660-diag.ps1`, 14 lines - no header, zero assertions.** A single B5-0913
sandbox (stale claim + stale heartbeat + no report) - the same case the harness
asserts as V3, with a PASS/FAIL verdict this file never computes.
**Class: provably transient.**

**`tmp-diag2.ps1`, 13 lines - no header, zero assertions.** The same B5-0912 sandbox
as diag3 but run as a child process **without `-Verbose`**, so the warnings it was
built to chase are absent from its output. A strict predecessor of diag3.
**Class: provably transient.**

## Named, not touched

`.agent/tmp-0719-harness.ps1` (9650 bytes, untracked) is a sixth scratch harness
that this row does not name. Left byte-identical.

## Footprint, honestly bounded

- Ledger: **one row**, line 916, `OPEN` -> `DONE` plus claim and verified cells.
  7 pipes, `doubleLead no`, 0 CR, no BOM, single trailing LF.
- `docs/DECISIONS.md`: one entry appended; the B5-0763 footer line that anchored the
  append was restored verbatim on the line above the new heading.
- New files: this report and one pattern file. Nothing else.
- No `src/` or `b5ccg/` edit. No commit, per repo convention.

## Gates

| Gate | Result |
|---|---|
| `compile.bat` | exit 0, "Build successful", `-source 6`, one benign bootstrap warning |
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit 0 |
| `ledger-query.ps1 -Status "DONE"` | `B5-0767 / DONE / 7 / no` |
| `validate-heartbeats.ps1` | exit 1 - pre-existing `solar-pro4:free` two-file collision, foreign |

Note `compile.bat` needs `.\` under `cmd /c` here; a bare name is not on PATH and
reads as a build failure that is not one (same trap as B5-0763).

## Reusable lesson

**A census row's own description of its subject is a premise to be measured, not a
given** - `git show <checkpoint> --stat` and `git ls-tree HEAD` cost two commands
and inverted the row's central claim from "already swept away" to "in the current
tip, with four uncommitted staged deletions on top", which is the opposite
instruction for whoever inherits the action.

