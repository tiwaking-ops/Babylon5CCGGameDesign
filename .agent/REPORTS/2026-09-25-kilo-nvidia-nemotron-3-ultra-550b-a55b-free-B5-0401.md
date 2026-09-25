---
author_llm: kilo (nvidia/nemotron-3-ultra-550b-a55b:free)
---

# B5-0401 — Tier-1 Remainder Action UI

## Summary

Implemented the Tier-1 remainder action UI controls for **Lead Fleet** (B5-0362) and **Use Rotate Effect** (B5-0366) in `MainWindow.java`. These controls complement the existing action-set UI from B5-0326 and B5-0380.

## Changes Made

### New UI Components

1. **Lead Fleet Control**
   - `leadFleetSelector` — JComboBox listing eligible unrotated fleets (no leader, ready)
   - `leadFleetButton` — submits `GameAction.leadFleet(leader, fleet)` 
   - Enabled when: ACTION phase, human's turn, selected card is a ready CharacterCard in Inner Circle or Supporting Role, at least one unrotated fleet without a leader exists

2. **Use Rotate Effect Control**
   - `rotateEffectKindSelector` — JComboBox with "Ability Boost (+1 D/I/L)" and "Sponsor Discount (-1 INF)"
   - `useRotateEffectButton` — submits `GameAction.useRotateEffect(assistant, ambassador, kind)`
   - Enabled when: ACTION phase, human's turn, selected card is a ready CharacterCard in Supporting Role, human has an ambassador

### Supporting Logic

- `refreshLeadFleetAndRotateEffect(Player, boolean)` — called from `refresh()` to update enablement based on game state and selection
- `updateLeadFleetButton(boolean)` — called when fleet selector changes
- `clearSelection()` — extended to reset `selectedFleet`, `selectedAssistant`, and new selectors/buttons
- `onCardSelected` — tracks `selectedAssistant` when a Supporting Role character is selected, triggers refresh

### Files Modified

- `b5ccg/src/b5ccg/ui/MainWindow.java` — added fields, toolbar components, enablement logic, and selection handling

## Verification

- **Compile**: `compile.bat` green (JDK 1.8.0_292, `-source 6`)
- **Conformance**: `HeadlessConformanceTest` 350/350 PASS
- **Smoke**: `HeadlessSmokeTest` PASS (446 cards, 31 AI actions, 4/4 legal)
- **Java 6**: grep for `->|::|stream\(|computeIfAbsent|@FunctionalInterface|try \(` on `ui/` — empty

## Notes

- Follows B5-0348 patterns for affordability/legibility gating
- Lead Fleet uses existing `RulesEngine.canLeadFleet` / `executeLeadFleet` (B5-0362)
- Use Rotate Effect uses existing `RulesEngine.canUseRotateEffect` / `executeRotateEffect` (B5-0366) with `RotateEffectKind.USE_ABILITY_BOOST` and `USE_SPONSOR_DISCOUNT`
- No engine/model/ai changes — UI only per task scope