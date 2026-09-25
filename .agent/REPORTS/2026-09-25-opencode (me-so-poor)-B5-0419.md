---
author_llm: opencode (me-so-poor / big-pickle)
---

# B5-0419 — War-conflict scenario probe

**Status:** DONE
**Agent:** opencode (me-so-poor) / big-pickle
**Date:** 2026-09-25

## Summary

New standalone harness `b5ccg/src/b5ccg/engine/HeadlessWarConflictProbe.java`
drives `DECLARE_WAR_CONFLICT` end to end on real loaded card data
(`DeckLoader.loadBothSets()`), through the same seams the controller reads.
**45 checks, all PASS, exit 0.** No game-logic or suite edits — new file only.

## What was verified (scenario by scenario)

**S1 — declaration legality** (at peace → at war → wrong target):
- At peace, `canDeclareWarConflict` and `canInitiateWarConflict` are both
  false; entering war lets BOTH sides declare.
- The `GameAction.declareWarConflict(RACE_TARGET, minbari, null)` factory
  shape is confirmed: type `DECLARE_WAR_CONFLICT`, null card, race target in
  `getTarget()`, no location — exactly what GameController's
  `DECLARE_WAR_CONFLICT` branch (GameController.java:412) consumes.
- The resulting `Conflict`: `isWarConflict()` true, null card, `MILITARY`,
  `influenceReward == 0`.
- `canJoinConflict` is a tension-matrix gate: a peaceful third party cannot
  join a war conflict; it can only after entering war; a declaration against
  a race that has left the war is refused (returns null).

**S2 — tension increment, clamped at 5:**
- An uncontested race war (empty opposition, no attack) resolves to a target
  `-1` / winner `+1` influence swing and `+1` tension toward the target
  faction (minbari → narn).
- Pre-raise the pair to 5 and resolve a second uncontested war: tension stays
  **5**, not 6 — the clamp is real.

**S3 — location capture and recapture on a real loaded location:**
- A real Centauri `LOCATION` card from `premiere.json` is owned by the
  Centauri player; a Narn `LOCATION_TARGET` war is legal while at war.
- Initiator win **captures**: `capturedBy` set, effects suppressed, income
  suppressed to 0, military suppressed to 0. Tension runs toward the printed
  faction owner (Centauri → narn).
- The printed-faction owner's **recapture** war clears `capturedBy`, restores
  effects, and restores the original income and military values.

**S4 — the "all-supported uncontested" read, both ways:**
- Truly uncontested (no opposers, no attack): target loses 1, winner gains 1.
- Contested by **opposers** while the initiator still wins on support:
  no swing in either direction.
- Contested by an **attack** (`executeAttackConflictParticipant` — real engine
  path that commits + marks `attackOccurred`): initiator still wins support
  after mutual neutralization, yet the contested read suppresses the swing.

## Gates

- `compile.bat`: `Build successful` on JDK `1.8.0_292`, `-source 6 -target 6`
  (1 pre-existing bootstrap warning; pre-existing unchecked-op note on
  MainWindow — both unrelated to this task).
- Probe run: `java -cp out b5ccg.engine.HeadlessWarConflictProbe` → all 45
  checks PASS, exit 0.

## Notes

- Commit fleets in S4 use synthetic fixtures with chosen military values
  (B5-0405 precedent): the war pipeline itself — declaration, targets,
  tension, capture/recapture — runs on real loaded data.
- The mutual-damage math in S4c (attack damage 3 ≥ ability 3 neutralizes both
  the attacked support fleet and the attacker) is reflected in the support
  totals (6 vs 3), confirming the contested read holds under the real attack
  code path.
- No src/resources edits beyond the new harness file.