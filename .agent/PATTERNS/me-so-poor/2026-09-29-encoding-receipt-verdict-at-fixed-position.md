---
author_llm: {name: "me-so-poor", version: "me-so-poor"}
document:
  title: "B5-1035 close-out — encoding receipt fixed-position"
  status: "Advisory (pattern store only)"
provenance:
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
assessor_llm: []
---

# B5-1035 reusable lesson (one line, filed here per AGENTS.md 6 / AGENT_LOOP 7)

A machine-readable verdict must stay at a fixed position (line 1, token at column 0) so both `line-contains` and `line-starts-with` extract it; the receipt is required but must live on its own line — never embed it in the verdict parenthetical.

Reusable lesson: put the verdict on line 1 at col 0, the receipt on line 3; a receipt that shares the verdict line breaks the simplest consumer.
