---
document:
  title: "Proposal — B5-2077: re-scope the task-cell budget median clause to OPEN plus BLOCKED rows"
  status: "Proposal"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-10-02T04:35:00Z"
  assessed_date: "2026-10-02T04:35:00Z"
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  last_modified_date: "2026-10-02T04:35:00Z"
---

# B5-2077 — Task-cell budget median clause re-scoping proposal

**Proposal-only.** No `b5ccg/src/`, `b5ccg/resources/`, or ledger files touched by this task. Doc-only.

## 1. Problem statement

Section 2 of `AGENTS.md` records the standing task-cell budget:

```
* `babylon5ccg TASK CELL BUDGET` — the working set of rows measuring
  task cells against: mean <= 764, median <= 479, p90 <= 1879, max <= 4397
```

B5-2015 measured all rows AFTER B5-1471 (2026-09-30) against these thresholds.
The measurement found that DONE close-out extensions carry **79 percent** of
Task-cell characters (long close-out notes with extensive documentation), while
OPEN and BLOCKED rows pass every clause.

Current threshold interpretation applies only to **OPEN** rows. The median
threshold (<= 479) is therefore challenged by the distribution shape: a burst of
DONE close-outs with extensive documentation pushes the median higher while
representing completed, not in-flight work.

## 2. Current measurement baseline

Running `measure-task-cells.ps1` against the ledger shows:

```
Summary statistics for Task cells in this wave:
  Count: 223
  Mean: 711.8
  Median: 665
  p90: 1119
  Max: 4811

Proposed budget thresholds (from B5-1471): mean <= 764, median <= 479, p90 <= 1879, max <= 4397
```

The current median of 665 exceeds the 479 threshold primarily due to DONE close-out
rows carrying extensive documentation.

B5-2015 specifically measured:
- OPEN rows: all pass every clause
- BLOCKED rows: all pass every clause  
- DONE rows (close-out extensions): 79% of characters

## 3. Proposed change

**Re-scope the median clause to OPEN plus BLOCKED rows.**

Rationale:
1. OPEN rows are the primary concern for task planning and seed effectiveness
2. BLOCKED rows are explicitly quarantined work that cannot proceed until gates clear
3. DONE close-out documentation, while important for traceability, is an orthogonal
   concern that inflates the median without reflecting in-flight work complexity

The change would modify the budget text in `AGENTS.md` section 2 and adjust
`measure-task-cells.ps1` line 25 to filter by status `OPEN` or `BLOCKED`.

## 4. Measure-task-cells.ps1 modification

The script's status filter at line 25 would change from:
```powershell
# Current (implied OPEN only)
if ([int]$id.Substring(3) -gt 1471) {
```

To:
```powershell
# Proposed: track OPEN + BLOCKED only
if ([int]$id.Substring(3) -gt 1471) {
    # Read the STATUS field (2nd pipe) to filter
    $status = $rowRegex.Match($line).Groups[2].Value.Trim()
    if ($status -ne "OPEN" -and $status -ne "BLOCKED") { continue }
}
```

Or alternatively, add a status-aware filter that recalculates the budget
excluding DONE rows.

## 5. Verification approach

This proposal is doc-only; verification would involve:
1. Applying the proposed filter to `measure-task-cells.ps1`
2. Re-running against the ledger
3. Confirming the new median <= 479 for OPEN + BLOCKED rows

## 6. Impact analysis

- **Effect on current measurements**: Median would drop from 665 to the OPEN+BLOCKED
  median, which should fall within the 479 threshold
- **Effect on seeding**: Will not affect the seeding of new tasks
- **Effect on existing rows**: No ledger rows would be modified; this is a
  proposal only, following AGENTS.md section 6

## 7. Dependencies

- Requires human approval for AGENTS.md modification if adopted
- Does not modify any Java source files
- Does not modify the TASK_LEDGER.md<tool_call>arg_value></tool_call>