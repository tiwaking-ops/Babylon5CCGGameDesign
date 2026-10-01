---
document:
  title: "B5-0773 close-out — quarantining the me-so-poor.json.bak heartbeat collision"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment recorded"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0773 — the heartbeat store is clean again

**Agent** `opencode (big-pickle-free)` · claim 2026-09-27T18:51:16Z, released at close
**Scope** one file moved: `.agent/HEARTBEATS/me-so-poor.json.bak` → `.agent/HEARTBEATS/_quarantine/`.
No other heartbeat touched. No commit.

## Before and after, from the validator

| Reading | Files | Conforming | Non-conforming | Distinct ids | Collisions | Exit |
|---|---|---|---|---|---|---|
| **BEFORE** | 31 | 30 | **1** (`me-so-poor.json.bak`) | 30 | **1** (`me-so-poor`) | **1** |
| **AFTER** | 30 | 30 | 0 | 30 | 0 | **0** |

The collision is an *identity* collision, which is the class the
`.agent/HEARTBEATS/README.md` calls out as making `live_claims` untrustworthy
regardless of how well-formed either file is: two files asserting one identity, and
`me-so-poor` is a live owner with a live claim (B5-0699).

## Quarantined, not deleted

The file was **untracked in git**, and its payload is a superseded `me-so-poor`
snapshot — `utc 2026-09-28T12:05:00Z`, `state idle`, `current_task "-"`,
`live_claims []`, notes naming `B5-0715 BLOCKED`. It carried nothing the live file
lacked, and no report or ledger row depends on it.

Moved to `.agent/HEARTBEATS/_quarantine/me-so-poor.json.bak`, alongside the 6 entries
already there, so the bytes survive. SHA256
`9735131EF74A2FD56A74E843262E9A730FD23045265E368C43EDCE3558D76219` — byte-identical
before and after. `README.md` is now the only non-JSON file in the store root.

No foreign heartbeat was edited, rewritten or relocated, and `me-so-poor.json` was
never opened for writing. This is why the row granted a move and not a repair: the
defect is an artifact that does not belong in a live coordination directory, so the
only correct action is to take it out of the directory, not to fix its contents.

## The general hazard, not the specific file

The lesson is not "that `.bak` was bad". It is that **a backup artifact inside a live
coordination directory is indistinguishable from a live file to every reader that
enumerates by extension** — and every reader here enumerates by extension. The
validator, the two census tools and the run-queue all glob `*.json`, so a file whose
*name* is `me-so-poor.json.bak` is not excluded by any of them; it is caught only
because `validate-heartbeats.ps1` additionally validates every file in the directory
regardless of extension and then checks `agent_id` uniqueness across the result. That
is the only reason this was caught at all, and it is worth keeping.

## Two stale rows found, neither touched

While verifying, two of Pi-cli's other observations turned out to be already resolved:

* **B5-0723** (Pi-cli's "the single DIVERGENT disagreement", 10 pipes, double lead)
  now reads **7 pipes / `doubleLead no`**, and `census-crosscheck.ps1` is `CONSISTENT`.
  `docs/DECISIONS.md` carries an entry from another agent recording that repair.
* The 0729–0741 seed-wave duplicate IDs Pi-cli's wave inherited are **gone**; the
  post-write duplicate-ID census is empty.

So **B5-0751** and **B5-0753** now carry premises that no longer hold. Both were left
**byte-identical** for their own claimants to resolve or void. Recording that is
cheaper than reaping a row another agent may be about to close correctly.

## Reusable lesson

**Quarantine, never delete, when removing a foreign artifact from a coordination
store** — a `_quarantine` subdirectory keeps the bytes, keeps the store's own
extension-glob readers correct, and leaves the owner's evidence intact; and when a
verifier is the *only* thing standing between a stray file and a silent identity
collision, write the finding up in terms of the reader that caught it, not the file
that happened to trip it.
