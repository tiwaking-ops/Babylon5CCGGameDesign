---
document:
  title: "Measure the premise before you debate the conclusion"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0954", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0954", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Measure the premise before you debate the conclusion

**Reusable lesson (B5-0954):** when a decision row hands you a measured premise,
re-measure the premise before you reason from it -- a seeded row can be hours
stale, and a premise that has quietly become false will silently select one of
your two branches for you.

## The failure this prevents

B5-0954 was a decision row with a clean binary: reap the orphaned B5-0481 claim,
or stand down because it belongs to a live session. Both branches were written to
be defensible, and the row helpfully pre-supplied the evidence for each -- the
claim's midnight `started_utc` argued for reaping, the absent owner heartbeat
argued for standing down.

The absent heartbeat was the load-bearing half, and it had quietly stopped being
true. When I measured it, `.agent/HEARTBEATS/solar-pro4.json` existed, carried the
exact `agent_id`, and was four minutes old. The row's cautious branch had not
become safer; it had become **factually wrong about its own stated reason**, while
happening to reach the right verdict.

Had I reasoned from the row as written, my report would have said "no heartbeat
resolves to this id" -- a false statement about the repo, filed under my name, in
a document whose entire purpose is to be the record a human decides from. A wrong
reason for a right answer is worse than a wrong answer, because it survives review
and it propagates: the next reader inherits the bad premise as settled fact.

## The tell

**A premise written in the row's own voice as "MEASURED this pass" is a
measurement that happened at seed time, and it is no fresher than the seed.** The
capitalisation signals confidence, not currency. Rows that quote timestamps, ages
and "about 605 minutes" are describing a *moment*, and moments pass.

So: when a row hands you its evidence, re-run the evidence. The cost is one
command. In this case it took under a minute and it inverted the analysis.

## What actually decides the case

Re-measuring was necessary but not sufficient. The rule did the deciding:
`.agent/HEARTBEATS/README.md` authorises a reap only when **all three** liveness
signals are STALE, and one of the three was LIVE. The mechanical bar, not my
judgement, is why the answer was "escalate" rather than "act" -- and saying so
matters, because a human being asked to authorise a deletion wants to know whether
the rule already forbids it or whether it is being asked to overrule one.

The supporting tool agreed independently (`ledger-query.ps1` printed
`LIVE` where it had printed `UNKNOWN` at seed time), and a tool changing its
verdict because the world changed -- not because the rule changed -- is the
cleanest available proof that the *premise* moved.

## The generalisation, including the part I nearly got wrong

The same reasoning applies one level up. B5-0811 and B5-0950 had recorded a
`census-crosscheck` red and attributed it to "an unresolvable foreign owner id",
reading it as a standing structural defect. Measured today the crosscheck exits 0
-- CONSISTENT, 457 rows. Their diagnosis described a **transient state that healed
when the owner wrote its heartbeat file**. An absent signal is the B5-0597
failure-3 class, and absent signals are transient by nature; a conclusion built on
one tends to be written as if it were permanent, because permanence is what a
report sounds like when it is not checked.

So the rule generalises past this task: **a recorded red is a claim about the past,
and a standing diagnosis needs a second measurement to deserve the standing.**
Especially when a prior note hands you a tidy root cause, because a tidy root
cause is exactly the thing a later agent will not think to re-test.

## Related

- The B5-0622 orphan rule (claim on a non-OPEN row: release, do not work) is what
  made this an orphan at all, and it is also why "delete it" was never available
  to me -- the file belongs to its owner.
- The reaper asymmetry ("a reaper that guesses is worse than no reaper") is what
  made the cautious branch correct in spirit; the three-signal rule is what made
  it mechanically mandatory. Both can be true, and when they are, the rule is
  what you cite in the report.
