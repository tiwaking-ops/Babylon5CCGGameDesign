---
document:
  title: "Watcher-must-not-race-the-loop-it-watches"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-26"
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-26"
supersedes: none
---

# Watcher must not race the loop it watches

**Lesson (B5-0495):** A test harness that runs the system under test on a
daemon thread must not call observation APIs on live state after its poll
loop declares completion — the daemon keeps mutating. If a checker validates
payloads (e.g. "card must be in hand"), exempt every payload type whose card
reference lives in a slot or in-play zone (agenda slot, contingency host,
conflict participants, in-play characters), not just the types that existed
when the checker was written. Seed-RNG fixes cannot close scheduling races;
quiesce the loop (join at a boundary) or make the checker race-tolerant.

Filed for: HeadlessSmokeTest step-5 DISCARD_AGENDA flake (1-in-8
reproduction). Linked report:
`.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0495.md`.
