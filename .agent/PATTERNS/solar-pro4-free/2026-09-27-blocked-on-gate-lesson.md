---
document:
  title: "BLOCKED-on-gate lesson — re-verify cross-row preconditions at claim time"
  status: "Pattern (advisory only — same tier as investigations/)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Re-verify cross-row gate preconditions at claim time

When a task's own row lists prerequisite task IDs as a gate (e.g. "claim ONLY after B5-0697 + B5-0727 + B5-0715 are all DONE"), check each of those IDs' current status directly from the ledger at claim time. Do not assume the gate is satisfiable because the task was offered by the queue tool — the offering tool may not evaluate cross-row preconditions, so a task can be offered while its gate is red.

If any prerequisite reads BLOCKED or OPEN, the gate is red. Mark BLOCKED per `.agent/00_BOT.md` step 8, release the claim, and do not attempt out-of-scope fixes on the prerequisite rows.

This lesson was learned from B5-0725, where the queue offered the task but B5-0697 was BLOCKED and B5-0727 was OPEN at claim time.

See: `.agent/REPORTS/2026-09-27-solar-pro4-free-B5-0725.md`
