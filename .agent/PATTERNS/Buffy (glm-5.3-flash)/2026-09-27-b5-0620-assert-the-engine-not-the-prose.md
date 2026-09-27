---
document:
  title: "Pattern — a conformance check asserts the engine, not the task prose"
  status: "Advisory pattern (B5-0620)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: a conformance check asserts the engine, not the task prose

Filed under B5-0620 (aftermath timing windows, B5-0594 audit gap 4).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0620.md`.

## The situation

The B5-0620 row required asserting "attached aftermaths discarded from the
registry after conflict resolution". The engine deliberately does not do this:
the B5-0338 `attachedAftermaths` registry is append-only by design, with no
clear API, so a persistent variant can inherit the D4 one-named-per-target
guard. Asserting the row's wording literally would have meant writing a check
that fails against the real engine — or worse, "fixing" the engine with a
clear call, which the row scope (suite file only) forbids.

## The rule

1. Read the engine first, and specifically read the code COMMENT that records
   the design intent — not just the method signatures. The append-only design
   was documented in `GameState`, not inferred.
2. When row wording and engine behavior disagree, the suite asserts the
   ENGINE (the thing the gate runs), and the divergence is filed in
   `docs/DECISIONS.md` and the close-out report — never silently absorbed by
   weakening the check into a tautology, and never silently enforced by
   editing game logic the scope forbids.
3. Assert the closest rulebook-faithful observable instead. Here: discard
   timing at the hand/discard-pile level (rulebook :424/:436) and registry
   state as a named PENDING seam, so the future persistent-variant slice has
   a ready-made red-to-green path.
4. Leave the follow-up as a seeded slice, not a scope grab.

## Supersedes

None. New pattern.
