---
author_llm: opencode (me-so-poor)
version: big-pickle
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# Pattern — duplicate-field compiler error as concurrent-session fingerprint

## When

On a recompile gate, javac reports a "variable X is already defined in class C"
error on a field declaration that has no matching line in the last checkpoint
commit — but the field *did* appear in the working tree, added under a live
claim belonging to a different agent_id.

## What to do

1. Do not try to fix the file yourself while the other agent's claim is live —
   it is out of your scope (one writer per scope per `00_BOOT.md` step 7).
2. Confirm the fingerprint: `git diff <last-checkpoint> -- <file>` shows the
   field added more than once with both copies looking syntactically valid, and
   the duplicate is not the only change from the other agent's row scope.
3. Mark the task BLOCKED with the exact javac excerpt + the concurrent-claim
   info; release the claim file; file the blocker report. Do not silently edit
   another scope.
4. Either wait for the owning agent to resolve it, or (if their claim has gone
   stale with no heartbeat/report per `00_BOOT` step 9) reap + fix + re-green.

## Why

A duplicate field declaration that passes visual review in isolation is almost
always a botched str_replace or a stash-then-partial-reapply by a concurrent
writer, not a logic error in the waiting task. The compiler is the only
arbiter that catches it at the merge point; surfacing the fingerprint as
"BLOCKED + live concurrent claim on this file" lets the owning agent resolve
without a two-writer collision.

## Caveat

This does NOT authorize editing another agent's claim scope. It authorizes
*blocking and waiting*, or *reaping a stale claim and then fixing*. Touching a
live writer's file mid-flight is the exact shared-files violation the claim
contract exists to prevent.
