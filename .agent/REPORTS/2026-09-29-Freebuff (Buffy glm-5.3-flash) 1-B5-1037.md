---
document:
  title: "B5-1037 close-out — participation and conflictType are executable"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1037 — Are `participation` and `conflictType` executable?

Task: for the 7 participation-bearing and the conflictType-carrying records,
report every value, its mapping, the mapping's totality, and whether the
mapping is validated anywhere — listing every unrecognised value explicitly.
Read-only; no JSON, mapping, or src edits.

## Verdict

**Both fields are executable, not documentation.** Every value in the data
maps to a real enum constant or engine-checkable predicate. The
unrecognised-value list is **empty on both sides**. One forward-looking risk
remains (see §conflictType).

## conflictType (instrument: Python json.loads, utf-8 strict bytes)

- **108 records** carry it, all of type CONFLICT; all 108 CONFLICT records
  carry it — total on its population, zero missing.
- Values: DIPLOMACY 47, INTRIGUE 40, MILITARY 17, PSI 4 — **4 distinct**,
  all real `ConflictType` constants (`enums/ConflictType.java`: DIPLOMACY,
  INTRIGUE, MILITARY, PSI).
- Mapping line: `DeckLoader.java:267` — `ConflictType.valueOf(value.toUpperCase())`
  inside `buildCard`. **Unvalidated**: an unexpected value throws out of
  `buildCard` into the `parseCards` catch and the *card is dropped at load*
  with a stderr line only — drawn never, displayed never, and the player could
  never tell. Currently unreachable because all present values are legal.
- Row arithmetic note: the row's headline says 106 carriers; its own
  breakdown (54 + 47 + the 2 participation-bearing) sums to 108, which is the
  measured figure. The headline was the defect, not the data.

## participation (7 records, all CONFLICT: 5 premiere, 2 deluxe)

| # | Record | Exact fragment | Maps to | Executable? |
|---|---|---|---|---|
| 1 | conf_complete_support | `{"mustTakeSide":true}` | `Participation.parse` :241 → `RulesEngine.isMustTakeSide` | yes |
| 2 | de_conf_complete_support | `{"mustTakeSide":true}` | same | yes |
| 3 | conf_immortality_serum | `{"mustCommitAmbassador":true}` | :237 → `isMustCommitAmbassador` | yes |
| 4 | conf_border_raid | `{"players":"INITIATOR_TARGET","requiresTarget":true,"cardTypes":["FLEET"],"perPlayerQuota":{"FLEET":1},"leadersIncluded":true}` | `isPlayerAllowed` :137–141 (string compares), `cardType` :219 (`CardType.valueOf`), `quotaFor`, `RulesEngine` | yes |
| 5 | de_conf_border_raid | identical fragment | same | yes |
| 6 | conf_limited_strike | `{"cardTypes":["FLEET"],"fleetSubtypes":["PICKET","COLONIAL","UTILITY"]}` | `allowsCardType` :148–154 vs `FleetCard.getFleetClass`; **22 of 80** FLEET records carry a matching `fleetClass`, so the filter admits a real population | yes |
| 7 | conf_the_great_machine | `{"allPlayersMustCommit":{"cardType":"CHARACTER","count":1}}` | :219 + :232–238 → `hasAllPlayersMustCommit`/`getAllPlayersCardType`/`getAllPlayersCount` | yes |

(DeckLoader attaches the fragment at `DeckLoader.java:109`
`((ConflictCard) c).setParticipation(Participation.parse(partStr))`, guarded
by the try/catch at :111–113 that logs and keeps the card **open**.)

## Totality and degradation

- `players` vocabulary (ALL, INITIATOR, INITIATOR_TARGET, TARGET) is total for
  the values in data; the unknown-value path (:143) falls open **loudly** on
  System.err, never silently coerced.
- Unknown cardTypes/quota keys (:219) and unknown keys (:244) are skipped loudly.
- The fragment guard (:111) degrades loudly to open participation without
  dropping the card.

## Validation status

No schema validation exists for either field — both are enum-or-predicate-
from-string by convention. The difference from the 25-shape schema class:
**the convention is written down** (`Participation` javadoc, proposal §3/§5).
The single actionable follow-up for any future row: guard or whitelist the
`:267` valueOf — do not widen it into a general schema effort.

## Gates

compile.sh green on JDK 8; run-dup-census exit 0 post-write; own row 7 pipes /
doubleLead no; claim released at close-out; no reap; no foreign artifact
touched; no commit, no push.

## Reusable lesson

Report the unrecognised-value list, not a pass rate — the census found 100%
recognition here, but the only live defect on the healthy side is a single
unvalidated `valueOf` whose failure mode is a silently dropped card, and a
94%-style pass figure would have buried both facts.
