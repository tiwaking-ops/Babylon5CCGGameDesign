---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-26"
---

# Capture per-game detail in regression probes (not just aggregates)

When re-running an existing harness for regression comparison, record the full
per-game breakdown (rounds, terminator, per-game action counts) alongside the
aggregate deltas. Aggregate stats can mask quality shifts: in B5-0462 vs
B5-0447, conflicts initiated rose 79→101 but initiator win rate fell 87%→64% —
the net volume increase looked like more engagement but actually reflected more
contested, less decisive conflicts. The per-game table also revealed the two
TIMEOUT games had high initiator win counts (6/3 and 11/3) but stalled just
short of the influence-20 threshold — a pattern invisible in aggregates alone.

See: [[b5-0462]]
