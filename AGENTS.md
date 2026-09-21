---
document:
  title: "AGENTS.md — Babylon 5 CCG autonomous development"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# AGENTS.md — Babylon 5 CCG (autonomous, lightweight)

Adapted from `Tiwas-ttrpg-project-documentation` governance/provenance + status-model,
stripped of its heavy human promotion process. This repo is fully autonomous on
shared live files; only external-library use needs a human.

## 1. Provenance (mandatory)

Every LLM-created `.md` MUST open with `author_llm: <name> (<version>)`
(frontmatter above satisfies this). Original `author_llm` is never overwritten.
Any LLM that later assesses, edits, or migrates the doc MUST append an
`assessor_llm` entry (list, earliest first) and update `last_modified_by_llm`
+ `last_modified_date`. Use `unknown` rather than inventing values. Any file
for which no record of authorship can be found SHALL be labelled
`author_llm: {name: "unknown", version: "unknown"}` — never infer authorship
from style, date, filename, or content. Never list
yourself as both author and assessor in the same pass.

## 2. Hard build rules

* `b5ccg/src/` is Java 6 only: `javac -source 6 -target 6`, stdlib only.
  No lambdas, method refs, streams, `computeIfAbsent`, `@FunctionalInterface`,
  try-with-resources, or diamond beyond Java 6.
* `b5ccg/src-java8-archive/` is frozen — never edit.
* No external libraries without explicit human approval (propose library +
  license + why stdlib cannot suffice; do not vendor until approved).
* `BABYLON5_CCG_RULEBOOK.md` is canonical reference — do not edit the body;
  record interpretations in `docs/DECISIONS.md`.

## 3. Statuses (lightweight, file-location based)

* `canonical/` + root rulebook + `b5ccg/src/` code = current truth.
* `docs/proposals/` = candidates, never truth until merged + compiled.
* `docs/reports/` = observations, test results, no authority.
* `docs/archive/` = superseded, retained for history.
* No authority from date, filename, length, or repetition. Copying or
  summarising never confers authority.

## 4. Autonomous promotion (no committee)

A proposal becomes truth when: (1) claimed scope only, (2) `compile.bat/sh`
green on JDK 8 with `-source 6`, (3) change logged in `docs/DECISIONS.md`,
(4) claim released. No human ruling needed except the external-library gate.
If in doubt, log the ambiguity in the report and STOP that item only — do not
block unrelated work.

## 5. Shared-files protocol (executable: `.agent/`)

Boot: `.agent/00_BOOT.md`. Tasks: `.agent/TASK_LEDGER.md`. Claim before edit
by creating `.agent/CLAIMS/<task-id>.json` (atomic — if it exists, the task is
taken); heartbeat at `.agent/HEARTBEATS/<agent-id>.json`; report to
`.agent/REPORTS/<date>-<agent-id>-<task-id>.md`. One writer per
scope (`engine/`, `model/`, `ai/`, `ui/`); 30-min TTL; small diffs; never touch
another agent's claim or heartbeat. Git is change-tracking only, not authority.
