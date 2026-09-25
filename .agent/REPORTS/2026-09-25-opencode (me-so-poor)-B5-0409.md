---
document:
  title: "B5-0409 report — B5-0408 finding verification"
  status: "Report"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0409 — B5-0408 finding verification

**Status:** DONE (DUAL COMPLETION — concurrent sessions)
**Agent:** opencode (me-so-poor) / big-pickle

> **Collision note (added at close-out):** a concurrent session (solar-pro4:free)
> independently reaped the same stale claim and completed B5-0409 first, flipping
> the ledger row to DONE with its own verification (report:
> `.agent/REPORTS/2026-09-25-solar-pro4-free-B5-0409.md`, DECISIONS entry at
> ~L2214). This report is the SECOND independent verification. It reaches
> materially stronger conclusions than the ledger row records and MUST be read
> for the B5-0413 actionability decision:
> * Ledger row credits the "100% stall" + "pass-bias consequence" attributions.
>   THIS report proves by no-timeout probe that games terminate naturally
>   (stall = 60s harness window) and isolates a NEW confirmed harness bug the
>   ledger missed: **B5-0349 `parseLog` promote counter is dead** (its
>   `": promotes "` token matches no real log line; `RulesEngine.java:177`
>   logs `" promotes "` and the action line is `"PROMOTE_CHARACTER"`).
> * B5-0409 row was NOT re-flipped by this session (another agent's close-out);
>   this report is observation-only per provenance rules.
**Date:** 2026-09-25
**Task:** `B5-0409 — B5-0408 finding verification (self-seeded per AGENTS.md §6, Buffy boot 03:07:44Z)`
**Scope:** harness execution only; scratch under git-ignored `b5ccg/out/scratch` deleted after; zero tracked-file source edits.

## 1. Prerequisite note (per HANDOFF §8 first-actions)

Reported before any edit this session: `javac 1.8.0_292` (JDK 8, `-source 6` OK); working tree ahead of commit `7f8f1e3` with modified `ai/AIPlayer.java`, `engine/HeadlessConformanceTest.java`, `ui/MainWindow.java`, ledger, DECISIONS, playtest-guide; `.agent/TASK_LEDGER.md` carries the REAPED note for the stale B5-0409 claim (started_utc 03:18:40Z, TTL 30 min expired, heartbeat silent since ~03:22Z, no report on disk) reaped per 00_BOOT step 9, then re-claimed by this session.

## 2. Method

1. Gate (compile.bat, JDK 1.8.0_292 `-source 6`): **green** — 0 errors, only the expected bootstrap warning.
2. Reproduced the verbatim B5-0408 command on the current tree:
   `java -cp b5ccg/out b5ccg.engine.HeadlessMultiRoundTest 5 456` (5 games, seed 456, 60s per-game daemon-thread window, identical player/RNG setup).
3. Scratch probe `b5ccg/out/scratch/Probe0409.java` (B5-0312 precedent, git-ignored, deleted after): mirrors game-1 seed-456 deck/player/RNG wiring exactly, but drives `controller.runGame()` with **no harness 60s timeout** (240s watchdog only) and snapshots per-player influence rating/pool, IC/supporting size, hand/deck, forfeit, and promotion-legality count at every round boundary.
4. Source inspection to classify each 0408 attribution against the tree (GameController, RulesEngine, GameAction, AIPlayer, HeadlessMultiRoundTest).

## 3. Reproduction of the seeded 5/456 run (current tree)

| Game | Seed | Round | Winner | Conflicts init/won/lost | Promotes | Builds | Aftermaths | Agendas |
|---|---|---|---|---|---|---|---|---|
| 1 | 456    | 7  | **Gamma**   | 13/6/5  | 0 |  7 | 4 | 0 |
| 2 | 10456  | 4  | stalled*    |  5/3/1  | 0 |  7 | 5 | 0 |
| 3 | 20456  | 4  | stalled*    |  8/3/2  | 0 | 11 | 7 | 0 |
| 4 | 30456  | 6  | **Gamma**   | 14/5/4  | 0 | 10 | 5 | 0 |
| 5 | 40456  | 4  | stalled*    |  4/4/0  | 0 |  6 | 2 | 0 |

`*` "stalled" = harness label for `state.getWinner() == null` at the 60s daemon-thread window close (HeadlessMultiRoundTest.java:144-145).

Aggregate: 44 conflicts initiated, 21 won / 12 lost (11 winner lines untracked by the parser), 0 promotes, 41 builds, 23 aftermaths, 0 agendas. Winner distribution: Alpha 0, Beta 0, **Gamma 2**, Delta 0. Elapsed: 294408 ms total.

**Reproduction diverges from B5-0408.** 0408 reported 100% stall (5/5, zero wins, rounds 4-5). The current tree terminates 2/5 games with a genuine winner (Gamma, HARD seat) within the 60s window; the other three are simply mid-game (round 4) when the window closes. The same command+seed did not reproduce 0408's per-game rows — evidence of tree drift (AIPlayer.java working-tree edits landed after 0408's run: MER-AI B5-0403, Tier-1/2 AI scoring B5-0377) or a mis-recorded 0408 run. Current-tree truth governs.

