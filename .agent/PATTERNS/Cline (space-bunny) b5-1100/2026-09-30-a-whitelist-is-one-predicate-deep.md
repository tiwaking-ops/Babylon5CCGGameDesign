---
document:
  title: "A whitelist is one predicate deep, and the sibling path may have none"
  status: "Advisory pattern (never canonical; copying confers no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-1100", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-1100", version: "space-bunny"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
task: B5-1100
---

# Reusable lesson

**A green leakage probe proves only that the path you probed is closed; the
sibling path that resolves the same record by a different route is a separate
question, and "0 occurrences" is not the same as "excluded".**

B5-1100 measured 200,000 starter-deck random slots and found zero
`RARE_WITHDRAWN` entries — and that zero was carried by a single enumeration,
`rarity != UNCOMMON && rarity != RARE`, not by any statement about withdrawn
cards. The record was playable by all four factions, so the very next line's
filter would have admitted it; it survived because the list omits the value, not
because anything excludes it. Meanwhile the *fixed*-slot path resolving the same
card by title applied no rarity check at all and was safe only because no data
file happened to name it.

Two transferable moves:

1. **Name the predicate, not the count.** "0 leaks" is unfalsifiable about the
   future; "the exclusion is one enum-comparison deep, and the next filter
   downstream would have admitted it" is a statement about the code that can be
   wrong in a way someone can act on. Report the guard that fires and what would
   have happened without it.
2. **Probe the sibling route even when the first answer is zero.** A zero result
   invites stopping. The value of the counterfactual is highest exactly when the
   measurement is clean, because that is when everyone is tempted to close the
   row. Ask what the *other* resolution path would return for the same record,
   and record it as an unfixed finding with its owning row rather than fixing it
   inside a row scoped to execution.
