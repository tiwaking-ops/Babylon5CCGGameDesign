---
document:
  title: "Playable-release checklist and 30-minute human playtest"
  status: "Proposal"
provenance:
  author_llm: {name: "GitHub Copilot", version: "Auto mode"}
  assessor_llm: []
  last_modified_by_llm: {name: "GitHub Copilot", version: "Auto mode"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# B5-2283: Playable-release checklist and 30-minute human playtest

This proposal defines a release candidate as playable without claiming that every
planned mechanic is complete. It is deliberately a checklist and operating script,
not a new rules source. The canonical rules remain
`BABYLON5_CCG_RULEBOOK.md`; current control behavior and known gaps remain in
`docs/playtest-guide.md` and `docs/HUMAN_PLAYTEST_GUIDE.md`.

## Playable definition

A candidate is **playable** only when all of these statements are true:

1. A new player can start the graphical client, choose one of the four supported
   factions, and reach the main game window with the card pool loaded.
2. The player can complete full action rounds against the three AI seats: take
   legal actions, pass, observe the next round, and recover from an illegal or
   unavailable action through an explicit UI status message rather than a crash.
3. The player can reach at least one conflict, participate through the available
   support or oppose decision, observe resolution, and continue into a later round.
4. The player can reach a victory banner in a controlled session. The script may
   use the existing station-victory route when that is the shortest reliable path;
   this proves the end-to-end victory display, not balance.
5. A fresh checkout or clean output directory can reproduce the automated gates
   listed below without external libraries or hand-edited data.

“Playable” does not mean that every engine-ready control is reachable from the
shipped card pool. Contingencies, mercenary offers, and war setup are explicitly
reported as data- or setup-dependent where the current guide says so. A blocked
or unreachable feature must not be silently counted as a failed basic-play
criterion unless the session specifically claims to test that feature.

## Release checklist

Record the date, operator, commit or working-tree identifier, operating system,
JDK version, and whether the card resources were loaded from the build output.

### Automated preflight

Run from the repository root:

```text
javac -version
cmd /c "cd b5ccg && compile.bat"
```

The compile command must exit 0 and report `Build successful`. From Git Bash,
run the wired verification suite as well:

```text
cd b5ccg
RUN_TESTS=1 sh compile.sh
```

The verification command must exit 0 and end with `Verification passed`. It
must include both `HeadlessConformanceTest` and `HeadlessSmokeTest`; the
additional headless gates named in `compile.sh` are part of the same required
verification run. If Git Bash is unavailable, invoke the same compiled classes
directly with `java -cp out`, recording each exit code and final line.

Confirm that the output contains the expected card-load line and that no
fallback/minimal-data error was printed. A compile-only pass is not a playable
release: the human session below is mandatory.

### Human acceptance record

Mark each item `PASS`, `FAIL`, or `NOT APPLICABLE` and attach the game-log
excerpt for every `FAIL`. `NOT APPLICABLE` is permitted only for a feature the
current card pool cannot offer; it is not permitted for the five basic-play
criteria in the definition above.

| Check | Acceptance evidence |
|---|---|
| Start | New player launches the client, selects a faction, sees the board, hand, and three AI seats; card loading is successful. |
| Full turns | Player completes two action rounds, including at least one legal card/action choice and one pass; the round and phase display advance. |
| Illegal-action feedback | Player deliberately selects an unavailable action or unaffordable card; the control stays safe and a status/log explanation appears. |
| Conflict | Player initiates or joins a conflict, makes the available support/oppose choice, sees the sides/outcome readout, and reaches the next action phase. |
| Victory | Player reaches a victory condition in a controlled session and sees a victory banner naming the winner; the client remains responsive afterward. |
| Exit/repeat | Player can close the session normally and start one more session without stale state or a resource-load failure. |

## Thirty-minute first-player script

The operator should not improvise a new rules interpretation during this run.
Use the current UI labels and record any mismatch against the two playtest
guides.

### Minutes 0–5: install and start

1. Run the automated preflight and record its output.
2. Launch the client with `run.bat` or `run.sh`.
3. Choose a faction without consulting implementation details.
4. Confirm that the board, hand, action controls, game log, and three AI
   opponents are visible. Record the card-load count and any startup warning.

### Minutes 5–12: learn one complete turn

1. Select a card that is legal and affordable, then use its displayed action.
2. Use `Pass Turn` once. Observe the initiative/phase readout and game log.
3. If an action is disabled, select it once and record the displayed reason.
4. Continue until the first action round ends and the next round begins.
5. Record whether the hand, board, and controls remain synchronized after the
   round transition.

### Minutes 12–20: conflict and continuation

1. Select a conflict card and choose an explicitly populated legal target.
2. Initiate the conflict and make the available `Support` or `Oppose` choice
   when the controller waits for the human seat.
3. Record the active-conflict sides, participant totals, outcome, reward, and
   any aftermath readout.
4. Continue to the next action phase. A conflict that resolves but leaves the
   client stuck fails the conflict criterion.

### Minutes 20–28: controlled victory path

Use the shortest reproducible victory route exposed by the current build. The
station route is preferred when it can be arranged without editing source or
card data. Record the setup actions, the round in which the condition becomes
true, and the exact victory banner. If the route cannot be reached within this
window, mark the victory criterion `FAIL` rather than inferring success from a
power or influence total.

### Minutes 28–30: repeatability and report

1. Close the client normally.
2. Launch a second session and confirm that faction selection and card loading
   still work.
3. File the result with the environment, exact commands, PASS/FAIL/N/A table,
   round and phase, action sequence, game-log excerpt, expected behavior, and
   actual behavior.

## Defect classification

* **Release blocker:** startup failure, missing card resources, crash, inability
  to complete a full action round, inability to continue after conflict
  resolution, or no reproducible victory banner.
* **Playability defect:** a legal basic action is unavailable, an illegal action
  mutates state, a readout contradicts the engine outcome, or a round transition
  leaves stale controls.
* **Known gap:** an engine-ready feature with no corresponding card or setup path
  in the current pool, provided the UI reports that state honestly.
* **Balance or design observation:** a session completes but raises a question
  about difficulty, pacing, or rule interpretation. File it separately from the
  release verdict.

This checklist becomes an adopted release gate only after a human accepts the
definition and the victory-route setup. Until then it is advisory proposal
material.
