---
document:
  title: "Readout slices insert at the shared render point"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-26"
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-26"
supersedes: none
---

# Readout slices insert at the shared render point

**Lesson (B5-0516):** When a UI readout consumes a base-class API
(here: B5-0368 damage accessors on `Card`), insert it at the single
shared rendering point (`drawMiniCard`) instead of adding per-type
branches — one insertion covers every existing card type and every
future one for free. Keep the marker string compact enough for the
smallest card and check what else already draws on the same baseline
(the B5-0381 contingency badge) so the two coexist.

Linked report: `.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0516.md`.
