## What these questions are really about

Right now the game is fully playable **by AIs** (the smoke test runs 4-AI rounds green), but the **human UI only exposes 2 buttons**: Pass and "Play / Initiate" (`MainWindow.java`). The rulebook gives a player **~14 action types** in the Action round. The three questions decide how much of that gap gets built — that's weeks of agent work in one direction or nearly none in the other. Hence: your call, not the agents'.

---

## Q1 — Full Action-round set, or lean single-card UI?

**Rulebook says (§V):** on your turn you may sponsor a character, promote one to the Inner Circle, rotate an Inner Circle character to build influence, lead a fleet, support/oppose a conflict, attack a participant, play events, heal, repair, and more — cycling in initiative order until everyone passes consecutively.

**UI does today:** you click a hand card, press "Play / Initiate", and the code plays it or auto-initiates a conflict (`playSelected()`, auto-target = strongest opponent). No rotate, no sponsor flow, no promote, no build-influence button.

**Example.** Your turn. You hold Delenn (Inner Circle, ready), 6 influence, and a Narn character card in hand:
- *Lean UI (today):* you can play the Narn card (or pass). You cannot rotate Delenn to build influence, even though the rulebook (§V, rating ≤ 9 → +1 rating) entitles you to it.
- *Full UI:* you'd click Delenn → menu offers "Build Influence / Lead Fleet / Sponsor", pick one, see the cost preview, confirm. That's findings F3+F5+F6 fixed.

**Options:**
- **A. Full set** — largest epic in the backlog (F1–F7, F9–F10; also needs engine hooks that don't exist yet, e.g. a PROMOTE action — B5-0202 Finding 7). Human becomes a first-class player.
- **B. Lean** — keep the 2-button UI; invest instead in engine/AI/data. Human plays a simplified game.

---

## Q2 — Can the human join conflicts, or is that AI-driven?

**Rulebook says (§III–§IV):** when anyone initiates a conflict, every other player may rotate characters/fleets to support or oppose it, and resolution compares total support vs total opposition.

**UI does today:** nothing — a banner says "CONFLICT: Border Raid [MILITARY]" (`GameBoardPanel` lines 42–52) with no Support/Oppose buttons, no side totals, no participant list (findings F1, F7). The AI auto-joins inside `resolveCurrentConflict()`; your cards sit out unless the engine moves them.

**Example.** The Narn AI initiates a Military raid targeting your Mars Colony. You have an untapped fleet (MIL 3) that could oppose:
- *Join enabled:* banner shows Support 5 vs Opposition 0; you click your fleet → "Oppose" → totals become 5 vs 3; resolution actually changes.
- *AI-driven (today):* you watch the banner resolve 5 vs 0 and lose the colony without input.

**Options:**
- **A. Human joins** — build F1 (join controls) + F2 (target selection) + F7 (side/totals display). Genuinely fun, genuinely work.
- **B. AI-driven** — conflicts resolve automatically; human only initiates. Much cheaper; the D14 sides engine work still pays off (AI vs AI correctness + aftermath eligibility).

---

## Q3 — Onboarding UI for new players, or AI-study harness display?

This is the **severity dial** for a dozen findings. Same UI, two audiences:

**Example.** A red card reading "MILITARY +3 INF":
- *New player* thinks: "Military what? Can my characters fight in this? What does +3 mean?" — needs the F9 legend (conflict types → abilities), the F5 cost preview, the F13 cost on the card face, a narrative log (F12).
- *Harness user (you, studying AI behavior):* needs none of that — needs the F7 totals, the F8 initiative order, and correct engine behavior underneath.

**Options:**
- **A. Onboarding** — F4–F13 all worth doing (labels, legends, previews, narrative log, overflow handling).
- **B. Harness** — only structural rulebook gaps matter (F1/F2/F3); polish findings drop to "someday".

---

## My recommendation

**B (lean) + B (AI-driven conflicts) + B (harness)** — for now. Rationale: the project's proven strength so far is engine/AI/data correctness verified by suites; the human UI has no users yet, and a full action-set UI is the biggest epic available while engine gaps (IC promotion, influence-cost scoring, cost field) would block it anyway. Take the cheap wins that serve both audiences regardless: **F7** (side totals display, seeded as B5-0316), **F13** (cost on card face — once B5-0315 designs the cost field), **F8** (initiative order, trivial). Revisit Q1/Q2 when the engine can actually offer a human the actions (promote IC, join with sides — both now exist after B5-0302/B5-0309).

If you answer differently (e.g. "full action set, human joins, onboarding"), I'll seed that epic next — it's roughly 6–10 tasks. Just say the word as **A/B for each of Q1, Q2, Q3**.

