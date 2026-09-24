---
document:
  title: "Human Playtest Guide — UI Game, Action Reference & Mechanics Matrix"
  status: "Guide"
provenance:
  author_llm: {name: "Qwen Code", version: "qwen-2.5-coder"}
  assessor_llm: []
  last_modified_by_llm: {name: "Qwen Code", version: "qwen-2.5-coder"}
  created_date: "2026-09-23"
  last_modified_date: "2026-09-23"
---

# Human Playtest Guide: Babylon 5 CCG

A comprehensive operational manual, action reference, and mechanics implementation guide for human playtesters evaluating the Babylon 5 CCG Java client.

---

## 1. Quickstart: Building & Running

### System Prerequisites
* **Java Development Kit (JDK):** JDK 8 (e.g. `1.8.0_292` or compatible).
* **Language Level:** The codebase compiles strictly with `-source 6 -target 6` using only standard library classes (no external JARs or dependencies).

### Build & Run Commands

#### Windows (`cmd.exe` or PowerShell)
```cmd
cd b5ccg
compile.bat
run.bat
```
* Or directly via Java:
```cmd
cd b5ccg
compile.bat
java -cp out b5ccg.Main
```

#### Linux / macOS / MSYS / Git Bash
```bash
cd b5ccg
./compile.sh
./run.sh
```
* To compile and run the full automated verification test suite:
```bash
cd b5ccg
RUN_TESTS=1 ./compile.sh
```

### Headless Test Runners (Automated Testing)
If running headless or running benchmarks:
* **Rulebook Conformance Suite:** `java -cp b5ccg/out b5ccg.engine.HeadlessConformanceTest`
* **Single-Round Smoke Test:** `java -cp b5ccg/out b5ccg.engine.HeadlessSmokeTest`
* **Seeded Multi-Round Playtest Harness:** `java -cp b5ccg/out b5ccg.engine.HeadlessMultiRoundTest <games> <seed>` (e.g. `java -cp b5ccg/out b5ccg.engine.HeadlessMultiRoundTest 2 42`)
* **AI Difficulty Contract Suite:** `java -cp b5ccg/out b5ccg.engine.HeadlessAIDifficultyContractTest`

---

## 2. Game Startup & Setup

