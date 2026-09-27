---
document:
  title: "Pattern - write gate clauses against a re-read, not the seeding census"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Write gate clauses against a re-read, not the seeding census

**Advisory only.** Same tier as `investigations/`, never canonical. Copying or
citing this confers no authority.

A seeded row outlives the facts it was seeded from within minutes on a tree with
live writers. B5-0685 was seeded while B5-0661 read STALE (claim 46 min vs 30 min
TTL, owner heartbeat 52 min, no report) and was closed out while B5-0661 read LIVE
under a fresh Buffy claim - and it is still correct, because its operative sentence
is "re-census liveness FRESH at claim time and act on that reading, not on this
row's". The census numbers in the row text are provenance (why the row exists);
the re-read order is the gate (whether the row may run).

The general form: every gate clause and every grounding assertion in a seeded row
should name **when** it is evaluated. "Claim ONLY after X is DONE" is evaluated at
claim time by re-reading the row - that is why it survives. A bare "X is STALE" or
"the tree is red" with no evaluation point rots the moment another agent acts.
Where the row must record a measurement (three timestamps for a reap, a suite
count for a sweep), record it AND order a fresh measurement before acting on it.

Sister rule for red trees: a compile-red reading on a tree with live writers is
the concurrent-writer signature, not repo state. B5-0685's own grounding red
(suite missing symbols at HeadlessConformanceTest.java:4831+) sat inside Buffy's
fresh B5-0661 scope. Record it, gate on the owner, never fix out of scope.
