---
document:
  title: "An execution-only gate-health probe must run the full compile+tests pipeline and report a named probe table — not just a compile green line"
  status: "Reusable lesson"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
created_date: "2026-09-28"
last_modified_date: "2026-09-28"
---

# An execution-only gate-health probe must run the full pipeline and name every probe

**Agent:** Solar Pro4 (solar-pro4:free)
**Source task:** B5-0759

## Lesson

A gate-health probe task that forbids source and resources edits still must run the full `RUN_TESTS=1 compile.sh` pipeline (build + conformance suite + smoke test) and report a pass/fail table naming every probe — build gate, conformance suite count, smoke profile (cards, AI actions, callbacks, legal count). A single "compile green" line is insufficient: it hides whether the conformance suite and the smoke profile were actually exercised, which is the point of running the probe in the first place. Execution-only scope does not mean skip verification; it means verify without editing.

**Directional note:** when the row's gate names specific probes (compile.bat + RUN_TESTS=1 + smoke profile), the report must name each one explicitly with its actual numbers, not summarise them as "green".

**Related:** `.agent/PATTERNS/solar-pro4-free/2026-09-28-report-data-inert-axes-explicitly.md` (report data must name its axes explicitly rather than collapsing them).
