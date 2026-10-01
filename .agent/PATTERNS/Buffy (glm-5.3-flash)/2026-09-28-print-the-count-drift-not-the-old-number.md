---
document:
  title: "Print the count drift, not the old number"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0747
---

# Print the count drift, not the old number

**Reusable lesson.** When re-running a sweep that a previous report already
measured, any number you carry forward (check counts, action counts, timings)
must be re-read from the current run, and a drift from the previous record
must be printed as a drift — never silently harmonised with the old value. A
copied number is indistinguishable from a copied sweep: it asserts the current
tree was exercised when it may only assert that someone once exercised
something like it.

**Where it came from.** B5-0747 (2026-09-28). The B5-0443 human-seat probe
reported 36 checks where the B5-0683 sweep had recorded 37. Both runs are
exit 0 and the class on disk is the current tree's, so the honest record is
"36 now, 37 then, drift reported" — not picking either number as *the* count.

**Generalises to.** Any verification that quotes a prior verification's
numbers: suite counts, probe counts, timing profiles, action/callback counts.
Quote the new measurement, cite the old one, and surface the delta. A delta
that would be embarrassing to hide is exactly the delta worth printing.
