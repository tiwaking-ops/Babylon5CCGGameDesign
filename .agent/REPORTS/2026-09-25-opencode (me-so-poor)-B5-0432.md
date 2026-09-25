---
document:
  title: "B5-0432 — Human conflict-attack decision window"
  status: "Report (completed engine slice)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0432 — Human conflict-attack decision window

**Agent:** opencode (me-so-poor)  
**Claim:** B5-0432, started 2026-09-25T09:20:27Z  
**Scope:** `b5ccg/src/b5ccg/engine/` only; no model or UI edits.

## Outcome

`GameController` now exposes a human attack decision after the mandatory join/participation sequence and before conflict resolution. The window opens only for the non-forfeiting human, only when a callback exists, and only when `RulesEngine.canAttackConflictParticipant(...)` finds at least one legal controlled attacker and opposing participant.

The collected action is the existing `GameAction.ATTACK_CONFLICT_PARTICIPANT`, so B5-0370 damage, faction, participation, fleet-leader, severe-overflow, and resolution gates remain authoritative. `PASS` skips the optional attack. Invalid attack submissions leave the window open, and a valid attack resumes the same resolution path with B5-0309 side totals unchanged. The AI-only path is unchanged.

## Files

- `b5ccg/src/b5ccg/engine/GameController.java:26-27,537,674-763`: volatile window state, attack wait, legal-offer scan, collection, pass handling, interruption cleanup, and public readiness accessor.
- `b5ccg/src/b5ccg/engine/HeadlessHumanConflictAttackWindowTest.java`: dedicated Java 6 headless conformance suite. It was added instead of editing the shared conformance file because B5-0436 had a live claim on that file.

## Coverage

The dedicated suite checks nine properties: join window precedes attack window; attack window follows join; invalid attack remains pending; valid attack closes the window; joined opposition side is preserved; the existing B5-0370 mutation path executes; pass closes without attack; pass reaches the window; no legal opposing participant creates no wait.

## Verification

- `b5ccg/compile.bat`: green on `javac 1.8.0_292` with `-source 6 -target 6`.
- `java -cp out b5ccg.engine.HeadlessHumanConflictAttackWindowTest`: PASS, 9/9 checks.
- `java -cp out b5ccg.engine.HeadlessConformanceTest`: PASS, 360/360 checks.
- `java -cp out b5ccg.engine.HeadlessSmokeTest`: PASS, including a complete AI round.
- Java 8+ construct grep on both touched files: no matches.
- `git diff --check`: clean apart from Git's existing LF-to-CRLF warning for the shared ledger.

## Follow-up

This row is engine-scoped. `MainWindow` still gates Attack on ACTION phase, active-human turn, and a selected card, so the new wait is not yet user reachable. Seeded B5-0440 owns the UI target selector and live-window button/pass wiring; until that task lands, the engine slice must not be described as Swing-complete.

## Reusable lesson

A blocking engine decision window is a half-feature unless its caller can observe, submit, and decline; sequence and side invariants need dedicated coverage, and live shared-file claims should be respected with a scoped new test instead of a write-through.
