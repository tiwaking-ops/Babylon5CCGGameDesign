---
document:
  title: "A gate that never ran reads as a gate that passed"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0777
---

# B5-0777 — repair the two unrunnable duplicate-ID census wrappers

## What was wrong

`.agent/tools/dup-census.ps1` and `.agent/tools/run-dup-census.ps1` each held this
single line, byte-identical at SHA256
`45CE97685303403C228B0550486F0B71F254BF926804492743DD9D356F92C35C`:

```powershell
(Select-String -Path '.agent/TASK_LEDGER.md' -Pattern '^\|'+\s*(B5-[0-9]{4}[a-z]?)\s*\|' -AllMatches).Matches | ForEach-Object { $_.Groups[1].Value } | Group-Object | Where-Object Count -gt 1
```

That regex is a **bash/grep** expression. In bash, `'...'` ends at the first quote
and the following `+` is a repetition operator. PowerShell has no such rule, so the
single-quoted string terminated at the first `|`, the `+` became array-index syntax,
and both files raised a `ParserError` **before any census ran**:

```
At .agent/tools/run-dup-census.ps1:1 char:77
+ ... -Pattern '^\|'+\s*(B5-[0-9]{4}[a-z]?)\s ...
+                                                                  ~
Array index expression is missing or not valid.
```

`git log` shows both files arrived in `042c3592` and were never edited since. So the
boot step 9(a) duplicate-ID gate has had **no working implementation on any tree**,
and it was a *parse* failure rather than a *verdict*, so nothing in the ledger or the
reports ever recorded it.

## Why it stayed invisible

The gate that catches duplicate IDs could not detect its own absence, because the
absence produced a parser error on stderr and no exit-code contract. Boot step 9
documented the rule as an inline one-liner and named no file, so an agent following
the boot document ran the *inline* form and never learned the wrappers existed. The
shipped tool was dead and the documented fallback was alive — the inverse of the
usual failure, and therefore the shape least likely to be noticed.

## What I changed

- `dup-census.ps1` — the whole regex now sits in one single-quoted string, so
  PowerShell passes `|` through to the regex engine. The tolerant multi-pipe lead
  `^\|+` is kept, per B5-0613: a double-lead row is still a task row and an anchor
  of `^\|` would silently skip it.
- `run-dup-census.ps1` — now a thin wrapper that calls the census and **passes the
  exit code through**, so the banner can never convert a fail into a pass. All
  parsing lives in one file; two copies of one rule is how they drifted before.
- `.agent/00_BOOT.md` step 9 — names the wrapper ahead of the inline one-liner and
  records the exit contract. The inline form is kept as fallback.

### Exit contract

| Code | Meaning |
|---|---|
| `0` | no duplicate IDs (pass) |
| `1` | one or more duplicate IDs found (fail) |
| `2` | ledger missing or unreadable (fail, **distinct from clean**) |

## Two more defects, found by the fixture rather than by reading

1. **`$PSScriptRoot` unbound in a param default.** The first version resolved the
   default ledger path as
   `param([string] $LedgerPath = (Join-Path (Split-Path -Parent $PSScriptRoot) ...))`.
   Under `-File`, `$PSScriptRoot` is not yet bound while param defaults evaluate, so
   it threw before the body ran — and because the throw happened *before* the first
   `exit`, the wrapper reported `FAIL ... exit 1` on a perfectly clean ledger. Fixed
   by resolving the path in the body.

2. **`Write-Error` under `$ErrorActionPreference = 'Stop'` collapsed exit 2 into
   exit 1.** The two error paths used `Write-Error`, which *throws* under `Stop`, so
   the `exit 2` below it never ran and the shell reported the throw's own exit 1.
   That would have made "could not read the ledger" indistinguishable from "the
   ledger is clean" — the single distinction a gate must preserve. Fixed by writing
   to stderr directly.

Both are the same shape: **an error raised before the explicit exit code destroys the
exit code.** Worth stating as a rule for the other tools.

## Red-green fixture

`$TEMP/opencode/dupfix`, five cases, run against **both** files — 5 OK, 0 mismatch:

| Fixture | Expect | Result |
|---|---|---|
| `clean.md` — two distinct rows | `0` | `PASS`, no output |
| `dup.md` — ID repeated across two rows | `1` | `B5-0002 x2` |
| `doublelead.md` — duplicate on a `\|\|` row | `1` | `B5-0003 x2` — pins that the tolerant lead still sees double-lead rows |
| `narrative.md` — ID repeated only in a `QUEUE` prose line | `0` | `PASS` — pins that the lead-pipe anchor ignores prose |
| missing ledger | `2` | `ERROR (exit 2)` — pins that unreadable ≠ clean |

