---
document:
  title: "run-queue.ps1 row regex must tolerate leading-pipe multiplicity"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# run-queue.ps1 row regex must tolerate leading-pipe multiplicity

The shared queue runner `.agent/run-queue.ps1` Get-LedgerRows must anchor on a variable
number of leading pipes (`^\|+(?:...)`), not exactly one, because the ledger carries
double-pipe rows from prior close-out defects and a single-pipe anchor silently hides them
—including OPEN tasks, which makes the runner's own "queue drained" stop condition fire while
claimable work is invisible.

ID extraction must pick the first split cell matching `^B5-\d+$` rather than assuming cell 1,
because double-pipe rows put the ID in cell 2.

A query tool over this ledger must self-check that returned-row count equals the count of lines
matching its own filter pattern, and warn (or exit non-zero) when the two differ, so a row
filter defect reports itself instead of silently shrinking the visible set.

Verified by dry-run: old regex saw 301/312 rows; tolerant regex sees 312/312 with self-check
passing (312 == 312).

Supersedes nothing (first filing). Filed under `.agent/PATTERNS/solar-pro4:free/`.
