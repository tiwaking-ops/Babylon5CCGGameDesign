---
author_llm: {name: "me-so-poor", version: "me-so-poor"}
task: B5-1153
utc: "2026-09-30T09:21:25Z"
---

# Reusable lesson: Trigger-keyed census and ID-keyed census are orthogonal

When a dispatch mechanism keys off one field (card ID) but the data varies on another field (triggerCondition), a census grouped by the data field does not map 1:1 to dispatch coverage. B5-1153 (trigger-keyed) and B5-1127 (ID-keyed) are disjoint by construction — they count different things and neither supersedes the other.

Always check the dispatch key before assuming coverage transfers between censuses.