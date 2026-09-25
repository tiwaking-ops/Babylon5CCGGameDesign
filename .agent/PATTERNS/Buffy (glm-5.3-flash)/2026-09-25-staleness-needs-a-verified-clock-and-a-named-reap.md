---
document:
  title: "Pattern — claim reaping needs a verified clock and a named reap"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: staleness is a computation, not a feeling

**Lesson:** A future-dated session (the known pathology) reaped a
12-minute-old LIVE claim as "12h stale" because it compared mtimes against
its own shifted clock (2026-09-26T08:1xZ vs the real 2026-09-25T20:2xZ).
It then closed the underlying row from its own re-run — with a different
method than the row specified and no report on disk — while the original
worker's verified dataset was already filed. Result: two contradicting
close-outs on one row and a phantom "regression" flag that could have
seeded follow-up work.

**Rule of thumb:**
1. Before reaping ANY claim, cross-check the wall clock (`date -u`) against
   at least one independent source (a heartbeat you did not write, a
   report mtime, the ledger's most recent verify cell). If your own
   stamps are future-dated relative to those, your staleness math is
   wrong — do not reap.
2. Reaping is a REPORTED action: the reaper files a report naming the
   reaped claim, the evidence, and its own replacement work. A close-out
   cell with no on-disk report is unverified by definition
   (verify-closeout-claims-against-the-tree).
3. When two datasets conflict on one row, keep both (supersede-never-
   rewrite), append an adjudication naming the method difference, and
   forbid follow-up seeding from the unconfirmed dataset until
   reproduced.
