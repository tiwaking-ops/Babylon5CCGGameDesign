---
document:
  title: "A per-session identity cannot correct its own tombstone"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A per-session identity cannot correct its own tombstone

**Task:** B5-0962 (non-conforming heartbeat adjudication, 2026-09-28).

**The trap.** "Owner-delivered correction" assumes the owner still exists. Under the
R1 ruling an `agent_id` names a *session instance*, and the per-task identity pattern
(`Cline (space-bunny) b5-0941`) creates sessions that end — permanently — with their
close-out. A schema violation inside such a session's final heartbeat is not a
correction waiting for its owner; it is a tombstone with a typo. Waiting for an owner
who cannot return is how a store stays permanently red.

**The adjudication shape that worked.**
1. Re-measure the census yourself (the store had grown 67→69 files since seeding;
   the miss stayed singular — that isolation is what makes a tombstone verdict safe).
2. Read the file for *all* defects, not just the one the validator names: the same
   payload carried a future-dated `utc` the validator passes in silence. A validator's
   output is a lower bound on a file's defects, never an inventory.
3. Prove the defect is behaviourally inert before leaving it: `state` has no consumer
   in the liveness path (three-signal + `live_claims`), the closure information is
   already recorded in the ledger and reports, and the liveness signal is mtime-based
   (measured: a heartbeat whose payload `utc` was hours ahead still aged correctly).
4. Then the pointer report is enough: name the file, record both defects, state the
   one-line fix and who may apply it, leave the bytes identical. The validator's exit 1
   is correct output, not an outage to fix.

**And the reason this is not laziness:** editing another agent's heartbeat is
fabrication under AGENTS.md §6 even when the edit would make the store green, and a
green store bought by a forbidden edit is worse than a red store with a pointer to
the exact byte that needs a human.
