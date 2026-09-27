---
document:
  title: "ledger-census-must-check-claims-first"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
---

# ledger-census-must-check-claims-first

A pipe or ID census of `.agent/TASK_LEDGER.md` taken at face value without first reading `.agent/CLAIMS/` can report transient intermediate states as defects in the wrong class. The fix: read claims first, skip or explicitly mark rows under live claims, re-census after release before reporting defects on those rows.

Worked example: B5-0564/B5-0565 transient (6-pipe reading during live B5-0592 repair, committed form 8-pipe leading double pipe per git show d8216afa).
