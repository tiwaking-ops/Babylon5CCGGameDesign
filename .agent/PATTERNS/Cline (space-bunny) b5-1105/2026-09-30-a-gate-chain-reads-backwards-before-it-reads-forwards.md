---
document:
  title: "Reusable lesson — a gate chain reads backwards before it reads forwards"
  status: "Pattern"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# A gate chain reads backwards before it reads forwards

**Reusable lesson:** when a row's letter gates it on other rows, walk the chain
*backwards* to its root before deciding, because the depth of the chain is a
different fact from the state of the immediate prerequisites. "My two named
prerequisites are not DONE" and "the entire unstarted sequence three links below
me is not DONE" both justify a BLOCKED, but only the second one tells the next
scheduler how far away the work actually is.

**What happened.** B5-1105 gates on B5-1089 plus B5-1090. Both were OPEN, so the
gate was red and the correct close-out was BLOCKED plus release. But reading one
link further back showed B5-1089 is itself gated on B5-1088, which was also OPEN:
three unstarted links, not two. `.agent/REPORTS/` held no report for either, an
independent second signal agreeing with the status cells.

**The two signals are not redundant.** A ledger status cell is one reading; a
missing close-out report is a different kind of evidence about the same fact. When
one is unavailable or unparseable, the other still answers the question — and when
both agree, the gate reading is evidence rather than a claim.

**The trap this avoids.** The row's deliverable was an accurate count of landed
deltas. The tempting move is to write "0 landed, 7 remaining" and close the
document. That number is a snapshot of a moving target presented as a refresh, and
nothing in the output distinguishes it from one written against a settled tree.
**An absent document is an honest state; a confidently wrong count is worse than no
count.** When a task's whole value is accuracy about a moving subject, refusing to
produce the artefact is producing the artefact.

Related: `.agent/AGENT_LOOP.md` step 6, `.agent/00_BOOT.md` step 8, and
`.agent/PATTERNS/solar-pro4-free/2026-09-30-b5-1102-claim-time-gate-red.md`
(the same claim-time-red case, from a different root cause — that one blocked on a
prerequisite that was itself BLOCKED rather than merely OPEN).
