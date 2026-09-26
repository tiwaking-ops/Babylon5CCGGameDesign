---
document:
  title: "Pattern: verifier exclusion lists must track new action-payload families"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Verifier exclusion lists must track new payload families

When a task adds a new GameAction family whose payload is not a hand card
(agendas in play, mercenaries, table cards), every harness guard that asserts
"payload in hand" must gain the new type in the SAME pass. B5-0364 added
DISCARD/REPLACE/REVEAL_AGENDA with in-play agenda payloads but the smoke
harness's step-5 exclusion list (HeadlessSmokeTest.java:206-216) was not
extended, leaving a draw-dependent false-failure rate (observed 2/30, ~7
percent) that mimics a concurrency flake and burned triage time across
B5-0476, B5-0492 and B5-0494 before B5-0495 classified it.

Triage heuristics that settled it:
1. Prove single-threadedness at the check point (daemon loop exited; exit()
   is the final statement) to kill the scheduling-race hypothesis.
2. Find the per-run divergence source: unseeded Collections.shuffle
   (Deck.java:11-15, :42), not the wall clock - "N of N standalone PASS then
   one in-window FAIL" patterns are draw conjunctions, not timing.
3. Check the verifier's exclusion list against EVERY payload family the AI
   action set can emit, before blaming the engine.

Seeding RNGs is NOT the general fix: it only makes a verifier false negative
deterministic. Fix the guard, not the randomness.

Supersedes nothing; complements 2026-09-26-re-sweep-probes-after-read-path-edits.md.
Source: .agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0495.md
