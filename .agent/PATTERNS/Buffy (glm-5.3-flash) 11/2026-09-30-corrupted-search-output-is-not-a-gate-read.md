---
document:
  title: "Corrupted search output is not a gate read"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# A gate verdict drawn from corrupted search output is not a verification — retract it in the same artifacts

**Trigger:** a verification (task status, gate precondition, receipt count) was
concluded from pattern-search output, and that output has shown corruption —
matched IDs rewritten mid-pattern, spliced rows, byte-shared note cells.

**Failure mode (B5-1163 instance):** treating the corrupted match as a clean read
and declaring a gate satisfied. The conclusion is not just wrong, it is
*unfalsifiable from that output* — you cannot tell which parts were the ledger and
which were the defect.

**Rule:**

1. Gate verdicts require line-anchored, byte-conservative reads
   (`grep -n "^| <id> |"` or a direct line-window read), taken twice when
   anything contradicts.
2. If a prior conclusion rested on output later found corrupted, **retract it
   explicitly** — in the ledger note, DECISIONS, and the close-out report — even
   when no work was done under it. A silent drop leaves the false verification
   standing as precedent.
3. Distinguish *conclusion corruption* (bad evidence) from *state corruption*
   (bad rows). A spliced note cell from another agent is state you preserve
   byte-identical and disclose; your own wrong gate verdict is something you
   retract and re-derive.
4. Retraction closes the false record, not the work: if nothing was done under
   the wrong reading, say so — the value is the corrected record.

**Reusable lesson:** verify a gate only with a clean, line-anchored read — never
with pattern-search output that has shown corruption — and put the retraction in
the same artifacts that recorded the wrong claim.
