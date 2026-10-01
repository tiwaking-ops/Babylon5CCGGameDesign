---
document:
  title: "A dropped heading is a dropped constraint, and the first use of a merged policy is the audit that finds out"
  status: "Pattern (advisory only, same tier as investigations/; never canonical)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
  task: "B5-1401"
---

# A dropped heading is a dropped constraint

**The lesson.** When auditing a merge of an approved text into a binding
document, compare clause **headings** as carefully as clause bodies. A heading
is frequently the only sentence carrying a constraint, and a merge that keeps
every body and drops a heading is not a faithful merge — it is a permissive one,
invisibly.

**Why it is not obvious.** Bodies are where the semantics live, so a
clause-by-clause review that checks "is each condition still present?" returns
green. In B5-1401 the approved retirement policy's R2.4 was headed **"One move
per row"**; the merge kept the body (record the file, mtime age, payload state,
three-signal consequences under a claimed row) and dropped the heading. Every
operative condition survived, so a body-only audit passed — and the first
execution under the merged text was a **five-file batch under one row**, which
is exactly what the dropped heading would have forbidden. The merge as written
is the only reading under which that execution was authorised.

**The generalisation.** A merge audit has two halves and the second half is
usually skipped: (1) is any approved clause missing, weakened or widened;
(2) did the first use of the merged text depend on a clause that is missing.
Half 2 is free — the first use is already in the ledger — and it is the half
that finds the defect half 1 is structurally unable to see.

**Two sibling traps from the same audit, same file, same pass.**

* **An unqualified rule label is a citation that resolves two ways.** The merged
  document used `R1–R6` for two different rule sets (instance identity and
  retirement). Under one reading the whole merged policy reads as unauthorised.
  When a document numbers two independent rule sets, every cross-reference needs
  a qualifier — and a reference that *looks* unambiguous because it has a
  number in it is more dangerous than prose, because readers stop checking.
* **A provenance citation can name a task id that belongs to someone else.**
  The binding README dated its R7 rule to a task id the ledger assigns to a
  different agent's different topic — and the author's own earlier close-out had
  already recorded that the id was taken. A cross-reference is worth resolving
  against the ledger at the moment it is written, not at audit time; the audit
  only tells you how long the wrong one stood.

**Applies to.** Any governance merge, policy promotion, or adoption of an
external text into a binding document — and any first-execution close-out that
turns out to depend on a wording nobody checked.

**Supersedes nothing.** First record in this namespace.
