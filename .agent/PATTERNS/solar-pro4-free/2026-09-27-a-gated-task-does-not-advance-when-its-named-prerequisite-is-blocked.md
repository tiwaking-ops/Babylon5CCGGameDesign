---
document:
  title: "A gated task does not advance when its named prerequisite is blocked"
  status: "Pattern (advisory, non-canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-27"
---

# A gated task does not advance when its named prerequisite is blocked

**Context:** B5-0687 (playtest-guide refresh part 27) required B5-0681 and B5-0683 as named prerequisites. Both were superseded by later rows (B5-0691 DONE, B5-0695 OPEN), but the gate text literally named the original IDs, not their superseders. Claiming the task revealed the precondition unmet: one named prerequisite BLOCKED, one OPEN.

**What happened:** B5-0681's block cause (overlapping claims) was cleared and B5-0691 superseded it to DONE. B5-0683's successor B5-0695 is OPEN, not DONE. A reader assuming "the work is done, the gate should pass" would be wrong — the gate names specific row IDs, and those IDs are not all DONE.

**Lesson:** When a task is superseded by a new ID, any downstream gate that names the original ID does not automatically track the superseder. The gate text must be updated to name the new ID, or the downstream task remains blocked on a row that no longer represents the current state. Read the actual row IDs in the gate text, not the semantic intent.

**Verification:** B5-0687 gate requires B5-0661, B5-0677, B5-0679, B5-0681, B5-0683 all DONE. Ledger shows B5-0681 = BLOCKED, B5-0683 = OPEN. Precondition unmet. Claim released per 00_BOOT step 8.

**File:** `.agent/REPORTS/2026-09-27-solar-pro4-free-B5-0687.md`

**Related:** B5-0697 (re-seeded successor with updated gates B5-0711 + B5-0695).
