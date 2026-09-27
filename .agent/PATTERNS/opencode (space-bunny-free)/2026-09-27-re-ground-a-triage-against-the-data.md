---
document:
  title: "Pattern - re-ground a prior triage against the data, not the rulebook"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Re-ground a prior triage against the data, not the rulebook

**Advisory only.** Same tier as `investigations/`, never canonical. Copying or citing
this confers no authority.

A prior close-out's gap list is a set of *claims about cards and code*, not facts
about them. Re-verify each candidate against the artefact it would touch before
seeding it.

## The instance

B5-0639 produced remainders R1-R15 from a rulebook-section triage, each with a
grounding. R7 (Vorlon/Shadow marks) was grounded in the rulebook :830 example that
the Agenda "Servants of Order" provides 1 Vorlon Mark. The repo's own record:

- `de_agenda_servants_of_order` text carries a *different* ongoing effect
  (Inner-Circle-count influence gain) and **no mark at all**.
- Searching **both** card files for the word `mark` in any card text: **0 of 383
  deluxe, 0 of 446 premiere**.

So R7 is data-gated. Seeding it as model work would have produced a task with
nothing to assert - the exact waste the "discovery before seeding" discipline exists
to prevent.

The same pass found R15 mislabelled in kind: `power` over
`b5ccg/src/b5ccg/model/Player.java` is **zero hits** and no Power field exists in
`model/`, so rulebook :1034 is *vacuous* today, not *missing*. "Unimplemented" and
"would have no effect even if implemented" need different task shapes.

## The rule

A rulebook example that names a card, a subtype or a field is a claim about an
artefact. The artefact file is where the claim is checkable, and it disagrees with
the book often enough to matter. When a gap list is inherited rather than
re-derived, check every example-grounded row against the data first - and when
several rows fail the same way, seed the reclassification itself as the task.
