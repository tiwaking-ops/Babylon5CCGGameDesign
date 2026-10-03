---
document:
  title: "Starter-deck builder floor conformance probe proposal"
  status: "Proposal"
provenance:
  author_llm: {name: "GitHub Copilot (Auto mode) 1832", version: "Auto mode"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Starter-deck builder floor conformance probe

## Problem

The existing CVD section in `HeadlessConformanceTest` checks the starter-deck
JSON directly and accepts a 45-card floor. It does not execute
`StarterDeckBuilder.build`, so a missing fixed card, a fixed-count mismatch, or a
short random pool can produce a short playable deck without failing the suite.
B5-1459 measured the current fixed side as 50 cards for HUMAN, CENTAURI,
MINBARI, and NARN, and measured the builder's intended total as 50 fixed plus
10 random cards. B5-1605 additionally confirmed that `DECK_SIZE` is currently
defined but has no consumers.

## Proposed probe

Add one builder-backed subsection to CVD (or a named `CVD-BLD` section called
from the existing conformance runner). It must:

1. Load the production pool once with `DeckLoader.loadBothSets()`.
2. For each of `HUMAN`, `CENTAURI`, `MINBARI`, and `NARN`, invoke
   `StarterDeckBuilder.build(faction, pool)` with a fixed random seed so the
   probe is repeatable.
3. Assert the returned deck size is exactly
   `StarterDeckBuilder.DECK_SIZE` (currently 60), not a literal 60.
4. Assert that all fixed-list rows for the faction resolve through the same
   id/title fallback used by the builder and sum to exactly
   `StarterDeckBuilder.FIXED_TARGET` (currently 50). This check must count
   row multiplicities, including count-3 entries, and must fail if any fixed
   row is missing; it must not infer the fixed count from the final 60-card
   list, because the ten random cards are intentionally indistinguishable there.
5. Assert `StarterDeckBuilder.findAmbassador(deck, faction)` is non-null and
   has the expected faction. This is the built-deck assertion, complementing the
   existing JSON-key census.
6. Convert resource-load or builder exceptions into named failed checks with
   the exception message; do not print a warning and continue as success.

The fixed-list check should share the builder's resource inputs and title
fallback contract rather than introduce a second card-data fixture. If the
production builder does not expose enough information to make the fixed portion
observable, the implementation should add a small package-visible diagnostic
seam or return type before the probe is added; it must not classify cards by
position in the final shuffled deck.

## Acceptance criteria

* The four factions each produce a deterministic builder-backed check for:
  exactly 60 total cards, exactly 50 resolved fixed cards by multiplicity, and
  one faction-matching ambassador.
* A fixture or temporary resource with one missing fixed row, a 49-card fixed
  side, or fewer than ten available random candidates fails a named check.
* The probe exercises the production builder and both id and title fallback
  resolution; the old JSON-only `>=45` CVD checks remain intact.
* The implementation remains Java 6 and stdlib-only, and no deck or card
  resource is changed by this proposal.

## Non-goals

This proposal does not change deck construction, promote the 45-card rulebook
minimum to a new game rule, or repair the existing stderr-only diagnostics.
Those are separate implementation decisions.
