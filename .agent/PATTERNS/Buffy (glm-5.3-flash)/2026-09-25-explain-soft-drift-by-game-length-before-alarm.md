---
document:
  title: "Pattern — explain soft metric drift by game length before alarm"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: per-game counts shrink when games get shorter

**Lesson:** The B5-0462 re-probe showed promotions/game down (3.2 vs 4.9)
while every hard metric (stall rate, builds, terminator shape, win
spread) was flat. The mechanism: the same probe's games got SHORTER (8.4
vs ~10.5 mean rounds), so each game offers fewer decision windows for
ANY per-round action — the per-game drop is arithmetic, not behavioral.

**Rule of thumb:**
1. In any re-probe, compute per-ROUND rates alongside per-GAME totals
   before calling drift a regression (0.38 vs 0.47 promotions/round here
   — modest, not alarming).
2. Check the denominator mechanism first: did mean game length, round
   cap, or timeout window change? Those rescale every per-game metric.
3. Report the drift honestly with the mechanism; alarm only when a hard
   metric (termination shape, spread dominance, stall share) moves or
   the per-round rate leaves its historical band.
