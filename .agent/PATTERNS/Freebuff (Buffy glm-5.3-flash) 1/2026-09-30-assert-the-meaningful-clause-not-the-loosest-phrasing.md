---
document:
  title: "Assert the receipt's meaningful clause, not its loosest phrasing"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1147"
---

# Pattern: assert the receipt's meaningful clause, not its loosest phrasing

**Context.** B5-1147 verified the B5-1107 deletion. The row's phrase
"load stderr shows zero UNKNOWN FIELD lines" is unsatisfiable until
B5-1068 lands (5,396 noise lines fire on every standard field). The
meaningful receipt from B5-1107 was numeric and specific: 5,398 → 5,396,
the 2 timing emissions gone.

**Lesson 1 — a verification inherits the receipt's intent, not its
errors.** The honest verdict here is "landed-clean: the deleted key's
warnings are gone and nothing else moved", with the loose phrasing named
and explained — not a false BLOCKED on an unsatisfiable literal, and not a
quiet redefinition of zero.

**Lesson 2 — numeric receipts verify by recomputation, not by rerunning
the original claim.** 5,396 = 5,398 − 2 was recomputed from a fresh
whole-stderr capture on an independent run. The ×2 pass rule from B5-1104
made the prediction falsifiable: 5,397 would have meant a second carrier.

**Lesson 3 — noise and signal must be counted by key before either can be
asserted about.** Total counts alone could not distinguish "deletion
landed" from "loader stopped validating". The keyed census
(`UNKNOWN FIELD: timing` = 0; armistice present only in id/title/type
noise) is what makes landed-clean a measurement.

Links: [B5-1147 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1147.md) ·
closes the armistice lineage after B5-1107; census discipline per B5-1104/B5-1111.
