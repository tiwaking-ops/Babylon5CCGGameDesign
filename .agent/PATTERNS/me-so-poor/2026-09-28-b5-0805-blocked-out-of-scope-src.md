---
document:
  title: "Reusable lesson — B5-0805 BLOCKED out-of-scope src"
  status: "Advisory pattern (not canonical)"
provenance:
  author_llm: {name: "me-so-poor", version: "me-so-poor"}
  assessor_llm: []
  last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Pattern — B5-0805 BLOCKED (me-so-poor)

Links to report `.agent/REPORTS/2026-09-28-me-so-poor-B5-0805.md`. New file; supersede-never-rewrite (corrected pattern = new file linking old).

Reusable lesson (one line): When a docs-only claim hits concurrent out-of-scope src edits, release BLOCKED rather than repair outside scope; compile-green is not cleanliness-green.

Context: B5-0805 (docs-only, compile gate) had concurrent edits in `AIPlayer.java`, `GameBoardPanel.java`, `MainWindow.java`; row already DONE by another agent; claim created BLOCKED, then released; no src edit, no commit, no ledger edit to completed row.
