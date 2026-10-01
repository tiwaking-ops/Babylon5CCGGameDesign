---
document:
  title: "The claim-existence pre-check is a sample, not a lock"
  status: "Advisory pattern (shared pattern store, AGENTS.md section 6)"
  provenance:
    author_llm: {name: "Cline (space-bunny)", version: "Space Bunny"}
    assessor_llm: []
  created_date: "2026-09-28"
---

# The claim-existence pre-check is a sample, not a lock

**Lesson.** Re-checking that `.agent/CLAIMS/<task-id>.json` is absent immediately before
writing it does not make the write safe — it narrows the race window to whatever remains
between that check and the write, and two agents in the same loop iteration can still
sample the same free slot. The only thing that makes the claim safe is a **post-write**
observation plus the fact that the loser *aborts* rather than proceeding.

**Observed 2026-09-28, B5-0833.** A runner pre-check reported
`.agent/CLAIMS/B5-0833.json` absent (4 claim files listed, no B5-0833). A separate boot
check `ls .agent/CLAIMS/` also read absent. Eight minutes later, a third check found the
file present, `LastWriteTimeUtc 2026-09-28T07:28:40Z`, `agent_id: me-so-poor` — written
**24 seconds** before the third check, and after both earlier checks. The row was still
`OPEN` and the owner's heartbeat still read `live_claims: []` with `state: idle`, so the
newest-of-three-signals verdict was `LIVE` purely on claim age. Correct action was to
abort before writing anything, not to "take it because the runner told me to".

**The asymmetry that makes this cheap.** The pre-check being wrong is harmless *if* the
loop is written so that a stale pre-check cannot cause damage. A claim file is the lock
and the *work* is the damage: an agent that verifies the file's absence, loses the race,
and proceeds anyway will do the work concurrently with the real owner, and two reports
will describe one task. Verifying existence **again** after finding it present, and
then exiting, converts a corruption into a no-op.

**Corollary — an idle heartbeat is not a free task.** The owner's `live_claims: []` and
`state: idle` would both suggest the claim was abandoned, and both are non-signals under
the three-signal rule: `live_claims` is a positive assertion about the *owner's* holding,
and it can lag the claim write by the length of one refresh cycle. Read claim mtime
alongside the payload, as `.agent/HEARTBEATS/README.md` requires, and treat the newest
signal as decisive.

**What the pre-check is still good for.** It is not decoration — it is what makes the
*normal* path cheap, and it is the only signal available before a file exists. Delete it
and every iteration pays a create-then-back-out. Keep it, and additionally re-read it
immediately before the write, and additionally treat a found file as terminal for that
item. The fix is not a better pre-check; it is that the post-observation must be
authoritative.

**Not a defect in this repo's rules.** `.agent/CLAIMS/README.md` and `.agent/AGENT_LOOP.md`
step 2 already say "if the file exists it is taken -- abort and take another", and
`AGENT_LOOP`'s § *The three ways this loop has lied to itself* #1 already names this
exact failure. This record adds only the measured timeline.

**Reusable lesson:** a pre-write check that races another writer is not a lock; make the
*abort* the load-bearing part of the protocol and the check is free to be as stale as it
likes.
