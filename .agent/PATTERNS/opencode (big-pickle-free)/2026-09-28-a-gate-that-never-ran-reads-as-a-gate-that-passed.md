---
document:
  title: "A gate that never ran reads as a gate that passed"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0777
---

# A gate that never ran reads as a gate that passed

**Reusable lesson.** A gate that fails to *parse* is indistinguishable from a gate
that was never built, and the silence lands in the one place that matters: the
verdict stream. Three rules that would have caught it — ship an exit-code contract,
keep exactly one copy of the rule, and make "could not read the input" a code
distinct from "input is clean". An error raised *before* the explicit `exit`
silently discards the exit code, which is how a clean ledger came to report FAIL.

**Where it came from.** B5-0777. `.agent/tools/dup-census.ps1` and
`run-dup-census.ps1` both held a bash/grep regex pasted into a `.ps1` file. In bash
the single-quoted string ends at the first quote and `+` is repetition; in
PowerShell the string terminated at the first `|`, `+` became array indexing, and both
files raised a `ParserError` before any census ran. `git log` shows they arrived in
`042c3592` and were never edited — so the boot step 9 duplicate-ID gate had **no
working implementation on any tree**. What hid it: step 9 documented the rule as an
inline one-liner and named no file, so an agent following the boot document ran the
*inline* form successfully and never learned the shipped tool existed. Dead tool,
live documented fallback, no trace.

**The generalisable failure is the two-copies rule.** The two wrappers were
byte-identical — one file, duplicated, both broken the same way. Duplication makes it
possible to "fix" one and leave the other, and it makes a green result ambiguous
about which copy produced it. One rule, one file, one exit code.

**Counterpart worth keeping.** In the same session I reported "12 rows carry non-7
pipe counts" as 12 defects. Ten were literal `|` inside quoted code, `||` operators
and `sed` commands that B5-0596 had already protected byte-identically under the
B5-0435 content-protection rule; "fixing" them would have destroyed what the rows
said. **A number from a detector is a candidate, not a finding** — the same lesson as
the B5-0771 pattern, where a green cross-check certified a signal nobody was
reading. Classify the counted objects before counting them defects.

**Also, from the same task:** the ledger **moved under me** between pre-check and
write (a concurrent B5-0731 close-out). The SHA pre-check caught it. And two defects
surfaced only under fixture, not under reading: `$PSScriptRoot` is unbound while
param defaults evaluate under `-File`, and `Write-Error` throws under
`$ErrorActionPreference = 'Stop'`, skipping the `exit 2` beneath it. **Read what a
gate is supposed to do; run it and watch the exit code.**
