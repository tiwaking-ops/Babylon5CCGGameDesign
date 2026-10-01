---
author_llm: GitHub Copilot (Auto mode)
task: B5-1115
agent_id: "GitHub Copilot (Auto mode) 0930"
utc: "2026-09-30T06:45:46Z"
---

# B5-1115 report

## Result

DONE as a read-only execution verification. No source or card JSON files were
changed.

## Verification

`cmd /c b5ccg\compile.bat` passed with JDK 1.8.0_292 and the repository's
`-source 6 -target 6` gate.

The git-ignored scratch probe loaded the real `de_loc_immolan_v` record. Its
printed `influencePerRound` was 1. With the location installed in its owner's
location zone, `RulesEngine.startRound` increased influence from 4 to 5 and
the location continued to report 1.

The probe then drove an uncontested `LOCATION_TARGET` war to completion. The
captured location was moved to the capturer's location zone and rotated.
`getInfluencePerRound()` reported 0 before and after the next `startRound`;
the capturer's influence did not change. This separately verifies the payment
path and the captured-effect suppression path rather than treating a zero as
evidence that no income loop exists.

The engine and UI agree: an owned location displays and pays its positive
value, while a captured or rotated location displays and pays zero.

## Gate receipt

- `javac -version`: `1.8.0_292`
- `b5ccg\compile.bat`: exit 0
- probe compile: Java 6 source and target, exit 0
- probe run: seven assertions passed, `PROBE-COMPLETE`, exit 0
- source/card-data edits: none
- commit/push: none

Reusable lesson: validate both a positive income case and an intentional
suppression case before concluding that a displayed economy value is wired.
