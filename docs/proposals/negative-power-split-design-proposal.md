---
document:
  title: "Negative Power — power-versus-influence split, feasibility and design proposal"
  status: "Proposal (never truth until merged + compiled)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Negative Power — feasibility and design proposal (B5-0639 remainder R15)

Task: **B5-0667**. Report-only deliverable — this file plus a `docs/DECISIONS.md`
entry. No model, engine, ai or ui code was touched and no Power field was added.

## 1. The rule, verbatim

`BABYLON5_CCG_RULEBOOK.md:1034` (section "Negative Power"):

> Any card that refers to only counting influence as power cannot affect any
> player whose power is lower than his influence.

Two supporting rules give the split its shape:

- `:171` — "A player's base Power is equal to his current Influence Rating.
  Other cards in play may add additional points to a player's Power total
  under conditions specified on the card itself."
- `:1158` (glossary) — "**Influence Rating** - A player's total influence
  available each turn. It is the influence component of a player's power.
  Gains and losses of influence alter a player's Influence Rating; applying
  influence does not."

So in the rulebook Power is a **derived** quantity: Power = Influence Rating +
Power add-ons granted by cards in play. Influence is a component of Power, not
a synonym for it. The Negative Power rule exists for exactly the case where a
card's effect targets someone by "power" while their Power has been pushed
*below* their raw Influence — the card loses its grip on them.

## 2. Premise verified against the code, not cited (B5-0657 discipline)

- Case-insensitive search for `power` over `b5ccg/src/b5ccg/model/Player.java`
  returns **0 occurrences**. No Power field, getter or setter exists anywhere
  in `model/`, and `RulesEngine.java` has no `getPower` either.
- Every victory and scoring path reads `Player.getInfluence()`
  (`RulesEngine.java:674,691,694,710` standard/major thresholds;
  `AgendaCard.isConditionMet` INFLUENCE_20; conflict reward at `:347`).
  Influence **is** Power engine-wide, exactly as the seeding row measured.
- Therefore the rule is **vacuous today, not merely unimplemented**: with
  Power ≡ Influence, no player's power can ever be lower than his influence,
  so the protection's precondition is never satisfiable. This is a materially
  different starting position from a missing feature — there is nothing to fix
  and no card that can currently exercise the rule (§4).

## 3. The minimum split

### 3.1 What must NOT happen (the failure mode this design rules out)

A stored `int power` field beside `influence` is the wrong shape and the
specific conflation the task names. The repo already has one mutable number
per concept, and every rulebook sentence that distinguishes the two says Power
is **derived** from Influence plus card-granted add-ons. A second mutable
field would have to be kept in sync with every `gainInfluence`,
`loseInfluence`, `applyInfluence`, aftermath, deck-out and surrender path
forever, and a stat total quietly drifting from its components is a
victory-integrity bug class, not a refactor. It would also put a mutable,
zero-backed field into a codebase whose card pool has no Power source at all
(§4), i.e. permanent dead state.

### 3.2 The seam: a computed Power, not a stored one

Add, when the rule first becomes exercisable:

- `Player.getPower()` — computed: `return getInfluence() + powerBonusTotal();`
  where `powerBonusTotal()` sums the existing `StatBonus` list filtered to a
  new `StatKey.POWER` (or a parallel `BonusKind.POWER` tag). The bonus
  machinery (`StatBonus.faction(...)` factories, `sweepBonusExpiries`,
  `removeBonusesBySource`) already handles sources, expiry and removal, so a
  Power add-on rides the existing channel instead of inventing a second one.
- Until any card grants a POWER bonus, `getPower() == getInfluence()` for
  every player — byte-identical behaviour, assertable by the existing suite.

This is the whole model-side split. Nothing else in the model changes; there is
no Power field to backfill because §4 shows the pool has no Power stat.

### 3.3 Which reads stay influence reads (correct as they are)

Everything that spends, grants or scores Influence stays on
`getInfluence()` — these are influence words in the rulebook and in every card
text in the pool:

