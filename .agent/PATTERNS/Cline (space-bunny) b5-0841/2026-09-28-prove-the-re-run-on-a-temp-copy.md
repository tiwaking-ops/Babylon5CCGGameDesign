---
document:
  title: "Prove the re-run on a temp copy: a script's danger is its next execution, not its last one"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0841", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0841", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: "B5-0841"
---

# Prove the re-run on a temp copy

Filed from B5-0841, which was asked to assess a tracked root script and
explicitly **not** to execute it.

## The pattern

Two habits that pull in opposite directions meet on a row like this one:

* the row says *report only, DO NOT EXECUTE*;
* the row also says *establish what re-running would do*.

The lazy resolution is to reason about the re-run in prose. The useful resolution
is that **executing and simulating are not the same act**. A python heredoc that
appends rows to a shared file is a *string transformation* wearing a shell
costume: copy the target to `%TEMP%`, extract the appended strings **as text**
with a regex over the script, append them to the copy, and run the real checker
against the copy. The live file is never opened for writing, and the evidence is
a measured exit code rather than an argument.

That is exactly what this pass did, and it converted the row's central claim from
an assertion into a measurement:

| | live ledger | temp copy + the 4 rows |
|---|---|---|
| `run-dup-census.ps1` | exit 0, `PASS (0 duplicate)` | exit 1, `B5-0511 x2` … `B5-0514 x2` |

And the negative control is free: the live ledger's byte length was measured
before and after (959171 both times) and the live census re-run green. A proof
that mutates the thing it measures is not a proof; a proof that leaves a
before/after size is.

## The second half: *danger is the next execution, not the last one*

Reading a script's history tells you what it already did, which is the harmless
question. The decision-relevant question is what it does **next**, and for a
one-shot appender those diverge completely: its one historical run was correct
(it seeded four tasks that all got done), and its next run would corrupt the
shared governance file. A "it worked fine last time" reading is not merely weak
here, it points the opposite way.

The cheap corroboration is that the tracked status is usually an accident. Here,
eight same-day reports classified the file as scratch and two checkpoint commits
deliberately excluded it, and the next commit swept it in with `git add -A`. When
the surrounding evidence contradicts a file's own tracked state, ask which
artifact recorded the decision — usually none did.

## Why the "just archive it" answer is weaker

The instinct is to relocate rather than delete, on the grounds that deletion is
harsh. But an archived copy of a *working* appender is still a working appender
to whoever finds it, and it stays one `cd` away from live. For a 21-line file,
**git history is the archive** — the blob is already durable at `418664de`, with
its content and its date, and `docs/archive/` would only add a second copy that
someone could mistake for live tooling. Prefer the store that cannot be executed
accidentally.

Related, not superseded: B5-0839 (build the negative control; a checker can
enforce a different rule from the one written down) — here the negative control
was the untouched live ledger, and the two censuses disagreeing is the evidence.

## Reusable lesson

When a row forbids execution but demands a claim about what execution would do,
extract the script's output **as text** onto a temp copy of the target and run
the real checker there — then prove the live file never moved by re-measuring it.
And judge a one-shot script by its next run, not its last.
