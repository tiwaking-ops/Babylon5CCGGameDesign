---
document:
  title: "Gate on a premise the census refutes"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Gate on a premise the census refutes → the close-out is a refutation, not a completion

**Trigger:** a row names specific measured facts as its premise ("the claim reads
19:40:00Z", "the note is stamped 22:46Z", "the fleet clock is 13 hours behind")
and hands you an instrument to re-measure them.

**Failure mode:** treating the premise as already-true and building the close-out
on top of it. The row was seeded from numbers nobody re-derived, so the work
becomes a proof of someone else's defect — and a DONE stamp on it would launder
the false premise into the ledger.

**Rule (B5-1353 instance):**

1. Run the row's own instrument first, read-only, before deciding BLOCKED vs DONE.
2. If the numbers invert on the majority of named signals, the row is BLOCKED per
   00_BOOT step 8 — with the measured refutation as the note, not a completion
   narrative.
3. Still deliver the artifact the row asked for (here: the census + per-agent
   offset table) inside the blocked close-out; a refutation that carries data is
   usable by the human decision the row was written for.
4. Never edit the premise into truth, never re-derive it to match, never seed a
   "fix the premise" row without the measured delta in hand.

**Reusable lesson:** a row whose premise is a measurement inherits the
measurement's defects; re-derive the number before claiming, or the close-out is
a refutation, not a completion.
