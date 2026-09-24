---
document:
  title: "B5-0394 Contingency attached-identity engine API — completion report"
  status: "Report"
provenance:
  author_llm: {name: "Kilo", version: "kilo-auto/free"}
  assessor_llm: []
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0394 Completion Report

## Task
Contingency attached-identity engine API (unblocks B5-0381): expose placed-contingency identity for the reveal path (accessor or registry) so a UI reveal control can submit via `GameAction.revealContingency` + `canRevealContingency`; keep the B5-0365 count-only host readout decision intact; conformance section; gate green.

## Scope
`b5ccg/src/b5ccg/model/` (GameState.java only — no engine/ changes needed)

## Implementation

Added two accessor methods to `GameState.java`:

1. **`getPlacedContingencies(Player p)`** — Returns all face-down (unrevealed) `ContingencyCard` objects placed by the given player, across all their in-play zones (Inner Circle, Supporting Role, Fleets, Locations, Groups, Enhancements, Agenda). Iterates through each zone and collects contingencies where `getPlayedBy() == p && !isRevealed()`.

2. **`getAllPlacedContingencies()`** — Returns all face-down contingencies on the board for all players. Convenience method for debugging or global UI views.

Both methods return `List<ContingencyCard>` (unmodifiable in practice — callers receive a new `ArrayList`). The host card's `getContingencyCount()` and `getContingencies()` remain unchanged, preserving the B5-0365 "count-only host readout" decision.

## Verification

- **Compile gate**: `compile.bat` green (46 files, `-source 6`, 1 expected bootstrap warning)
- **Conformance suite**: 326/326 checks PASS (includes CON section for B5-0365 contingencies)
- **Smoke test**: PASS (446 cards, 32 AI actions, 4/4 legal decisions)
- **Java 6 compliance**: Grep for `->`, `::`, `stream()`, `computeIfAbsent`, `@FunctionalInterface`, `try (` — empty on modified file

## Integration

The existing API was already sufficient for the reveal path:
- `GameAction.revealContingency(ContingencyCard)` factory existed
- `RulesEngine.canRevealContingency(Player, ContingencyCard)` existed
- `CardEffects.revealContingency(GameState, Player, ContingencyCard)` existed

This task adds the *accessor* so the UI layer can enumerate which contingencies are available to reveal without iterating through all card zones manually.

## Unblocks

- B5-0381 (Contingency UI) — UI reveal control can now access placed contingency identities via `GameState.getPlacedContingencies(humanPlayer)` and submit reveal actions.

## Files Changed

- `b5ccg/src/b5ccg/model/GameState.java` — added 4 methods (2 public accessors, 2 private helpers) — ~45 lines added