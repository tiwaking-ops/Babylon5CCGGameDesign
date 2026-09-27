---
document:
  title: "Pattern — a new legitimate path turns old green fixtures red; read before touching"
  status: "Advisory pattern (B5-0629)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: a new legitimate path turns old green fixtures red

Filed under B5-0629 (Major Victory + Shadow War condition-1 guard).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0629.md`.

## The situation

Implementing Major Victory (rulebook :182) — a mandated engine behavior
change — turned D12 check C red: "major agenda holder at 21 power does not
standard-win" (21 vs 7). The holder was being crowned by the NEW Major path,
correctly. The old check was not regression-failing; it was asserting the
world that no longer exists.

## The rule

1. When an old check goes red after a mandated behavior change, READ it before
   touching anything. Classify: regression (engine broke its promise) vs
   stale fixture (check asserts the pre-change world).
2. Never delete or rewrite the old check's intent. Adjust only the fixture so
   it isolates the property it always meant to test — here, tightening the
   spread from 21-vs-7 to 21-vs-12 keeps the standard-path bar observable
   without the new path reaching it.
3. Disclose the adjustment in DECISIONS and the report, naming which check,
   why it went red, and what it still asserts. A silent fixture edit is a
   green-passing proxy — exactly what this repo's procedure forbids.
4. Respect explicit row constraints ("do NOT edit the two VIC checks") even
   when adjacent files (the VIC javadoc) carry a sanctioned exception.

## Supersedes

None. New pattern.
