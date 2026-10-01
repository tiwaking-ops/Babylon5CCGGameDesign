---
document:
  title: "B5-1123 — the explicit cost-0 class: 6 records, indistinguishable from absent at runtime, backfill consequence stated"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:50:26Z"
  instrument: "python re over explicit-UTF-8 bytes of both card pools; grep count corroboration"
---

# B5-1123 — the 6 records carrying an explicit `cost: 0`

## The list (measured)

| id | type | set |
|---|---|---|
| char_delenn_transformed | CHARACTER | PREMIERE |
| group_rabble_rousers | GROUP | PREMIERE |
| loc_sleeping_zhadum | LOCATION | PREMIERE |
| de_char_delenn_transformed | CHARACTER | DELUXE |
| de_group_rabble_rousers | GROUP | DELUXE |
| de_loc_sleeping_zhadum | LOCATION | DELUXE |

Three titles, each twin-printed with the identical explicit 0 — one
CHARACTER, one GROUP, one LOCATION. Cost-key totals corroborate the row's
arithmetic: premiere 209 keyed (3 zero), deluxe 168 keyed (3 zero) = 377
keyed, 452 absent, 829 records.

## The row's question, answered from B5-0968's source findings

**An explicit 0 and an absent key are distinguishable only in the raw
JSON — nowhere in the model.** B5-0968 (DECISIONS 2026-09-29) measured
`Card.cost` as a primitive `int = 0` (no null possible) hydrated by
`parseCards` only from a present, non-empty, parseable-non-negative key;
absent, empty, explicit-zero, non-numeric, and negative all round-trip to
`getCost() == 0`. The B5-0968 entry's own conclusion stands confirmed by
this new class: the ruling's absent-vs-zero distinction is **not
observable at runtime**, and B5-0968 filed the schema follow-up (a
distinguishing field) as a blocker for any backfill design.

## What follows for a future backfill (the row's second question)

A backfill **must not write `cost: 0`** into a never-costed record: doing
so silently converts "no printed cost was ever recorded" into "deliberately
free", and — because the two classes are JSON-only distinguishable — the
conversion would be invisible to every model-level check, undetectable by
any runtime instrument, and irreversible except by re-deriving the original
absence from this audit's list. Until the B5-0968 follow-up field exists,
`cost: 0` written by a backfill is indistinguishable from these six
deliberate zeros; the honest options are (a) leave never-costed records
keyless, or (b) land the distinguishing field first and backfill with it.

## Face-value note (bounded)

Scans exist for the three deluxe twins only
(`de_char_delenn_transformed.gif`, `de_group_rabble_rousers.gif`,
`de_loc_sleeping_zhadum.gif`); no orb transcription for these titles exists
in any diff report, so this audit cannot say whether the printed faces
show 0-bubbles or no-bubbles — and inventing that reading is exactly what
the row forbids. A human with the three faces (or a new transcription row)
settles "deliberately free" vs "accidentally explicit".

No JSON edited, no value written, no src edit, no commit, no push.

**Reusable lesson:** when a ruling's distinction lives only in the source
format, every new writer into that format inherits the ruling as a hazard —
audit the class before the class audits you.
