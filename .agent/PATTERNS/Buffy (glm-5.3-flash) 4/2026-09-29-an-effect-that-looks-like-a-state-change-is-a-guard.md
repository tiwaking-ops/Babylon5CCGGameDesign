---
document:
  title: "An effect that looks like a state change is a guard against the engine's own prior action"
  task: "B5-0990"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# An effect that looks like a state change is a guard against the engine's own prior action

**One-line lesson:** when card text says "keep X from happening" and the engine
has already made X happen, implement the effect as the **inverse of the
engine's own prior action**, and pin the interpretation with a setup-first
conformance check that asserts the precondition before the effect.

## Shape of the case

Negotiated Surrender: "The loser may keep one fleet from being rotated." The
engine's conflict resolution rotates **every** committed fleet, so by the time
aftermaths play, "keeping a fleet from being rotated" can only mean restoring
one rotated loser fleet. Implementing it as a cancellation of a rotation that
has not happened yet would be untestable and wrong; implementing it as
`unrotate()` on one committed fleet is falsifiable: the check asserts
`fleet.isRotated()` before the effect and ready after.

The same shape produced a second finding on the first red iteration: the
fixture had not dealt the aftermath into hand, and `canPlayAftermath` refused
it. That failure was the gate working — the fixture was fixed, not the gate.

## What worked

- Read the engine action first (`RulesEngine.resolveConflict` rotate loop),
  then derive the effect as its inverse, before writing a line.
- Split the effect's beneficiaries by rulebook voice: the "keep" clause is the
  loser's, the "Gain 1 Influence" is the loser's — so the whole dispatch fires
  on `target != winner` only, which is what makes "never fires on a won one" a
  structural property rather than an added guard.
- Wire id-keyed dispatch first with the generic path as fallback, so adding
  the next aftermath is a table entry plus a check, not a play-site edit.
- Prove scope with a git hunk audit (1 + 1 + 2 hunks) before claiming the
  small-diff rule was honoured in a shared dirty tree.

## Related records

- Companion to the B5-0741 census that proposed this slice under its
  at-most-one rule; B5-0999 (gate-release re-census) and B5-0969 (symbol
  census) cleared its lane this session.
