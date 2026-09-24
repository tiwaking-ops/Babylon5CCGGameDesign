---
document:
  title: "B5-0360 — Economy modeling proposal: double-cost, mercenary bids, free-participant waiver"
  status: "Proposal"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-23"
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  last_modified_date: "2026-09-23"
---

# B5-0360 — Economy modeling proposal

Proposal-only, no `b5ccg/src/` or `b5ccg/resources/` edits. Prerequisites: B5-0342
(D9 influence pool) must be accepted/implemented first — every spend path below
assumes the Rating/pool split. Source findings: B5-0203 audit D13 (double-cost
gap), B5-0345 §V action audit (mercenary gap), Q4 human ruling on Non-Aligned
Support free-participant (corroborated by the Perplexity advisory intake).

## 1. What "economy modeling" covers here

Three distinct but related gaps in how influence flows in the current engine:

| # | Topic | State today | Proposal scope |
|---|-------|-------------|----------------|
| E1 | Double-cost for other-race loyal characters | Partial: `baseRecruitCost` doubles loyal other-race, but expressed as a Rating computation, not a spend against the pool; no explicit "waiver" concept | Model the apply-site interaction with B5-0342's pool; record the waiver rule |
| E2 | Mercenary influence bids | Not modeled at all (B5-0345 Tier 4: deep slice, new phase + data) | Proposal shape only — do NOT implement here; define the contracts |
| E3 | Non-Aligned Support free-participant waiver | Not encoded; the card exists in data but its participation rule is widening, not a restriction, so it was deliberately left unencoded (B5-0336 decision) | Define the waiver as a first-class concept so it composes with E1 + B5-0342 |

These three share a single theme: **how influence is spent, waived, or bid**
— hence one proposal doc, but three separable implementation tasks if/when the
human wants them.

## 2. Rulebook specification

### 2.1 Sponsor cost and the double-cost rule (Rulebook §Sponsor, :647–:659)

- Each supporting character has a listed influence cost (now in the data per
  B5-0335; absent → 0 per B5-0323).
- Sponsoring a loyal or neutral character requires applying the listed cost
  (§Sponsor bullet 2).
- "Characters with a different race name written as part of their card type
  require a faction to apply double the listed influence cost to sponsor."
  (:659) — the **double-cost rule**.
- "Neutral characters may be sponsored by any race at no additional influence
  cost." (:659) — the **neutral exemption**.
- The rulebook does not attach the double-cost to a field on the card; it is a
  faction-vs-card-race computation. B5-0315's design decision (cost is a card
  field; double-cost is a Faction-side computation) is the right seam.

### 2.2 Mercenaries (Rulebook §Mercenaries, :735–:741)

- "Some cards in the game may be used each turn only by the player who applies
  the most influence to control them. Such cards are called 'Mercenaries'."
- "A faction may, as an action, apply influence as a 'bid' to control a
  mercenary card for the current turn or to increase the amount of a previous
  bid."
- "Mercenaries act after all players have passed, but before the beginning of
  the Resolution round."
- "Their action is dictated by the faction which applied the most influence
  during the turn (bids are cumulative)."
- The current engine has: no mercenary concept, no bid state, no cumulative
  bid resolution, no post-pass mercenary phase.

### 2.3 Free participant (Rulebook §Free, :1154)

> "Especially, 'sponsor for free'. If you are permitted to sponsor a card for
> free you may do so immediately, no matter what round it is, for no influence
> cost and without rotating a sponsoring character. You must meet any other
> restrictions, however; you may not, for example, 'sponsor for free' a limited
> character who is already in play."

### 2.4 Non-Aligned Support (Q4 human ruling, corroborated advisory)

Per the Q4 ruling (recorded in `docs/reports/perplexity-non-aligned-support-free-participant-ruling-2026-09-21.md`
and the human-session ruling recording in DECISIONS.md):

- "Free participant" on Non-Aligned Support means: a Non-Aligned fleet may join
  the conflict as a normal participant, but does so at zero influence cost and
  without requiring a sponsor rotation.
- This is an **eligibility widening**, not a restriction — it was deliberately
  left unencoded in B5-0336 (the card's participation field stays absent/open;
  the fleet joins normally).
