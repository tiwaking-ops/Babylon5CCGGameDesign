---
document:
  title: "Design proposal — B5-0358: war-conflict participation rules (tension/war state, race vs location targeting, the all-supported uncontested test)"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  last_modified_date: "2026-09-23"
---

# B5-0358 design proposal — war-conflict participation rules

Proposal-only. No `b5ccg/src/` or `b5ccg/resources/` file is touched by this
task. Source: rulebook §States/War (:799–815), the sample conflict round
(:374), §Unconditional Surrender (:817, pointer only), same-race
non-aggression (:974); cross-references: B5-0336 (declared targets,
requiresTarget), B5-0309 (support/opposition sides), B5-0345 (attack actions
are a Tier-3 gap this design depends on for one clause), B5-0357 (bonus
layer; unaffected here), B5-0353 (fleet-class vocabulary — unrelated).

## 1. Problem statement

War conflicts do not exist in the engine, and the state they depend on does
not exist either:

* **No tension, no war.** A grep for `tension|Tension|WarState|atWar` over
  `b5ccg/src/` returns nothing. Races have no pairwise relationship, so
  nothing can gate or trigger war conflicts.
* **No card-less declaration.** `GameAction.Type` has `INITIATE_CONFLICT`
  (which requires a hand `ConflictCard`), and `canInitiateConflict` checks
  `p.getHand().contains(c)`. The rulebook's "may always declare a War
  Conflict (no conflict card or influence required)" path has no action
  type, no factory, no legality check.
* **No location targeting.** `Conflict.target` and `GameAction.target` are
  `Player`-only (B5-0336). The rulebook's "target either the opposing race
  as a whole or else a specific location in play for a faction of that
  race" cannot be expressed; `Player.getLocations()` exists but no conflict
  can point at one, and `LocationCard` has no capture/suppression state.
* **No uncontested semantics.** B5-0309 resolution wins on
  support > opposition, uniformly. The rulebook's war-only outcome rule —
  win + *uncontested* ⇒ influence swing; win + *contested* ⇒ no effect —
  has no implementation site, and "contested" includes *attacked*, which
  the engine cannot yet represent (no attack action; B5-0345 Tier 3).
* **No tension increment.** "Whenever a War conflict is resolved, the
  target's tension toward the player who initiated the conflict increases
  by 1 (to a maximum of 5)" — no site exists.

So war participation is blocked three layers deep: state, action, outcome.

## 2. Rulebook specification (normative excerpts)

* §War (:803): entering war cancels all other states between the races.
* §War (:805): "Any player whose race is embroiled in a war may always
  declare a 'War Conflict' (no conflict card or influence required) during
  the conflict round targeting a race with whom they are at war. A war
  conflict counts as a player's conflict for the turn."
* §War (:807): "A war conflict may target either the opposing race as a
  whole or else a specific location in play for a faction of that race. A
  war conflict is a 'Military conflict'."
* §War (:809): location-target win ⇒ **capture** — inherent effects and
  abilities suppressed; recapture by the original owner restores everything
  including the Military ability; location enhancements are discarded on
  capture or recapture.
* §War (:811): race-target win ⇒ "If you win the conflict and the conflict
  is uncontested (**all participants in the conflict must have supported
  the conflict**), then the target loses one influence and you gain one
  influence… If the conflict is contested (at least one participant opposed
  or attacked) then it has no effect. However, aftermath cards will still
  be valid depending on whether the initiator has 'Won' or 'Lost'."
* §War (:813): resolution always raises the target's tension toward the
  initiator by 1, max 5.
* :374 sample: the war conflict is *declared* in step 1 without a target and
  the target (race or specific location) is **named at initiation** —
  mirroring B5-0336's declare/reveal split.
* :974 (record-only today): same-race factions at tension ≤ 3 have
  automatic non-aggression and cannot target each other with Military
  conflicts; :982 adds the multi-faction influence-loss spreading rule.

