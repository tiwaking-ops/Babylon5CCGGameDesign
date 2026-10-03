---
document:
  title: "Proposal — B5-2077 (corrected): re-scope the task-cell budget median clause to OPEN plus BLOCKED rows"
  status: "Proposal"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2330", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-03T02:35:00Z"
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2330", version: "stealth-space-bunny-alpha"}
  last_modified_date: "2026-10-03T02:35:00Z"
supersedes:
  - "docs/proposals/2026-10-02-opencode-space-bunny-free-B5-2077-median-budget-rescope-proposal.md"
---

# B5-2077 — Task-cell budget median clause re-scoping (corrected proposal)

**Proposal-only.** No `b5ccg/src/`, no `b5ccg/resources/`, no ledger content, and no
tool edited by this task. Doc-only, per section 4 of the row's scope.

## 0. Why this document exists (supersession note)

An earlier deliverable for this task,
`docs/proposals/2026-10-02-opencode-space-bunny-free-B5-2077-median-budget-rescope-proposal.md`,
was filed on 2026-10-02 but is **truncated mid-sentence** and cites a source
location that does not exist. Both defects are recorded here so the correction is
traceable; the earlier file is left byte-identical (supersede-never-rewrite).

Its three checkable claims, verified this pass:

| Claim in the 2026-10-02 file | Status on 2026-10-03 |
|---|---|
| Budget text is in `AGENTS.md` section 2 | **WRONG.** `grep -n "479\|budget" AGENTS.md` returns no budget clause. The budget lives in `.agent/00_BOOT.md` step 3a, line 74. |
| Wave stats: Count 223, mean 711.8, median 665, p90 1119, max 4811 | **STALE.** Re-measured: Count 323, mean 838.4, median 512, p90 1293, max 7421. |
| File ends with a complete section 7 | **CORRUPT.** Ends mid-token: `...the TASK_LEDGER.md<\u200btool_call>arg_value></\u200btool_call>` — a leaked tool-call frame, not prose. |

The *conclusion* it reached is nonetheless correct, and this proposal re-derives it
from a clean measurement. See section 3.

## 1. Problem statement

`.agent/00_BOOT.md` step 3a records the standing task-cell budget adopted by B5-1471:

```
The budget for NEW ledger rows adopted by B5-1471 is
mean <= 764, median <= 479, p90 <= 1879, max <= 4397 characters in the Task cell.
```

`measure-task-cells.ps1` measures every ledger row with an id above 1471 and
prints one `VERDICT` line. The instrument has **no status awareness**: it pools
OPEN, BLOCKED, DONE and VOID rows into one distribution.

That pooling is the defect. A DONE row's Task cell is not a task description — it
is a close-out narrative, and it grows with how thoroughly the closing agent
documented its verification. Mixing that with the Task cells of work still in
flight measures two different quantities in one statistic.

## 2. Current measurement baseline (re-measured this pass)

`powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/measure-task-cells.ps1`,
run 2026-10-03, exit 0 (which per the B5-2079 header means MEASURED, not compliant):

```
Count: 323   Mean: 838.4   Median: 512   p90: 1293   Max: 7421
VERDICT: This wave does NOT comply; max 7421 exceeds proposed max 4397;
         300 of 323 rows within budget
```

The pooled median of 512 already breaches the 479 clause. Broken out by status
(read-only parse of the same ledger, 323 rows, ids > 1471):

| status | n | median | mean | max |
|---|---|---|---|---|
| DONE | 222 | 687 | 839.0 | 7421 |
| BLOCKED | 66 | 447 | 1051.7 | 4376 |
| OPEN | 31 | 405 | 439.5 | 964 |
| VOID | 4 | 347 | 346.5 | 390 |
| **all pooled** | **323** | **512** | 838.4 | 7421 |
| **OPEN + BLOCKED** | **97** | **434** | 856.0 | 4376 |

DONE rows carry **68.8%** of all Task-cell characters while being 68.7% of the
rows — and their median (687) sits 253 characters above the OPEN median (405).
The 2026-10-02 file attributed the 79% figure to B5-2015; the share measured
today is 68.8%. The direction is unchanged, the magnitude is not, and the
proposal does not depend on the exact figure.

## 3. Proposed change

**Re-scope the median clause to OPEN plus BLOCKED rows**, excluding DONE and
VOID from the median (and from the mean, which has the same contamination).

Under the re-scoping the median clause reads **434 <= 479 — PASSES**. The
clauses that remain genuinely red are unaffected by status and are *not* in
scope for this proposal:

- **max 7421 > 4397.** Both extremes are DONE/BLOCKED close-out narratives
  (B5-2285 at 4651, B5-2287 at 6013). Re-scoping the *median* does not fix this,
  and a max clause over close-out text is arguably measuring the wrong thing
  too — but that is a separate question and this proposal does not silently
  widen its own scope to cover it.
- **BLOCKED mean 1051.7 > 764.** Real signal: long rows that stalled.

So the honest summary is that re-scoping **rescues the median clause only**. It
does not make the wave compliant, and this proposal should not be read as doing
so.

## 4. `measure-task-cells.ps1` change (described, NOT applied)

The tool currently filters on id only. `$rowRegex` at line 83 already captures
status as `Groups[2]`, so the filter needs no regex change:

```powershell
# current, line 104-107
$num = $id.Substring(3)
$numClean = $num -replace '[a-zA-Z]+$',''
if ([int]$numClean -gt 1471) {
    $results += [pscustomobject]@{ Id = $id; TaskLen = $taskLen }
}

# proposed
$numClean = ($id.Substring(3)) -replace '[a-zA-Z]+$',''
if ([int]$numClean -gt 1471) {
    $status = $m.Groups[2].Value.Trim()
    if ($status -ne 'OPEN' -and $status -ne 'BLOCKED') { continue }
    $results += [pscustomobject]@{ Id = $id; Status = $status; TaskLen = $taskLen }
}
```

Note the proposal keeps the B5-2079 `$numClean` suffix tolerance and the
B5-2079 exit-code contract (0 = measured, 2 = not measured) intact. The earlier
2026-10-02 file quoted `if ([int]$id.Substring(3) -gt 1471)` as the "current"
line, which is the pre-B5-2079 form and would **regress** the suffixed-id
support if applied verbatim.

## 5. Verification approach for an adopting agent

1. Apply the section 4 filter.
2. Re-run `measure-task-cells.ps1`; expect `Count: 97`, median 434, exit 0.
3. Run `-SelfTest`: both B5-2079 fixtures must still behave (zero-row wave
   exits 2; wide/suffixed ids captured). A status filter that drops the
   zero-row guard's `continue` path would make fixture 1 pass vacuously.
4. Confirm the printed `Count` is non-zero before reading any verdict.

## 6. Impact

- **Ledger rows:** none modified. This is a proposal.
- **Seeding:** unaffected; new rows are OPEN and so remain in the measured set.
- **Existing history:** the DONE distribution becomes unmeasured by this
  instrument. That is the intent, but it means the tool stops being a check on
  close-out verbosity. If that check is wanted, it needs a separate clause with
  its own threshold — B5-1471's numbers were calibrated on a mixed population and
  should not be silently reinterpreted as in-flight-only.

## 7. Dependencies

- Requires human approval to edit `.agent/00_BOOT.md` step 3a and the tool.
- No Java source touched. No external library. `b5ccg/src-java8-archive/`
  untouched.
- Independent of the red `engine/` build currently in the working tree; this
  proposal is doc-only and its gate is "none claimable immediately".
