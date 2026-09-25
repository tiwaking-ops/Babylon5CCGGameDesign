---
document:
  title: "Pattern — quote the deferral when a later task reverses it"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: reversing a recorded deferral requires quoting it

**Lesson:** Several tasks in this repo exist to reverse a scope decision an
earlier task explicitly deferred (B5-0458 vs the B5-0328 note "tightening
would be a behavior change beyond F4"). A reversal that silently ignores
the deferral looks like a regression to the next auditor.

**Rule of thumb:**
1. Find and read the deferral text before editing (DECISIONS entry or
   ledger verify cell).
2. Quote its exact scope line in the code comment, the DECISIONS entry,
   and the report.
3. State explicitly what part of the old boundary still stands (here:
   Play Card breadth) so the reversal cannot be over-read as a license.
4. Verify the reversal with the regression probes that pin the ORIGINAL
   behavior (0328's own tests still pass after 0458 — they pin button
   mechanics, not the deferred breadth).
