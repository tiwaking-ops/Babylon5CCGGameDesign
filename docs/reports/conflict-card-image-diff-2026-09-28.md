---
document:
  title: "CONFLICT card-image diff — all 59 scan-backed titles, 2026-09-28"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0941"
---

# B5-0941 — CONFLICT face-vs-pool diff

Read-only transcribe-and-diff of every pool `type: CONFLICT` title that has a scan in
`investigations/card-images/sorted/`. Method and verdict vocabulary follow B5-0931.
No card JSON, no `b5ccg/src` and no git history touched.

Pools read: `b5ccg/resources/cards/premiere.json`, `b5ccg/resources/cards/deluxe.json`.
Scans: 59 GIFs, all legible at ~305x445.

## Headline

- **Batch is 59 titles, not the row's 56.** Resolved below; the row is an undercount, not a
  scope difference. 59 = 49 deluxe + 10 premiere-only.
- **CHANGED (rules text) 59 of 59.** Not one scan-backed CONFLICT face agrees with its pool
  text. The pool `text` field is a flat *"Winner gains N Influence / Loser loses N"* template
  family; the faces use a margin-threshold, state-creating, card-conversion vocabulary the pool
  does not contain.
- **Orb == pool `cost`: UNTESTABLE, 0 of 0.** No CONFLICT record in either pool carries a `cost`
  field, so the correspondence B5-0927/0931/0933 measured at full agreement has no pool side to
  compare against. **This generalises B5-0935's single-missing-cost finding into an entire
  uncosted type.** B5-0947 cannot record a cost-vs-orb verdict for CONFLICT without a backfill.
- **Type line: UNCHANGED 57 of 59, mismatched 2** — and both mismatches are the same root cause:
  two cards that are *dual-type* on the face and single-valued in the pool.
- **Rarity: UNVERIFIED 59 of 59.** No legible rarity marking at this scan resolution. Recorded as
  unverifiable, **not** as agreement.
- **`influenceReward` is present on 108/108 records and has a printed basis on 0/59.** The field
  is uniformly populated and uniformly unfounded; see "The template problem" below.

## The count: 56 vs 59 — resolved

The ledger row says "CONFLICT (56 titles)". The pool says 59. The 59 is correct and the row is a
low estimate written before the deluxe/premiere overlap was accounted for.

| Measure | Count |
| --- | --- |
| `type: CONFLICT` records, both pools | 108 |
| distinct CONFLICT titles | **59** |
| premiere CONFLICT records | 59 |
| deluxe CONFLICT records | 49 |
| titles present in **both** sets | 49 |
| titles present in **one** set only | 10 |
| scan files `*conf*` matching the pool | 62 |
| of those, `de_event_conf*` (false positives, not CONFLICTs) | 3 |
| **true CONFLICT scans** | **59** |

59 = 49 deluxe + 10 premiere-only, and the scan set matches 1:1. Zero pool CONFLICT ids lack a

## Type line: 57 UNCHANGED, 2 mismatched

All 57 single-type faces agree with the pool's `subtype` / `conflictType`, and the pool is
internally consistent — `CONFLICT_` + `conflictType` == `subtype` on **108/108** records, zero
exceptions.

The two mismatches are both dual-type cards:

| Card | Face type line | Face body | Pool `subtype` | Pool text |
| --- | --- | --- | --- | --- |
| **The Great Machine** | bare `Conflict` | "Initiates both a Diplomacy and a Military conflict" | `CONFLICT_DIPLOMACY` | "Any conflict type." |
| **Sleeper Personality** | bare `Conflict` | "Initiates both an Intrigue and a Psi conflict" | `CONFLICT_INTRIGUE` | "Intrigue/Psi combined conflict." |

The pool's *prose* half-acknowledges both ("Any conflict type", "Intrigue/Psi combined"), but
the *typed* half has no representation for a dual conflict: `subtype` and `conflictType` are both
single-valued, and the vocabulary is exactly `DIPLOMACY | INTRIGUE | MILITARY | PSI`. A card that
initiates two conflicts has nowhere to put the second one. This is a **schema gap, not a data
error** — it is not fixable by editing the two records.

