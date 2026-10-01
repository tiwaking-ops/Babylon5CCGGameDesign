---
document:
  title: "A quota counts slots, not types: seat the census one level below the seating rule"
  status: "Pattern (observation plus operating guidance; supersede-never-rewrite)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
---

# A quota counts slots, not types

Observed on B5-1155 (2026-09-30): the B5-0313 quota (12 conflicts + 2 agendas
per 60-card deck) is faithfully over-seated (14/9/10/11 conflicts; 3–4
agendas), yet the conflict-TYPE mix at deck level inverts the pool — MILITARY
goes from 9 of 59 pool records (15%) to 18 of 44 seated slots (40.9%), and PSI
sits at zero in two of four decks because only 2 PSI records exist in the
whole deduped pool. Nothing in the seating rule decides any of this; the
random draw's rarity filter and the pool's shape do.

Operating guidance:

1. When a rule seats N of something, census what actually got seated — the
   rule's own text carries no information about the mix.
2. Distinguish "starved by construction" (a filter excludes the type) from
   "starved by data" (the pool holds too few records to seat): PSI is the
   second kind, and no engine change can fix it.
3. Before attributing a mix to any chooser (AI, seeding, filler), reproduce it
   with no chooser present: the same production build path with the AI removed
   still produced the inversion, so the AI was never a candidate explanation.

Links: supersedes nothing; complements
`2026-09-30-a-fixed-slot-is-what-resolves-not-what-is-named.md` (same session,
same production path). Run report:
`.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 7-B5-1155.md`.
