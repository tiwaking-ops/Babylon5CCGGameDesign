---
document:
  title: "CORRECTION — the B5-0613 runner-regex lesson mandates a tautological self-check"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm:
    - {name: "solar-pro4:free", version: "solar-pro4:free", note: "original author of the superseded record; not an assessor of this correction"}
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
supersedes:
  - ".agent/PATTERNS/solar-pro4\u2028free/2026-09-27-runner-row-regex-must-tolerate-leading-pipe-multiplicity.md"
  - ".agent/REPORTS/2026-09-27-solar-pro4-free-B5-0613.md (lines 22, 25, 34, 54)"
---

# CORRECTION — the B5-0613 runner-regex lesson mandates a tautological self-check

Advisory only, same tier as `investigations/`. Never canonical. Citing confers nothing.

**This record supersedes one specific claim.** It does not dispute the rest of the
B5-0613 work, which was sound. Supersede-never-rewrite applies, so the defective record
is left byte-intact on disk; this file is the correction and it links forward.

The defective claim, quoted from
`.agent/PATTERNS/solar-pro4⟨U+2028⟩free/2026-09-27-runner-row-regex-must-tolerate-leading-pipe-multiplicity.md`:

> "A query tool over this ledger **must** self-check that returned-row count equals the
> count of lines matching **its own filter pattern** … so a row filter defect reports
> itself instead of silently shrinking the visible set."
>
> "Verified by dry-run: old regex saw 301/312 rows; tolerant regex sees 312/312 with
> self-check passing (312 == 312)."

**That is not a defect report. It cannot fail.** If `$rows` is built from the lines a
pattern matches, and the check counts the lines that same pattern matches, the two numbers
are equal by construction. The check is a set compared with itself. It went green
(`312 == 312`) **while 9 rows carried a corrupt status and 4 rows were invisible to the
tool entirely.** The lesson generalises that construction into a rule with a *must*, which
is the harmful part: the next agent to read the store will add the same dead check to the
next tool, and will report its green result as evidence.

**Three specific errors, each independently confirmed by execution.**

| # | Error in the superseded record | Reality |
|---|---|---|
| 1 | "self-check … count of lines matching its own filter pattern" presented as a general rule | tautology; cannot detect a hidden row or a corrupt field; the reason this defect survived a fix that was reported verified |
| 2 | "first split cell matching `^B5-\d+$`" | `\d+` cannot match the letter-suffixed task IDs. `B5-0202c`, `B5-0329a`, `B5-0330a`, `B5-0331a` stayed **invisible** even after the fix. Correct form is `^B5-[0-9]{4}[a-z]?$` |
| 3 | *Status is not mentioned at all* | `Status` is the field the **stop condition filters on** (`Where-Object { $_.Status -eq 'OPEN' }`, `run-queue.ps1` line 117). It was still read from a hard-coded cell index. On a `||`-row, `$parts[2]` **is the ID**, so those rows could never satisfy `-eq 'OPEN'` |

Error 3 is the one that mattered, and the lesson's silence on it is why the lesson reads
as complete when it is not. Measured after the B5-0613 fix: the tool reported **17** OPEN
rows; the ledger held **24**. The seven invisible: `B5-0572`, `B5-0574`, `B5-0604`,
`B5-0605`, `B5-0606`, `B5-0607`, `B5-0608`.

**The consequence, stated plainly because it is the whole point.** A human had been
hand-seeding work for six waves to compensate for agents reporting *"no OPEN tasks
remaining."* The agents were not careless and the human was not confused. The tool
returned a **confident false negative** — 17 instead of 24 — and a tool that returns wrong
data is more dangerous than one that fails, because it is believed. The lesson as written
would have preserved that failure mode while making it look like a verified fix.

**What the corrected rule is.** A self-check must compare its subject against something
**independently derived**, never against its own predicate. Concretely, the check that
works compares the parser output to a *deliberately more permissive, separately written*
pattern (`^\|+\s*B5-[0-9]{4}[a-z]?\s*\|` — no capture group, no ID constraint), plus a
distinct warning for any row that parsed with **no recognisable status**. Both can go red
on real data.

**The test to apply before shipping any self-check:** *what input makes this red?* If the
answer is "none," it is not a check, it is a comment, and shipping it as evidence is worse
than shipping nothing — it converts an unknown into a false assurance.

**The second rule this record adds, which the superseded one has no equivalent of:** a
parser fix and a **consumer** fix are different tasks. Fixing row *visibility* while the
consumer filters on row *status* produces a tool that is **worse than broken** — it
returns wrong answers instead of omitting rows. Do not call a query-tool fix done until the
consumer's own filter has been re-run against the new output and reconciled against
ground truth by hand, counting the rows the tool claims and the rows the file contains
separately.

**Fixed state, for the record.** `.agent/run-queue.ps1` `Get-LedgerRows` now derives both
`Id` and `Status` by scanning split cells for the first value that looks right, widens the
ID pattern to `B5-[0-9]{4}[a-z]?`, and self-checks against the permissive pattern plus a
status-less-row warning. Verified: runner OPEN **24 == 24** disk OPEN, 0 invisible rows,
0 rows without a status, 316 rows, 0 duplicate IDs, all four suffixed IDs resolved
(`B5-0202c=DONE`, `B5-0330a=DONE`, `B5-0331a=DONE`, `B5-0329a=DONE`).

**Companion, same session — a `-WhatIf` the script does not declare is not a dry run.**
Testing that function via `. .agent/run-queue.ps1 -WhatIf` ran the script's main loop,
because the script declares no such parameter and PowerShell ignored it: it claimed a
task and launched an agent. **Extract the function to test it; never dot-source a script
whose top level has side effects.**

**Applies to:** any self-check, assertion, lint rule, or "verified N == N" claim whose
predicate resembles the thing it verifies; any query tool feeding a stop condition, gate,
or scheduler; any automated index, cache, or census that reports counts.

**Read before trusting:** any green "self-check", "sanity check", or `N == N` result
quoted as acceptance evidence — especially the one in the superseded record above.
