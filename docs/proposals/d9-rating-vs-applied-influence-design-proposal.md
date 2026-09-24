---
document:
  title: "Design proposal — D9 rating-vs-applied: split Influence Rating from the per-turn applied pool"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  last_modified_date: "2026-09-23"
---

# D9 design proposal — Influence Rating vs the per-turn applied pool

B5-0342, proposal-only. No `b5ccg/src/` or `b5ccg/resources/` file is touched
by this task. Source findings: B5-0203 audit deviation **D9**; rulebook
§Influence; cross-reference: B5-0341 (D6) makes this split a **hard
ordering dependency**.

## 1. Problem statement

`Player.influence` is a single integer serving two rulebook-distinct roles:

1. **Influence Rating** — permanent strength of the faction: starts at 4,
   altered only by permanent gains/losses, and the Power total counted for
   victory ("Each point of Influence Rating also counts equally towards the
   Power total needed to win").
2. **Applied (unapplied) influence** — the spendable portion during a turn:
   "players may apply influence (up to the total of their Influence Ratings)
   as a means to further their aims. Influence applied during a player's turn
   is restored at the beginning of the following turn."

Because the model has only the one integer, every rulebook "apply" spend
permanently reduces the Rating in the current code. Today's spend sites
already do this silently: recruit and promote (`spendInfluence`) and Build
Influence (`loseInfluence(3)` then `gainInfluence(1)` — the engine comment
even reasons in applied-pool terms, "net pool change = -2", while the code
permanently destroys 2 Rating per build). The audit's D9 record predates the
B5-0323/B5-0321 spend wiring, so the conflation is now live behavior, not a
latent gap: **each sponsor/promote permanently weakens the faction's Power
total**, and Build Influence — an action whose whole point is to raise the
Rating — permanently shrinks it by 2 per use while raising it by 1.

Call-site census (grep, model/ + engine/ + ai/): 68 references to
getInfluence/spendInfluence/gainInfluence/loseInfluence. The semantic split
must therefore be an additive layer with a mapping table (§3.3), not a
signature scramble.

## 2. Rulebook specification (§Influence — normative)

> "Each player has an Influence Rating which represents the raw strength of
> his faction. It starts at 4. During each turn, players may apply influence
> (up to the total of their Influence Ratings) as a means to further their
> aims. Each point of Influence Rating also counts equally towards the Power
> total needed to win the game. Influence applied during a player's turn is
> restored at the beginning of the following turn. Influence may be
> permanently gained or lost through game play, altering a player's Influence
> Rating…"

Derived rules:

* **D9.1** Pool capacity per turn = current Rating. Applying never changes
  the Rating.
* **D9.2** The pool restores at the start of the following turn (the
  engine's round boundary — `startRound` — is the turn boundary in this
  implementation).
* **D9.3** Permanent gains/losses (conflict rewards, aftermath effects,
  agenda effects, location income — anything phrased as gaining/losing
  influence) alter the Rating, which changes both Power and next turn's
  pool capacity.
* **D9.4** Victory reads the **Rating** (Power), never the remaining pool.
* **D9.5** Build Influence: apply 3 from the pool, Rating +1 permanently
  (rulebook §Influence/§Actions; the current engine comment already
  documents this intent).
* **D9.6** Rating < 3 cannot afford Build Influence's apply-3 even when
  Rating ≥ 1; Rating > 9 blocks the action (already enforced by
  `canBuildInfluence`).

## 3. Design

### 3.1 Model changes (Player)

* `influence` keeps its name and meaning: **the Rating**. All existing
  `getInfluence()` readers (victory, AI scoring, UI, agenda conditions)
  remain correct with zero changes.
* New field `appliedPool` with:
  * `getAppliedPool()` — spendable this turn;
  * `applyInfluence(int n)` — pool spend, returns false when
    `n > appliedPool` (state unchanged);
  * `restoreAppliedPool()` — `appliedPool = influence` (D9.1+D9.2), called
    from `RulesEngine.startRound` at the turn boundary;
  * permanent `gainInfluence`/`loseInfluence` ALSO adjust `appliedPool` in
    the same direction (a permanent gain is immediately spendable; a loss
    clamps the pool) so a mid-turn Rating change behaves per D9.3.
* Assistant sponsor discount (B5-0339) interacts at the apply site: the
  discounted amount is simply applied (§3.3).

### 3.2 Engine changes

* `RulesEngine.startRound` calls `p.restoreAppliedPool()` per player
  (D9.2) — one line next to `collectLocationIncome()`.
* Affordability gates switch from `getInfluence() >= cost` to
  `getAppliedPool() >= cost`:
  * `canRecruit` / `canPromote` / `canBuildInfluence` (D9.6: check the
    pool for the apply-3, the Rating for the ≤9 rule);
  * AIPlayer affordability offers (same substitution).
* `executeBuildInfluence`: `loseInfluence(3)` → `applyInfluence(3)`; the
  `gainInfluence(1)` stays (permanent Rating +1, D9.5). Net effect flips
  from the current *permanent −2* to *pool −3 this turn, Rating +1 forever*.
* `executePromote` / the recruit site in GameController:
  `spendInfluence(cost)` → `applyInfluence(cost)`; the controller consumes
  the assistant discount exactly as today (B5-0339), just against the pool.

### 3.3 Caller mapping table (the 68-site triage)

| Call shape | Sites | D9 disposition |
|---|---|---|
| `gainInfluence` — conflict rewards, aftermath/agenda/event effects (CardEffects, resolveConflict, aftermath play) | engine/model effects | unchanged = permanent Rating (D9.3) |
| `gainInfluence` — location income, enhancement income (startRound) | engine | unchanged = permanent (recorded interpretation) |
| `spendInfluence` — recruit, promote | GameController, RulesEngine | → `applyInfluence` (pool) |
| `loseInfluence(3)` — Build Influence | RulesEngine | → `applyInfluence(3)` |
| `getInfluence` — checkVictory/standardVictory | RulesEngine | unchanged — reads Rating (D9.4) |
| `getInfluence` — AI scoring, affordability offers | AIPlayer | affordability reads pool; scoring stays Rating-based (unchanged) |
| `getInfluence` — UI readouts, agenda conditions, tests | ui/, model/AgendaCard, suite | unchanged (Rating); UI gains a pool readout as follow-up (B5-0348-adjacent) |
| suite helpers `loseInfluence(getInfluence()-5)` etc. | HeadlessConformanceTest | unchanged — they shape the Rating |

### 3.4 Interaction with D6 (ordering)

With the pool live, the Action Round can adopt unlimited cycles
(B5-0341 §3.4): every apply-spend draws a finite per-turn pool, so the
termination argument holds without relying on the one-action cap.
**Recommended implementation order: this proposal's slice first, then the
D6 loop change**, or one combined task — but never D6 alone.

## 4. What changes observably

* Build Influence stops shrinking Power: a faction at Rating 9 that builds
  to 10 keeps Power 10 (currently it would sit at effective 7–8 after the
  spends). Standard-victory pacing accelerates slightly and becomes
  rulebook-correct.
* Sponsoring/promoting no longer permanently weakens Power; wealthy factions
  can sponsor multiple cards per round once D6 lands (bounded by pool +
  ready cards).
* The smoke test's influence-adjacent numbers may shift (income vs spends
  across the fixed round); conformance sections assert rules, not absolute
  influence paths — the CST/PRM/AST checks that DO assert spend amounts must
  switch to pool-based assertions in the same change.
