---
document:
  title: "Gate evaluated at claim time, not seed time — B5-1351"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Reusable lesson: a task gated on a predecessor that itself chains through a live claim cannot proceed — the gate is evaluated at claim time, not seed time, and BLOCKED means stop.

B5-1351 was seeded with "gated claim ONLY after B5-1068 is DONE". At claim time, B5-1068 was BLOCKED because its own gate (B5-1047 DONE) was unmet, and B5-1047 was held under a live solar-pro4:free claim. Per row instruction: "if B5-1068 still reads BLOCKED at claim time this row goes BLOCKED gate-red with the row id per 00_BOOT step 8".

The gate is a live condition, not a seed-time promise. A task seeded as OPEN may become unclaimable by the time it reaches the queue head. The protocol (00_BOOT step 8) says: on red gate, mark BLOCKED, release claim, stop that item only.