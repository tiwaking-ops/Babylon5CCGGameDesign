---
document:
  title: "Reusable lesson — re-sweep BLOCKED is not a missing claim"
  status: "Advisory pattern (not canonical; namespace me-so-poor only)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
---

# Pattern: a harness-only re-sweep BLOCKED is not a missing claim

Namespace: `me-so-poor`. File: `2026-09-27-a-re-sweep-blocked-is-not-a-missing-claim.md`. Links: `.agent/REPORTS/2026-09-27-me-so-poor-B5-0683-BLOCKED.md` (this session's B5-0683 BLOCKED close-out, gate red on B5-0679 OPEN + B5-0681 BLOCKED); `.agent/REPORTS/2026-09-27-me-so-poor-B5-0681-BLOCKED.md` (lane-block precedent, B5-0681 BLOCKED, overlap with live B5-0677 + B5-0679); `.agent/HEARTBEATS/README.md` (three-signal rule; do not reap B5-0679 — newest signal 11 min < 30 min TTL → LIVE); `.agent/00_BOOT.md` step 8 (BLOCKED with log excerpt, never fix out of scope); `AGENT_LOOP.md` step 6 (compile first; red → STOP that item only).

Reusable lesson (one line): A harness-only row whose four predecessors include one OPEN and one BLOCKED must go BLOCKED with the dependency state as the evidence, never with a fabricated compile failure or a "missing claim" explanation — the gate is the message, and fixing out of scope violates the one-writer-per-file rule and the three-signal reap rule; the next claimant measures fresh at claim time (not from this BLOCKED note) and only claims when ALL four predecessors read DONE.

Why it applies: B5-0683's row requires B5-0661 + B5-0677 + B5-0679 + B5-0681 all DONE. At 09:15Z B5-0679 was OPEN (Buffy claim 09:03Z, AIPlayer.java parse failure at :1138, heartbeats fresh, no report → LIVE, not reaped, not fixed outside scope) and B5-0681 BLOCKED (scope overlap with live claims). Compiling and reporting a red excerpt would have been a fabricated failure — the failure mode is dependency state, not B5-0683 code. The correct close-out: claim, verify gate, write BLOCKED report with dependency-state evidence, delete claim, refresh heartbeat (state idle, live_claims []), file pattern, update ledger row in-place (not a new row — same ID, status changed, pipes preserved), leave DECISIONS untouched (BLOCKED is not implementation).

When NOT to apply: when all predecessors read DONE and the tree is genuinely red from the harness row's own edit — in that case the failure is the row's, log the compile/run excerpt and STOP that item (same step 8, different cause). When a predecessor is STALE (all three signals outside TTL) — reap the stale claim per 00_BOOT step 10 with inline three-signal evidence in the reap note, never silently; do not treat STALE as DONE.

Boundary conditions: Do not rename this pattern into "fix anything that's broken" — it only covers the case where a sweep row is blocked by predecessors, not by its own code. Do not reuse it for a code-row gate failure (those have different failure modes — compile error, suite failure — and different close-out artifacts — DECISIONS entry, source edit, report with pass/fail table). Do not use it to justify skipping the compile gate when predecessors ARE all DONE — that is a different failure (run not executed) and requires a different fix.

Supersede-never-rewrite: this file is new; a corrected version (e.g., distinguishing B5-0679-style open-predecessor BLOCKED from B5-0681-style blocked-predecessor BLOCKED) would be a NEW file linking this one, per AGENTS.md 6 / AGENT_LOOP.md step 7.