- The Q4 ruling applies the §Free concept to the *joining* action (no cost, no
  rotation) — it is not a "sponsor for free" in the §Sponsor sense, but the
  same §Free semantics (no influence cost, no sponsor rotation, must meet other
  restrictions) carry over.

## 3. Proposal

### 3.1 E1 — Double-cost as a pool spend (depends on B5-0342)

Today `baseRecruitCost(card, faction)` returns `cardCost * 2` for other-race
loyal characters, and the sponsor site calls `spendInfluence(effectiveCost)`.
With B5-0342, `spendInfluence` becomes `applyInfluence` against the pool — the
double-cost is then correctly spent from the per-turn pool, not permanently
destroyed from the Rating.

No new model field needed. The proposal is to **make the double-cost rule
explicit in the codebase** rather than implicit in `baseRecruitCost`:

- Add a named constant / named helper `isDoubleCostRequired(CharacterCard, faction)`
  so that the rule is greppable and auditable.
- The effective sponsor cost remains `baseRecruitCost − assistantDiscount` (B5-0339
  seam), just applied against the pool rather than the Rating.
- Neutral exemption: `baseRecruitCost` already returns the card's raw cost for
  NEUTRAL — no change, but add a comment citing the rulebook :659 exemption so
  it does not get "fixed" later.

**What does NOT change**: card costs (B5-0335 owns them), the double-cost rule
itself (rulebook-faithful, already implemented behaviorally), the assistant
discount (B5-0339 owns it).

### 3.2 E2 — Mercenary bid model (proposal shape only, no implementation)

This is a deep slice (B5-0345 Tier 4). The proposal below defines the contracts
so a future implementation task has a spec, not a blank page.

**Model additions (proposal):**

- `MercenaryCard` subtype/flag on card data (no data task yet — B5-0355 text
  audit is the prerequisite; the rulebook's "some cards" is not encoded anywhere
  today).
- Per-player bid state: `Map<MercenaryCard, Integer> bids` on `GameState` (or
  per-player, depending on whether bidding is simultaneous or turn-ordered — the
  rulebook says "a faction may, as an action, apply influence as a bid… bids are
  cumulative," which reads as pooled across the turn, not per-player turn order).
- Bid action: `BID_ON_MERCENARY` GameAction type (placeholder name), applying N
  influence from the pool to a specific mercenary (pool spend per B5-0342).
- Resolution: after all players pass (pre-Resolution phase), the mercenary's
  controlling faction = highest cumulative bidder; tie-break rule is unspecified
  in the rulebook excerpt — proposal: highest bidder wins, ties go to current
  controller or no one (open question for the implementation task).
- Mercenary action: the controlling faction executes the mercenary's action
  during the mercenary phase, before Resolution.
- Phase gating: a new `MERCENARY` phase between `ACTION` end and `CONFLICT_RESOLUTION`
  (or wherever the rulebook places it — the excerpt says "after all players have
  passed, but before the beginning of the Resolution round," which maps to a new
  phase inserted before `CONFLICT_RESOLUTION`).

**Open questions for the implementation task (not resolved here):**

1. Is bidding turn-ordered or simultaneous? Rulebook says "a faction may, as an
   action" — which reads turn-ordered, but "bids are cumulative" suggests a shared
   pool. The implementation task must choose and record.
2. Tie-break for highest bidder.
3. What counts as a mercenary card in data? The rulebook says "some cards" — no
   list exists in our data. B5-0355 text-audit + a data task to flag mercenaries
   is the prerequisite. This proposal does not invent mercenary cards.
4. Does the mercenary phase consume rotations / actions / influence beyond the
   bid? Rulebook is silent. Conservative default: the bid is the only cost; the
   mercenary action is free once controlled.

### 3.3 E3 — Free-participant waiver as a first-class concept

The §Free rule (:1154) is a **generic waiver**: if a card/text grants "sponsor
for free" or "join as a free participant," the recipient pays no influence cost
and skips the rotation requirement, but must still meet all other restrictions
(race loyalty, timing, "limited character already in play," etc.).

**Proposal: model the waiver, not the cards that grant it.**

- Introduce a `SponsorCost` value object (or a small result type) returned by
  the recruit/sponsor affordability path:
  - `amount` — the influence to apply from the pool (0 for waived).
  - `requiresRotation` — whether a ready Inner Circle member must rotate.
  - `isWaived` — whether the §Free waiver applies (for logging/audit, not for
    cost math; a waived sponsor has amount 0 and requiresRotation false).
- The waiver is granted by **card text effects**, which today are implemented via
  `engine/CardEffects.java` id-keyed tables (B5-0307). The waiver would be
  another effect class in that registry — e.g. an `effect_recruit_free` entry
  that, when applied to a recruit action, sets the waiver flags.
- Non-Aligned Support's "free participant" is the canonical example: a Non-Aligned
  fleet joining a conflict pays no cost and rotates no sponsor. Under this model,
  the join path would consult the same waiver concept — a join with waiver has
  `amount 0` and no rotation requirement. This unifies the §Free semantics across
  sponsor and join actions (the rulebook's §Free is general; the Q4 ruling applies
  it to joining).

**Why model the waiver rather than hard-coding Non-Aligned Support:**

- The rulebook's §Free is general — many cards grant "sponsor for free," "join
  for free," etc. Hard-coding one card's behavior would repeat when the next
  free-card appears.
- The waiver concept is card-text-effect-driven, which fits the existing
  `CardEffects` registry design (B5-0307) — no card-text parsing, just an
  id-keyed entry that flips the waiver flags on the action being considered.
- This keeps the Q4 ruling's semantic (zero cost, no rotation, other restrictions
  still apply) as a reusable primitive, not a one-card special case.

