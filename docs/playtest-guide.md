---
document:
  title: "Human playtest guide — running the B5 CCG prototype"
  status: "Guide (describes the working tree as of 2026-09-25)"
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
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-25"
---

# Human playtest guide — B5 CCG prototype

This guide tells a human how to run the current build, what every control
does, and where the honest gaps are. It describes the working tree as of
2026-09-25; features landed after that date may not be covered. The
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
until every player has passed consecutively (a non-rulebook safety cap of
8×playerCount guards against infinite loops) — then the draw phase, then
the next round. One simplification to know before you playtest: **a
declared conflict resolves immediately when initiated** (not in a separate
Resolution Round as the rulebook structures it). Joining now offers a real
decision window when you are eligible to participate:
AI seats choose first, then the controller pauses during conflict resolution
for your Support or Oppose choice. Your face-up ambassador, if present, is
committed to that side and play resumes. Mandatory-participation conflicts (B5-0336) still
apply, including cases where the engine compels your ambassador without a
voluntary choice. The conflict still resolves as part of the initiation action;
there is no separate Resolution Round.

## 4. What you can do today — control reference

Main window buttons (enabled contextually; a disabled button is illegal for
you right now, not broken):

> **B5-0414 audit caveat (P0):** the **Lead Fleet, Use Rotate Effect, Attack,
> Heal, and Repair** buttons enable from a *selected* card, but today only
> **hand** cards can be selected — board characters/fleets/locations have no
> selection hooks. In a real game those five controls stay dark because their
> eligible targets are board cards. The fix (a board CardSelectedListener) is
> the B5-0423 slice. The rows below describe the intended behavior once that
> lands.

| Control | What it does | Notes |
|---|---|---|
| **Pass Turn** | Pass your action | Consecutive passes by all players end the Action Round |
| **Play Card** | Play the selected hand card | Events route to the effect tables; agendas install as your agenda |
| **Initiate Conflict** | Initiate the selected conflict card | Some conflicts require a declared target (B5-0336: e.g. Border Raid); use the target selector when enabled |
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
| **Attack** | Attack a participant in the active conflict with the selected ready card; damage is mutual — each side deals its current conflict ability + 2 per its own Strife mark | Enabled on your Action turn with an active conflict and a selected ready, non-neutralized card; the target is auto-selected as the first valid opposing committed card (B5-0402, engine B5-0370, damage model B5-0368). Neutralization can trigger on either side from the overflow. |
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
resolves; agenda slots show MAJOR/minor and a `[WIN]` marker when the
agenda's condition is currently met; `A+`/`A$` mark the assistant
assist-bonus and sponsor-discount states (B5-0339).

## 5. The AI seats

* **EASY (Londo)** passes often (~53% of decisions) and otherwise picks
  uniformly among legal actions — useful for watching mechanics play out.
* **MEDIUM (Delenn)** is cost-aware and deterministic; scores mercenary bids
  by projected win probability minus the bid cost (B5-0403).
