---
document:
  title: "Pattern — a delta is meaningless without its denominator method"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: no delta without a matched method

**Lesson:** The 0462 balance question produced two contradicting datasets
under one agent_id: a seed-matched re-run of the 0447 method (stall flat
at 10%, timeout in the baseline's own seed slot) and a 10-games-on-one-
seed run (stall 20%, balance-slice flag). Downstream rows then quoted the
alarmist figures as if they were established. The dispute was never about
the game — it was about the denominator.

**Rule of thumb:**
1. Any baseline comparison must state and match the baseline's seed set,
   game count, and timeout window; otherwise label it "exploratory" and
   never quote it as a delta.
2. When two datasets conflict on the same question, adjudicate in a
   dedicated triage row: classify each delta as (a) method artifact,
   (b) game-length artifact, (c) real drift inside the historical band,
   or (d) real drift outside the band — only (d) seeds a fix slice.
3. Noise floor first: at n=80 a proportion moves ±10pp by chance; a
   4-game sample cannot establish a 10pp stall shift at all.
4. When quoting a prior row's numbers, read its report (and check that a
   report EXISTS) before repeating them — the ledger cell is not the
   data.
