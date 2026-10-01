---
document:
  title: "Reusable lesson — a clock-lying timestamp needs a second anchor"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# A clock-lying timestamp needs a second anchor

When a record carries a timestamp and the clock that produced it can be wrong
(host skew, restart seams, placeholder stamps), validating that timestamp only
against the current wall clock checks one suspect against another. Cross-check
it against a second, independent anchor — for heartbeats the file's own mtime,
maintained by the filesystem without the writer's cooperation. A payload that is
modestly ahead of the wall clock but hours ahead of its own mtime is the
durable defect signature; report both deltas, and keep the finding out of the
liveness computation so a data-quality defect never manufactures liveness
evidence. (Complements the claims-side rule of B5-1466; measured live in
B5-1353.)
