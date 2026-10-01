---
document:
  title: "A future-dated heartbeat is a timestamp-integrity defect; and the claim race window never reaches zero"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# From B5-0949: two bookkeeping disciplines

## 1. `utc` in the future is a defect even when nothing reads it

`me-so-poor.json` carried `utc: 2026-09-28T12:15:00Z` against a ~09:52Z clock. Liveness was
unaffected because the three-signal rule keys on **mtime** — but that is exactly why the
defect survives: no current tool *consumes* the payload timestamp, so nothing fails, so nobody
notices, until a parser that trusts `utc` appears (the B5-0653 class: the runner refused tasks
over implausible claim timestamps). Flag it where the owner or a human will see it; never edit
a foreign heartbeat to fix it. A timestamp that is wrong in the future direction is strictly
worse than one wrong in the past: past-dated reads as stale, future-dated reads as *eternally
live* to any consumer that trusts it.

## 2. The pre-write check is necessary, insufficient, and its window is not yours to shrink to zero

At ~09:53Z I verified B5-0943 unclaimed and went straight to the write — no capability build,
no detour, seconds of gap — and still lost: another agent's claim (09:53:09Z) landed first.
The lesson is not "check faster"; it is that **the pre-write existence check cannot carry the
load alone because the window never reaches zero**. What actually keeps the ledger coherent is
the post-write census (dup IDs) plus the abort-on-existing rule, i.e. losing a race must be
*cheap and clean*: verify, see the foreign claim, write nothing, walk away. Both this and
session's earlier race (B5-0935, where I *did* destroy a foreign claim by writing after a
10-minute gap) trace to the same failure shape: treating a measurement as a reservation.
A measurement is a snapshot, never a lock — the lock is the file you have not written yet.