* **HARD (G'Kar)** adds agenda win-condition proximity, aftermath
  anticipation, event/contingency catch-up scoring, and rotate-effect scoring
  (B5-0344), plus conflict-side choice instead of always-oppose defaults
  (B5-0343), damage-aware attack/heal/repair scoring (B5-0378), and
  mercenary-bid scoring with a steeper opportunity-cost penalty (B5-0403).
* Difficulty contracts are pinned by the standalone harness
  `HeadlessAIDifficultyContractTest` (B5-0351).
* **Pass-bias proposal status (B5-0422):** the ~53% EASY pass bias sits inside
  its designed 0.35-0.70 band but drags multi-seat games into quiet,
  low-promotion rounds (B5-0409). Two parallel advisory proposals are on
  record: solar-pro4:free recommends a harness/Main seat-mix change first,
  then an EASY retune; Buffy (glm-5.3-flash) recommends a one-line EASY
  direct-pass-gate retune (observed pass ~0.45-0.50, still in band). No
  behavior has changed; both await the normal proposal-approval path.
  Note: default seat mixes already include exactly one EASY seat (Main:
  MEDIUM/HARD/EASY; harnesses: EASY/MEDIUM/HARD/MEDIUM) — no all-EASY
  default exists in the tree.

## 6. Headless testing (no UI)

`sh compile.sh` with `RUN_TESTS=1` runs the full conformance suite
(360 checks) plus a smoke game. Several standalone CLI harnesses exist
(not wired into RUN_TESTS — run directly with `java -cp out`):

* Seeded multi-round runner from B5-0349 prints N games of aggregate stats:
  ```
  java -cp out b5ccg.engine.HeadlessMultiRoundTest <games> <seed>
  ```
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

**Engine + AI done, no human UI yet — the AI can do these; you cannot trigger them from the window:**

* (empty as of this refresh — the last entries in this bucket, Lead Fleet /
  Use Rotate Effect (B5-0401) and Attack / Heal / Repair (B5-0402), now have
  UI controls; see the control reference above and the live list below.)

**Engine done and live — visible or materially affects gameplay today:**

* **Influence economy** — the D9 rating-vs-applied-pool split (B5-0369) is
  live: Build Influence applies +3 to your per-turn pool (not your permanent
  Rating), and sponsor/promote spend from the pool. The old defect where
  every sponsor permanently eroded your Rating is resolved. The
  free-participant waiver (B5-0374, E3) is also live. The E1 double-cost
  pool interaction (B5-0373) is DONE: the named `isDoubleCostRequired`
  helper and neutral exemption are wired into the applyInfluence spend site
  — behaviour-preserving (base recruit cost math unchanged); conformance
  360/360 PASS.
* **Damage subsystem** — characters can be damaged and neutralised; the
  damage/neutralisation model (B5-0368) plus attack (B5-0370) and
  heal/repair (B5-0371) are all live, with UI controls landed (B5-0402).
  **But the controls are unreachable in real play** pending B5-0423 (the
  B5-0414 audit caveat above: board-card selection is missing), **and in
  AI-vs-AI play they do not fire**: conflicts resolve synchronously inside the initiating
  action (B5-0409 finding A), so the active-conflict state those actions
  require never exists when an AI seat picks its action — pure AI-vs-AI
  rounds observed zero attacks, heals, repairs, and neutralisations across
  all verification probes. This is a real engine-loop gap (the rulebook's
  separate Resolution Round is the fix direction), not a UI bug.
* **Assistant mechanic** — real (B5-0339): rotate your assistant for +1
  Diplomacy/Intrigue/Leadership, or consume a sponsor discount on a later
  recruit. The A+/A$ markers on the ambassador mini-card reflect live state.
* **Bonus layer** — stat bonuses now use an expiry-backed layer (B5-0367)
  rather than permanent mutation; fleet leadership (B5-0337) and assistant
  (B5-0339) compose through it. Not directly player-visible beyond the
  existing markers, but it means round-boundary expiry and stacking now work.
* **AI mercenary bidding** — live (B5-0403): MEDIUM/HARD offer and score
  BID_ON_MERCENARY actions for offered mercenaries (minimal strictly-winning
  increment, pool-affordability gated); EASY picks uniformly from the same
  legal list. MER-AI conformance section ×10 landed with it (suite 350→360).
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

**Honesty notes for playtesters:**

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
  all-EASY default exists) at ~53% pass bias, and rounds can advance with
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
* **Influence Rating 20 is reachable and standard victory can trigger.**
  The B5-0409 probe reached Rating 20 in ~2 minutes and ended the game at
  the round boundary. In the sampled games no agenda won before standard
  victory did — the agenda economy currently loses that race, not because 20
  is unreachable but because build-influence has no cap and compounding
  bonuses outrun agenda conditions.
* **Agendas ARE being installed and played** — earlier "agendas set: 0"
  harness output is the same parser-artifact class as the promote count:
  the runner's agenda token matches no real log line. End-state inspection
  in the B5-0409 verification found agendas installed in every sampled game
  (e.g. "As It Was Meant To Be"), plus live DISCARD_AGENDA plays.
* Deck-out penalty is live (B5-0204/B5-0304): drawing from an empty deck
  discards your non-ambassador Inner Circle character if you have one, else
  signals forfeit for the victory check. Severe-damage overflow can also
  neutralise (B5-0368).
* Conflict resolution is synchronous at initiation, not in a separate
  Resolution Round. The human join window is real (B5-0363): when a conflict
  is active and you are eligible, the Support/Oppose controls light up and
  your click unblocks the controller. Mandatory-participation conflicts
  (B5-0336) still compel your ambassador when the rules require it.
* **Conflict initiators win more often than not.** B5-0409 reproduction
  measured ~64% initiator wins on trackable lines (58-71% varied by seed and
  trackability); 0408 reported 71%. This is consistent with the B5-0309
  sides rule (initiator wins iff support > opposition at initiation) plus the
  AI's B5-0343 "initiate when winning" bias — not a pathology.
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