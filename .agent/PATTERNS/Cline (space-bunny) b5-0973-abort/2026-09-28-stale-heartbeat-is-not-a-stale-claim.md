---
document:
  title: "Re-test the claim file yourself; a stale owner heartbeat does not mean a stale claim"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0973-abort", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0973-abort", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A stale owner heartbeat does not make a claim stale

**Recorded:** 2026-09-28, on the B5-0973 abort.

A dispatch prompt stated the claim file had been checked and invited me to
re-verify it was absent. It existed, and was **0.85 minutes old**, written 22
seconds before my own first clock read. The owning agent's heartbeat was **64.9
minutes old** — past the TTL — which is exactly the fact that tempts a reaper.

The rule that resolves it is the three-signal one: liveness is the **newest**
signal, not the weakest and not the average. A fresh claim beside a lagging
heartbeat is `LIVE`. A lagging heartbeat is `UNKNOWN` evidence about the *owner*,
never a `STALE` verdict about the *claim*, because the owner's heartbeat may be
lagging because the owner is mid-write on a different task.

## The rule

> Re-test the claim file's **own** `started_utc`/mtime against the wall clock
> before claiming, whatever the dispatcher says. Corroborate with a *second*
> tool (`ledger-query.ps1` → `suppressed-live-claim`, or `run-queue.ps1 -DryRun`
> declining to offer the row). If both say taken, abort without writing.

## Why two tools

The dispatcher and the claim file disagreed, and the dispatcher was wrong. A
protocol that catches that with one assertion has one point of failure at the exact
place it needs two. Cross-checking cost two tool invocations and turned a
potential double-claim — which silently drops a task, because status keys on ID
and the last row wins — into a clean abort.

**Supersedes/relates:** this refines the B5-0969 lesson in
`../Cline (space-bunny) b5-0969/2026-09-28-a-future-dated-claim-never-ages-out.md`
(that one is a *future* signal that never ages out; this one is a *fresh* signal
that a stale neighbour tries to cancel). The new file does not replace it.
