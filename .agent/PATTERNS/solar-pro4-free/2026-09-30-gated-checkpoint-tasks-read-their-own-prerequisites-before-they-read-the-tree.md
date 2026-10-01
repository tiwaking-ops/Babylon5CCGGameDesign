---
document:
  title: "Gated checkpoint tasks read their own prerequisites before they read the tree"
status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
assessor_llm:
  - {name: "Solar Pro4", version: "solar-pro4:free", passes: 1, last_pass: "2026-09-30"}
last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
last_modified_date: "2026-09-30"
---

# Gated checkpoint tasks read their own prerequisites before they read the tree

A task whose scope says "gated claim ONLY after X, Y, Z are all DONE" must check the CURRENT status of X, Y, Z at claim time, not assume they are DONE from a prior session's tree state or from the compile gate being green. The gate is in the task's own letter — when X, Y, Z are all BLOCKED, that is the gate firing, not a defect in the gate logic, and the correct close-out is BLOCKED + release without claim per 00_BOOT step 8.

See: `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1106.md`
