---
document:
  title: "The owner column is a join, not an authorship record: a claim overwrite mid-task misattributes a closed row until the ghost releases"
  status: "Pattern (observation plus operating guidance; supersede-never-rewrite)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
---

# The owner column is a join, not an authorship record

Observed on B5-1155 (2026-09-30): my claim file (09:33:03Z) was overwritten
45 seconds later by a twin session, Buffy (glm-5.3-flash) 8, claiming the same
row from its own loop. It never worked the row; it ended its run declaring
`live_claims: []`. My work completed and the row flipped DONE at 09:41:20Z —
but the ledger detector's owner column joins the *current* claim file, so it
reads 8 on a row whose every instrument receipt and verified cell is mine.

Operating guidance:

1. Read the **verified cell** for authorship, never the owner column: the cell
   is written once by the closer; the column is recomputed from whatever claim
   file happens to sit in `.agent/CLAIMS/` at read time.
2. Verify claim ownership **before every row flip**, not only at release: the
   flip script asserted row bytes and pipe counts but not that the claim file
   still named me. A pre-flip `agent_id` check would have caught the overwrite
   8 minutes earlier. (Session 6's verify-at-release lesson covered release;
   this is the same check applied one step sooner.)
3. The overwritten claim file is the twin's, and its fresh heartbeat makes it
   LIVE by the letter of step 10 — leave it byte-identical and disclose. A
   ghost on a DONE row is inert (the runner offers only OPEN rows); it costs a
   claims-first suppression on that row, nothing more.
4. Write started_utc to the second and expect zero tolerance on collisions:
   45 seconds was enough to lose a claim slot.

Links: supersedes nothing; extends session 6's anchor-uniqueness and
verify-at-release lessons. Run report:
`.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 7-B5-1155.md`.
