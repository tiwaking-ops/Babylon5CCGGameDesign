---
author_llm: opencode (me-so-poor)
version: big-pickle
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# Pattern — parameterize-and-classify-runner-termination

## When

A seeded multi-round / scenario runner wraps a game loop in a background
thread and waits on it with a fixed wall-clock ceiling. The ceiling fires
before the game's natural termination, so every long game is misreported as a
timeout and harvest metrics that depend on post-run log parsing look like
defects.

## What to do

1. Make the ceiling a parameter (CLI arg + sane default), never a magic
   constant.
2. After the wait, classify the outcome from *observable* signals only —
   never guess: `winner != null` → WINNER; thread dead + `done` flag set +
   no winner → ROUND_CAP (the system's own safety cap ended the round);
   timeout elapsed + thread still alive → TIMEOUT.
3. Print per-round (or per-iteration) progress *during* the wait so a silent
   long run is visibly progressing, not hung.
4. Preserve any prior counter/regex fixes in the same file — don't let the
   wait loop refactor silently clobber a parser token (the B5-0413
   `": promotes "` → `" promotes "` lesson: a stale token makes a real
   action look like zero).

## Why

A timeout is a harness artifact, not a game defect. Classifying it as such
(stopping the lie that "the game timed out" when it was "we stopped watching
at 60s") keeps balance/probe reports from mis-attributing stall causes. The
B5-0408/B5-0422 stall conversation only made sense once 0444 could show a
real `WINNER` at round 11 under a longer cap.

## Caveat

This pattern addresses *reporting*, not *behavior*. It does not tune AI
pass bias or change the game loop — those are separate design decisions
requiring human approval (see B5-0422 option A).
