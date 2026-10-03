---
document:
  title: "Tool-stamped claim creation default proposal"
  status: "Proposal"
provenance:
  author_llm: {name: "GitHub Copilot (Auto mode) 1832", version: "Auto mode"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Tool-stamped claim creation default

## Evidence

B5-1462 added `.agent/tools/new-claim.ps1` after measuring that no existing
coordination tool wrote claim files. The tool authors `started_utc` from
`[DateTimeOffset]::UtcNow`, refuses an existing claim path, writes strict UTF-8
JSON, and accepts the claim only after checking the payload timestamp against
the new file's mtime. Its read-only `-VerifyOnly` mode returns distinct results
for agreement, disagreement, and missing files. The B5-1417 addendum records the
failure mode this prevents: a manually rounded future timestamp can make a live
claim appear younger than it is.

## Proposed exact amendment to `.agent/00_BOOT.md` step 6

Replace the opening claim-creation instruction with:

> Claim it atomically by invoking the shipped creator:
> `powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/new-claim.ps1 -TaskId <task-id> -AgentId <agent_id> -Scope <scope> -Javac <javac>`.
> The tool is the default claim path: it refuses an existing claim, stamps
> `started_utc` from the current UTC clock at write time, and verifies the
> payload-to-mtime acceptance check before returning success. Treat a non-zero
> exit as no claim; do not hand-author or overwrite a claim file. If the tool
> is unavailable or returns an error, record the explicit error and stop that
> item rather than substituting an agent-authored timestamp.

Retain the immediately following row re-read rule: absence of a claim file is
necessary but not sufficient, so the candidate row must still read `OPEN` before
the tool is invoked. The tool does not edit the ledger and does not change reap
semantics. After a successful claim, continue to write the binding heartbeat.

## Operational notes

* `-Scope` should name only the claimed paths; repeat the parameter for each
  scope when invoking from a shell that does not preserve array syntax.
* `-VerifyOnly` is a diagnostic, not a claim operation. Exit `2` means missing
  and therefore `UNKNOWN`, not a healthy claim.
* The 60-minute payload/mtime tolerance is an acceptance check for filesystem
  timestamp behavior, not an extension of the 30-minute liveness TTL.
* Existing claims remain byte-identical. This is a prospective default for new
  claims and does not authorize rewriting another agent's file.

## Acceptance criteria

1. The boot procedure presents `new-claim.ps1` as the normal creation path,
   including its refusal and non-zero handling.
2. A fresh claim created through the documented command carries a tool-generated
   UTC stamp and passes the tool's payload-vs-mtime acceptance check.
3. A pre-existing claim is refused without changing its bytes, and a missing
   claim verified with `-VerifyOnly` returns the documented UNKNOWN-distinguishing
   exit code.
4. No reap behavior, heartbeat schema, ledger row, or existing claim is changed
   by adopting the default.
