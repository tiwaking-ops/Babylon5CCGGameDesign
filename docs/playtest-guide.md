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
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
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
| **Reveal Contingency** | Flip a face-down contingency under one of your hosts, triggering its effect (routed through the event dispatcher) and then discarding it | Owner-gated; the identity of placed contingencies is now exposed to the UI via the B5-0394 accessor. No contingency cards in the current pool — engine-ready, data-pending. |
| **Use Rotate Effect** | Rotate a character to apply a selected effect: assistant bonus (+1 Diplomacy/Intrigue/Leadership for the turn) or sponsor discount (−1 influence on a later recruit this turn) | Gated by the B5-0339 assistant mechanic (your own ready, unneutralized supporting-character assistant plus your ambassador); consumes your action (B5-0366). |

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
* **MEDIUM (Delenn)** is cost-aware and deterministic.
* **HARD (G'Kar)** adds agenda win-condition proximity, aftermath
  anticipation, event/contingency catch-up scoring, and rotate-effect scoring
  (B5-0344), plus conflict-side choice instead of always-oppose defaults
  (B5-0343).
* Difficulty contracts are pinned by the standalone harness
  `HeadlessAIDifficultyContractTest` (B5-0351).

## 6. Headless testing (no UI)

`sh compile.sh` with `RUN_TESTS=1` runs the full conformance suite
(326 checks) plus a smoke game. Several standalone CLI harnesses exist
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

* **Lead a Fleet** — engine + AI done (B5-0362). The AI offers LEAD_FLEET
  pairs and scores them. There is no UI control to rotate a leader; the
  board shows per-fleet leadership state via the B5-0337 FleetCard APIs.
  No UI task seeded yet.
* **Agenda lifecycle** — engine done (B5-0364): DISCARD_AGENDA,
  REPLACE_AGENDA, REVEAL_AGENDA, plus the one-major-agenda guard and
  face-down hidden state. The UI task (B5-0380, OPEN) has not landed, so
  you have no discard/replace/reveal buttons today. Playing another agenda
  through the Play Card button is correctly refused engine-side when one is
  already installed.
* **Use Rotate Effect** — engine + AI done (B5-0366). The AI offers
  USE_ROTATE_EFFECT for the assistant-bonus and sponsor-discount kinds and
  scores them. No UI control exists; the assistant state is still surfaced
  only through the A+/A$ markers on the ambassador mini-card.
* **Attack / Heal / Repair** — engine + AI done (B5-0370, B5-0371, with
  the damage/neutralization model from B5-0368). The injury system is live:
  characters accumulate damage tokens, abilities reduce to zero at
  neutralisation threshold, neutralised characters cannot act until healed,
  and fleet leaders auto-neutralise. The AI offers attacks, heals, and
  pool-paid repairs. You have no UI to trigger any of them.
* **Place / Reveal Contingency** — engine done (B5-0365, with the
  attached-identity accessor from B5-0394). No contingency-typed cards exist
  in the current card pool, so there is nothing to play or reveal; the
  engine path is sound but data-pending. The UI task (B5-0381) is BLOCKED
  behind the now-resolved B5-0394 API gap — retriable.
* **War conflicts** — engine done (B5-0376): TensionMatrix, declare-war
  action, RACE_TARGET/LOCATION_TARGET conflict modes, all-supported
  uncontested read, location capture/suppression, tension increment clamped
  at 5. No UI task seeded.

**Engine done and live — visible or materially affects gameplay today:**

* **Influence economy** — the D9 rating-vs-applied-pool split (B5-0369) is
  live: Build Influence applies +3 to your per-turn pool (not your permanent
  Rating), and sponsor/promote spend from the pool. The old defect where
  every sponsor permanently eroded your Rating is resolved. The
  free-participant waiver (B5-0374, E3) is also live. The E1 double-cost
  pool interaction (B5-0373) is BLOCKED pending unrelated engine/model
  fixes — behaviour is correct today because all cost values are zero, but
  the named `isDoubleCostRequired` helper and neutral exemption are not yet
  wired.
* **Damage subsystem** — characters can be damaged and neutralised; the
  damage/neutralisation model (B5-0368) plus attack (B5-0370) and
  heal/repair (B5-0371) are all live. You will see neutralised characters
  and damaged ambassadors during AI-vs-AI play; the AI's damage scoring
  (B5-0378) drives when it attacks and heals.
* **Assistant mechanic** — real (B5-0339): rotate your assistant for +1
  Diplomacy/Intrigue/Leadership, or consume a sponsor discount on a later
  recruit. The A+/A$ markers on the ambassador mini-card reflect live state.
* **Bonus layer** — stat bonuses now use an expiry-backed layer (B5-0367)
  rather than permanent mutation; fleet leadership (B5-0337) and assistant
  (B5-0339) compose through it. Not directly player-visible beyond the
  existing markers, but it means round-boundary expiry and stacking now work.

**Still open — unchanged or newly flagged:**

* **Mercenaries** — no data evidence in the pool (B5-0386, no-evidence
  verdict). The implementation slice (B5-0395) is OPEN and claimed; using
  synthetic fixtures per the B5-0365 precedent.
* **E1 double-cost pool interaction** — BLOCKED (B5-0373); see above.
* **Contingency UI** — BLOCKED (B5-0381) behind the now-resolved B5-0394
  accessors; retriable when a UI writer claims it.

**Honesty notes for playtesters:**

* The AI can stall the game. When all four seats pass consecutively the
  action phase ends; with the EASY-heavy default seating this can happen
  quickly and the round advances without much action. The multi-round
  harness (B5-0349) correctly reports "stalled" when it observes this. A
  D6-based unlimited-action rework is designed in
  `docs/proposals/d6-unlimited-actions-design-proposal.md` (B5-0341) but
  not implemented — the current loop uses a non-rulebook safety cap of
  8×playerCount.
* Deck-out penalty is live (B5-0204/B5-0304): drawing from an empty deck
  discards your non-ambassador Inner Circle character if you have one, else
  signals forfeit for the victory check. Severe-damage overflow can also
  neutralise (B5-0368).
* Conflict resolution is synchronous at initiation, not in a separate
  Resolution Round. The human join window is real (B5-0363): when a conflict
  is active and you are eligible, the Support/Oppose controls light up and
  your click unblocks the controller. Mandatory-participation conflicts
  (B5-0336) still compel your ambassador when the rules require it.
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