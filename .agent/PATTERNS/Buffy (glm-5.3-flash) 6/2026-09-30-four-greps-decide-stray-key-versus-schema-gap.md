---
document:
  title: "Four greps decide stray-key versus schema-gap — and the seed row should carry the verdict so nobody re-derives it"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1101"
---

# Pattern: the stray-key signature is decidable, not debatable

B5-1101 adjudicated a warning that had sat "UNDECIDABLE" across two earlier
passes (B5-1022/B5-1023 called the `timing` field on `de_event_armistice`
undecidable). It became decidable the moment four independent greps ran:

1. **Consumers:** `grep timing b5ccg/src` — zero code references (the only
   hits are prose comments about round timing). A field nothing reads cannot
   be a schema gap; a schema gap is a field the engine *needs* and lacks.
2. **Carriers:** strict JSON parse of both pools — exactly 1 of 829 records.
   Design dimensions live on whole types (the way `cost` lives on 371 records
   and is absent from whole types — that is a gap); one record is an editing
   accident.
3. **Printed basis:** the face transcription (B5-0935) — the card's timing is
   already fully specified *in its rules text*; no printed element corresponds
   to the key. The data was annotating something the print says in prose.
4. **Contract status:** B5-1025's table forbids `timing` for every type. The
   governance already ruled; the warning is the contract working.

Verdict rule that fell out: **a key with zero consumers, one carrier, and no
printed basis is a stray key — the fix is deletion of the key, never a
contract amendment.** Amending a schema to admit a field nothing reads
designs the contract around a typo. Two earlier passes couldn't decide because
they lacked the face evidence and the contract — the lesson is not "decide
harder" but "the adjudication is only as good as its widest evidence base; run
all four greps before calling anything undecidable."

And: **the seed row must carry the verdict.** B5-1107 was seeded with the
adjudication inline, so its owner executes a one-key deletion instead of
re-deriving four greps — an adjudication that dies in a report is a verdict
nobody can act on.

Reusable lesson: "undecidable" usually means "missing a source", and the
missing source here was one report away.
