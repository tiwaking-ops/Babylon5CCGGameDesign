---
author_llm: {name: "me-so-poor", version: "me-so-poor"}
document:
  title: "Seed branch guard verification"
  status: "Pattern (advisory, non-canonical)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
  last_modified_date: "2026-09-29"
assessor_llm: []
---

# Seed branch guard verification

**One-line lesson:** A self-seeding branch must be tested in isolation with a synthetic ledger, verifying guards through visible output (not silent assumptions), because the branch that writes rows to the queue is the most likely component to produce malformed output.

## Shape of the test (B5-1014)

The B5-0900 seed branch (`/MaxSeedInvocations` opt-in) was verified by:

1. **Empty ledger + fresh heartbeat** → queue is "drained" and seedable
2. **Dry-run simulation** → confirms `Test-SeedableQueue` logic:
   - Holds seeding when any OPEN/CLAIMED/BLOCKED row exists
   - Holds seeding when any live claim on terminal rows exists
   - Prints named messages for holds (visible, not silent)
3. **Code inspection** → confirms static properties:
   - `$stagnant` counter NOT incremented during seed
   - Budget enforced: `$seedInvocations < $MaxSeedInvocations`
   - Default 0 = opt-in only

## What worked

- Isolation: TEMP harness with synthetic ledger, never touching live tree
- Observable guards: `Test-SeedableQueue` prints which rows caused a hold
- Gates: `run-dup-census.ps1` PASS 0 duplicates, `ledger-query.ps1` 7 pipes/doubleLead no
- Compile gate: `javac -source 6` green on JDK 1.8.0_292

## Reusable lesson

A seed-branch test must isolate the CLI invocation — the runner only passes the prompt; the CLI writes the row, and its correctness is proved by post-write census and detector probes.

**Reusable lesson:** A self-seeder that writes queue rows must be tested with a synthetic ledger to verify that every guard (empty queue, no live claims, budget) fires correctly and produces valid, parseable rows.