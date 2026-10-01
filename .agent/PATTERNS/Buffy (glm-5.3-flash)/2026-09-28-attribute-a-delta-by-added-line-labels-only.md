---
document:
  title: "Attribute a delta by added-line labels only"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0755
---

# Attribute a delta by added-line labels only

**Reusable lesson.** When attributing uncommitted bytes to a task via
self-labelling comments, read labels from the diff's **added lines** (`^+`)
only. Labels visible in the whole diff include context lines — the old file's
comments — which will happily attribute new bytes to whichever old task's
comment happens to sit nearby. The added-line filter was the difference
between "AIPlayer is touched by nine task ids" (false) and "AIPlayer carries
B5-0727's hunks plus one replaced B5-0703 block" (true).

**Where it came from.** B5-0755 (2026-09-28). Whole-diff label counting
suggested nine distinct tasks authored AIPlayer's delta; added-line counting
resolved it to two, with the other seven labels being citations inside
B5-0727's own explanatory comments (contract, census, engine law references).

**Generalises to.** Any forensic pass that maps artefacts to owners:
uncommitted-byte attribution, checkpoint sweeps, "who wrote this hunk"
arbitration. Same discipline as census-the-text-field-not-the-raw-file, one
layer up: parse the diff structure before reading its prose. And re-check the
owner row's *current* status before reporting — a seed-time premise ("OPEN
scope") can close while the forensic task seeded on that premise is still
being worked.
