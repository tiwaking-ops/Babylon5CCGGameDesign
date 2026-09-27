---
document:
  title: "When a concurrent writer pins the shared gate red, verify your scope in a mirror"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# When a concurrent writer pins the shared gate red, verify your scope in a mirror

**Source task:** B5-0677. **Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

My task's gate is repo-wide (`compile.bat` + suite), but a concurrent live
claim (B5-0679) held `AIPlayer.java` mid-edit — twice red with two different
transient errors, then stalled for 10+ minutes. Fixing their file is
forbidden (their claim, their bytes); waiting indefinitely stalls my
close-out; marking my row BLOCKED would misreport my own scope, which was
complete and clean. The honest middle path: **mirror the tree in %TEMP%
exactly (uncommitted bytes included), substitute ONLY their contested file
byte-identical from `git show HEAD`, and run the full gate in the mirror.**
Result: 0 errors, 609/609, smoke PASS — proof at the exact bytes my claim is
responsible for, with their work excluded rather than judged.

Two sub-lessons from the same hour: a harness that silently skips a build
step (the resource copy) reports failures that are its own; and a mid-paste
stall (file quiet but red) is distinguishable from an active edit by mtime —
check before waiting, and never edit a file under someone else's live claim.

## The transferable rule

When the shared gate is red from a live claim outside your scope:

1. Confirm the red is theirs (error file × claim scope) and not transient
   chaos — rerun once after a short wait.
2. Mirror the tree; restore the contested file from `git show HEAD` (or the
   last known-good committed state) so their uncommitted bytes are neither
   lost nor relied upon.
3. Run the full gate in the mirror; report the live-tree red as a
   concurrent-writer disclosure with the excerpt, never as your own failure
   and never silently.
4. Close your row on mirror evidence; the live gate returns green when their
   leg lands — which a later re-sweep row (B5-0683-class) re-verifies.

## Anti-patterns this heads off

- Fixing their file "to unblock the gate" — editing inside a live claim
  destroys the one-writer guarantee the claim exists to provide.
- Reporting the row BLOCKED for an out-of-scope red — that trains the ledger
  to record someone else's WIP as your defect.
- Waiting indefinitely for a stalled writer — mtime tells you whether you
  are waiting for an edit or for a corpse.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0677.md`.
