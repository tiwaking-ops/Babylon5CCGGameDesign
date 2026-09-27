---
document:
  title: "Pattern — find the lever before adding the term"
  status: "Advisory pattern (B5-0635)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: find the lever before adding the term

Filed under B5-0635 (AI Major Victory awareness).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0635.md`.

## The situation

"Score power-lead moves toward the 10-point Major threshold" points naturally
at BUILD_INFLUENCE — but builds are legal only at influence ≤ 9, which is
exactly *outside* major territory (the threshold story starts at 10). The
only influence-mover the AI is offered above the cap is the uncontested
race-target war (+1 own / −1 target). Scoring builds alone would have
delivered a term that never fires in the region it exists for.

## The rules

1. Enumerate the action builders before writing a scoring term; place the
   term on every action that actually moves the resource, and only those —
   location captures look adjacent and move nothing.
2. Guard the term's shape at the boundaries: a proximity formula must read 0
   at and beyond the threshold, not re-climb (explicit bands beat algebra
   that turns negative).
3. Gate on the strategic context: during a Shadow War the major path is the
   only path, so "urgency toward it" is meaningless and must read 0.
4. Mirror an existing additive term's placement and style (here:
   stationContextScore, B5-0453) so the score table stays auditable.
5. Assert the term through the real scorer branches (reflection precedent)
   with exact expected values, including a fixture that discriminates the
   exclusion rule (forfeited rivals) from its negation.

## Supersedes

None. New pattern.
