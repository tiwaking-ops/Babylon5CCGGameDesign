---
document:
  title: "Human playtest guide — running the B5 CCG prototype"
  status: "Guide (describes the working tree as of 2026-09-26)"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  assessor_llm:
    - {name: "GPT-6 Codex", version: "GPT-6"}
    - {name: "Solar Pro4", version: "solar-pro4:free"}
    - {name: "Qwen (qwen-2.5-coder-32b-instruct)", version: "qwen-2.5-coder-32b-instruct"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (me-so-poor)", version: "big-pickle"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (me-so-poor)", version: "big-pickle"}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
    - {name: "Buffy (unknown)", version: "unknown"}
    - {name: "Buffy (unknown)", version: "unknown" — B5-0484 part 11 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" — B5-0498 part 13 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" — B5-0512 part 14 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" — B5-0517 part 15 refresh}
    - {name: "Buffy", version: "unknown" — B5-0531 part 17 refresh}
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  last_modified_date: "2026-09-26"
---

# Human playtest guide — B5 CCG prototype

This guide tells a human how to run the current build, what every control
does, and where the honest gaps are. It describes the working tree as of
2026-09-26; features landed after that date may not be covered. The
canonical rules reference is `BABYLON5_CCG_RULEBOOK.md` (do not edit);
known deviations are recorded in `docs/DECISIONS.md`.

## 1. Requirements and build

* **JDK 8** (e.g. `1.8.0_292`) — the only toolchain that still accepts the
  project's `-source 6 -target 6` flags. Check with `javac -version`.
* No external libraries; everything is stdlib + Swing.

Build and run:

* **Windows (cmd):** `cd b5ccg` then `compile.bat`, then `run.bat`.
* **Git Bash / Linux / macOS:** `cd b5ccg` then `sh compile.sh`, then
  `sh run.sh`. `run.sh` auto-runs the compile first if `out/` is missing.

Two benign build notes: you may see one bootstrap `-source 6` warning
(expected), and on first launch the console prints a card-load line —
"Loaded 829 cards." is the healthy path (446 premiere + 383 deluxe); if
loading fails the game falls back to a minimal test set and says so on
stderr, in which case stop and report it.

## 2. Starting a game

1. A dialog asks you to **choose a faction** (Human, Minbari, Centauri,
   Narn). Your choice is fixed for the session; the three other races are
   the AI seats.
2. You play against three AI opponents with fixed personalities and
   difficulties: **Delenn (MEDIUM)**, **G'Kar (HARD)**, **Londo (EASY)** —
   see `b5ccg/src/b5ccg/Main.java` if you want to know which seat is which.
3. Each seat gets a faction deck; your race ambassador is pinned to the top
   of your draw pile so it opens in your hand (B5-0319), and you start with
   a 4-card hand.
4. The main window opens and the game loop starts on a background thread;
   the log and board update as the AI seats act.

## 3. The round structure (as implemented)

Each round is: `startRound` upkeep (agenda income, location income,
assistant-bonus and sponsor-discount reset, bonus-layer expiry sweep,
leadership-rotation expiry), then
the **Action phase** — players act in initiative order (human clicks, AI
decides) one action at a time
until every player has passed consecutively. The implementation retains a
non-rulebook 8×playerCount safety backstop against infinite loops, but the
B5-0436 D6 fixture verifies that a normal action round reaches the
consecutive-pass exit without hitting that backstop. The draw phase follows,
then the next round. One simplification to know before you playtest: **a
declared conflict resolves immediately when initiated** (not in a separate
Resolution Round as the rulebook structures it). Joining now offers a real
decision window when you are eligible to participate:
AI seats choose first, then the controller pauses during conflict resolution
for your Support or Oppose choice. Your face-up ambassador, if present, is
committed to that side and play resumes. After mandatory participation, if
you are eligible and at least one legal opposing target exists, the controller
opens the optional human **Attack** window before resolution. Select a ready
attacker and an explicit target, or choose **Skip Attack**; the controller
revalidates the pair and then resumes resolution. Mandatory-participation
conflicts (B5-0336) still apply, including cases where the engine compels
your ambassador without a voluntary choice. The conflict still resolves as
part of the initiation action; there is no separate Resolution Round.

## 4. What you can do today — control reference

Main window buttons (enabled contextually; a disabled button is illegal for
you right now, not broken):

> **B5-0423 update (board selection landed):** the five selection-driven
> controls — **Lead Fleet, Use Rotate Effect, Attack, Heal, and Repair** —
> now select their targets on the board as well as in hand: board
> characters, fleets, groups and locations are clickable (a click chain
> whose hit-test geometry is copied from the paint code, so the clickable
> area cannot drift from the drawn area), and hand and board selections run
> through one shared handler (`applyCardSelection`), so target population,
> enablement and cost preview behave identically for both. Heal/Repair
> enablement uses the engine's own predicate rather than a hand-rolled copy,
> and Play Card requires the selection to actually be in your hand (a board
> card can never be played from hand — B5-0423 guard). Face-down cards stay
> unresolvable so their identity remains hidden (B5-0381).
>
> **B5-0432/B5-0440 update (human attack window live):** after mandatory
> participation, MainWindow observes the controller's optional attack wait.
> Select a ready board attacker, choose an explicit legal target from the
> target selector, and then activate Attack. The engine revalidates the pair;
> the prior first-valid-target fallback is gone. During this wait Pass is
> relabeled **Skip Attack**, which declines the optional attack and resumes
> resolution. Outside the live wait, Attack and its selector remain disabled.

| Control | What it does | Notes |
|---|---|---|
| **Pass Turn** | Pass your action | Consecutive passes by all players end the Action Round |
| **Play Card** | Play the selected hand card | Events route to the effect tables; agendas install as your agenda |
| **Initiate Conflict** | Initiate the selected conflict card | Some conflicts require a declared target (B5-0336: e.g. Border Raid); use the target selector when enabled. Since B5-0452 the selector no longer pre-selects during population — Initiate stays dark until you explicitly pick a target, and the stale fallback that auto-picked the highest-influence location is gone (B5-0451). Since B5-0458 initiation is an ACTION-phase act: the button is dark during CONFLICT_RESOLUTION, AFTERMATH and DRAW (B5-0451 F4). Play Card keeps its broader phase window by design. |
| **Support / Oppose** | Join the active conflict on that side | Enabled only when the engine is waiting for your decision and you are eligible to join; choosing a side commits your face-up ambassador, if present, and resumes resolution. Mandatory participation still applies (B5-0336). |
| **Sponsor** | Recruit a supporting card | Applies the card's influence cost (B5-0323) |
| **Promote** | Promote a supporting character to your Inner Circle | Costs per `canPromote`/`promotionCost` (B5-0321/0323) |
| **Build Influence** | Rotate an Inner Circle member, apply 3 influence to your per-turn pool, rating +1 | Cost is drawn from the per-turn applied pool (B5-0369); the old defect where influence was a single permanent number is resolved. Only offered at rating ≤ 9. |
| **Lead Fleet** | Rotate a ready character with Leadership into the leader slot of one of your own unrotated fleets; that fleet's Military total gains the leader's Leadership for the round | One leader per fleet; the rotation consumes your action; the leader must be ready, in your Inner Circle or a supporting role, and the fleet must be unrotated and yours (B5-0362). |
| **Discard Agenda** | Remove your installed agenda from the game entirely (not the discard pile) | A Major agenda cannot be discarded; the button is disabled with that reason when a Major is installed (B5-0364). |
| **Replace Agenda** | Rotate a ready Inner Circle member to leader, remove your current agenda from the game, and install a new agenda from your hand | Replacement must be Major-for-Major when your current agenda is Major; the new agenda must be in your hand and sponsorable; pulling the leader from the Inner Circle rotation is part of the cost (B5-0364). |
| **Reveal Agenda** | Expose a face-down agenda: apply its on-play effect immediately if it is sponsorable, otherwise discard it | Face-down agendas render without their title or win-condition status until revealed (B5-0364). |
| **Play Card** (agenda guard) | Play the selected hand card; events route to the effect tables | If you already have an agenda installed, the Play Card button will not play another agenda from your hand — the one-major rule is enforced engine-side before the card leaves your hand (B5-0364). Use Discard/Replace/Reveal for agenda lifecycle instead. |
| **Place Contingency** | Play a ContingencyCard from your hand face-down under a host card in one of your in-play zones (Inner Circle, fleet, group, or location); the host's contingency count increments and the card's identity stays hidden | Requires a valid target and a host with an available slot; race matching reads the host card's subtype (B5-0365). No contingency-typed cards exist in the current pool, so this control is engine-ready but has no in-pool cards to play. |
| **Reveal Contingency** | Choose a face-down contingency from the dropdown selector and flip it, triggering its effect (routed through the event dispatcher) and then discarding it | Owner-gated; the selector lists each placed contingency as "<title> (under <host>)" (B5-0381); the reveal button is enabled only when it is your action turn and at least one placed contingency passes the reveal gate; no contingency cards in the current pool — engine-ready, data-pending. |
| **Use Rotate Effect** | Rotate a character to apply a selected effect: assistant bonus (+1 Diplomacy/Intrigue/Leadership for the turn) or sponsor discount (−1 influence on a later recruit this turn) | Gated by the B5-0339 assistant mechanic (your own ready, unneutralized supporting-character assistant plus your ambassador); consumes your action (B5-0366). |
| **Attack** | Attack a participant in the active conflict with the selected ready card; damage is mutual — each side deals its current conflict ability + 2 per its own Strife mark | Enabled only during the live human attack wait with a ready controlled attacker and an explicit target selected from the engine-approved opposing committed cards (B5-0432/B5-0440). The engine revalidates the pair and reuses B5-0370 damage, faction, participation, and overflow rules (damage model B5-0368). Neutralization can trigger on either side from the overflow. Pass becomes **Skip Attack** only during this wait. |
| **Heal** | Rotate to heal the selected damaged Inner Circle or supporting character | Enabled on your Action turn for a selected damaged character in those roles (B5-0402, engine B5-0371). If every IC member performed a heal action this round, your ambassador is fully healed at the round's end. |
| **Repair** | Repair the selected damaged fleet or location: removes normal damage at 1 influence per token, paid from your per-turn applied pool (Rating untouched) | Enabled on your Action turn for a selected damaged, ready fleet/location with affordable pool cost (B5-0402, engine B5-0371). |
| **Bid** (mercenary) | Bid the selected amount of applied-pool influence to control an offered mercenary for the turn; bids are cumulative per player per turn | Amount selector offers 1/2/3/5/10; the label above shows the offered mercenary, the controller after resolution. Enabled on your Action turn when the selected amount is affordable now (B5-0404, engine B5-0395). Highest cumulative total controls at the MERCENARY phase; a tie crowns nobody (D12 discipline). No mercenary-typed cards exist in the current pool (B5-0386), so in a normal game this control shows "(no mercenary offers)". |
| **Declare War** | Declare a war conflict against a selected race or location target while you are at war with that faction | The target selector lists races at war with you, then their locations (marked "[loc]"); a status label explains the current war state (B5-0407, engine B5-0376). Note: no card or effect in the current pool moves tension, so no faction is ever at war in a normal game — the control shows "Not at war" unless war state is set by a future data/effect task (B5-0358 design). |

Hand panel (B5-0348): filter checkboxes by card **type** and **faction**,
sort radio buttons (**unsorted / cost ↑ / cost ↓**), and a show-dimmed
toggle. Cards you cannot currently afford or legally play are **dimmed**
(greyed with a black overlay); the affordability dot is hidden on dimmed
cards. Aftermath cards show a green **ELIGIBLE** tag when they can legally
attach to the last resolved conflict (B5-0361) — this is an eligibility
*readout*, not a play button (aftermaths are played by the engine at
resolution time, auto-played for AI seats only).

Board readouts (B5-0316/0346/0347): each player band shows committed
conflict sides and per-participant card lists with totals (supporters
green, opposers orange); a green **WON BY** banner appears after a conflict
> resolves (B5-0347). **B5-0470 update:** the outcome banner now renders for
> every resolved conflict — normal and war alike — as
> "WON BY <winner> — LOST BY <loser>", where the loser is derived from the
> sides (initiator lost, the conflict target, or the captured location's
> owner), with a "WAR: …" title prefix for war conflicts (B5-0376). The
> detail line beneath shows the support/opposition totals plus the winner's
> influence reward.
resolves; agenda slots show MAJOR/minor and a `[WIN]` marker when the
agenda's condition is currently met; `A+`/`A$` mark the assistant
assist-bonus and sponsor-discount states (B5-0339). Captured/suppressed
locations show red `CAP:<player>`/`SUP` markers on their mini-cards, and
damaged or neutralized cards of any type show a red damage-state marker
on every board mini-card — `DMG:<n>` (severe appended as `+<s>`), `NEUT`
when neutralized, or `NEUT <n>+<s>` combined (B5-0516; face-up cards
only, read from the B5-0368 damage API). Hand cards carry the same
marker via the same API (B5-0519, right-aligned on the stats baseline
so it cannot collide with the left-aligned stat string) — dormant in
normal play since damage targets in-play cards, but visible if the
engine ever applies hand-card damage.

> **B5-0520 re-sweep (post-B5-0506 + B5-0516, 2026-09-26):** after the
> shunned wiring (B5-0506) and damage-state readouts (B5-0516) landed,
> the harness health re-sweep re-ran RUN_TESTS=1 (460/460 conformance
> PASS) plus all seven standalone probes (0350 tiebreak 26, 0351 AI
> contract 10/10 bands unshifted, 0382 station 6/0, 0383 participation
> PASS, 0384 lead-fleet 9/0, 0419 war PASS, 0443 human-seat 37/37) —
> all exit 0. The 0351 band re-confirmation is the key check since 0506
> moved registry/scoring-adjacent paths.
board pill lists at-war faction pairs whenever a war exists, and a bottom
line always shows station influence plus Shadow/Vorlon ratings — gaining a
red `[SHADOW WAR]` marker when either rating reaches the condition-2
threshold — with any nonzero tension pairs listed just above it (B5-0427/
B5-0429; all of these stay clear in normal games, since nothing in the pool
moves tension yet).

> **B5-0445 addendum (station-hooks landed, 2026-09-26):** B5-0437 wired the
> station-influence card hooks from the 0428 proposal. Three sources move the
> ratings at the round boundary (`applyEndOfRoundStation`, run BEFORE
> `advanceRound` because `advanceRound` resets the `stationSourceFired` flag):
>
> 1. **Capture source** — winning a LOCATION_TARGET war where the location is
>    not already under friendly control raises **human-side station influence**
>    by +1 (capped 100) and sets `stationSourceFired = true`. Recapturing a
>    suppressed location restores effects but yields **no station gain**
>    (recapture = restoration, not a source).
> 2. **Vorlon presence-bleed** — when a Vorlon player holds one or more
>    captured locations at the round boundary, **vorlon influence** gains +1
>    (capped 100) and sets `stationSourceFired = true`. (Shadow presence-bleed
>    is deliberately NOT wired — there is no Shadow faction in the current
>    enum, per B5-0354; it waits for a future card hook.)
> 3. **Decay sink** — if NO source fired this round (neither capture nor
>    bleed), all three ratings (human, shadow, vorlon) decay -1 toward 0. The
>    `stationSourceFired` no-source guard means one source firing protects all
>    three from decay that round.
>
> **Condition-2 / Shadow-War implications:** Shadow War (condition-2 input)
> triggers when either shadow or vorlon influence reaches the
> `CONDITION_2_THRESHOLD (20)`. Station victory condition 2 (rulebook :176)
> crowns the unique strict influence leader when station influence reaches 20+,
> **but is suppressed when Shadow War is active** (verified by the
> `HeadlessStationVictoryTest`, B5-0382). In normal games these ratings stay
> near zero (no tension-moving cards exist yet, B5-0386), so the markers stay
> clear; the hooks exist and are unit-tested, but do not affect visible play
> until the data pool gains tension sources.

## 5. The AI seats

* **EASY (Londo)** passes often (~46% of decisions since the B5-0508
  retune; previously ~53%) and otherwise picks
  uniformly among legal actions — useful for watching mechanics play out.
  EASY never consults station ratings (B5-0453): the uniform pick is the
  intended contract, and difficulty-level behavior is unchanged.
* **MEDIUM (Delenn)** is cost-aware and deterministic; scores mercenary bids
  by projected win probability minus the bid cost (B5-0403). Since B5-0453
  it also scores station, shadow and vorlon rating context on
  influence-moving choices (war declarations on captured locations and
  location plays) — the same additive term HARD uses.
* **HARD (G'Kar)** adds agenda win-condition proximity, aftermath
  anticipation, event/contingency catch-up scoring, and rotate-effect scoring
  (B5-0344), plus conflict-side choice instead of always-oppose defaults
  (B5-0343), damage-aware attack/heal/repair scoring (B5-0378), and
  mercenary-bid scoring with a steeper opportunity-cost penalty (B5-0403).
  Its war scoring also carries the station-context term (B5-0453):
  −1 when the Shadow War is active, +2 at station influence 15+, +1 when
  shadow or vorlon influence is 15+, 0 otherwise (zero-rating invariance —
  orderings when the pool is inert are unchanged).
* Difficulty contracts are pinned by the standalone harness
  `HeadlessAIDifficultyContractTest` (B5-0351). The station-aware scoring is
  pinned by the suite's STH-AI section (B5-0453, 7 checks).
* **EASY pass-bias retune (B5-0508, human Ruling 1A — landed):** the EASY
  pass floor moved from 0.35 to 0.20 by human order, widening the
  B5-0351 contract band to 0.20-0.70 (upper bound 0.70 unchanged; MEDIUM
  and HARD untouched). Observed pass rate after the retune: **0.463**
  (contract 10/10 PASS). The two B5-0422 advisory proposals are resolved
  by this ruling; the EASY-vs-MEDIUM blur it introduces was accepted per
  the brief. Acceptance evidence (B5-0509, 0447 seed-matched, 10 games):
  9 WINNER + 1 TIMEOUT reproducing in the historical seed-105 game-2
  slot; promotions rose 3.2 → 4.4/game (EASY passing less feeds more
  actions to every seat; within noise, watch on the next re-probe);
  builds ~flat 9.0; agendas stable 3.6; initiator win 55%.
  [CORRECTED by the B5-0527 reconciliation audit (2026-09-26): the cited
  0509 report’s own per-seed table places the TIMEOUT at SEED 109
  GAME 2 (round 11), not the seed-105 slot; and this paragraph’s
  figures match the 0509 row’s verify cell, not the cited report,
  which gives promotions 4.7/game, builds 8.6, aftermaths 8.2,
  agendas 4.3, initiator win 53% (66/124). Both runs support the
  ACCEPTED verdict; the second run’s logs were not persisted.]  Note: default seat mixes already include exactly one EASY seat (Main:
  MEDIUM/HARD/EASY; harnesses: EASY/MEDIUM/HARD/MEDIUM) — no all-EASY
  default exists in the tree.

## 6. Headless testing (no UI)

`sh compile.sh` with `RUN_TESTS=1` runs the full conformance suite
(**460 checks**; see the suite banner for the live count; re-verified green
2026-09-26 with the B5-0506 shunned-wiring, B5-0516 board-readout, and
B5-0528 mines wiring changes coexisting green, by the B5-0500 re-sweep
before them, and by the B5-0532 hygiene re-sweep after them, which also
re-ran all seven standalone probes PASS) plus a smoke game. One transient smoke-run failure mode is
known and classified — see the B5-0495 honesty note in section 7.
B5-0437's station hooks added 14 STH
assertions (373 → 387); B5-0453 added 7 STH-AI assertions (387 → 394);
B5-0464 added 4 AGL-LOG assertions (394 → 398); B5-0436 added D6/D7 named
assertions and D15
winner-only `influenceReward` coverage; B5-0469 added 22 ENH-SEAM assertions
(398 → 420ish band, see suite banner for the live count); **B5-0473 added 14
ENH-WIRE assertions** covering the opponent-targeted enhancement wiring;
**B5-0486 added 8 FLOOR assertions** for the minimum-1
bonus-floor path; **B5-0506 added 16 SHN assertions (current total
460)** for the shunned opponent-character wiring (explicit character
target, four per-stat penalties in the victim registry, reactive
discard-on-heal). Several standalone CLI harnesses exist
(not wired into RUN_TESTS — run directly with `java -cp out`):

* Human-seat end-to-end probe (B5-0443, extended B5-0460 to 26 checks,
  B5-0471 to 29, **B5-0478 to 37**):
  drives a full game through the same `submitHumanAction` path the UI
  buttons use, and additionally exercises heal, repair, mercenary bid and
  war declaration on a synthetic fixture so the formerly soft-gated paths
  are hard coverage on every run. **B5-0478 addition:** an opponent-targeted
  enhancement scenario (Censure-shape fixture) asserts the penalty lands in
  the opponent's bonus registry via the public read path — the fixture part
  is deterministic. **B5-0482 determinism outcome (supersedes the B5-0476
  flake note):** the human driver RNG and the starter-deck random draw are
  both now seeded from the CLI seed (`StarterDeckBuilder.setRandomSeed`), and
  the `COVERAGE: agenda lifecycle` gate is a SOFT gate like bid/war — a
  rulebook-faithful game can legally end with zero agenda lifecycle actions,
  so it no longer fails runs. Residual honesty: because the soft gate marks a
  check only when the path fires, the printed check count can differ between
  runs (e.g. 36 vs 37) with the same seed; the PASS/FAIL verdict, not the
  count, is the gate. **B5-0471 addition:** a face-up agenda install check asserts the
  `"sets agenda:"` token (the B5-0464 emitter fix for the B5-0459 zero-count
  parser artifact) is emitted from a human seat.
  `java -cp out b5ccg.engine.HeadlessHumanSeatProbe [seed] [timeoutSec]`
* Seeded multi-round runner from B5-0349 prints N games of aggregate stats.
  **B5-0444 update:** the per-game timeout is now parameterized (180s default,
  overridable via a 3rd CLI arg) and each game reports a `terminator=` classification:
  `WINNER` (rulebook victory), `ROUND_CAP` (natural round exhaustion without a
  winner), or `TIMEOUT` (the per-game window fired while the game thread was
  still running). Per-round progress lines are emitted as each round is reached.
  ```
  java -cp out b5ccg.engine.HeadlessMultiRoundTest <games> <seed> [timeoutSec]
  ```
  The default 180s window is long enough for natural termination in seeded games
  (observed WINNER at round 11, ~142s, at the 300s cap); shorter caps produce
  TIMEOUT-terminated games for testing. See the B5-0444 report for observed
  termination classes across 60s/120s/300s caps.
* Reporting tiebreak probe (B5-0350) evaluates the round-cap tiebreak chain.
* AI difficulty contract test (B5-0351) asserts EASY randomness bounds and
  MEDIUM/HARD cost-aware ordering; exits 0/1.
* Station victory probe (B5-0382) drives station influence to 20+ and
  asserts condition-2 crowns the strict leader.
* Participation gates probe (B5-0383) exercises Border Raid, Limited Strike,
  and Complete Support participation rules against real loaded data.
* Lead-a-fleet scenario probe (B5-0384) checks legal pairing, handler
  rotation, and round-expiry.

## 7. Known gaps (with task links)

The rulebook's §V action list is partially reachable. The full
action-by-action audit lives in
`.agent/REPORTS/2026-09-23-freebuff-01-B5-0345.md`; the short version
reflects the working tree as of 2026-09-25:

**Human-action controls now reachable:** Lead Fleet, Use Rotate Effect, Attack,
Heal, and Repair have live UI controls; see the control reference above and
the live list below.

**Engine done and live — visible or materially affects gameplay today:**

* **Influence economy** — the D9 rating-vs-applied-pool split (B5-0369) is
  live: Build Influence applies +3 to your per-turn pool (not your permanent
  Rating), and sponsor/promote spend from the pool. The old defect where
  every sponsor permanently eroded your Rating is resolved. The
  free-participant waiver (B5-0374, E3) is also live. The E1 double-cost
  pool interaction (B5-0373) is DONE: the named `isDoubleCostRequired`
  helper and neutral exemption are wired into the applyInfluence spend site
  — behaviour-preserving (base recruit cost math unchanged); its coverage remains
  green in the current 444-check suite.
* **Damage subsystem** — characters can be damaged and neutralised; the
  damage/neutralisation model (B5-0368) plus attack (B5-0370) and
  heal/repair (B5-0371) are all live, with UI controls landed (B5-0402).
  Board-card selection landed (B5-0423), so **Heal and Repair are reachable
  in real play**: select the damaged character, fleet or location on the
  board and the buttons enable via the engine's own predicates.
  **Human attack window** — live (B5-0432 engine plus B5-0440 UI): after
  mandatory participation, an eligible human can select a ready board
  attacker, choose an explicit legal target, and activate Attack. The
  controller revalidates the pair and reuses B5-0370 damage, faction,
  participation, and overflow rules. Pass becomes **Skip Attack** during
  this optional wait. This supersedes the former human-seat structural gate
  from B5-0409 finding A.
  **In AI-vs-AI play attacks do not fire either**: conflicts still resolve
  synchronously inside the initiating action, so the active-conflict state
  the attack requires never exists when an AI seat picks its action — pure
  AI-vs-AI rounds observed zero attacks, heals, repairs, and neutralisations
  across all verification probes. That remains a separate real engine-loop
  gap (the rulebook's separate Resolution Round is the fix direction), not
  a human UI bug.
* **Assistant mechanic** — real (B5-0339): rotate your assistant for +1
  Diplomacy/Intrigue/Leadership, or consume a sponsor discount on a later
  recruit. The A+/A$ markers on the ambassador mini-card reflect live state.
* **Bonus layer** — stat bonuses now use an expiry-backed layer (B5-0367)
  rather than permanent mutation; fleet leadership (B5-0337) and assistant
  (B5-0339) compose through it. Not directly player-visible beyond the
  existing markers, but it means round-boundary expiry and stacking now work.
* **AI mercenary bidding** — live (B5-0403): MEDIUM/HARD offer and score
  BID_ON_MERCENARY actions for offered mercenaries (minimal strictly-winning
  increment, pool-affordability gated);  EASY picks uniformly from the same
  legal list. MER-AI conformance section ×10 landed with it (historical suite
  expansion; current total is 444 after B5-0436 (373) + B5-0437 STH ×14 + B5-0453 STH-AI ×7 + B5-0464 AGL-LOG ×4 + B5-0469 ENH-SEAM ×22 + B5-0473 ENH-WIRE ×14 + B5-0486 FLOOR ×8).
* **Declare War UI** — live (B5-0407): a war target selector + Declare War
  button wired to the B5-0376 engine branch; the selector lists races at
  war, then their locations. Inert in normal games (no tension sources in
  the pool; see the control-reference note).

**Still open — unchanged or newly flagged:**

* **Mercenary card data** — no data evidence in the pool (B5-0386,
  no-evidence verdict). The engine slice (B5-0395), AI bidding (B5-0403),
  and bid UI (B5-0404) are all DONE using synthetic fixtures; a real game
  still has nothing to bid on until real mercenary cards are sourced (needs
  human direction).
* **Bid control reads only the first offer** — the B5-0423 audit's P3
  residual: the bid handler hardcodes the first offered mercenary
  (`offers.get(0)`), so if several mercenaries were ever offered
  simultaneously only the first would be bidable. Latent today: the
  mercenary pool is empty (B5-0386), so the control shows "(no mercenary
  offers)" in every real game.* **Opponent-targeted enhancements** —
  the engine path is live (B5-0468 model seam + B5-0473 wiring: an explicit
  opponent target routes the penalty into that owner's fleet registry, held
  in play if the target cannot resolve), and the pool's only exact-class
  card is Censure (B5-0477: `enh_censure` premiere + `de_enh_censure`
  deluxe, Military −2). The UI target-picker (B5-0487) lets a human player
  select an opponent faction + face-up fleet to populate the B5-0468
  target before playing the card; if no opponent fleet is available the
  card still submits via the engine's held-in-play path (no self-fallback).
  Row-text correction (B5-0496, per the B5-0493 audit): the B5-0487 claim
  of an API ripple (`GameState.getFactions()`,
  `Player.canShowFleetForController()`, `Faction.isWon()`) was fabricated —
  the shipped picker uses existing API (a `getPlayers()`/`getFleets()`
  iteration with face-up, unrotated, non-human, non-forfeited filters) with
  zero model churn; the delivered control is real and unchanged.
  RESOLVED (B5-0484 status update): the printed
  "(minimum 1)" floor on Censure is now implemented via B5-0486 (per-bonus
  floor field on StatBonus + effectiveStat floor pass + BONUS_FLOORS table;
  FLOOR ×8 suite section) — the B5-0477 flag is closed.
  **B5-0506 extended the wiring to opponent CHARACTERS** (the shunned
  pair): a character enhancement carrying an explicit target attaches its
  four per-stat penalties to the chosen opponent character and is
  discarded — lifting its bonuses — when that character is healed
  (reactive discard-on-heal, SHN ×16 suite section). The B5-0522 picker
  extension makes this path human-reachable (below). Data note RESOLVED (B5-0523):
  the shunned records previously carried `militaryBonus: 0` against their printed
  "all stats" text (B5-0311 class; data wins), so the penalty values in
  play came from the engine table; the data task landed 2026-09-26 and both
  records now carry `militaryBonus: -2`, matching the printed text.
* **Mines + Energy Mines reactive (B5-0528)** — attacking an opponent
  protected by Mines in play adds +1 to the damage your fleet takes back:
  the engine checks the defending side for a held Mines-class enhancement
  at the attack-resolution site (`DAMAGE_ON_ATTACK` registry over all four
  pool ids: enh/de_enh_mines, enh/de_enh_energy_mines). Energy Mines keeps
  its printed +2 Military self-attach on the owner’s best fleet via the
  normal fleet-grant path. Playtest note: the reactive fires once per
  resolved attack on the protected side; check the battle log line for the
  inflated return-damage figure.
  **B5-0523 (shunned militaryBonus data values):** the two shunned records
  [SUPERSEDED 2026-09-26 by B5-0523 landing: both records now carry
  militaryBonus -2, matching the printed text; the present-tense
  discrepancy note below predates the data fix. See the verified
  B5-0531 note above.]  (`enh_shunned` in premiere.json, `de_enh_shunned` in deluxe.json) carry
  `militaryBonus: 0` in data against their printed "loses 2 from all stats"
  text (B5-0311 class; data wins over printed text for the B5-0473/B5-0506
  engine grant path, which reads each bonusFor value individually). The
  printed text implies -2 Military, but the engine grants whatever the data
  field says (0 for both records until a data task backfills them). A data
  task (separate from this guide refresh) sets both records' militaryBonus to
  -2 so the data matches the printed text; this guide row documents the
  current data-vs-printed discrepancy for playtesters. GUIDE GAP CLOSED by
  this row (data note surfaced).
  **B5-0522 (opponent-character target picker for shunned-class CHARACTER
  [SUPERSEDED 2026-09-26 by B5-0522 landing: the picker IS live (the
  trailing NOTE describing "no picker" is the pre-0522 defect state),
  and the "non-won" filter does not exist in code (B5-0493 F1
  fabrication descendent). Same-class fix also repaired the 0487 fleet
  handler’s targeted branch, which tested a seam its own delegate
  sets at submit time so targeted fleet clicks self-penalized.]  enhancements):** the Play Card handler now populates an opponent-character
  dropdown when the selected hand card is a CHARACTER enhancement carrying
  the 0468 seam (shunned-class today) — face-up, non-won, non-human,
  non-forfeited opponent characters across Inner Circle, supporting role,
  and ambassador, per the B5-0506 characterById scope. Selecting a target
  routes play through setOpponentTarget before submit, mirroring the B5-0487
  fleet flow including empty-state disable and the held-in-play fallback when
  no opponent character is available. NOTE: MainWindow.refreshCensureControl
  gates the picker on Enhancement FLEET subtype only, so a human-played
  shunned-class CHARACTER enhancement has no picker and routes to the legacy
  self path (self-penalizing); the B5-0506 engine explicit-target CHARACTER
  branch is live for AI. GUIDE GAP CLOSED by this row.

* **Opponent-CHARACTER target picker (B5-0522, verified)** — selecting an
  Enhancement CHARACTER card from hand now shows an opponent-character
  dropdown (face-up inner circle, supporting role, and ambassador of every
  non-forfeited opponent — the same scope the engine resolves); picking
  one and pressing Play (opponent char target) routes the penalty into that
  character’s owner registry; with no target chosen the engine’s
  held-in-play rule applies (no self-fallback). Verify: RUN_TESTS=1 green
  (460/460 + smoke PASS), B5-0443 human-seat probe 37/37.
**Honesty notes for playtesters:**

* **A transient smoke-run FAIL is a harness-internal race, not an engine
  defect (B5-0495 triage).** `HeadlessSmokeTest` very occasionally exits 1
  with `AIPlayer(...) chose DISCARD_AGENDA: <title> but that card is not in
  its hand` (reproduced 1 in 8 consecutive runs; a rerun passes). The
  harness watcher declares round 1 complete as soon as `roundNumber > 1`
  while the daemon game loop is still running round 2; the harness then
  asks each AI for a decision on that live state. The DISCARD_AGENDA
  payload points at the player's agenda slot, so if the loop discards that
  agenda between the AI's offer and the harness's check, a perfectly legal
  action fails the harness's hand-membership assertion (which exempts only
  Build Influence, Promote and Use Rotate Effect). Not stale offers —
  `buildLegalActions` is recomputed from live state on every call — and
  not engine nondeterminism; the engine re-gates every action and never
  crashes on it. Fix direction (checker exemption for slot-payload types,
  or quiescing the loop before the live-state check) is recorded for
  B5-0505. No source has changed; treat a single smoke FAIL as
  rerun-and-verify.
* **"Stalled" is mostly a harness-timeout label, not a true hang.** The
  multi-round runner (B5-0349) labels a game "stalled" when no player has
  won inside its 60s per-game window. Independent no-timeout verification
  (B5-0409, both sessions) shows seeded games actually **terminate
  naturally**: the probe game ended at round 12 (~118s) with a standard
  victory at Influence Rating 20. Rounds advance steadily (~8-10s each), so
  the action phase is not hanging — the 60s harness window just closes before
  low-action games reach a winner. Read any harness "stall rate" that way.
* **Quiet, pass-heavy rounds are still real.** When all four seats pass
  consecutively the action phase ends; every default mix has exactly one EASY
  seat (Main: MEDIUM/HARD/EASY; harnesses: EASY/MEDIUM/HARD/MEDIUM — no
  all-EASY default exists) at ~46% pass bias since the B5-0508 retune
  (previously ~53%), and rounds can advance with
  sparse action. B5-0372's
  D6 initiative cycle is live (un-passing on non-pass actions, 8×playerCount
  liveness backstop), but the EASY bias still makes some rounds quiet.
* **Zero promotions in harness output is a counting artifact.** The B5-0349
  `parseLog` counts promotions only for the token `": promotes "`, which no
  real log line produces: `RulesEngine` logs `"<player> promotes <title>…"`
  (no colon) and the action line is `"<player>: PROMOTE_CHARACTER: …"`.
  So promote counts print 0 even when promotions occur (the B5-0409 probe
  observed Inner Circles growing 1→2 during play). Trust IC size in a live
  game, not the harness promote figure. (Fix flagged for B5-0413.)
* **B5-0447 re-probe (10 games, seeds 101-109 step 2, 180s window):** 9
  WINNER + 1 TIMEOUT — timeout behavior is down to ~10% from the earlier
  all-timeout baseline under the parameterized window. End-state inspection
  counted ~4.9 promotions per game while the log-token promote figure
  printed 0 (the parser artifact above). The aggregate "agendas set" figure
  read 0/10 — the same parser-artifact class: the runner's agenda token
  still matches no real log line even though installs are visible in live
  inspection, so read agenda participation from the game log, not the
  aggregate. (B5-0413 still pending.) The B5-0462 re-probe under the
  station-aware AI reproduced this shape seed-for-seed (9 WINNER + 1
  TIMEOUT, the timeout in the same seed-105 game-2 slot as the baseline);
  promotions now print real counts and read ~3.2/game, mostly because
  games got shorter (mean rounds ~10.5 → ~8.4; per-round rate nearly
  flat) — see the B5-0465 triage. Balance re-probes must reuse the 0447
  seed set (2 games × seeds 101-109 step 2, 180s) for their deltas to be
  quotable; other seed choices are exploratory.
* **Influence Rating 20 is reachable and standard victory can trigger.**
  The B5-0409 probe reached Rating 20 in ~2 minutes and ended the game at
  the round boundary. In the sampled games no agenda won before standard
  victory did — the agenda economy currently loses that race, not because 20
  is unreachable but because build-influence has no cap and compounding
  bonuses outrun agenda conditions.
* **Agendas ARE being installed and played** — earlier "agendas set: 0"
  harness output is the same parser-artifact class as the promote count:
  the runner's agenda token matched no real log line (confirmed by the
  B5-0459 triage). End-state inspection in the B5-0409 verification found
  agendas installed in every sampled game (e.g. "As It Was Meant To Be"),
  plus live DISCARD_AGENDA plays. Since B5-0464 the missing emitter is
  landed and the aggregate counts real face-up installs — zeros before
  that change are artifacts, nonzero figures after it are real.
