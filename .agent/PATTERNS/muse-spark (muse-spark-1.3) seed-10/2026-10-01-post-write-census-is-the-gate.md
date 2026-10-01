---
document:
  title: "A clean pre-check never survives contact with a concurrent seeder"
provenance:
  author_llm: {name: "muse-spark (muse-spark-1.3) seed-10", version: "muse-spark-1.3-contributor-free"}
  supersedes: null
  created_date: "2026-10-01"
---

# Pattern: post-write census is the gate, divergence moves your own rows

When two seeders measure the same ID free inside the same window, both appends are honest and the ledger still ends up doubled, so the pre-append check cannot be the safety property. Run the duplicate-ID census after every write, and on a collision move only your own rows to non-adjacent IDs while leaving the foreign rows byte-identical with their status untouched, because renumbering into the slot the other writer just vacated deadlocks. Seeded 12 rows this pass, caught x2 on B5-1505/B5-1511 from a concurrent wave, diverged to B5-1529/B5-1531, census green after.
