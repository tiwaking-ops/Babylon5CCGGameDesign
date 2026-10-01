---
document:
  title: "B5-0751 close-out - VOID, premise already repaired"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0751 - VOID (Cline (space-bunny-free))

## Verdict

**VOID.** The row's gate was green, the claim was legitimate, and the work it asked
for was already done by another writer. No splice was made.

## Claim and gate

* `.agent/CLAIMS/B5-0751.json` verified absent before creating my own
  (`Test-Path` False; the directory held only B5-0481, B5-0699, B5-0737, B5-0747,
  B5-0777 - none mine). Claim created atomically, `started_utc` the real current
  UTC `2026-09-28T05:23:10Z`, never a placeholder (B5-0653).
* Row re-read immediately before the write: ledger line 907 still `OPEN`.
* **Gate green.** B5-0751 gates on "claim ONLY after B5-0749 is DONE" so ledger
  writes serialise one writer at a time. B5-0749 is `DONE` at line 906. So this is
  a *premise* failure, not a *gate* failure, and marking it `BLOCKED` would have
  been wrong - it would have written a false red implying a missing precondition.

## The premise was already falsified

B5-0751 exists to repair `B5-0723` at ledger line 886, described in the row as
"double pipe lead with 10 pipes while ledger-query expects 7".

Measured **before** any write, on SHA
`A300609A2188B2DA73173223C3A0E757818D38A9D8F0923DB38D37B44A662B82`:

```
B5-0723 | DONE | 7 | no | - | - | - | - | UNCLAIMED | reportable
```

7 pipes, single leading pipe, `doubleLead no`, no live claim. Byte-level
confirmation on the raw line: `lead=True`, `pipes=7`, `doubleLead=False`.

The repair was already performed by the **muse-spark B5-0703 adjudication pass**,
recorded at `docs/DECISIONS.md:5036`: the row "carried a doubled leading pipe plus
two trailing empty cells (10 pipes, crosscheck pipe-shape divergence). Lead fixed
and end-offset empties removed with all content bytes preserved, no live claim on
the row. Crosscheck is CONSISTENT at 396 rows after the repair." A second witness
is the B5-0773 close-out, which measured the same and deliberately left the row
byte-identical "for their own claimants to resolve or void".

## Why no splice

The row's own instructions forbid the alternative: classify every excess pipe by
offset as structural or content per B5-0568, preserve every content pipe
byte-identically, and **never normalise to seven blindly**. The honest
classification is that no structural defect remains. The only edit available to
line 886 was therefore none.

Rewriting a correct row to look like work happened would have destroyed the
recorded evidence of the other writer's repair and produced a false diff. `VOID`
is the honest status and is already established in this ledger (B5-0314, B5-0590).

The `B5-0725` rows and the 0729..0741 wave were left byte-identical, as the row
requires - they belong to B5-0753's claimant, not mine.

## A real defect, found in my own write path

`[System.IO.File]::WriteAllLines` emitted **CRLF**. The byte-level check went from
**0 CR to 927 CR**, moving every line ending in the ledger off its committed
bare-LF form. Note what did *not* catch this: the row was structurally correct
throughout - correct pipe count, correct status, correct content. A row-count or
pipe-count check passes straight through a whole-file encoding regression.

Repaired with an explicit `WriteAllText` newline normalisation. Back to **0 CR,
no BOM, 926 rows held**.

The generalisable point: the byte-level check I ran to confirm the *premise* was
also the only check that caught the *write* defect. Running a verification before
a write does not constitute having run it after.

## Footprint, honestly bounded

* Ledger: **one row**, line 907, `OPEN` -> `VOID` plus its note. 7 pipes,
  `doubleLead no`.
* `git diff --numstat` reads `60 9` against HEAD. **That is not my footprint.**
  The 9 deletions are other agents' rows (B5-0660, 0687, 0689, 0695, 0697 and the
  0713.. wave) already modified in the working tree at session start. Every diff
  hunk outside the 879..927 region is pre-existing; my edit is confined to line
  907 within that region.
* `docs/DECISIONS.md`: one assessor entry appended in the AGENTS.md §1a compacted
  form plus `last_modified_by_llm`; `author_llm` and `created_date` untouched.
* No other agent's claim, heartbeat, row, or pattern namespace was written.
* No `src/` or `b5ccg/` file touched. No commit, per repo convention.

## Gates on this tree

JDK `1.8.0_292`, `-source 6`.

| Gate | Result |
|---|---|
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit 0 |
| `ledger-query.ps1 -Status "*"` | my row `pipeCount 7`, `doubleLead no` |
| B5-0723 after close | unchanged, `7` / `no`, byte-identical |
| ledger bytes | 0 CR, no BOM |
| `census-crosscheck.ps1` | exit 1 `DIVERGENT`, 2 disagreements - see below |

**Not actioned, out of scope.** The 2 crosscheck disagreements are both `B5-0481`
(its claim liveness and its suppression reading against a missing `solar-pro4`
heartbeat, i.e. an `UNKNOWN` owner signal). This is the same orphan B5-0777
declined to reap for the same reason: per `HEARTBEATS/README.md`, `UNKNOWN` is
never `STALE`, so the all-three-STALE reap precondition is not met and the
B5-0653 guard already refuses to offer it. Not my row, not my claim; recorded,
not touched.

## Reusable lesson

**A green gate does not guarantee a live premise** - a task can be correctly
claimable, ungated and yet already satisfied by another writer, and the honest
close for that is `VOID` with the measurement cited, not a rewrite of a correct
row to look like work happened.
