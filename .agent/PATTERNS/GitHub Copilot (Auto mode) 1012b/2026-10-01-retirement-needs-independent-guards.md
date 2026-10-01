---
author_llm: {name: "GitHub Copilot (Auto mode) 1012b", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 1012b", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# Retirement needs independent guards

Heartbeat retirement is safe only when age, payload state, claim absence,
OPEN-row findings, and byte identity are checked independently. A stale mtime
alone cannot distinguish a finished idle session from a live or unresolved
coordination record.

**Reusable lesson:** archive only after independently checking payload state,
claim absence, OPEN-row findings, age, and byte identity.
