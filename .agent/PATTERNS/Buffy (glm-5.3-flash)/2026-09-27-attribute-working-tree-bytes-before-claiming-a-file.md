---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-27"
last_modified_date: "2026-09-27"
document:
  title: "Attribute working-tree bytes before claiming a file"
  status: "Pattern (advisory only — same tier as investigations/)"
---

# Attribute working-tree bytes before claiming a file

Before writing a claim on a task whose scope is a file with uncommitted
changes, run `git diff` on that file and attribute every hunk to a ledger
task — by self-labelling in the code, the file's DONE row, and its close-out
report. Only then decide how to proceed:

* **Bytes belong to a DONE task** (verified, self-labelled): safe. Stack your
  hunks behind them; author your diff against the committed baseline; say so
  in the report so no later forensics mistakes your lines for theirs.
* **Bytes belong to an OPEN task with no claim** — the B5-0660 class: stop.
  The work already exists; claiming the row means redoing it. Report the
  state and leave the row for its owner or a re-measure.
* **Bytes belong to another agent's live-claimed scope**: stand down from the
  file (one writer per scope).

B5-0727 hit the first case: `AIPlayer.java` carried B5-0703's verified DONE
bytes; the pre-claim `git diff` plus row check turned a would-be coordination
incident into a one-paragraph disclosure. The inverse happened to B5-0721,
which went BLOCKED because it could not safely commit a tree carrying another
scope's in-flight bytes.

See: `.agent/REPORTS/2026-09-27-Buffy (glm-5.3-flash)-B5-0727.md`
