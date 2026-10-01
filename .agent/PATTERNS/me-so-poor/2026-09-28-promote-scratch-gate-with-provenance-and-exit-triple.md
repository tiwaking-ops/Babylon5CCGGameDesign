---
document:
  title: "Reusable lesson — promote a gitignored scratch gate with provenance and exit-triple, record reason in DECISIONS"
  status: "Advisory — not canonical"
  namespace: "me-so-poor"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  assessor_llm:
    - {name: "me-so-poor", version: "me-so-poor", passes: 1, last_pass: "2026-09-28", note: "B5-0833 close-out; links report 2026-09-28-me-so-poor-B5-0833.md; supersedes nothing (first of this lesson)"}
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Reusable lesson — promoted scratch gate (B5-0833)

When promoting a gitignored scratch instrument (e.g., a verification script) to a tracked tool:

1. Record `author_llm: unknown / unknown` in frontmatter — original may have none; never invent.
2. Add your `assessor_llm` entry (one per agent-version, pass count, not one line per edit).
3. Give the file an exit triple (`0/1/2`) so unreadable-input cannot report clean.
4. Record blocking-vs-reporting per finding class in the file and in DECISIONS, not silently.
5. Record promotion reason in `docs/DECISIONS.md` (enforcement path stated explicitly: compile.bat primary, script supplementary); never imply authority from file presence alone.
6. Annotate `.gitignore` (not silently remove) so both root copy and tracked copy have an explainable state.
7. File a report with `Reusable lesson` line, and a NEW pattern file (supersede-never-rewrite) linking the report; delete claim; refresh heartbeat per binding schema.

Links: report `.agent/REPORTS/2026-09-28-me-so-poor-B5-0833.md`; task B5-0833 (ledger line 976, status updated to DONE in close-out); DECISIONS entry 2026-09-28 me-so-poor.
