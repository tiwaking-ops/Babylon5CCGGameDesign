---
document:
  title: "A gate naming a predecessor ID is not a tree check — resolve the ID to its own gate"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-1143", version: "space-bunny"}
  created_date: "2026-09-30"
  task: "B5-1143"
---

# Pattern: a gate naming a predecessor ID is not a tree check

B5-1143 was gated on `claim ONLY after B5-1088 is DONE`, "on the green tree". Two
different things are named, and they fail independently:

- B5-1088 was `BLOCKED` — itself blocked behind B5-1047 `OPEN`, which was under a
  **live** `solar-pro4:free` claim on `DeckLoader.java`.
- `compile.bat` exited **0** with only the expected bootstrap warning.

The compile being green is what makes this trap. `compile.bat` bare is a *compile*
gate; "the green tree" is the conformance suite exiting 0, and it exited 1 with
`ClassCastException: FleetCard cannot be cast to ConflictCard` at
`testParticipation:507`. Every section before `PAR` passed, which is exactly why the
green-looking signal is trustworthy-looking and wrong.

Rules:

1. **`X is DONE` is a claim about a task, not a statement about the tree.** Resolve it
   to its own status *and* to the gate behind it, because a chain of `BLOCKED` and
   `OPEN` rows is a red gate wearing a healthy build.
2. **A row that names only a predecessor ID supplies no tree-state clause**, so the
   compile is the only thing left to run — and it is the wrong thing. Supply the
   missing check yourself: run the suite, do not infer it.
3. **The claim itself can be the forbidden act.** B5-1143 gates the *claim* on B5-1088
   DONE, so creating the claim would have been the violation, not just the work behind
   it. B5-1088's close-out set the precedent (`No claim created`); creating one and
   then marking BLOCKED would have written a lock on a row the gate said not to touch.

Paired corollary, same session: a green test can *change what a red row means*.
`[D12] 20-20 tie yields no standard winner: PASS` is green, so 20-20 ties resolving to
no standard winner is specified, verified behaviour. B5-1143's "stall with final
powers" column is therefore measuring a **designed** outcome, not a regression — a
report presenting stalls as defects would be wrong before it started.

Reusable lesson: when a row gates on `X is DONE`, read that as a pointer and follow it
to X's own gate; and when the build is green but the row wanted a green *tree*, the
build result is not a partial answer to the gate — it is the answer to a different
question.
