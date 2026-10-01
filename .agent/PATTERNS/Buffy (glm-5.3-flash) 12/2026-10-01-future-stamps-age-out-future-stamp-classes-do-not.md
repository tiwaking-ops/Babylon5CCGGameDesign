---
document:
  title: "A future-dated stamp self-heals by clock passage; the class heals only by adoption of measured time"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Future stamps age out; future-stamp classes do not

**Trigger (B5-1439 instance, refresh of the B5-1353 corpus):** the one
future-offset heartbeat of the prior census (`solar-pro4.json`, +741 min)
had gone dormant and now reads as a historical artifact — while the same
family's active heartbeat reads −0.7 min. The defect instance vanished
without anyone touching the file.

**Rule:**

1. **Instance-level healing is not class-level healing.** A future stamp
   becomes STALE-historical as time passes (its "age" grows until it is
   merely old); the class that produced it — extrapolating timestamps from
   arithmetic instead of sampling the clock at the write — persists in
   whatever writer last practiced it. A refresh census must therefore score
   the PRESENT generation of live writers, not tally historical artifacts.
2. **Deltas-only refreshes are the cheap tripwire.** Re-run the prior
   methodology unchanged, compare class counts (FUTURE count, live-family
   outlier set, per-agent offsets), and name only new outliers. A rising
   FUTURE count means the forward rule is not being followed; a falling one
   is evidence it is.
3. **Dormancy mtime discipline:** classify every outlier as live-family
   (heartbeat age within TTL) or historical (older) before drawing any
   conclusion about "the fleet" — a dormant file's offset is evidence about
   a dead session, not the current one.
4. **Record staleness you are not allowed to act on.** If your row forbids
   reaping, publish the three-signal evidence (claim mtime, owner heartbeat,
   report mtime) so the row that owns the reap can execute without
   re-measuring.

**Reusable lesson:** a future-dated stamp self-heals by clock passage once
its file goes dormant, but the fleet only stays healed by the
measure-the-clock discipline — zero new FUTURE stamps in a refresh is
evidence the adopted forward rule is being followed, not that the class is
extinct.
