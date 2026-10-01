---
document:
  title: "B5-0775 report: add the third (report) signal to census-crosscheck Get-Verdict-L"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  provenance_note: "agent_id 'Cline (space-bunny-free) b5-0775' - per-instance discriminator per HEARTBEATS README R1-R6."
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
assessor_llm: []
---

# B5-0775 DONE — Get-Verdict-L now reads the report signal

## The defect

`.agent/tools/census-crosscheck.ps1` exists to diff `run-queue.ps1` against
`ledger-query.ps1` by computing **each tool's own verdict by that tool's own logic**
and comparing them. Its whole value is that a DIVERGENT exit 1 means the two real
tools disagree.

`Get-Verdict-L` (the ledger-query replica) folded only **two** signals into its
liveness decision:

```powershell
$newestMin = [math]::Min([double]$claimAge, [double]$hbAge)
```

The rule it claims to replicate — `ledger-query.ps1` lines 263–268 — folds a third:

```powershell
$newestMin = [math]::Min([double]$claimAge, [double]$hbAge)
if ($reports.ContainsKey($r.Id)) {
    $reportAge = [math]::Round(($now - $reports[$r.Id]).TotalMinutes, 1)
    $newestMin = [math]::Min($newestMin, [double]$reportAge)
}
```

So the cross-check was comparing a two-signal verdict against a three-signal one.
On any task whose claim **and** owner heartbeat are both outside the 30-minute TTL
while a same-id report is fresh, `ledger-query.ps1` reads `LIVE` and the replica
read `STALE` — and the tool emitted a `DIVERGENT` that was an **artefact of the
replica, not a real disagreement**. A cross-check that manufactures fake red is
worse than no cross-check, because every false red costs a human an investigation.

## The fix

Four lines added inside `Get-Verdict-L` only, replicating `ledger-query.ps1`
lines 263–268 verbatim rather than paraphrasing them. The index read is the
`$reportsL` map this file **already builds** (line 246) for `Get-Suppression-L`, so
it is the identical signal, not a re-derived one. The guard is a `ContainsKey` test:
the report is contributing-only, and its absence mid-task is the normal case, so
this can never invert into the B5-0597 absent-signal-as-fresh bug.

Scope held exactly: `Get-Verdict-R`, `Get-Suppression-R`, `run-queue.ps1`,
`ledger-query.ps1` and every ledger row untouched. The 19 uncommitted
`Get-Suppression-R` lines attributed to B5-0658 and the three B5-0771 hunks are
left byte-identical — confirmed by reading the `git diff` hunk headers back: the
file carries four hunks, and only `@@ -220,2 +224,18 @@` is mine.

## Red/green proof (TEMP fixtures, never the live tree)

Fixture at `$TEMP/b5-0775-fixture`: one row `B5-9001`, claim `started_utc` 180 min
old, owner heartbeat mtime 180 min old, same-id report mtime **0 min**, TTL 30.

| Reading | Result | Exit |
|---|---|---|
| **A** — current tree **minus only** my hunk, fresh report | `B5-9001 \| claim-liveness \| run-queue: live(age 0.4,ttl 30) \| ledger-query: stale(claim 180.4,hb 180.4)` → DIVERGENT | **1** |
| **B** — current tree, same fixture | `CONSISTENT -- 1 row(s), run-queue and ledger-query agree.` | **0** |
| **C** — control at `$TEMP/b5-0775-control`: stale report **and** no owner heartbeat | 2 genuine disagreements surfaced (`claim-liveness` and `suppression`) | **1** |

Control A was built by string-removing exactly my hunk from the current tree rather
than by checking out `HEAD`. That matters: the working tree already carried the
uncommitted B5-0658 and B5-0771 edits, so a `HEAD`-versus-working-tree comparison
would have attributed *their* behaviour change to mine. Isolating the one hunk
means the red is caused by exactly the lines I added and nothing else.

Control C is the "a cross-check that cannot go red is not a cross-check" case from
the row: with a stale report and an owner that has no heartbeat file at all, the two
tools genuinely differ (`unknown:no-heartbeat` versus `reportable`) and the tool
still exits 1. Fixing the false red did not cost the true reds.



## Gates

- `compile.bat` **exit 0**, `Build successful.`, `javac 1.8.0_292` (from `b5ccg`,
  via `cmd /c ".\compile.bat"`). Note the harness artifact recorded in B5-0769: a
  bare `compile.bat` is not on `cmd`'s path from the repo root, and PowerShell
  promotes the benign `-source 1.6` bootstrap warning to a `NativeCommandError`, so
  a stderr-reading wrapper calls a green build failed. Capturing output and reading
  `$LASTEXITCODE` separately gives the true 0.
- `run-dup-census.ps1` **PASS, exit 0**, 0 duplicate task IDs.
- `ledger-query.ps1 -Status "*"`: row `B5-0775 | OPEN | 7 | no |` — 7 pipes,
  `doubleLead no`. Its `suppressed-live-claim` reading is **my own** row under **my
  own claim, which B5-0657 exempts: I know it is mine and complete, so I judge it
  directly rather than re-censusing after release.
- `validate-heartbeats.ps1` **exit 1**, unchanged: the pre-existing foreign
  `solar-pro4:free` two-file identity collision. Both foreign files left
  byte-identical per R5/R6. My new heartbeat conforms and is not in the collision
  list.
- Live `census-crosscheck.ps1` exits 1 on 2 disagreements, both on `B5-0481` — a
  foreign claim with no owner heartbeat, the documented deliberate UNKNOWN-vs-
  claim-age divergence. Unchanged by this task and outside my scope.
- **No commit.** No `b5ccg/src` or card-data edit, no ledger row edit beyond this
  close-out.

## Reusable lesson

A cross-check that mirrors another tool's rule is only as faithful as the mirror;
when the original gains a signal, every replica of it silently becomes a *different*
rule, and the cross-check then reports the difference it just invented — so pin a
replica to its source with a test whose fixture is the exact shape that exposes the
missing signal, and isolate the red by removing your own hunk from the current tree
rather than diffing against HEAD, because HEAD also holds other agents' uncommitted
work and will happily take credit for your fix.

This is the B5-0771 failure shape reached by a different route. B5-0771 repaired a
*dead* third signal; the third signal was missing here, never having been written.
