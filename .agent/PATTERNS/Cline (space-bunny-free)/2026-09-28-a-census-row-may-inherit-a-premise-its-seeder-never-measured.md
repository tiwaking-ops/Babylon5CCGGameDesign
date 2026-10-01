---
document:
  title: "A census row may inherit a premise its seeder never measured"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A census row may inherit a premise its seeder never measured

**One line:** when a row's own text describes the state of its subject ("swept into
history", "already gone", "superseded"), that description is a premise like any
other - measure it with `git show <sha> --stat` and `git ls-tree HEAD` before
reporting on it, because the two readings imply opposite actions for the row that
inherits the decision.

## The case

B5-0767 was seeded to run an attribution census over five scratch diagnostics
"named ... swept into history by checkpoint 042c3592". The census was to classify
each file so B5-0737 could decide keep-or-delete without redoing the forensics.

Two commands inverted the premise:

```
git show 042c3592 --stat   ->  the four .ps1 files listed as INSERTIONS (14/104/13/14)
git ls-tree HEAD -- .agent ->  all five still present in HEAD 02716363
```

042c3592 **added** the files. They were committed into history, not swept out of
the tree, and they are in the current tip. The follow-on measurement mattered just
as much: four of the five had **staged but uncommitted deletions** in the index, and
the fifth was untouched and live in the working tree. So the state was neither
"gone" nor "present" but "present in the tip, with someone partway through removing
it" - a state the row's own wording did not name and would have led a reader to
mis-describe.

## Why this recurs

The seeder wrote the row from a narrative it had been told, not from a measurement
it had taken. Nothing in the row reads as unchecked: the commit sha is real, the
five filenames are real, the count is right. Every token is correct and the
conclusion drawn from them is still wrong - the B5-0614 failure shape, where the
artefact and the assertion about the artefact come apart.

It is also the more dangerous direction. "Already gone" invites a reader to skip the
forensics, or to treat a delete as a formality; "in the tip, mid-deletion" demands
both. The cost of believing the row is a report that tells the inheriting agent the
content is unrecoverable when one `git show` would have handed it over.

## The check, and its two-command form

1. `git show <named-checkpoint> --stat -- <paths>` - does the checkpoint **add** or
   **remove** them? "Swept into history by X" should read as a deletion; a stat
   full of `+` is the opposite and settles it.
2. `git ls-tree -r HEAD -- <dir>` - are they in the **current tip**? This is the
   question that decides recoverability, and it is not the question the row asked.
3. `git status --porcelain` / `git diff --cached --name-status` - is someone
   *mid-deletion*, uncommitted? Never complete or revert that yourself; it is
   another writer's in-flight edit and usually another row's git scope.

Three commands, and the census becomes a census instead of a confirmation.

## The generalisable form

A row that reports on a subject inherits that subject's *description* as well as
its *location*, and only the location gets checked. Whenever the row text carries a
verb about the subject's fate - swept, gone, superseded, landed, orphaned - run the
command that could make that verb false before repeating it downstream. The verbs are
load-bearing; the filenames are the part that gets verified by reflex.

## Related

The sibling case in this repo is the gate that names a superseded ID: B5-0763
(`2026-09-28-a-superseded-prerequisite-makes-a-downstream-gate-permanently-unsatisfiable.md`).
Same shape one level up - a row states a relationship between two artefacts, the
relationship is checkable in one command, and nobody checked it. Both patterns
reduce to the same instruction: **the row's claim about the world is data, and the
row's subject is not evidence for it.**
