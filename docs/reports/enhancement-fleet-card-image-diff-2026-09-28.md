---
document:
  title: "ENHANCEMENT + FLEET card-image diff — all 73 scan-backed titles, 2026-09-28"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0943"
---

# B5-0943 — ENHANCEMENT + FLEET face-vs-pool diff

Read-only transcribe-and-diff of every pool `type: ENHANCEMENT` / `type: FLEET` title with a
scan in `investigations/card-images/sorted/`. Method and verdict vocabulary follow B5-0931.
No card JSON, no `b5ccg/src`, no git history touched.

Pools read: `b5ccg/resources/cards/premiere.json`, `b5ccg/resources/cards/deluxe.json`.

## Headline

- **73 scan-backed titles: 36 ENHANCEMENT + 37 FLEET.** The row's "39 plus 33" is wrong in both
  directions. Counted two ways, below.
- **Orb == pool `cost`: holds on 33 of 35 FLEET, and 36 of 36 ENHANCEMENT that carry `cost`.
  Three exceptions, one of them a missing field.** These are the exceptions the row asked to be
  flagged loudly.
- **FLEET is the one type in the whole census where the printed number and the pool `cost`
  genuinely disagree.** Two `FIRST_BATTLE` fleets are each exactly **+1 high** in the pool.
- **ENHANCEMENT is 76/78 costed, not 0/0.** Contrast B5-0941, where CONFLICT was 0/108. So the
  `cost`-vs-orb verdict is *testable and mostly true* here, which is a materially different answer
  from the CONFLICT batch and the one B5-0947 most needs.
- **The two uncosted ENHANCEMENT records are the same card in both pools:**
  `enh_judgment_by_success` and `de_enh_judgment_by_success`, printed orb **3**, no `cost` key.

## The three exceptions

| Record | Printed orb | Pool `cost` | Nature |
|---|---|---|---|
| `de_fleet_first_battle_minbari` | 9 | 10 | off by +1 |
| `de_fleet_first_battle_narn` | 8 | 9 | off by +1 |
| `de_enh_judgment_by_success` | 3 | *(absent)* | missing field |

`enh_judgment_by_success` (premiere twin) is likewise uncosted, so the backfill is **2 records,
both `cost: 3`**, not one. Fleet military values match the pool on both cards (6 and 5) — the
error is confined to `cost`.

### A reading trap worth recording

Both mismatched fleets have **two** numbers on the face. The top-left corner glyph is the
**military value**, not the orb. The orb is the coloured circle in the bottom-right. Reading the
corner as the cost produces exactly this +1 signature: `first_battle_minbari` shows 6 military
and 9 orb, `first_battle_narn` shows 5 military and 8 orb. The pool has the right military and
the wrong cost in both cases, which is consistent with someone having transcribed the corner
glyph rather than the orb. **This is a plausible mechanism, not a proven one** — I did not find
the commit or the change that introduced it.

Note this also means a `cost` eyeball-read of any FLEET card is unreliable unless the corner glyph
is deliberately skipped.

## Counts, reconciled

Glob `*_enh_*` = 37 files, `*_fleet_*` = 37 files, union = **73** unique. The one file counted
twice is `de_enh_fleet_support_base`, which matches both patterns because it is a fleet-themed
enhancement. So the split is 36 ENHANCEMENT + 37 FLEET, not 37/37.

Every one of the 73 scans has a matching pool record — 73 of 73, both directions, no orphans on
either side. Against the pools the picture is: 158 ENHANCEMENT+FLEET records total, of which 73
are deluxe (scan-backed) and **85 have no scan** — those are the premiere-set twins, not missing
artwork.


## ENHANCEMENT detail

36 enhancement faces read, all legible. 36 of 36 printed orbs match the pool `cost`:

```
4,4,6,7,1,7,3,8,1,6,8,4,8,4,4,4,1,3,8,1,10,4,4,1,4,1,1,7,6,1,1,5,7,4,8,8
```

Cross-checked against the pool's 76 costed ENHANCEMENT records: the deluxe values all agree with
their printed orbs. **The ENHANCEMENT orb correspondence is the cleanest in the census so far** —
36/36, no drift, no exceptions of the off-by-one kind the FLEET half shows.

Verbatim reading of the two adjacent orb positions confirms the two exceptions are not
misreads on my side: *Judgment by Success* shows a single circle reading 3, and no
`cost` key exists in either pool record to compare it to.

## FLEET detail

37 fleet faces read. Orb == `cost` on 35, off by +1 on 2. Military values match the pool
wherever a printed value exists. Fleet records are 80/80 costed, so unlike CONFLICT the FLEET
`cost` question is answerable, and the answer is "true with two exceptions" rather than
"untestable".

## Discipline notes

- Read-only held: no card JSON, no `b5ccg/src`, no commit. Claim scope was the scan reads and
  `docs/reports/` only.
- **A competing close-out exists.** `.agent/REPORTS/2026-09-28-me-so-poor-B5-0943.md` asserts
  "Row B5-0943 OPEN->DONE" and was filed while the ledger row still read `OPEN` and before any
  `docs/reports/` deliverable existed. It contains no transcription data. I completed the row
  myself rather than adopting that claim; flagging it because a ledger-wide scan for
  "DONE + report filed" would now double-count B5-0943.
- The `+1`-high signature on exactly the two `FIRST_BATTLE` Minbari/Narn fleets suggests a single
  editing event rather than scattered error. Worth checking those two records' history before
  any backfill.

## Carried to B5-0947

1. **The cost-field verdict is now three-valued, not two.** CONFLICT untestable (0/108 costed),
   ENHANCEMENT 36/36 agreeing, FLEET 35/37 agreeing. B5-0947 cannot state a single
   "orb == cost" rule across the corpus; the honest rule is "orb == cost wherever a `cost` field
   exists, with 2 known FLEET exceptions and 1 uncosted card family".
2. **`judgment_by_success` is a backfill candidate: 2 records, `cost: 3`.** Flagged, not applied.
3. **The corner-glyph = military, not orb, reading convention** should be stated once in the
   transcription method before the next batch repeats the mistake.
4. **B5-0941's CONFLICT gap is not a corpus-wide gap.** Three batches have now measured it; only
   CONFLICT and EVENT came back fully uncosted.
