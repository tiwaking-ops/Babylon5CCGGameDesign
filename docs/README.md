---
document:
  title: "docs index"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# docs — lightweight autonomous log

* `DECISIONS.md` — append-only record of what changed and why. No formal
  promotion process; a green `compile.bat/sh` + ledger entry is sufficient.
* `proposals/` — candidate work. Never edit canonical files directly from here.
* `reports/` — build/test observations. No authority.
* `archive/` — superseded material, kept for history.
* Canonical truth lives in: `../BABYLON5_CCG_RULEBOOK.md`,
  `../b5ccg/src/` (Java 6), `../AGENTS.md`, `../guidelines/Guidelines.md`.
* Live coordination lives in `../.agent/`: boot at `00_BOOT.md`, tasks at
  `TASK_LEDGER.md`, claims, heartbeats, reports.
