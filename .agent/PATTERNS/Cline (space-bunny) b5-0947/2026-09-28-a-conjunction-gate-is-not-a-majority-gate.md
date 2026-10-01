---
document:
  title: "A conjunction gate is not a majority gate"
  status: "Reusable pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0947", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0947", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A conjunction gate is not a majority gate

**Filed from B5-0947** (2026-09-28), which was claimed, measured, and released
BLOCKED without producing its deliverable.

A task row may gate itself on a list: *"claim ONLY after A plus B plus C plus
D … are all DONE."* Seven of those eight reading `DONE` is **not** partial
progress toward the gate. The gate is a conjunction; a conjunction is red until
**every** member is green, and the temptation to treat "nearly all done" as
"ready" is strongest exactly when the work looks nearly finished.

Three things that made the red gate cheap to honour rather than expensive to
discover late:

1. **Measure the conjunction as a table before claiming, not prose after.**
   One row per member, one evidence column naming the artefact that proves the
   status. Seven of my eight rows resolved in a single read.
2. **Name the missing member in the report.** "B5-0939 has no report anywhere"
   is the whole finding; a reader must not have to re-derive it.
3. **Distinguish *green build, red gate* from *red build*.** They are different
   close-outs with different remedies. I ran the compile anyway and reported it
   green, so the block is unambiguously on the dependency and not on the tree —
   and so nobody re-runs the build hunting for the cause.

## The companion rule: never publish a partial census

The deliverable was a *census* of nine batch reports. Seven existed.

Because task status is keyed by **task ID**, a census covering 7 of 9 batches
published as `DONE` is **indistinguishable from a complete one** to every later
reader — the row is closed, the ID resolves, and the 58 missing titles are
invisible. This repo has already been bitten by the sibling form: B5-0943's row
note records a competing stub close-out that asserted `DONE` with no
transcription data, so ledger-wide DONE-plus-report scans now double-count it.

So the correct move for a partial result is to **publish it labelled as partial
and close BLOCKED**, never to round it up to `DONE`. A census with a stated
denominator is useful; a census with an implied one is a trap.

## And check the premise, not just the prerequisites

The row asked for a ruling *"per B5-0654"*. B5-0654 had been **withdrawn by
human ruling** two hours earlier (B5-0821), with no replacement stated.

Prerequisite status is the *mechanical* gate. It says nothing about whether the
instruction is still executable. A gate can be fully green and the task still
unroutable, because the authority the task would exercise has been retracted.
When a task names a ruling in its instruction text, **resolve that ruling's
current status as part of the same check** — it is one grep, and it converts a
guaranteed-garbage deliverable into a clean BLOCKED.
