---
document:
  title: "Design proposal — B5-0422: breaking the EASY pass-bias cascade"
  status: "Proposal"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  last_modified_date: "2026-09-25"
---

# B5-0422 — Pass-bias cascade design proposal

B5-0422, proposal-only. No `b5ccg/src/` or `b5ccg/resources/` file is touched
by this task (no compile gate). Shaping inputs: the B5-0409 verdict
(seed note, TASK_LEDGER line 303), the B5-0351 AI difficulty contract, the
B5-0406 contract re-verification, the D9/D6 lineage (B5-0372), and the
B5-0202c Finding 3 "EASY 30% pass bias" design decision.

## 1. Problem statement — what the cascade is and is not

The B5-0409 verification (both sessions) classifies the multi-round harness
"stall" as follows:

* **It is an AI pass-bias cascade, not a per-player action cap.** B5-0372
  (D6) is live: `GameController.runActionPhase` runs initiative cycles until
  players pass consecutively, un-passes on any non-pass, and carries a
  non-rulebook liveness cap of `8 * playerCount` actions per round.
* **It is not a B5-0321 seat gap.** `canPromote`/`executePromote`
  (RulesEngine.java:118-162) exist; zero promotions in harness output was a
  B5-0349 parser artifact, now fixed by B5-0413 (token `" promotes "`).
* **The EASY pass bias is a designed constant.** `AIPlayer.easyChoose`
  (AIPlayer.java:446-449) returns `GameAction.pass()` on a 30% blind roll
  whenever the legal set has more than one option, then picks uniformly over
  the legal set. Observed pass rates: 0.523 (B5-0406), 0.53-0.567 (B5-0409) —
  all inside the B5-0351 contract band **0.35-0.70** (expected 0.53 = 0.30 +
  0.70 × 1/3 where the legal set contributes one pass slot).
* **The result is a wall-clock artifact.** Games terminate naturally
  (no-timeout probe: standard victory at round 12, ~118 s, B5-0409), but the
  B5-0349 runner closes each game at 60 s, so slow, pass-heavy games read as
  "stalled" and knock-on counts (promotes, agendas at the window) look zero.

Mechanism chain for the playtest feel (B5-0409, playtest-guide honesty note):
EASY seats blind-pass ~30% per eligibility visit → sparse round actions →
the economy (builds/plays/conflicts) grows slowly → standard victory lands
late (~round 12) → dense 60 s windows close mid-game. MEDIUM/HARD are not
cascade drivers: their scoring floors (`PASS` at 0 MEDIUM, -0.5 HARD; any
≥0 action strictly beats MEDIUM pass) make them pass only when resources are
exhausted — the designed self-termination of d6 §3.3.

The B5-0351 contract is the pin on any change: **no retune may silently alter
the four statistical EASY checks** (legality, non-determinism, pass-bias band
0.35-0.70, uniform tail ≥ 15%) **or the six MEDIUM/HARD deterministic checks**
(zero-cost invariance, determinism, cost-awareness).

## 2. Current contract recap (B5-0351 / HeadlessAIDifficultyContractTest)

* EASY: 300 samples; 0 illegal actions; non-deterministic; pass rate in
  [0.35, 0.70]; each equal-value play ≥ 15% of runs.
* MEDIUM/HARD: first-listed of two identical cost-0 plays wins (zero-cost
  invariance, B5-0324 pin); deterministic over 50 identical states; cheaper of
  two otherwise-identical plays wins.

## 3. Option-by-option analysis

### Option A — EASY retune bands (lower the blind direct-pass)

Change `easyChoose`'s 30% blind-pass roll to a lower `X`. Contract-band math
on the exact B5-0351 fixture (legal set = {pass, ev0, ev1}, tail uniform 1/3):
expected pass rate = `X + (1-X)/3`.

