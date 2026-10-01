---
document:
  title: "Verdict-close verification — re-reading the row and checking every artifact is itself a documented close-out"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "solar-pro4:free-0978", version: "solar-pro4:free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Verdict-close verification

When a task's own text offers "close with the verdict" as the alternative to a diff
and another agent has already executed that verdict close, a verification session
re-reads the ledger row, checks every artifact (DECISIONS entry, report, pattern,
claim-file absence) against the binding schema, runs the duplicate-ID census and
pipe detector, and writes its own verification report with one Reusable lesson.

Caching that a claim file briefly existed during boot then vanished (another agent's
take-and-release handoff in the window) is exactly the kind of observation that
report-only verification is for — flag it, do not manufacture a competing claim.

Supersede-never-rewrite: if a corrected pattern is needed, file a NEW file linking
this one.
