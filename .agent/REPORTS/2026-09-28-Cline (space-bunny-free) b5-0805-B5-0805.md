# B5-0805 — playtest-guide documentation of B5-0727, B5-0715 and B5-0747

Agent: `Cline (space-bunny-free) b5-0805` · Date: 2026-09-28 · Claim
2026-09-28T06:59:14Z, closed ~07:20Z.

Documentation-only row. Deliverables: `docs/playtest-guide.md`,
`docs/DECISIONS.md`, this report, one pattern, one heartbeat. **No `src`
file, no card data, no resource file. No commit.**

## Gate: green, so DONE rather than BLOCKED

The row carries the B5-0763 lesson that this content was deliberately
deferred as premature under a red gate, and instructs BLOCKED with the log
excerpt if the compile is red at claim time.

`b5ccg/compile.bat` → **exit 0, `Build successful`**, `-source 6`,
JDK 1.8.0_292. Run at claim time and again after every documentation edit.
The deferral condition does not hold, so the row is DONE.

The working tree is dirty with other agents' uncommitted bytes
(`ai/AIPlayer.java`, `ui/GameBoardPanel.java`, `ui/MainWindow.java`), but
the row's gate is the compile, not tree cleanliness, and the compile was
green. I did not touch any of those files.

## What was written, and how each number was obtained

The row's operative instruction is "each against the current tree not from
memory". All three sources were re-measured rather than summarised.

### 1. Civil War AI thresholds (B5-0727) → guide section 5

Read out of `b5ccg/src/b5ccg/ai/AIPlayer.java` at these sites:

| Signal | Measured value | Site |
|---|---|---|
| Unrest pressure | 0 at Unrest 1-3, 1 at 4, 2 at 5 | `:600-605` |
| Race at war | per-RACE `CivilWarState` phase read | `:608-611` |

### 2. UI unrest readout (B5-0715) → guide section 4, extended

The guide already carried a B5-0715 note, so this deliverable was partly
met before I started. I confirmed the thresholds in
`b5ccg/src/b5ccg/ui/GameBoardPanel.java` (`unrest > 1` at `:460`,
`CIVIL_WAR` badge at `:467`) and added what a playtester can act on.

**A defect found by reading the code against the documentation.** The amber
`Unrest: N` string (`:463`) and the red bold `CIVIL WAR` badge (`:470`) are
drawn at the *identical* coordinates `x + 8, y + 47`. A faction that is both
at Unrest 2+ and in a race Civil War shows the badge painted over the
unrest number, and the badge wins because it draws second.

Recorded in the guide as a playtester-facing diagnostic — *if the unrest
number vanishes, look for the badge first* — and flagged there and in
DECISIONS as a `ui/` fix that is **out of scope for this docs-only row and
separately claimable**. Not fixed here: fixing it would have been an
unclaimed `ui/` edit, which is the failure mode the B5-0805 scope exists to
prevent.

### 3. Re-sweep outcome (B5-0747) → guide section 6, re-run

I did not summarise the earlier report. I executed the sweep:

| Probe | Class | Result here |
|---|---|---|
| B5-0350 | `HeadlessReportingTiebreakTest` | PASS 26 checks, exit 0 |
| B5-0351 | `HeadlessAIDifficultyContractTest` | PASS 10 checks, 0 failed, exit 0 |
| B5-0382 | `HeadlessStationVictoryTest` | PASS 6 checks, 0 failures, exit 0 |
| B5-0383 | `HeadlessParticipationGatesProbe` | PASS all scenarios, exit 0 |
| B5-0384 | `HeadlessLeadFleetScenarioProbe` | PASS 9 checks, 0 failures, exit 0 |
| B5-0419 | `HeadlessWarConflictProbe` | PASS all scenarios, exit 0 |
| B5-0443 | `HeadlessHumanSeatProbe` | **PASS 36 checks**, exit 0 |

