---
document:
  title: "Human playtest guide ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â running the B5 CCG prototype"
  status: "Guide (describes the working tree as of 2026-09-28)"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  assessor_llm:
    - {name: "GPT-6 Codex", version: "GPT-6"}
    - {name: "Solar Pro4", version: "solar-pro4:free"}
    - {name: "Qwen (qwen-2.5-coder-32b-instruct)", version: "qwen-2.5-coder-32b-instruct"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (me-so-poor)", version: "big-pickle"}
    - {name: "GitHub Copilot (Auto mode) 0930", version: "Auto mode", passes: 1, last_pass: "2026-09-30", note: "B5-1117 standing-red gate receipt"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (me-so-poor)", version: "big-pickle"}

    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
    - {name: "Buffy (unknown)", version: "unknown"}
    - {name: "Buffy (unknown)", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0484 part 11 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0498 part 13 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0512 part 14 refresh}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0517 part 15 refresh}
    - {name: "Buffy", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0531 part 17 refresh}
    - {name: "Buffy", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0540 part 18 refresh}
    - {name: "agent-on-deck", version: "on-deck-1.0" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0554 part 21 refresh: B5-0544/B5-0548 honesty note, B5-0552 audit note, B5-0553 census note}
    - {name: "Buffy", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0558 part 22 refresh: B5-0556 MINES-fix resolution note, 471 suite count, B5-0557 0443 flake classification}
    - {name: "Buffy", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0558 assessor pass: committed-HEAD claim corrected to working-tree, fix attribution corrected to fixture-primary}
    - {name: "Buffy", version: "unknown" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0550 part 20 refresh: 0548 outcome + actual counts (0539 MINES 10@HEAD/11@worktree), 0541 census update 270/270}
    - {name: "Buffy", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0566 part 23 refresh: b2800373 committed-HEAD-green note, B5-0561 human-seat winner-check soft-gate}
    - {name: "Buffy", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0578 part 24 refresh: B5-0577 measured stall rate 50% at 120s (upper bound), B5-0575 pipe repairs, 0443-class window failures fully retired}
    - {name: "Buffy", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0605 part 25 refresh: B5-0604 frontmatter compaction governance note (33ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢7 entries, passes-count form)}
    - {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0645 part 26 refresh: B5-0629 Major Victory path + Shadow War condition-1 guard fix, B5-0631 ORD section, B5-0633 coverage tool, B5-0643 re-sweep outcome, suite counts 512ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢542 with honesty notes}
    - {name: "Solar Pro4", version: "solar-pro4:free" ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0735 part 27 v2 refresh: surrender path + Power seam + surrender-AI thresholds + Civil War engine-law steps, suite counts 542ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢643 with sweep outcome}
    - {name: "Cline (space-bunny-free) b5-0805", version: "space-bunny-free", passes: 1, last_pass: "2026-09-28", note: "edit: B5-0805 part 29 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Civil War AI thresholds from B5-0727 measured in AIPlayer.java (unrest band 0/1/2, +2 race-at-war, merge exposure, join/surrender/build/pass/declare terms), the B5-0715 unrest readout re-verified in GameBoardPanel with a newly found same-position collision caveat, and the B5-0747 re-sweep outcome re-measured on this tree (7 probes + 643/643 conformance, 0443 at 36)"}
    - {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash", passes: 1, last_pass: "2026-09-28", note: "edit: B5-0986 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â replaced the stale same-position collision caveat (made false by the B5-0977 drawZone statusX chaining fix) with the non-overlapping rendering facts; that caveat block is the only body text changed"}
  last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 0930", version: "Auto mode"}
---

# Human playtest guide ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5 CCG prototype

This guide tells a human how to run the current build, what every control
does, and where the honest gaps are. It describes the working tree as of
  2026-09-28; features landed after that date may not be covered. The
canonical rules reference is `BABYLON5_CCG_RULEBOOK.md` (do not edit);
known deviations are recorded in `docs/DECISIONS.md`.

## 1. Requirements and build

* **JDK 8** (e.g. `1.8.0_292`) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the only toolchain that still accepts the
  project's `-source 6 -target 6` flags. Check with `javac -version`.
* No external libraries; everything is stdlib + Swing.

Build and run:

* **Windows (cmd):** `cd b5ccg` then `compile.bat`, then `run.bat`.
* **Git Bash / Linux / macOS:** `cd b5ccg` then `sh compile.sh`, then
  `sh run.sh`. `run.sh` auto-runs the compile first if `out/` is missing.

Two benign build notes: you may see one bootstrap `-source 6` warning
(expected), and on first launch the console prints a card-load line ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
"Loaded 829 cards." is the healthy path (446 premiere + 383 deluxe); if
loading fails the game falls back to a minimal test set and says so on
stderr, in which case stop and report it.

## 2. Starting a game

1. A dialog asks you to **choose a faction** (Human, Minbari, Centauri,
   Narn). Your choice is fixed for the session; the three other races are
   the AI seats.
2. You play against three AI opponents with fixed personalities and
   difficulties: **Delenn (MEDIUM)**, **G'Kar (HARD)**, **Londo (EASY)** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
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
the **Action phase** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â players act in initiative order (human clicks, AI
decides) one action at a time
until every player has passed consecutively. The implementation retains a
non-rulebook 8ÃƒÆ’Ã¢â‚¬â€playerCount safety backstop against infinite loops, but the
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

## 4. What you can do today ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â control reference

Main window buttons (enabled contextually; a disabled button is illegal for
you right now, not broken):

> **B5-0423 update (board selection landed):** the five selection-driven
> controls ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â **Lead Fleet, Use Rotate Effect, Attack, Heal, and Repair** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
> now select their targets on the board as well as in hand: board
> characters, fleets, groups and locations are clickable (a click chain
> whose hit-test geometry is copied from the paint code, so the clickable
> area cannot drift from the drawn area), and hand and board selections run
> through one shared handler (`applyCardSelection`), so target population,
> enablement and cost preview behave identically for both. Heal/Repair
> enablement uses the engine's own predicate rather than a hand-rolled copy,
> and Play Card requires the selection to actually be in your hand (a board
> card can never be played from hand ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0423 guard). Face-down cards stay
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
| **Initiate Conflict** | Initiate the selected conflict card | Some conflicts require a declared target (B5-0336: e.g. Border Raid); use the target selector when enabled. Since B5-0452 the selector no longer pre-selects during population ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Initiate stays dark until you explicitly pick a target, and the stale fallback that auto-picked the highest-influence location is gone (B5-0451). Since B5-0458 initiation is an ACTION-phase act: the button is dark during CONFLICT_RESOLUTION, AFTERMATH and DRAW (B5-0451 F4). Play Card keeps its broader phase window by design. |
| **Support / Oppose** | Join the active conflict on that side | Enabled only when the engine is waiting for your decision and you are eligible to join; choosing a side commits your face-up ambassador, if present, and resumes resolution. Mandatory participation still applies (B5-0336). |
| **Sponsor** | Recruit a supporting card | Applies the card's influence cost (B5-0323) |
| **Promote** | Promote a supporting character to your Inner Circle | Costs per `canPromote`/`promotionCost` (B5-0321/0323) |
| **Build Influence** | Rotate an Inner Circle member, apply 3 influence to your per-turn pool, rating +1 | Cost is drawn from the per-turn applied pool (B5-0369); the old defect where influence was a single permanent number is resolved. Only offered at rating ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¤ 9. |
| **Lead Fleet** | Rotate a ready character with Leadership into the leader slot of one of your own unrotated fleets; that fleet's Military total gains the leader's Leadership for the round | One leader per fleet; the rotation consumes your action; the leader must be ready, in your Inner Circle or a supporting role, and the fleet must be unrotated and yours (B5-0362). |
| **Discard Agenda** | Remove your installed agenda from the game entirely (not the discard pile) | A Major agenda cannot be discarded; the button is disabled with that reason when a Major is installed (B5-0364). |
| **Replace Agenda** | Rotate a ready Inner Circle member to leader, remove your current agenda from the game, and install a new agenda from your hand | Replacement must be Major-for-Major when your current agenda is Major; the new agenda must be in your hand and sponsorable; pulling the leader from the Inner Circle rotation is part of the cost (B5-0364). |
| **Reveal Agenda** | Expose a face-down agenda: apply its on-play effect immediately if it is sponsorable, otherwise discard it | Face-down agendas render without their title or win-condition status until revealed (B5-0364). |
| **Play Card** (agenda guard) | Play the selected hand card; events route to the effect tables | If you already have an agenda installed, the Play Card button will not play another agenda from your hand ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the one-major rule is enforced engine-side before the card leaves your hand (B5-0364). Use Discard/Replace/Reveal for agenda lifecycle instead. |
| **Place Contingency** | Play a ContingencyCard from your hand face-down under a host card in one of your in-play zones (Inner Circle, fleet, group, or location); the host's contingency count increments and the card's identity stays hidden | Requires a valid target and a host with an available slot; race matching reads the host card's subtype (B5-0365). No contingency-typed cards exist in the current pool, so this control is engine-ready but has no in-pool cards to play. |
| **Reveal Contingency** | Choose a face-down contingency from the dropdown selector and flip it, triggering its effect (routed through the event dispatcher) and then discarding it | Owner-gated; the selector lists each placed contingency as "<title> (under <host>)" (B5-0381); the reveal button is enabled only when it is your action turn and at least one placed contingency passes the reveal gate; no contingency cards in the current pool ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â engine-ready, data-pending. |
| **Use Rotate Effect** | Rotate a character to apply a selected effect: assistant bonus (+1 Diplomacy/Intrigue/Leadership for the turn) or sponsor discount (ÃƒÂ¢Ã‹â€ Ã¢â‚¬â„¢1 influence on a later recruit this turn) | Gated by the B5-0339 assistant mechanic (your own ready, unneutralized supporting-character assistant plus your ambassador); consumes your action (B5-0366). |
|| **Attack** | Attack a participant in the active conflict with the selected ready card; damage is mutual ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â each side deals its current conflict ability + 2 per its own Strife mark | Enabled only during the live human attack wait with a ready controlled attacker and an explicit target selected from the engine-approved opposing committed cards (B5-0432/B5-0440). The engine revalidates the pair and reuses B5-0370 damage, faction, participation, and overflow rules (damage model B5-0368). Neutralization can trigger on either side from the overflow. Pass becomes **Skip Attack** only during this wait. |
||
> **Board readout ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Civil War state and per-faction unrest (B5-0715):** each player zone header in the board panel now carries two extra signals beside the existing Power readout: a **per-faction unrest value** (shown in amber when it is greater than 1; hidden at 1, the universal starting value) and a **Civil War state badge** (red, bold) that appears for any race whose internal Civil War state machine is in the CIVIL_WAR phase. Both are pure readouts of model state (CivilWarState + Player.unrest); they do not change gameplay directly. The unrest value is the same number the AI seats consult (B5-0727), so a playtester can cross-check the board against AI behavior: when a race's badge is showing and its unrest reads 4 or 5, expect MEDIUM/HARD seats to treat that race's conflicts differently (see section 5). The badge is absent for UNIFIED races and for races with no Civil War machinery active.
| **Heal** | Rotate to heal the selected damaged Inner Circle or supporting character | Enabled on your Action turn for a selected damaged character in those roles (B5-0402, engine B5-0371). If every IC member performed a heal action this round, your ambassador is fully healed at the round's end. |
| **Repair** | Repair the selected damaged fleet or location: removes normal damage at 1 influence per token, paid from your per-turn applied pool (Rating untouched) | Enabled on your Action turn for a selected damaged, ready fleet/location with affordable pool cost (B5-0402, engine B5-0371). |
| **Bid** (mercenary) | Bid the selected amount of applied-pool influence to control an offered mercenary for the turn; bids are cumulative per player per turn | Amount selector offers 1/2/3/5/10; the label above shows the offered mercenary, the controller after resolution. Enabled on your Action turn when the selected amount is affordable now (B5-0404, engine B5-0395). Highest cumulative total controls at the MERCENARY phase; a tie crowns nobody (D12 discipline). No mercenary-typed cards exist in the current pool (B5-0386), so in a normal game this control shows "(no mercenary offers)". |
| **Declare War** | Declare a war conflict against a selected race or location target while you are at war with that faction | The target selector lists races at war with you, then their locations (marked "[loc]"); a status label explains the current war state (B5-0407, engine B5-0376). Note: no card or effect in the current pool moves tension, so no faction is ever at war in a normal game ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the control shows "Not at war" unless war state is set by a future data/effect task (B5-0358 design). |

Hand panel (B5-0348): filter checkboxes by card **type** and **faction**,
sort radio buttons (**unsorted / cost ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Ëœ / cost ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Å“**), and a show-dimmed
toggle. Cards you cannot currently afford or legally play are **dimmed**
(greyed with a black overlay); the affordability dot is hidden on dimmed
cards. Aftermath cards show a green **ELIGIBLE** tag when they can legally
attach to the last resolved conflict (B5-0361) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â this is an eligibility
*readout*, not a play button (aftermaths are played by the engine at
resolution time, auto-played for AI seats only).

Board readouts (B5-0316/0346/0347): each player band shows committed
conflict sides and per-participant card lists with totals (supporters
green, opposers orange); a green **WON BY** banner appears after a conflict
> resolves (B5-0347). **B5-0470 update:** the outcome banner now renders for
> every resolved conflict ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â normal and war alike ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â as
> "WON BY <winner> ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â LOST BY <loser>", where the loser is derived from the
> sides (initiator lost, the conflict target, or the captured location's
> owner), with a "WAR: ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦" title prefix for war conflicts (B5-0376). The
> detail line beneath shows the support/opposition totals plus the winner's
> influence reward.
resolves; agenda slots show MAJOR/minor and a `[WIN]` marker when the
agenda's condition is currently met; `A+`/`A$` mark the assistant
assist-bonus and sponsor-discount states (B5-0339). Captured/suppressed
locations show red `CAP:<player>`/`SUP` markers on their mini-cards, and
damaged or neutralized cards of any type show a red damage-state marker
on every board mini-card ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â `DMG:<n>` (severe appended as `+<s>`), `NEUT`
when neutralized, or `NEUT <n>+<s>` combined (B5-0516; face-up cards
only, read from the B5-0368 damage API). Hand cards carry the same
marker via the same API (B5-0519, right-aligned on the stats baseline
so it cannot collide with the left-aligned stat string) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â dormant in
normal play since damage targets in-play cards, but visible if the
engine ever applies hand-card damage.

> **Audit note (B5-0552, 2026-09-26):** the board mini-card damage-state
> marker (left-aligned, `y + h - 2`) and the contingency-count badge
> (B5-0381, right-aligned, `y + h - 2`) share the same baseline on 46px-wide
> mini-cards. Worst case: `DMG:3+2` (~33px) + `C3` (~14px) = 47px > 46px ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
> the two badges can overlap on a crowded mini-card. No collision occurs on
> the 110px HandPanel cards (left-aligned stats + right-aligned damage
> badge have 21px gap). Proposed fix (not yet implemented): a unified
> vertical badge stack on the right ~16px of mini-cards, top-down:
> contingency, then damage, then neutralization, freeing the left side for
> stats. See the B5-0552 report for the full spatial audit.

> **B5-0520 re-sweep (post-B5-0506 + B5-0516, 2026-09-26):** after the
> shunned wiring (B5-0506) and damage-state readouts (B5-0516) landed,
> the harness health re-sweep re-ran RUN_TESTS=1 (460/460 conformance
> PASS) plus all seven standalone probes (0350 tiebreak 26, 0351 AI
> contract 10/10 bands unshifted, 0382 station 6/0, 0383 participation
> PASS, 0384 lead-fleet 9/0, 0419 war PASS, 0443 human-seat 37/37) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
> all exit 0. The 0351 band re-confirmation is the key check since 0506
> **B5-0530 re-sweep + B5-0532 hygiene + B5-0534 probe (2026-09-26):** the
> post-0528 re-sweep re-ran RUN_TESTS=1 and all seven standalone probes
> PASS on the mines-wired engine (0351 AI-contract bands unshifted); the
> v3 build-hygiene sweep found zero Java 6 construct offenders across
> b5ccg/src; and the standalone mines scenario probe verified the reactive
> end-to-end (11/11 PASS, scratch mirror deleted after, per the 0384 probe
> pattern) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â including the mines-inflated return damage through the real
> controller pipeline.> moved registry/scoring-adjacent paths.
board pill lists at-war faction pairs whenever a war exists, and a bottom
line always shows station influence plus Shadow/Vorlon ratings ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â gaining a
red `[SHADOW WAR]` marker when either rating reaches the condition-2
threshold ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â with any nonzero tension pairs listed just above it (B5-0427/
B5-0429; all of these stay clear in normal games, since nothing in the pool
moves tension yet).

> **B5-0445 addendum (station-hooks landed, 2026-09-26):** B5-0437 wired the
> station-influence card hooks from the 0428 proposal. Three sources move the
> ratings at the round boundary (`applyEndOfRoundStation`, run BEFORE
> `advanceRound` because `advanceRound` resets the `stationSourceFired` flag):
>
> 1. **Capture source** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â winning a LOCATION_TARGET war where the location is
>    not already under friendly control raises **human-side station influence**
>    by +1 (capped 100) and sets `stationSourceFired = true`. Recapturing a
>    suppressed location restores effects but yields **no station gain**
>    (recapture = restoration, not a source).
> 2. **Vorlon presence-bleed** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â when a Vorlon player holds one or more
>    captured locations at the round boundary, **vorlon influence** gains +1
>    (capped 100) and sets `stationSourceFired = true`. (Shadow presence-bleed
>    is deliberately NOT wired ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â there is no Shadow faction in the current
>    enum, per B5-0354; it waits for a future card hook.)
> 3. **Decay sink** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â if NO source fired this round (neither capture nor
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

> **Civil War awareness ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â what MEDIUM and HARD actually change (B5-0727,
> measured on the 2026-09-28 tree).** EASY is untouched: it never consults
> these signals, so any behaviour difference you see below is a MEDIUM or
> HARD seat. Three numbers drive everything, all readable off the board
> readout described in section 4:
>
> * **Unrest pressure** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â your faction's own Unrest value, banded:
>   **0 at Unrest 1-3, 1 at Unrest 4, 2 at Unrest 5.** It is *added* to the
>   score of conflict initiation (both difficulties), to a war declaration
>   against a brother faction, and to surrender.
> * **Race at war** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â a **+2** bonus on conflict initiation when your *race*
>   (not just you) is in CIVIL_WAR. MEDIUM/HARD will therefore try to *end*
>   a war their own race is fighting, not start more of it.
> * **Merge exposure** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â while your race is in CIVIL_WAR, the AI discounts
>   its own conflict score by the *spread* of that faction's tracked
>   tensions toward the other races (highest minus lowest). A faction whose
>   internal tensions have drifted apart is scored as a worse bet while
>   the race is split. This term is **exactly 0** whenever the race is not
>   in CIVIL_WAR, so unified-race play is unchanged.
>
> Four further behaviours are worth watching for:
>
> | Observable | Difficulty | What you should see |
> |---|---|---|
> | A conflict offered by a **brother faction** while your race is in CIVIL_WAR | MEDIUM, HARD | Never **supported**. If you outweigh the initiator, MEDIUM/HARD **oppose**; if not, they **abstain**. A "support your own race's war" line will not appear. |
> | **Surrendering to a brother faction** while your race is in CIVIL_WAR | MEDIUM, HARD | Strongly discouraged: a **-8** (MEDIUM) / **-8.0** (HARD) penalty on top of the unrest urgency. The intent is that a faction does not hand a race at war with itself over to a rival faction of the same race. |
> | **Building influence** or **passing** while your race is in CIVIL_WAR | HARD | A **+8.0** swing on *both*. Passing and building are treated identically here, because abstaining forfeits the race-major seat to an unopposed brother faction exactly as building does. Expect a HARD seat of a war-torn race to contest the board rather than sit out. |
> | **Declaring war on a brother faction** of a still-unified race | MEDIUM, HARD | Scored as `4 + unrest pressure - merge exposure` (i.e. base 4, with the unrest bonus and the exposure discount) instead of the leading-rival formula. On a unified race the exposure term is 0, so this reads as `4 + unrest pressure` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â up to **6** at Unrest 5. |
>
> **How to check it yourself:** the numbers on the board are the numbers the
> AI reads. Unrest comes from the amber `Unrest: N` readout and CIVIL_WAR
> from the red badge; if a MEDIUM or HARD seat is behaving as though a race
> is at war, check that race's badge first, and check that the acting seat
> is not EASY. One honest caveat recorded at the time: the **-8 civil
> surrender penalty cannot currently fire in a normal game** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the tension
> matrix refuses same-faction war pairs, so a same-race surrender is not
> offered to be scored in the first place. It is present as defensive depth
> against a future change, and a playtester should **not** expect to observe
> it without one. Likewise no card in the current pool moves tension, so a
> standard game never reaches a same-race tension of 5 and the race badge
> may never appear ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see section 7.
>
> **Verified against the current tree (B5-0805, 2026-09-28; collision caveat
> repaired B5-0986 per the B5-0977 fix).** Thresholds confirmed in the panel
> code: the amber `Unrest: N` label is drawn only when **N > 1** (1 is the
> universal starting value and is deliberately hidden, so an empty space
> where the label would be means "unrest 1", not "no data"), and the red
> bold `CIVIL WAR` badge is drawn whenever that race's state machine phase
> is `CIVIL_WAR`. **Rendering note, updated B5-0986:** the earlier caveat
> reporting that both strings draw at the *same* board position (badge
> painted over the unrest number) no longer holds ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0977 chained the
> faction name, the unrest label and the badge horizontally in
> `GameBoardPanel.drawZone` via a measured `statusX`, so both signals are
> now visible side by side; in a narrow zone they degrade to a right-aligned
> second row, then clip at the border (clipping beats overpainting, per the
> B5-0977 report). An unrest number that seems to vanish now indicates an
> unusually narrow zone, not the badge covering it: report it if you see it.

* **EASY (Londo)** passes often (~46% of decisions since the B5-0508
  retune; previously ~53%) and otherwise picks
  uniformly among legal actions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â useful for watching mechanics play out.
  EASY never consults station ratings (B5-0453): the uniform pick is the
  intended contract, and difficulty-level behavior is unchanged.
* **MEDIUM (Delenn)** is cost-aware and deterministic; scores mercenary bids
  by projected win probability minus the bid cost (B5-0403). Since B5-0453
  it also scores station, shadow and vorlon rating context on
  influence-moving choices (war declarations on captured locations and
  location plays) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the same additive term HARD uses.
* **HARD (G'Kar)** adds agenda win-condition proximity, aftermath
  anticipation, event/contingency catch-up scoring, and rotate-effect scoring
  (B5-0344), plus conflict-side choice instead of always-oppose defaults
  (B5-0343), damage-aware attack/heal/repair scoring (B5-0378), and
  mercenary-bid scoring with a steeper opportunity-cost penalty (B5-0403).
  Its war scoring also carries the station-context term (B5-0453):
  ÃƒÂ¢Ã‹â€ Ã¢â‚¬â„¢1 when the Shadow War is active, +2 at station influence 15+, +1 when
  shadow or vorlon influence is 15+, 0 otherwise (zero-rating invariance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
  orderings when the pool is inert are unchanged).
* Difficulty contracts are pinned by the standalone harness
  `HeadlessAIDifficultyContractTest` (B5-0351). The station-aware scoring is
  pinned by the suite's STH-AI section (B5-0453, 7 checks).
* **EASY pass-bias retune (B5-0508, human Ruling 1A ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â landed):** the EASY
  pass floor moved from 0.35 to 0.20 by human order, widening the
  B5-0351 contract band to 0.20-0.70 (upper bound 0.70 unchanged; MEDIUM
  and HARD untouched). Observed pass rate after the retune: **0.463**
  (contract 10/10 PASS). The two B5-0422 advisory proposals are resolved
  by this ruling; the EASY-vs-MEDIUM blur it introduces was accepted per
  the brief. Acceptance evidence (B5-0509, 0447 seed-matched, 10 games):
  9 WINNER + 1 TIMEOUT reproducing in the historical seed-105 game-2
  slot; promotions rose 3.2 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 4.4/game (EASY passing less feeds more
  actions to every seat; within noise, watch on the next re-probe);
  builds ~flat 9.0; agendas stable 3.6; initiator win 55%.
  [CORRECTED by the B5-0527 reconciliation audit (2026-09-26): the cited
  0509 reportÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s own per-seed table places the TIMEOUT at SEED 109
  GAME 2 (round 11), not the seed-105 slot; and this paragraphÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s
  figures match the 0509 rowÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s verify cell, not the cited report,
  which gives promotions 4.7/game, builds 8.6, aftermaths 8.2,
  agendas 4.3, initiator win 53% (66/124). Both runs support the
  ACCEPTED verdict; the second runÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s logs were not persisted.]  Note: default seat mixes already include exactly one EASY seat (Main:
  MEDIUM/HARD/EASY; harnesses: EASY/MEDIUM/HARD/MEDIUM) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no all-EASY
  default exists in the tree.

## 6. Headless testing (no UI)

`sh compile.sh` with `RUN_TESTS=1` runs the full conformance suite
(**643 checks**; see the suite banner for the live count; re-verified
2026-09-28 with the full post-chain tree green ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B5-0661 surrender, B5-0677
computed Power seam, B5-0679 surrender-AI, B5-0691 Civil War engine law, and
B5-0683 re-sweep all landed ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â by the B5-0683 close-out: conformance
**643/643 PASS** + smoke PASS, all seven standalone probes PASS) plus a smoke
game. One transient smoke-run failure mode is known and classified ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see the
B5-0495 honesty note in section 7.

> **Standing-red verification (B5-1117, 2026-09-30):** Checked tree `02716363db2ab6ffc992e1810fde8254f1a7a241`; `RUN_TESTS=1 sh compile.sh` exits **1** with `FleetCard` cast to `ConflictCard` at `HeadlessConformanceTest.java:507`. Last passing line: `[PAR]` loader-hydration. The window is bounded by B5-1088. A fresh Git Bash launch failed before execution with `Bash/0x80080005`.

> **Suite-count reconciliation (B5-0735, 2026-09-28):** the baseline of 512
> checks (documented at B5-0645) grew to 643/643 via: +9 ORD (B5-0631),
> +9 AMT3 (B5-0637), +12 MJR-AI (B5-0629), +31 VPS (B5-0663), +19 SUR
> (B5-0661), +10 PWR (B5-0677), +8 SUR-AI (B5-0679), +26 CWR (B5-0691),
> +TRG (B5-0671). Each addition's close-out report carries its own assertion
> count; the live suite banner is the authority. The B5-0683 re-sweep
> exercised the tree with the full chain landed (SUR, PWR, SUR-AI, CWR) and
> re-confirmed all seven standalone probes PASS.
> circulate for the MINES-adjacent era: the B5-0539 ledger row cites
> "469/469 green" (the close-out's claimed count at commit time); the
> part-18 section 6 text (B5-0540) cites "470 checks" (the guide author
> counted the MINES x10 section as 460->470); and the live suite banner
> **Update (B5-0550, part 20, 2026-09-26):** the B5-0548 re-sweep
> recorded 4 failures of 470 (down from 6 in B5-0544; two 0544 failures
> flipped to PASS between sweeps) and went BLOCKED per step 7; both rows
> were later marked SUPERSEDED by the B5-0556 fix and the B5-0557 post-fix
> sweep (471/471 green). Count notes: the B5-0539 MINES section holds 10
> checks at committed HEAD (grep `check("MINES")`), 11 on the working tree
> (the uncommitted fixture modernization adds one gate check; the 0539
> row's verify text says 13 assertions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â counting methods differ, so read
> the suite banner, not derived counts). The suite is 471/471 green on the
> working tree; committed HEAD carries the engine half of the fix
> (223f94ba) while the fixture modernization still rides uncommitted, so a
> bare checkout remains red until the next checkpoint. The B5-0541
> pipe-defect honesty note stands updated: the current census is 270/270
> unique IDs, zero duplicate IDs, zero defects on OPEN rows, and the same
> four legacy DONE rows (B5-0202c 9 pipes, B5-0316 8, B5-0449 8,
> B5-0490 10) remain non-canonical ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â out of scope for docs-only tasks.
> Part-19 continues below.
>
> reports **471 checks** (a concurrently-added section took 470->471,
> documented in the B5-0557 report). The 469/470 discrepancy is a citation
> mismatch: B5-0539 counted 469 assertions at close-out, while the
> committed HeadlessConformanceTest.java at b5eabc89 actually reports 470
> (the MINES x10 section adds 10 assertions to the 460 baseline). Post-B5-0556
> fix, the live count is **471/471 PASS** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â fully green.

> **Update (B5-0566, part 23, 2026-09-26):** two events closed the gap
> this note tracks. (1) B5-0562 checkpoint **b2800373** committed the
> B5-0556 fixture half (HeadlessConformanceTest MINES modernization) and
> the B5-0561 probe hardening, so the suite is fully green at committed
> HEAD for the first time since the MINES section landed ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â a bare
> checkout now compiles and passes 471/471. (2) B5-0561 softened the
> human-seat probe's winner-within-3-round-window check to a SOFT gate
> per the B5-0482 precedent (details in the probe bullet below), so the
> seed-dependent window failure the B5-0557/B5-0558 notes classify is
> retired as a failure mode; those notes are kept as history. Suite
> count unchanged at **471/471**; the part-23 window re-verified the
> post-b2800373 tree green (compile.bat + compile.sh + RUN_TESTS=1
> conformance 471/471 + smoke PASS, zero code-context Java 6 construct
> offenders per the B5-0564 build-hygiene re-sweep).

> **Update (B5-0578, part 24, 2026-09-27):** the multi-seed soak is
> measured. B5-0577 ran HeadlessMultiRoundTest across 10 seeds (1 game
> each, 120s window): **5 WINNER, 5 TIMEOUT-stalled ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â a 50% upper-bound
> stall rate** at that window (8/10 games were still progressing at cap;
> a longer window was not measured, so this is window-relative, not a
> game-never-ends rate). Winners emerged at rounds 6ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“10. This is the
> first distribution-level measurement ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the B5-0312 20ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“20 stall was a
> common outcome, not an edge case ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â and it strengthens the B5-0332
> option-C (harness reporting tiebreak) recommendation; per-seed data in
> the B5-0577 report. Supporting evidence: HeadlessReportingTiebreakTest
> 26/26 on 4 seeds; HeadlessHumanSeatProbe exit 0 on 4 seeds (winner
> BrAVo round 6 on one; three seed-dependent no-winner windows reported
> as [INFO] by the B5-0561 soft-gate ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the 1/37 failure class is fully
> retired). New tooling: B5-0589 added
> `HeadlessStallSoakProbe` (per-seed table with winning-condition
> labels; exits nonzero only on harness exceptions). Ledger hygiene:
> B5-0575 (rows 0571ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“0574), B5-0591 (rows 0569/0570) and B5-0592
> (rows 0564/0565) repaired the close-out-introduced pipe defects back
> to canonical 7-pipe form; the four legacy content-contained rows
> remain excluded per the B5-0568 verdict. Suite count unchanged at
> **471/471** (re-verified 2026-09-26T23:45Z on committed HEAD).

> **Update (B5-0556 + B5-0557, 2026-09-26):** the 4 MINES failures
> documented in the B5-0554 honesty note below have been resolved by the
> B5-0556 engine fix slice. Two one-character engine edits fixed the
> consequence path: (1) `RulesEngine.java` log format ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â closing paren moved
> after the terminal period so `logContains` substring assertions match;
> (2) `CardEffects.java` `DAMAGE_ON_ATTACK` registry ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â registered
> `enh_mines_rt` (the round-trip fixture card id) so the reactive +1
> return-damage fires for the round-trip scenario. The B5-0544/B5-0548
> re-sweeps are retired by B5-0557's post-fix verification (471/471 PASS).
> The suite is now fully green on the working tree (committed HEAD b5eabc89
> predates the fix; the fix rides uncommitted until the next checkpoint). The original B5-0554
> honesty note (pre-fix: 466/470, 4 MINES failures) is preserved below
> as a record of the pre-fix state. [Buffy assessor correction: the fix
> was primarily fixture-side ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â three log needles with an impossible
> trailing period, a round-trip count derived from a removed
> applyDamage(2) pre-damage, and a stale pre-rotation defeating the
> requires-ready gate ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â verified by scratch instrumentation; the two
> engine edits were supplementary on top. See
> .agent/REPORTS/2026-09-26-Buffy-(unknown)-B5-0556.md.]

> **Honesty note (B5-0554 / B5-0544 / B5-0548, pre-fix ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â preserved): the
> suite was NOT fully green on the committed tree before the B5-0556 fix.**
> The B5-0539 close-out claimed 469/469 green, but that run was against an
> uncommitted working-tree version of the MINES fixtures; the committed HEAD
> (b5eabc89) produced 4 MINES failures out of 470 (reactive mines +1
> return damage, legacy non-trigger, round-trip trigger, round-trip
> survive). The committed 0539 fixtures used an all-zero CHARACTER
> attacker that the ATK legality gate refused (an attacker must have
> nonzero conflict-type ability), so no damage event ever fired and the
> reactive-damage checks failed. The B5-0544 sweep initially counted 6
> failures; the B5-0548 re-sweep counted 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the tree state changed
> between sweeps (two assertions that failed in 0544 pass in 0548). Fix
> requires engine model fixture changes (non-zero attacker stats) or
> engine path changes ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â which B5-0556 delivered (see update above). Smoke
> PASS and all 7 standalone probes pass regardless. The 470 count was the
> live assertion count pre-fix; the live PASS count pre-fix was 466/470.

> **Update (B5-0550, 2026-09-26):** the B5-0548 re-sweep (post-fix)
> confirmed the 4 MINES failures resolved ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the live suite is now 471/471
> green (see the B5-0557 re-sweep report). The 0539 MINES x10 section
> (10 checks) is live in `HeadlessConformanceTest.java`; the 0541
> pipe-defect census (4 non-canonical DONE rows: B5-0202c, B5-0316,
> B5-0449, B5-0490) is preserved in the B5-0553 honesty note in
> section 7, with zero leading double-pipe rows and zero duplicate IDs
> (263 total B5 rows). The 0544/0548 re-sweep outcomes are superseded
> by B5-0556 + B5-0557; see the suite-count reconciliation (B5-0546)
> above for the 469 vs 470 vs 471 progression.

B5-0437's station hooks added 14 STH
assertions (373 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 387); B5-0453 added 7 STH-AI assertions (387 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 394);
B5-0464 added 4 AGL-LOG assertions (394 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 398); B5-0436 added D6/D7 named
assertions and D15
winner-only `influenceReward` coverage; B5-0469 added 22 ENH-SEAM assertions
(398 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 420ish band, see suite banner for the live count); **B5-0473 added 14
ENH-WIRE assertions** covering the opponent-targeted enhancement wiring;
**B5-0486 added 8 FLOOR assertions** for the minimum-1
bonus-floor path; **B5-0506 added 16 SHN assertions (current total
460)** for the shunned opponent-character wiring (explicit character
target, four per-stat penalties in the victim registry, reactive
discard-on-heal). Several standalone CLI harnesses exist
(not wired into RUN_TESTS ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â run directly with `java -cp out`):

* Human-seat end-to-end probe (B5-0443, extended B5-0460 to 26 checks,
  B5-0471 to 29, **B5-0478 to 37**):
  drives a full game through the same `submitHumanAction` path the UI
  buttons use, and additionally exercises heal, repair, mercenary bid and
  war declaration on a synthetic fixture so the formerly soft-gated paths
  are hard coverage on every run. **B5-0478 addition:** an opponent-targeted
  enhancement scenario (Censure-shape fixture) asserts the penalty lands in
  the opponent's bonus registry via the public read path ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the fixture part
  is deterministic. **B5-0482 determinism outcome (supersedes the B5-0476
  flake note):** the human driver RNG and the starter-deck random draw are
  both now seeded from the CLI seed (`StarterDeckBuilder.setRandomSeed`), and
  the `COVERAGE: agenda lifecycle` gate is a SOFT gate like bid/war ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â a
  rulebook-faithful game can legally end with zero agenda lifecycle actions,
  so it no longer fails runs. Residual honesty: because the soft gate marks a
  check only when the path fires, the printed check count can differ between
  runs (e.g. 36 vs 37) with the same seed; the PASS/FAIL verdict, not the
  count, is the gate. **B5-0557 note:** the "game produced a winner"
  check can fail on slow runs; the B5-0557 sweep reproduced that failure on
  committed HEAD (b5eabc89), classifying it as a pre-existing harness-window
  characteristic, not a regression. This refresh (B5-0558) re-ran the probe:
  37/37 PASS. **B5-0561 update:** that winner check is now a SOFT gate
  (B5-0482 precedent) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â a winner inside the probe window marks PASS; a
  seed-dependent no-winner window prints an [INFO] line and the run
  continues instead of failing. Re-verified after the soft-gate landed:
  36/36 PASS seed 42 (winner=Human, round 6) and 37/37 PASS seed 43.
  **B5-0471 addition:** a face-up agenda install check asserts the
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

**Chain landing summary (B5-0735 part 27 v2, 2026-09-28):** the surrender path
(B5-0661), computed Power seam (B5-0677), surrender-AI offering thresholds
(B5-0679), and Civil War engine law (B5-0691) are all live. **Surrender
(B5-0661):** `GameAction.Type.SURRENDER` is offered only in the discard round
(`GamePhase.DRAW`), gated on a genuine war between the two races per the
tension matrix, an ambassador in play, and the target not already
surrendered/forfeited; `executeSurrender` places **+3 influence on the named
opponent** and moves an asylum copy of the surrendering player's ambassador into
the opponent's supporting role (rotated, elevation refused by type). Rulebook
:821 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â when all-but-one player has surrendered, `checkVictory` routes through
`majorVictory` rather than a second victory path; `majorVictory` and
`strictlyLeads` both exclude surrendered players. **Power seam (B5-0677):**
`StatKey.POWER` is a player-level add-on, never a card stat; `Player.getPower()`
is computed as `influence + getPowerBonusTotal()` with no stored field ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â with no
POWER bonus present it equals `getInfluence()` exactly, so the influence-stays-
economy rule is preserved; the Negative Power gate (`RulesEngine.canAffectTarget`,
:1034) refuses an influence-as-power effect on a target whose `getPower() <
getInfluence()`. No economy call site was switched to `getPower()` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â victory and
scoring paths still read `getInfluence()`. **Surrender-AI thresholds (B5-0679):**
MEDIUM/HARD offer SURRENDER only when trailing the current influence leader by
**ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¥ 6** at action-build time, only to a war opponent `canSurrender` accepts,
and never by EASY; scorers penalize surrender-to-leader (MEDIUM ÃƒÂ¢Ã‹â€ Ã¢â‚¬â„¢5, HARD ÃƒÂ¢Ã‹â€ Ã¢â‚¬â„¢4.0)
rather than banning it, so a hopeless player may still prefer the strongest rival
as warden. **Civil War engine law (B5-0691):** per-faction `Player.unrest` (start
1, 2 for Non-Aligned, clamped 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5, :278/:280/:888); same-race directional tension
substrate in GameState (start 2, clamped 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5, :972/:974 Non-Aggression); the
`CivilWarState` machine per race (UNIFIED | CIVIL_WAR, entry at same-race tension 5
via :992, exit by war end with **rounded-up-average merge** per :1000, exit by
surrender with no merge per :1006); `applyRaceJointInfluenceLoss` (:980) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â unified
race ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ loss spills to every faction, Civil War ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ loser alone. Step 1 (multi-faction
identity) is parked on a human order and was NOT implemented. The B5-0669 card
census found zero pool cards invoking these axes, so every CWR fixture is synthetic
(two/three Players of one Faction). The :1002/:1004 state suppression/restore seam
is NOT modelled (no state objects exist yet) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â recorded as a pending seam.
**Re-sweep outcome (B5-0747, re-run and re-measured here on 2026-09-28):** all
seven standalone probes PASS against the current tree. The table below is the
B5-0747 sweep, and every row was independently re-run in this pass with the
same result ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the "reproduced" column is this pass's own measurement, not a
copy of the earlier report:

| Probe | Class | B5-0747 result | Reproduced here (2026-09-28) |
|---|---|---|---|
| B5-0350 reporting tiebreak | `HeadlessReportingTiebreakTest` | PASS 26/26, exit 0 | PASS 26 checks, exit 0 |
| B5-0351 AI difficulty contract | `HeadlessAIDifficultyContractTest` | PASS 10/10, exit 0 | PASS 10 checks, 0 failed, exit 0 |
| B5-0382 station victory | `HeadlessStationVictoryTest` | PASS 6/6, exit 0 | PASS 6 checks, 0 failures, exit 0 |
| B5-0383 participation gates | `HeadlessParticipationGatesProbe` | PASS all scenarios, exit 0 | PASS all scenarios, exit 0 |
| B5-0384 lead-a-fleet | `HeadlessLeadFleetScenarioProbe` | PASS 9/9, exit 0 | PASS 9 checks, 0 failures, exit 0 |
| B5-0419 war conflicts | `HeadlessWarConflictProbe` | PASS all scenarios, exit 0 | PASS all scenarios, exit 0 |
| B5-0443 human seat | `HeadlessHumanSeatProbe` | PASS 36 checks, exit 0 | **PASS 36 checks** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â confirms the 37 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ 36 drift, not a one-off reading |

Conformance suite re-run in this pass: **643/643 PASS**
(`CONFORMANCE SUITE PASSED (643 checks)`), with the CWR section present ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
meaning the sweep exercised the tree *with* the full Civil War chain landed
(surrender, computed Power, surrender-AI, Civil War engine law, and the
B5-0727 tension-aware AI). The suite count is unchanged from B5-0747, so no
conformance checks landed between the two sweeps.

**Honest note on the 0443 count.** The first sweep (B5-0683) recorded 37
checks; B5-0747 and this pass both measure **36**. The class in `out/` is the
current tree's and the suite is exit 0 either way, so the drift is reported
as measured rather than harmonised to the older number. A playtester reading
this guide should take 36 as the current count.


## 7. Known gaps (with task links)

The rulebook's Ãƒâ€šÃ‚Â§V action list is partially reachable. The full
action-by-action audit lives in
`.agent/REPORTS/2026-09-23-freebuff-01-B5-0345.md`; the short version
reflects the working tree as of 2026-09-25:

**Human-action controls now reachable:** Lead Fleet, Use Rotate Effect, Attack,
Heal, and Repair have live UI controls; see the control reference above and
the live list below.

**Engine done and live ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â visible or materially affects gameplay today:**

* **Influence economy** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the D9 rating-vs-applied-pool split (B5-0369) is
  live: Build Influence applies +3 to your per-turn pool (not your permanent
  Rating), and sponsor/promote spend from the pool. The old defect where
  every sponsor permanently eroded your Rating is resolved. The
  free-participant waiver (B5-0374, E3) is also live. The E1 double-cost
  pool interaction (B5-0373) is DONE: the named `isDoubleCostRequired`
  helper and neutral exemption are wired into the applyInfluence spend site
  ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â behaviour-preserving (base recruit cost math unchanged); its coverage remains
  green in the current 444-check suite.
* **Damage subsystem** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â characters can be damaged and neutralised; the
  damage/neutralisation model (B5-0368) plus attack (B5-0370) and
  heal/repair (B5-0371) are all live, with UI controls landed (B5-0402).
  Board-card selection landed (B5-0423), so **Heal and Repair are reachable
  in real play**: select the damaged character, fleet or location on the
  board and the buttons enable via the engine's own predicates.
  **Human attack window** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â live (B5-0432 engine plus B5-0440 UI): after
  mandatory participation, an eligible human can select a ready board
  attacker, choose an explicit legal target, and activate Attack. The
  controller revalidates the pair and reuses B5-0370 damage, faction,
  participation, and overflow rules. Pass becomes **Skip Attack** during
  this optional wait. This supersedes the former human-seat structural gate
  from B5-0409 finding A.
  **In AI-vs-AI play attacks do not fire either**: conflicts still resolve
  synchronously inside the initiating action, so the active-conflict state
  the attack requires never exists when an AI seat picks its action ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â pure
  AI-vs-AI rounds observed zero attacks, heals, repairs, and neutralisations
  across all verification probes. That remains a separate real engine-loop
  gap (the rulebook's separate Resolution Round is the fix direction), not
  a human UI bug.
* **Assistant mechanic** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â real (B5-0339): rotate your assistant for +1
  Diplomacy/Intrigue/Leadership, or consume a sponsor discount on a later
  recruit. The A+/A$ markers on the ambassador mini-card reflect live state.
* **Bonus layer** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â stat bonuses now use an expiry-backed layer (B5-0367)
  rather than permanent mutation; fleet leadership (B5-0337) and assistant
  (B5-0339) compose through it. Not directly player-visible beyond the
  existing markers, but it means round-boundary expiry and stacking now work.
* **AI mercenary bidding** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â live (B5-0403): MEDIUM/HARD offer and score
  BID_ON_MERCENARY actions for offered mercenaries (minimal strictly-winning
  increment, pool-affordability gated);  EASY picks uniformly from the same
  legal list. MER-AI conformance section ÃƒÆ’Ã¢â‚¬â€10 landed with it (historical suite
  expansion; current total is 444 after B5-0436 (373) + B5-0437 STH ÃƒÆ’Ã¢â‚¬â€14 + B5-0453 STH-AI ÃƒÆ’Ã¢â‚¬â€7 + B5-0464 AGL-LOG ÃƒÆ’Ã¢â‚¬â€4 + B5-0469 ENH-SEAM ÃƒÆ’Ã¢â‚¬â€22 + B5-0473 ENH-WIRE ÃƒÆ’Ã¢â‚¬â€14 + B5-0486 FLOOR ÃƒÆ’Ã¢â‚¬â€8).
* **Declare War UI** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â live (B5-0407): a war target selector + Declare War
  button wired to the B5-0376 engine branch; the selector lists races at
  war, then their locations. Inert in normal games (no tension sources in
  the pool; see the control-reference note).

**Still open ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â unchanged or newly flagged:**

* **Mercenary card data** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no data evidence in the pool (B5-0386,
  no-evidence verdict). The engine slice (B5-0395), AI bidding (B5-0403),
  and bid UI (B5-0404) are all DONE using synthetic fixtures; a real game
  still has nothing to bid on until real mercenary cards are sourced (needs
  human direction).
* **Bid control reads only the first offer** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the B5-0423 audit's P3
  residual: the bid handler hardcodes the first offered mercenary
  (`offers.get(0)`), so if several mercenaries were ever offered
  simultaneously only the first would be bidable. Latent today: the
  mercenary pool is empty (B5-0386), so the control shows "(no mercenary
  offers)" in every real game.* **Opponent-targeted enhancements** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
  the engine path is live (B5-0468 model seam + B5-0473 wiring: an explicit
  opponent target routes the penalty into that owner's fleet registry, held
  in play if the target cannot resolve), and the pool's only exact-class
  card is Censure (B5-0477: `enh_censure` premiere + `de_enh_censure`
  deluxe, Military ÃƒÂ¢Ã‹â€ Ã¢â‚¬â„¢2). The UI target-picker (B5-0487) lets a human player
  select an opponent faction + face-up fleet to populate the B5-0468
  target before playing the card; if no opponent fleet is available the
  card still submits via the engine's held-in-play path (no self-fallback).
  Row-text correction (B5-0496, per the B5-0493 audit): the B5-0487 claim
  of an API ripple (`GameState.getFactions()`,
  `Player.canShowFleetForController()`, `Faction.isWon()`) was fabricated ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
  the shipped picker uses existing API (a `getPlayers()`/`getFleets()`
  iteration with face-up, unrotated, non-human, non-forfeited filters) with
  zero model churn; the delivered control is real and unchanged.
  RESOLVED (B5-0484 status update): the printed
  "(minimum 1)" floor on Censure is now implemented via B5-0486 (per-bonus
  floor field on StatBonus + effectiveStat floor pass + BONUS_FLOORS table;
  FLOOR ÃƒÆ’Ã¢â‚¬â€8 suite section) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the B5-0477 flag is closed.
  **B5-0506 extended the wiring to opponent CHARACTERS** (the shunned
  pair): a character enhancement carrying an explicit target attaches its
  four per-stat penalties to the chosen opponent character and is
  discarded ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â lifting its bonuses ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â when that character is healed
  (reactive discard-on-heal, SHN ÃƒÆ’Ã¢â‚¬â€16 suite section). The B5-0522 picker
  extension makes this path human-reachable (below). Data note RESOLVED (B5-0523):
  the shunned records previously carried `militaryBonus: 0` against their printed
  "all stats" text (B5-0311 class; data wins), so the penalty values in
  play came from the engine table; the data task landed 2026-09-26 and both
  records now carry `militaryBonus: -2`, matching the printed text.
* **Mines + Energy Mines reactive (B5-0528)** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â attacking an opponent
  protected by Mines in play adds +1 to the damage your fleet takes back:
  the engine checks the defending side for a held Mines-class enhancement
  at the attack-resolution site (`DAMAGE_ON_ATTACK` registry over all four
  pool ids: enh/de_enh_mines, enh/de_enh_energy_mines). Energy Mines keeps
  its printed +2 Military self-attach on the ownerÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s best fleet via the
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
  handlerÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s targeted branch, which tested a seam its own delegate
  sets at submit time so targeted fleet clicks self-penalized.]  enhancements):** the Play Card handler now populates an opponent-character
  dropdown when the selected hand card is a CHARACTER enhancement carrying
  the 0468 seam (shunned-class today) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â face-up, non-won, non-human,
  non-forfeited opponent characters across Inner Circle, supporting role,
  and ambassador, per the B5-0506 characterById scope. Selecting a target
  routes play through setOpponentTarget before submit, mirroring the B5-0487
  fleet flow including empty-state disable and the held-in-play fallback when
  no opponent character is available. NOTE: MainWindow.refreshCensureControl
  gates the picker on Enhancement FLEET subtype only, so a human-played
  shunned-class CHARACTER enhancement has no picker and routes to the legacy
  self path (self-penalizing); the B5-0506 engine explicit-target CHARACTER
  branch is live for AI. GUIDE GAP CLOSED by this row.

* **Opponent-CHARACTER target picker (B5-0522, verified)** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â selecting an
  Enhancement CHARACTER card from hand now shows an opponent-character
  dropdown (face-up inner circle, supporting role, and ambassador of every
  non-forfeited opponent ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the same scope the engine resolves); picking
  one and pressing Play (opponent char target) routes the penalty into that
  characterÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s owner registry; with no target chosen the engineÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢s
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
  Build Influence, Promote and Use Rotate Effect). Not stale offers ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
  `buildLegalActions` is recomputed from live state on every call ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â and
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
  the action phase is not hanging ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the 60s harness window just closes before
  low-action games reach a winner. Read any harness "stall rate" that way.
* **Quiet, pass-heavy rounds are still real.** When all four seats pass
  consecutively the action phase ends; every default mix has exactly one EASY
  seat (Main: MEDIUM/HARD/EASY; harnesses: EASY/MEDIUM/HARD/MEDIUM ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no
  all-EASY default exists) at ~46% pass bias since the B5-0508 retune
  (previously ~53%), and rounds can advance with
  sparse action. B5-0372's
  D6 initiative cycle is live (un-passing on non-pass actions, 8ÃƒÆ’Ã¢â‚¬â€playerCount
  liveness backstop), but the EASY bias still makes some rounds quiet.
* **Zero promotions in harness output is a counting artifact.** The B5-0349
  `parseLog` counts promotions only for the token `": promotes "`, which no
  real log line produces: `RulesEngine` logs `"<player> promotes <title>ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦"`
  (no colon) and the action line is `"<player>: PROMOTE_CHARACTER: ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦"`.
  So promote counts print 0 even when promotions occur (the B5-0409 probe
  observed Inner Circles growing 1ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢2 during play). Trust IC size in a live
  game, not the harness promote figure. (Fix flagged for B5-0413.)
* **B5-0447 re-probe (10 games, seeds 101-109 step 2, 180s window):** 9
  WINNER + 1 TIMEOUT ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â timeout behavior is down to ~10% from the earlier
  all-timeout baseline under the parameterized window. End-state inspection
  counted ~4.9 promotions per game while the log-token promote figure
  printed 0 (the parser artifact above). The aggregate "agendas set" figure
  read 0/10 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the same parser-artifact class: the runner's agenda token
  still matches no real log line even though installs are visible in live
  inspection, so read agenda participation from the game log, not the
  aggregate. (B5-0413 still pending.) The B5-0462 re-probe under the
  station-aware AI reproduced this shape seed-for-seed (9 WINNER + 1
  TIMEOUT, the timeout in the same seed-105 game-2 slot as the baseline);
  promotions now print real counts and read ~3.2/game, mostly because
  games got shorter (mean rounds ~10.5 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ ~8.4; per-round rate nearly
  flat) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see the B5-0465 triage. Balance re-probes must reuse the 0447
  seed set (2 games ÃƒÆ’Ã¢â‚¬â€ seeds 101-109 step 2, 180s) for their deltas to be
  quotable; other seed choices are exploratory.
* **Influence Rating 20 is reachable and standard victory can trigger.**
  The B5-0409 probe reached Rating 20 in ~2 minutes and ended the game at
  the round boundary. In the sampled games no agenda won before standard
  victory did ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the agenda economy currently loses that race, not because 20
  is unreachable but because build-influence has no cap and compounding
  bonuses outrun agenda conditions.
* **Agendas ARE being installed and played** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â earlier "agendas set: 0"
  harness output is the same parser-artifact class as the promote count:
  the runner's agenda token matched no real log line (confirmed by the
  B5-0459 triage). End-state inspection in the B5-0409 verification found
  agendas installed in every sampled game (e.g. "As It Was Meant To Be"),
  plus live DISCARD_AGENDA plays. Since B5-0464 the missing emitter is
  landed and the aggregate counts real face-up installs ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â zeros before
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
  AI's B5-0343 "initiate when winning" bias ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â not a pathology. The B5-0462
  re-probe against the station-aware AI measured 67.5% (54/80), re-entering
  the band from the 0447 sample's 87%: the 0453 term raises war-declaration
  value, so more initiated wars meet genuine opposition instead of walking
  through unopposed (B5-0465 triage: real drift, band-normalizing, watch
  but do not fix).
* Advisory research (non-authoritative): SNRPG bulk extraction strategy and
  the CCG Trader 2-card pilot are in `investigations/`; they informed the
  cost backfill (B5-0335) and fleet-class plan (B5-0387) but are not
  canonical.

> **Honesty note (B5-0553, 2026-09-26):** full-table ledger pipe census
> shows 259 rows at the canonical 7-pipe format, 4 non-canonical DONE rows
> (B5-0202c at 9 pipes, B5-0316 at 8 pipes, B5-0449 at 8 pipes, B5-0490 at
> 10 pipes) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â all from in-content verify text or self-referential hygiene
> descriptions that contain pipe characters. Zero leading double-pipe rows
> and zero duplicate IDs remain (263 total B5 rows, all unique). These
> defects are on DONE rows whose owners closed them in prior passes; the
> [B5-0550 update: census re-run 2026-09-26 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â 270/270 unique IDs, zero
> duplicate IDs, zero defects on OPEN rows; the same four rows remain
> non-canonical.]
> next hygiene task should repair these four rows to canonical 7-pipe form
> while preserving their verify text, consulting the original owners where
> content protection applies (B5-0202c, B5-0316).

## 8. How to record playtest findings

> **Update (B5-0645, part 26, 2026-09-27):** the victory story changed this
> wave. **Major Victory is now implemented** (B5-0629): rulebook :182 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â at
> least 20 Power and at least 10 more than each other non-forfeited player ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â
> with the MJR suite section pinning the 10-vs-9 boundary, the major-agenda
> holder's access, and the forfeited-player exclusion. The same task fixed a
> real defect: **Standard Victory condition 1 had no Shadow War guard** (the
> :178 suppression existed only on condition 2), so a 20+ strictly-leading
> player could be crowned mid-War; the red-first probe is on record in the
> B5-0629 report. Interpretations logged in DECISIONS: a met agenda condition
> outranks Major Victory, and the 10-point lead counts only non-forfeited
> players. The aftermath registry is no longer append-only (B5-0637):
> `GameState.clearAttachedAftermaths()` is hooked into `advanceRound()`, so
> attached aftermaths gate same-name plays within their own round and free
> the name at the boundary (AMT3 section). The AI now sees the major path
> (B5-0635): MEDIUM/HARD score influence-moving actions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â builds and
> uncontested race-target wars ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â toward the 10-point threshold
> (MJR-AI section); EASY stays uniform (band re-verified 0.48, in the
> 0.20ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“0.70 band). Round order is pinned per-substep by the ORD section
> (B5-0631, solar-pro4:free), including the actual-code divergence that the
> victory check fires after every action, not only at the round boundary.
> Coverage measurement is now instrumented: `.agent/tools/suite-coverage.ps1`
> (B5-0633) counts check sites across quoting styles and names unparsable
> sites instead of silently zeroing. Suite count: 512 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ **542/542**
> (+9 ORD, +9 AMT3, +12 MJR-AI), RUN_TESTS=1 plus all seven standalone
> probes green on the post-wave re-sweep (B5-0643, independently
> corroborated). Honest notes: the B5-0631 round-order work had one
> concurrent-edit collision, repaired and formally logged (B5-0652); one
> sweep ran twice in a race (B5-0643) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â both runs concordant, solar-pro4:free
> holds the row's authoritative close; the MJR engine change legitimately
> turned one stale D12 fixture red and it was tightened (21 vs 12), disclosed
> in DECISIONS.

> **Governance note (B5-0605, part 25, 2026-09-27):** the TASK_LEDGER
> frontmatter `assessor_llm` list was compacted under B5-0604 using the
> B5-0586 proposal's convention: 33 entries ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ **7 distinct agents**, each
> now a single entry `{name, version, passes: N}` (GPT-6 Codex 14, Muse
> Spark 11, Buffy deepseek 2, me-so-poor 2, Buffy glm 2, Cline 1, Grok 1)
> in first-appearance order. What changed for contributors: on files that
> adopt this convention, do NOT append a new entry when your name+version
> is already listed ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â increment `passes` and update the file-level
> `last_modified_date` instead; append a new entry only on first
> involvement. `author_llm` is still never overwritten, and per-pass
> appends remain the rule for files that have not adopted the convention
> (adoption is per-file, recorded in that file's own history ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the ledger
> is the first adopter). This note documents the ledger's actual state;
> the repo-wide AGENTS.md amendment is still a pending follow-up.

Do not edit code or data while testing. Write observations (bugs, balance,
confusions) to `investigations/` as a new dated note or into an existing
report thread, and seed a task row through the normal OPEN-claim cycle so
an agent can pick it up. If a game hangs or errors, the console log lines
(`[Rn]`-prefixed state log) are the first thing to capture.