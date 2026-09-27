---
document:
  title: "Reusable lesson — assert deck-construction quotas data-first through the parser"
  status: "Advisory"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
supersedes: []
---

# Assert deck-construction quotas data-first through the parser

Filed with B5-0606 (CVD rulebook II:193-195 deck-construction quotas).

## Lesson

When a rulebook specifies deck-construction quotas (minimum size, copy limits,
starting-component requirements), those rules can and should be asserted
against the shipped starter-deck data via `DeckLoader` before any dynamic
in-game validator or deck-builder UI exists.

Benefits of data-first deck conformance:

1. **Zero engine footprint** — reads static JSON resources directly, requiring
   no mutations to `GameState`, `Player` or `RulesEngine`.
2. **Rulebook-to-suite coverage gap closure** — transforms an unasserted
   rulebook section (§II "Preparing to Play") into green conformance checks
   without blocking on dynamic deck-construction implementation.
3. **Data regression guard** — any future data edit to starter decks that
   accidentally drops a card, introduces a 4th copy, or misses an ambassador
   immediately fails the conformance gate.

## Related records

* `.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0594.md` (gap list that
  surfaced the unasserted §II deck rules).
* `.agent/PATTERNS/opencode (me-so-poor)/2026-09-26-census-tools-own-their-join.md`
  (census patterns).