Three further cards are typed correctly but have a cross-stat or stat-swap effect the type field
cannot express, so their `subtype` is a simplification rather than a mismatch: *Psi Interrogation*
(Intrigue conflict where "Psi may be used as Intrigue"), *Saber Rattling* (Diplomacy conflict
where "Military may be used to support or oppose this conflict"), *Dishonor* (same, Intrigue
conflict with Diplomacy support), *Test Their Mettle* (Diplomacy, Military may support on the
target's choice), *Neutrality Treaty* / *Trade Pact* / *Euphrates Treaty* / *Non-Aggression Pact*
(characters of both races "apply double diplomacy during resolution").

## Participation: 1 of 8 records faithful, 3 contradicted by the face

Eight CONFLICT records carry a `participation` object (7 distinct cards, Border Raid and
Complete Support in both sets). This is the most interesting non-text result, because here the
pool is *sometimes* better than its own text field — and sometimes invented.

**Faithful — Border Raid.** Face: *"Target another faction. Only the following cards can
participate in this conflict: One fleet from you and your target, and leaders for those fleets."*
Pool: `players: INITIATOR_TARGET`, `requiresTarget: true`, `cardTypes: [FLEET]`,
`perPlayerQuota: {FLEET: 1}`, `leadersIncluded: true`. Every element maps 1:1 onto the face,
including the non-obvious "and leaders for those fleets" clause. This is the single best
example in the type of a structured field transcribed from print, and it is evidence that the
`participation` schema *can* carry face truth when someone actually transcribed it.

**Contradicted by the face — Limited Strike.** Face: *"Target a player with whom you have a
tension of 3 or higher. **No other player may participate in the conflict.**"* Pool:
`cardTypes: [FLEET]`, `fleetSubtypes: [PICKET, COLONIAL, UTILITY]`, and the `text` field repeats
"Only Picket, Colonial, and Utility fleets may participate." The face restricts **players**; the
pool restricts **fleet classes**. These are different mechanics, and the pool's version has no
basis on this card.

**No face basis — three records:**

| Card | Pool `participation` | What the face actually says |
| --- | --- | --- |
| Complete Support | `mustTakeSide: true` | No such line. Face is "+1 Diplomacy to your race's characters; if this conflict succeeds by at least five times your unrest, alter one of your tensions". |
| Immortality Serum | `mustCommitAmbassador: true` (and `text`: "Both players must commit their Ambassador") | "**Any player may be supported in this conflict.**" — the face says the opposite of an ambassador lock. |
| The Great Machine | `allPlayersMustCommit: {CHARACTER, 1}` | "Any player may be supported in these conflicts." No mandatory commitment. |

`mustCommitAmbassador` is the sharpest contradiction: the pool asserts a restriction that the
face explicitly denies, in both the structured field and the prose. Anything implementing
participation from these three records is implementing behaviour no printed card authorises.

scan and zero scans lack a pool CONFLICT id. The two initially-suspected gaps (*Hunter, Prey*,
*Na'Ka'Leen Feeder*) were false negatives of my own punctuation-normalised filename match, not
missing scans — both are present as `de_conf_hunter_prey.gif` and `de_conf_nakalen_feeder.gif`.

## The template problem

Pool CONFLICT `text` is a closed template family. Replacing every digit with `#` and grouping:

- 49 titles collapse into a handful of repeated patterns, the largest being
  `"Diplomacy conflict. Winner gains # Influence."` (4 titles), with many further patterns
  appearing exactly twice — once per set, for the 49 dual-set titles.
- The recurring shapes are all flat: *Winner gains N*, *Loser loses N*, *Loser must discard N*,
  *may draw N*, plus a per-card trailing clause.

None of that vocabulary appears on the faces. The faces instead build effects out of:

**Margin thresholds — 22 of 59** use explicit win-by-N language (*"if you win by 5 or more"*,
*"for each 10 points by which this conflict succeeds"*, *"...5-9, or +2 if you win by 10 or more"*):
A Brighter Future, Affirmation of Power, Attacking Pawns, Bio-Weapon Discovery, Border Raid,
Compatible Goals, Court the Rebellious, Demonstrative Victory, Dishonor, Euphrates Treaty,
Free the Souls, Gunboat Diplomacy, Hate Crime, Limited Strike, Parliament of Dreams,
Precision Strike, Rally the People, Sleeper Personality, Stop Hostilities, Temptations,
Test Their Mettle. A 23rd, Complete Support, scales by *five times your unrest*, and
Consolidated Position likewise gates on `unrest x 5` — a threshold keyed to a per-player variable
rather than a constant.

The pool has no field for a margin at all: `influenceReward` is a single flat integer. So for
these 22 cards the pool is not a simplification of the face, it is a **different card**.

**State creation — 4 cards** create a persistent named state rather than resolving once:
Alliance (a state of Alliance between two races, mutual, with a war-triggered penalty),
Neutrality Treaty (state of Neutrality, mutual attack ban, cancellable in any draw round),
Non-Aggression Pact (state of Non-Aggression between two *targeted* races, ends automatically at
tension 4+), Trade Pact (state of Free Trade, granting each faction +1 influence rating).
The pool models none of these; each is a single flat reward in the data.

**Card conversion — 15 cards** turn the conflict into a different card type on success, which is
the mechanic the flat template is least able to express: A Brighter Future (Babylon 5
enhancement), Bio-Weapon Discovery (fleet enhancement, plus a per-round bioweapon token counter),
Compatible Goals (aftermath targeting your own ambassador), Consolidated Position (aftermath),
Demonstrative Victory (a *global* aftermath that caps the loser's per-turn influence), Dishonor
(aftermath on the target's ambassador reducing his abilities by 1 per 5 of margin), Forced
Impairment (an enhancement that blanks the target card's effect text), Hunter Prey, Muddy the
Waters, Court the Rebellious, Technological Espionage, Supplement Security, Temptations
(character enhancement), Free the Souls (aftermath banning a whole card type), and Raid Shipping
(an **additional Intrigue conflict** — the only face that chains a second conflict).

**Marks — 10 cards** use a Mark economy the pool never mentions: Doom Mark (Attacking Pawns,

## Verdict summary

| Field | UNCHANGED | CHANGED | UNVERIFIED |
| --- | --- | --- | --- |
| Rules text | 0 | **59** | — |
| Type line / conflict type | 57 | 2 (both dual-type, schema gap) | — |
| Rarity | — | — | **59** (no legible marking) |
| Orb == pool `cost` | — | — | **0 of 0** (no `cost` field exists) |
| `participation` vs face | 1 of 8 records | 3 contradicted, 4 absent where the face restricts | — |
| `influenceReward` | 0 | 59 (uniformly populated, no printed basis) | — |

## Data-layer integrity (independent of print)

These checks need no scan and all pass:

- duplicate ids: **0**
- `CONFLICT_` + `conflictType` == `subtype`: **108/108**, 0 mismatches
- `influenceReward` present: **108/108**
- `cost` present: **0/108**
- rarity distribution: COMMON 28, UNCOMMON 34, RARE 22, FIXED 24
- titles in both sets: 49; one-set-only: 10

So the CONFLICT records are internally tidy. The divergence is entirely between the pool and the
printed cards, and it is systematic rather than sporadic — which is the opposite signature from a
transcription-error sweep and is consistent with the pool being an independently authored
simplified layer rather than a corrupted transcription of print.

## Gates

`b5ccg\compile.bat` exit 0, `Build successful.` on javac 1.8.0_292 with `-source 1.6`
(11 lines of output; the `bootstrap class path not set` warning is pre-existing noise, not a
failure). No source or data file was modified in this task.

## Carried forward to B5-0947

1. CONFLICT needs a `cost` backfill before any cost-vs-orb verdict is possible. This is now the
   second whole type (after EVENT) with no `cost` coverage.
2. `subtype`/`conflictType` cannot represent dual conflicts; 2 cards are affected in CONFLICT and
   the same question will recur in every other type.
3. The "pool text is a flat template, print is not" pattern should be checked against the other
   seven batch reports before any ruling on whether the pool stays the authored design layer.
   If all nine batches show it, the finding is about the *design of the pool layer*, not about
   transcription quality, and that distinction matters for the B5-0388 reopen bar.
4. The `participation` object is the one place the pool demonstrably holds face-true structured
   data (Border Raid). It is worth checking whether that is a per-card transcription effort rather
   than a schema property, since the schema itself is fine and the data is not.

Black Market, Extreme Sanction, Sponsor Rebels, Kidnapping), Destiny Mark (Non-Aligned Support
and Rally the People require one to initiate; Free the Souls awards one), Shadow Mark
(Precision Strike requires 3, Shadow Assault requires three).

**Other face-only mechanics**: hidden agendas are revealed (Psi Interrogation), support totals
are distinguished from power (Campaign for Support, Na'Ka'Leen Feeder), fleets contribute
*opposition* rather than support (Sabotage), and war/tension state is read and rewritten
(Blockade, Condemn Deportations, Stop Hostilities, Peacekeeping, Affirmation of Peace).

Net: **0 of 59 faces are representable in the pool's CONFLICT schema as written.**


Note for the B5-0947 collation: 59 titles but 108 records, because 49 titles ship in both sets
with independent text. Batches that count *records* and batches that count *titles* will not
reconcile unless the unit is named explicitly.
