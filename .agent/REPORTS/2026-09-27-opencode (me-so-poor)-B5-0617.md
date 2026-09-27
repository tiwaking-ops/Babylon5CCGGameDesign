---
document:
  title: "B5-0617 close-out — Standard Victory conditions 1 & 2 conformance section (VIC)"
  status: "Observation"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0617 close-out — Standard Victory conditions 1 & 2 conformance section (VIC)

Task: add `testVictoryConditions()` with section code `VIC` to
`HeadlessConformanceTest.java` asserting rulebook Standard Victory conditions
1 and 2 (:175-:184). Covers gap 3 identified in the B5-0594 coverage audit.
Suite file only, zero game-logic edits.

## Delivery

Added `testVictoryConditions()` with the `VIC` section code to
`b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java` (8 checks, suite count
rose 485 → 493):

* **Standard Victory (Condition 1)**: 20+ power and strictly leading by ≥1
  power over second place produces victory (PASS).
* **Standard Victory tiebreak (D12)**: tie at 20+ power produces NO winner
  (crowns nobody, PASS).
* **Threshold gate**: power under 20 never triggers victory even with a
  strict lead (PASS).
* **Station Victory (Condition 2)**: 20+ station influence with a
  strictly-leading standard-eligible leader crowns that player (PASS).
* **Major Agenda bars Condition 2**: a player with a revealed Major Agenda
  cannot win Condition 2 even if they lead overall (PASS), and tied eligible
  opponents yield no station winner (PASS).
* **Hidden Major Agenda is inert**: a face-down Major Agenda does NOT bar its
  owner from winning Condition 2 (:520 rulebook, PASS).
* **Shadow War suppression**: Shadow War suppresses Condition 2 Station
  Victory (PASS).
* **Wiring**: `testVictoryConditions()` wired into `main()` immediately after
  `testDeckConstruction()`.

## Verification

* `compile.bat`: `Build successful` (JDK 1.8.0_292, `-source 6 -target 6`, stdlib only).
* `HeadlessConformanceTest`: **493/493 checks PASS** (all 8 VIC checks green).
* `HeadlessSmokeTest`: `SMOKE TEST PASSED` (DeckLoader, GameState and full AI round ran headless, 4/4 decisions legal).
* Java 6 construct grep on touched file: 0 code-context hits (only comment/string arrow chars, clean).
* Suite file only: zero game-logic edits, zero model edits, zero resource edits.

## Notes

* Reusable lesson: victory predicates with multi-condition branches (standard
  power threshold, station influence crown, major-agenda eligibility filter,
  inert hidden agendas, Shadow War suppression) should be asserted in an
  exhaustive single-section matrix with isolated two-player and three-player
  state fixtures, verifying each condition both independently and in
  suppression combinations.