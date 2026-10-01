---
document:
  title: "A singleton field is adjudicated by triangulation, not by its value"
  status: "Pattern"
  task: "B5-1023"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A singleton field is adjudicated by triangulation, not by its value

**Measured 2026-09-29, Babylon 5 CCG, while adjudicating `de_event_armistice`'s
`"timing": "ANY"` (B5-1023).**

One field on one record of 829, read by no code. Three hypotheses, opposite
consequences: unimplemented feature, missing schema, stray hand-edit. The value
`ANY` alone cannot decide it — any string can be *someone's* intent. Three
independent evidence classes can:

1. **Consumers, across all of history.** Not just today's tree: grep the frozen
   archive too. Zero consumers ever means no code path ever depended on the
   field, which kills the "feature awaiting implementation" hypothesis in
   practice — nobody ever wrote the reader, even when the data was current.
2. **Vocabulary.** A real structured field needs at least a two-value domain to
   be worth structuring. A domain of exactly `{ANY}` encodes no distinction —
   nothing to contrast, nothing to branch on.
3. **Internal consistency against its own record.** `ANY` on a card whose own
   prose says "Play before a conflict resolves" is a self-contradiction. A
   deliberate encoding agrees with its host text; an accident need not.

All three rejected the meaningful hypotheses independently, so the convergence
is strong even though each signal alone is suggestive only. The single-occupant
heuristic in the pool-baseline report ("sparse fields with a single occupant are
usually a hand-edit") pointed the same way.

## What NOT to do

- Do not propagate an undefined value to "complete the schema" — that converts
  one stray into 828 strays (the row's own warning: a typo becomes a standard).
- Do not silently delete, either: the field is evidence, and the record's own
  row (if any) is the place the verdict belongs.
- Do not seed a loader follow-up for a key that duplicates its own card's prose;
  the prose is the dataset's timing contract, uniform across ~96 events.

## Reusable lesson

Adjudicate a singleton field by triangulating consumers-history, vocabulary, and
internal consistency; when all three reject meaning independently, the verdict
is stray hand-edit and the fix is one authorised deletion row, never
propagation and never a silent edit.
