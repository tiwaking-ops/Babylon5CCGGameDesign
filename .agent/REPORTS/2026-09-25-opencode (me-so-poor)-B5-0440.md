---
document:
  title: "B5-0440 — Human attack target selection UI"
  status: "Report (completed UI slice)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# B5-0440 — Human attack target selection UI

**Agent:** opencode (me-so-poor)  
**Claim:** B5-0440, started 2026-09-25T09:50:30Z  
**Scope:** `b5ccg/src/b5ccg/ui/` only; no engine or model edits.

## Outcome

The live B5-0432 controller wait is now reachable in Swing. During that wait, the human selects an attacker on the board, receives an explicit dropdown containing only targets accepted by `RulesEngine.canAttackConflictParticipant(...)`, and must choose a target before Attack becomes actionable. The prior first-valid-target fallback is removed.

The existing Pass control becomes **Skip Attack** while the wait is live, submits the accepted PASS path, clears attack selection, and returns to the normal Pass Turn label afterward. Outside the live wait, Attack, its selector, and the attack-specific decline state remain disabled. The engine still revalidates the submitted pair before closing the wait.

## Implementation

- `MainWindow.java:108-110`: target selector, index-to-card list, and selected target state.
- `MainWindow.java:479-503`: explicit-pair dispatch plus selector listener; no auto-pick path remains.
- `MainWindow.java:1035-1089`: legal target refresh from snapshotted participants and committed cards, preservation only when the prior target remains legal, and authoritative button revalidation.
- `MainWindow.java:1193-1245,1289-1295`: live wait observation, contextual Skip Attack label, and prompt.
- `MainWindow.java:1579-1580,1626-1632`: board-selection refresh and complete post-submit reset.

Target labels are display-only. A parallel card list maps the selected index to the actual object, so duplicate titles cannot redirect a submission.

## Verification

A transient Java 6 probe constructed the real `MainWindow`, drove `GameController` through join and attack waits, and passed 10/10 checks:

- Skip Attack is enabled and relabeled only in the live wait.
- No attacker selection keeps selector and Attack disabled.
- One attacker exposes two distinct legal targets.
- Attack stays disabled until an explicit target is chosen.
- Choosing the second target executes against that target and leaves the first untouched.
- Submitted target state clears.
- Skip Attack resolves without attacker rotation or target damage.

Final gates:

- `b5ccg/compile.bat`: green on `javac 1.8.0_292`, `-source 6 -target 6`.
- `HeadlessHumanConflictAttackWindowTest`: PASS, 9/9.
- `HeadlessConformanceTest`: PASS, 360/360.
- `HeadlessSmokeTest`: PASS, full AI round.
- Java 8+ construct grep on `MainWindow.java`: no matches.
- `git diff --check`: clean apart from the ledger's existing LF-to-CRLF warning.
- Adversarial read-only review found no blocker, high, or medium issue in the attack path.

## Reusable lesson

A human-choice selector should map indices to live objects rather than trust display labels, rederive every legal option on each refresh, revalidate at click time, and always provide a tested decline path before enabling a blocking engine wait.