| Call-site class | Sites (representative) | Verdict |
|---|---|---|
| Victory thresholds | `RulesEngine.java:674,691,694,710` (standard :175/:182), `AgendaCard` INFLUENCE_20 / MILITARY_SUPREMACY / MOST_INNER_CIRCLE | **Stay influence.** Rulebook victory text says Power, but Power ≡ Influence + add-ons, and with no add-on source the distinction is empty; when §3.2 lands these become `getPower()` **as one mechanical pass** (the rulebook's victory conditions are genuinely Power conditions). |
| Economy | `gainInfluence`/`loseInfluence`/`applyInfluence`, conflict reward `RulesEngine.java:347`, location income, surrender +3 influence, deck-out | **Stay influence.** Card texts say "Influence" and influence is spendable; Power is not (rulebook :163 — applying influence is not losing it, Power has no apply/spend semantics). |
| AI scoring | `AIPlayer.java` influence offers, `majorProximityMedium/Hard` (B5-0635) | **Stay influence** for affordability/moves; the proximity terms should switch to `getPower()` when §3.2 lands, since the :182 threshold is a Power threshold. |
| Card effects | `CardEffects.java` influence deltas, aftermath gains/losses | **Stay influence** (§4: every text says Influence). |

The split's honest summary: **only victory/threshold evaluation and
power-referencing card gating ever read Power**; the entire economy stays on
Influence. That is what makes this a small seam rather than an engine rewrite.

### 3.4 Where the protection is evaluated

The Negative Power predicate — "cannot affect any player whose power is lower
than his influence" — must be evaluated **at the moment the card's effect
applies**, as a target gate, and must not be re-checkable after the fact:

- Snapshot the predicate once, at application time:
  `canAffectTarget(sourceCard, targetPlayer)` returns
  `!(targetPlayer.getPower() < targetPlayer.getInfluence())` — i.e. protection
  applies exactly while the target's Power add-ons are negative enough to
  push Power below Influence.
- Placement: alongside the existing target-legality gates in
  `RulesEngine`/`GameController` (the same place `canJoinConflict`,
  `canPlayAftermath` and the participation gates live), not as a post-hoc
  rollback in the effect executor. Once the effect has applied, later
  influence changes do not retroactively void it — the rule protects a player
  from being *targeted*, not from having been affected.
- Only cards that "refer to only counting influence as power" consult the
  gate; a card whose text says plain "Influence" (i.e. every card in the
  pool today) never does.

## 4. Which cards could exercise any of this

Measured over both card files (829 records: 446 premiere + 383 deluxe):

- The word "power" appears in card **texts** **0 times** in either file
  (grep for text fields opening with a case-insensitive "power": 0 hits).
- It appears only in **8 titles**, all flavor, none mechanical:
  A Rising Power, Knowledge is Power, Power Politics, Affirmation of Power,
  Power Posturing, The Price of Power, Rise to Power (premiere; the first
  three title-deduped in deluxe per B5-0320). Read of each text: all effects
  are influence-vocabulary ("Reach 20 Influence", "gain 1 Influence",
  "loses 2 Influence", "+3 Leadership").
- **No card in the pool carries any stat that could serve as a Power value.**
  There is no Power key, no Power bonus grant, and no card text that would
  push a player's Power below or above his Influence.

Consequences, cleanly separated as the task demands:

- **Implementable with NO card-data change:** the §3.2 seam (computed
  `getPower()` + POWER bonus channel) and the §3.4 gate can land today and be
  conformance-tested with **synthetic fixtures** (granted POWER bonuses via
  the existing `StatBonus` factories, exactly as MJR/ORD sections build
  synthetic states). The engine would be rule-complete; the rule simply has no
  natural trigger.
- **Data-gated:** any *behavioural* Negative Power protection visible in play,
  and any Power add-on card, waits on a real card that references Power. That
  is not this wave's to unblock and requires a human IP-safe data decision
  before any text is authored — consistent with B5-0385/B5-0396 paraphrase
  discipline and the B5-0388 ruling that the authored pool is the design layer.

## 5. Call-site audit list (the full sweep to run when the seam lands)

`getInfluence()` occurs 50 times outside the station object and the test
suite, across 9 production files: `RulesEngine.java`, `GameController.java`
(indirect), `CardEffects.java`, `AIPlayer.java`, `Player.java`,
`AgendaCard.java`, `Babylon5Station.java` (station influence — separate
concept, untouched), `GameBoardPanel.java`, plus test probes
(`HeadlessConformanceTest.java` uses 80 sites, suite-only). Classification
when the seam lands: **RulesEngine victory/threshold lines → `getPower()`**;
**AIPlayer threshold-proximity terms → `getPower()`**; **everything else
stays**. Each switch must be individually justified in its slice against the
rulebook sentence it implements, because a blanket rename is how influence
semantics leak into economy paths.

## 6. Recommendation

**Do not implement now.** The rule is vacuous (§2), no card can trigger it
(§4), and the failure mode of a half-applied split — a Power read where the
rulebook says influence — is worse than the status quo. The deliverable of
value is this sizing: when a Power-bearing card is ever approved for the pool,
the implementation is a **two-piece seam** — computed `getPower()` over the
existing bonus channel, plus one target gate at the effect-application point —
with the call-site audit of §5 as its checklist. Record the influence-as-power
state as a *known equivalence under an empty distinction*, not as a bug: it is
rulebook-faithful until a card breaks the equivalence.

## 7. What this proposal does NOT do

No Power field was added anywhere; no model, engine, ai or ui code changed; no
card JSON changed; the influence-as-power state is recorded as a measured
fact, not flagged for repair. All numbers in §2, §4 and §5 were produced by
running greps over the tree this pass, not by citing prior reports.

---
*Reusable lesson: a rule whose precondition can never fire is not a backlog
item — size it, prove the equivalence it currently collapses, and file the
seam, so the next agent implements two pieces instead of re-deriving the
whole architecture.*
