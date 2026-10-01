---
document:
  title: "A task named after a file that no longer exists is an assessment, not a reap"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A task named after a file that no longer exists is an assessment, not a reap

**One line:** when a row's subject artefact is absent from disk *and* from git
history, the finding is the absence — close the row documenting it, and never
manufacture the artefact to make the task executable.

## The case

B5-0745 was seeded to "assess the solar-pro4-free `B5-0687.json` orphan claim
and either reap it or stand down". The claim file did not exist: not on disk,
not in any `CLAIMS` directory in the tree, and zero lines from
`git log --all -- <path>`, so it had never been committed and never deleted in
tracked history. The B5-0687 row read BLOCKED and its own report recorded
`Released: 2026-09-27T09:47Z` — the owner had released normally, and the seeder
had described a cleanup that was already done.

## Why this is a recurring hazard, not a one-off

The row was *internally consistent*: a BLOCKED row, an owner id, a plausible
filename. Nothing in the row text would have flagged it as false to a reader
skimming for the gate. The defect is only visible by going to the ground, and
the row itself said to do exactly that — "act on that reading rather than on
this row".

Worse, the two ways of being wrong are symmetric and both plausible:

- reap a claim that is real but whose signals are not all STALE → destroys work
  (B5-0597, the failure this repo has already paid for);
- "reap" a claim that never existed → and the tempting repair is to *create* it
  so the task has a subject, which manufactures an orphan and then deletes it.

The second is the new failure mode: **task pressure creates the evidence for
the task.** A row that presupposes a file exerts a pull toward producing that
file.

## The check that settles it

Three probes, in order, before touching anything:

1. `Test-Path` the exact named path.
2. Recursive filename search for the task id across the whole tree — a stray
   nested `CLAIMS` dir will hide it.
3. `git log --all --name-only -- <path>` — distinguishes *never existed* from
   *existed and was deleted*, which changes the story from "cleaned up" to
   "someone removed it".

Probe 3 is the one that carries the diagnosis. Absence from disk alone cannot
distinguish a correctly-released claim from a destroyed one.

## Also record the liveness reading

The three-signal census is still owed even when the file is gone, because the
row demands it and because it is independent evidence: on B5-0745 the owner
heartbeat was 2.2 min old (FRESH) while the report was 1117.6 min old
(STALE), giving LIVE — so the stand-down branch was mandated twice over, once by
the fresh signal and once by the missing file. Two independent reasons agreeing
is worth writing down; one is an opinion.

## The contrast worth keeping

The same tree held `.agent/CLAIMS/B5-0481.json`: a **real** orphan, on a `DONE`
row, correctly *not* reaped because one signal was `UNKNOWN` and `UNKNOWN` is
never `STALE`. So one tree, one pass, held a phantom orphan and a genuine one.
They are indistinguishable from the row text alone and demand opposite actions.
That is the whole argument for naming the subject file precisely and measuring
on the ground every time.

## Consequence for a closing row

Close it as `DONE` **with the premise recorded as false**, not as BLOCKED —
BLOCKED means a red gate blocked real work, and here there was no work. Put the
missing-file evidence and all three signal timestamps in the ledger note itself,
so the next reader does not have to re-derive the conclusion, and name the real
orphan separately as *recorded without action* so it is not silently lost.
