---
document:
  title: "B5-0809 pattern: per-card engine-treatment table is the minimum for a ruling question"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0809 — per-card engine-treatment table for a ruling question

**Reusable lesson** (B5-0809): when briefing a human ruling that asks whether a class of cards is treated one way or another by the engine, deliver a per-card treatment table that names the exact engine path (file, line, dispatch table, or fallback) for each card, plus a count of how many of those paths touch the rule concept in question (here StatKey.POWER / getPower). The card list alone is necessary but not sufficient — the ruling needs to know that the 13 power-titled-or-texted cards are all Influence/Leadership/draw/generic-floor in the engine, that 0 of them read Power, and that the only Power-vs-Influence rule site (RulesEngine.canAffectTarget, line 922, B5-0677) is already landed and green. Without the per-card table the human cannot distinguish "power means influence by design" from "power means influence because no card exercises the POWER channel yet" — which is exactly the permanent-or-per-card fork the ruling is about.

This pattern does NOT confer authority on any ruling; it is the advisory-tier format a report should use when the human asks a yes/no question that depends on per-card engine state. Filed under `.agent/PATTERNS/solar-pro4-free/`.
