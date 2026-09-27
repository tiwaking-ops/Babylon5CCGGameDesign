---
document:
  title: "Re-read provider API surface before writing a gated UI row"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Re-read provider API surface before writing a gated UI row

**Context:** B5-0665 (ui leg) is gated on B5-0663 (engine/model leg). The gate exists because the UI row consumes an engine query that the engine row provides, and getting the query signature wrong means a compile error or a silent wrong render.

**Lesson:** When a task is gated on a provider task that has already landed, do not rely on the provider's report summary or the task description to know the API shape. Re-read the actual files: the enum (every value and its order), the result type (fields, null contract, convenience methods), and the query method signature. The parts that matter for the consumer are exactly the parts the report may summarise in prose — enum names, null semantics, qualifier types — and prose can be wrong or ambiguous where the source is not.

**What B5-0665 did:** re-read VictoryPath.java (five values, order), VictoryPathResult.java (path/power/qualifier, null-path contract, getQualifierAsInt vs getQualifierAsString), and RulesEngine.checkVictoryPath() before writing MainWindow.java. The null-path rendering (`GAME OVER — No winner` instead of `Winner: None`) came directly from the null contract in VictoryPathResult's javadoc, not from the B5-0665 task description.

**Applies to:** any row whose gate says "claim ONLY after X is DONE" and whose scope consumes an API X adds.
