---
document:
  title: "Cold-boot sequence for autonomous agents"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-27"
---

# 00_BOOT — read this first, every session

1. Read `AGENTS.md`, `guidelines/Guidelines.md`, `docs/DECISIONS.md`.
2. Read `.agent/TASK_LEDGER.md` + list `.agent/CLAIMS/*.json` +
   `.agent/HEARTBEATS/*.json`. A task with a live claim (age < 30 min) is taken.
3. Run `javac -version` (expect JDK 8, e.g. `1.8.0_292`) and record it in your
   heartbeat. Build gate is `b5ccg/compile.bat` (or `compile.sh`):
   `javac -source 6 -target 6`, stdlib only.
4. Obtain the OPEN-task census ONLY by running the shared census tool:
   `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun`
   (or its bash equivalent `bash .agent/run-queue.sh -DryRun`). Do NOT read or
   grep `TASK_LEDGER.md` directly to find OPEN tasks — hand-rolled censuses miss
   double-pipe rows and other structural defects, silently hiding claimable work.
   The one-liner `rg -o '^\|+(?:\s*)(B5-\d+)(?:\s*\|)\s*\|/{0,1}OPEN' .agent/TASK_LEDGER.md`
   is the manual fallback if the shared tool is unavailable. A task ID mentioned in
   a narrative field (e.g. a heartbeat `last_completed`) is a mention, not a claim.
   An absent liveness signal is UNKNOWN — never treat it as live.
5. Pick the highest-priority `OPEN` task with no live claim.
6. Claim it atomically: create `.agent/CLAIMS/<task-id>.json` (see
   `.agent/CLAIMS/README.md`). If the file already exists, abort and pick
   another. Never overwrite or delete another agent's claim.
7. Work ONLY inside the claimed scope. Small diffs. Java 6 only.
   `b5ccg/src-java8-archive/` is frozen. No external libs without human approval.
8. Verify: `compile.bat/sh` green. On red, mark task `BLOCKED` with the log
   excerpt and release your claim.
9. Finish: update `TASK_LEDGER.md` row, append `docs/DECISIONS.md` entry, write
   `.agent/REPORTS/<date>-<agent-id>-<task-id>.md` (with `author_llm`), delete
   your claim file, refresh `.agent/HEARTBEATS/<agent-id>.json`. When editing
   the ledger, preserve the table pipes exactly — never add or remove a `|`.
10. Claims older than 30 min are stale: you may reap one ONLY after noting the
   reaping in `TASK_LEDGER.md`. Never touch live claims or heartbeats.
11. Shared pattern store (standing convention, B5-0430): every close-out report
   gains a one-line **Reusable lesson** item, and the author files it as a
   Markdown record under `.agent/PATTERNS/<agent-id>/` (front matter with
   `author_llm`; supersede-never-rewrite — a corrected pattern is a NEW file
   that links the old one). Agents read all namespaces but write only their
   own. Boot skim: when reading this file, also glance at the newest records
   across namespaces so prior lessons shape your work before you claim.