**What does NOT change here:** Non-Aligned Support's data (stays absent/open per
B5-0336 — it is a widening, not a restriction), the join path (B5-0322/B5-0336
own it), the card-effect registry wiring (proposal shape only — a future task
adds the waiver effect class to the registry).

## 4. Interaction with B5-0342 (D9 pool) — the load-bearing dependency

Every spend in this proposal is a pool spend, not a Rating mutation:

| Spend | Today (pre-B5-0342) | With B5-0342 | This proposal adds |
|-------|---------------------|--------------|--------------------|
| Sponsor other-race loyal | `spendInfluence(cardCost * 2)` — permanent Rating loss | `applyInfluence(cardCost * 2)` — pool spend | Explicit double-cost rule name; neutral exemption documented |
| Sponsor neutral | `spendInfluence(cardCost)` — permanent Rating loss | `applyInfluence(cardCost)` — pool spend | No change, just documented |
| Sponsor "for free" (waiver) | N/A — no waiver model | `applyInfluence(0)` + no rotation | Waiver flags on the sponsor result; effect-driven |
| Mercenary bid | N/A — no mercenary model | `applyInfluence(bidAmount)` — pool spend | New bid action + bid state + resolution phase |
| Build Influence | `loseInfluence(3) + gainInfluence(1)` — permanent −2 Rating | `applyInfluence(3)` pool + `gainInfluence(1)` Rating | No change (B5-0342 already fixes this) |

The dependency is load-bearing because **without B5-0342, every sponsor permanently
weakens the Rating** — which is the D9 defect B5-0342 fixes. Implementing E1/E2/E3
before B5-0342 would bake the defect into the economy model.

## 5. Ordering and task slicing

- **E1 (double-cost documentation + pool interaction)**: thin slice. Depends on
  B5-0342's `applyInfluence` being live. Could be a few lines in
  `RulesEngine.baseRecruitCost` + a named helper. No data, no new model.
- **E2 (mercenary bids)**: deep slice. Depends on B5-0342 (pool spend) + a data
  task to flag mercenary cards (B5-0355 text audit is the prerequisite) + new
  phase gating (B5-0341 D6 loop, or a standalone phase insert). Multiple files:
  model (mercenary flag), engine (bid state, bid action, resolution), controller
  (phase insert), AI (bid scoring — future). Not in scope for this proposal's
  implementation — define the contracts only.
