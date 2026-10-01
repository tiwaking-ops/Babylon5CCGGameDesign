---
document:
  title: "An abandoned diff is evidence, not litter"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0977-take2", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# An abandoned diff is evidence, not litter

**Rule.** When you reap a stale claim, the dead agent's uncommitted working-tree
edits are *evidence about the task*, not litter to revert. Read them, gate them
with the row's own acceptance criteria, and close them out if they are correct.

**Why.** Reverting is the reflexive move and it throws away the one thing the
dead session produced. Worse, the edit is frequently *nearly* right, so shipping
it unexamined is just as bad as reverting it: on B5-0977 the predecessor's diff
was a correct in-place fix of the exact top-ranked finding, and nothing in the
task's normal gate set would have objected to it.

**The tell.** Read `git diff --stat` for **0 deletions**. Zero deletions on a
paint/layout fix is the signal that the old block was *replaced* rather than a
second copy being stacked on top of it. Confirm it with a search for the draw or
call site — expecting exactly one. A duplicated readout passes compilation,
conformance and smoke tests; only the diffstat and the search catch it.

**Corollary on the claim itself.** Do not trust a runner's "the claim file is
absent" as a substitute for looking. On B5-0977 the file was present and 35 min
stale. Re-verifying first is what kept a second writer out of one `ui/` scope.
And when the owner heartbeat *exists and parses*, the three-signal verdict is
`STALE` rather than `UNKNOWN`, which is the difference between a permitted reap
and a forbidden one.

**Scope discipline still binds.** Correct code found in a dead agent's diff does
not widen your claim. The B5-0805 playtest-guide caveat went stale the moment the
fix landed, and fixing it would have meant editing documentation outside a
`ui/`-scoped row — so it was flagged for a follow-up row instead.

**Reusable lesson:** gate what you inherit with the same rigour as what you
wrote, and let the diffstat tell you whether the fix replaced or duplicated.
