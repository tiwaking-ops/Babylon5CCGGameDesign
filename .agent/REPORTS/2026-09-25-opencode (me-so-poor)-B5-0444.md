---
author_llm: opencode (me-so-poor)
version: big-pickle
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# B5-0444 Report — Multi-round runner timeout + natural-termination reporting

## Status

DONE.

## Scope and claim

Self-seeded (AGENTS.md §6, B5-0444 seeder note): the multi-round runner
`HeadlessMultiRoundTest.java` had a hardcoded 60s per-game timeout that fired
before natural termination (~118s/R12 at Rating 20, per the B5-0422/B5-0408
honesty notes). Claimed `2026-09-25T23:04:00Z` UTC. Scope is this one harness
file only — zero game-logic, model, UI, resource, or docs edits.

## What changed (`b5ccg/src/b5ccg/engine/HeadlessMultiRoundTest.java`)

1. **Parameterized timeout** — `60000L` hardcoded → `180000L` default, overridable
   via a 3rd CLI positional arg `timeoutSec`. Usage line updated to
   `[numGames] [seed] [timeoutSec]`. Invalid values fall back to the 180s
   default with a stderr warning.

2. **Per-round progress** — the game-watch loop now prints
   `[game N] reached round R after Xs` whenever `state.getRoundNumber()`
   advances during the wait, so long games are observable instead of silent
   until timeout.

3. **Terminating-condition classification** — each game now emits an explicit
   `terminator=` field on its result line with one of:
   - `WINNER` — `state.getWinner() != null` (rulebook victory).
   - `ROUND_CAP` — the game thread exited on its own without a winner (the
     per-action safety cap at `MAX_ACTIONS_PER_ROUND = 8 × playerCount`
     ended the round; the thread is dead and `done[0]` is true).
   - `TIMEOUT` — the per-game timeout fired while the thread was still alive.

   The classification is computed *after* the wait loop from the observed
   `done[0]`, `t.isAlive()`, and `state.getWinner()` signals, so it cannot
   mislabel a natural finish as a timeout.

4. **B5-0413 promotes-counter fix preserved** — `parseLog` matches the token
   `" promotes "` (leading space, no colon), exactly as B5-0413 set it. A
   120-second run reported `promotes=4`, confirming the counter is live (it
   was always 0 before 0413's fix). The Javadoc log-format example string
   `"X: INITIATE_CONFLICT: CardName -> target"` is untouched.

## Verification (2026-09-25, UTC)

- `b5ccg/compile.bat`: PASS on JDK `1.8.0_292` with `-source 6 -target 6`;
  only the expected bootstrap class-path warning; MainWindow unchecked note
  unchanged.
- `java -cp b5ccg/out b5ccg.engine.HeadlessConformanceTest`: PASS,
  **373/373** (unchanged by this harness-only change — the suite file is
  HeadlessConformanceTest.java, which 0444 did not edit).
- `java -cp b5ccg/out b5ccg.engine.HeadlessSmokeTest`: PASS.
- Dedicated human-conflict-attack engine suite (`HeadlessHumanConflictAttackWindowTest`,
  from B5-0432): PASS 9/9.
- Real-`MainWindow` attack-control regression (`MainWindowAttackControlTest`,
  from B5-0441): PASS 10/10.
- Forced-headless UI regression: explicit `SKIPPED (headless environment)`.
- **Terminator classes observed live**:
  - `terminator=WINNER winner=Alpha` at round 11 (~142s, natural termination,
    300s timeout arg) — `java ... HeadlessMultiRoundTest 1 42 300`
  - `terminator=TIMEOUT winner=stalled` at round 10 (~122s, 120s arg, timeout
    fired) — `java ... 1 42 120`
  - `terminator=TIMEOUT` at rounds 4–9 under 60s and 120s caps —
    `java ... 2 42 60`
- B5-0413 counter live: `promotes=4` at 120s (was 0 pre-0413).
- **Java 6 construct grep** on the touched file: clean. The only `-->` / `->`
  hit is a Javadoc comment example string (`CardName -> target`), a false
  positive — no code constructs use lambdas, method references, streams,
  `computeIfAbsent`, `@FunctionalInterface`, try-with-resources, or diamond.
- `git diff --check`: clean (no whitespace errors).

## Reusable lesson

When a harness's hardcoded timing ceiling fires before the system-under-test
naturally terminates, parameterize the ceiling and surface the real
termination signal (here `state.getWinner()` / thread-liveness) so callers can
distinguish "the process finished" from "we stopped watching" — otherwise
harvest metrics (here the promote counter) look like defects when they are
just invisibly truncated.

## Records

- Pattern: `.agent/PATTERNS/opencode (me-so-poor)/2026-09-25-parameterize-and-classify-runner-termination.md`.
