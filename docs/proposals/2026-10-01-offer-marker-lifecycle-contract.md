---
document:
  title: "Run-queue offer-marker lifecycle contract"
  status: "Proposal (advisory; not implemented)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Run-queue offer-marker lifecycle contract

## Status and boundary

This is a proposal only. It does not change `.agent/run-queue.ps1`, claims,
heartbeats, or any other coordination file. An offer marker is a short-lived
scheduler reservation: it says that one runner selected a candidate. It never
claims the task, changes ledger status, or replaces the recipient's mandatory
OPEN-row and claim-absence checks followed by atomic claim-file creation.

## Exact marker paths

The runner derives a ledger key by replacing every non-ASCII-letter-or-digit
character in the ledger path with `_`, then uses:

```
<OS temp directory>\run-queue-offers-<ledger-key>\<task-id>.offer
```

For the current checkout, B5-1501 measured this concrete marker directory:

```
%TEMP%\run-queue-offers-C__temp_projects_Babylon5CCGGameDesign__agent_TASK_LEDGER_md\
```

So the per-task example is:

```
%TEMP%\run-queue-offers-C__temp_projects_Babylon5CCGGameDesign__agent_TASK_LEDGER_md\B5-1521.offer
```

A short-lived per-task arbitration lock can use the sibling path
`B5-1521.offer.gate`; its file may persist, but only an open exclusive handle
means the gate is held. This temp namespace is scheduling state, not
`.agent/CLAIMS/` and not a durable task record.

## Proposed lifecycle

1. **Acquire only when selecting an offer.** After the shared census has built
   the eligible OPEN set and `Select-OfferRow` reaches a candidate, acquire that
   task's `.offer.gate` with an exclusive OS file handle. Under the gate,
   re-check the marker and acquire an absent `.offer` with atomic
   create-if-absent semantics (`FileMode.CreateNew`, or an equivalent that
   cannot truncate an existing marker). Never infer claim ownership from this
   reservation. Release the gate handle immediately after the marker is safely
   written.
2. **Record enough identity to distinguish a reused PID.** The marker payload
   should include a schema/version, host identity, runner PID, process start
   time, a fresh run token, and creation time. A numeric PID by itself is not
   proof that the original runner still owns the marker: B5-1501 saw repeated
   PIDs and could not distinguish concurrent lanes from PID reuse.
3. **Keep the reservation only through the offered work.** In normal mode,
   retain the marker while the selected agent invocation is running. In a
   `finally` path after that invocation returns (success, failure, or handled
   exception), reacquire the `.offer.gate`, re-read the `.offer`, and remove it
   only if its run token still matches this runner. A failed cleanup is safe to
   leave for stale recovery; it must not authorize deleting a different run's
   marker. In-run de-duplication remains separate and continues to prevent the
   same runner offering the same row twice.
4. **Make ordinary DryRun observationally pure.** The current `-DryRun` passes
   through `Select-OfferRow` and writes the same shared `.offer` files despite
   not invoking an agent. Default DryRun should preview candidates without
   acquiring persistent markers. If shared scheduling simulation is needed, it
   should be an explicit mode whose reservations are always cleaned up in a
   `finally` block. A preview must say which mode it used.
5. **Recover only under the same gate.** When a marker exists, acquire its
   `.offer.gate` and re-read the marker before deciding. If host, PID, and
   process-start identity still match a running owner, leave it alone regardless
   of age. If the process is absent or the PID now belongs to a different
   process start, the marker is stale and may be removed under the gate. For an
   unreadable or legacy PID-only marker, retain the current conservative
   two-minute age bound, measured from marker mtime, then re-check under the
   gate before removal. After removal, create the replacement marker with
   create-if-absent semantics before releasing the gate. The gate serializes
   stale readers and prevents a stale check from deleting a replacement that
   another runner just wrote.
6. **Do not fail open silently.** An expected create-if-absent collision means
   this candidate is reserved elsewhere; continue to the next candidate. An
   unexpected temp-directory, gate, or marker I/O failure should be surfaced
   and must not be represented as a successful reservation. The atomic claim
   file remains the final ownership boundary, but it should not be used to hide
   scheduler-arbitration failures.

## Current behavior and gap

The inspected `Test-OfferMarkerAvailable` checks
`%TEMP%\run-queue-offers-<ledger-key>\<task-id>.offer`, stores only `$PID`,
blocks while that numeric PID appears alive, removes a marker for a dead PID,
and uses a two-minute mtime bound when PID parsing yields no positive value.
The runner has no completion-time marker cleanup. A long-lived or PID-reused
process can therefore keep a row suppressed; dry runs also leave markers for
their runner PID. The selector currently performs a separate existence check
and then calls `System.IO.File.Create(..., FileShare.None)`. The proposal
requires true no-overwrite atomic creation because a check followed by a create
that can truncate an existing path is not itself an ownership protocol. These
are contract requirements for a future implementation, not edits made here.

B5-1501 found 147 historical `.offer` files and four offers in a
`-DryRun -MaxIterations 20` observation before marker rejection. This pass's
boot census also used DryRun and printed ten offers; per the inspected code,
those selections use the shared marker path even though no agent was invoked.
No marker was manually removed or changed in this pass.

## Verification criteria for a future implementation

- Two concurrent selectors for one task yield at most one marker owner; the
  losing selector continues to another eligible task.
- A completed invocation releases only its own run-token marker, including on
  a handled failure; a foreign replacement is preserved.
- A dead owner and a reused PID are distinguished by process start identity;
  malformed legacy markers recover only after the documented age bound.
- Concurrent stale recovery cannot remove a newly created marker because all
  re-check/remove/create transitions use the same exclusive gate.
- Default DryRun leaves the marker directory unchanged; any explicit shared
  simulation removes its own reservations.
- Marker I/O failures are visible, and a task is still claimable only through
  the independent claim-file protocol.

**Reusable lesson:** a scheduler reservation needs an explicit acquisition,
release, and stale-recovery protocol; a PID and a marker file alone do not prove
who owns the reservation or when it is safe to clear it.
