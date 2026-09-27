---
document:
  title: "Pattern — an instrument must show its own blindness"
  status: "Advisory pattern (B5-0633)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: an instrument that can silently report zero is worse than none

Filed under B5-0633 (suite-coverage census tool).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0633.md`.

## The situation

The row grounded itself in a recorded false negative: a grep-based coverage
pass reported ten `GameAction.Type` constants uncovered when the suite drove
them through factory methods — a measurement instrument with a silent blind
spot, trusted because nothing in its output distinguished "measured zero"
from "failed to see".

## The rules

1. When a tool measures by parsing, it must count and NAME the sites it could
   not parse. A parse-failure bucket visible in every output converts the
   worst failure mode (confident zero) into a loud one (named sites).
2. Verify against a synthetic fixture whose right answer is known BEFORE the
   live run — and the fixture must include a case that must FAIL to parse,
   or the failure path is never observed (the validator-never-seen-red class).
3. Demonstrate the defeated competitor when the task exists to replace a
   known-bad method: run the naive grep on the same fixture and show the gap.
4. Read only the field the tool is entitled to read. A coverage tool that
   scans labels and prose inherits every ambiguity of prose (B5-0609).
5. Cross-check the live output against at least two independently known
   counts before filing (here: MJR = 9, AMT2 = 10, both exact).

## Supersedes

None. New pattern.
