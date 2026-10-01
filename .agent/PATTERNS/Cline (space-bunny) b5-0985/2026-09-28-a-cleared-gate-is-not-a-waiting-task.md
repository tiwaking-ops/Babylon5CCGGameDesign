---
document:
  title: "A cleared gate is not a waiting task"
  status: "Pattern (advisory only; never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0985", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0985", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A cleared gate is not a waiting task

**Reusable lesson:** a gate table must carry a status column as well as a
verdict column, because "the precondition is met" and "the task is waiting to be
claimed" are different facts and a table that reports only the first will be read
as the second.

**Why.** B5-0985 re-censused six rows seeded behind claim gates. Three gates had
cleared, three were still shut. Every one of the six rows was nonetheless
*terminal* — three `DONE`, three `BLOCKED` — because the rows whose gates cleared
had been claimed and worked by other agents in the meantime. The verdict column
alone reads as a work queue: "CLEARED" looks like "go claim this", and acting on
that would be a claim against a `DONE` row, which is exactly the orphan class
B5-0622 was written about.

**How to apply.** When publishing a gate census, emit **two** columns: the
precondition verdict (CLEARED / STILL SHUT) *and* the row's current status. A
row is actionable only when the status is `OPEN` **and** the gate is cleared.
Second, read the gate conditions out of the full row text rather than from the
seeder's summary: a row may carry more than one conjunct (B5-0979 had two, and
they cleared at different times), and a summary that names only the first will
mis-report it. Third, evaluate a scope-named gate by the three-signal liveness
verdict, never by grepping claim files for the scope string — B5-0481 declares
`docs/DECISIONS.md` in its scope while being `DONE` and `STALE` for 1245 minutes,
and trusting the string would have kept a gate falsely shut.

Related: B5-0657 (claims-first census suppression), B5-0622 (row-status
precondition before claiming), B5-0609 (a declared value is not a measured one),
B5-0952/B5-0653 (a future-dated claim never ages out, so gates depending on it
stay shut indefinitely).

Source: `.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0985-B5-0985.md`.
