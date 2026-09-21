---
document:
  title: "Data proposal — conflict participation restrictions field (card-specific)"
  status: "Proposal"
provenance:
  author_llm: {name: "big-pickle", version: "opencode/big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "big-pickle", version: "opencode/big-pickle"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# Data proposal — conflict participation restrictions field

Status: **Proposal** — candidate, not truth. Per `AGENTS.md` §3, proposals are
"never truth until merged + compiled"; this document proposes the data-side
schema only. The engine-side enforcement is a separate task (see §8).

## 1. What this proposal is (and is not)

**This is a data-schema + data-population proposal for conflict-card
participation restrictions.** It defines a `participation` field on conflict
cards in `b5ccg/resources/cards/` so that card-specific rules like Border
Raid's "only one fleet from you and your target, and leaders for those fleets,
may participate" are machine-readable:

* The general rulebook default — **any** player may support or oppose **any**
  conflict (§IV Conflict Cards; §V "ACTION: Support or Oppose a Conflict") —
  is correct and stays. Restriction is **card-text-specific**; only restricted
  cards get the field. Absent field = open participation = current behavior.
* **No `src/` change.** The engine task that reads and enforces this field is
  explicitly out of scope here and recorded as a dependency (§8). No JSON files
  are edited by this proposal; it defines the target schema and the per-card
  values to apply.

This proposal exists because the previous discussion established the gap:

* `b5ccg/src/b5ccg/model/Conflict.java` tracks `supporters`/`opposers` as
  per-player sets and commits any card (`commitCard`) with **no eligibility
  check** — participation is open for every conflict.
* The real Border Raid card (user-supplied text, 2026-09-21 session) restricts
  who may join at all — a rule our data cannot currently express.

## 2. Exact rules this field must support

### 2.1 The default (do not encode — absence means open)

Rulebook §IV "Conflict Cards":

> Characters and fleets may rotate to either support or oppose a given conflict
> during the action round with their abilities.

Rulebook §V "ACTION: Support or Oppose a Conflict":

> Characters and fleets (and, in some cases, locations) may rotate to either
> support or oppose a conflict that has been initiated ... Only cards with a
> non-zero ability of the appropriate type may rotate to support or oppose a
> conflict.

So the general rule is: any ready card with the right ability type, any player.
That is already the engine's behavior and the correct default.

### 2.2 The exceptions — card-text restrictions (what this field encodes)

Restrictions come from the **card's own text**. Observed forms in our data set:

**a. Restricted participant set + quota** — Border Raid (real card, premiere
`conf_border_raid`):

> Military Conflict. Target another faction. Only the following cards can
> participate in this conflict: One fleet from you and your target, and leaders
> for those fleets. If this conflict is uncontested, the target loses
> 1 influence. If you win by 5 or more, gain +1 influence. Tensions between
> your two races increase by 1.

This restricts (i) who may join (initiator + target only), (ii) what may join
(one fleet per side + the characters leading those fleets), and (iii) calls for
a **target declaration** at initiation. (Also carries outcome clauses —
uncontested / win-by-5 / tension — which are *outcome* rules, not
participation rules; recorded in §7.4 as out of scope for the field.)

**b. Restricted card kinds** — Limited Strike (premiere `conf_limited_strike`):

> Only Picket, Colonial, and Utility fleets may participate.

**c. Mandatory commitment** — Immortality Serum (premiere
`conf_immortality_serum`):

> Both players must commit their Ambassador.

— and The Great Machine (premiere `conf_the_great_machine`):

> All players must commit at least one character.

**d. Mandatory participation by all others** — Complete Support (premiere
`conf_complete_support`):

> All other players must either support or oppose.

**e. Expands eligibility (not a restriction)** — Non-Aligned Support (`conf_non_aligned_support`): "A Non-Aligned fleet may join as a free participant." This **widens** the pool; it does not restrict anyone already eligible.

**f. Rulebook-generated war conflicts** — §War: a war conflict may target a
race as a whole or a specific location; whole-race resolution depends on
"all participants in the conflict must have supported" (§811 uncontested test).
War conflicts are not JSON cards; their participation rule is engine-scoped and
out of this proposal's data scope (see §8).

## 3. Proposed JSON schema extension

### 3.1 Field name, placement, type

- Field name: **`participation`** (matches "participant" vocabulary already
  used by the engine: `addParticipant`, `MILITARY_PARTICIPANT`, etc.).
- Placement: **top-level key on `type: "CONFLICT"` cards only**, optional.
- Type: **object** (null/absent = open participation — the default).

### 3.2 Member keys and semantics

All keys optional; omitted key = "no special rule for this dimension".

| Key | Type | Meaning | Default |
|---|---|---|---|
| `players` | string | Who may commit cards to this conflict: `"ALL"` (any player, the rulebook default), `"INITIATOR_TARGET"` (only the initiator and the declared target), `"INITIATOR"`, `"TARGET"` | `"ALL"` |
| `requiresTarget` | boolean | A target must be declared at initiation (Conflict round step 2, rulebook §III) — Border Raid's "Target another faction". When `true`, conflict initiation is illegal without a target. | `false` |
| `cardTypes` | array of string | Card kinds allowed to participate (superset filter): values from `CHARACTER`, `FLEET`, `LOCATION`, `ENHANCEMENT`, `GROUP`, `AMBASSADOR`. Empty/absent = all ready cards eligible by the type-ability rules. | `[]` |
| `fleetSubtypes` | array of string | When `cardTypes` includes `FLEET`: fleet classes allowed (Picket, Colonial, Utility, ...). Absent = all fleet classes. Consumes the `fleetClass` field on candidate `FLEET` cards (§3.6). Example: `["PICKET","COLONIAL","UTILITY"]` for Limited Strike. | `[]` |
| `perPlayerQuota` | object | Max number of committed cards per player per card kind. Keys are the `cardTypes` values. Border Raid: `{"FLEET": 1}`. Absent = no quota. | `{}` |
| `leadersIncluded` | boolean | When true, characters leading an allowed fleet may participate alongside that fleet (Border Raid's "and leaders for those fleets"). Only meaningful with `cardTypes` containing `FLEET`. | `false` |
| `mustCommitAmbassador` | boolean | The eligible players must commit their ambassador to the conflict (Immortality Serum: "Both players must commit their Ambassador"). | `false` |
| `allPlayersMustCommit` | object | Every player must commit cards of a specific kind. Shape: `{"cardType": "CHARACTER", "count": 1}`. Used by The Great Machine ("All players must commit at least one character"). | `null` |
| `mustTakeSide` | boolean | Every other player must commit cards of any kind (support or oppose), no card-kind requirement. Used by Complete Support ("all other players must either support or oppose"). Distinct from `allPlayersMustCommit` because it constrains *whether* you join, not *what kind* you join with. | `false` |

### 3.3 Example shape (proposal only — no edit)

Border Raid (premiere `conf_border_raid`) would gain:

```json
{"id":"conf_border_raid","title":"Border Raid","type":"CONFLICT",
 "subtype":"CONFLICT_MILITARY", ...,
 "conflictType":"MILITARY","influenceReward":2,
 "participation":{
   "players":"INITIATOR_TARGET",
   "requiresTarget":true,
   "cardTypes":["FLEET"],
   "perPlayerQuota":{"FLEET":1},
   "leadersIncluded":true}}
```

Limited Strike:

```json
"participation":{"cardTypes":["FLEET"],"fleetSubtypes":["PICKET","COLONIAL","UTILITY"]}
```

Immortality Serum:

```json
"participation":{"mustCommitAmbassador":true}
```

The Great Machine:

```json
"participation":{"allPlayersMustCommit":{"cardType":"CHARACTER","count":1}}
```

Complete Support:

```json
"participation":{"mustTakeSide":true}
```

### 3.4 Backward compatibility

`participation` is an **optional top-level key**. Cards without it (all current
cards) load exactly as today — open participation, no target requirement. The
field is an additive, non-breaking schema extension in the same spirit as the
proposed `cost` field (B5-0315). **No loader change is required to keep current
cards loading**; a future reader simply treats absent/`null` as the defaults in
§3.2.

### 3.5 Deluxe handling

`deluxe.json` mirrors premiere cards with deluxe text changes. Proposal:
**each deluxe conflict card gets its own `participation` decided from its own
printed text**, never auto-copied from its premiere counterpart (a deluxe text
change can alter participation, e.g. deluxe Border Raid's "seize control of a
contested Location" changes outcomes, not participants). Currently only
`de_conf_border_raid` in deluxe carries a participation restriction (same
Premiere restriction — deluxe has no Limited Strike, Immortality Serum, The
Great Machine, or Complete Support equivalents in the set).

Resolved (human, 2026-09-21, Q5): **deluxe Border Raid's participation is
confirmed unchanged from premiere** before the data edit.

### 3.6 Companion field: `fleetClass` on `FLEET` cards

Resolved (human, 2026-09-21, Q1): add a `fleetClass` field. `fleetSubtypes`
(§3.2) references fleet classes, so a companion field must exist on the cards
being filtered:

- Field name: **`fleetClass`**, top-level key on `type: "FLEET"` cards only,
  optional.
- Type: **string**, closed vocabulary of the game's fleet classes (e.g.
  `"PICKET"`, `"COLONIAL"`, `"UTILITY"`, `"FIGHTER"`, `"FRIGATE"`,
  `"DESTROYER"`).
- Absent/`null` = fleet class unknown; a fleet without a class is excluded from
  any conflict that filters by `fleetSubtypes` (cannot be proven eligible), and
  is otherwise unaffected.
- This is a **second, smaller data-edit task**: populate `fleetClass` on the
  fleet cards that Limited Strike's filter must discriminate. It does not change
  the `participation` schema; it is a prerequisite data source for
  `fleetSubtypes`.

Where the class currently lives in the model/JSON (if anywhere) was not
confirmed in this pass; the field is additive either way.

## 4. Affected cards and proposed values (data population)

Restriction-bearing conflict cards found in the current data (scope: the four
"restrict/require/mandate participation" forms in §2.2). Values below are
proposals to apply when the data edit is authorized; each cites its evidence.

| id | set | rule form | proposed `participation` | evidence |
|---|---|---|---|---|
| `conf_border_raid` | PREMIERE | a (set+quota+target) | `players:INITIATOR_TARGET`, `requiresTarget:true`, `cardTypes:[FLEET]`, `perPlayerQuota:{FLEET:1}`, `leadersIncluded:true` | real card text, user-supplied 2026-09-21; header clause only (outcome clauses, §7.4) |
| `de_conf_border_raid` | DELUXE | a | same as premiere (deluxe change affects outcome clause, not participants) | same source; deluxe text-change note in data |
| `conf_limited_strike` | PREMIERE | b (card kinds) | `cardTypes:[FLEET]`, `fleetSubtypes:[PICKET,COLONIAL,UTILITY]` | own card text in `premiere.json:215` ("Only Picket, Colonial, and Utility fleets may participate.") |
| `conf_immortality_serum` | PREMIERE | c (mandatory) | `mustCommitAmbassador:true` | own card text in `premiere.json:177` ("Both players must commit their Ambassador.") |
| `conf_the_great_machine` | PREMIERE | c (mandatory) | `allPlayersMustCommit:{cardType:CHARACTER,count:1}` | own card text in `premiere.json:222` ("All players must commit at least one character.") |
| `conf_complete_support` | PREMIERE | d (mandatory) | `mustTakeSide:true` | own card text in `premiere.json:170` ("All other players must either support or oppose.") |

Explicitly **not** given the field (verified grep, 2026-09-21):

* `conf_non_aligned_support` — widens eligibility ("may join as a free
  participant"); nobody who is already eligible is excluded. Recording it
  would need a "freeParticipant" concept (fee waiver), which is an *economy*
  rule, not a participation restriction — flag for the engine task, no field.
  The free-participant ruling is stored advisory in
  `docs/reports/perplexity-non-aligned-support-free-participant-ruling-2026-09-21.md`
  (assessed 2026-09-21: consistent with rulebook glossary "Free",
  `BABYLON5_CCG_RULEBOOK.md:1154`; resolves proposal Q4 as "keep as text").
* Level the Playing Field (b5ccg EVT, 2026-09-21) — an event that lets a
  player participate in **any** conflict using **any** ability; that is a
  per-turn *expansion* of eligibility on the participant (not the conflict),
  not a conflict-card restriction. Not expressible in this field's vocabulary
  (which lives on conflict cards); it needs event-level handling by the engine
  task, not a `participation` value. Quota note (human, Q3): Border Raid's
  "one fleet from you and your target" is the only per-player quota in the
  data; no per-side or per-conflict quota has been observed and the vocabulary
  should not invent one.
* `conf_saber_rattling` — commits fleets but as a stat-modifier (Military added
  to Diplomacy total), not a participation set change. No field.
* Outcome clause on Border Raid alone is not a participation rule; the rest of
  the affected real-card clauses (§7.4) are outcome semantics.

## 5. Schema vocabulary constraints (to keep the field tiny)

1. The vocabulary in §3.2 is intentionally small. Each key maps to one
   engine-checkable predicate; no free-text rule language. Anything not
   expressible in the vocabulary **must not** be shoehorned in — leave the card
   without a field and record the text-level limitation.
2. `players` and `allPlayersMustCommit.cardType` accept only the closed
   value sets listed; `fleetClass` accepts only the closed fleet-class set
   (§3.6); `mustTakeSide` is a boolean. Unknown values are data errors for the
   future reader to reject loudly.
3. A card with `players` ≠ `"ALL"` whose conflict name implies a target (e.g.
   "Target another faction") is expected to also carry `requiresTarget:true`.
   The reader may warn if inconsistent, but the proposal does not require
   coercion.
4. A card may not carry both `allPlayersMustCommit` and `mustTakeSide`; they
   are alternatives (kind-specific mandate vs any-kind mandate), not combinable.
   The reader may warn if both are present.

## 6. IP-safe constraint

B5 IP-safe rules forbid copying card text, images, or lore dumps (established
in B5-0315 §6). The `participation` object is **structured data, not text** —
it encodes rules in the closed vocabulary. The `text` field on affected cards
may be refreshed to **original paraphrases** (the project's standing practice —
the current `text` entries are already paraphrases) so humans see the
restriction at a glance; verbatim printed-card text must not be copied. The
Border Raid header clause used in this proposal is the user-supplied summary
from the 2026-09-21 session, not a verbatim copy.

## 7. Interaction with prior findings

### 7.1 General rule stands (Q from the human session)

Confirmed: the rulebook's "every other player may rotate characters/fleets to
support or oppose" (§III/§IV/§V) is the general rule and remains the default.
Border Raid's two-player conflict is **card-specific**, not general.

### 7.2 D14 sides work still applies

The B5-0309 side/total logic and the "initiator wins iff support > opposition"
rule are unaffected. For a restricted conflict, the eligible players commit to
either side; totals compare as today. The field only bounds *who/what* may
commit, not how resolution compares.

### 7.3 F1/F2 UI dependency (B5-0310)

`requiresTarget:true` makes the F2 target selection gap (auto-target in
`MainWindow.playSelected()`) an engine/data-enforced requirement for the
affected cards. This proposal records the dependency; the UI task (Q2-A in the
human report) is separate.

### 7.4 Out of scope for THIS field (outcome, not participation)

Border Raid's full printed rule also has: uncontested → target loses 1
(`uncontested` is already defined, rulebook glossary; §811 uses the same test),
win-by-5 → +1 (a margin-based reward), and tensions +1 (§813). These are
**resolution/outcome** semantics with their own consumers (aftermath eligibility
already keys on initiator Won/Lost; tension state does not exist in the model).
They are recorded here so the engine task does not confuse them with
participation, but this field does not carry them.

## 8. What this proposal recommends doing now / not now

### 8.1 Recommend: accept the schema (§3) as the target data shape

Add `participation` as optional top-level object on `CONFLICT` cards with the
§3.2 vocabulary. Backward-compatible: absent = open participation = today.

### 8.2 Recommend: populate the five premiere + one deluxe card rows in §4

Values are evidence-cited; the largest is Border Raid (needs `requiresTarget`
+ set/quota/leaders). This is a data-edit task (`b5ccg/resources/cards/`), kept
separate from the schema acceptance so each is independently reviewable. Data
edits in this repo require overseer authorization for multi-file changes (see
B5-0317/B5-0318 precedent for single-field fixes). The companion `fleetClass`
population on `FLEET` cards (§3.6) is a second, smaller data-edit task,
prerequisite only to Limited Strike's `fleetSubtypes` discrimination.

### 8.3 Recommend: do NOT build the engine enforcement here

The reader/enforcement task (make `Conflict.commitCard` /
`addParticipant` honor `participation`, enforce `requiresTarget` at
initiation, apply quota/leaders/ambassador/mandate predicates) is a distinct
`engine/` + `model/` task ("Option 2" from the 2026-09-21 session). It should
be seeded after this data proposal is accepted, and it owns the §7.4 outcome
clauses and the rulebook war-conflict participation rules (which are not
JSON-backed).

### 8.4 Recommend: do NOT add free-text rule parsing

The closed vocabulary (§5.1) is the boundary. A card whose restriction cannot
be expressed is left unfiled rather than given a text-language field.

## 9. Resolved questions (human rulings, 2026-09-21)

1. **Fleet subtype vocabulary (Q1).** RESOLVED: add a `fleetClass` field —
   the companion data field on `FLEET` cards defined in §3.6. The proposal
   carries it; whether fleet class already lives in the model/JSON today was
   not confirmed in this pass (§3.6) and remains an implementation check for
   the data-edit task.
2. **`"ANY"` in `allPlayersMustCommit.cardType` (Q2).** RESOLVED: use a
   separate **`mustTakeSide` boolean** instead (Complete Support, §3.2/§4).
   `allPlayersMustCommit` is now kind-specific only.
3. **Quota scope (Q3).** RESOLVED: Border Raid is the **only** one-fleet-per-
   player restriction in the data; quota is per-player, matching the card
   text. No per-side or per-conflict quota is known, and the vocabulary is not
   extended for one. Level the Playing Field (co-participate in **any**
   conflict with **any** ability) is an event-level eligibility *expansion*,
   not a conflict-side restriction: recorded in §4 as not expressible in this
   field, to be handled by the engine task as an event-level waiver, not by a
   conflict-card value.
4. **`freeParticipant` (Q4).** RESOLVED: keep as text (no field). Ruling
   assessed & stored advisory at
   `docs/reports/perplexity-non-aligned-support-free-participant-ruling-2026-09-21.md`;
   confirms the "free" = no influence cost / no sponsor rotation reading, in
   line with the rulebook glossary "Free" (§2.2e/§4). A cost/waiver field
   belongs to the economy modeling task (B5-0315 orbit), not this schema.
5. **Deluxe divergence (Q5).** RESOLVED: deluxe Border Raid's participation is
   unchanged from premiere; deluxe per-card text governs, never auto-copied.
6. **Premiere card pool (human ruling, 2026-09-21).** The card pool defaults to
   **no Premiere** cards; the game exposes an mode toggle **"No Premiere" vs
   "Removed Duplicates"** (remove only premiere cards that have a deluxe
   counterpart). Scope note for the data-edit task: under the default and under
   "Removed Duplicates", the deluxe `de_conf_border_raid` row is live; the
   premiere-only restriction rows (§4) are live only when premiere cards are in
   the pool (i.e. neither the default nor the duplicates-removed mode selects
   them). This ruling is a card-pool decision, not a field decision; it affects
   which rows the data task *must* populate first.

## 10. Verification note

Proposal-only: no `b5ccg/src/` or `b5ccg/resources/` file edited; build gate
not re-run for this task. The 2026-09-21 session gate was green before this
proposal (36 files, `-source 6`, smoke exit 0). Line references for the
affected cards verified by grep against the working tree on this date.