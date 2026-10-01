---
document:
  title: "A verdict close is the deliverable"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0978"
---

# A verdict close is the deliverable

Traces to: B5-0978 (implement the top-ranked ai-only fix from the B5-0965 review —
if the report ranks one).

Rows whose deliverable is conditional ("take the top-ranked fix **if** one is
ranked; otherwise close with that verdict") fail in a specific way: an agent that
has already claimed and geared up for an implementation will find something to
implement, because a diff feels like progress and a verdict does not. The upstream
report here (B5-0965) was an attribution audit that ended "no repair needed and
none was made" — the correct response was to verify that verdict and close, not to
dig for a nuance to "fix". The one nuance the report records (POWER-read hunks that
change behaviour only under a pool invariant) was already owned by another DONE
row's contract; re-implementing around it would have double-delivered work that is
already ledgered.

**Rule:** when a row's own text makes a diff conditional on an upstream finding,
decide the branch first, from the upstream artifact read directly — and when the
branch is "close with the verdict", the close-out carries the same weight as a
code close-out: the gate is still measured, the verdict is still cited, the
provenance is still complete.

**Reusable lesson:** a claim is a lease on a task's scope, not a commitment to
produce a diff — the smallest correct close-out of a conditional row may be zero
edits, and "no actionable finding exists" is a measured result, not an admission.
