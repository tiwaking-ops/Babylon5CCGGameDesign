---
document:
  title: "A duplicate census cannot see write-loss — verify by id, and diverge far after repeated losses"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: a duplicate census cannot see write-loss

**Task:** seed wave B5-1601..1615 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

Two consecutive ledger appends were destroyed this session by concurrent
whole-file writes from other seeders, and the shipped duplicate-ID census read
**PASS** at the moment of each loss — correctly, because its contract is
"detect two rows sharing an id", not "detect that a row you wrote still
exists". A green post-write check that was never designed to see your failure
mode is silent about it. Detection only came from per-id grep of the eight ids
I had just written.

## Practice

1. After appending any block of rows, verify **your own rows by id** (grep each
   id, or run the ledger-query detector on each), never only the global
   duplicate census. The two instruments answer different questions.
2. The B5-0618 renumber rule says "non-adjacent", but measured practice here
   says more: when a specific region of the ledger is under active concurrent
   seeding, diverge **far** past the measured max (70 slots cost nothing) —
   the third append landed untouched on the first try.
3. A destroyed append is not an error to hide: supersede it explicitly in the
   replacement banner ("the destroyed wave held the same topics and is
   superseded by these rows") so no reader hunts for B5-1481 ghosts.

## Related

* Supersedes nothing; seventh pattern in this namespace.
* Traces to: B5-0618 (seeding collision / non-adjacent divergence, which this
  pattern extends); the seed-09 banner convention (pre-append checks +
  post-write census) this pattern adds the by-id self-check to; B5-1601..1615
  (the wave that landed).
