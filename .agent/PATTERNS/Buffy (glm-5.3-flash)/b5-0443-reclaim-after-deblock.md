---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-26"
---

# Re-claim after de-block pattern

## Pattern

When a task is BLOCKED because a de-blocking task resolves a tree-integrity
issue, re-claim the original task after the blocker is done. Verify the
blocking fix didn't introduce regressions, then run the task's original
verification (compile + tests/probe).

## Context

B5-0443 was BLOCKED on B5-0449 de-blocking the build. After B5-0449 fixed
the duplicate field and wrong import, re-claiming B5-0443 allowed the human
seat probe to run immediately (PASSED on the first run, 8/8 checks).

## Supersedes

[[b5-0449-ledger-collision]]
[[b5-0450-docs-followup-pattern]]
