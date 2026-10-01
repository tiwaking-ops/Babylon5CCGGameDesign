---
document:
  title: "The gap between a file's assertion count and its runtime count is the finding"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0919", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0919", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: "B5-0919"
---

# The gap between a file's assertion count and its runtime count is the finding

Filed from B5-0919, which was asked to *assess* a 5961-line test file and
explicitly not to change it.

## The pattern

A size-assessment row arrives with its numbers already stated — "the 643 checks
run as one flat sequence in a single main" — and the natural response is to
confirm the numbers and propose a split. The numbers are the interesting part,
not the framing. Three counts of the same thing disagreed:

```
  654  check( call sites in the file
 -  1  the check() declaration itself
 - 18  inside a test method that is never called from anywhere
 +  8  net extra executions from checks inside loops
 ----
  643  what actually runs
```

The residual 8 is unremarkable. **The 18 are the whole finding**: an entire
test method, 94 lines, whose name appears exactly once in the whole tree — its
own declaration — while the file's Javadoc advertises the behaviour it asserts as
covered. Nobody noticed for the life of the file, because a size review counts
lines and a coverage review counts the suite, and neither counts the
difference.

## Why the arithmetic is the cheap part

Each term above is a one-line measurement against the file, and each is a
*different kind* of check, which is why all of them are needed:

* the declaration itself — a naive grep for `check(` always includes it;
* unreachable call sites — requires parsing which methods `main` actually calls,
  which is a different question from which exist;
* loop multiplicity — requires brace-depth tracking, since a check in a `for`
  runs N times and a `grep -c` says 1.

**Static site count and runtime count are different measurements, and neither
one is a proxy for the other.** The count of assertions in a file is not the
count of assertions that run. Only executing it tells you which, and the
difference is where the bugs live.

## The transferable form

When a report's own headline numbers disagree with each other, do not reconcile
them silently and do not pick the one that matches the row's framing. Write the
subtraction out. A number that arrives in the task description is a claim to be
*tested*, and here testing it cost three commands and produced a coverage hole
that a 5961-line refactor proposal would have faithfully preserved.

Related, not superseded: B5-0841 (prove the re-run; a claim about what execution
would do is cheaper to measure than to argue) — here the "re-run" was allowed, so
the measurement was direct, and the negative control was the byte count of the
file before and after, which was unchanged.

## Reusable lesson

Measure every count of the same thing before believing any of them, and write the
subtraction out: the gap between static call-sites and runtime checks found an
entire dead test method that the file's own Javadoc claimed was covered.
