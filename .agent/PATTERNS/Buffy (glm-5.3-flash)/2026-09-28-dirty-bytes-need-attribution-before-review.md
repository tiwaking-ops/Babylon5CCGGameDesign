---
document:
  title: "Dirty bytes need attribution before review"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Dirty bytes need attribution before review

**Task:** B5-0965 (dirty AIPlayer.java review, 2026-09-28).

**The trap.** A review row says "read the diff hunk by hunk" — and the honest first
reading is unsettling: 154 insertions of scoring logic nobody's session just wrote.
The reflex is to review it as mystery work, line by line, against baselines. The
correct first step costs one grep: find the *ledger row that owns these bytes*.
Here, a B5-0727 comment block inside the diff carried its own row id, the ledger read
`| B5-0727 | DONE | … | Buffy (glm-5.3-flash) |`, and B5-0703's pattern file named the
second hunk family. Mystery became verification: does the tree match what the DONE
rows said they delivered? (It does.)

**Attribution before evaluation, because the two verdicts differ.**
- Unattributed dirty bytes ⇒ review for safety, consider BLOCKED-on-foreign-work.
- Attributed dirty bytes ⇒ verify the deliverable against its row's own contract and
  leave it alone; the commit decision belongs to the checkpoint rows and the human.
A reviewer who skips the attribution step either re-reviews already-closed work or,
worse, "fixes" a DONE row's delivered bytes in the name of tidiness.

**"Order preserved" is two claims; say which one you proved.**
1. *Identity* — the expressions produce identical values by construction (added terms
   are literal 0 today: dead branches, zero-returning helpers).
2. *Pool invariant* — the expressions differ only under state that production cannot
   currently reach (`getPower() == getInfluence()` because no POWER bonus exists
   outside conformance fixtures — verified by grepping every grant site, not by
   trusting a comment).
Both make the change safe today; only the first survives a data change for free. A
verdict that says just "no behaviour change" has not said whether tomorrow's pool edit
silently re-tunes the AI.

**And measure the gate on the bytes you are reviewing, not on a rebuild of what the
tree should be:** the conformance suite (643/643) and smoke test both ran directly
against the dirty working tree, which is the only configuration whose verdict
describes the bytes in question.