* Deck-out penalty is live (B5-0204/B5-0304): drawing from an empty deck
  discards your non-ambassador Inner Circle character if you have one, else
  signals forfeit for the victory check. Severe-damage overflow can also
  neutralise (B5-0368).
* Conflict resolution is synchronous at initiation, not in a separate
  Resolution Round. The human join window is real (B5-0363): when a conflict
  is active and you are eligible, the Support/Oppose controls light up and
  your click unblocks the controller. After mandatory participation, the
  human attack window (B5-0432/B5-0440) is live when a legal opposing target
  exists: choose an explicit target and Attack, or choose Skip Attack. The
  engine revalidates the pair before resolving. Mandatory-participation
  conflicts (B5-0336) still compel your ambassador when the rules require it.
* **Conflict initiators win more often than not.** B5-0409 reproduction
  measured ~64% initiator wins on trackable lines (58-71% varied by seed and
  trackability); 0408 reported 71%. This is consistent with the B5-0309
  sides rule (initiator wins iff support > opposition at initiation) plus the
  AI's B5-0343 "initiate when winning" bias — not a pathology. The B5-0462
  re-probe against the station-aware AI measured 67.5% (54/80), re-entering
  the band from the 0447 sample's 87%: the 0453 term raises war-declaration
  value, so more initiated wars meet genuine opposition instead of walking
  through unopposed (B5-0465 triage: real drift, band-normalizing, watch
  but do not fix).
* Advisory research (non-authoritative): SNRPG bulk extraction strategy and
  the CCG Trader 2-card pilot are in `investigations/`; they informed the
  cost backfill (B5-0335) and fleet-class plan (B5-0387) but are not
  canonical.

## 8. How to record playtest findings

Do not edit code or data while testing. Write observations (bugs, balance,
confusions) to `investigations/` as a new dated note or into an existing
report thread, and seed a task row through the normal OPEN-claim cycle so
an agent can pick it up. If a game hangs or errors, the console log lines
(`[Rn]`-prefixed state log) are the first thing to capture.