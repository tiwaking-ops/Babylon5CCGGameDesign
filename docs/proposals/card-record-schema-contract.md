---
document:
  title: "Card-record schema contract — type-to-fields table, v1 (proposal, B5-1025)"
  status: "Proposal (not truth until merged; BABYLON5_CCG_RULEBOOK.md and governance files win on conflict)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1025"
---

# Card-record schema contract (proposal)

Grounded in a measured census of all 829 records (instrument: `json.loads` on
utf-8-strict bytes, 2026-09-29T09:22Z) cross-read against
`DeckLoader.buildCard` / `parseCards`. This row is documentation and a ruling
proposal, **not** a validation implementation.

## 1. The observed shapes, reconciled

The row's "25 shapes" is a per-file count: **12 distinct field-sets in
premiere + 13 in deluxe = 25**, but the sets overlap across files; globally
there are **13 distinct shapes**, and they are **per card type**, not per file:

| Type | Records | Shapes | Field-sets observed |
|---|---|---|---|
| AFTERMATH | 117 | 1 | base + `triggerCondition` |
| AGENDA | 47 | 1 | base + `isMajorAgenda` + `winCondition` |
| CHARACTER | 161 | 2 | base + 4 stats + `isAmbassador` (+`cost` on 149; **without cost on 12**) |
| CONFLICT | 108 | 2 | base + `conflictType` + `influenceReward` (+`participation` on the 7 known records) |
| ENHANCEMENT | 78 | 2 | base + 5 bonus fields (+`cost` on 76; **without cost on 2**) |
| EVENT | 166 | 2 | base (165) + **one `timing` carrier** |
| FLEET | 80 | 1 | base + `cost` + `military` + `fleetClass` |
| GROUP | 51 | 1 | base + `cost` |
| LOCATION | 21 | 1 | base + `cost` + `influencePerRound` |

(base = `faction, id, imageKey, rarity, set, subtype, text, title, type` —
present on every record of every type.)

## 2. The contract (proposed required / optional / forbidden, per type)

**Common to every type** — required: `id`, `title`, `type`. Optional:
`subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary`.

**Type-specific columns:**

| Type | Required | Optional | Forbidden (anything else) |
|---|---|---|---|
| CHARACTER | `diplomacy`, `intrigue`, `psi`, `leadership`, `isAmbassador` | `cost` | e.g. `timing`, `participation` |
| FLEET | `military` | `fleetClass`, `cost` | stats, agenda/agenda-adjacent fields |
| CONFLICT | `conflictType`, `influenceReward` | `participation` (object fragment) | stats |
| AGENDA | `isMajorAgenda`, `winCondition` | — | `cost`, stats |
| AFTERMATH | `triggerCondition` | — | `cost` |
| ENHANCEMENT | the 5 `*Bonus` fields | `cost` | stats, `participation` |
| GROUP | — | `cost` | stats, `participation` |
| LOCATION | `influencePerRound` | `military`, `cost` | stats |

A record violating this table is **UNEXPECTED**, not merely "another shape."

## 3. The UNEXPECTED shapes (the point of the table)

1. **CHARACTER without `cost` — 12 records**: the six ambassadors
   (Sinclair, G'Kar, Delenn, Londo, Zack Allen, Kosh) in *both* sets. Not
   blessed by the table: `cost` is optional but these are the only
   CHARACTERS omitting it, and the engine bills them anyway (AIPlayer's
   getCardCost substitutes its 5-cost floor, blessed by B5-0957). The data
   says "unpriced"; the engine says "5". The table's honest reading: these
   are **either** a deliberate ambassador exemption needing a one-line DECISIONS
   note **or** 12 missing fields. Adjudication is a designer call; today the
   engine's substitution is recorded behaviour, not an accident.
2. **ENHANCEMENT without `cost` — 2 records**: `enh_judgment_by_success` /
   `de_enh_judgment_by_success` (known B5-0943 finding: needs human-supplied
   printed cost). UNEXPECTED and already adjudicated as a data gap.
3. **EVENT carrying `timing` — 1 record**: `de_event_armistice`,
   `timing: "ANY"`. Forbidden under the table for every type; the loader
   never reads the key. Carried from B5-1022's UNDECIDABLE class
   (metadata-without-consumer); not blessed.

Everything else is EXPECTED: the 13 shapes reduce to the table above, and the
CONFLICT `participation` variant is the B5-0336 optional column doing its job.

## 4. What the loader does with broken records (measured)

- **Missing `id`/`title`/`type`**: `req()` throws → `parseCards` catches →
  **the card is dropped at load**, one stderr line, the game runs short
  without any in-game signal.
- **Missing a type-specific field** (e.g. a CHARACTER without `diplomacy`):
  `intVal`/`boolVal` substitute defaults (0/false; FLEET military → 1,
  CONFLICT conflictType → DIPLOMACY) → **the card loads silently wrong**.
- **Unknown field** (e.g. `timing`): kept in the map, ignored by `buildCard`
  → **silently ignored forever**.
- **Unknown enum value**: rarity → silently `COMMON`; faction → silently
  `ANY`; conflictType → throws → **card dropped** (the B5-1037 finding).

So: a loader that defaults-and-ignores accepts a broken card forever, and the
one hard failure (unknown conflictType) drops the card rather than naming the
defect in-game. Both behaviours are exactly why this contract needs to exist
on paper before any validating loader is proposed.

## 5. Recommendation

Adopt the table as the record contract via a human or DECISIONS ruling; a
**validating loader is a separate row with its own claim** (per this row's
scope): fail loudly on required-missing and forbidden-present, keep the
tolerant defaults only where the table says optional, and add a conformance
probe asserting 829/829 conform. Unresolved designer questions this proposal
raises but does not answer: the ambassador cost exemption (12 records) and
the armistice `timing` field (1 record).

Related: `blocked-prereq-token-action-ready-proposal.md` (same tier),
B5-1037's conflictType findings, B5-1022's adjudication.
