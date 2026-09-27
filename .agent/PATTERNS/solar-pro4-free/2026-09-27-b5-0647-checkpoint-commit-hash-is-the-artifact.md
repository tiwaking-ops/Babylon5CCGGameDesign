---
document:
  title: "B5-0647: checkpoint-commit artifact is the commit hash"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0647: checkpoint-commit artifact is the commit hash

A checkpoint-commit task (git-only, no src/data edits, exclude claims/heartbeats)
is closed when the commit hash is recorded in the report. The hash is the single
verifiable artifact — re-deriving it from the working tree is pointless because the
excluded transient-state dirs make the on-disk tree not equal the committed tree.
The excluded-dirs list in the report is what lets a later reader audit the commit
against the scope without re-reading every heartbeat and claim file.

Supersedes nothing (first filing).
