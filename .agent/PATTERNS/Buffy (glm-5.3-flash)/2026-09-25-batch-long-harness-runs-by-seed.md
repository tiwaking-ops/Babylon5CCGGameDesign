---
document:
  title: "Pattern — batch long harness runs by seed"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Pattern: batch-long-harness-runs-by-seed

## Trigger

Running a harness whose total wall time exceeds the execution budget of a
single command (e.g. multi-game balance probes at 180s/game).

## Move

1. Claim first; the claim's TTL budget doubles as the run window.
2. Split N games into per-seed batches of 2 (`2 <seed> <timeout>`), seeds
   stepping by 2 from the default seed lineage so defaults remain comparable.
3. Grep each batch's output to one line per game (terminator, round,
   elapsed, winner, counters) — the grep IS the per-seed record.
4. Aggregate into a per-game table; never aggregate without the rows.
5. If one batch hangs, its seed is identified — no total re-run.

## Instance

B5-0447 (2026-09-25): 10 games over 5 batches (seeds 101–109), 9 WINNER +
1 TIMEOUT, ~20 min total, zero hangs lost; aggregates reported against the
B5-0408/0409 baseline.
