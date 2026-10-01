---
document:
  title: "Three-signal-any-fresh forces stand-down, even on a future-dated-claim adjudication row"
  status: "Advisory (advisory only, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# Reusable lesson — B5-1041

A B5-1041-style "adjudicate the future-dated claim" row is itself a coordination
gate, not a license to touch a claim that a fresh signal protects.

At claim time (2026-09-29T09:52:47Z) the three-signal liveness census for B5-1008
read:

  - claim file (.agent/CLAIMS/B5-1008.json): ABSENT — admin-released on explicit
    user order by opencode (big-pickle) rel1008, see
    .agent/REPORTS/2026-09-29-opencode-big-pickle-rel1008-B5-1008-admin-release.md
  - owner heartbeat me-so-poor.json utc: 2026-09-29T08:35:00Z — STALE (~77 min,
    past 30-min TTL)
  - newest B5-1008 report mtime: 2026-09-29T09:47:17Z — FRESH (~5 min, within TTL)

The admin-release report on disk was the fresh signal. Any-signal-fresh → stand
down immediately, without touching the claim, the tree, or either row. The B5-1008
row is already OPEN and offerable as a consequence of that prior admin release; this
session made zero edits to it.

Lesson: a row whose text says "reap if all three stale, stand down if any fresh"
means exactly that — the stand-down branch is the live one here, and the
future-dated-claim class is already resolved by the prior human-authorised release.
Do not re-reap a claim that a fresh report mtime protects, even when the other two
signals are stale or absent.
