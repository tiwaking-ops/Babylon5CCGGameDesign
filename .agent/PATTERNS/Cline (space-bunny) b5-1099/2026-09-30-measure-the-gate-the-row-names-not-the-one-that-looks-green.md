---
document:
  title: "Measure the gate the row names, not the one that looks green"
  status: "Pattern"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
task: B5-1099
---

# Measure the gate the row names, not the one that looks green

**Reusable lesson (B5-1099).** A task row's gate and the build command an agent
reaches for first can be *different gates*, and the one reached first is the one
that tends to look green. `b5ccg/compile.bat` exits 0 on a tree where the
conformance suite throws, because `compile.bat` carries **no test branch by
decision** - the test selection lives only in `compile.sh`'s `RUN_TESTS=1` branch.
So a row that gates on "compile green plus **suite green**" is *blocked* while
`compile.bat` reports `Build successful`. Read the gate clause, identify which
command it names, and run that one; a green compile is not evidence about a suite.

Corollary, from the same task: when a row's gate names **both** a predecessor row
*and* a tree state, check the predecessor's own gate too. B5-1099 was gated on
"B5-1097 is DONE with the tree green per B5-1088"; B5-1097 read `OPEN` and was
itself gated on B5-1090, which also read `OPEN`. A chain of `OPEN` rows is a red
gate wearing an `OPEN` status - the status column describes the leaf row, never
the reachability of the work behind it.

The pairing matters because both halves produced a *plausible* green: compile
green for the build clause, `OPEN` for the predecessor clause. Two green-looking
signals, one blocked task. When a gate is red, enumerate every clause and check
each against the signal that clause actually names (this extends the
B5-1119 two-independent-red-lights lesson from "both red" to "one red, one
misleadingly green").

Related: `.agent/PATTERNS/Cline (space-bunny) b5-1119/2026-09-30-two-independent-red-lights-on-one-gate.md`
(both clauses red), `.agent/PATTERNS/solar-pro4-free/2026-09-30-gate-precondition-blocked-release-on-claim.md`
(release on claim, do not hold).

Supersedes nothing - first filing.
