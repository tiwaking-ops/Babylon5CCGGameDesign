---
document:
  title: "Pattern: SUPERSEDES verifier-exclusion-lists (checker gap stands; single-threadedness heuristic withdrawn)"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# SUPERSEDES: 2026-09-26-verifier-exclusion-lists-must-track-new-payloads.md

Core lesson STANDS: a harness checker that asserts "payload in hand" must
exempt every payload family whose cards live outside the hand (agendas in
play, mercenaries, contingencies); B5-0364 added the agenda family without
extending the smoke step-5 exclusions and the gap fired at ~1-in-8 to ~2-in-30.

WITHDRAWN heuristic 1 ("prove single-threadedness at the check point"): WRONG
for this harness -- the game loop is a daemon that keeps running round 2 while
step 5 runs on live state (HeadlessSmokeTest.java:113-131 breaks on round>1
but never joins the loop). Concurrent state mutation during verification is
possible and is the concurrent session's co-mechanism.

WEAKENED heuristic 2: draw-dependence (unseeded Collections.shuffle, PASS 0
vs DISCARD 1 scoring) explains WHEN the bad offer exists, but watcher-vs-loop
racing can co-trigger it. Both mechanisms converge on the same fix: exempt the
payload types in the checker or quiesce/join the loop before verifying.

New lesson: before publishing a single-mechanism classification for a flake,
enumerate EVERY thread that can still mutate the checked state at check time.
I claimed the check point single-threaded from the polling break condition --
a break condition is not a join.

Source: .agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0495.md (my triage)
+ the concurrent close-out cells in ledger row B5-0495.
