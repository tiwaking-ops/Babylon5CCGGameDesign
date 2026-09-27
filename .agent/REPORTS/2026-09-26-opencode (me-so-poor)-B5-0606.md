---
document:
  title: "B5-0606 close-out — CVD rulebook II:193-195 deck-construction quotas"
  status: "Observation"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# B5-0606 close-out — CVD rulebook II:193-195 deck-construction quotas

Task: implement the smallest conformance section that covers the highest-value
gap identified by the B5-0594 audit (rulebook-to-suite coverage audit). Gap 1
from that audit was "§II deck-construction rules have no conformance
asserts". Gate B5-0594 satisfied (report on disk, DECISIONS entry, DONE in
ledger).

## Delivery

Added `testDeckConstruction()` with the `CVD` section code to
`b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java` (14 new checks, suite
count rose 471 → 485):

* **Starting Ambassador census** — dynamically discovers all cards in
  `b5ccg/resources/cards/premiere.json` where `isAmbassador()` is true (5
  ambassadors across sets: Jeffrey Sinclair, Londo Mollari, Delenn, G'Kar,
  Delenn Transformed). Asserts ≥1 ambassador is found.
* **Per-race deck-construction quota assertions** (rulebook II "Preparing to
  Play", "Customizing Your Game Deck" :193-:195) for all four Premier Edition
  playable races (HUMAN, CENTAURI, MINBARI, NARN):
  1. **Minimum 45 cards (rulebook :193)** — each race's fixed starter deck
     contains exactly 50 cards (PASS for all 4).
  2. **Maximum 3 copies of any card (rulebook :194)** — per-card copy counts
     summed; no card exceeds 3 copies (max is 3 copies for
     `event_level_the_playing_field`, 2 for `char_centauri_agent`; all others
     1; PASS for all 4).
  3. **Exactly one Starting Ambassador (rulebook :195)** — each deck carries
     exactly 1 ambassador card from its own race (Sinclair for Human, Londo
     for Centauri, Delenn for Minbari, G'Kar for Narn; PASS for all 4).
* **Overall deck-data integrity** — asserts all four required race starter
  decks are present and accounted for in
  `b5ccg/resources/decks/premiere-starter-decks.json`.
* **Wiring** — `testDeckConstruction()` wired into `main()` immediately after
  `testMinesReactive()`.

## Verification

* `compile.bat`: `Build successful` (JDK 1.8.0_292, `-source 6 -target 6`, stdlib only).
* `HeadlessConformanceTest`: **485/485 checks PASS** (all 14 CVD checks green).
* `HeadlessSmokeTest`: `SMOKE TEST PASSED` (DeckLoader, GameState and full AI round ran headless, 4/4 decisions legal).
* Java 6 construct grep on touched file: 0 code-context hits (only comment/string arrow chars, verified clean).
* Suite file only: zero game-logic edits, zero model edits, zero resource edits.

## Notes

* Reusable lesson: rulebook deck-construction quotas can be asserted
  data-first through the parser before any dynamic deck-construction UI or
  engine validator exists — asserting the shipped data satisfies the rulebook
  guarantees the baseline is green before dynamic deck-building arrives.