## 4. Stall-vs-timeout scratch probe (seed 456, no harness 60s window)

`Probe0409` run, game seed 456, watchdog 240s:

- **The game terminated naturally at round 12, t=118489 ms, winner Alpha (HUMAN/EASY seat)** — standard victory: Alpha Influence Rating 20 vs 15 (Beta), 12 (Gamma), 11 (Delta). Not a stall.
- Rounds advanced steadily every ~8-10s (R1@2s, R2@20s, … R12@116s) → the D6 consecutive-pass action loop (B5-0372) terminates rounds cleanly; there is **no action-phase hang**.
- Per-player end state: Alpha inf 20/20 IC=0; Beta 15/15 IC=2; Gamma 12/12 IC=0; Delta 11/11 IC=2. Promotion-legality probes: promoLegal=0 for all four at end (peak 1, Delta R2-R3).

**Conclusion:** "stalled" in the harness output means "no winner within the 60s harness window", never an action-phase hang and rarely a true unreachable-game. Games in this economy terminate by standard victory once a player crosses Influence Rating 20 (~2 min), or by earlier strict-leader wins (Gamma at rounds 6-7).

## 5. Finding-by-finding classification of B5-0408

| # | 0408 claim | Verdict |
|---|---|---|
| F1 | "100% stall; all 4 players 0 wins" | **STALE / NOT REPRODUCED** — 2/5 current-tree games end with a winner (Gamma). "stalled" = 60s harness window. |
| F2 | "matches the known D6 limitation: pass loop terminates after one pass per player (actionsLeft cap)" | **STALE ATTRIBUTION** — D6 (B5-0372) landed: `GameController.runActionPhase` is an initiative-cycle consecutive-pass loop, no per-player cap (source `GameController.java:87-137`); probe rounds advance ~8-10s each. |
| F3 | "no damage/heal/repair — Tier-3 not yet implemented" | **STALE** — B5-0368 (damage model), B5-0370 (attack), B5-0371 (heal/repair) DONE; live in engine/model with conformance coverage. |
| F4 | "zero promotions … AI may not be choosing to promote" | **CONFIRMED OBSERVATION, WRONG CAUSE — harness counting artifact** (see §6). Promotions DO occur (probe: Beta/Delta IC 1→2) but B5-0349's parser never counts them. |
| F5 | "zero agendas — 20-influence threshold unreachable before timeout" | **CORRECTED** — reachable: probe Alpha hit Rating 20 at R12 and won at the round boundary. Not "unreachable": standard victory simply outruns the agenda economy in sampled games. |
| F6 | "no mercenary bids — pool carries zero mercenary evidence" | **CONFIRMED (unchanged, data-side)** — zero mercenary cards in pool (B5-0386 no-evidence verdict); engine/AI/UI (B5-0395/0403/0404) have nothing to exercise. |
| F7 | "initiators won 71% of trackable" | **CONFIRMED directionally** — current tree 21/33 = 64%. Consistent with B5-0309 (initiator wins iff support > opposition at initiation) + B5-0343 (initiate when winning). Parser blind spot: 11 winner lines untracked. |
| G1 | "D6 … until D6 lands probes will consistently timeout" | **STALE** — D6 landed (B5-0372); "timeout" is the 60s window. |
| G2 | "no mercenary cards" | **CONFIRMED**. |
| G3 | "agenda threshold unreachable" | **CORRECTED** — reached in probe; outrun by standard win. |
| G4 | "no damage/heal/repair" | **STALE** — B5-0368/0370/0371 DONE. |

