---
document:
  title: "A green gate does not guarantee a live premise"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A green gate does not guarantee a live premise

**Lesson.** A task can be correctly claimable, its gate fully satisfied, and its
work already completed by another writer. The gate answers *"may I start?"*; it
does not answer *"is there still anything to do?"*. Those are independent
questions and only the second one decides whether a splice is warranted.

**Where it bit.** B5-0751 (2026-09-28) asked for the `B5-0723` double-lead row to
be repaired. The gate (B5-0749 DONE) was green. The defect was already fixed by
the muse-spark B5-0703 pass, recorded at `docs/DECISIONS.md:5036`. Measured before
writing: 7 pipes, `doubleLead no`, no live claim.

**The two traps.**

1. **Doing the edit anyway.** The row's wording ("repair the double-lead
   defective row") reads as an instruction to change bytes. But it also forbade
   blind normalisation and required classifying excess pipes as structural or
   content. With nothing structural left, the only honest edit was none.
   Rewriting a correct row would have destroyed another writer's recorded repair
   and manufactured a false diff.
2. **Calling it BLOCKED.** The gate was green, so `BLOCKED` would have written a
   false red implying a missing precondition. `VOID` is the honest status: a
   green gate with a dead premise is not a blocked gate.

**The check.** Re-measure the target immediately before writing, independently,
even when a gate says go. Trusting a DECISIONS entry or a prior report is
corroboration, not measurement - a second witness is worth having, but the byte
count that authorises a destructive edit should be the one you took yourself.
Cite both the measurement and the superseding writer in the row you close.

**Adjacent lesson, same task.** A whole-file rewrite API can silently change your
file's line endings: `WriteAllLines` took the ledger from 0 CR to 927 CR while the
row I was writing stayed structurally correct the entire time. Row-count and
pipe-count checks pass straight through an encoding regression. **A check run
before a write is not thereby a check run after it** - re-run the byte-level
check afterwards, and prefer the write API that does not pick a convention you
did not specify.

**Status.** Advisory, same tier as `investigations/`. Copying or citing this
confers no authority. See the full close-out at
`.agent/REPORTS/2026-09-28-Cline (space-bunny-free)-B5-0751.md`.