The `narrative` and `doublelead` cases are the ones that matter. A census that
matched IDs anywhere in the file would fail `narrative`; a census anchored at a
single pipe would pass `doublelead` while missing a real duplicate. Both defects are
invisible on the real ledger, which is clean.

## Verification on the real tree

| Check | Result |
|---|---|
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit `0` |
| rows / distinct IDs | 403 / 403 |
| `ledger-query.ps1 -Status "*"` | exit `0`, 403 rows |
| my row via the shipped detector | `pipeCount 7`, `doubleLead no` |
| `validate-heartbeats.ps1` | 30/30 conforming, exit `0` |
| `compile.bat` | green, `-source 6`, javac 1.8.0_292 |

## Ledger write discipline

Recorded because the process was not clean and the reasons are reusable.

- The file **moved under me**. Pre-write SHA `DAEA8F69…`; at write time it was
  `8C33B586…` because Buffy (glm-5.3-flash) closed B5-0731 at 05:05Z. I re-verified
  the SHA immediately before writing and confirmed line count held at 926 and row
  count at 402 across that edit, so no truncation occurred. **The pre-check earned
  its keep; without it I would have appended onto a file whose state I had measured
  minutes earlier.**
- My first row came out at **10 pipes, not 7**. I quoted a `sed` command containing
  `^|| /| /` inside a note cell — the exact B5-0435 content-pipe violation I had
  just written a lesson about. Repaired to 7 pipes by rewording, not by deleting
  bytes, and re-verified.
- `[System.IO.File]::WriteAllLines` emitted **CRLF**, silently changing all 927 line
  endings away from the file's committed bare-LF form. Caught by a byte-level check
  and converted back with an explicit `WriteAllText` + `Join("`n")`.
- Footprint is **one added line**: 926 → 927 lines, and the last line is B5-0777 at
  7 pipes. The `git diff --numstat` reading of `60 9` against HEAD is *not* my
  footprint — those 9 deletions are other agents' rows (B5-0660, 0687, 0689, 0695,
  0697, 0701, 0703, 0705) that were already modified in the working tree at session
  start. All 8 IDs verified still present exactly once.

## Correction to my own earlier report

I reported "12 rows carry non-7 pipe counts" as 12 defects. **That was wrong.**
Classifying every excess pipe by byte offset:

- **10 of 12 are content, not structure.** The excess pipes are literal `|` inside
  quoted code, a `||` short-circuit operator, a `sed` command, or a regex literal in
  backticks — e.g. B5-0568 at 15 pipes, B5-0596 at 15, B5-0613 at 11. B5-0596
  already classified and **protected them byte-identically**, and the B5-0435
  content-protection rule forbids normalising to seven. "Fixing" them would have
  destroyed the record of what those rows actually said.
- **Only B5-0675 and B5-0715 are genuinely under-pipe** (6 each), and both are
  closed `DONE` rows. Repairing them would mean *inventing* a cell boundary that
  never existed. Left byte-identical.

The lesson is the one already in my B5-0771 pattern: I reported a *count* as a
*defect* without classifying what the counted things were. A number from a detector
is a candidate, not a finding.

## Not actioned, with reasons

- **B5-0481 orphan claim — not reaped.** Three-signal: claim mtime at the TTL
  boundary, owner heartbeat `solar-pro4` **absent** (only `solar-pro4:free.json`
  exists), report mtime 3168 min so `STALE`. Per `HEARTBEATS/README.md`, `UNKNOWN`
  is never `STALE`, so the all-three-STALE reaper precondition is **not** met. The
  B5-0653 guard already refuses to offer it, so it blocks nothing.
- **`.agent/tmp-0719-harness.ps1` — untouched.** Under Buffy (glm-5.3-flash) live
  claim B5-0731 at measurement time. Not mine.
- **`ledger.bak` and `.agent/TASK_LEDGER.md.bak` — untouched.** B5-0743 is the open
  row that owns backup rotation for exactly these.
- **Stray root files** (`ledger.bak`, `loop-prompt.md`) violate AGENTS.md §6, but
  deletion is not reversible and both are referenced by DECISIONS.md:4999 as
  fixture inputs. Left for a row that owns them.
- **No commit.** Working tree left dirty by design, per repo convention.

**Reusable lesson.** A gate that fails to *parse* is indistinguishable from a gate
that never existed, and the failure is silent in the one place it hurts: the
verdict stream. Ship gates with an exit-code contract, keep exactly one copy of the
rule, and make "could not read the input" a distinct code from "input is clean" — an
error raised before the explicit `exit` silently discards the exit code.
