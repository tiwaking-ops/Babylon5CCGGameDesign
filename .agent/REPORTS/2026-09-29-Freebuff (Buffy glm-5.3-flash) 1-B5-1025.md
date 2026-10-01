---
document:
  title: "B5-1025 close-out — the card-record schema contract, published"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1025 — The card-record schema contract

Instrument: `json.loads` on utf-8-strict bytes over all 829 records; loader
behaviour read from `DeckLoader.parseCards`/`buildCard`. Read-only; the
deliverable is `docs/proposals/card-record-schema-contract.md`.

## The shapes, reconciled

The row's 25 = 12 distinct field-sets in premiere + 13 in deluxe (per-file
counting, instruments named in the row). Globally: **13 distinct shapes,
organised per card type** (full table in the proposal): AFTERMATH 1, AGENDA 1,
CHARACTER 2, CONFLICT 2 (the 7 B5-0336 participation records), ENHANCEMENT 2,
EVENT 2, FLEET 1, GROUP 1, LOCATION 1. The nine-field base
(`faction, id, imageKey, rarity, set, subtype, text, title, type`) is present
on every record of every type.

## The contract, in one line per decision

Common required: `id`, `title`, `type`; common optional: `subtype`, `rarity`,
`faction`, `set`, `imageKey`, `text`, `cost`, `mercenary`. Type-specific
required/optional/forbidden columns per the proposal's table. A record
violating the table is UNEXPECTED — that is the table's purpose.

## The UNEXPECTED shapes (identified, not blessed)

1. **CHARACTER without `cost` — 12 records**: the six ambassadors, in both
   sets. Genuinely ambiguous: deliberate exemption or missing fields. The
   engine already substitutes a 5-cost floor via AIPlayer's getCardCost
   (B5-0957), so the cards are billed regardless — recorded behaviour, not an
   accident, but the table does not bless it.
2. **ENHANCEMENT without `cost` — 2 records**: `enh_judgment_by_success`
   both sets (B5-0943's known data gap; needs a human printed cost).
3. **EVENT carrying `timing` — 1 record**: `de_event_armistice`, `timing:
   "ANY"`. Forbidden for every type; unread by any loader line; carried from
   B5-1022's UNDECIDABLE class.

All other shapes are EXPECTED under the table.

## What the loader does with broken records (measured)

- Missing `id`/`title`/`type` → throw → catch → **card dropped at load**
  (stderr only).
- Missing type-specific numeric/bool field → **silent default** (0/false;
  FLEET military → 1; CONFLICT conflictType → DIPLOMACY).
- Unknown field → kept in map, **ignored forever** (`timing` lives here).
- Unknown rarity → silently `COMMON`; unknown faction → silently `ANY`;
  unknown conflictType → throw → **card dropped** (B5-1037's finding).

A loader that defaults-and-ignores accepts a broken card forever; the one
hard failure drops the card rather than naming the defect. This is why the
contract must exist on paper before any validating-loader row.

## Recommendation

Adopt the table by ruling; **a validating loader is a separate row with its
own claim** (fail loudly on required-missing/forbidden-present; conformance
probe asserting 829/829). Designer questions raised, not answered: the
ambassador cost exemption (12) and armistice `timing` (1).

## Gates

No JSON/loader/src edit; compile.sh green this session; run-dup-census exit 0
post-write; own row 7 pipes / doubleLead no; claim released; no commit, no push.

## Reusable lesson

A table that blesses every shape it observes certifies the data instead of
constraining it — the deliverable is the UNEXPECTED list, and a per-file
shape count silently multiplies a 13-shape reality into a 25-shape headline.
