---
document:
  title: "Design proposal — D10/D11: a stat bonus layer with expiry, blanking, cumulative stacking, and the Psi-from-zero rule"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  created_date: "2026-09-23"
  last_modified_by_llm: {name: "Buffy", version: "deepseek-v4-flash"}
  last_modified_date: "2026-09-23"
---

# D10/D11 design proposal — stat bonus layer with expiry

B5-0357, proposal-only. No `b5ccg/src/` or `b5ccg/resources/` file is touched
by this task. Source findings: B5-0203 audit deviations **D10** and **D11**;
rulebook §Events ("lasts only until the end of the current turn"), §Psi note,
glossary "cumulative"; cross-references: B5-0342 (D9 influence split —
influence is explicitly **not** part of this layer), B5-0341 (D6), B5-0345
(Tier 3 damage subsystem is designed **with** this layer and depends on it),
B5-0337/B5-0339 (existing computed read-time overlays that this layer must
compose with).

## 1. Problem statement

The audit's D10/D11 verdicts, still true today:

* `FleetCard.applyMilitaryDelta` floors at 1 (`military = Math.max(1, …)`)
  — no rulebook justification (locations print Military 0; reductions exist
  in card text) — and **mutates the printed value permanently**.
* `CharacterCard.applyStatDelta` floors every stat at 0 and likewise mutates
  the printed value. Nothing distinguishes "raises Psi" from "specifically
  increases Psi", so the rulebook's Psi-from-zero rule is silently violated.
* Both methods were written when enhancements had no effect dispatch at all
  (D11: "zero callers"). Since then `engine/CardEffects.java` routes real
  bonuses through them — so the permanent-mutation semantics are now **live
  behavior**, not a latent gap.

Current mutation-site census (grep over `b5ccg/src/`):

| Site | Effect today |
|---|---|
| `CardEffects.applyPlayEnhancement` → `_FLEET` | `target.applyMilitaryDelta(delta)` on the strongest own fleet |
| `CardEffects.applyPlayEnhancement` → `_CHARACTER` | `target.applyStatDelta(...)` on the strongest own character |
| `CardEffects.applyPlayEnhancement` → `_FACTION` | all own fleets `applyMilitaryDelta(mil)` |
| `CardEffects.applyAgendaOnPlay` | all own fleets `applyMilitaryDelta(1)` |

Why mutation is now actively harmful:

1. **No un-apply.** The printed value is destroyed the moment a bonus
   lands. Detach-on-discard, blanking, and any future "until end of turn"
   event cannot restore the base. The layer can only add, never subtract
   correctly.
2. **Double-count with computed overlays.** B5-0337 (fleet leader's
   Leadership rides `getEffectiveMilitary()`) and B5-0339 (assistant
   `+1` computed in `getPrimaryStatValue`) deliberately avoid mutation.
   Once a mutated base feeds those read paths, the same +1 can be counted
   twice (once baked into `military`, once as an overlay).
3. **Floors are wrong.** The 0/1 floors change printed semantics instead of
   clamping an *effective* value, and the character floor hides negative
   penalties the rulebook allows.
4. **Psi-from-zero is unenforceable.** With mutation there is no base left
   to test "raised from 0".

## 2. Rulebook specification (normative excerpts driving the design)

* **Durations.** Events "last only until the end of the current turn",
  "until the beginning of your next turn", or "while this card is in play".
  A bonus's lifetime must therefore be expressible per-source.
* **Cumulative.** The glossary's "cumulative" marks effects that stack with
  each other; non-cumulative same-named effects replace rather than add.
* **Blanking.** "all text is blank"-style effects turn off card *text*
  abilities; printed stats remain. Blanking must remove bonuses **sourced**
  from the blanked card while keeping its own stat contribution intact.
* **Psi.** "Psi cannot be raised from a base of 0 unless a card specifically
  increases Psi." Two bonus classes exist: generic (`+1 Psi`) and
  Psi-specific (`specifically increases Psi`).
* **Out of scope.** Influence is not a stat here — D9/B5-0342 owns the
  rating-vs-applied pool. Damage is a *consumer* of the layer (B5-0345
  Tier 3), not part of it. Face-down/neutralized stat suppression already
  lives in `getPrimaryStatValue` read paths and stays there.

## 3. Design

### 3.1 Value object `StatBonus` (model/)

Java 6 plain final class (no lambdas/streams; enums are fine):

```
StatBonus {
  String    sourceCardId;   // card that granted it ("" for engine-granted)
  StatKey   stat;           // DIPLOMACY | INTRIGUE | PSI | LEADERSHIP | MILITARY
  int       delta;          // may be negative (penalties)
  Scope     scope;          // ATTACHED(targetCardId) | FACTION(ownerPlayerName, statFilter)
  Expiry    expiry;         // WHILE_IN_PLAY | END_OF_TURN | START_OF_NEXT_OWNER_TURN | ON_EVENT(key)
  boolean   psiFromZero;    // true only for "specifically increases Psi" cards
  boolean   cumulative;     // false = same-source replacement semantics
  int       createdRound;   // GameState.getRoundNumber() at grant
}
```

`StatKey`/`Scope`/`Expiry` are small enums. `ON_EVENT(key)` defers custom
expiry (e.g. "until a Shadow Mark is purged") to explicit engine calls;
nothing grants it until such effects exist.

### 3.2 Registry, not per-card storage

Each `Player` holds a `List<StatBonus>` (its own faction-wide bonuses plus
those it granted to attached cards). One registry per player keeps a single
source of truth, makes the round-boundary expiry sweep O(bonuses), and keeps
discard/blanking cleanup to `removeBySource(cardId)`. Per-card storage was
considered and rejected: faction-scope bonuses would have to be duplicated
onto every current/future card, and blanking would need back-references.

