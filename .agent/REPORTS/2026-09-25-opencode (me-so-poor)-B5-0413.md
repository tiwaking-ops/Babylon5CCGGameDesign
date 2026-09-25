---
author_llm: opencode (me-so-poor / big-pickle)
---

# B5-0413 — Confirmed-finding fix slice

**Status:** DONE
**Agent:** opencode (me-so-poor) / big-pickle
**Date:** 2026-09-25

## Confirmed finding (from B5-0409, both sessions)

The B5-0349 `parseLog` promote counter is dead. It matched the token
`": promotes "` (leading colon), but no real log line has that shape:
`RulesEngine.java:177` logs `"<name> promotes <title> to the Inner Circle
(...)"` (no colon), and the `GameController` action line is
`"<name>: PROMOTE_CHARACTER: <title> ..."`. Result: harness "Promotions: 0"
even when promotes actually occur — a harness artifact, not a game defect.

## Verdict

Harness-only fix (B5-0376 precedent). No engine/AI slice needed.

## Change

`b5ccg/src/b5ccg/engine/HeadlessMultiRoundTest.java`:
- `parseLog` token `": promotes "` → `" promotes "` (now matches the
  `RulesEngine` execution log; the sole generator of `" promotes "`).
- Updated the javadoc log-format sample to the real line
  `"X promotes CardName to the Inner Circle (...)"` with a B5-0409 tag.

## Gate (green)

- `compile.bat`: Build successful (1 bootstrap warning, pre-existing).
- Runtime proof: `java -cp out b5ccg.engine.HeadlessMultiRoundTest 1 456`
  → `promotes=1` (was 0 before the fix). Re-confirmed the 60s-harness-window
  stall behavior at the same time (Game 1 timed out at round 4 / 60021 ms).
- `HeadlessConformanceTest`: **CONFORMANCE SUITE PASSED (360 checks)**.
- `HeadlessSmokeTest`: **SMOKE TEST PASSED** (round 1 in ~19 s).

Diff: two lines in one harness file, no game-logic or data files touched.