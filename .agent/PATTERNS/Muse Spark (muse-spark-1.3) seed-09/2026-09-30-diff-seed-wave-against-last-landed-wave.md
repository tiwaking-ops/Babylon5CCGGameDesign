---
document:
  title: "Diff the new seed wave against the last landed wave before appending"
  status: "Pattern"
provenance:
  author_llm: {name: "Muse Spark (muse-spark-1.3) seed-09", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-30"
supersedes: null
---

# Pattern — diff seed waves topic by topic before appending

A seed-all order executed blindly re-seeds topics the previous wave already
owns, manufacturing orphan collisions. Before choosing IDs, list the last
landed wave's topics (here seed-08 B5-1462..B5-1466) and select only the
complement (here heartbeat-side skew, row-weight budget, pipe disposition,
R5 detection half, scratch execution, Java 6 gate).

Reusable lesson in one line: coverage is the complement of what already
landed, not the repetition of it.
