---
document:
  title: "Gate-red-at-claim-time-B5-1319: precondition-unmet gates mark BLOCKED and release"
  status: "Advisory"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Gate-red-at-claim-time-B5-1319

**Class:** gate handling, single-task close-out

**Observed on:** B5-1319 (2026-10-01, solar-pro4:free)

**What happened:** B5-1319's gate clause required B5-1088 DONE. B5-1088 was BLOCKED at claim time (gated on B5-1047 OPEN under a live claim plus B5-1087 DONE), so the gating prerequisite "gated claim ONLY after B5-1088 is DONE" was unmet at the moment of claim.

**What the close-out did:** marked the row BLOCKED per .agent/00_BOOT.md step 8, released the claim, wrote no src/ui edits, and recorded the blocker (B5-1088 BLOCKED) plus the unblock path (B5-1047 DONE → B5-1087 DONE → B5-1088 DONE) in the ledger row note, the DECISIONS entry, and the report.

**Why this matters:** a red gate at claim time is not a task to workaround — it is a precondition the task does not currently meet, and the correct action is to mark BLOCKED and release with the blocker and unblock path cited, so the next claimant can re-check rather than re-derive the red state.

**When to apply:** any task whose gate clause names a predecessor row whose status is not the required value at claim time. The gate is the precondition; if it fails, the task is BLOCKED regardless of what else is true.

**What not to do:** do not run the harness when the gate is unmet; do not fix the blocker out of scope to make the gate green; do not invent a result and file it as if the harness had run; do not hold the claim waiting for the prerequisite.

**Scope:** advisory, same tier as investigations/; never canonical.

**Reusable lesson:** a task whose row carries an explicit gate precondition that is not satisfied at claim time must be marked BLOCKED and the claim released immediately — not held, not partially worked, and not silently deferred. The gate is a precondition on the claim, not on the work start. Checking it after the row re-read and before any edit is the correct moment. Holding the claim while waiting for the prerequisite would consume the one-writer-per-scope slot and block the prerequisite's owner from closing it.