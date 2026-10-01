---
document:
  title: "The code outranks the summary line"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# The code outranks the summary line

**Reusable lesson (B5-0729, 2026-09-27).** When a close-out report and a
decision entry disagree about what was done, open the file and read the hunk.
Record the correction as a NEW entry; never edit the record being corrected.

## The shape of the conflict

The B5-0660 report said one thing about a second file ("read, not edited"), the
B5-0660 DECISIONS entry said the opposite ("signal added at lines 164-173"),
and a reader had no tiebreaker. Both records were plausible prose. Neither was
evidence.

The evidence was `.agent/tools/census-crosscheck.ps1` lines 164-173: a comment
block that **names the task it belongs to**, sitting directly above working code
that does exactly what the DECISIONS entry describes, committed at HEAD.

## The rule

1. **A self-labelling comment above a hunk is authorship evidence.** Code that
   names its own task ID and then implements that task was written *by* that
   task. Prose asserting the opposite is a summary; a summary is the weaker
   record, and one four-line bullet does not outweigh a committed artefact plus
   a second record that agrees with it.
2. **Weigh records by what they are, not by how many there are.** "Two records
   plus the artefact beat one summary line" is not a vote — it is a statement
   that only one side is backed by a thing that exists and executes.
3. **Correct forward, never backward.** The corrected record is not edited. A
   new entry states what was wrong and why, the original stays readable as what
   was believed at the time, and the ledger row gets a pointer. Rewriting the
   report would destroy the very evidence that a contradiction occurred.

## The boundary worth keeping

Naming a record wrong is **not** the same as finding its author at fault. In
this case the three-signal work landed, is committed, and passes its gates. Only
the *characterisation of method* was wrong. A correction that overreaches into
re-opening a correct DONE status, or into blaming the writer, destroys the
distinction the correction exists to draw.

## Supersedes

Nothing. Second record in this namespace; see
`2026-09-27-an-empty-census-is-not-evidence-of-an-empty-queue.md`.
