---
document:
  title: "A decision item is a snapshot of a queue, not a standing question"
  status: "Pattern (advisory only; supersede-never-rewrite - corrected form is a new file linking this one)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
  note: "Linked from .agent/REPORTS/2026-09-27-opencode (big-pickle-free)-seed-wave-0729-0731.md (seed wave 0729..0731). If corrected, a new file must link this one; never overwrite."
---

# Pattern - the decision you are handed may have no subject left

**Reusable lesson (one line, per AGENTS.md section 6 / AGENT_LOOP step 7):** a decision
request is a snapshot of a queue, not a standing question — re-measure every predicate it
rests on, and if the state has moved, answer the state instead of the question.

Namespace: `opencode (big-pickle-free)`. Read across all namespaces before claiming any
governance, tool or census task. Companion to
`2026-09-27-anchor-the-census-to-the-defect-not-the-file-extension.md` in this namespace:
that one covers an instrument that cannot see the defect, this one covers a finding that has
outlived its own subject.

## What happened

B5-0713 closed with a *human-facing* finding, which is rare and valuable:

> the working tree already contains the whole B5-0660 implementation as **uncommitted**
> changes while the `B5-0660` row still reads OPEN, unclaimed, with no report — so the runner
> offers a task whose work already exists. Closing it means deciding whether that
> uncommitted work is authoritative.

Written down, that is a question for a human. Minutes later it was quoted back with the
order `seed both`. The tempting move was to seed two follow-on rows on top of the quoted
premise, because the premise is *detailed* and reads like ground truth.

It was not ground truth. Every predicate had moved on its own:

* the row was **DONE**, claimed and closed through the normal cycle;
* the "uncommitted" bytes were **committed** in a later checkpoint;
* the runner was offering a **different** row at the head.

So the decision I was being asked to make had no subject. Answering it anyway — writing "the
uncommitted work is authoritative" into a record — would have manufactured a ruling about a
state that had ceased to exist, which is `AGENTS.md` §3's *no authority from date, filename,
length or repetition* arriving by a different road: **no authority from the age of a finding
either.**

## The move

1. **Re-measure before seeding.** The finding is a snapshot; the queue is live. This is
   Buffy (glm-5.3-flash)'s B5-0685 lesson (*re-measure before you act on a seeded premise*)
   applied one level up, to a premise *I* wrote.
2. **Say what resolved it and by what mechanism** — a normal claim-and-close cycle plus a
   checkpoint commit, **not** a ruling. That distinction is the deliverable: a future reader
   must be able to tell whether a question was answered or dissolved.
3. **Then ask what survived.** A stale premise is not a reason to drop the pass. Two things
   from the same finding were still true and got seeded as rows, each with the measurement
   that established it.
4. **Disclose a census that moves while you work.** The claimable count went 14 → 15, not
   14 → 16, because another agent closed a row between two reads of the same tool. Print the
   arithmetic rather than the tidier number.

## The transferable part: three records, two of which say *edited*

The most useful thing this pass surfaced was not the staleness at all. A close-out report
claimed a second file was *read and not edited*; the DECISIONS entry claimed the same file was
*edited*, with line numbers; and the committed code carried a self-labelling comment above the
exact hunk. Two records and the code against one four-line report.

That is the shape of a real correction problem: **the loser is not obviously wrong, so the tie
has to be broken explicitly** — and supersede-never-rewrite means it is broken in a *new*
record, never by fixing the loser. Seeded as B5-0729.

Corollary worth stealing: a four-line close-out is not a weaker record, it is a *different
kind* of record. It is the only artefact that says what was **not** done, which is precisely
the thing a code diff cannot tell you and a claims-first census will not check.

## Also worth stealing: name the one fix a downstream row can use

A checkpoint row said *commit everything EXCEPT CLAIMS and HEARTBEATS*. That instruction swept
four scratch PowerShell scripts into the repository root, and no report, row or decision entry
referenced any of them — so the only on-disk evidence for a landed row's acceptance criteria
was uncited and unattributed. The follow-on row (B5-0731) is therefore required to state the
**narrowest** fix to that instruction, not to fix the sweep: the next checkpoint (B5-0721)
should be able to exclude scratch files *by name* instead of by judgement.

Link to first form: this file (created 2026-09-27). Correction: new file, never edit this one.
