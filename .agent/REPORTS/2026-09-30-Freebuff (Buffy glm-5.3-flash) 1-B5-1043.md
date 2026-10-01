---
document:
  title: "B5-1043 — the 58 unreachable deluxe-only records do not exist: premise refuted at runtime, verdict close, zero src edits"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T05:27:52Z"
  probe: ".agent/tmp_b51043/DupTitleProbe.java (scratch, compiled to git-ignored b5ccg/out/), Java 6 source level"
---

# B5-1043 — verdict: the premise is refuted; nothing to fix

## Measurement (runtime, loader-authoritative)

The scratch probe loads through `DeckLoader` itself (no reimplementation)
and censuses the pool:

```
PROBE pool=446 duplicateTitles=0 unionTitles=446 poolMissingFromUnion=0 deluxeOnlyTitles=0
```

- **pool = 446**: `loadBothSets()` (DeckLoader.java:36-45) returns all 383
  deluxe records plus the 63 premiere records whose titles have no deluxe
  twin.
- **deluxeOnlyTitles = 0**: every deluxe title also exists in premiere.
  There is no deluxe-only class by the loader's title key.
- **poolMissingFromUnion = 0, duplicateTitles = 0**: the pool covers all
  446 distinct titles in play with zero duplicates — the row's own success
  criterion ("every deluxe-only record loads with zero duplicate titles")
  is already satisfied, vacuously.

A static regex census over the raw JSON undercounted (441/381 — the naive
`{[^{}]*}` object split misses nested structures); the numbers above are
the **loader's** parse, which is the only one that counts.

## Why the row's premise was wrong

The "58 deluxe-only + 121 premiere-only" twin census is the same
de_-stripped-join artifact B5-1024 already adjudicated (that row measured
the identical 325/58/121 triple and refuted it; correct-by-title is
383/0/63). B5-1031's headline "446 premiere-only of 829" counted **records,
not titles**: 829 records collapse to 446 distinct titles, and the pool
holds all 446.

## What IS unreachable (named for the record)

The genuinely unreachable class is the **383 premiere twin copies** —
`loadBothSets()` admits all deluxe first, so deluxe wins every twin slot
(the same mechanism B5-1053 recorded for RARE_WITHDRAWN). This is
deliberate under B5-0320's pool rule, and the content that differs between
twins already has its home: B5-1032 filed the 128 annotated deluxe errata,
B5-1021/B5-1022 adjudicated the text/stat deltas, and B5-1045's dispatch
approach extends `CardEffects` for the deluxe deltas that are actually
wrong. Nothing in this class requires a loader change.

## The row's gate consequence

This row gated B5-1045, B5-1047, B5-1051, and transitively B5-1057/B5-1059.
Closing it **DONE with a refuted premise** unblocks the chain; their own
premises are unaffected (B5-1045's delta dispatch targets `CardEffects`,
not the loader; B5-1047's valueOf guard targets DeckLoader.java:267 and is
independent of this verdict).

## Two side findings (flagged, not fixed)

1. The loader's validation logging emitted `UNKNOWN FIELD: id/title/type on
   card …` for **every standard field of every record** during any load —
   either B5-1055's validator's known-field list is mis-wired or the log is
   printing fields it *does* map. Loud enough to drown any real warning;
   belongs to the B5-1055/B5-1047 owners, not this row.
2. My first static census undercounted records (regex vs nested JSON) —
   caught because the loader's runtime numbers disagreed with it. The
   loader is the authoritative parser; static JSON regexes are estimates.

## Bounds

Zero src edits, zero card-JSON edits, no suite edits, no instrument
changed. The probe (`.agent/tmp_b51043/DupTitleProbe.java`, 44 lines,
`-source 6`) is the reproducible instrument; its class file lives only in
git-ignored `b5ccg/out/`. No commit, no push.

**Reusable lesson:** before fixing a loader, run the loader — the premise
was a records-vs-titles miscount wearing a fix-me sign, and five gated rows
were waiting on a no-op.
