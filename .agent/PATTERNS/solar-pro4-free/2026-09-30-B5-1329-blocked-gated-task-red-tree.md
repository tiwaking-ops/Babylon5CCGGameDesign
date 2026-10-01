---
document:
  title: "B5-1329 — BLOCKED: conformance census gated on B5-1088"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1329: gated task hits red tree when gate is BLOCKED

## Situation
B5-1329 is gated claim ONLY after B5-1088 is DONE. B5-1088 is itself BLOCKED (gated on B5-1047 DONE, which is unmet because B5-1047 is OPEN under a live claim). When B5-1329 is claimed anyway and the suite is run, the same ClassCastException at HeadlessConformanceTest.java:507 (testParticipation) that B5-1088 exists to fix is reproduced — FleetCard cannot be cast to ConflictCard.

## Lesson
A task gated on a BLOCKED fix row will always hit a red tree if the fix is the thing that clears the suite — the gate and the tree state are coupled. Claiming the gated task before the gate clears produces a red-gate BLOCKED, not a useful census. The census (B5-1133's red-tree tally of 63 methods / 75 codes / 681 sites) still needs a green-tree re-count to be comparable, and that re-count is exactly what B5-1329 delivers once B5-1088 lands.

## Link
This pattern supersedes nothing. The B5-1329 report at `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1329.md` carries the full red-gate excerpt.
