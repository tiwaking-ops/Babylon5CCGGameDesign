---
document:
  title: "B5-0613 status residual — the queue-drained signal was still false"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0613 status residual — the queue-drained signal was still false

Agent id: `opencode (space-bunny-free)`. Machine: javac 1.8.0_292.
File changed: `.agent/run-queue.ps1` (one function). No `b5ccg/` file touched.

Defect-fix follow-up to **B5-0613** (closed DONE by `solar-pro4:free`). B5-0613 fixed
row *visibility*. It did not fix row *status correctness*, and the runner's stop
condition reads status. So the false signal that caused six waves of unnecessary
seeding was still live after B5-0613 closed.

## 1. Why the user's seeding was rational

The human had been seeding rows because agents kept reporting **"no OPEN tasks
remaining."** That report was not carelessness. It was the tool:

- Stop condition, `run-queue.ps1` line 186, fires on the output of
  `Get-LedgerRows | Where-Object { $_.Status -eq 'OPEN' }` (line 117).
- `Status` was assigned `= $parts[2].Trim()`.
- On a double-pipe row, `|| B5-0604 | OPEN | …` splits to
  `['', '', ' B5-0604 ', ' OPEN ', …]`, so `$parts[2]` is **`B5-0604`**, the ID.

Those rows could therefore never satisfy `-eq 'OPEN'`. B5-0613 made the ID extraction
tolerant and left `Status` on the hard-coded index, so it made the broken rows *visible
with a corrupt status* — still invisible to the one filter that decides "drained."

Measured before this fix, against the live ledger:

| Measure | Runner reported | Disk truth | Invisible |
|---|---|---|---|
| OPEN rows | 17 | 24 | 7 |

The 7: `B5-0572, B5-0574, B5-0604, B5-0605, B5-0606, B5-0607, B5-0608`.

Had those been the last unclaimed tasks, the runner would have printed
*"Queue drained: no OPEN task without a live claim. Done."* with 7 claimable rows on
disk. That is the exact false negative the human was compensating for by hand.

## 2. Three defects, only one of which B5-0613 addressed

| # | Defect | B5-0613 | Now |
|---|---|---|---|
| 1 | `Status` read from fixed cell index — corrupt on every double-pipe row | not addressed | **fixed** |
| 2 | ID pattern `B5-\d+` cannot match letter-suffixed IDs | not addressed | **fixed** |
| 3 | Self-check compared the pattern under test against itself | shipped as a fix | **fixed** |

**Defect 2** — `B5-0202c`, `B5-0329a`, `B5-0330a`, `B5-0331a` stayed unmatched because
`\d+` cannot match the trailing `c`/`a`. The ID-cell test `^B5-\d+$` shared the blind
spot. All four are finished rows, so nothing claimable was hidden — which is precisely
why nobody noticed.

**Defect 3 is the important one.** B5-0613's self-check counted matches using the *same*
regex that had just built `$rows`:

```powershell
if ($ln -match '^\|+(?:\s*(B5-\d+)\s*\|)') { $matched++ }   # same pattern
if ($matched -lt $rows.Count) { Write-Warning ... }
```

`$rows` is built only from lines that regex matched, so the check compares a set with
itself. It is a tautology: it can never detect a row the regex cannot see, nor a row
whose status is garbage. It reported `312==312` — green — while 9 rows carried a corrupt
status and 4 were invisible. A self-check that shares its subject's blind spot certifies
the blind spot. The reusable lesson filed alongside B5-0613 states the fix makes
"the defect class report itself rather than hiding"; for this class it did the opposite.

## 3. The change

`$LedgerStatuses` declared once at file scope. Both `Id` and `Status` are now derived by
scanning split cells for the first value that *looks like* the thing sought, so neither
depends on a positional index. The self-check now compares against a deliberately
**permissive** pattern (`^\|+\s*B5-[0-9]{4}[a-z]?\s*\|`) that is not the pattern under
test, plus a second warning for any row that parsed with no recognisable status.

## 4. Verification (isolated parser, not the loop)

| Check | Result |
|---|---|
| runner OPEN vs disk OPEN | **24 == 24, MATCH** |
| OPEN rows invisible to the filter | **0** |
| Rows parsed with no status | **0** |
| Total rows | 316, **0 duplicate IDs** |
| Suffixed IDs resolved | `B5-0202c=DONE, B5-0330a=DONE, B5-0331a=DONE, B5-0329a=DONE` |
| Prior count for comparison | old regex 301 rows, 17 OPEN |
| `b5ccg/` files changed | **0** |

## 5. Disclosure — a mistake I made during verification

To test the fix I dot-sourced the script with `-WhatIf`:

```powershell
. .agent/run-queue.ps1 -WhatIf
```

**The script has no `-WhatIf` parameter.** PowerShell ignored the argument and the main
loop executed: it printed `Claiming lane for B5-0574 (22 claimable OPEN)` and launched a
`freebuff` agent, which died immediately on a command-argument parsing error. Notably the
"22 claimable OPEN" it reported is higher than the 17 the old code saw, so the fix was
already live and working at that moment.

**Damage assessment — benign, verified:**

- No `B5-0574.json` claim file persisted in `.agent/CLAIMS/`.
- Row `B5-0574` is unmodified, still `OPEN` with its original text.
- The spawned process exited on argument parsing before doing work.
- Four `node` processes are running; I did **not** kill them, because they are most
  likely other agents' live sessions and terminating them would do more damage than the
  mistake did.

Lesson recorded in the pattern store: *test a script's function by extracting the
function, not by dot-sourcing the file.* Dot-sourcing runs any top-level code, and a
`-WhatIf` that the script does not declare is not a dry run — it is an ignored argument.

## 6. Left undone, deliberately

- **No ledger row appended.** `solar-pro4:free` holds a live claim on **B5-0615** — the
  false-gate repair row seeded last pass — and other agents hold B5-0597 and B5-0572 in
  the same file. Appending to the tail during their windows is the collision class
  B5-0344 and this session's own near-miss both record. This report is the accounting
  record; the gated checkpoint rows (B5-0587 / 0595 / 0599 / 0612) will commit the file
  change and this report together.
- **B5-0613's own row is still `|| B5-0613 | DONE |`** — a leading double pipe, so the
  row documenting the runner fix was itself invisible to the runner. B5-0611's named band
  is 0604–0608, so 0613 is an unowned instance of that class. It is visible *now* (this
  fix), but the delimiter is still non-canonical.
- **B5-0613 items 2–5 remain unseeded** — whole-line census stop, `Test-LiveClaim`
  liveness via heartbeat/report mtime with `UNKNOWN` on absent signal, dynamic gate
  derivation from row text, invisible-row exit code. Deferred with "no new task seeded,"
  so four scoped items currently exist in no row. These are worth filing; `Test-LiveClaim`
  is the one that would stop a reaper killing live work.

## Reusable lesson

A parser fix and a *consumer* fix are different tasks, and shipping only the first
produces a tool that is worse than broken: it returns confident wrong answers instead of
omitting rows. When a query tool's output feeds a stop condition, the fix is not complete
until the consumer's filter has been re-run against the new output and matched to ground
truth. And a self-check must never share a pattern with the code it is checking — that
construction cannot fail, so a green result from it carries no information at all.
Filed as `.agent/PATTERNS/opencode (space-bunny-free)/a-self-check-must-not-share-its-subjects-pattern.md`.
