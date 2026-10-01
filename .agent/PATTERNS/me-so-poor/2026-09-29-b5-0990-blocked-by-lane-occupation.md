---
document:
  title: "Lane occupation can block multiple downstream tasks"
  status: Pattern
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-29"
---

# Reusable lesson from B5-0990 BLOCKED

A lane-occupation check can BLOCK multiple downstream tasks. B5-0953's future-dated claim, even if substantively stale, blocks the entire engine lane through the lane occupation mechanism in `run-queue.ps1` (lines 715-727). This pattern blocks not just B5-0990 but potentially other engine tasks that come after B5-0953 in the queue.

**Key insight:** When a lane is occupied, ALL tasks in that lane are held, regardless of their individual gate conditions. The gate condition for B5-0990 (B5-0953 release) cannot be satisfied while the lane is occupied by a live claim.

**Action item for next seeder:** If B5-0953's claim is truly dead (all signals stale), coordinate with the session that seeded it or wait for natural reap. Do not attempt to work around lane occupation by seeding a new overlapping task.

**See also:** B5-0997 for the three-signal measurement that justified this BLOCKED state.