Read path (the single formula, composing the existing overlays):

```
effective(card, stat) =
    printedBase(card, stat)                      // raw field, never mutated
  + sum(bonuses: scope matches card, stat matches, source not blanked/left play)
  + computedOverlay(card, stat)                  // B5-0337 leader, B5-0339 assistant — unchanged
```

* **Final clamp only.** Effective values floor at 0 (Military included —
  the D10 floor-at-1 dies; a Military-0 fleet becomes legal, which the
  audit itself notes is correct). Negative *penalties* may push the
  effective value to the floor; the base never moves.
* **Psi-from-zero.** If `printedBase(Psi) == 0`: generic Psi bonuses
  contribute 0 unless at least one `psiFromZero=true` bonus is present, in
  which case all Psi bonuses apply (recorded interpretation: the specific
  card "unlocks" the stat; mixed stacking after unlock is the plain sum).
  If base > 0, all bonuses apply.
* **Stacking.** Bonuses with `cumulative=true` all add. Same
  `sourceCardId` non-cumulative re-grants replace (remove previous entry
  for that source).

### 3.3 Expiry sweep

`GameState.advanceRound()` already increments `roundNumber` and is the one
round boundary. It calls `sweepExpiries(roundNumber)` on each player's
registry: `END_OF_TURN` bonuses whose owner's turn has passed are removed;
`START_OF_NEXT_OWNER_TURN` likewise. Per-owner turn identity comes from the
existing `currentPlayerIndex` rotation — the implementer stores the owner
reference on the bonus (already in `Scope`) and compares round parity, no
new state machine needed. Leaving play (discard/deck-out/blanking) removes
bonuses by `sourceCardId` immediately.

### 3.4 Migration map (every existing writer/reader)

| Current code | Becomes |
|---|---|
| `CharacterCard.applyStatDelta` callers (CardEffects `_CHARACTER`) | `registry.add(StatBonus(permanent, ATTACHED, …))` per JSON bonus field; method deleted from model after last caller moves |
| `FleetCard.applyMilitaryDelta` callers (`_FLEET`, `_FACTION`, agenda) | same, `MILITARY` key; `_FACTION`/agenda bonuses are `FACTION`-scope, target-free |
| Enhancement/agenda discard or replacement | `registry.removeBySource(cardId)` |
| `getEffectiveMilitary()` (B5-0337) | unchanged; composes as `computedOverlay` in the formula |
| `getPrimaryStatValue()` assistant bonus (B5-0339) | unchanged; same composition |
| `Conflict.playerTotal` / `Player.conflictTotal` consumers | read `effective(...)` instead of raw getters (single call-site change each) |
| `AIPlayer` scoring reads of `getMilitary()/getDiplomacy()/…` | switch to effective reads so AI values what players experience |

Behavioral delta, deliberate and called out: **the D10 Military floor of 1
disappears** and previously-baked permanent bonuses (any game in progress
across the migration commit) reset to printed bases plus registry entries —
acceptable at this stage (no persistence exists).

### 3.5 Phasing (each phase independently green)

* **A. Introduce the layer.** New model classes + read-path integration +
  registry entries created at the existing CardEffects call sites. No
  consumer-visible change except the D10 floor fix. Gate: existing
  conformance sections re-run green.
* **B. Remove mutation.** Delete `applyStatDelta`/`applyMilitaryDelta`
  bodies' callers; printed fields become `final`-by-discipline. Gate:
  grep proves zero mutation callers remain.
* **C. Expiry consumers.** First `END_OF_TURN` grants (engine event table
  gains duration metadata); sweep wired and unit-tested.
* **D. Damage subsystem (B5-0345 Tier 3).** Damage/repair/heal read and
  reduce via the effective layer; attack/heal become reachable.

## 4. Test plan (for the implementing task, not this one)

Conformance additions in the existing `HeadlessConformanceTest` pattern:

1. attach +2 Military → effective reads base+2; detach restores exactly
   the printed base (no residual).
2. two cumulative fleet bonuses stack (base +2 +1).
3. blanking the source card removes its bonuses; the card's own stats
   still count in the sum.
4. Psi-from-zero: generic-only grant leaves effective Psi 0; specific
   grant raises it; specific + generic together sum.
5. `END_OF_TURN` bonus disappears after the owner's next round start;
   `WHILE_IN_PLAY` survives.
6. penalties can floor an effective Military at 0 (and 0 is legal in
   conflict totals) — pinning the D10 fix.
7. leader/assistant overlays still contribute exactly once (no
   double-count vs mutated bases — impossible now by construction, pinned
   by test).

## 5. Risks

* **AI divergence.** If any `AIPlayer` read misses the switch to effective
  values, AI decisions quietly ignore bonuses. Mitigation: §4.7-style
  assertion plus a grep audit of raw stat getters in `ai/` during the
  implementing task.
* **Sweep timing bugs** (bonus expiring one turn early/late) — the most
  likely real defect class. Mitigation: §4.5 pins both directions.
* **Scope creep into influence.** Influence stays out (D9 owns it); the
  `StatKey` enum deliberately has no INFLUENCE member.

## 6. Explicitly unchanged

`Player.influence` pool semantics (B5-0342), conflict sides (B5-0309),
aftermath eligibility (B5-0338), the UI readouts (B5-0346/0347) — all
consume stats through the same read paths listed in §3.4 and need no
changes under this proposal.
