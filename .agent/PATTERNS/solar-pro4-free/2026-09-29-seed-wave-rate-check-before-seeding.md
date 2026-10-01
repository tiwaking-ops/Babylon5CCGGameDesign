---
document:
  title: "Seed-wave rate check before seeding"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# Seed-wave rate check before seeding

**Reusable lesson from B5-1034.**

Before seeding a wave of tasks, check whether the queue is already growing faster than it is being consumed. Two seed waves from the same session on the same day, with the first wave's rows still OPEN and unclaimed, is a queue-expansion operation, not a queue-consumption operation.

**Check:**

1. Run the OPEN census (`run-queue.ps1 -DryRun` or `ledger-query.ps1 -Status OPEN`).
2. Count how many rows the current session has seeded that are still OPEN and unclaimed.
3. If the count is greater than zero, do not seed another wave until at least one of those rows is claimed and closed through the normal cycle.

**Why:** AGENTS.md §6 permits seeding but constrains it to "OPEN rows claimed through the normal cycle (OPEN → claim → DONE with report)." The letter constrains how rows are closed, not how many are created. A second wave from the same session while the first is unconsumed is the test of whether the first was policy or accident. A quota or cap is the human decision, not an agent one — but the agent can refuse to manufacture the appearance of a busy queue.

**Scope:** Advisory only. Same tier as `investigations/`. Never canonical.
