---
document:
  title: "Design proposal — breaking 20–20 stalls: agenda points, the Babylon 5 leader rule, and interim reporting tiebreaks (B5-0332)"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-22"
---

# B5-0332 — Tiebreak / agenda-victory design proposal (report-only)

Agent: `freebuff-01` (Buffy, deepseek-v4-flash). No engine, model, ai, ui, or
data files were edited for this task. All engine references below were read
from the working tree at commit-state 2026-09-22 (~08:55Z).

## 1. The problem, precisely

The B5-0312 playtest ended a run with **both AI players at 20 influence and no
winner**. That behavior is *rulebook-faithful*, not a bug: the rulebook's
Standard Victory requires "20 Power, **and more than any other player**"
(§Victory, condition 1), and `RulesEngine.standardVictory` implements exactly
that strict comparison — a tie with any opponent blocks the win, so at 20–20
`checkVictory` returns null and play continues.

The stall window in this implementation is narrow but real. A player wins the
moment they sit at ≥ 20 with a strict lead, so a lasting stall needs *both*
conditions:

1. **Both players reach ≥ 20 in the same window** (none strictly ahead), and
2. **No one holds a satisfied agenda win** — an `INFLUENCE_20` agenda
   (`>= 20`, no strict comparison) already breaks the tie today.

Three landed changes have already narrowed this window since B5-0312:

- **B5-0321** seated the ambassador in the Inner Circle, making Build
  Influence reachable — the designed catch-up/progress action (rotate IC,
  +3 apply, +1 rating). A player at 19 who Builds once reaches 20 *with a
  strict lead* and wins immediately.
- **B5-0313** put 2 agendas in every harness deck, so ties at 20 are far more
  likely to be broken by an agenda win than in the B5-0312 era.
- **B5-0324** armed cost-aware AI scoring (inert until costs are backfilled).

Still, symmetric influence income (personalities + locations) means two
players climbing in lockstep can hit 20 together, and the B5-0312 evidence
shows it actually happened. The remaining proposal space is therefore about
**what should break (or prevent) a double-arrival at 20 with no agenda**.

## 2. Options considered

### Option A — Rulebook-faithful "Babylon 5 leader" rule (recommended engine milestone)

Rulebook §Victory condition 2: *"If Babylon 5 has an Influence Rating of 20 or
more at the end of a turn — and one player eligible to win a standard victory
is leading in Power — then that player wins."*

This is the game's own anti-stall mechanism, and it is currently **unbuilt**:
there is no station entity anywhere in `model/`/`engine/` (verified by grep).
Proposed shape:

- New model entity `Babylon5Station` (influence rating, starts at a small
  value; the exact start value needs a data check against station-related
  cards — the pool contains at least "Support Babylon 5" and
  "Babylon 5 Unrest" in both sets).
- Station cards' effects push/pull station influence; until their effects are
  wired (the B5-0203 `CardEffects` dispatch knows per-card ids), the station
  can drift upward on a slow rulebook-consistent schedule or stay fixed.
- `checkVictory` gains the condition-2 path: station ≥ 20 AND exactly one
  non-forfeited, standard-eligible player strictly leads → that player wins.

Pros: pure rulebook; resolves stalls exactly when the fiction says the
station's stability crowns a leader; foundation for Shadow War (§Shadow War)
later. Cons: largest scope (model + engine + card-effect wiring); needs a
decision on the station's start value and drift.

### Option B — Agenda points as victory currency

Extend the existing (fully implemented) agenda vocabulary: agenda points
accumulate per agenda-owned condition met during play (e.g., +1 per won
conflict of the agenda's associated type), and N points either (a) count as
bonus Power toward the strict-20 comparison, or (b) become a threshold win at
20+. This mirrors how agenda cards historically function as the differentiator
that makes "more than any other player" reachable.

Pros: reuses the shipped `AgendaCard`/`winConditionKey` machinery and the
B5-0313 quotas; keeps victory dynamic and conflict-driven. Cons: not stated by
the rulebook as a power modifier — it is a house-rule layer needing its own
DECISIONS entry; requires a data backfill (agenda-to-conflict-type
association does not exist in the JSON today).

### Option C — Deterministic reporting tiebreak (no engine change)

For headless playtests and the future dashboard only: when a round cap is hit
with ≥ 2 players at ≥ 20 and no winner, apply a fixed, documented tiebreak
chain for *reporting* — (1) most total committed fleet Military, (2) most
Inner Circle members, (3) most influence gained over the run, (4) declared
shared victory. Implemented entirely in the harness/report layer; the engine's
`checkVictory` stays rulebook-pure.

Pros: zero engine risk; converts "stall" data points into comparable
outcomes; can ship inside any harness task. Cons: not a real game rule —
purely observability.

## 3. Recommendation

Ship in this order, as separate future tasks (none authorized by this
proposal's report-only scope):

1. **Option C now** — one small harness task, removes the reporting blind
   spot that B5-0312 hit.
2. **Option A next** — the rulebook-faithful fix; the only one of the three
   that belongs in the shipped game. Scope it as model + engine + a station
   card-effect wiring sub-task under the existing `CardEffects` dispatch.
3. **Option B only if playtesting shows** agendas still fail to differentiate
   after A lands — it is the most invasive to game balance.

## 4. Verification hooks for whichever lands

- Conformance suite: tie at 20–20 → `checkVictory` still null (rulebook
  purity) *unless* the option's own condition applies; strict-lead at 20 still
  wins; agenda win at 20 still wins before any new path.
- Harness: re-run the B5-0312 scenario; the 20–20 endpoint should either
  resolve (A) or report a labeled tiebreak result (C).
