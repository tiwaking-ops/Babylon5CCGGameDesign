---
author_llm: opencode (me-so-poor)
assessor_llm: none
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# B5-0442 Report — Attack-Window Regression Audit

## Status

DONE.

## Scope and claim

Self-seeded because B5-0436 remained live under Buffy and the queued follow-ups were gated. Claimed at `2026-09-25T10:19:41Z` with scope limited to this report and one reusable-lesson pattern. No source, documentation, or git files were edited.

## Audit findings

- `GameController` exposes the human join and attack waits as separate observable states.
- The attack submission path accepts only `PASS` or a currently legal `ATTACK_CONFLICT_PARTICIPANT`; invalid actions leave the wait open.
- The controller offers the attack wait only when at least one controlled attacker has a legal opposing committed target.
- `MainWindow` derives the attack-target list from a snapshot of opposing participants and committed cards, revalidates through `RulesEngine.canAttackConflictParticipant`, and maps the selector index to the live card object.
- The attack path has a `(select target)` placeholder and does not auto-pick the first legal target. A prior selection is restored only when that exact card remains in the newly derived legal list.
- A separate legacy fallback remains in `initiateOnly()` for conflict initiation: when no target is selected, it chooses the highest-influence non-human opponent. This is not the B5-0440 participant-attack path and was not changed by this audit.

## Verification

- `b5ccg/compile.bat`: PASS on JDK `1.8.0_292`, `-source 6 -target 6`.
- Existing conformance: PASS, 360/360.
- Headless smoke: PASS.
- Dedicated human conflict-attack engine suite: PASS, 9/9.
- Real-`MainWindow` attack-control regression: PASS, 10/10.
- Forced-headless UI regression: PASS with explicit `SKIPPED (headless environment)`.
- Forbidden Java 7/8 construct scan across the changed attack-window files: clean.
- `git diff --check` across the changed attack-window files: clean.

The conformance suite still reports its pre-existing informational D2/D4-D7/D9-D11/D15 plumbing findings; no new failure was introduced. B5-0436 remains the owner of the shared conformance-file changes.

## Reusable lesson

When removing an automatic target fallback, audit the neighboring action paths separately and name the retained fallback explicitly. A static search hit in a different dispatch path is not evidence that the target path still auto-selects, but it should remain visible as a follow-up.

## Records

- Pattern: `.agent/PATTERNS/opencode (me-so-poor)/2026-09-25-separate-auto-target-fallback-paths.md`.
