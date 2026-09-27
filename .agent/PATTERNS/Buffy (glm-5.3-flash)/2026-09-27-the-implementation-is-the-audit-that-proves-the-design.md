---
document:
  title: "The implementation is the audit that proves the design"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# The implementation is the audit that proves the design

**Source task:** B5-0691 (implementing my own B5-0669 proposal, steps 2–4).
**Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

The proposal was written from static reads and still missed three things only
implementation could surface: same-race tension had to be *directional* to
match the landed matrix's semantics (the proposal said "a parallel matrix" and
left directionality unstated); the :992 rule has TWO entry paths (end-of-turn
tension-5 AND declaration/war-effect) and a test harness that tries to
side-step the second manufactures an unregistered machine; and a per-faction
field initialised from another field hits Java's initializer-order rule. None
of these were design errors exactly — they were unstated details the design
prose had no reason to resolve until code had to.

## The transferable rule

Treat implementation as the design's verification pass:

1. **Record every deviation as you hit it** (proposal text → DECISIONS entry),
   never silently redesign — the proposal file stays frozen, the deviation log
   is what keeps the two honest.
2. **When a test needs an engine behaviour that doesn't exist, add the engine
   API, not the workaround** — a construct-in-test machine skipped the
   GameState registration that the real entry path performs; the API fix made
   both the test and the engine correct.
3. **Fixture failures are signal** — check whether the fixture mis-reads the
   design (directional vs symmetric) before deciding the engine is wrong.

## Anti-patterns this heads off

- "I wrote the proposal, so the implementation will match it" — authorship
  bias is exactly why the deviation log must be explicit.
- Making tests pass by constructing parallel state outside the engine —
  the test then verifies a shadow implementation nobody ships.
- Leaving unstated semantics (directionality, entry paths, initializer
  ordering) to chance — each surfaces as a compile error, a failed check, or
  worse, a silent divergence.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0691.md`.
