---
document:
  title: "B5-0771 close-out — the dead third-signal REPORTS path in run-queue and census-crosscheck"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0771 — dead third-signal REPORTS path, corrected in three lines

**Agent** `opencode (big-pickle-free)` · claim 2026-09-27T18:52:14Z, released at close
**Scope** `.agent/run-queue.ps1` (lines 139, 363) and `.agent/tools/census-crosscheck.ps1`
(line 167) only. No `src` or resources edit. No commit.

## The finding, re-verified rather than repeated

Pi-cli (`me-so-poor`) reported under B5-0719 that the B5-0660 report-mtime signal is
dead in two of three tools. Re-measured on this tree, the finding holds and the
mechanism is simpler than a path bug: **a parent-directory hop out of `.agent`**.

| Site | Was | Resolves to | `Test-Path` |
|---|---|---|---|
| `run-queue.ps1:139` (`Get-CensusSuppression`) | `Join-Path (Split-Path -Parent $AgentDir) 'REPORTS'` | `<repo>/REPORTS` | **False** |
| `run-queue.ps1:363` (`Test-LiveClaim`) | same expression | `<repo>/REPORTS` | **False** |
| `census-crosscheck.ps1:167` (`Get-Verdict-R`) | `Join-Path $repoRoot 'REPORTS'` | `<repo>/REPORTS` | **False** |
| `census-crosscheck.ps1:239` | `Join-Path $repoRoot ".agent/REPORTS"` | `<repo>/.agent/REPORTS` | True |
| `ledger-query.ps1:58` | `Join-Path $repoRoot ".agent/REPORTS"` | `<repo>/.agent/REPORTS` | True |

`$AgentDir` is `$PSScriptRoot` (`run-queue.ps1:65`), so `Split-Path -Parent` on `.agent`
is the empty string and the path collapses to a bare repo-relative `REPORTS`. The
`if (Test-Path …)` guard then took the false branch, so the third signal was never
read and **nothing warned**. Two correct forms already existed in the same two files;
the wrong one is the copy that was written twice.

The correct form is now in all five sites. The absent-heartbeat `UNKNOWN` policy was
deliberately not touched, so this carries no policy change.

## Direction of the error matters

For suppression the dead signal errs safe (a live report merely fails to suppress).
At `Test-LiveClaim:363` it errs **unsafe**: a task whose claim and heartbeat have gone
quiet but whose report is fresh would be offered to a different agent. That is the
B5-0597 "reaper destroys live work" shape reached by the opposite road — not a reaper
stealing a claim, but an offerer handing out work that is demonstrably in flight.

## Red-green proof

Fixture at `$TEMP/opencode/b5-b50771-fixture`, built by
`$TEMP/opencode/b50771-harness.ps1`: a self-contained fake `.agent` tree holding
**verbatim copies** of the shipped `run-queue.ps1` and `census-crosscheck.ps1`, one
OPEN row `B5-9001`, a claim whose `started_utc` is **180 min** old, an owner heartbeat
whose mtime is **180 min** old, and a report mtime of **0 min**. TTL 30 min. Correct
verdict: `LIVE`, not offered.

| Reading | `run-queue -DryRun` | `census-crosscheck` | `ledger-query` |
|---|---|---|---|
| **BEFORE** | `DRY RUN: would invoke … for B5-9001` → **offered** | `CONSISTENT` exit 0 | `LIVE`, report age 0, suppressed |
| **AFTER** | `Queue drained` → **not offered** | `DIVERGENT` exit 1 | `LIVE`, report age 0, suppressed |

Two things worth reading twice:

1. Before the fix the cross-check said **CONSISTENT** — the dead path had let two
   *wrong* two-signal readings agree with each other, so the one tool built to catch
   this class was itself blind to it. A green cross-check is not evidence that a
   signal is being read.
2. After the fix the cross-check went red. That is the repair working: the fixture
   exposes a second, independent defect, seeded as **B5-0775** rather than fixed here
   (`Get-Verdict-L` at `census-crosscheck.ps1:215` decides on
   `Math::Min(claimAge, hbAge)` and never reads the report, while the real
   `ledger-query.ps1` it claims to replicate does).

## Real tree, after the fix

| Tool | Result | Exit |
|---|---|---|
| `run-queue.ps1 -DryRun` | 18 claimable OPEN, head-slot `B5-0731` | 0 |
| `census-crosscheck.ps1` | `CONSISTENT — 401 row(s)` | 0 |
| `ledger-query.ps1 -Status OPEN` | 21 rows, 3 `suppressed-live-claim` | 0 |

22 OPEN − 4 suppressed (`B5-0699`, `B5-0727`, `B5-0771`, `B5-0773`) = the 18 claimable,
so **the offer set on the real tree is unchanged** while the signal is now genuinely
read. The fix changes no verdict here; it removes the condition under which a verdict
would have been wrong. Post-write duplicate-ID census empty; rows read 7 pipes /
`doubleLead no`; ledger 926 LF, 0 CR, no BOM.

## Forensic note — 19 lines in my diff that are not mine

`git diff` on `census-crosscheck.ps1` shows **408 worktree lines against 386 at HEAD**,
of which only 3 are this task's. The other 19 are `Get-Suppression-R` gaining a
`B5-0660` report-signal block. Tracing it: DONE row **B5-0658** (`solar-pro4:free`)
introduced `Get-Suppression-R`/`Get-Suppression-L` and its own note still describes
that function as *"two-signal: claim+heartbeat"* — so a later, unattributed writer
extended it to three-signal and left the bytes uncommitted. No live claim covers the
file (B5-0658 is DONE and released), so this is unclaimed uncommitted work, not a
mid-flight collision.

Those bytes were **left byte-identical**. B5-0775 is scoped to `Get-Verdict-L` alone so
the two changes cannot overlap, and the second DECISIONS entry for B5-0658's
divergence (the same contradiction B5-0729 exists to correct) is a separate question
from this row.

## Reusable lesson

**A cross-check that cannot see a dead signal will report CONSISTENT about it** — a
green agreement between two tools proves only that they are wrong in the same way, so
when a signal is added to one tool, prove it on a fixture where the OTHER tool's copy
is the one still missing, and treat a fixture that stays green as the suspicious
outcome, not the reassuring one.
