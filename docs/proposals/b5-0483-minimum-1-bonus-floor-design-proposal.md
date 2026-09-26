---
document:
  title: "B5-0483 — Minimum-1 bonus floor design (Censure-class printed floors vs the clamp-0 bonus layer)"
  status: "Proposal"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
---

# B5-0483 proposal — per-bonus floor vs read-path special case

Scope: proposal only, no src/ or resources/ edits. Grounded in the live tree
(2026-09-26): the B5-0357 bonus layer, the B5-0367 clamp-0 implementation, and
the B5-0473 explicit-target wiring this design must compose with.

## 1. Problem statement

Censure's printed text (both sets, `enh_censure` / `de_enh_censure`):
*"That fleet's Military value is reduced by 2 (minimum 1)."* The engine has
no way to express that floor:

* `Player.effectiveStat` (Player.java:320) returns `printedBase + sum(bonuses)`
  with no floor concept.
* The read sites clamp at 0 (FleetCard.java:58/69, CharacterCard.java:79-82/104)
  — the D10 floor-at-1 was DELIBERATELY killed by B5-0367 (Military-0 is
  legal and "reduce to exactly 0" is the intended generic semantics).
* So Censure on a Military-1 fleet yields 0 where the printed rule floors
  at 1. Today this is only observable through the B5-0473 explicit-target
  path (the pool's one Censure-class card); the generic path never sets a
  target, so the defect is latent, not live.

Constraint from the row: the default stays 0. Any design must be opt-in per
bonus and must not resurrect a global floor-at-1.

## 2. Design principles (inherited from B5-0357/B5-0367 decisions)

1. **Data-driven, not text-parsed** — floors must come from a structured
   field or a table keyed like CardEffects' existing tables, never from
   regexing card text.
2. **Read-path composition** — a floor is a property of the composed read,
   not of a single bonus (two −2 bonuses with different floors must compose
   deterministically), so it cannot live inside `StatBonus.delta` arithmetic
   alone.
3. **Clamp-0 stays the outer boundary** — a floor can never lift a value
   above the printed base, and damage still subtracts after floors (floors
   describe bonus-reduction outcomes, not damage immunity).
4. **Composition with the 0473 wiring** — the floor must be visible to the
   victim-registry read path (`FleetCard.getEffectiveMilitary` →
   `owner.effectiveStat`), i.e. the floor travels with the bonus that
   carries it.

## 3. Option A — per-bonus floor field (RECOMMENDED)

Add `public final int floor` to `StatBonus` (Java 6: an extra constructor
parameter with the two existing factories delegating `floor = 0`), plus a
third factory `attachedFloored(...)`.

Read path: `Player.effectiveStat` gains a floor pass after summation —
`int floored = printedBase + bonus; for (b : bonuses) if (appliesTo(b,...) &&
b.floor > 0) floored = Math.max(floored, Math.min(printedBase, b.floor));`

Semantics:

* Multiple floored bonuses: the HIGHEST floor wins (a fleet under two
  different Censures keeps the more protective floor — matches the printed
  per-card promise "minimum 1" applying to each attachment).
* Floor is capped at the printed base (`Math.min(printedBase, b.floor)`):
  a floor never turns a penalty into an enhancement above the printed value.
* Damage subtraction stays AFTER the floor (FleetCard.java:58 unchanged in
  order): floors apply to the bonus-reduced stat, damage still reduces to 0
  and triggers the B5-0368 neutralization path.
* Default 0 = no floor = today's behavior byte-identical (zero-cost
  invariance discipline, B5-0324/B5-0351 precedent).

Data plan: no JSON key needed yet (the pool has zero floored cards' worth of
structured data — the floor comes from Censure's text). Two delivery options
for the later slice: (a) an id-keyed engine table `BONUS_FLOORS` in
CardEffects (B5-0307/0395 precedent, zero model churn, table editable by
data tasks); or (b) a `floor` JSON key hydrated by DeckLoader (B5-0323 cost
precedent). Recommend (a) first — one card family, engine table suffices;
migrate to (b) only if a data task sources more floored cards.

Conformance hooks to specify in the later slice (name them now per the row):

1. Floor lifts a −2 on printed-1 fleet to exactly 1 (Censure shape).
2. Floor does NOT lift a −2 on printed-4 fleet (4−2=2 above floor 1).
3. Highest-floor-wins with two floored sources.
4. Floor capped at printed base (floor 5 on printed-1 stays 1).
5. Damage after floor: printed-1 fleet, floor 1, 1 damage token → 0.
6. Default-0 invariance: all existing suite sections green unchanged.
7. Composition with the 0473 wiring: floored bonus granted into the VICTIM
   registry reads correctly through `getEffectiveMilitary` (extends ENH-WIRE).
8. Character-path parity: `CharacterCard` read sites honor the same floor
   pass (for future Shunned-class floors), asserted on a fixture.

Risks: (i) `StatBonus` is immutable-final — every constructor call site
(neighbors: CardEffects ×6, suite fixtures) needs the default-0 overload,
small mechanical diff; (ii) AI scoring reads `effectiveStat`-shaped values
via cards — floors automatically visible, no ai/ change needed (B5-0367
migration map precedent); (iii) floor interplay with Psi-from-zero: floors
are irrelevant to Psi unlock (floor > 0 on a Psi bonus does not unlock
anything — `psiFromZero` stays the only unlock), spell this out in the
slice's javadoc.

## 4. Option B — read-path special case (REJECTED for now)

Hard-code `enh_censure`/`de_enh_censure` in `Player.effectiveStat` or
FleetCard: cheapest diff, but (i) puts card identity inside the generic read
path (the exact anti-pattern B5-0307's id-table design removed from
GameController), (ii) does not generalize to Shunned-class character floors
or future data, (iii) composes badly — a second floored card needs another
if-branch with hand-rolled highest-wins logic. Viable only as a throwaway
demo, not as the standing mechanism.

## 5. Recommendation

Option A, delivered as one later slice: StatBonus floor field + default-0
factory overload + Player.effectiveStat floor pass + the 8 conformance hooks
above + an id-keyed `BONUS_FLOORS` table entry for Censure's two ids.
Sequencing: after the B5-0482 probe determinism fix lands (different file,
but the slice touches Player.java which the 0468/0469/0473 lineage just
stabilized — one writer per window keeps the registry lineage clean).

Unblocks: closes the B5-0477 gap; makes the Censure control-reference note
in docs/playtest-guide.md §7 (B5-0475) rescindable; the B5-0454 human
decision brief needs no update (rulings 2+3 untouched).

## Reusable lesson

Latent-rule gaps found during research get their design NOW and their code
LATER — recording the option space, the constraint set, and named conformance
hooks at discovery time halves the later slice's analysis work (filed under
`.agent/PATTERNS/Buffy (unknown)/`).
