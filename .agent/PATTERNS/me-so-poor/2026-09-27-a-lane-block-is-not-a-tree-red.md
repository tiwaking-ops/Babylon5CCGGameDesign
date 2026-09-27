---
document:
  title: "A claim against overlapping in-flight work is not a red tree — it's a lane block"
  status: "Advisory pattern (not canonical)"
provenance:
  author_llm: {name: "me-so-poor", version: "unknown"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A claim against overlapping in-flight work is not a red tree — it's a lane block

**Source task:** B5-0681 (Civil War engine law steps 2-4). **Record:**
2026-09-27, me-so-poor.

## The lesson

The row gate is a row-status check (B5-0661 DONE), not a scope-liveness
check. Row gates can read green while the claimed scope is occupied by live
concurrent claims on files I never wrote — and an in-flight claim is not
compile-red, it is lane-blocked. A row gate being green does not mean
"the tree is ready for my edits"; it means "the row's own prerequisite is
satisfied". The scope-gate lives in the claim file's scope array and the
shared-files protocol (one writer per scope).

## The transferable rule

When a row gate passes but a scope check fails, classify the block, do not
blame the tree:

1. **Lane block** — one or more live claims overlap the claimed scope but
   have not finished their own rows. Solution: wait for those claims to
   release. Close out this row as BLOCKED with the overlapping claim IDs
   and heartbeat timestamps logged, release the claim, and re-claim later.
2. **Tree red** — compile fails for a reason unrelated to concurrent edits.
   Mark BLOCKED per 00_BOOT step 8 with the log excerpt and stop.
3. **Scope mismatch** — the row's claimed scope is too wide or too narrow.
   Do not silently narrow the scope; close out with a measurement note.

A block is not a failure of the row or the tree. It is a coordination
signal.

## Anti-patterns this heads off

- Treating an in-flight claim as a stale file and writing through it — that
  corrupts the other claimant's work and invalidates the three-signal
  liveness rule.
- Closing a row as DONE without verifying its scope is free — ownership is
  per-scope, not per-row.
- Ignoring the row gate and skipping straight to compile — the gate is
  ordered; if it is red the row's prerequisites are not satisfied and the
  tree may not be ready.

**Filed alongside:** `.agent/REPORTS/2026-09-27-me-so-poor-B5-0681-BLOCKED.md`.
