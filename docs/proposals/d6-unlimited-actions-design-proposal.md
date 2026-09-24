---
document:
  title: "Design proposal — D6 unlimited actions: initiative cycles until all pass consecutively"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  last_modified_date: "2026-09-23"
---

# D6 design proposal — unlimited actions in the Action Round

B5-0341, proposal-only. No `b5ccg/src/` or `b5ccg/resources/` file is touched
by this task. Source findings: B5-0203 audit deviation **D6**; rulebook
"Playing Fast and Loose" (§II) and "The Action Round" (§III).

## 1. Problem statement

`Player.resetActions()` grants **1 action per action round**
(`actionsLeft = 1`), and the B5-0202c legality guard makes every AI pass once
it is spent. The rulebook instead defines the Action Round as **initiative
cycles**: each player acts (or passes) in initiative order, the cycle repeats
"as many times as necessary," and the round ends only when **all players have
passed consecutively**. The current one-action structure is a
deliberate-looking simplification (audit D6): it caps Build Influence,
sponsoring, and promotion per round at one each, and makes "consecutive
passes" vacuous (everyone passes exactly once, in order).

Not everything is missing. `GameController.runActionPhase()` already
implements the *skeleton* of the rulebook loop — a `passCount` that resets on
any non-pass action and ends the round at `passCount == playerCount` is
exactly the consecutive-pass rule. What is wrong is what feeds it: the
per-player action cap and sticky passed-flags.

Current observed shape (B5-0312 playtests): all-AI rounds are deterministic
at 8 actions = 4 real actions + 4 passes, because every player acts once and
then is forced to pass.

## 2. Rulebook specification (§III, "The Action Round" — normative)

> "A player may only perform one action at a time. The player with the lowest
> initiative acts first. Then the player with the next lowest initiative
> becomes eligible to perform one action, and so on, until all players have
> acted once. The player with the lowest initiative is then eligible to take a
> second action and the cycle repeats as many times as necessary. A player may
> pass as his action at any time. Play continues in this fashion until all
> players have passed consecutively. A player who passes may act later in the
> action round, unless the round has ended. Once every player has passed in a
> row, the action round ends immediately."

Supporting rules elsewhere:

* §II (timing disputes): eligibility cycles in initiative order, each player
  acts or passes, "until all players pass in a row."
* §V (Pass): "A player may pass as his action at any time. He may take
  another action later in the round unless all players pass consecutively."
* §V reminder: "A rotated character may not rotate to take another action" —
  rotation economy, not an action-count, is the real per-character limiter.

## 3. Design

### 3.1 Model changes (Player)

* **Redefine the action grant.** `resetActions()` stops meaning "1 action."
  Eligibility to act during the ACTION round is not a countable resource: a
  player is eligible whenever the cycle reaches him and he has not passed
  *this cycle*. Proposed concrete shape:
  * `actionsLeft` is repurposed or retired from the ACTION round entirely;
    the ACTION-round legality checks (`canInitiateConflict`, B5-0202c's
    `actionsLeft <= 0` early-PASS) switch to a round-scoped
    `hasActedThisCycle`/`passed` pair owned by the controller.
  * Other rounds that use actions (aftermath step, mercenary bids) are
    explicitly out of scope here (§7) — the aftermath step already runs its
    own consecutive-pass rulebook flow in `resolveCurrentConflict`.
* **Un-passing.** `setPassed(false)` is applied when a player takes a
  non-pass action (rulebook: a passer may act later). Today the flag is
  sticky within the round.
* `resetActions()` continues to clear `passed` at the round boundary
  (unchanged semantics, next round starts fresh).

### 3.2 Engine changes (GameController.runActionPhase)

The loop keeps its consecutive-pass end condition and gains:

1. **Cycle over players in list order** (the engine's current turn order is
   list order; rulebook says lowest-initiative-first — see §3.5).
2. **Eligibility check:** ask the player for an action whenever reached;
   `PASS` sets `passed = true`; any other action clears it
   (`current.setPassed(false)`) — this one line delivers un-passing.
3. **No per-round action cap:** remove the `actionsLeft` gate from the
   ACTION-round path. One action *at a time* (rulebook) is preserved by the
   loop structure itself: exactly one action per eligibility visit.
4. **Victory check after every action** (unchanged; with more actions per
   round this fires more often — see §6 risk R4).
5. **Safety cap (liveness, non-rulebook):** a hard bound
   `maxActionsPerRound = 8 × playerCount` (configurable constant), logged
   loudly if ever hit. This exists ONLY to guarantee the harness/UI cannot
   hang on a pathological AI; with the resource bounds in §3.4 it should
   never fire in a well-behaved game.

### 3.3 AI changes (AIPlayer) — required for termination

* Remove the B5-0202c early-PASS (`isPassed() || actionsLeft <= 0`): the AI
  must be able to act again in later cycles. The guard's *liveness intent*
  moves to the new design: PASS is offered always (unchanged) and the
  scoring floors keep the AI honest.
