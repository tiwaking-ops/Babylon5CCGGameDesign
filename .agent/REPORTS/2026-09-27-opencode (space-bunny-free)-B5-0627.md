---
document:
  title: "B5-0627 — the smallest task, filed anyway"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0627 — one clause

Human approval: fix the wording so the agent is not asked for something the loop
never does.

## The defect

`AGENT_LOOP.md`'s STOP condition read:

> Then report the run: iterations, tasks closed, **commit hash**, and every file
> deliberately left uncommitted.

Self-contradictory in a single sentence. It demands a commit hash *and* a list of
uncommitted files in the same breath, as though a run were expected to commit some
things and leave others. There is no middle case here: the loop commits nothing.

## Verified before changing the words

The claim "no commit hash can exist" is a claim about code, so it was checked rather
than asserted:

```
git commit appears in .agent/AGENT_LOOP.md   -> False
git commit appears in run-queue.ps1 template -> False
```

The invariant the fix asserts is one the code **already had**. Only the prose
contradicted it. That ordering matters — changing the wording first and checking the
code afterwards would have made the document the evidence for itself.

## The census, and what I did not touch

A repo-wide search for the phrase returned **31 occurrences**. Exactly **one** was a
live instruction — this clause. The other thirty were historical close-out reports
and ledger rows recording tasks that genuinely did commit, and were correct when
written.

All thirty were left alone. Rewriting a historical report so it agrees with present
behaviour falsifies the record, and a ledger is append-only for a reason. This is the
flip side of the B5-0625 lesson about stale pointers: fix the live instruction, leave
the archaeology.

## The replacement

> Then report the run: iterations, tasks closed, and every file you left uncommitted.
> **This loop does not commit** — step 7 has no commit step and the runner's task
> template has none either, so there is no commit hash to report. Committing and
> pushing stay human decisions; a run that ends with a dirty tree is a correct run,
> not a failed one.

The last sentence is the one that earns the edit. Without it, an agent handed the
original wording has two bad options and no good one: **fabricate a hash**, or
**report the run as broken**. Both are worse than reporting honestly, and both are
plausible — the first because the instruction is confident and the second because a
dirty tree looks like an error to anyone expecting a commit. Stating that a dirty tree
is the *correct* outcome removes the trap rather than documenting it.

## Verification

Re-read from disk after the edit, not trusted from the editor:

- no longer asks for a commit hash
- now states that the loop does not commit
- surrounding code fence balanced
- YAML frontmatter still opens on line 1
- em dash in the new clause is a real U+2014, and no mojibake or double-encoded
  sequence was written anywhere in the file

One clause of one document. No behavioural code touched, so no compile was required
beyond the standing gate.

**Reusable lesson:** a small fix can still be worth a full close-out, because the
reason to file it is not the size of the diff but that the defect was *live* — and
"live" is the only property that makes prose worth correcting. The discipline that
earns its keep here is the census: one live instance among thirty-one historical
ones, and the temptation to bulk-edit all thirty-one is exactly what would have
corrupted the record.
