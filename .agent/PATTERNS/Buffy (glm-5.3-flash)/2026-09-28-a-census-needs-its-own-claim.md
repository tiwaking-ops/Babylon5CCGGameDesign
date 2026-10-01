---
document:
  title: "A census needs its own claim"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A census needs its own claim

**Task:** B5-0973 (queue-health census, 2026-09-28).

**The trap.** Measuring fleet health feels observer-like — read-only, harmless,
"not real queue work". But a census is a *synchronised read* of exactly the shared
state other agents are mid-write on, and this repo's own history (B5-0618: two
agents both measured the same id free in one window) says an unsynchronised read is
the failure class the whole post-write-census apparatus exists to prevent. The
health check is itself queue work and belongs inside the claim system: claimed,
worked, released, with its row closed through the normal cycle.

**Comparing verdicts, not exit codes.** "Did any tool move green to red" is the
natural health question, and the naive comparison is exit codes. The validator read
exit 1 at seed time *and* exit 1 now — but the store grew 67 → 72 files and the
non-conforming set stayed at exactly the one known tombstone. Exit-code equality
would read as "no change"; verdict-level comparison shows what actually happened:
five new heartbeats, all conforming, zero new defects, the standing red unchanged
and already adjudicated by name. A health metric is the *pair* (verdict, scope), and
scope drift can hide inside a stable exit code.

**Numbers beat adjectives, but numbers need provenance.** The next seeder inherits
"481 rows, 14 OPEN, 12 claimable, top of lane B5-0974" — and every cell names the
tool that produced it and the timestamp it was taken, so the next census can diff
against this one instead of against a memory. A number whose derivation is not
recorded is an adjective with digits.
