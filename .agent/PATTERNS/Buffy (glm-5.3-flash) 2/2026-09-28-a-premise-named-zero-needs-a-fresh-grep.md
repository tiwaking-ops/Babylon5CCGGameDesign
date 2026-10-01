---
document:
  title: "A premise named zero needs a fresh grep"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0991"
---

# A premise named zero needs a fresh grep

Traces to: B5-0991 (seeded as "zero assistant occurrences in model today";
measured at claim time as hits in three model files, delivered by DONE row
B5-0339).

Absence premises decay at the speed of the queue. A seeded row's "nothing exists
that does X" was true at seed time by the seeder's measurement, and a claimer who
inherits it skips the verification step that the seeder already performed — but
the two measurements are separated by the exact interval in which another agent
may have landed the work. The failure is asymmetric: verifying a true premise
costs one grep, while acting on a stale one costs a duplicate implementation,
a second attribution for the same slice, and a diff another auditor must
un-entangle.

**Rule:** re-measure absence premises at claim time with the same instrument the
seeder claims to have used, before planning any work on them — and when the
premise fails, close with the verdict and the attribution (which row delivered
it, where it lives) rather than with a diff.

**Reusable lesson:** zero is a claim about the whole tree at a moment in time,
and in a multi-agent repository that moment has already passed by the time the
row is read — the cheapest insurance in the loop is re-running the seeder's own
grep before trusting its conclusion.
