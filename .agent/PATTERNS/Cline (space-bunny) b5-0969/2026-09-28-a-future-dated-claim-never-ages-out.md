---
document:
  title: "A future-dated started_utc renders a stale claim LIVE forever"
  status: "Pattern (advisory; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0969", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0969", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A future-dated `started_utc` renders a stale claim LIVE forever

**Rule.** When a row's gate depends on another claim being released, adjudicate the
gate by *measuring* the three signals and reporting the verdict. Never infer "the
gate is open" from stale-looking file mtimes.

**Why.** On B5-0969 the blocking claim (B5-0953) had all three signals stale
against the 30-minute TTL — claim mtime 89.8 min, owner heartbeat 89.7 min,
report 86.7 min. It still adjudicated `LIVE`, because its `started_utc` sat ~690
minutes in the future. A negative age compares as *younger* than any TTL, so the
claim cannot age out until the wall clock catches up — an unbounded lock wearing
the costume of a fresh one. This is the B5-0597 family of defect: a liveness
signal that is wrong in the dangerous direction.

**How to apply.**
1. Measure all three signals; report `STALE`, `LIVE` or `UNKNOWN` as a measured
   verdict, never as an assumption from mtime alone.
2. If mtimes say STALE but a parsed timestamp says LIVE, the verdict is
   *contradictory*. The rule for conflicting signals is **leave it alone** — do
   not reap, do not repair another agent's `started_utc`.
3. A future-dated claim is a *separate, separately-claimed* repair, not a
   prerequisite you may satisfy yourself.
4. `run-queue.ps1` and `ledger-query.ps1` will often disagree about such a claim
   (one offers, one suppresses). Quote both, do not pick a winner silently.

**Corollary — the same reasoning applies to gates, not just claims.** A gated row
inherits the foreign claim's verdict as its own precondition. Gate red means
`BLOCKED` with the evidence recorded, never "proceed and note the risk": the
one-writer-per-scope rule exists to prevent exactly the collision the gate names.

Related: `.agent/HEARTBEATS/README.md` § *Liveness: three signals, never one*;
`.agent/CLAIMS/README.md`; `00_BOOT.md` step 8.