- **E3 (free-participant waiver)**: medium slice. Depends on B5-0342 (pool spend,
  so a waived sponsor is `applyInfluence(0)`) + the card-effect registry extension
  (a future task adds the waiver effect class). The concept (SponsorCost result
  type with waiver flags) is the proposal deliverable; the wiring is a future task.

Recommended sequence: **B5-0342 → E1 → E3 → E2** (E2 is deepest; E1/E3 are
thin/medium and independent of each other once the pool is live).

## 6. Open questions (deferred to implementation tasks)

1. **Mercenary tie-break** (E2 Q1): highest bidder wins; ties go to current
   controller, or no one wins, or initiative order breaks ties — unspecified by
   the rulebook excerpt.
2. **Mercenary card identification** (E2 Q3): which cards are mercenaries? No
   data field exists. A data task (post-B5-0355) must flag them, or the engine
   must derive it from card text (not favored — B5-0307's id-keyed registry is
   the preferred shape).
3. **Bid turn-ordering** (E2 Q1): simultaneous pooled bids vs turn-ordered bids —
   rulebook ambiguous.
4. **Waiver scope** (E3): does "sponsor for free" also waive race-loyalty double
   cost? Rulebook §Free says "no influence cost" — which should waive the entire
   sponsor cost including any double, but the rulebook is not explicit. The
   implementation task should record the interpretation.
5. **Non-Aligned Support specifically**: the Q4 ruling applies §Free to joining,
   not sponsoring — the fleet joins as a free participant. If a future card says
   "sponsor a Non-Aligned fleet for free," that is a §Free sponsor waiver (E3),
   not a join waiver — the two are semantically parallel under this proposal but
   wired to different action paths (sponsor vs join).

## 7. Verification (proposal-only)

No code touched. Gate context: last full shared gate this session is RUN_TESTS=1
exit 0 (conformance 176/176 + smoke) as recorded in B5-0357's DECISIONS entry;
this proposal makes no claim on it. Acceptance criteria for the future
implementation tasks are summarized in §5 (ordering) — each slice defines its own
suite assertions when implemented.

## 8. Files referenced

- Rulebook §Sponsor: `BABYLON5_CCG_RULEBOOK.md` lines 647–659 (double-cost,
  neutral exemption, sponsor rotation).
- Rulebook §Mercenaries: `BABYLON5_CCG_RULEBOOK.md` lines 735–741 (bid action,
  cumulative bids, post-pass phase, control resolution).
- Rulebook §Free: `BABYLON5_CCG_RULEBOOK.md` line 1154 (free-participant/sponsor
  waiver semantics).
- Q4 ruling: `docs/reports/perplexity-non-aligned-support-free-participant-ruling-2026-09-21.md`
  + DECISIONS.md human-ruling recording (2026-09-21, big-pickle).
- B5-0342 (D9 pool): `docs/proposals/d9-rating-vs-applied-influence-design-proposal.md`
  — load-bearing dependency.
- B5-0345 (F3 triage): `.agent/REPORTS/2026-09-23-freebuff-01-B5-0345.md` —
  mercenary gap is Tier 4, deepest slice.
- B5-0335 (cost backfill): `.agent/REPORTS/2026-09-23-solar-pro4-B5-0335.md` —
  card costs now in data.
- B5-0339 (assistant discount): DECISIONS.md — sponsor discount seam consumed by
  this proposal's effective-cost computation.
- B5-0336 (participation enforcement): DECISIONS.md — Non-Aligned Support stays
  unencoded (widening, not restriction); this proposal's E3 does not change that.

## 9. Proposed task breakdown (if the human wants implementation)

- **B5-0360a** (E1, thin, engine/model): document double-cost rule + neutralize
  the pool interaction; depends on B5-0342.
- **B5-0360b** (E3, medium, engine/model): SponsorCost waiver result type +
  §Free effect-class concept; depends on B5-0342; wires to future card-effect
  registry extension.
- **B5-0360c** (E2, deep, engine/model/phase): mercenary bid state + bid action +
  resolution phase + data flag task prerequisite; depends on B5-0342 + B5-0341
  (phase gating) + a future mercenary-card data task.

These are proposal-record entries only — no tasks are claimed or seeded here.
The human decides whether to authorize any of them.
