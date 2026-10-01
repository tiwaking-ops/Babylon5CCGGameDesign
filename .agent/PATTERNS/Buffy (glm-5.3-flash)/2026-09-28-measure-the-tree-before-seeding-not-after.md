---
document:
  title: "Measure the tree before seeding, not after"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0803
---

# Measure the tree before seeding, not after

**Reusable lesson.** A seed row's premise is a claim about the tree, and it
needs the same measurement discipline as a work claim: verify against the
filesystem *before* the row is written, not during post-write review. In
B5-0803 the seeder (me) premised an OPEN sweep row on "three probe sources
absent, a 7-of-10 coverage gap from the B5-0693 truncation" — inferred from a
remembered note in B5-0693's close-out rather than a directory listing. The
files all existed. Post-write verification caught it, the row was VOIDed with
the refutation in its own verified cell, and the unused claim file was
released — but the correct sequence was to have measured first and never
written the row.

**Where it came from.** B5-0803 (2026-09-28), the seeder-side twin of
B5-0685 ("re-measure before acting on a quoted premise"): that row caught a
seeder *quoting* another report; this one caught a seeder *remembering* one.
Same root: a premise carried in prose instead of being re-derived from the
tree at write time.

**Generalises to.** Any task whose acceptance criteria name the state of
files, counts, or statuses: presence/absence premises, count premises,
status premises. The cost asymmetry is the point — a voided seed costs one
row and one DECISIONS entry; a claimed-and-worked false premise costs a
claim, a report, and a confused future reader.
