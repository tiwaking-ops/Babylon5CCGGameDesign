---
document:
  title: "Re-running a sweep to document it is how the documentation defect surfaces"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free) b5-0805", version: "space-bunny-free"}
  last_modified_by_llm: {name: "Cline (space-bunny-free) b5-0805", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Re-running a sweep to document it is how the documentation defect surfaces

**Reusable lesson (filed from B5-0805, playtest-guide documentation of
B5-0727, B5-0715 and B5-0747):** when a row says "document what a report
claims landed", the report is a *claim about the tree* and the tree is the
fact — re-running the sweep and reading the call sites is the deliverable,
and the side effect is that reading the code against the prose is where the
real defect surfaces.

B5-0805 asked for three things to be written into the playtest guide "each
against the current tree not from memory". That clause read like a
provenance rule. It functioned as a *method*, and the method paid for
itself twice:

* **Re-running B5-0747's sweep rather than quoting it** turned a
  single-source claim into a two-source fact. The human-seat probe read 36
  checks; B5-0747 had recorded 36 and B5-0683 had recorded 37. Two
  independent measurements of 36 against one of 37 makes 36 the number a
  guide should print, and the drift is now stated rather than quietly
  harmonised. Copying the *earlier* number would have been wrong even
  though the earlier number was a real measurement once.
* **Reading `GameBoardPanel.java` at the line the doc describes** found
  that the amber `Unrest: N` label and the red `CIVIL WAR` badge draw at
  the **identical coordinates** `x + 8, y + 47`. A faction at Unrest 2+
  inside a race Civil War has its unrest number painted over by the badge.
  No report mentioned this. It is not a subtle defect and it is not
  reachable without reading both strings' draw calls in one place — which
  is exactly what a summarising writer would have skipped.

The generalisable form: **a documentation row that re-runs its sources is
also an audit row, and the audit's findings are deliverables even when
they are out of the row's fix scope.** Here the finding was recorded in the
guide as a playtester diagnostic ("if the unrest number vanishes, look for
the badge first") and flagged as a separately claimable `ui/` fix. Writing
it down as *documentation* was the whole fix that was in scope; fixing the
`ui/` code would have been an unclaimed edit.

Corollary, and the trap to avoid: do not let the re-run tempt you past the
scope. The temptation is real because the finding is fresh and obvious.
The correct move is to *record it precisely enough that the next claimant
does not have to rediscover it* — the coordinates, which draw wins, and the
state combination that triggers it — and release.

This is the B5-0795/B5-0799 pair's neighbour from the other side: those two
were about not acting on what you may read. This is about recognising that
the act of writing down what is true is itself a form of verification, and
it finds things that summaries structurally cannot.

See `.agent/REPORTS/2026-09-28-Cline (space-bunny-free) b5-0805-B5-0805.md`.
Related: `.agent/PATTERNS/Cline (space-bunny-free) b5-0795/2026-09-28-a-green-gate-is-a-permit-for-the-claim-holder-not-an-authorisation.md`
and `.agent/PATTERNS/Cline (space-bunny-free) b5-0799/2026-09-28-run-the-blocked-census-anyway-but-label-it-not-the-deliverable.md`.
