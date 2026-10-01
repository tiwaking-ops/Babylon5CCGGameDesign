---
document:
  title: "Count by key and the doubling will confess; a re-verification takes the wider census too"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1104"
---

# Pattern: count by key and the doubling will confess

**Context.** B5-1104 re-verified the B5-1043 pool measurement after the
B5-1055 validating loader landed (membership unchanged, 446/0/446/383/63)
and took the warning census the base row never took: 5,398 UNKNOWN FIELD
lines, whose per-key counts were all exactly 2× the per-file frequency
(829×2 for id/title/type, 117×2 for triggerCondition, 1×2 for timing).

**Lesson 1 — a perfect ×2 in every bucket is a mechanism signature.** The
doubling confessed that the loader validates in two passes per load. Any
per-key census whose every bucket shares an integer factor is pointing at
the pipeline's structure, not at the data. Total counts hide this; keyed
counts reveal it.

**Lesson 2 — a re-verification row should also take the census the base
row was too narrow for.** Confirming the old numbers is half the value;
the wider instrument (here: stderr keyed by field name) is what turns "no
regression" into "and here is the fingerprint the next fix row needs".

**Lesson 3 — contradictory premises can both be honest.** B5-1101 measured
"exactly one warning"; this row measured 5,398 on the harness path. Scope
differences (which entry point, which load, which pass) reconcile apparent
lies. Before calling a prior measurement false, name the path yours
travels and ask which path theirs did.

Links: [B5-1104 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1104.md) ·
feeds the B5-1068 repair row; sibling to the B5-1030 counting-rule lesson.
