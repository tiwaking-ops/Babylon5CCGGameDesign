---
document:
  title: "B5-0431 — Seat-mix premise reconciliation"
  status: "Report (advisory; no src/ or resources/ edits)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0431 — Seat-mix premise reconciliation

**Agent:** opencode (me-so-poor)  
**Claim window:** 2026-09-25T09:11:43Z–09:18:56Z  
**Scope:** report only; no source or resource edits.

## Verdict

B5-0422 Option B is **DEAD as written**. The tree has no all-EASY default to replace. Every production or full-game default already contains one EASY seat alongside non-EASY seats. Changing the remaining EASY seat would be a new balance, onboarding, or metrics-baseline decision, not completion of Option B.

## Current executable defaults

- **Production game:** `b5ccg/src/b5ccg/Main.java:47-52` creates one human plus three AI seats in fixed order. `b5ccg/src/b5ccg/Main.java:69-73` assigns Delenn `MEDIUM`, G'Kar `HARD`, and Londo `EASY`: exactly one EASY and two non-EASY AI seats. This four-seat topology cannot be all-EASY because the first seat is human.
- **Smoke harness:** `b5ccg/src/b5ccg/engine/HeadlessSmokeTest.java:37-43` declares Alpha `EASY`, Beta `MEDIUM`, Gamma `HARD`, Delta `MEDIUM`. `HeadlessSmokeTest.java:76-100` builds four all-AI players and preserves that ordered mix.
- **Seeded multi-round harness:** `b5ccg/src/b5ccg/engine/HeadlessMultiRoundTest.java:69-72` uses the same `EASY/MEDIUM/HARD/MEDIUM` mix. `HeadlessMultiRoundTest.java:27-42` exposes only game-count and seed arguments, not a difficulty selector.
- **Reporting/tiebreak integration:** `b5ccg/src/b5ccg/engine/HeadlessReportingTiebreakTest.java:434-440` also uses `EASY/MEDIUM/HARD/MEDIUM`. Contrary to the parallel proposal's description, its EASY slot is not merely a reporting-position fixture: `HeadlessReportingTiebreakTest.java:480-503` starts a real `GameController.runGame()` integration loop. Changing it would rebaseline that live integration, not preserve a proven fixture contract.
- **No hidden all-EASY path:** `b5ccg/run.bat:10` and `b5ccg/run.sh:11` pass arguments to `b5ccg.Main`, but `Main.java:13-29` ignores them. `b5ccg/src/b5ccg/ai/AIPlayer.java:60-67` requires an explicit difficulty in its only constructor. The claim that a user can currently request all-EASY has no executable interface.

Isolated EASY instances in `HeadlessAIDifficultyContractTest` and `HeadlessConformanceTest` are deliberate tier-under-test fixtures, not default seat assignments. They are outside Option B.

## Reconciliation

- `docs/proposals/2026-09-25-solar-pro4-free-B5-0422.md:27-39,80-84` assumes an `[EASY,EASY,EASY,EASY]` default, claims `Main` passes a difficulty array, and says a user can retain all-EASY. All three premises are false against current source.
- `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0426.md:32-37` correctly establishes that no all-EASY default exists. Its wording about “both harness files” is incomplete: Smoke, MultiRound, and Reporting are three mixed full-game harnesses.
- `.agent/REPORTS/2026-09-25-solar-pro4-free-B5-0408.md:9-13`, the probe cited as evidence by the proposal, records the actual `EASY/MEDIUM/HARD/MEDIUM` setup. B5-0408/0409 were mixed-tier runs, not all-EASY runs.
- The parallel proposal `docs/proposals/b5-0422-pass-bias-cascade-design-proposal.md:98-122` accurately lists the current mixes, but its claim that Reporting is not a game loop is contradicted by `HeadlessReportingTiebreakTest.java:480-503`.

## Restated Option B

**No seat-mix change is available under the original rationale.** Production, Smoke, MultiRound, and Reporting already implement dilution. Removing EASY from either harness would eliminate the only EASY full-game sample and redefine the balance baseline; changing Londo from EASY to MEDIUM would deliberately change human onboarding. Either could be proposed independently, but neither repairs the nonexistent all-EASY premise. A new balance policy should not reuse B5-0422 Option B without a fresh task and evidence.

## Reusable lesson

Before acting on a “change the defaults” recommendation, census every executable default and separate seat configuration from isolated tier fixtures; recommendations can preserve a valid mixed baseline while describing a configuration that never existed.

## Verification

Report-only scope: no source or resource files changed, so no compile or test gate applies. Source evidence was re-read directly from the current tree; `b5ccg/src-java8-archive/` was excluded as frozen.
