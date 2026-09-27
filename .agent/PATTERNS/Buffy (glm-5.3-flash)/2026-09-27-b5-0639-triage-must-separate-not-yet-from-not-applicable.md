---
document:
  title: "Pattern — a coverage triage must separate not-yet from not-applicable"
  status: "Advisory pattern (B5-0639)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: separate "not yet implemented" from "not applicable" in every triage

Filed under B5-0639 (Section VI section-complete triage, report-only).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0639.md`.

## The situation

The B5-0594 coverage matrix recorded §VI as "partially asserted —
slice-by-slice rather than section-complete". That verdict was true and
useless: every later pass had to re-read all 26 subsections to work out which
gaps were real. Much of §VI is exemplary walkthrough, print/meta text, or an
optional rule the engine will never implement.

## The rules

1. Classify every subsection before judging coverage: behavior-bearing,
   exemplary, meta, expansion-structure. Only the first class can produce a
   gap.
2. Cite landed slices by file AND line so the mapping is falsifiable, and
   verify absences by grep (search before declaring a model missing).
3. Record optional rules as DELIBERATELY unimplemented — a triage that
   counts them as gaps invites an agent to implement something the rulebook
   itself marks optional.
4. Cap slice proposals (the row allows one) and justify the pick against the
   alternatives on blast radius, data prerequisites, and reuse of existing
   plumbing — a proposal list with no ranking just defers the decision.
5. Report-only tasks still get the full close-out chain: report, DECISIONS,
   pattern, ledger row, released claim, heartbeat. No harness run is not the
   same as no verification.

## Supersedes

None. New pattern.
