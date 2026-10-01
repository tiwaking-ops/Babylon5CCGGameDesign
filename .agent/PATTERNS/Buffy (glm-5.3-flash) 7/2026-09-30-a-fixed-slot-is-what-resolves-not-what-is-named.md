---
document:
  title: "A fixed slot is what resolves, not what is named: deduped-pool fallback imports another set's record (and cost)"
  status: "Pattern (observation plus operating guidance; supersede-never-rewrite)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
---

# A fixed slot is what resolves, not what is named

Observed on B5-1149 (2026-09-30): the premiere fixed starter lists carry
Premiere ids, but the engine pool is deduped to ALL Deluxe plus
Premiere-never-reprinted (DeckLoader.loadBothSets), so `StarterDeckBuilder`
resolves a fixed slot by id or falls back to title match — and under that
pool the resolved record is frequently the **Deluxe reprint**: 109 of 200
fixed slots (54.5%), plus every faction ambassador. The reprint carries its
own cost, so the B5-1038 play-time charge prices printed-starter cards at
Deluxe rates: 55 of 200 fixed slots cost more than the starting appliedPool
of 4.

Operating guidance:

1. Any audit of a fixed list, deck list, or id-keyed expectation must census
   the **resolved card** (rarity, set, cost), never the named id — the
   name-to-record mapping is not the identity map it looks like.
2. When a deduped pool sits between a definition file and the engine, expect
   the fallback to import the surviving record's attributes, and report the
   share — a bare "all resolve" hides the set shift entirely.
3. Cross-check the engine path with a strict-JSON read of the definition file
   (187 rows, 4 × 50 here): the two instruments catch each other's silent
   assumptions (the JSON census alone would have reported the Premiere ids;
   the engine probe alone would not have shown how deliberate the fallback
   is).

Links: supersedes nothing; complements
`2026-09-30-a-census-is-a-point-in-time-verdict.md` (same session). Run
report: `.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 7-B5-1149.md`.
