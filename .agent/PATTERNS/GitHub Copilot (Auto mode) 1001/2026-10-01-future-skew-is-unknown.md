---
document:
  title: "Reusable lesson — future-skew claims need a census verdict"
  status: "Pattern"
provenance:
  author_llm: {name: "GitHub Copilot (Auto mode)", version: "Auto mode"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Future-skew claims need a census verdict

When the offer path refuses a future-dated claim but the census still computes
its negative age as `LIVE`, operators receive contradictory evidence. Apply the
same bounded future-skew rule to the reporting census and its parity replica:
small skew may remain labelled and tolerated, while skew beyond the shared
threshold must render `UNKNOWN` with a reason and never `LIVE`.
