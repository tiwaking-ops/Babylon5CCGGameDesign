---
document:
  title: "AGENTS.md — Babylon 5 CCG autonomous development"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-27", note: "edit: section 1a assessor_llm compaction convention, human-approved 2026-09-27 (B5-0655)"}
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-27"
---

# AGENTS.md — Babylon 5 CCG (autonomous, lightweight)

Adapted from `Tiwas-ttrpg-project-documentation` governance/provenance + status-model,
stripped of its heavy human promotion process. This repo is fully autonomous on
shared live files; only external-library use needs a human.

## 1. Provenance (mandatory)

Every LLM-created `.md` MUST open with `author_llm: <name> (<version>)`
(frontmatter above satisfies this). Original `author_llm` is never overwritten.
Any LLM that later assesses, edits, or migrates the doc MUST record an
`assessor_llm` entry (list, earliest first) and update `last_modified_by_llm`
+ `last_modified_date`. Use `unknown` rather than inventing values. Any file
for which no record of authorship can be found SHALL be labelled
`author_llm: {name: "unknown", version: "unknown"}` — never infer authorship
from style, date, filename, or content. Never list
yourself as both author and assessor in the same pass.

### 1a. assessor_llm compaction (adopted 2026-09-27, B5-0655)

One entry per agent per file, carrying a pass count, **not** one appended line per
edit pass. The literal append-per-pass rule turned the ledger's assessor list into
35 entries holding 7 distinct facts — a monotonic copy counter whose only
information content was the pass count, which then stopped being usable as an edit
history.

An assessor entry therefore has the form:

```yaml
assessor_llm:
  - {name: "GPT-6 Codex", version: "GPT-6", passes: 14, last_pass: "2026-09-26"}
  - {name: "Buffy", version: "glm-5.3-flash", passes: 1, last_pass: "2026-09-26", note: "B5-0566 part 23 refresh"}
```

Rules:

1. **Repeat pass** — your `name` + `version` already appears: do **not** append.
   Increment `passes` on your existing entry and set `last_pass` to today.
2. **First pass**, or a same-agent **different version**: append a new entry with
   `passes: 1` and `last_pass` set. Version is part of identity, matching claim files
   and heartbeats, so a version bump is a new entry, never an edit of the old one.
3. `note` is **optional** and only for a substantive pass (a refresh, correction or
   migration) — not for a drive-by metadata touch.
4. An entry is never deleted, renamed, or rewritten beyond its `passes` /
   `last_pass` / `note` fields. The list stays **append-only at the entry level**.
5. **No retro-compaction.** Lists already on disk keep their full contents forever.
   Only edits made after this rule was adopted follow it. Do not consolidate existing
   entries — that is rule 4's deletion case wearing a tidier hat.

Compaction preserves attribution density: counts plus dates carry the same forensic
information as repetition, which is what the coordination forensics this repo has
needed (claim destructions, index absorption, seeding collisions are reconstructed
from pass counts plus report files, not from entry multiplicity). It also makes
non-append deviations rule-compliant rather than requiring a disclosure note for
simply following the rule. Rationale and the rejected alternatives:
`docs/proposals/assessor-list-compaction-proposal.md`.

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

## 6. Self-seeding and file placement (added 2026-09-21 after B5-0319)

* Agents may seed their own tasks ONLY as `OPEN` rows claimed through the
  normal cycle (OPEN → claim → DONE with report). Never mark work DONE that
  was never OPEN+claimed, and never touch `engine/`/`model/`/`ai/`/`ui/`
  outside a claimed scope — including via a self-seeded task.
* No new `.md` files at the repo root. Incoming/external material goes to
  `investigations/` (advisory, never canonical); agent proposals to
  `docs/proposals/`; observations and test results to `.agent/REPORTS/` or
  `docs/reports/`. Root holds only governance, the rulebook, and code.
* One stable `agent_id` per agent across sessions. Assessors are self-added
  only — adding another agent's name is fabrication.
* Shared pattern store (B5-0430, human-approved 2026-09-25): every close-out
  report gains a one-line "Reusable lesson" item, filed as a Markdown record
  under `.agent/PATTERNS/<agent-id>/` (stable agent_id = namespace). The
  store is **advisory only** — same tier as `investigations/`, never
  canonical; copying or citing never confers authority. Read all
  namespaces, write only your own; section-1 provenance rules apply;
  supersede-never-rewrite (a corrected pattern is a NEW file linking the
  old one). Boot skim: glance at the newest records across namespaces
  before claiming (see `.agent/00_BOOT.md` step 10).
