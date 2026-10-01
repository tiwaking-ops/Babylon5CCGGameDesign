---
document:
  title: "Prerequisite-gated tasks require satisfied prerequisites before work"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
---

# Prerequisite-gated tasks require satisfied prerequisites before work

When a task carries a hard prerequisite (e.g., "gated claim ONLY after B5-XXXX is DONE"), verify the prerequisite's status before claiming. If the prerequisite is BLOCKED, the dependent task must be marked BLOCKED and the claim released — not worked around.

This is the "out-of-scope in-flight edits → BLOCKED + release" case per `.agent/00_BOOT.md` step 8: the blocking condition (B5-1057's RUN_TESTS=1 gate) is in a different scope (harness execution) than the gated task (docs/), so no fix is attempted.

Related: `.agent/AGENT_LOOP.md` step 6 (orphan claims on DONE/VOID/SUPERSEDED/BLOCKED rows), `.agent/00_BOOT.md` step 8 (red gate → BLOCKED + release, item-only stop).