1. **Launch:** Executing `Main` starts the graphical client with the system look-and-feel.
2. **Faction Selection Dialog:** A prompt asks you to choose your faction:
   * **Human Systems** (Ambassador: *Jeffrey Sinclair*)
   * **Minbari Federation** (Ambassador: *Delenn*)
   * **Centauri Republic** (Ambassador: *Londo Mollari*)
   * **Narn Regime** (Ambassador: *G'Kar*)
3. **Player Roster & AI Opponents:**
   * **You:** Human player with your selected faction.
   * **Opponent 1:** Delenn (AI Difficulty: `MEDIUM` — cost-aware, strategic conflict join/target evaluation).
   * **Opponent 2:** G'Kar (AI Difficulty: `HARD` — optimized scoring, aggressive counter-play).
   * **Opponent 3:** Londo (AI Difficulty: `EASY` — randomized choices with designed ~53% pass bias).
4. **Deck Construction:**
   * Uses real 60-card Premier starter decks (50 fixed faction cards + 10 random uncommons/rares from the card pool, loaded via `StarterDeckBuilder`, [B5-0319]).
   * The faction Ambassador is guaranteed and placed in your opening setup.
   * Initial hand size: 4 cards.

---

## 3. Game Interface Tour & Controls

The single-player window (`b5ccg.ui.MainWindow`) is divided into distinct functional areas:

```
+---------------------------------------------------------------------------------------------------+
| Top Toolbar: Actions, Cost Preview, Target Selector, Support/Oppose, Initiative, Filter/Sort     |
+-------------------------------------------------------------------+-------------------------------+
|                                                                   | Right Sidebar:                |
| Center Board (GameBoardPanel):                                    | 1. Game Log (Grouped by Phase)|
|  - 4 Faction Zones (Influence, Power, Ambassador, IC, SR, Fleets) | 2. Conflict-to-Ability Legend |
|  - Active Conflict Banner + Sides Readout + Participant Breakdown |                               |
|  - Last Resolved Conflict Outcome Banner                          |                               |
+-------------------------------------------------------------------+-------------------------------+
| Bottom: HandPanel (Interactive Hand, Filtering, Sorting, Cost Tags, Affordability Dimming)       |
+---------------------------------------------------------------------------------------------------+
```

### Top Toolbar Controls
* **Pass Turn:** Yields your action for the current round cycle.
* **Play Card:** Plays selected non-conflict cards (Events, Enhancements, Locations, Groups, Agendas) from hand.
* **Initiate Conflict:** Initiates the conflict card selected in hand against the target chosen in the dropdown.
* **Sponsor:** Recruits the selected Character card from hand into your Supporting Role (spends Influence cost).
* **Promote:** Promotes the selected Character card from Supporting Role to Inner Circle (rotates an unrotated IC leader and spends promotion cost).
* **Build Influence:** Rotates an unrotated Inner Circle character to generate Influence (available when Influence ≤ 9).
* **Target Selector Dropdown:** Automatically populated with eligible non-human opponent targets when a Conflict card is selected in hand ([B5-0325]).
* **Support / Oppose:** Commits your faction to either the Supporting side or Opposing side during an active conflict ([B5-0325]).
* **Cost Preview Readout (`costLabel`):** Shows live preview of Sponsor cost, Promote cost, or Build Influence availability for the selected card ([B5-0326]).
* **Initiative Label:** Displays the current round's initiative order based on faction Power/Influence ratings ([B5-0327]).
* **Status Label:** Displays context-sensitive feedback on card selection, targeting, and legal action prompts.

### Hand Filter & Sort Controls (`filterPanel` — [B5-0348])
* **Type Filters (9 toggles):** `Char`, `Fleet`, `Conflict`, `Agenda`, `Aftermath`, `Event`, `Enh`, `Group`, `Loc`.
* **Faction Filters (8 toggles):** `Human`, `Minbari`, `Centauri`, `Narn`, `Neut`, `NonAl`, `Vorlon`, `ANY`.
* **Sort Modes (3 radio options):**
  * `Order`: Default hand insertion order.
  * `Cost↑`: Ascending influence cost.
  * `Cost↓`: Descending influence cost.
* **Show Dimmed Toggle:** When enabled, unplayable/unaffordable cards are rendered with a 50% dark overlay; when unchecked, affordability dots remain visible without dimming.

### Hand Cards & Visual Feedback (`HandPanel`)
* **Card Anatomy:**
  * **Header Bar:** Color-coded card type (Character = Blue, Fleet = Dark Blue, Conflict = Red, Agenda = Gold, Aftermath = Purple, Event = Green, Enhancement = Forest Green, Group = Brown, Location = Teal).
  * **Title & Faction:** Card name, uniqueness, and faction affinity.
  * **Influence Cost:** Displayed in gold as `Cost: X INF` ([B5-0333]).
  * **Stats Line:** Displays `D` (Diplomacy), `I` (Intrigue), `P` (Psi), `L` (Leadership) for Characters; `Military: X` for Fleets; Conflict Type & `+X INF` reward for Conflicts.
  * **Rarity Dot:** Gold (Rare), Silver (Uncommon), Light Blue (Fixed), Pink (Promo), Gray (Common).
* **Interactive Highlights:**
  * **Green Affordability Dot:** Displayed in top-right corner of character cards when Sponsor/Promote cost is fully affordable ([B5-0348]).
  * **Green `ELIGIBLE` Badge:** Displayed at bottom of Aftermath cards when at least one legal target exists on the last resolved conflict ([B5-0361]).
  * **Selection Ring:** Bright green outline and elevation on the clicked card.

### Game Board Readouts (`GameBoardPanel`)
* **Faction Zones:** Renders all 4 players' current Power, Influence, Ambassador, Inner Circle characters (with rotation state and Assistant bonus tags `A+`/`A$`), Supporting Role characters, Fleets (with rotated fleet-leader assignments), Agendas (with `[WIN]` status), and attached Aftermaths ([B5-0338], [B5-0347]).
* **Active Conflict Banner:** Appears centered in red during an active conflict, showing the conflict title and type ([B5-0316]).
* **Sides Readout:** Displays committed participant counts and cumulative side totals (e.g. `2 support (8) | 1 oppose (5)`).
* **Participant Breakdown:** Color-coded listing of each committed participant's cards and individual totals (Supporters in Green, Opposers in Orange) ([B5-0346]).
* **Outcome Banner:** Retains the resolution summary (Winner, Support vs Opposition totals, and Influence reward) after conflict resolution until the next conflict begins ([B5-0347]).

---

## 4. Full Action Reference (Rulebook §V Inventory)

The table below details every action defined in rulebook §V, how it operates in the game client, its UI trigger method, and its underlying engine rules.

| # | Action Name | UI Status | How to Perform in UI | Engine Rule & Mechanics |
|---|-------------|-----------|----------------------|-------------------------|
| 1 | **Sponsor a Supporting Card** | **Reachable** | Select a Character in hand &rarr; click **Sponsor** in toolbar. | Deducts printed recruit cost (`base - sponsorDiscount`). Character enters the Supporting Role ready. ([B5-0323], [B5-0335]) |
| 2 | **Promote a Character** | **Reachable** | Select a Character in Supporting Role &rarr; click **Promote** in toolbar. | Rotates an unrotated Inner Circle character to sponsor; pays promotion cost (`cost - sponsorDiscount`). Character moves from Supporting Role to Inner Circle. ([B5-0321]) |
| 3 | **Rotate to Build Influence** | **Reachable** | Click **Build Influence** in toolbar. | Rotates an unrotated Inner Circle character. Requires current Influence &le; 9. Adds +1 Influence rating. ([B5-0301]) *(See note on D9 pool proposal)* |
| 4 | **Pass Turn** | **Reachable** | Click **Pass Turn** in toolbar. | Forfeits action for current initiative cycle. Player un-passes if another player takes an action. ([B5-0341]) |
| 5 | **Initiate Conflict** | **Reachable** | Select Conflict card in hand &rarr; choose target in dropdown &rarr; click **Initiate Conflict**. | Enforces one-conflict-per-turn limit ([B5-0302]), target legality ([B5-0325]), and participation restrictions ([B5-0336]). Initiator commits to Support side. |
| 6 | **Support / Oppose Conflict** | **Reachable** | During an active conflict, click **Support** or **Oppose**. | Joins the active conflict on the selected side. Opponents/AI dynamically join based on strategic outcome scoring ([B5-0309], [B5-0343]). |
| 7 | **Play an Event** | **Reachable** | Select Event card in hand &rarr; click **Play Card**. | Triggers immediate effect via `CardEffects` registry (e.g. influence gain, card draw, stat boost) and discards to discard pile. ([B5-0307]) |
| 8 | **Play Enhancement / Location / Group** | **Reachable** | Select card in hand &rarr; click **Play Card**. | Attaches enhancement to valid target or places permanent Location/Group in player's faction zone. ([B5-0307]) |
| 9 | **Sponsor an Agenda** | **Reachable** | Select Agenda card in hand &rarr; click **Play Card**. | Places Agenda in player's Agenda slot. Win condition is continuously checked at round end ([B5-0305], [B5-0347]). |
| 10 | **Play an Aftermath** | **Readout (Auto)** | Hand highlights card with green `ELIGIBLE` badge. | Engine automatically evaluates and plays eligible aftermaths onto valid conflict participants post-resolution ([B5-0338], [B5-0361]). Manual human aftermath button is queued. |
| 11 | **Lead a Fleet** | **Engine Ready** | *(Pending dedicated UI button)* | Rotates an unrotated ready character with Leadership &gt; 0 to lead an unrotated fleet, adding character Leadership to fleet Military. ([B5-0337], [B5-0345]) |
| 12 | **Use Assistant Action** | **Engine Wired** | *(Automatic passive/engine boost)* | Assistant character rotates to provide +1 Diplomacy/Intrigue/Leadership or -1 recruit cost discount for the turn ([B5-0339]). Surfaced via `A+`/`A$` tags on Ambassador. |
| 13 | **Agenda Lifecycle (Discard / Replace / Reveal)** | **Partial** | Sponsor supported via Play Card. | Discarding minor agendas or replacing major agendas is designed in [B5-0345] Tier 1.3. |
| 14 | **Attack Conflict Participant** | **Unreachable** | *(Pending damage model)* | Requires the per-card damage, neutralization, and Strife mark subsystem ([B5-0345] Tier 3, [B5-0357]). |
| 15 | **Heal Character / Repair Fleet** | **Unreachable** | *(Pending damage model)* | Requires damage counter removal rules and influence spend mechanics ([B5-0345] Tier 3). |
| 16 | **Play Contingency Card** | **Unreachable** | *(Pending model class)* | Face-down placement under host cards and triggered reveals ([B5-0345] Tier 2). |
| 17 | **Mercenary Influence Bids** | **Unreachable** | *(Pending phase)* | Pre-resolution bidding phase for mercenary cards ([B5-0345] Tier 4, [B5-0355]). |

---

## 5. Mechanics Implementation & Task Reference Matrix

The following matrix documents the relationship between game features, rulebook sections, implementation status, and development task records:

| Feature / Mechanic | Rulebook Section | Status | Implemented Tasks & Design Links |
|--------------------|------------------|--------|----------------------------------|
| **Java 6 Clean Build Gate** | Governance | **Complete** | [B5-0001], [B5-0101], [B5-0102], [B5-0103], [B5-0306] |
| **Faction Starter Decks (60 cards)** | §I Setup | **Complete** | [B5-0319], [B5-0320] (`StarterDeckBuilder`, pinned ambassadors) |
| **Card Cost Data Backfill** | §IV Cards | **Complete** | [B5-0315] (schema), [B5-0334], [B5-0335] (210 cards, 377 entries) |
| **Deck-Out Penalty & Forfeiture** | §III / §V | **Complete** | [B5-0204] (D8: empty draw discards IC character or forfeits) |
| **One Conflict Per Turn Limit** | §IV / §V | **Complete** | [B5-0302] (`GameState.conflictsInitiatedThisTurn`, legal action guard) |
| **Conflict Sides & Resolution Rule** | §IV Conflicts | **Complete** | [B5-0309] (D14: initiator wins iff Support > Opposition; leader opposer wins on tie/greater) |
| **Conflict Participation Restrictions** | §IV Conflicts | **Complete** | [B5-0336] (`Participation.java` parser, quotas, targets), [B5-0352] (data) |
| **Fleet Leadership Mechanic** | §IV Fleets | **Complete** | [B5-0337] (D5: `FleetCard.leader`, rotate-to-lead, Military only from led fleets) |
| **Aftermath Targeting & Uniqueness** | §IV Aftermaths | **Complete** | [B5-0338] (D2: participant targeting, D4: one named aftermath per target) |
| **Assistant Mechanic & Boosts** | §IV Assistants | **Complete** | [B5-0339] (rotate for +1 stat boost or -1 sponsor discount, `A+`/`A$` tags) |
| **Station Entity & Victory Path** | §VI Victory | **Complete** | [B5-0340], [B5-0354] (`Babylon5Station`, Condition 2: station 20+ strictly leading) |
| **Card Effect Dispatch Registry** | §IV Effects | **Complete** | [B5-0307] (`CardEffects.java` ID-keyed effects, stat deltas, event triggers) |
| **UI Action Buttons & Cost Preview** | UI Playability | **Complete** | [B5-0326] (F3: Sponsor/Promote/Build buttons, F5: Cost preview readout) |
| **UI Split Play / Initiate & Targeting** | UI Playability | **Complete** | [B5-0325] (F1/F2: target selector, support/oppose), [B5-0328] (F4: button split) |
| **UI Initiative & Conflict Legend** | UI Playability | **Complete** | [B5-0327] (F8: initiative label), [B5-0329] (F9: conflict &rarr; ability legend) |
| **UI Grouped Narrative Game Log** | UI Playability | **Complete** | [B5-0331], [B5-0331a] (F12: round/phase visual grouping) |
| **UI Conflict Participation & Sides** | UI Playability | **Complete** | [B5-0316] (F7: sides banner), [B5-0346] (per-participant breakdown) |
| **UI Outcome Banner & Play Indicators** | UI Playability | **Complete** | [B5-0347] (outcome banner, agenda [WIN], attached aftermaths line) |
| **UI Hand Filtering, Sorting & Dimming** | UI Playability | **Complete** | [B5-0348] (9 type filters, 8 faction filters, cost sort, playable dimming) |
| **UI Eligible-Aftermath Hand Badge** | UI Playability | **Complete** | [B5-0361] (`ELIGIBLE` green badge verified against resolved conflict) |
| **AI Strategic Joining & Target Pick** | AI Subsystem | **Complete** | [B5-0343] (oppose-as-strategy, target selection against weaker opponents) |
| **AI Agenda, Aftermath & Event Scoring** | AI Subsystem | **Complete** | [B5-0344] (agenda proximity, aftermath hold, catch-up event scoring) |
| **AI Difficulty Contract Verification** | AI Subsystem | **Complete** | [B5-0351] (EASY ~53% pass bias, MEDIUM/HARD cost-aware determinism) |
| **Unlimited Actions Loop (D6)** | §III Action Round | **Proposal** | `docs/proposals/d6-unlimited-actions-design-proposal.md` ([B5-0341]) |
| **Applied Influence Pool (D9)** | §II / §V Economy | **Proposal** | `docs/proposals/d9-rating-vs-applied-influence-design-proposal.md` ([B5-0342]) |
| **Stat Bonus Layer & Expiry (D10/D11)** | §IV Modifiers | **Proposal** | `docs/proposals/d10-d11-bonus-layer-expiry-design-proposal.md` ([B5-0357]) |
| **War Conflict Participation Rules** | §IV Conflicts | **Proposal** | `docs/proposals/war-conflict-participation-rules.md` ([B5-0358]) |
| **Damage & Neutralization Subsystem** | §V Combat | **Queued** | [B5-0345] Tier 3 (counters, neutralization, heal/repair, attacks) |
| **Tiebreak Option C Reporting** | §VI Victory | **Harness** | [B5-0350] (`HeadlessTiebreakTest.java`, deterministic tiebreak chain) |

---

## 6. Victory Conditions Reference

1. **Standard Victory (Condition 1 — Rulebook §VI):**
   * A player wins at the end of a round if they have **&ge; 20 Power** and strictly more Power than any other non-forfeited player.
   * Ties prevent victory until broken.
   * Holding an unfulfilled Major Agenda blocks a player from winning a Standard Victory ([B5-0305]).
2. **Major Agenda Victory:**
   * A player holding a Major Agenda wins immediately when its specific card condition is met (e.g. *Cut Off the Food Supply*, *Shadow War*), regardless of Power score ([B5-0305]).
3. **Station Influence Victory (Condition 2 — Rulebook §VI / [B5-0340]):**
   * If Babylon 5 Station reaches **&ge; 20 Influence** at end of round (and no Shadow War is active), the eligible player strictly leading in Influence wins the game.
4. **Last Standing Victory:**
   * If all other players forfeit (e.g. through repeated deck-out penalties), the sole remaining player wins ([B5-0204]).

---

## 7. Playtesting Tips, Strategy & Known Quirks

* **Hand Management with Large Decks:** Starter decks quickly deal 4–10 cards. Use the **Filter / Sort** panel in the top toolbar to toggle off card types (e.g. uncheck `Fleet` or `Enh` when focusing on characters) or sort by `Cost↑` to prioritize affordable drops.
* **Affordability Indicators:** Look for the green dot on the top right of Character cards in your hand. If a card is grayed out / dimmed, your current Influence is insufficient to recruit or promote it.
* **Managing Influence Economy:**
  * Sponsoring cards from hand and promoting from Supporting Role consumes Influence.
  * *Known Quirk (D9 Conflation):* Currently, spending influence subtracts from your permanent Influence Rating rather than a restored per-turn applied pool. Use **Build Influence** early to keep your rating high until the [B5-0342] pool split is implemented.
* **Conflict Initiation & Joining Tactics:**
  * Select a Conflict card in hand &rarr; choose a target with lower matching stats in the dropdown &rarr; click **Initiate Conflict**.
  * Keep an eye on the **Sides Readout** in the center board (`supporters vs opposers`). Opposing AI players will dynamically commit to opposing you if they can defeat your total.
  * In Military conflicts, winning by &ge; 3 Military inflicts 1 damage on the losing initiator's ambassador ([B5-0309]).
* **Resolving Aftermaths:**
  * When a conflict resolves, check your hand for the green `ELIGIBLE` badge. Eligible aftermath cards are auto-processed post-conflict and attached to target player zones ([B5-0338], [B5-0361]).

---

## 8. Reporting Playtest Findings

When reporting bugs, edge-case rules interactions, or UI defects during playtests, please include:
1. **Round & Phase:** (e.g. *Round 3, Action Phase*).
2. **Action Sequence:** Exact buttons clicked or cards selected prior to the issue.
3. **Game Log Excerpt:** Copy relevant lines from the right-hand Game Log box.
4. **Expected vs Actual Behavior:** Reference the relevant rulebook section from `BABYLON5_CCG_RULEBOOK.md`.
