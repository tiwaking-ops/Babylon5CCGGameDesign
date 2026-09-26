---
document:
  title: "Pattern: verify the combined tree when twin sessions race a row"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# verify-combined-tree-on-twin-race

**Trigger:** While working a claimed row, the file is edited by another writer,
your claim file disappears, and a concurrent close-out under your own
agent_id family lands. The 2026-09-23 twin notes and B5-0482 both show the
failure mode: re-implementing over their work, or reverting to "win" the row.

**Response sequence:**

1. Stop editing immediately (B5-0344 clean-yield). Do not delete, reword, or
   "correct" their bytes — even fragments you believe are wrong.
2. Read their report before judging. Their claims may describe work you have
   not seen, and yours may be invisible to them.
3. Verify the COMBINED tree: build + run the row's own gate against the
   merged working state. Complementary fixes usually compose; only the gate
   verdict decides, not whose code shape survived or which check count prints.
4. Record the adjudication as a ledger note or verification addendum (never
   inside their report or row cells — supersede-never-rewrite), with the
   real-UTC timeline, who edited what, and the verification command results.
5. Leave claim-file ownership disputes as notes, not counter-deletions.

**Evidence from B5-0482 (2026-09-26):** this session seeded
StarterDeckBuilder.setRandomSeed + javadoc under a valid claim; the twin
landed driverRng + soft-gate + report + row close-out concurrently and
deleted the live claim. Combined tree verified green and PASS; both edit
sets survived; zero bytes of theirs touched.

**Related:** 2026-09-23 twin-collision ledger notes; B5-0403 timestamp
adjudication (a future-dated clock makes live claims look stale — verify
against independent wall-clock sources before reaping).
