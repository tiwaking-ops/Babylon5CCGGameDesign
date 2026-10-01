---
document:
  title: "BLOCKED prerequisite tokens and the ACTION-READY surface (proposal, B5-1018)"
  status: "Proposal (not truth until merged; 00_BOOT and AGENTS.md win on conflict)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1018"
---

# Proposal: BLOCKED prerequisite tokens → the ACTION-READY surface

Deliverable of B5-1018 (the one mechanism that row permitted). Full evidence:
`.agent/REPORTS/2026-09-29-Freebuff (Buffy glm-5.3-flash) 1-B5-1018.md`.

## The measured problem

17 BLOCKED rows (2026-09-29). Classified at block time: GATE-SHUT 4,
PREREQ-RED 9, SCOPE-BLOCKED 4, **ACTION-READY 0**. Re-measured two days
later: **ACTION-READY 7 (41%)** — every stated blocker of those 7 is gone
while the letter still says BLOCKED. The class is invisible when created and
dominant before anyone looks; a calendar expiry neither finds it (age matched
0 of the 7) nor avoids waking correctly-blocked rows (B5-1001, 0.2d, human
gate).

## The mechanism

1. **Convention:** every future BLOCKED verdict begins
   `BLOCKED on B5-xxxx[,B5-yyyy…]; class GATE-SHUT|PREREQ-RED|SCOPE-BLOCKED|HUMAN; <prose>`
   — the cells already name their prerequisites in prose; this makes them
   parseable. `HUMAN` covers "blocked on a human decision" (B5-1001).
2. **Surface:** the close-out census (or the runner offer path) re-reads the
   named rows of every BLOCKED row; a BLOCKED row whose every named
   prerequisite now reads DONE (or whose blocker is verifiably gone) is
   reported as **ACTION-READY**. Report line first; offer-lane change only if
   a human wants the queue pushed.
3. **Discipline preserved:** no status cell changes hands automatically. An
   ACTION-READY surface is an *offer*; the row is claimed and re-verified
   through the normal step-6 cycle before anything moves.

## Cost

- One migration sweep over the 17 existing rows (verdicts already name their
  prereqs in prose). One genuine casualty: **B5-0811 cannot be migrated
  mechanically** — no date, anomalous cells — so the migration row must
  hand-repair that row first (UNKNOWN never STALE applies until then).
- One sentence in 00_BOOT, one census addition. No schema change to any other
  tool.

## Rejected alternative (recorded, not silently dropped)

An N-day expiry: cheaper to build, costlier forever — resurrects by calendar,
which matched none of the ready rows and would eventually wake correctly
blocked ones.

## Adoption path

Human ruling or a separately claimed row; this document changes nothing by
existing. Related: the B5-0743 backup-pointer proposal (same tier, also
awaiting a human).
