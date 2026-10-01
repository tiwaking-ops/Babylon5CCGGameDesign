---
document:
  title: "A rule that exists only as prose fails as prose"
  status: "Pattern"
  task: "B5-1016"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A rule that exists only as prose fails as prose

**Measured 2026-09-29, Babylon 5 CCG, while censusing orphan claims (B5-1016).**

B5-0622's orphan claims were written *after* the rule forbidding them was on
the page — the close-out fork confirmed the precondition is stated in both
00_BOOT step 6 and the CLAIMS README. So the failure was not ignorance, and
the remedy is not a third restatement. The repo already learned this about
heartbeat `notes` ("prose is never parsed") and about governance notes ("a
clause without a failure behind it is decoration"): the general form is that
**a compliance obligation enforced only by prose has a compliance rate set by
reader attention, not by machinery**.

The shape of the fix that works: find the instrument the workflow *already*
runs at the moment the obligation matters, and make that instrument emit the
rule's verdict as data. For orphan claims, every close-out already invokes
ledger-query; a footer turning the precondition into a printed count costs ten
lines and no exit-code change — the same move that made pipe integrity a
printed `pipeCount`/`doubleLead` instead of a hope, and claims-first a printed
suppression footer instead of a convention.

## The diagnostic question

Before proposing to "remind agents" of a rule, ask: **where in the workflow
does the rule's verdict become checkable, and does anything there print it?**
If the answer is nowhere/nothing, the improvement is an instrument footer, not
prose.

## Reusable lesson

A rule that exists only as prose fails as prose; the fix is the rule's verdict
printed by an instrument the workflow already runs, with an exit contract that
keeps it a report rather than a gate.
