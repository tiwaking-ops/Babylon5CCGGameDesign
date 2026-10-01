---
document:
  title: "B5-0729 — record the B5-0660 PART 3 correction as a new record"
  status: "Report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0729 — B5-0660 PART 3 correction

**Author:** Cline (space-bunny-free) · **Date:** 2026-09-27 · **Status:** DONE

## Summary

The B5-0660 close-out report contradicts itself with the B5-0660 DECISIONS entry
on the one part that touched a second file. The landed code settles it. The
correction is recorded as a **new** DECISIONS entry plus a note on the B5-0660
ledger row; the B5-0660 report itself was not edited.

## Re-measured at claim time (not quoted from the seed)

The row requires re-measuring all three targets because each is a moving target.
One citation had already drifted: the row cites `docs/DECISIONS.md:4905`, which
is still the B5-0660 entry today, but the file is now 4924 lines (entries
including B5-0733 have been appended below it). The reading taken is recorded
rather than the one quoted.

## The contradiction, stated exactly

| Source | Claim about Part 3 |
|---|---|
| `REPORTS/2026-09-27-me-so-poor-B5-0660.md` line 3 | "census-crosscheck reconciled (**read, not edited** — B5-0653 scope)" |
| `docs/DECISIONS.md:4905` | "report signal **added at lines 164-173**, two tools now compare same triad" |

A file that was read and not edited cannot have gained ten lines.

## The tiebreaker — the code, not a vote

`.agent/tools/census-crosscheck.ps1` lines 164–173:

```powershell
# B5-0660 reconcile: run-queue Test-LiveClaim is now THREE-signal (claim,
# owner heartbeat, and the newest report matching the task id). Mirror the
# report signal here so the two tools compare the same triad.
$reportsDir = Join-Path $repoRoot 'REPORTS'
if (Test-Path -LiteralPath $reportsDir) {
  $pattern = '*' + $taskId + '*.md'
  Get-ChildItem -LiteralPath $reportsDir -Filter $pattern -File ... {
    if ($_.LastWriteTimeUtc -gt $newest) { $newest = $_.LastWriteTimeUtc }
  }
}
```

A self-labelling comment naming its task, sitting directly above the code that
task describes, is evidence of authorship. It is present in the working tree and
committed at HEAD `042c3592`. The same file also carries the later B5-0660
UNKNOWN-reason work at lines 268–299, keyed off the third signal.

So: **two records plus the artefact against one four-line summary line.** The
report clause is wrong on *method*.

## What is and is not being called

- **Not** a finding that me-so-poor failed the task. The three-signal work
  landed, is committed, and passes its gates today. This entry affirms that
  outcome.
- Only the characterisation of *how the second file changed* is corrected.
- The B5-0660 ledger row stays **DONE** — not re-opened, re-scoped or
  re-litigated. Its status was correct when written and is correct now.

## Scope held

No tool edited: `census-crosscheck.ps1` and `run-queue.ps1` were read only. No
`b5ccg/src` or `b5ccg/resources` byte touched. The B5-0660 report file was
**not** edited, per the row's instruction and supersede-never-rewrite. No
commit. No external library, human gate untouched.

## Reusable lesson

**The code outranks the summary line** — when a close-out report and a decision
entry disagree about what was done, open the file and read the hunk; a
self-labelling commit comment above working code settles what a prose summary
cannot, and record the correction as a new entry rather than editing history.
