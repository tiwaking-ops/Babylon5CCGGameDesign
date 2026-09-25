---
author_llm: opencode (me-so-poor)
assessor_llm: none
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# B5-0441 Report — Persistent Human Attack-Window Swing Regression

## Status

DONE.

## Scope and claim

Self-seeded after the B5-0440 queue scan found B5-0436 live under Buffy and B5-0437 through B5-0439 gated. Claimed B5-0441 normally at `2026-09-25T10:15:17Z` with scope limited to one new `b5ccg/src/b5ccg/ui/` test file. No production, engine, model, resource, or shared conformance file was edited.

## Delivered

Added `b5ccg/src/b5ccg/ui/MainWindowAttackControlTest.java`, a Java 6-compatible standalone Swing regression test. It constructs a real `MainWindow`, advances a live `GameController` conflict through the human join and attack waits, and verifies:

- the join and attack windows become observable;
- the contextual pass control reads `Skip Attack`;
- no attacker selection leaves the target selector and Attack button disabled;
- selecting a board attacker exposes both legal targets;
- an explicit target is required;
- choosing the non-default target enables Attack and mutates only the chosen live card;
- post-submit target state clears;
- `Skip Attack` resolves without rotating or neutralizing either target.

The test checks `GraphicsEnvironment.isHeadless()` before constructing Swing components and prints an explicit skip in a genuinely headless environment.

## Verification

- `b5ccg/compile.bat`: PASS on JDK `1.8.0_292`, `-source 6 -target 6`.
- Desktop real-`MainWindow` regression: PASS, 10/10 checks.
- Forced-headless invocation: PASS with explicit `SKIPPED (headless environment)`.
- Java 6 forbidden-construct scan for the new file: clean.
- `git diff --check` for the new file: clean.

The first compile attempt identified incorrect model-enum imports in the new test; imports were corrected before the final green run. No production code was changed.

## Reusable lesson

A persistent real-Swing regression should keep the desktop checks but branch to an explicit headless skip before constructing any window. This preserves local coverage without making a desktop-only test fail in a display-less build environment.

## Records

- Decision: `docs/DECISIONS.md`, B5-0441 entry.
- Pattern: `.agent/PATTERNS/opencode (me-so-poor)/2026-09-25-guard-swing-tests-with-headless-skip.md`.
