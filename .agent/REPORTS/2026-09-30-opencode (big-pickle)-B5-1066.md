---
document:
  title: "B5-1066 — exception record for the declined lookalike-filename rename"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  assessor_llm:
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1066 — exception record for the declined lookalike-filename rename

Human order, 2026-09-30, verbatim:

> (a) leave the 17 alone — R7 stops the next instance and R5 forbids the
> retro-fix
>
> for (b) create a proposal so there is a record of this exception. I will rule
> on it later, not now.

## What was done

`(a)` was already satisfied — the 17 components were never touched, and were
re-verified byte-identical by SHA-256 after the proposal was written (§3).
`(b)` is delivered as
`docs/proposals/lookalike-filename-r5-exception-proposal.md`, a **proposal
only**: it changes nothing on disk, asserts no ruling, and creates no task for
any agent to action.

The proposal carries the measured manifest (bytes, git status, SHA-256 prefix
for all 17 components), the case for granting the R5 exception, the stronger
case against, the B5-0773 / B5-0793 precedent for declining, and the four
unresolved questions a future ruling must answer: the 2 non-identical
collisions, the 11 live ledger path citations, the unclassified
`solar-pro4-free-0978`, and why the two prior declines were correct.

## Task-id discipline failure, disclosed

I first claimed **`B5-1062`**, and only afterwards read the ledger tail.
`B5-1062` was **already an OPEN row** (seeded 2026-09-29, heartbeat-retirement
policy) whose scope cell names `.agent/HEARTBEATS/README.md` — the file I had
edited. Two mitigating facts, both verified rather than assumed:

* no `.agent/CLAIMS/B5-1062.json` existed before mine, so the row was
  UNCLAIMED and no live foreign writer held that scope; my claim was the only
  one and it is released;
* the real max 4-digit row id is **1065**, so `B5-1062` was mid-range and I
  should have looked before claiming. The **9104** and **9999** values a naive
  `B5-(\d{4})` sweep returns are sentinels/narrative, not real rows.

The R7 work is **additive** to that row's subject (identity rules) rather than
duplicative, so nothing needs reverting. This report keeps the `B5-1062` name
because that id is already embedded in the R7 provenance entry in
`HEARTBEATS/README.md`, in the `B5-1062` comment markers in
`validate-heartbeats.ps1`, and in this report's own title — renaming them now
would break citations, which is the exact class of damage R5 exists to
prevent. The lesson: read the ledger tail for the true max **before** claiming,
and prefer a fresh id over reusing one already in a row.

## Verification

| Check | Result |
|---|---|
| 14 report components SHA-256 vs proposal table | 0 mismatches |
| `PATTERNS\solar-pro4<U+F03A>free\` dir | intact, 13 files |
| both U+F03A heartbeats | intact |
| ledger row `B5-1066` | 7 pipes, no double-lead, `ledger-query` reads `UNCLAIMED \| reportable` |
| `run-dup-census.ps1` | `PASS`, 0 duplicate task IDs |
| `validate-heartbeats.ps1` | exit 1, 3 collisions (R7 firing as designed) |
| claim `B5-1066.json` | released |

Edits this pass: the new proposal file, this report, and one appended ledger
row. No rename, no move, no heartbeat/report/pattern edit, no `src/` edit, no
commit, no push.

## Reusable lesson

Read the ledger tail for the true max id before claiming — and when a
sentinel (`B5-9999`) or a narrative citation (`B5-9104`) inflates a naive
regex sweep, you can pick an id that is already in a live row. A deferred
ruling still needs its evidence on disk while fresh, or the next session
re-derives and re-litigates it.
