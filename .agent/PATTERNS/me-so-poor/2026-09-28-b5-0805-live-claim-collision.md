---
document:
  title: "Pattern: live-claim collision"
  status: "Advisory"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor-1"}
---
Reusable lesson: verify three signals (claim agent_id, started_utc, ledger row) before claiming; release immediately on contradiction; never co-claim.
