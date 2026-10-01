---
document:
  title: "A proposal pool rots by implementation more than by neglect"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1028"
---

# A proposal pool rots by implementation more than by neglect

Traces to: B5-1028 (disposition of the bare-Proposal pool).

**One-line lesson:** the biggest disposition class in a stale-looking proposal
pool was "already built, status field never moved" — a sweep by age cannot
find that; disposition must diff the proposal against the tree, not against
the calendar.

## The shape

- The row framed the pool as starved of human rulings. Measured: 4 of 27
  files were already terminal, 4 more were non-terminal *and already promoted
  into governance* (their conventions live in AGENTS.md, the CLAIMS/HEARTBEATS
  READMEs, and the shared census tools), and 5 were non-terminal *and already
  implemented* (StatBonus floor/expiry, Participation, CivilWarState,
  VictoryPath, the participation data field). Nine proposals were decided by
  reality while no field moved.
- The reverse also held: the two oldest-looking engine rows (D6/D9, 2026-09-23)
  are the *freshest* candidates, because nothing landed and nothing
  contradicts them — age made them look stale and they are the opposite.
- Two files that look like drafts-by-name ("2026-09-25-solar-pro4-free-…")
  needed a duplicate check before any DEAD verdict; one of them is the only
  station-motion design in the repo. The git-diff rename rendering nearly
  manufactured a false "duplicate" verdict — an artefact worth naming once.

## What worked

- Grep the tree for the proposal's named artifacts (class names, enum
  constants, line-level mechanics) before dispositioning anything: the
  implemented-vs-waiting split falls out in minutes and is checkable.
- Cite the promotion site for every "already promoted" verdict — a convention
  in force is evidence, a stale status field is not.
- Answer the narrow factual questions in DECISIONS (row-authorised) so the
  proposals can move on facts without a human; leave all judgement calls to
  the report.

**Reusable lesson:** a proposal's status field records what an agent once
hoped; the tree records what actually happened — disposition against the
tree, and a pool that reads neglected may be mostly already-decided.
