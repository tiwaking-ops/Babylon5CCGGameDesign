---
document:
  title: "B5-1341 — CharacterCard reachability into applyGenericCardPlay"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1341: CharacterCard path census — all paths closed, one stale claim corrected

## Census result
Five paths checked. All CLOSED on the live tree (2026-09-30T23:01Z):

1. **AIPlayer.buildLegalActions** (AIPlayer.java line 191): CharacterCard → `recruitCharacter`, not `playCard`. `else` branch (line 214) excludes CharacterCard. AI path closed.
2. **MainWindow.playOnly** (MainWindow.java line 2147-2148): explicit `instanceof CharacterCard` refusal. UI door closed (B5-1171).
3. **MainWindow.updatePlayInitiateButtons** (MainWindow.java line 2134): NEW production caller of `canPlayCard` — B5-1109's "zero production callers in MainWindow" claim is stale; B5-1177 added this line after B5-1109 closed. Does not open a CharacterCard path (line 2130 excludes it).
4. **HeadlessHumanSeatProbe.chooseHumanAction** (HeadlessHumanSeatProbe.java line 343-344): test harness only, gated on `canPlayCard`, no CharacterCard in any test hand. Closed.
5. **HeadlessConformanceTest** PLAY_CARD tests (line 1612, 2976, 5025, 6281-6302): use AgendaCard/EnhancementCard/AftermathCard — no CharacterCard. Closed.

**applyGenericCardPlay** (GameController.java line 630-674) still corrupt for CharacterCard (charged raw cost, discarded, no effect) but unreachable from all current paths.

## Stale-claim correction
B5-1109 (DONE) reported "ZERO production callers in MainWindow" for `canPlayCard`. That was accurate when written. B5-1177 (DONE) added `rules.canPlayCard(humanPlayer(), selectedCard)` at MainWindow.java line 2134 as a production enablement check. The count is now 1, not 0. The B5-1109 claim should be read as time-stamped: accurate as of its close-out date, stale after B5-1177.

## Ranked input for post-B5-1047 engine fix
1. `applyGenericCardPlay` (GameController.java line 630): add CharacterCard branch routing to `canRecruit` + `sponsorCost` + `placeInSupportingRole`, matching RECRUIT_CHARACTER (lines 220-247).
2. `HeadlessHumanSeatProbe.chooseHumanAction` else branch (line 343): add explicit CharacterCard exclusion for consistency with AIPlayer, once the engine fix lands.

## Link
Full report: `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1341.md`