* **MEDIUM/HARD:** unchanged scoring — every positive-value action is taken
  until resources or ready cards run out, then scores floor at 0 and PASS
  wins. Because scores are cost-aware (B5-0324) and floored at 0, the AI
  self-terminates when its resources are exhausted. EASY: keep the existing
  pass bias so easy games stay short.
* **Determinism note:** zero-cost data (B5-0311 C1; B5-0335 backfilled real
  costs) keeps MEDIUM/HARD ordering stable; the AIS suite's zero-cost
  invariance checks stay valid.

### 3.4 Termination proof sketch (why the game still ends)

Every non-pass ACTION consumes a renewable-only-at-round-start resource:

* Sponsor/Promote spend influence (finite pool) and a ready unrotated card;
* Build Influence needs a ready IC character and 3 applied influence, and
  the character stays rotated;
* Conflict initiation is once per faction per turn (B5-0302 marker);
* Join/support/oppose rotates the committed card.

With D9's applied-influence pool (below) the pool itself restores only at
turn start, so per-round action totals are bounded by
`influence + ready cards`. The §3.2 safety cap is belt-and-braces.

**Hard dependency — D9 (B5-0342) should land first or with D6.** With the
current semantics (influence = permanent rating, no applied pool), unlimited
actions let a wealthy faction convert its whole rating into sponsored cards
in one round, which is rulebook-legal *only* because the rulebook spends the
per-turn applied pool, not the rating. Implementing D6 alone would make
influence massively more powerful than intended. Ordering: **B5-0342 →
B5-0341-implementation** (or a single combined task).

### 3.5 Initiative order (recorded, not required)

Rulebook eligibility is lowest-initiative-first. The engine's player-list
order approximates it and B5-0327 (F8) already displays initiative. A
`Player.initiative` field with list sort in `runActionPhase` is a small
follow-up; it does not block D6 and is not part of this task's slice.

## 4. What changes observably

* Rounds contain a variable number of actions (no longer exactly
  playerCount × 1 + passes); the smoke test's "8 actions" profile changes —
  harness expectations belong to B5-0349's seeded runner, and the
  conformance suite's CPT/AIS/CST/PAR/FLR/AMT/AST sections are
  action-economy-independent (they test rules, not loop shape).
* AI players with resources will sponsor/promote/build multiple times per
  round — rounds take longer in wall-clock (600 ms pause per AI action; the
  pause is per action, not per player).
* Human play: the UI needs no structural change (actions are already
  free-form submissions); the B5-0327 phase gating and action-button
  enablement keep working because they key on turn/phase, not action count.

## 5. Acceptance criteria (for the future implementation task)

1. A constructed state where a player has 2+ affordable actions takes them
   all in one round (probe: sponsor, promote, build in a single ACTION
   round); a player with no resources passes immediately every cycle.
2. Un-passing: a player who passed takes an action again in a later cycle
   after the state changes (rulebook §V Pass).
3. The round ends iff all players pass consecutively; a single non-pass
   resets the consecutive counter (existing loop semantics, now
   assert-tested).
4. All-AI 8-round seeded runs (B5-0349 runner) terminate and show a higher
   per-round action/conflict/promotion rate than the one-action baseline
   (B5-0312: zero promotions; target: promotions and builds occur without
   hand-of-cards starvation).
5. The safety cap never fires in 100 seeded runs (log assertion).
6. RUN_TESTS=1 green; Java 6 grep clean.

## 6. Risks

* **R1 — Game length:** more actions per round lengthen games and UI pacing.
  Mitigation: EASY pass bias; UI pause is per action; B5-0349 runner
  quantifies before/after.
* **R2 — AI inflation:** without D9's pool, influence spends lose their
  turn-scope. Mitigation: hard ordering dependency on B5-0342 (§3.4).
* **R3 — Liveness:** a scoring bug could make an AI never pass. Mitigation:
  the §3.2 safety cap + the scoring floor (PASS wins ties at 0).
* **R4 — Victory timing:** `checkVictory` after every action can end a round
  mid-cycle (correct per the rulebook's general victory timing; noted so the
  UI's phase transitions are not assumed to be cycle-aligned).

## 7. Explicitly out of scope

* Aftermath-step timing (already implemented per rulebook in the conflict
  flow, B5-0309/B5-0338).
* Mercenary bids and the mercenary action window (no model yet).
* "Fast and loose" simultaneous play (§II variant; not a target of the
  digital implementation).
* The D9 influence split itself (B5-0342's proposal).

## 8. Recommendation

Accept the §3 design as the implementation shape; sequence the
implementation as **B5-0342 first, then D6**, landing them as one combined
engine change (model `Player` eligibility + `GameController` loop + AIPlayer
guard removal) with the §5 acceptance suite. The combined change touches
`Player.java`, `GameController.java`, `AIPlayer.java` (three scopes —
serialize via one claim) and updates the smoke/conformance harnesses' action
count expectations via B5-0349's seeded runner.
