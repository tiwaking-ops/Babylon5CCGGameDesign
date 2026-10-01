---
document:
  title: "A predicate is a dispatch table in different syntax"
  status: "Pattern"
  task: "B5-1033"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A predicate is a dispatch table in different syntax

**Measured 2026-09-29, Babylon 5 CCG, while tracing the AFTERMATH class
(B5-1033).**

The row's suspicion was reasonable: 117 records keyed on one string field, and
"a search of the engine finds zero case-style dispatch entries". The class
turned out fully reachable — because its dispatch is `AftermathCard.isEligible`,
seven `String.contains` guards in the *model*, not a switch in the engine. A
census that greps for `case` statements (or id-keyed registry tables, the
CardEffects shape) structurally cannot see this shape, and reports a live
pipeline as a dead one.

## The general rule

When a field is consumed by a predicate rather than a table, the consumption
census must include:

1. predicates that `contains`/`startsWith`/`matches` the field's values
   (grep the *vocabulary tokens*, not the field name alone);
2. model-side evaluation called from engine gates (`isEligible` inside
   `canPlayAftermath`);
3. the AI offer path, which keeps a class reachable even when no human UI
   dispatches it.

The flip side: this same flexibility is what makes silent dead cards possible
elsewhere — a token nobody implements (`PSI` today: implemented, used by zero
records) is invisible until a record starts using it. The vocabulary census
(engine tokens vs data values, both listed) is the cheap instrument that
catches it.

## Reusable lesson

A class with no switch statements can still be fully dispatched — trace
predicates over the field's value vocabulary and their callers before
declaring a data-keyed class unreachable.
