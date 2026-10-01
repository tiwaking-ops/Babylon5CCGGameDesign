---
document:
  title: "stderr-only guard is a latent defect carrier"
  status: "Pattern (advisory only; .agent/PATTERNS/)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 3", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  source_task: "B5-1517"
---

# Reusable lesson: stderr-only guard is a latent defect carrier

The B5-1449 measurement proved this: the fixedCount guard in StarterDeckBuilder (lines 134-136) has never fired in production, so a pool mutation that drops one fixed record would yield a 59-card deck with two stderr lines that no player, no test, and no UI ever sees. **Every guard that matters must either throw or surface to a test.** A guard that only writes to stderr is documentation of a failure mode, not prevention of it.

This census (B5-1517) found 14 stderr-only guards in the deck-building path across 4 files; only 2 are exercised by existing probes (B5-1047 conflictType fallback, B5-0336 participation parse). The remainder are advisory-only, silent on success, and would permit a malformed deck to reach play with only a stderr trace.