---
document:
  title: "A status line is a claim about a decision until you read the decision"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A status line is a claim about a decision until you read the decision

**Task:** B5-0971 (dirty proposal triage, 2026-09-28).

**The trap.** A proposal's status line now reads "APPROVED by human ruling" — and the
naive triage treats the file as either a live candidate (old status) or settled truth
(new status). Both readings skip the actual question: does the cited decision exist,
does it say what the status line says, and are the decision record's own supporting
claims true? A status line is one agent's summary of a ruling; the ruling lives in
DECISIONS, and the difference between the two is exactly where drift hides.

**The three-check triage that settled both files.**
1. **Read the cited entry** (B5-0787, line 5631): both rulings present, scoped
   exactly as the status lines claim — including the easy-to-miss half (the approval
   is *design-only*; the card-content question stays open).
2. **Re-verify the record's supporting claims on the live tree** (two greps: the
   rejected §2.1 was indeed never implemented; the cross-check tool did land). A
   decision record that cites facts makes those facts checkable, and checking them
   is what upgrades the record from plausible to trusted.
3. **Watch the anchors, not just the claims** — the rejection paragraph's cited
   line number had drifted (252 → 310) because the file grew. Substance matched;
   recorded the drift so nobody "repairs" it by deleting a live comment. Line
   anchors are addresses in a moving document; verify the *neighbourhood*, then say
   the anchor moved.

**And classify honestly on the row's own axis.** The three-way triage (live candidate
/ decided elsewhere / withdrawn) had a fourth shape hiding inside "decided":
**partially decided** — one half of the power-split is canonical, the other half is an
open human question with no seeded row. Recording the partial state, with the open
question named, is the difference between a triage and a filing exercise.
