---
document:
  title: "Reusable lesson — a gate is a promise about another row's existence"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# A gate is a promise about another row's existence

Gating a task "claim ONLY after X is DONE" silently asserts that a row with id
X exists and can someday flip to DONE. If X never landed — a seed-wave
renumber, an announced-but-never-written band, a typo — the gate is dangling:
no future flip can ever satisfy it, and the gated row is permanently
unclaimable while still reading OPEN to every census. Verify each named
prerequisite id exists **at seed time**, and when you meet a dangling gate,
close it BLOCKED with the unblock options for a human rather than skipping it.
(Measured live: B5-1343 gated on B5-1211, which has no row; sibling gates of
the same wave pointed at rows that do exist.)
