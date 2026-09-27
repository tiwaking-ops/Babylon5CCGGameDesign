---
document:
  title: "B5-0626 — the unattended path now boots from a document that is true"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0626 — closing the unattended-run gap

Human approvals: repoint the runner template at `AGENT_LOOP.md`; mark `HANDOFF.md`
superseded. Then commit and push.

## The measurement that motivated both

B5-0625 filed `.agent/AGENT_LOOP.md`. Nothing could reach it.

```
$ grep -r AGENT_LOOP .agent docs AGENTS.md
  7 references — every one a document written ABOUT the file
  0 references from any executable
```

And there is exactly one automated path that exists. At `run-queue.ps1:218`, every
agent it invoked was told:

> Read .agent/HANDOFF.md and complete task ... only

`HANDOFF.md` is not merely out of date. It is **misleading in a way that produces a
specific wrong action**:

| its text | reality |
|---|---|
| §7 "`b5ccg/src/` with `-source 6` is **RED** (expected)" | green since 2026-09-21 |
| §8 "Fix this **FIRST** as task B5-0001" | B5-0001 DONE since 2026-09-21 |
| §9 "Claim B5-0001 (or the highest `OPEN` task…)" | would create a claim on a closed task |

That last row is the one that matters. An unattended agent obeying §9 literally would
have created a claim file against a `DONE` row — **the exact orphan class B5-0622 spent
a task closing the protocol against.** The automated path was about to walk straight
into the bug the previous task had just fixed, by following a document nobody had
re-read since day one.

## Part 1 — the template, and four rules it was missing

Repointed at `AGENT_LOOP.md`, keeping `00_BOOT.md` as the boot step. While rewriting
it I found the template was also missing four rules that only the new procedure
carries, so folding them in was not optional tidying:

- the **sanitised** agent-id in the report filename (B5-0623's rule);
- the reusable-lesson line filed as a **new file** under `.agent/PATTERNS/<agent-id>/`,
  supersede-never-rewrite;
- the **binding heartbeat schema** in `HEARTBEATS/README.md`;
- the **re-read-the-row-still-OPEN** check beside the claim-file check (B5-0622).

The template also carried its own stale pointer — `mark BLOCKED per step 7`, when
verify is **step 8** of the current eleven-step boot file. That is the *second*
instance of exactly this defect in two tasks: B5-0625 found the same stale step
pointer in the human-supplied prompt (`step 10`, really 11). A pointer next to a
citation is not checked against it, however carefully the surrounding prose was
written.

And I added one instruction that costs a line: **do not read `HANDOFF.md`**. An agent
that remembers the old pointer will look for it, and being told where *not* to look is
cheaper than hoping it does not.

## Part 2 — superseded, with the two lies named

`HANDOFF.md` keeps its history and loses its authority:

- frontmatter `status: "Superseded (retained for history; not authority)"`;
- a machine-readable `superseded_by:` block naming boot, loop and reason;
- a banner above the body that **names both false statements specifically**;
- sections 1–6 left intact and marked broadly accurate.

The banner names the lies rather than saying "this document is old" on purpose. A
reader who knows *precisely which two claims are false* can act on that. A reader told
merely that a document is stale tends to skim, find §8 still confident and
well-formatted, and follow it — which is exactly the failure the banner exists to
prevent.

## Verification, and one caveat I am not dressing up

Both changes were verified by **running** the tool, not by reading the diff:

```
run-queue.ps1 -DryRun   exit 0, no crash, no duplicate warning, same head task (B5-0621)
template no longer contains "Read .agent/HANDOFF.md"        True
template contains "Do NOT read .agent/HANDOFF.md"           True
__TASK_ID__ occurrences: 4, all substituted by String.Replace (all-occurrences)
```

**The caveat:** `ConvertFrom-Yaml` **does not exist** in Windows PowerShell 5.1, so
`HANDOFF.md`'s frontmatter was *not* machine-parsed. I verified it structurally —
delimiters at lines 1 and 16, and every interior line classified — which flagged five
lines that are in fact valid YAML (key-only lines opening nested blocks, plus one
flow-mapping list item in the same style `00_BOOT.md` already uses). That is a weaker
guarantee than a real parse and is recorded as such rather than reported as "parses".

Left untouched: `HANDOFF.md` was not deleted, the runner's stop conditions / TTL /
lane and prereq tables / duplicate-ID assertion / B5-0624 suffix fix are unchanged, and
so are Buffy (glm-5.3-flash)'s live `B5-0620` claim, their untracked B5-0612 report,
and the `SCOPE_RELEASES` logs.

**Reusable lesson:** a procedure that nothing launches is documentation, not
instruction. And when you discover which document your automation actually feeds to
agents, read it before assuming it is current — the automated path is the one path
nobody re-reads, because it is the one path nobody watches.
