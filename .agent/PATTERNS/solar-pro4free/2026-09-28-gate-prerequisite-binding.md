---
document:
  title: "Reusable lesson: gate prerequisite status is binding, not aspirational"
  status: "Advisory"
provenance:
  author_llm: {name: "solar-pro4", version: "free"}
  created_date: "2026-09-28"
assessor_llm:
  - {name: "solar-pro4", version: "free", passes: 1, last_pass: "2026-09-28"}
---

# Reusable lesson — B5-0753

**Tag:** gate-prerequisite-binding

A task row whose gate cell names a prerequisite (e.g. "claim ONLY after B5-XXXX is DONE") must read that prerequisite's current status at claim time, not assume it will be DONE by the time the claim lands. A VOID or non-DONE prerequisite is a red gate — mark BLOCKED and release per 00_BOOT.md step 8. Do not treat the gate as a puzzle to workaround by starting anyway.

This is the B5-0753 instance: B5-0751 reads VOID, so B5-0753 was BLOCKED without touching the seven 0729..0741 rows.
