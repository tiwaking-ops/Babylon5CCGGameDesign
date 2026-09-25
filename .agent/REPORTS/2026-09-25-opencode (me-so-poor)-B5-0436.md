---
author_llm: opencode (me-so-poor)
assessor_llm: []
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# B5-0436 Report — D-remainder conformance sections

## Status

DONE.

## Scope and claim

Claimed B5-0436 at `2026-09-25T10:36:54Z` after the governed stale-claim reap required by the B5-0432 gate. The only source scope was `b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java`; no game-logic, model, UI, or resource files were edited. Concurrent worktree changes were left untouched.

## Delivered

- R1 at `HeadlessConformanceTest.java:3419` now reports D1-D14 resolved, D15 partial by effect coverage, and dedicated D6 and D7 assertions.
- R2 at `HeadlessConformanceTest.java:1395` replaces the inherited D6 fixture with a deterministic two-player `MEDIUM` AI fixture. Each player has two Inner Circle leaders and two fleets; the test invokes the private action-phase loop and verifies two actions per player, termination with every player passed, and no safety-cap or game-over exit.
- R3 at `HeadlessConformanceTest.java:1437` covers Build Influence with explicit non-ambassador Inner Circle leaders at rating 4 and rating 9, including the rating-cap and already-rotated-leader no-op cases.
- R4 at `HeadlessConformanceTest.java:1496` covers winner-only `influenceReward`, zero-reward conflicts, and an opposer win. The opposer card is committed with `commitCard(o3, big, false)`, preserving opposition semantics.

No production behavior was changed; the D15 fixture correction and all other changes are conformance-test changes only.

## Verification

- `b5ccg/compile.bat`: PASS on JDK `1.8.0_292` with `-source 6 -target 6`.
- `RUN_TESTS=1 ./compile.sh` through Git Bash: PASS, including 56 source files, conformance `373/373`, and smoke `PASS`.
- Direct `HeadlessSmokeTest`: PASS; 446 cards, 4-player AI round, 26 AI actions, 33 UI callbacks, and 4/4 legal decisions.
- Dedicated `HeadlessHumanConflictAttackWindowTest`: PASS, 9/9.
- Added-line Java 7/8 construct scan for the target diff: no matches.
- `git diff --check` for the target file: clean.

## Reusable lesson

A conformance remainder should use explicit, non-ambassador fixtures at each semantic boundary and verify both positive transitions and no-op rejection paths; a deterministic action-loop fixture can prove round termination without relying on a safety cap.

## Records

- Ledger: `.agent/TASK_LEDGER.md`, B5-0436 row.
- Decision log: `docs/DECISIONS.md`, B5-0436 entry.
- Pattern: `.agent/PATTERNS/opencode (me-so-poor)/2026-09-25-cover-d15-opposition-and-d6-termination.md`.
