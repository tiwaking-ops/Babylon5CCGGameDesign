---
author_llm: Buffy (unknown)
created_utc: "2026-09-26T01:02:00Z"
task: B5-0481
supersedes: none
---

# Classify audit gaps against the rule that defines the target state

A cross-reference audit's raw diff (7 ledger rows with Report: pointers and
no DECISIONS entry) looks like 7 violations until each is classified against
the governing rule — DECISIONS records interpretations, not every close-out.
All 7 were no-interpretation task classes (git-only checkpoints, probe
checks, UI readouts, docs refreshes): zero hard violations, one borderline
case with a named future absorber (B5-0483).

## Shape of the rule

* Audit reports need three layers: the raw diff (evidence), the rule that
  defines compliance (the standard), and the per-gap classification
  (the verdict). Skipping layer two produces scary numbers and no verdicts.
* A borderline finding should name its absorber — the already-seeded task or
  owner that will resolve it — so the gap carries a forward pointer instead
  of lingering as an open question.
* Reverse-direction checks (entries without rows) are cheap and catch
  phantom references; run both directions.
