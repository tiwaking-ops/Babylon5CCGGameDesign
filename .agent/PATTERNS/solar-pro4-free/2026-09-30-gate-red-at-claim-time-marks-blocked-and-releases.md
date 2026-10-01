---
document:
  title: "Gate red at claim time marks BLOCKED and releases"
  status: "Advisory"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Gate red at claim time marks BLOCKED and releases

**Class:** gate handling, single-task close-out

**Observed on:** B5-1141 (2026-09-30, solar-pro4:free)

**What happened:** B5-1141's gate clause required B5-1088 DONE. B5-1088 was BLOCKED at claim time (gated on B5-1047 OPEN under a live claim plus B5-1087 DONE), and the tree was red (HeadlessConformanceTest ClassCastException FleetCard→ConflictCard at line 507). The gate clause failed before any harness run.

**What the close-out did:** marked the row BLOCKED per .agent/00_BOOT.md step 8 and .agent/AGENT_LOOP.md, released the claim, wrote no harness output, touched no src or resources file, and recorded the blocker (B5-1088 BLOCKED) plus the unblock path (B5-1047 DONE → B5-1087 DONE → B5-1088 DONE) in the ledger row note, the DECISIONS entry, and the report.

**Why this matters:** a red gate at claim time is not a task to workaround — it is a precondition the task does not currently meet, and the correct action is to mark BLOCKED and release with the blocker and unblock path cited, so the next claimant can re-check rather than re-derive the red state.

**When to apply:** any task whose gate clause names a predecessor row whose status is not the required value at claim time. The gate is the precondition; if it fails, the task is BLOCKED regardless of what else is true.

**What not to do:** do not run the harness on a red tree when the gate is "if the tree is red go BLOCKED"; do not fix the blocker out of scope to make the gate green; do not invent a result and file it as if the harness had run.

**Scope:** advisory, same tier as investigations/; never canonical.
