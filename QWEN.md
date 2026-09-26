---
document:
  title: "QWEN.md — qwen-code tool pins for this repo (Windows)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-25"
---

# QWEN.md — Babylon5CCGGameDesign (qwen-code, Windows)

## 1. Shell tool name (critical — stops the `shell not found` loop)

* The ONLY shell tool is `run_shell_command`.
* NEVER call `shell`, `bash`, `Shell`, or `Bash` as a tool name.
  `Bash`/`Shell` are valid ONLY inside `settings.json` permission rules
  (e.g. `Bash(git *)`), never as a tool call.
* Correct call shape: `{ "command": "<cmd>", "description": "<one line>",
  "directory": "<absolute path within workspace, optional>" }`.
* If you get `Tool "shell" not found in registry`, you used the wrong name.
  Stop, retry the SAME command with `run_shell_command`, do NOT loop on `shell`.
* Prefer dedicated tools over shell: `read_file` (NOT cat/head/tail),
  `glob` (NOT find/ls/dir), `grep_search` (NOT grep/rg/findstr in shell).

## 2. Windows — `cmd.exe /c`, not bash

* Commands run via `cmd.exe /c`. There is NO `head`, `tail`, `wc`, `cat`,
  `grep`, `sed`, `awk`, `find ... -exec`, `ls`.
* Safe patterns:
  * `git -C C:\temp\projects\Babylon5CCGGameDesign diff --stat -- b5ccg/src/b5ccg/ui/MainWindow.java`
  * `git -C C:\temp\projects\Babylon5CCGGameDesign status --short`
  * `b5ccg\compile.bat` (build gate, JDK 8, `-source 6`)
  * `findstr /n "pattern" <file>` for quick content search (or use `grep_search`)
* NEVER pipe through `head -100`, `wc -l`, `tail`. Use `read_file` with
  offset/limit, or `git diff --stat`, instead.
* Do NOT use newlines to separate commands. Chain dependent commands with `&&`.

## 3. Repo rules (summary — `AGENTS.md` wins on conflict)

* `b5ccg/src/` is Java 6 only (`javac -source 6 -target 6`, stdlib only).
  No lambdas, method refs, streams, `computeIfAbsent`, try-with-resources.
* `b5ccg/src-java8-archive/` frozen — never edit.
* Claim before edit: create `.agent/CLAIMS/<task-id>.json`; one writer per
  scope (`engine/`, `model/`, `ai/`, `ui/`); 30-min TTL.
* Boot: `.agent/00_BOOT.md`; tasks: `.agent/TASK_LEDGER.md`.
