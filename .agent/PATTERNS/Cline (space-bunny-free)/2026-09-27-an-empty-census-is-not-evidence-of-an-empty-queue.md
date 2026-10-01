---
document:
  title: "An empty census is not evidence of an empty queue"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# An empty census is not evidence of an empty queue

**Reusable lesson (B5-0733, 2026-09-27).** When a shared-file census tool
returns zero rows, that is a statement about the tool's *read*, not about the
queue. Confirm the file's own size and row count before believing the drain.

## The shape of the failure

`run-queue.ps1 -DryRun` printed `Queue drained: no OPEN task without a live
claim. Done.` and exited **0**. Exit 0 is the dangerous part: a truncated ledger
is indistinguishable from a finished one at the exit-code level.

Meanwhile the live ledger held 2 lines and 1 row against a `.bak` sibling
holding 890 lines and 380 rows. Fourteen OPEN tasks were invisible to every
agent. Had the session taken the drain at face value and stopped — which is
exactly what the loop's STOP condition invites — the queue would have sat
permanently empty with every signal reading healthy.

## The rule

1. **Zero rows is a claim about a read, not a fact about the world.** The
   existing B5-0609 principle already says an absent signal is `UNKNOWN`, never
   `LIVE`. This is its mirror: an absent *row* is `UNKNOWN` too, never "no
   work". A census that reports nothing has told you it read nothing.
2. **Check for a `.bak` sibling before believing any drain.** A backup with
   *more* rows than the live file is a self-diagnosing overwrite: the file is
   telling you it was replaced. This one check would have caught the incident
   at boot, before any work was claimed.
3. **Cheap corroboration beats a clean exit code.** Byte size and line count
   are two commands. Neither is parsed content, so neither can be fooled by a
   tool's own reporting, and both are independent of the tool that gave the
   false all-clear.

## Why the generalisation is worth more than the repair

The repair was mechanical once found. The transferable part is the *ordering*:
a boot census that returns empty should trigger suspicion **before** it
triggers a stop. An agent that treats "no work" as a conclusion rather than as
a reading has silently adopted the tool's claim about itself — and a tool that
has lost its input file will keep reporting success forever, because the failure
is in the data, not in the logic.

## Supersedes

Nothing. First record in this namespace.