| Blind pass X | Fixture pass rate | vs band floor 0.35 | note |
|---|---|---|---|
| 30% (status quo) | 0.533 | ≥ 5σ | observed 0.523-0.567 |
| 15% | 0.433 | ~2.9σ | comfortable margin |
| 10% | 0.400 | ~1.7σ | acceptable |
| 6% | 0.373 | ~0.8σ | marginal under flakiness |
| 0% | 0.333 | **below floor** | would FAIL the contract |

(SD ≈ 0.028-0.029 for n=300 binomial.) The band therefore *permits* a retune
to ~10-15% without any contract re-pin; a 0% blind pass would break it.

Cascade effect: halves the direct-pass volume → rounds denser → economy grows
faster → earlier terminations. **Honest caveat:** the "stall" metric is
wall-clock (60 s window) and every AI action sleeps 600 ms, so more actions
per round can actually *lengthen* round wall-clock even as rounds-to-victory
falls. Option A alone is not a guaranteed fix for the harness label.

Design tension: B5-0202c Finding 3 and d6 §3.3 ("keep the existing pass bias
so easy games stay short") make EASY's bias a deliberate *game-length* knob
for the gentle-opponent seat. A retune shortens rounds-to-victory but changes
EASY's play feel; it stays inside the contract band, so it is a *within-design*
tuning, not a contract change.

Scope: `ai/` only. Gate: compile + RUN_TESTS=1 + HeadlessAIDifficultyContractTest
10/10 + band re-measurement recorded (B5-0406 certify pattern).

### Option B — harness and Main default seat-mix change

Today the EASY seat is default in four loops: `HeadlessMultiRoundTest`,
`HeadlessSmokeTest`, `HeadlessReportingTiebreakTest` (`{EASY, MEDIUM, HARD,
MEDIUM}`) and `Main.java` (Delenn MEDIUM / G'Kar HARD / Londo EASY).

* **B1 (recommended subset): swap EASY → MEDIUM in `HeadlessMultiRoundTest`
  and `HeadlessSmokeTest` only.** `HeadlessReportingTiebreakTest` MUST be left
  untouched: its EASY slot is a reporting-position fixture for the B5-0350
  tiebreak contract, not a game loop.
* **B2: `Main.java` Londo EASY → MEDIUM.** Removes the gently-random opponent
  from the default human game; changes onboarding difficulty and contradicts
  the documented playtest intent unless the docs refresh with it (B5-0426
  gate is already 0414 + 0422 DONE).

Contract effects: **none on the B5-0351 bands** — the contract measures EASY
in isolation via its own fixture; seat mix is irrelevant to it. Effects on
*metrics*: the harness stops sampling EASY, so every downstream aggregate
(B5-0409 reproduction, stall rates, winner distributions) changes meaning.
`HeadlessMultiRoundTest`'s "stall" label would mostly disappear (2/5 games
already won with the EASY seat present). This is the only option that directly
fixes the *labeled* metric, at the cost of losing EASY coverage from balance
reporting.

Scope: `b5ccg/src/b5ccg/Main.java` + two engine harness files + docs refresh.

### Option C — un-pass incentives (kills the cascade's back-to-back driver)

The 30% blind pass is re-rolled on **every** eligibility visit, so an EASY
seat can blindly pass on adjacent cycles (~0.3² ≈ 9% of adjacent visits).
Within the D6 loop a passer is *invited back* (un-pass on the next non-pass;
a passer may act again later — rulebook §III), yet EASY still blind-passes
again. Options:

* **C1 (recommended): round-scoped repeat-pass suppression.** When a seat has
  already passed once this round (`p.isPassed()`), the blind 30% roll is
  skipped on later visits; only the uniform tail may pass. No other EASY
  behavior changes.
* C2: same scope, but force an act on repeat visits (tail pass also removed) —
  stronger, risks an exhausted seat acting with nothing useful.
* C3: MEDIUM/HARD "acted after passing" micro-bonus — a soft nudge rewarding
  returning to the cycle. Lowest cascade impact (MEDIUM/HARD already act while
  any ≥0 action exists) and touches deterministic scoring; least preferred
  because it churns cost-aware scoring tables for little gain.

Contract effects: the B5-0351 fixture never calls `setPassed` (the 300-sample
loop reuses one Player whose passed flag stays false), so **the EASY band stats
and the MEDIUM/HARD deterministic checks are bit-for-bit unchanged.** The
uniform-tail ≥15% check is untouched under C1. Implementation must store the
round-scoped flag on the Player and clear it each round (B5-0372 `advanceRound`
clears conflicts initiations — same clearing site), never on the AI singleton.

This is the smallest-diff, principle-preserving cascade breaker: it keeps the
*designed* 30% threshold on a seat's first visit while making the D6
"come back after passing" contract meaningful for EASY.

Scope: `ai/` (+ a one-line round-clear in `GameController.advanceRound` or a
Player field, both engine-adjacent — a claimed task must hold the scope).

### Option D — MEDIUM/HARD termination safety (defense in depth)

Status quo is already safe: the `8 * playerCount` cap breaks the loop and
falls through to `rules.finishActionRound` (draw phase), so a round always
ends; MEDIUM/HARD self-terminate at resource exhaustion (d6 §3.3); d6 §3.4
already argues bounded rounds. This option formalizes it:

* **D1: game-level liveness governor.** Track consecutive safety-cap-ends
  (or a max-round bound, e.g. 30 rounds); on repeated cap-ends force
  termination via the existing victory checks (leader-plays-tiebreak path).
  Pure safety net; fires nowhere in current practice.
* **D2: termination-proof harness case (NEW file, executable).** Assert that
  an all-pass world ends its round within the promised bound and that a
  cap-hitting world terminates. Mirrors the B5-0351 standalone-CLI precedent.
* D3: lower the cap multiplier 8 → 4. Rejected: dense late-rounds legitimately
  exceed 4 actions per player; premature round-end would distort play.

Contract effects: none on the EASY bands (D is MEDIUM/HARD/loop-centric) and
none on MEDIUM/HARD determinism/cost-awareness (those checks do not measure
round termination). D1/D2 are verification/insurance, not behavior changes in
reachable play.

## 4. Recommended combination

1. **C1** (repeat-pass suppression) + **D1** (liveness governor) in one engine/
   ai task — the cascade's back-to-back driver dies, termination gets an
   explicit bound, and nothing in the B5-0351 contract moves.
2. **A-lite** (blind pass 30% → 15%) as its own `ai/` task, landed the
   B5-0406 way: claim → retune → HeadlessAIDifficultyContractTest 10/10 →
   band re-measurement recorded in the report → DECISIONS. Explicitly **not** a
   stealth retune; 15% keeps the fixture rate ~0.43, ≥2σ above the 0.35 floor.
3. **B1** (harness seat swap in MultiRound + Smoke only, Tiebreak untouched)
   as a harness/docs task, paired with the B5-0426 playtest-guide refresh;
   B2 deferred until product intent is recorded in a decision (Londo EASY is a
   documented gentle-opponent UX).
4. **D2** proof harness only if the liveness governor (D1) changes production
   code — it proves the promise; otherwise it is optional wrapping.

Explicitly off the table: raising the EASY pass bias (would push games longer
and toward the 0.70 ceiling), and any change landed without its own
OPEN → claim → DONE cycle with the standing gates (compile green, RUN_TESTS=1
green, Java 6 grep clean, difficulty contract 10/10).

## 5. What this proposal does not do

* Touches no `b5ccg/src/` or `b5ccg/resources/` file.
* Reverses B5-0202c Finding 3 (EASY keeps a designed pass bias; C1/A-lite only
  shape *when* it fires).
* Alters rulebook semantics: D6 un-passing, consecutive-pass round end, and
  the "pass when you choose" principle all stand; C1 merely stops a blind
  re-roll after the seat has already exercised that choice this round.