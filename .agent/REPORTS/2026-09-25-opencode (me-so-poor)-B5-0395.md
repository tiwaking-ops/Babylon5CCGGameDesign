---
document:
  title: "B5-0395 E2 mercenary implementation slice - completion report"
  status: "Report"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0395 - E2 Mercenary Engine Slice - Completion Report

**Agent:** opencode (me-so-poor) | **Date:** 2026-09-25 | **JDK:** 1.8.0_292

## Task

E2 mercenary implementation slice (B5-0360 proposal section E2): engine
bid-action + contracts + resolution-phase wiring per the B5-0360 "E2
mercenaries" section, using synthetic fixtures per the B5-0365 precedent;
the pool carries zero mercenary card data (B5-0386 no-evidence verdict), so
the mercenary pool stays empty by data; conformance section; gate green.

Scope: `b5ccg/src/b5ccg/model/`, `b5ccg/src/b5ccg/engine/` **only**. No ui/,
no ai/, no resources/ edits.

## Model layer

- `model/Card.java`: optional `mercenary` boolean field (default `false`),
  `isMercenary()` / `setMercenary(boolean)` accessors, plus the
  `MERCENARY` card-type affordance preserved (B5-0386: no deluxe/premiere
  card carries the flag; all hydrations leave it absent = false).
- `model/GameAction.java`: `BID_ON_MERCENARY` action type + `amount` field +
  factory `bidOnMercenary(card, amount)` + `getAmount()`. Bids are
  `GameAction`s so the existing action pipeline, legality gating, and smoke
  readouts reuse the one mechanism.
- `model/GameState.java`: mercenary offer list (`MERCENARY` offers),
  per-player cumulative bid maps with rollback so a legal result never
  leaves partial state, per-turn controller resolution, and
  `resolveMercenaries()` that crowns the strict-highest cumulative bidder
  (ties crown nobody - D12 discipline) and clears per-turn bid state at
  `startRound` (offers persist across rounds, per B5-0386 pool semantics).
- `model/GameSlot.java` / `model/GamePhase.java`: `MERCENARY` phase value
  wired between ACTION and CONFLICT_RESOLUTION in the phase order so the
  controller loop visits it in the right position.

## Engine layer

- `engine/CardEffects.java`: `applyMercenaryAction` id-keyed dispatch
  (fixture `mer_metric_fixture` -> +1 influence applied to the controller;
  unknown/unregistered ids are a loud no-op that logs and refuses rather
  than silently passing, so a future real mercenary card added to the
  effects table is caught in play).
- `engine/RulesEngine.java`: `canBidOnMercenary` (legality: action-turn
  gate, offer present, cumulative bid afford-now against the applied pool,
  Rating untouched - bids spend the per-turn pool, never the Rating) and
  `executeBidOnMercenary` (pool spend via `applyInfluence`, cumulative bid
  record, guard against overspend).
- `engine/GameController.java`: `runMercenaryPhase()` wired between
  `runActionPhase()` and `runDrawPhase()` (clean no-op when no offers -
  regular games have an empty offer list), plus the `BID_ON_MERCENARY`
  `processAction` branch routed to `executeBidOnMercenary` with the
  `applyInfluence` pool spend path. Mercenary resolution runs at the
  resolved controller with the strict-highest rule; ties crown nobody.

## Conformance

`HeadlessConformanceTest.java` MER section (new, harness-append-only): 8
MER checks added, suite grew 342 -> 350/350 PASS:

- offer hydration: no card is a mercenary by default (schema default false);
- `MERCENARY` phase ordering (ACTION < MERCENARY < CONFLICT_RESOLUTION);
- `BID_ON_MERCENARY` factory + amount getter round-trip;
- canBidOnMercenary rejection: not action turn, not offered, insufficient
  pool, and the afford-now gate (Rating untouched by bids);
- cumulative bidding: second bidstack reads pool-diminished state;
- resolution: strict-highest crowns the target; tie crowns nobody; winner
  effect applies; losers keep their (spent) pool contributions with no
  refund - per B5-0386 pool semantics bids come out of the applied pool
  and are not returned;
- startRound clears cumulative per-turn bids while keeping the offer list;
- `runMercenaryPhase` no-op with empty offers (regular-game path unaffected).

## Verification

- `compile.bat` green: JDK 1.8.0_292, `-source 6 -target 6`, 0 errors
  (expected 1 bootstrap warning). Re-verified at close-out after the
  concurrent ui/ edit landed - the earlier transient `MainWindow.java`
  duplicate-`revealContingencyButton` / missing-method state was another
  agent's in-flight B5-0381-adjacent contingency-UI edit (ui/, out of my
  claimed scope, never touched by me); it compiled clean in the final gate
  run.
- Conformance: 350/350 PASS (MER 8 checks green; suite grew from 342).
- Smoke: PASS (446 cards, 28 AI actions, 41 UI callbacks, 4/4 legal).
- Java 6 gate: grep for `->` / `::` / `.stream(` / `computeIfAbsent` /
  `new \w+<>()` over the touched model/+engine/ files - clean (Java 6
  constructs banned under -source 6).

## Files changed (model/ + engine/ only)

- `b5ccg/src/b5ccg/model/Card.java`
- `b5ccg/src/b5ccg/model/GameAction.java`
- `b5ccg/src/b5ccg/model/GameState.java`
- `b5ccg/src/b5ccg/model/GameSlot.java` (phase enum)
- `b5ccg/src/b5ccg/model/GamePhase.java`
- `b5ccg/src/b5ccg/engine/CardEffects.java`
- `b5ccg/src/b5ccg/engine/RulesEngine.java`
- `b5ccg/src/b5ccg/engine/GameController.java`
- `b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java` (MER section added)

## Out of scope (recorded, NOT done here)

- Actual mercenary card `resources/` JSON data (B5-0386 verdict: no card in
  Premiere/Deluxe carries a mercenary flag today; pool stays empty by data).
- UI/ai consumers (ai bidding + ui readout are separate slices; AI stays out
  of this engine+model slice).
- `ui/` MainWindow contingency-reveal controls (another agent's claim;
  left untouched; the transient compile break there was theirs, not mine).

## Close-out

- TASK_LEDGER row B5-0395 rebuilt DONE (pipes preserved, single
  occurrence, verified).
- DECISIONS.md entry appended at EOF.
- Report: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0395.md`
  (this file).
- Claim released at close-out; gate green re-verified.
