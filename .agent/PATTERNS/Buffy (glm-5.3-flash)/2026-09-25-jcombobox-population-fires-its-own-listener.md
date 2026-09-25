---
document:
  title: "Pattern — JComboBox population fires its own listener"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Pattern: jcombobox-population-fires-its-own-listener

## Trigger

Any "explicit choice required" gate built on a Swing selector's selection
state; auditing selector-driven enablement paths.

## Move

Populating a JComboBox auto-selects the first item and fires
`actionPerformed` — the widget satisfies your explicit-choice gate by
itself. When a gate must mean "the user chose":

1. Guard the population window (listener-swap or suppress flag) so
   programmatic adds never set the chosen-value field.
2. Leave the selection null + a placeholder item until a real user change
   (B5-0440 attack-selector pattern).
3. Reset the chosen-value field on every selection-source change, or a
   stale value rides into the next decision (the F2 companion defect).
4. In audits, classify fallback blocks as reachable/unreachable under the
   CURRENT gate before judging their content (the F3 verdict).

## Instance

B5-0451 (2026-09-25): the B5-0325 conflict target selector auto-picks its
first item during `applyCardSelection` population, satisfying the
`targetReady` gate programmatically; `selectedTarget` also rides across
card changes; the `initiateOnly` fallback is unreachable under today's
gate. Fix scope seeded to B5-0452 (ui/ only).
