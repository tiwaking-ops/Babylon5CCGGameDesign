---
document:
  title: "Human playtesting and humans joining conflicts — findings, options, and open decisions"
  status: "Report"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# Human playtesting and humans joining conflicts

Author: Muse Spark (muse-spark-1.3-contributor-free)

Status: **Report** — observations and decision support only. No authority;
see `docs/DECISIONS.md` and `.agent/TASK_LEDGER.md` for the record. Written so
it can be pasted verbatim into another chat as the standing brief on this topic.

## 1. Background: what the audits established

The game is fully playable **by AIs** (headless smoke test: 4-AI rounds green;
conformance suite 45/45). The **human UI exposes exactly 2 buttons** — Pass
and "Play / Initiate" (`b5ccg/src/b5ccg/ui/MainWindow.java`) — while the
rulebook (`BABYLON5_CCG_RULEBOOK.md`, §V) grants a player ~14 Action-round
action types. The B5-0310 UI audit (`.agent/REPORTS/2026-09-21-solar-pro4-B5-0310.md`,
13 findings F1–F13) and the B5-0312 scenario playtest
(`.agent/REPORTS/2026-09-21-freebuff-01-B5-0312.md`) jointly establish:

* **F3 (P0/HIGH):** most Action-round actions are unreachable from the UI
  (sponsor/promote/build-influence/lead/support-oppose/attack/event/heal/repair…).
  The human plays one card or passes; the AI plays the full round.
* **F1 (P0/MEDIUM):** no support/oppose UI during an active conflict. The
  banner renders (`GameBoardPanel` lines 42–52) but the human cannot join;
  the AI auto-joins in `resolveCurrentConflict()`.
* **F2 (P0/MEDIUM):** no conflict target selection — `playSelected()` lines
  136–144 auto-target the leading non-human player.
* **F5 (P1/HIGH):** no cost/affordability preview; **F7 (P2/MEDIUM):** no
  support/opposition totals display (now renderable via the D14 sides API,
  B5-0309); **F13 (P2/LOW):** hand cards omit influence cost (blocked on the
  missing cost field, C1/D13 → B5-0315 design proposal).
* **B5-0312 headline:** 24 playtest rounds produced ZERO conflicts — all 108
  conflict cards sit past the smoke deck builder's file-order filler cut, so
  the harness never exercises the conflict pipeline (B5-0313 harness deck fix
  is CLAIMED to address exactly this).
* Deliberate scope (not defects): single-human UI ("Single Player" shell);
  phase-agnostic `isWaitingForHuman` engine gating.

## 2. The three open decisions (human-only; never seeded as agent tasks)

### Q1 — Full Action-round set, or lean single-card UI?

*Example.* You hold Delenn (Inner Circle, ready), 6 influence, and a Narn
character in hand. Lean UI (today): play the card or pass — you cannot rotate
Delenn to build influence though the rulebook (§V, rating ≤ 9 → +1) entitles
you to it. Full UI: click Delenn → "Build Influence / Lead Fleet / Sponsor"
with cost preview and confirm (fixes F3+F5+F6).
**A:** full set — largest epic available (F1–F7, F9–F10; needs engine hooks
that do not exist yet, e.g. PROMOTE, B5-0202 Finding 7). **B:** lean — keep
2 buttons; invest in engine/AI/data.

### Q2 — Can the human join conflicts, or is that AI-driven?

*Example.* Narn AI raids your Mars Colony (Military); you hold an untapped
MIL-3 fleet. Join enabled: banner shows Support 5 vs Opposition 0, you click
Oppose → 5 vs 3, resolution changes. AI-driven (today): you watch 5 vs 0.
**A:** human joins — build F1 (join controls) + F2 (target selection) + F7
(totals display). **B:** conflicts auto-resolve; human initiates only. The
D14 sides work still pays off under B (AI-vs-AI correctness + aftermath
eligibility).

### Q3 — Onboarding UI for new players, or AI-study harness display?

The severity dial for F4–F13. *Example:* a red "MILITARY +3 INF" card — a new
player needs the F9 legend, F5 preview, F13 cost face, F12 narrative log; a
harness user needs F7 totals, F8 initiative order, and correct engine behavior.
**A:** onboarding — F4–F13 all worth doing. **B:** harness — only structural
gaps (F1/F2/F3) matter; polish drops to "someday".

## 3. Overseer recommendation (advisory, not a decision)

**B / B / B (lean + AI-driven + harness), for now.** The project's verified
strength is engine/AI/data correctness under suites; the UI has no users yet;
a full action-set UI is the costliest epic while engine gaps (IC promotion,
influence-cost scoring, cost field) would block it regardless. Take the cheap
wins serving both audiences anyway: **F7** side-totals display (seeded B5-0316),
**F13** cost on card face (once B5-0315 designs the field), **F8** initiative
order (trivial). Revisit Q1/Q2 when the engine can offer a human the actions
(promote IC, join with sides — both exist since B5-0302/B5-0309).

## 4. Resolved by the human (opencode session ses_f3cae7b86ffe9XWGuSqYAtcU5d, "Human playtesting conflict joining decisions", 2026-09-21)

1. **Q1 = A (Full Action-round set).** Reason: "You are playing only 10% of
   the game without a full action-round set." F1/F2/F3 + F5-preview epic is
   now authorized direction (supersedes the lean recommendation in §3).
2. **Q2 = A (Human joins).** With a targeting rule: "Border Raid [MILITARY]
   targets only one other player. This conflict is only between two players
   not between multiple players." I.e. targeted conflicts are initiator +
   target(s) only — the participation-restriction model (fleetClass /
   mustTakeSide data proposal) rests on this ruling. Stated context: the game
   is for humans (humans-only, mixed, or even AI-only as spectators).
3. **Q3 = B (Harness).** Reason: "The current audience are all B5 CCG
   experts. Polish can be added in the far far future." F4/F8–F13 stay
   "someday"; structural gaps keep priority.
4. Premise recorded: no computer-player versions nor complete strict
   computer-playable rulesets exist — AI players are approximations, and
   rules fidelity is owed to human play, not to AI convenience.

## 5. Remaining unresolved issues

1. Long-game stall risk (B5-0312: 20–20 tie, no winner under D12
   strictly-greatest) — tiebreak/agenda-point design not yet tasked.
2. Build Influence unreachable in practice (no IC promotions exist) and
   influence-cost AI scoring (Finding 6) — queued behind conflict visibility.
3. Data-proposal Q5 (deluxe Border Raid confirmed unchanged from premiere)
   has NO user-turn evidence in the cited session — recorded as
   agent-data-comparison, NOT a human ruling, until the human confirms (see
   DECISIONS entry).

## 5. Traceability

Rulebook: `BABYLON5_CCG_RULEBOOK.md` (§III rounds, §IV cards, §V actions).
Audits: B5-0310 report (F1–F13), B5-0312 report (harness conflict gap).
Engine state: D14 sides (B5-0309 DONE), one-conflict-per-turn (B5-0302 DONE),
deck-out penalty (B5-0204/D8), victory ties + major-agenda block (B5-0305/D12).
Open UI-side tasks: B5-0316 (F7). Awaiting design: B5-0315 (cost field).
