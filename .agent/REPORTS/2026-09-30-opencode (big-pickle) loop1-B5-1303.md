---
document:
  title: "B5-1303 close-out — the PSI starvation B5-1155 measured is a data property, not a loader artifact, and print-faithfulness is unreachable from in-repo data"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1303"
---

# B5-1303 close-out — starved-by-data or starved-by-loader?

**Claim:** `.agent/CLAIMS/B5-1303.json` (`opencode (big-pickle) loop1`,
started 2026-09-30T22:50:10Z), released at close-out. Row re-read `OPEN` before
claiming; no live claim on the row. Gate `b5ccg/compile.bat` green (exit 0)
before the probe and re-certified after. Scope held: read-only probe under the
git-ignored `b5ccg/out/b51303/`, this report, one pattern, heartbeat, DECISIONS,
the row flip. **No deck JSON, card JSON, or src edit; no commit; no push.**

## Verdict

**Not a loader artifact.** Every layer the loader owns is measured clean, and the
one filter that does drop a PSI record belongs to the deck builder and is
rulebook-faithful. The starvation is a property of the authored data: two PSI
titles exist, one of them carries rarity `FIXED` (so it can never enter a random
half), and the fixed-list resource names exactly one PSI conflict slot in the
whole file.

**Print-faithfulness: UNREACHABLE from in-repo data** — recorded as such, as the
row directs. No citable in-repo transcription of the printed set exists.

## Instruments

1. `b5ccg/out/b51303/b51303/B51303PsiCensusProbe.java` (Java 6, stdlib only,
   git-ignored), run as `java -cp b5ccg/out;b5ccg/out/b51303 b51303.B51303PsiCensusProbe`,
   receipt `b5ccg/out/b51303-receipt.txt`, stderr `b5ccg/out/b51303-stderr.txt`.
   It reads the pool through the **production path only** —
   `DeckLoader.loadFromResource` per file, `DeckLoader.loadBothSets` for the
   deduped pool, `StarterDeckBuilder.build` for seating, with
   `setRandomSeed` for the seeded runs and a 400-seed sweep for the eligibility
   measurement.
2. An **independent raw-text instrument** (`Select-String` over the two JSON
   files) counting `"conflictType": "PSI"` lines and `"id":` lines, so the
   loader's own parse is not the only witness for its own record count.

## Measurements

### A/B/C. Per file before dedupe, and the dedupe itself

| set | PSI rows | distinct PSI titles |
|---|---|---|
| `premiere.json` (raw, 446 records) | 2 | 2 |
| `deluxe.json` (raw, 383 records) | 2 | 2 |
| deduped pool (`loadBothSets`, 446 records) | 2 | 2 |

The four raw rows are `conf_psi_attack` (**rarity UNCOMMON**) and
`conf_telepathic_scan` (**rarity FIXED**), plus their Deluxe twins. Cross-check:
4 `"conflictType": "PSI"` lines by raw text, matching the probe's 4.

**PSI titles dropped by the title dedupe: 0.** `loadBothSets` keeps all Deluxe
plus every Premiere title absent from Deluxe, so it drops the 2 Premiere twin
*rows* and no title. Twin comparison shows the survivors are faithful copies:

```
conf_psi_attack       vs de_conf_psi_attack       : conflictType PSI/PSI  rarity UNCOMMON/UNCOMMON  influenceReward 2/2  sameType=true
conf_telepathic_scan  vs de_conf_telepathic_scan  : conflictType PSI/PSI  rarity FIXED/FIXED         influenceReward 1/1  sameType=true
```

**Records dropped at parse: 0.** Raw `"id":` line counts equal parsed record
counts — premiere 446 = 446, deluxe 383 = 383 — so no PSI card (or any card) is
lost in tokenising.

### D. The rarity filter, which is the one real drop

`StarterDeckBuilder.drawRandomUncommonsRares` keeps only `UNCOMMON`/`RARE`
cards playable by the faction. Per faction, measured:

