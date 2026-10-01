---
document:
  title: "A stale fixture fails against correct code"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0731
---

# A stale fixture fails against correct code

**Reusable lesson.** A fixture kept as evidence has a job: fail when the thing
it fixed regresses, pass when the code is right. When a re-run shows the
opposite signature — passing on old defects, failing on corrected behavior —
the fixture has become a record of a superseded tree. Keeping it trades a
small archive win for the next agent's false-red trap, and repairing it means
re-litigating the very change that made it stale.

**Where it came from.** B5-0731. The B5-0660 acceptance harness
(`tmp-b5660-harness.ps1`) was committed as "acceptance evidence" by a
checkpoint sweep. Re-run on 2026-09-28 it failed V2/V3/V4 — not because the
runner regressed, but because B5-0771 had *corrected* the runner's REPORTS
path the same day. The harness certified the pre-fix runner. Nothing in the
repo cited the harness, so the DELETE branch (decision recorded in
`docs/DECISIONS.md` before touching files) beat both KEEP and a KEEP-fork
that would have had to re-argue B5-0771.

**Generalises to.** Any acceptance harness, golden file, or regression probe
pinned to a specific implementation detail of the thing it tests. Before
keeping one, re-run it against the current tree; treat an unexplained FAIL as
a question ("what changed — the code or the fixture?") rather than evidence
of a live defect, and treat a vacuous PASS the same way. If the answer is
"the fixture is behind", the honest dispositions are repair-with-new-record
or delete-with-recorded-decision — not keep-as-is.
