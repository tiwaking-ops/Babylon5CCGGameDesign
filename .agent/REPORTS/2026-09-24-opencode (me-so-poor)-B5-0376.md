---
document:
  title: "B5-0376 War-Conflict Engine - Phases B + C + Conformance Close-out"
  status: "Done"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "opencode/big-pickle"}
  created_date: "2026-09-24"
  last_modified_date: "2026-09-24"
---

# B5-0376 - War-Conflict Engine (Phases B + C + conformance)

**Agent:** opencode (me-so-poor) | **Date:** 2026-09-24 | **JDK:** 1.8.0_292

## Summary

Phase A (declaration and participation) and the harness `state()` fix were
completed earlier and reported in
`2026-09-24-poolside-s-01-B5-0376.md`. That claim went stale (released
09:35Z, >30 min TTL) and was reaped per 00_BOOT step 9 and re-claimed as
`opencode (me-so-poor)`. This session landed Phase B (location
capture/suppression), Phase C (attack integration), the proposal's
AI/UI consumer surface, and conformance tests 10-16. Suite is green:
308/308.

## Phase B - location capture/suppression

- `model/LocationCard.java`: new `effectsSuppressed` flag +
  `isEffectsSuppressed()` / `setEffectsSuppressed()`. Output gates:
  `getInfluencePerRound()` returns 0 while suppressed,
  `getMilitary()` returns 0, `getPrimaryStatValue()` returns 0.
- `engine/RulesEngine.java`: `resolveWarOutcome` made `public`.
  - capture branch: winner sets `capturedBy = winning faction`, suppresses
    the location, and calls the engine-local
    `removeLocationIncomeEnhancements(Player)` which discards the owner's
    ENH_LOCATION_INCOME enhancements at the location (per proposal capture
    semantics; the model layer stays engine-free).
  - recapture branch: winner faction == location's printed faction clears
    `capturedBy` and unsuppresses (income + military restored).
  - the tension-increment block reads the location's printed faction
    (`getFaction()`), not the mutated capturedBy, so occupy-before-tension
    stays correct.
- `engine/CardEffects.java`: `isLocationIncomeEnhancement(EnhancementCard)`
  helper over ENH_LOCATION_INCOME ("enh_exploitation",
  "de_enh_exploitation").
- `model/GameState.java`: `findLocationOwner` returns the `capturedBy`
  occupier first, then the list owner; this makes recapture war
  declarations legal.

## Phase C - attack integration (contested read)

- `model/Conflict.java`: `attackOccurred` field + `markAttackOccurred()`;
  `anyAttackOccurred()` returns the real committed-attack flag (the
  proposal's interim constant is gone).
- `engine/RulesEngine.java` `executeAttackConflictParticipant`: calls
  `conflict.markAttackOccurred()` after a successful committed attack.

## AI + UI consumers (proposal section 3.6)

- `ai/AIPlayer.java`: `buildLegalActions` offers DECLARE_WAR_CONFLICT
  actions (RACE_TARGET per enemy faction at war, LOCATION_TARGET per
  enemy-held location) gated by `canDeclareWarConflict`; MEDIUM/HARD
  `scoreAction` cases for both target kinds (medium: locations
  2+influence, races 4; hard: similar with a leader-aware bump).
- `ui/GameBoardPanel.java`: WAR banner via new `warConflictTitle(Conflict)`
  helper plus a null-guard for the card-less war conflict (the old
  `c.getCard()` path would NPE).

## Conformance

WAR suite in `HeadlessConformanceTest.testWarConflict` grown to 31 checks
(tests 10-16 added):

- uncontested race war: target loses 1 influence, winner gains 1;
- location-target war: declaration, capture marker, income/military
  suppressed to 0, tension incremented;
- recapture war: capture cleared, income and military restored;
- attack integration: declaration, fleet commitment, attack resolution,
  `anyAttackOccurred` after a committed attack, contested race war makes
  no influence swing.

`controlsCard` excludes `hand`, so attack-legality fixtures commit owned
fleets (added via `addFleet`, which sets the owner and live-list add).
The `state(Player...)` harness helper is players-only (the earlier
Phase A report recorded the state()-player-slicing bug fix).

## Verification

- Build: `b5ccg/compile.bat` -> "Build successful" on JDK 1.8.0_292
  (`-source 6 -target 6`, stdlib only, 1 expected bootstrap warning).
- Conformance: `HeadlessConformanceTest` -> 308/308 PASS (WAR 31/31).
- Smoke: `HeadlessSmokeTest` -> PASS (446 cards, 32 AI actions, 4/4 legal,
  round 1 in 19.2s).
- Java 6 gate: ripgrep for `->|::|stream(|computeIfAbsent|putIfAbsent|getOrDefault|@FunctionalInterface|new X<>|< >` over all touched files clean
  (only test-description arrow text in HeadlessConformanceTest).

## Scope notes

- `BABYLON5_CCG_RULEBOOK.md` not edited. Interpretations follow
  `docs/proposals/war-conflict-participation-rules-proposal.md`.
- No card data (`resources/`) touched.

## Files changed this session

- `b5ccg/src/b5ccg/model/LocationCard.java`
- `b5ccg/src/b5ccg/model/Conflict.java`
- `b5ccg/src/b5ccg/model/GameState.java`
- `b5ccg/src/b5ccg/engine/CardEffects.java`
- `b5ccg/src/b5ccg/engine/RulesEngine.java`
- `b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java`
- `b5ccg/src/b5ccg/ai/AIPlayer.java`
- `b5ccg/src/b5ccg/ui/GameBoardPanel.java`

## Claim

Reaped stale poolside-s-01 residual claim (released 09:35Z) per 00_BOOT
step 9 and re-claimed B5-0376 as `opencode (me-so-poor)` (claim file
`.agent/CLAIMS/B5-0376.json`, started 2026-09-24T10:40:00Z). Claim
released at close-out.