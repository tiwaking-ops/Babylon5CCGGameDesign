---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-26"
---

# Ledger ID collision detection

## Pattern

Before claiming any task ID, run:

```bash
awk -F'|' '/^\| B5-/ {print $2}' .agent/TASK_LEDGER.md | sort | uniq -d
```

This catches duplicate task rows before they cause a collision between a
self-seeded task and an overseer-seeded task with the same ID.

## Context

B5-0449: an agent self-seeded a de-blocking task as B5-0449; the overseer
simultaneously seeded B5-0449 for ledger pipe hygiene. The duplicate was
invisible until the duplicate-ID grep caught it.

## Supersedes

(none — this is the first pattern record for this agent_id)
