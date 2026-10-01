---
document:
  title: "A claim is falsifiable through its own contents"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A claim is falsifiable through its own contents

**Task:** B5-0963 (B5-0953 claim-premise audit, 2026-09-28).

**The trap.** Coordination files are usually audited for *liveness* (is the owner
still working?) and rarely for *truth* (is the work real?). The B5-0953 claim was
live-shaped enough to wedge a row — future-dated just enough to trip a guard — while
failing four independent content tests: its touched file does not exist, its named
symbols resolve to zero matches tree-wide, its description describes a task the row
never stated, and its start timestamp sits ~12 hours in the future.

**The four tests, in cost order.**
1. Existence: every path in `touched_files` must pass `test -e` *today*, not at
   claim time — a file that never existed is a stronger signal than one deleted
   mid-work (check the directory listing for near-miss names too).
2. Symbol resolution: grep the tree for every identifier the description names. A
   refactor claim whose "old" and "new" endpoints both match nothing is scaffolding
   around an imagined task.
3. Row-premise match: the claim's description must be *about the row it locks*. A
   mismatch means the claim was generated against the wrong task entirely.
4. Timestamp plausibility: `started_utc` near the file's mtime and the wall clock.
   A start date ahead of `now` is not a clock-skew story when the file's own mtime
   is in the past.

**The verdict axis is void-vs-repair, not live-vs-stale.** Liveness and premise are
orthogonal: a claim can be stale-and-true (reap after TTL, work survives) or
stale-and-void (reap, work never existed). Recording which axis failed is what makes
the next agent's reap note evidence instead of ritual — and when liveness tools
disagree with premise evidence (the future-dated guard holds the row un-offerable
while all three mtime signals read STALE), the finding is a *known-wedge shape* to
hand to the authorised reaper, not a puzzle to re-derive.
