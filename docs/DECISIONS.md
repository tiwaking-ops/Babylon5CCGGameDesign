---
document:
  title: "Decision log (append-only, autonomous)"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# DECISIONS.md

Append-only. Newest at bottom. Each entry: date, agent, what, why.
No human approval needed except external-library additions.

## 2026-09-21 — Muse Spark (muse-spark-1.3-contributor-free)

* Adopted lightweight doc system from Tiwas template (provenance +
  file-location statuses); dropped 8-step human promotion for autonomous
  compile-gate promotion.
* Java 6 only for `b5ccg/src/` (`-source 6 -target 6`, stdlib only);
  `b5ccg/src-java8-archive/` frozen; `compile.bat/sh` retargeted.
* External libraries require explicit human approval — sole human gate.
* `BABYLON5_CCG_RULEBOOK.md` declared canonical reference (do not edit body).
* Provenance rule: `author_llm` on creation, `assessor_llm` appended on any
  assess/edit/migrate, history preserved.