## 6. NEW confirmed finding — B5-0349 parseLog promote counter is dead (harness bug)

`HeadlessMultiRoundTest.parseLog` counts promotions with `content.contains(": promotes ")` (line 244 in the current source, and documented at line 203 as the intended log format "`X: promotes CardName`"). **No log line in the codebase produces that token:**

- The controller logs the *action* line first: `state.log(p.getName() + ": " + action)` → `"Delta: PROMOTE_CHARACTER: Jeff Sinclair (leader: Londo)"` (`GameController.processAction` + `GameAction.toString`, no `" promotes "`).
- The *execution* line is `state.log(p.getName() + " promotes " + ch.getTitle() + " to the Inner Circle (...)" )` (`RulesEngine.java:177`) — leading space, **no colon** before `"promotes"`.
- The parser's `": promotes "` (colon + space + `promotes`) matches **neither** format, so the promote count is always 0.

Behavioral confirmation: the probe shows Beta and Delta Inner Circles growing 1→2 (real promotions executed through `executePromote`) while the same seed's harness output reports `promotes=0`. Every multi-round aggregate built on this harness (B5-0349 itself, B5-0408, this reproduction) undercounts promotions to 0.

Correct fix (B5-0413 candidate): change the parser token to match `" promotes "` (no colon) — or the action-line token `"PROMOTE_CHARACTER"` — and re-verify. Harness-file-only; game logic untouched.

## 7. Secondary observations

- **Promotion legality is rare in this economy** (peaks at 1 laggard count; 0 at end of the probe game) — ambassadors periodically leave the Inner Circle (Alpha IC 1→0, Gamma IC 1→0 across rounds), removing the rotator `canPromote` requires, and supporting-role characters are scarce. This is the real balance note behind 0408's F4, distinct from its stated cause.
- **Build Influence parity:** both this reproduction's 41 builds and 0408's 44 are real-counted (the parser token `" builds influence:"` matches `RulesEngine.java:114`). Ratings climb ~+1/build, which is what ultimately produced the probe's 20→win.
- **Per-game determinism caveat:** identical seed + command did not reproduce 0408's rows on the current tree (AIPlayer working-tree edits post-0408, or a mis-recorded 0408 run). Flag for the record; re-runs on one tree are consistent.

## 8. Gate

- `compile.bat` (JDK 1.8.0_292, `-source 6`): **exit 0**, 0 errors, only expected bootstrap warning.
- `HeadlessConformanceTest`: **360/360 PASS**.
- `HeadlessSmokeTest`: **PASS** (446 cards; round 1 in 16364 ms; 27 AI actions, 34 callbacks, 4/4 legal).
- Java 6 construct grep: not applicable (no tracked source edits).
- Scratch files (`b5ccg/out/scratch/Probe0409.java`, `.class`, `repro_5_456.txt`, `probe0409_result.txt`): git-ignored, deleted at close-out.
- Zero tracked-file edits: no `src/`, no `resources/`, no governance .md edited by this task (ledger REAPED note + report are the only ledger/DECISIONS contributions, per protocol).