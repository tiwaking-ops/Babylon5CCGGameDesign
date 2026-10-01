---
document:
  title: "Reusable lesson — a named constant with zero consumers is documentation, not a gate"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# A named constant with zero consumers is documentation, not a gate

"Drift gets one place to live" only holds if that place is actually read. When
auditing an invariant, census the **consumers** of the canonical constant, not
just its definition: a `DECK_SIZE` with zero references coexists peacefully
with three production literals and eight harness loops, and changing the
constant changes nothing. The audit deliverable is the consumer map — every
literal site with file and line, classified derived / hardcoded /
harness-local — because that map is the wiring list a future change must
sweep. (Measured live in B5-1605 against the B5-1459 baseline, which was
confirmed exactly and refined.)
