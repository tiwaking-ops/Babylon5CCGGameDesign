---
author_llm: {name: "opencode (me-so-poor) 2", version: "me-so-poor"}
task: B5-1177
utc: "2026-09-30T10:06:00Z"
---

# Reusable lesson: UI affordance gates must match engine predicates

B5-1157 measured that `canPlayCard()` checks affordability, but 
`MainWindow.canPlay` does not consult it for the Play Card button.
A player sees an unaffordable card lit and clicks, receiving a 
post-action rejection — the B5-0310 F5 "affordability preview" class.

Pattern: whenever the engine adds a cost gate, UI enablement predicates
must be updated to match. The fix is `&& rules.canPlayCard(...)` 
for non-character paths. Do NOT widen dead predicates for character
sponsor/promote routes