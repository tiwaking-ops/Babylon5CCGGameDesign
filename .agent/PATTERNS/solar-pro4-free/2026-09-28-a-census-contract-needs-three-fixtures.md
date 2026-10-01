---
document:
  title: "A duplicate-ID census contract is pinned by three fixtures, not one"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A duplicate-ID census contract is pinned by three fixtures, not one

**Author:** Solar Pro4 (solar-pro4:free) · **Date:** 2026-09-28 · **Task:** B5-0765

## Summary

A single clean-ledger fixture (exit 0 + empty output) cannot tell the difference
between "the census correctly passes on a clean ledger" and "the census never
outputs anything, ever." To pin the contract you need three independent fixtures:

1. **dup-positive ledger** (exit 1 + non-empty output naming the dup) — proves
   the census CAN fail, i.e. that a duplicated id is detectable.
2. **clean ledger** (exit 0 + empty output) — proves the census passes on a
   clean ledger.
3. **prose-only id mention** (exit 0 + empty output, where the id string appears
   only inside a narrative/QUEUE line and not on a task row) — proves the
   lead-pipe anchor does not false-positive on prose, i.e. the regex anchored on
   `^\|+\s*(B5-\d{4}[a-z]?)\s*\|` correctly ignores non-row text.

A single fixture of type 2 is consistent with both a working census and a broken
one that never reports anything; only the combination of all three makes the
contract mean something.

## Evidence

B5-0765 fixture run (2026-09-28, repaired wrappers post-B5-0729):

- fixture-a-dup.md: exit 1 + "B5-0765 x2" → dup detection confirmed.
- fixture-b-clean.md: exit 0 + empty → clean pass confirmed.
- fixture-c-note-only.md: exit 0 + empty → prose-ignore confirmed.
- BASELINE real ledger: exit 0 + empty → no collateral contamination.

Post-write duplicate-ID census on the real ledger: exit 0, 0 duplicate task IDs.

## When to use this pattern

Any time you are writing a close-out report that claims a tool "works" or "passes"
based on a single fixture: ask whether a second fixture with the opposite
expected result would change your conclusion. If it would, add it before you
finalise the report.

## Related

- B5-0765 report: `.agent/REPORTS/2026-09-28-solar-pro4-free-B5-0765.md`
- B5-0729 report: `.agent/REPORTS/2026-09-27-Cline-(space-bunny-free)-B5-0729.md`
  (the wrapper repair this fixture run tests against)
