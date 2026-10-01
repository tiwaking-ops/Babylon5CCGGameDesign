---
document:
  title: "Cross-check a diff-derived census against git status before publishing"
  status: "Pattern — advisory only, never canonical"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1531
supersedes: null
---

# Cross-check a diff-derived census against `git status`

A census built from `git diff` inherits every flag that shaped that diff.

Measured on B5-1531: the census ran `git diff -U0 -- b5ccg/src/` with
`-c core.autocrlf=false -c core.safecrlf=false` added for no reason other than to
silence git's `LF will be replaced by CRLF` warnings. Disabling `autocrlf` makes
git compare bytes rather than normalised content, so **every CRLF/LF difference
became a whole-file rewrite**. The real tree — 8 modified files, 49 hunks, 1251
added lines — was reported by the flagged run as 50 files, 66 hunks, 18972 added
lines: a 15x inflation.

**The failure was silent.** The run produced a well-formed per-file table, totals,
and three classification branches. Nothing errored. Every downstream percentage
would have been computed from the inflated base and looked publishable.

**What caught it** was a second, independent count of the same population:
`git status --porcelain` said 8 modified files, and the run said 50. Two
instruments, two answers, and the cheap one is the one that was right.

The rule:

- Any census derived from `git diff` gets its file count and hunk count
  cross-checked against `git status --porcelain` and `git diff --stat` before the
  numbers are written anywhere.
- A tolerance or noise-suppression flag added *purely to reduce output* is assumed
  to change semantics until proven otherwise. Ask what the flag changes about what
  the tool *measures*, not just about what it prints.
- When two instruments disagree, discard the derived census and re-run without the
  flag. Do not average, and do not report the larger one as "including more".

The wider form: **a measurement's noise-suppression settings are part of its
definition, so they must be published with its results.** A census that does not
name its flags is not reproducible, even when it is correct.

Related: the same report (`docs/reports` sibling
`.agent/REPORTS/2026-10-01-Kilo (kilo-auto-free) 4-B5-1531.md`) records the
complementary finding from the same row — a presence test for ownership markers
passes on 75.5% of hunks while 45.9% of those labelled hunks still cannot name
their owner, which is the general shape of this pattern: **the instrument reports
a plausible number, and the plausibility is what carries the error forward.**

**Reusable lesson:** silence a tool's noise by changing what it measures only if
you have checked that it does not; validate every derived census against a second
instrument that shares none of its flags.