---
document:
  title: "Working-tree inventory needs a live reproduction command"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-30"
---

# Working-tree inventory needs a live reproduction command

**Reusable lesson:** an inventory row that states a count without the exact command to reproduce it is a snapshot, not an inventory. B5-1009's row said "376 untracked entries measured 2026-09-29 via git status porcelain" — that is a number with a date, not a reproducible census. When the tree grows (483 now), a later reader cannot tell whether the growth is new agent activity, a stale measurement, or a counting error, because the row did not ship the command that produced 376.

**What to do instead:** every inventory or census row must include the exact command (full shell line, no ellipsis) that produced the numbers, run against the live tree at claim time, so a later reader can re-run it and get the same shape. The command must be live (run against the actual tree, not a cached output) because the tree is the authority, not the row.

**Why this matters here:** the 376→483 growth is real — 107 files were added by subsequent agent sessions (seeding wave, other agents' heartbeats/reports/patterns). Without the reproduction command, a reader could not distinguish "the tree grew" from "the original count was wrong." With it, the growth is verifiable and the row's value is the partition framework, not the snapshot count.

**Scope:** applies to any row that reports a count, census, or measurement of the working tree, untracked files, or any other live state. The command is part of the deliverable, not an appendix.

**Bad:** "measured 376 untracked entries via git status porcelain"
**Good:** "measured via `git status --porcelain | grep '^??' | wc -l` = 376 on 2026-09-29; re-run command at claim time = 483 on 2026-09-30; partition command: ..."

**Anti-pattern:** citing a count from another report without re-running the command. If B5-1009 had said "376 per the 2026-09-29 measurement" and cited a report, that would be copying, which AGENTS.md section 3 says confers no authority.
