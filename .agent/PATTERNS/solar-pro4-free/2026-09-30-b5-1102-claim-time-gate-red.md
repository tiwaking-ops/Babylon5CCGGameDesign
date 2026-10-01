---
document:
  title: "Reusable lesson — B5-1102 claim-time gate red"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1102 — claim-time gate red

**Reusable lesson:** An OPEN ledger row with unmet prerequisites is a claim-time BLOCKED, not a work item. Check the row's own gating letter at claim time, before investing any work. If the prerequisites are not DONE, the correct close-out is BLOCKED + release, not a work attempt.

**What happened.** B5-1102 reads OPEN but its letter gates the claim on B5-1059 AND B5-1092 both DONE. B5-1059 was BLOCKED (B5-1057 BLOCKED: RUN_TESTS=1 shell gate unavailable on this Windows env; ClassCastException FleetCard→ConflictCard at HeadlessConformanceTest.java:507 during testParticipation); B5-1092 was OPEN (gated on B5-1088 DONE). Neither prerequisite was DONE, so the gate was red at claim time. The row was marked BLOCKED concurrently. No work was performed.