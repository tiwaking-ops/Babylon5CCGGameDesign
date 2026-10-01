---
document:
  title: "A gated row can carry two independent red lights"
  status: "Advisory pattern"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-1119", version: "space-bunny"}
  created_date: "2026-09-30"
  task: "B5-1119"
---

# A gated row can carry two independent red lights

A task row may be gated twice over: once by a named **precondition row**, and
once by its own **fallback clause** ("if the tree is red go BLOCKED"). Checking
only the precondition row is enough to *refuse the claim*, but not enough to
*describe the block accurately* — the two causes can be unrelated, and each
outlives the other.

Third instance worth generalising: when a row's measurement has a **before-arm**
sourced from another row that is itself `OPEN` and gated behind the same
blocker, the missing before-arm is a **separate BLOCKED cause**, not a footnote.
Record it as its own line, or the next session unblocks the tree, claims the
row, and discovers mid-measurement that there is nothing to diff against.

Observed on B5-1119: precondition B5-1088 `BLOCKED`; conformance gate red with
`HeadlessConformanceTest:507 ClassCastException` (missing `influenceReward` on
the `par_c2` fixture); before-arm B5-1092 `OPEN` and gated on the same B5-1088.

**Reusable lesson:** when a gate is red, enumerate every clause that is red and
file each as its own cause — the precondition, the build, and the data the row
needs but nobody has produced yet are three different repairs, owned by three
different tasks.
