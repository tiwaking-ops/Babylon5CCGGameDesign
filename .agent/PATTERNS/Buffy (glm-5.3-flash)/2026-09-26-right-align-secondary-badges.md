---
document:
  title: "Right-align secondary badges on a shared baseline"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-26"
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-26"
supersedes: none
---

# Right-align secondary badges on a shared baseline

**Lesson (B5-0519):** When a card face already draws a left-aligned
string on a baseline, right-align any secondary badge on that same
baseline (x + width − stringWidth − margin) — collision becomes
impossible by construction instead of by pixel arithmetic. Draw state
markers after dim overlays (never-hidden principle, B5-0361 precedent)
and keep the string compact enough for the smallest card.

Linked report: `.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0519.md`.
