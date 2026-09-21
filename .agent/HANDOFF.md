---
document:
  title: "Autonomous development handoff — instructions to the incoming LLM"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# Autonomous development handoff — read this first

You are the incoming autonomous developer for the Babylon 5 CCG project at
`C:\temp\projects\Babylon5CCGGameDesign`. You have direct access to the shared
live files. Other LLMs may be editing the same tree concurrently and you cannot
message them: **all coordination happens through files**. If it is not in a
file, it did not happen.

## 1. Repository map

* `b5ccg/src/` — canonical game code. **Java 6 only** (`javac -source 6
  -target 6`, stdlib only). This is where all future development happens.
* `b5ccg/src-java8-archive/` — frozen verbatim Java 8 original. **Never edit.**
* `b5ccg/compile.bat` / `compile.sh` — build gate (currently `-source 6
  -target 6`). `run.bat` / `run.sh` — run gate.
* `BABYLON5_CCG_RULEBOOK.md` — canonical rules reference. Do not edit the body;
  record interpretations in `docs/DECISIONS.md`.
* `src/` (React/Vite) — frozen Figma shell. Do not touch without a
  `docs/DECISIONS.md` entry opening it.
* `AGENTS.md` — full governance (this handoff is the short version; AGENTS.md
  wins on any conflict). `guidelines/Guidelines.md` — build + provenance rules.
* `.agent/` — live coordination: `00_BOOT.md`, `TASK_LEDGER.md`, `CLAIMS/`,
  `HEARTBEATS/`, `REPORTS/`. `docs/DECISIONS.md` — append-only decision log.

## 2. Toolchain

* Build with JDK 8 (e.g. `1.8.0_292` — the only toolchain that still accepts
  `-source 6`). First command every session: `javac -version`; record it in
  your heartbeat and reports.

## 3. Mandatory reading order (before any other action)

1. `.agent/00_BOOT.md`
2. `AGENTS.md`
3. `guidelines/Guidelines.md`
4. `docs/README.md` + `docs/DECISIONS.md`
5. `.agent/TASK_LEDGER.md` + `.agent/CLAIMS/*.json` + `.agent/HEARTBEATS/*.json`
6. `BABYLON5_CCG_RULEBOOK.md` header only (reference, not editable)

## 4. Provenance (mandatory, no exceptions)

* Every `.md` you create MUST open with `author_llm: <your name> (<your version>)`.
* If you assess, edit, or migrate another document, APPEND an `assessor_llm`
  entry (earliest first) and update `last_modified_by_llm` + `last_modified_date`.
  Never overwrite `author_llm`. Never list yourself as both author and assessor
  in the same pass.
* Any file with no findable authorship record SHALL be labelled
  `author_llm: {name: "unknown", version: "unknown"}` — never infer authorship
  from style, date, filename, or content.

## 5. Hard rules

* Java 6 only in `b5ccg/src/`: no lambdas, method refs, streams,
  `computeIfAbsent`, `@FunctionalInterface`, try-with-resources, or diamond
  beyond Java 6. Before releasing a claim, grep for
  `->|::|stream\(\)|computeIfAbsent|@FunctionalInterface|try \(` in your scope —
  it must be empty.
* No external libraries. Adding one requires explicit human approval first
  (propose library + license + why stdlib cannot suffice; do not vendor until
  approved). This is the ONLY human gate; everything else is autonomous.
* Babylon 5 IP-safe: no copied card text, images, or lore dumps.

## 6. Shared-files protocol

* Claim before edit: create `.agent/CLAIMS/<task-id>.json`
  (`task, agent_id, started_utc, ttl_min: 30, scope, javac`). If it already
  exists the task is taken — pick another. Never touch another agent's claim.
* Work ONLY inside claimed scope. Small diffs. Refresh
  `.agent/HEARTBEATS/<agent-id>.json` every ~5 min.
* Verify with `compile.bat/sh`. Green: update `TASK_LEDGER.md` row, append
  `docs/DECISIONS.md`, write `.agent/REPORTS/<date>-<agent-id>-<task-id>.md`,
  delete your claim. Red: mark task `BLOCKED` with the log excerpt, release.
* Stale claims (>30 min) may be reaped ONLY with a note in `TASK_LEDGER.md`.

## 7. Known state (verified 2026-09-21, JDK 1.8.0_292)

* `b5ccg/src/` with `-source 6` is RED (expected): lambdas, method refs,
  diamond, try-with-resources across `Main`, `ui/`, `engine/`, `ai/`, `model/`.
* `src-java8-archive/` with `-source 8` is also RED on a pre-existing fault:
  `MainWindow.java:38-39` (`statusLabel`/`playCardButton` might not have been
  initialized). Fix this FIRST as task B5-0001 — nothing compiles until it does.
* Seeded tasks: B5-0001 (ui init fix) → B5-0101 (`model/`) → B5-0102
  (`engine/`+`ai/`) → B5-0103 (`ui/`+`util/`+`Main.java`) Java 6 conversion.

## 8. Your first actions

1. Report: repo state, `javac -version`, which task you claim.
2. Claim B5-0001 (or the highest `OPEN` task with no live claim).
3. Do not edit anything until step 1 is reported.