## 3. Design

### 3.1 Tension/war state — engine-owned (not JSON)

New `model/TensionMatrix` (or engine-owned helper owned by `GameState`):

* Pairwise `Map<RacePair, Integer>` tension in 0–5, plus explicit
  `Set<RacePair> atWar`. War is *entered* through named entry points
  (`enterWar(a, b, source)`) rather than read off `tension == 5`, because
  the rulebook's entry triggers are card- and effect-driven (tension 5 via
  events, "Declaration of War" cards, Shadow-War interplay) and several
  cards alter the *consequences*, not the number. Deriving war purely from
  tension would hard-code one trigger and make card-driven entry
  unrepresentable.
* One-way increments: `raiseTension(target→initiator, +1)` clamps at 5 and
  **may** enter war when reaching 5 if the source says so (each entry site
  passes its own policy flag; the war-conflict resolution increment uses
  the tension-only policy — the rulebook does not say mutual tension 5
  auto-declares war; that is triggered by cards). Recorded interpretation.
* Not in JSON: per the task row and the rulebook, tension is a *game
  state* quantity; card data carries only triggers/effects (which remain
  in CardEffects tables). Nothing in `premiere.json`/`deluxe.json` changes.

### 3.2 War conflicts as first-class Conflicts

* `GameAction.Type` gains `DECLARE_WAR_CONFLICT` (factory
  `warConflict(Player targetPlayer)`); the location choice is **not** in
  the action — matching :374, the target detail is declared at initiation,
  i.e. when the engine creates the `Conflict`.
* `Conflict` gains a second, optional target slot: `LocationCard
  targetLocation` (B5-0336's `target` Player slot is untouched; exactly one
  of the two is non-null for a war conflict). `GameAction` needs no new
  fields.
* No synthetic ConflictCard is introduced: `Conflict` already carries
  `card` non-null today. Smallest change is a nullable-card mode: war
  conflicts carry `card == null`, `conflictType = MILITARY`, and a
  `warKind` enum (`RACE_TARGET | LOCATION_TARGET`). Rationale: a synthetic
  card would need a fake id/faction/text that every `card.getId()`-keyed
  CardEffects table would have to know to ignore — a recurring trap.
  Alternative considered and rejected for that reason.
* One-conflict-per-turn (B5-0302) applies unchanged: declaring a war
  conflict marks `conflictsInitiatedThisTurn` like any other.

### 3.3 Eligibility (`RulesEngine.canDeclareWarConflict(p, state)`)

Offerable when: not passed; actionsLeft > 0; not already initiated a
conflict this turn; the player's race is in `atWar` with ≥1 other race
(the action is offerable without a chosen target; the chosen target is
validated at initiation: `isAtWar(p.getRace(), targetRace)` for race
targets, or the location's controller's race for location targets).
No hand check, no influence check — per :805 verbatim. UI/AI see it through
the existing legal-action offer loop.

### 3.4 Resolution deltas (the war-only outcome rule)

On a war conflict resolving (initiator wins iff support > opposition,
B5-0309 unchanged):

1. **Uncontested test** (race-target kind only):
   `uncontested = opposers.isEmpty() && !anyAttackOccurred(conflict)`.
   * All-supported is directly readable from the B5-0309 side sets — no
     new tracking.
   * **Attacks are the dependency:** `anyAttackOccurred` cannot exist until
     the attack action does (B5-0345 Tier 3). Interim semantics: treat
     `anyAttackOccurred` as constant false, recorded as such in the
     implementing task and the code comment, so the all-supported half
     ships now and the attacked half activates with the damage subsystem.
   * Win + uncontested ⇒ target loses 1 influence, initiator gains 1.
     Win + contested ⇒ **no effect** (but the resolution is still a Won
     for aftermath eligibility — D1's initiator-perspective semantics
     unchanged). Loss ⇒ normal loss handling, no influence swing.
