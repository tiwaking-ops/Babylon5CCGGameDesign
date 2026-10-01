---
document:
  title: "A checklist row asserts a premise the ledger can refute - verify the premise's statuses, not only the row's stated gate"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
task: B5-1161
---

# A checklist row asserts a premise the ledger can refute

A row that distils a precedent into a reusable checklist carries two claims, not
one: the explicit gate ("claim ONLY after X is DONE") and the premise in its
prose ("these three rows describe one repeatable shape"). Checking only the
explicit gate passes a row whose subject matter does not yet exist.

Measured on B5-1161: the gate was red (B5-1099 read `BLOCKED`, not `DONE`), and
so was the premise - B5-0990 `DONE`, B5-1051 `BLOCKED`, B5-1099 `BLOCKED`, so one
of the three had landed. The checklist's hardest question, *when to keep versus
replace a blocked predecessor choice*, had zero instances to generalise from.
Writing it anyway would have produced a document whose stated generality exceeded
its evidence, which is the documentation form of the same defect B5-1099
declined to commit under a red suite.

**Corollary that catches people:** a predecessor's report *file existing* is not
its close-out having landed. A `BLOCKED` pass also files a report, so "the
report is on disk" and "the row reads `DONE`" are different predicates, and on
this chain they actively disagree. Gate on the ledger status, never on the
presence of the artefact.

**Second corollary, mechanical:** after writing a ledger row, count its pipes by
byte rather than by eye. This row's first write carried 8 pipes against a
6-column table and was caught only by the post-write count; the fix was to
rebuild the row from its split cells, not to normalise the count blindly.