| pool PSI record | rarity | filter passes | random candidate |
|---|---|---|---|
| `de_conf_psi_attack` (Psi Attack, faction ANY) | UNCOMMON | yes | yes, all four starters |
| `de_conf_telepathic_scan` (Telepathic Scan, faction ANY) | **FIXED** | **no** | **never, any faction** |

This is rulebook-faithful: the printed starter decks' 10 random slots are
uncommons/rares, so a FIXED card is not eligible. But it is the binding
constraint on half the PSI content, and B5-1155 described both PSI records as
"rare-line conflicts" — that premise is false in the data (`Psi Attack` UNCOMMON,
`Telepathic Scan` FIXED, neither RARE).

### F. Fixed lists

The deck resource names exactly **one** PSI conflict slot across all 187
entries: `HUMAN / conf_telepathic_scan`. The MINBARI, CENTAURI and NARN fixed
50s contain no PSI conflict at all, which is why two of those decks seated zero
PSI in B5-1155.

### E. Seating through the production path

Seed 42, fixed 50 plus random 10:

| deck | conflicts | PSI fixed | PSI random |
|---|---|---|---|
| HUMAN | 11 | 1 (`de_conf_telepathic_scan`) | 0 |
| MINBARI | 8 | 0 | 0 |
| CENTAURI | 10 | 0 | 0 |
| NARN | 11 | 0 | 0 |

1 of 40 seated conflicts, against B5-1155's 2 of 44 — a different random draw,
not a disagreement. Over **400 seeds × 4 factions = 1600 random halves**: a PSI
card was seated **103 times (6.4 %)**, all 103 of them `de_conf_psi_attack`;
`de_conf_telepathic_scan` was seated **0** times, exactly as the rarity gate
predicts.

## Why print-faithfulness is unreachable here

Searched repo-wide for a transcription of the physical Premier/Deluxe sets:

* `docs/reports/conflict-card-image-diff-2026-09-28.md` (B5-0941) is the only
  document that read printed CONFLICT faces. It is a **diff, not a census**: it
  asserts 57 of 59 single-type printed faces agree with the pool's
  `subtype`/`conflictType` without recording the printed type per card, and
  transcribes only two dual-type lines — The Great Machine, and Sleeper
  Personality, a printed Intrigue/Psi dual that the one-`conflictType` schema
  can store only as `INTRIGUE`.
* `investigations/b5-premier-starter-deck-fixed-lists-2026-09-21.md` is a fan
  compilation of four starter **fixed lists**, not a set-wide census.
* `BABYLON5_CCG_RULEBOOK.md` is rules only: four conflict types defined, no card
  appendix, no counts.
* `investigations/card-images/` holds 2067 images whose `MAPPING.json` ids are
  derived from the pool, so it cannot witness the pool.
* The genuine per-card OCR tables in `docs/reports/*card-image-diff*` cover
  AFTERMATH, EVENT, ENHANCEMENT and FLEET — not CONFLICT typing.

A printed PSI tally is therefore reachable only by chaining pool typing with
B5-0941's unrecorded 57/59 agreement assertion plus its two dual-type lines. That
is an inference, not a transcription, so the verdict is recorded as unreachable
rather than asserted. Closing the gap needs a per-card printed-type table for the
59 CONFLICT faces in the shape of the existing AFTERMATH/EVENT diffs; no OPEN row
owns that today.

## Not done

No deck JSON, card JSON, src, conformance-section or AI edit; no commit; no
push. `b5ccg/resources/cards/deluxe.json` was already modified in the working
tree by another agent before this task started and was read, never written, by
this session.

**Reusable lesson:** a filter that drops a record tells you nothing until you read
the field it filters on — the same card is "rare" in the predecessor's prose and
`FIXED` in the data, and the data is what the engine obeys; and a "we compared
this against the cards" note that reports an *agreement count* instead of a
per-card transcription can never confirm a type, only fail to contradict it.