2. **Location-target kind:** win ⇒ capture. `LocationCard` gains a
   `capturedBy` owner pointer + `effectsSuppressed` flag: inherent income
   (`influencePerRound` in `Player.getIncome()` path) and Military use are
   gated on it; location enhancements are discarded from the owner's list
   on capture; recapture by the original owner restores everything.
3. **Tension increment (always):** `raiseTension(targetRace → initiator,
   +1)` after resolution, both kinds, max 5.
4. Aftermaths: unchanged — `canPlayAftermath` reads Won/Lost exactly as
   today; war resolution just produces those outcomes like any conflict.

### 3.5 Same-race non-aggression and civil war (record-only)

The engine has one faction per race, so :974/:982 have no live target.
The design keeps the door open: `TensionMatrix` keys on faction pairs (two
same-race factions are a legal key), so civil-war rules can land without a
migration. No behavior proposed now.

### 3.6 Consumers and touchpoints

* `GameController.processAction`: new switch branch for
  `DECLARE_WAR_CONFLICT` → initiation with `card == null` + chosen
  race/location target.
* `AIPlayer`: offer legality (via the existing loop) + modest scoring —
  declare only when at war, prefer race targets when the influence swing is
  uncontested-likely, location targets when a high-income enemy location is
  poorly defended (read from committed-strength proxies). Numbers left to
  the implementing task; parity with B5-0301's deliberately-modest Build
  Influence scoring is the house style.
* UI: war conflicts flow through the existing conflict readouts (B5-0346
  sides, B5-0347 banner) with no new panels; the declared kind is visible
  via the conflict card slot being empty — a `WAR` label is recommended in
  the implementing task's UI slice.
* Shadow War interplay (B5-0340 station/standard-victory guard): the
  Shadow War is a separate global state; out of scope here, but
  `TensionMatrix` deliberately does not model Shadow/Vorlon influence.

## 4. Phasing and conformance tests

Phases (each independently green, following the B5-0357 house pattern):

* **A. State + action + race-target resolution** (tension matrix, action
  type, eligibility, uncontested-with-attacks-as-false, tension increment).
* **B. Location targeting + capture/suppression/recapture.**
* **C. Attack integration** (flips the `anyAttackOccurred` interim constant
  once B5-0345 Tier 3 lands) + AI scoring slice + UI label slice.

Conformance additions (HeadlessConformanceTest pattern):

1. at-war player is offered `DECLARE_WAR_CONFLICT`; non-war player is not.
2. declaration consumes the one-conflict-per-turn slot; second declaration
   same turn is illegal.
3. win + zero opposers ⇒ −1/+1 influence swing; pin exact values.
4. win + one opposer ⇒ no influence change (contested), aftermath sees Won.
5. tension rises exactly +1 toward the initiator after resolution,
   clamps at 5 across repeated wars.
6. location capture suppresses income and Military; recapture restores;
   enhancements discarded on both events.
7. race-target win still raises tension (both kinds increment).
8. non-war Military conflicts resolve exactly as before (no regression on
   the B5-0309 suite).

## 5. Risks

* **The interim uncontested constant** is the main correctness debt:
  until attacks exist, "attacked" cannot contest. Documented in code, task,
  and here; phase C closes it.
* **Entry-trigger policy divergence** (which tension/card paths auto-enter
  war) is a rules-interpretation surface; this proposal pins only the
  resolution-side increment as non-declaring and leaves each entry site
  explicit.
* **Nullable ConflictCard** touches every `conflict.getCard()` caller;
  the migration must audit each (they are few and enumerated in
  CardEffects/Conflict).

## 6. Explicitly unchanged

B5-0309 sides and tie rule; B5-0336 requiresTarget gating; aftermath
Won/Lost perspective (B5-0204/D1); Build Influence and other action
economics (B5-0342/D9 own influence); B5-0337 leader / B5-0339 assistant
overlays; no card JSON changes (engine-owned state per the task row).
