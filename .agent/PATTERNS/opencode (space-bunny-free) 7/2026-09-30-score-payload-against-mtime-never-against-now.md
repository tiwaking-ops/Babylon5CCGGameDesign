---
document:
  title: "Score payload-against-mtime, never payload-against-now"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 7", version: "space-bunny-free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Score payload-against-mtime, never payload-against-now

Traces to: `docs/reports/clock-integrity-in-agent-coordination-files-2026-09-30.md` §3.4 and
§4.2, measured 2026-09-30T18:17Z over 125 claim and heartbeat files.

Two measurement rules that each cost a wrong answer here, and both are cheap to get right.

**One: a stale agent is not a skewed agent.** `payload - real_now` measures *freshness*; every
heartbeat in a store is hours behind real time because those sessions stopped, which is the
system working correctly. Integrity is `payload - file_mtime`, because mtime is written by the
filesystem for the same write and cannot be back-dated by the process doing the writing. In
this store the two readings ranked the fleet *differently*: three files look bad on
freshness, and only one file is actually broken — `solar-pro4` at +741.8 min against its own
inode, while `solar-pro4:free` sits at -0.7 min on the same client.

**Two: a shared mtime is a bulk touch, not N writes.** 87 of 125 files here carried the
mtime `2026-09-29 08:44` with no git commit at that hour. Scored naively, they produced ~90
false "disagreements" with gaps to -11,422 min. Detect shared-mtime clusters and exclude them
*before* scoring. This also bounds the obvious fix: mtime is unforgeable by an agent, but it
is not a per-write record if any tool rewrites the tree, so "just use mtime" is unsafe
without this exclusion.

**Reusable lesson:** before reporting an agent as defective, ask which channel is measuring
what — a large gap against the wall clock is usually an idle session, and a large gap against
the file's own mtime is a real disagreement, but only once you have subtracted the files a
bulk operation moved. A defect report that cannot say which of the two it measured is not a
finding, and neither is a guard that fires on ninety-seven idle agents and one broken clock
in the same breath.
