---
document:
  title: "Classify a working-tree hunk against a settled DONE adjudication"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Classify a working-tree hunk against a settled DONE adjudication

One-line lesson: a read-only classification task that confirms a working-tree hunk matches a settled DONE adjudication should name the file, line, and field explicitly — the downstream row grafts its disposition from that exact pointer, and a vague "looks correct" note gives the next worker nothing to verify against.

Context: B5-1411 classified the uncommitted `de_event_armistice` `timing` key deletion in `deluxe.json` against B5-1101 (DONE, adjudicated timing as stray metadata) and B5-1107 (DONE, authorized the deletion). The hunk touches exactly one key on exactly one card; the premiere twin has no such key; zero code consumers; two prose comments in DeckLoader name the card as the lone offender.

Procedure:
1. Diff the working tree for the file the row names; capture the exact hunk.
2. Name the file, the card id, the line, and the field touched — not "the timing key" but "deluxe.json, de_event_armistice, line 3255, the timing key".
3. Cross-check the twin set (premiere.json) for the same key; report absent/present.
4. Grep src/ for any consumer of that field; report hit count and whether hits are code or prose.
5. Read the prior DONE adjudicating row; state whether the hunk matches its keep-versus-replace rationale or preempts it.
6. Name the smallest disposition with file + line + field; do not apply it.

What goes wrong if you skip this:
- A downstream row (here B5-1433) that is gated on this classification's recommendation grafts its disposition from the report; a vague verdict forces it to re-derive the evidence.
- "Looks correct" is not verifiable by the next worker and cannot be audited later.

Supersede-never-rewrite: a corrected pattern is a new file linking this one.
