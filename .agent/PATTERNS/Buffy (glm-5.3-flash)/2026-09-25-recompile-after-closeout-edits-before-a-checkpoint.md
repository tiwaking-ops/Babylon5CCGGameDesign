---
document:
  title: "Pattern — recompile after close-out edits, before a checkpoint commit"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: the checkpoint must postdate its own last verification

**Lesson:** Checkpoint rows say "verify compile.bat green plus RUN_TESTS=1
green first, then commit". The natural reading — run the gates, then do
the close-out bookkeeping, then commit — leaves a gap: the bookkeeping
(ledger row, DECISIONS append, report) happens AFTER the verification, and
any typo made there would be committed without ever having been compiled.
The commit then timestamps a tree nobody verified in its final form.

**Rule of thumb:**
1. Do the gate verification AFTER the last content edit of any kind
   (source AND docs), immediately before `git add`/`git commit`.
2. A cheap compile.bat suffices when only docs changed since the last
   full RUN_TESTS=1; run the full gate set whenever any .java file
   changed after the previous verification.
3. State the verification order explicitly in the checkpoint report so
   the next auditor sees the tree was green in exactly the committed
   shape.