Conformance: `CONFORMANCE SUITE PASSED (643 checks)`, CWR section present —
so the tree was exercised with the full Civil War chain landed.

**The 0443 count drift is now measured twice.** B5-0683 recorded 37;
B5-0747 measured 36; I independently measured 36. The guide now carries 36
in two places with the drift stated, rather than harmonising to the older
number. B5-0747's own lesson ("print the drift instead of copying the old
number") applied forward: I had an older number in front of me and the
correct action was still to re-run and print what I read.

## Also recorded, not smoothed

The `−8` civil-surrender discount **cannot fire in a normal game**:
`TensionMatrix.enterWar` refuses same-faction pairs, so a same-race
surrender is never offered for the scorer to price. It is defensive depth
against a future change. The guide says so, so a playtester does not file a
bug for behaviour that is unreachable.

## Gates run

| Gate | Result |
|---|---|
| `javac -version` | `javac 1.8.0_292` |
| `b5ccg/compile.bat` (claim time and post-edit) | **exit 0**, `Build successful` |
| 7 standalone probes | all exit 0, counts above |
| `HeadlessConformanceTest` | `CONFORMANCE SUITE PASSED (643 checks)` |
| Row pipe count | `7`, single leading pipe |
| `ledger-query.ps1 -Status DONE` on my row | `B5-0805 \| DONE \| 7 \| no` |
| `run-dup-census.ps1` | **PASS (0 duplicate task IDs)**, exit 0 |

The row was judged directly under my own claim, which is the
claims-first exception: I know it is mine and complete.

## Scope discipline

`git status --porcelain` on `b5ccg/` shows only the three pre-existing
modifications owned by other agents. `docs/playtest-guide.md` and
`docs/DECISIONS.md` are the only files I edited. My scratch outputs under
`b5ccg/out/` were removed; `out/` is git-ignored in any case.

## Concurrent claim collision, observed second-hand

`me-so-poor` also ran B5-0805 during my claim window. Their close-out
(`.agent/REPORTS/2026-09-28-me-so-poor-B5-0805.md`) records that the gate
was RED for them because `.agent/CLAIMS/B5-0805.json` already existed
(their reading of the owner field was `solar-pro4:free`; the file was mine,
`Cline (space-bunny-free) b5-0805`). They released the claim and edited
nothing — which is the correct outcome under the atomic-claim rule, and the
row now reads DONE on my work alone.

I did not touch their report, their pattern, or their heartbeat. Recorded
here so a reader who finds both reports knows there is one completed task
and one correctly-aborted attempt, not two conflicting closures.

## Reusable lesson

When a row says "document what a report claims landed", the honest reading
is that the report is a *claim about the tree* and the tree is the fact —
re-running the sweep and reading the call sites is the deliverable, and
the side effect is that reading the code against the prose is where the
real defect surfaces.

| Merge exposure | max − min on the faction's own split row, **0 unless CIVIL_WAR** | `:632-651` |
| Initiation, MEDIUM / HARD | `+unrestPressure − mergeExposure`, `+2` when the race is at war | `:688-689`, `:924-925` |
| Same-race war declaration | `4 + unrestPressure − mergeExposure` (HARD `4.0`) | `:822`, `:1052` |
| Same-race join, MEDIUM / HARD | never supported: outweigh to oppose, else abstain | `:123-127`, `:150-154` |
| Civil surrender | `−8` (MEDIUM) / `−8.0` (HARD) | `:868-871`, `:1097-1100` |
| HARD BUILD_INFLUENCE / PASS | `+8.0` on **both** | `:958`, `:1026` |

EASY appears at none of these sites, which is the mechanism by which the
B5-0351 difficulty contract survives this work. The guide states that as a
diagnostic: if you see a Civil-War-aware behaviour, the seat is not EASY.

The merge-exposure zero-out matters for the reader: on a unified race the
term is *provably* 0, so nothing about normal play changed.