* The UI influence readout shows the Rating only; an applied-pool readout is
  a small follow-up in the B5-0348 hand/board work.

## 5. Acceptance criteria (for the future implementation task)

1. Pool restore: a player who spends the whole pool acts at Rating-level
   buying power again next round (`restoreAppliedPool` in startRound,
   suite-asserted).
2. Apply ≠ Rating: sponsoring N influence leaves `getInfluence()` unchanged
   and `getAppliedPool()` reduced by N.
3. Build Influence: pool −3, Rating +1, Power +1 (the current −2-permanent
   regression is gone).
4. Victory: a 20-Rating player with an empty pool still standard-wins;
   a 19-Rating player with a full pool does not.
5. Permanent-loss clamp: a Rating loss larger than the current pool clamps
   the pool at 0, never negative.
6. Assistant discount consumes from the pool (B5-0339 semantics preserved).
7. RUN_TESTS=1 green after the suite's spend assertions migrate; Java 6
   grep clean.

## 6. Risks

* **R1 — Hidden Rating-dependence:** any effect silently assuming
  "influence = spendable" (e.g. an AI offer gate) would over-offer under the
  split; the §3.3 census is the checklist.
* **R2 — Suite churn:** spend-amount asserts in CST/PRM/AST must migrate to
  pool semantics in the same commit or the gate goes red.
* **R3 — Balance:** Build Influence becoming +1 Power per use strengthens
  the standard-victory path; B5-0349's seeded runner should measure
  time-to-victory before/after.
* **R4 — Save/state compatibility:** none (no persistence exists).

## 7. Explicitly out of scope

* The D6 loop change itself (B5-0341's slice).
* Applying influence for non-spend purposes (conflict support totals,
  tension tracks) — no model representation exists.
* Mercenary bids (no model), §Influence of non-player forces (Shadows,
  Vorlons, Babylon 5 station — the station's Rating is B5-0340's model).

## 8. Recommendation

Accept §3 as the implementation shape: `appliedPool` additive to an
unchanged Rating, restore at startRound, the §3.3 mapping table as the
migration checklist, and the §5 suite assertions added alongside the switch.
Sequence before (or combined with) the D6 implementation, per B5-0341.
