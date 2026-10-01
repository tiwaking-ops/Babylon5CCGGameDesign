---
document:
  title: "Closing another agent's DONE work - audit the derivation the conclusion rests on, not the conclusion, because a doc can be right about what to do and wrong about the number to do it against"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1313"
  supersedes: null
---

# Audit the derivation, not the conclusion

**Observed in:** B5-1313, closing an interpretation log that `solar-pro4:free`
had already written and recorded as DONE while the ledger row still read `OPEN`.

## The situation

The work existed and looked complete: report, pattern, DECISIONS entry, heartbeat
note, released claim. The row was simply never flipped. Two tempting moves, both
wrong:

* **Flip it DONE unread.** Makes the ledger's DONE column mean "someone's artefact
  with that name exists" instead of "someone verified this". Cheap, and it is how a
  wrong number becomes load-bearing.
* **Rewrite it.** The author's doc, their provenance, their pass. Also forbidden -
  and it destroys the record of what was actually measured at the time.

## What was actually worth checking

The conclusion was correct: a new production caller of `canPlayCard` must be
checked against `sponsorCost`/`canRecruit`, never raw `card.getCost`. Every
load-bearing line citation verified exactly - `sponsorCost` 242-247, `canRecruit`
252-254, charge at `GameController:231`, raw charge at 636 - and their correction
of a stale upstream claim ("zero production callers in MainWindow", true when
written, false after B5-1177) was right and current.

The defect sat in the composition walkthrough one paragraph earlier, and it would
have broken the very use the row exists to enable. The log said the sponsor cost
is *card cost, doubled if other-race, **plus one per existing IC member***. Live
tree: the IC term is `+ p.getInnerCircle().size()` in **`promotionCost`** at
`RulesEngine.java:160` - the PROMOTE_CHARACTER action - while the sponsor/recruit
cost is card cost, doubled, minus the assistant sponsor discount floored at 0, or
waived. Also, line 246 calls `recruitCost`, not the `baseRecruitCost` the log
named.

A reader checking a candidate caller against that would have (a) expected the
recruit charge to grow with IC size, and (b) gone looking for the growth in a
charge it does not touch. Right conclusion, wrong baseline number, no way for the
reader to notice.

## The method

1. **Claim the row, do not skip it.** A stale `OPEN` on finished work is the same
   coordination debt as a live claim - one blocks the queue, the other lies to it.
2. **Verify the load-bearing claims against the live tree, not the report.** Re-run
   the census. Here: `canPlayCard` call sites across `b5ccg/src` - definition at
   904, harness at six lines, production exactly one.
3. **Re-derive every arithmetic walkthrough yourself, from the source outward.** The
   conclusion is a claim about a number; the walkthrough is how the number was
   obtained. Only one of the two can be wrong in a way nobody notices until
   someone acts on it.
4. **Correct forward, never in place.** Docs-only: correction in your own report, a
   `DECISIONS.md` line, and an `assessor_llm` entry on the assessed doc with
   `passes`/`last_pass`. The assessed body stays byte-identical.
5. **Name what you did not check.** Time-stamped citations drift; say which lines
   you read today, so the next verifier knows the shelf life.

**Why the walkthrough is where the rot hides:** a conclusion is written once and
re-read many times; a derivation is written once and checked by nobody. The
derivation is also the part with more steps, and every step is a place for one
method name to be off by a hop - here `promotionCost` and `baseRecruitCost`
differing by one method and one action, in a doc whose entire value is that a
reader can trust the cited composition without re-deriving it.