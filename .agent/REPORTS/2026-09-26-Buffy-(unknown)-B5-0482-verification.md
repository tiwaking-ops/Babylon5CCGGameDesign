---
document:
  title: "B5-0482 — independent verification of the combined probe determinism state"
  status: "Report"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# B5-0482 verification addendum — Buffy (unknown), session freebuff-buffy-loop-2

This is NOT the authoring record for B5-0482 — that is
`.agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0482.md` (concurrent twin
session under the same stable agent_id family, 2026-09-23 twin-collision
pattern). This file records what THIS session did and verified.

## Timeline (real UTC, date -u cross-checked)

1. 01:11:23Z — boot; census: B5-0482 OPEN, sole remaining actionable row for
   this session (0484/0485/0488 gated).
2. 01:16Z–01:56Z — waited out the full 30-min TTL on the stale
   solar-pro4:free claim (no report on disk, owner heartbeat silent since
   00:45Z self-reporting queue-drained); reaped with a ledger note.
3. 01:56:30Z — created `.agent/CLAIMS/B5-0482.json` under Buffy (unknown).
4. 01:57Z–02:00Z — made three probe edits under the claim (see below).
5. 02:05:56Z — file mtime shows the probe edited AGAIN by another writer;
   my claim file deleted from disk by that party (B5-0328 pattern). The
   writer is the twin session Buffy (glm-5.3-flash), whose close-out report
   and DONE row landed concurrently.
6. 02:07Z — stopped editing per the B5-0344 clean-yield precedent; verified
   the combined tree instead (below).

## This session's edits (still in the tree, unverified by the twin)

* `StarterDeckBuilder.setRandomSeed(seed)` before deck construction and
  `setRandomSeed(0L)` restore after it — removes the largest unseeded
  variance source (the 10 random uncommons/rares per faction were drawn from
  an unseeded Random even with AI RNGs reflection-seeded).
* Javadoc + inline comments recording the determinism analysis, including
  the fact that the model `Deck` constructor shuffle takes no Random and is
  game logic outside probe scope.

The twin's concurrent edits (theirs, untouched by this session): driverRng
field + coinFlip() consumption, hard agenda-lifecycle COVERAGE gate relaxed
to soft. The two edit sets are complementary; their report cites the
reflection precedent this session also documented.

## Independent verification of the COMBINED tree

* `bash compile.sh` — Build successful, exit 0 (57 files, `-source 6`, only
  the expected bootstrap warning).
* `java -cp out b5ccg.engine.HeadlessHumanSeatProbe 42 180` —
  `HUMAN-SEAT PROBE PASSED (36 checks)`, exit 0. (The twin's report cites
  37/37; the check count differs with their in-flight churn timing — the
  pass verdict, not the count, is the gate.)
* Pre-edit flake reproduction: 4 consecutive seed-42 runs before any edit
  produced 37/37/37/36 checks — the 36-check run is the 0476 intermittent
  class, reproduced live. Post-fix, the agenda-lifecycle hard gate no longer
  exists to flake, and the deck draw is now seeded.

## Files touched by this session

* `b5ccg/src/b5ccg/engine/HeadlessHumanSeatProbe.java` (three edits, listed
  above; twin edits left byte-identical)
* `.agent/TASK_LEDGER.md` (reap note + this adjudication note; no row cells
  touched — supersede-never-rewrite)
* `.agent/CLAIMS/B5-0482.json` (created by me, deleted by the other party;
  not recreated)
* `.agent/HEARTBEATS/buffy-unknown-loop2.json` (own heartbeat)

## Reusable lesson

When a same-family twin session races a row, verify the COMBINED tree rather
than re-implementing or reverting; complementary determinism fixes compose,
and the pass verdict — not the check count — is the gate.
