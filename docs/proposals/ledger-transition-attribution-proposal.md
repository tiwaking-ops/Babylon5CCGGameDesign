---
author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
status: "Proposal"
---

# Ledger transition attribution (B5-1464)

## Problem

A `BLOCKED` → `OPEN` flip was executed on a live-claimed row (B5-1047, measured 2026-09-30T19:07:39Z) with both the owner cell and the note cell left bare — no claim-owner id, no human-admin note, no trace of who closed the defect or why. The flip was visible in the ledger but anonymous in the ledger: a reader could see *that* the status changed but not *who* changed it or under what authority. B5-1415 recorded this as the second same-day bare-cell flip and closed as pure observation with zero remediation because its own letter stood down while the foreign claim was live.

The B5-0807 half-close precedent shows the assessment-first discipline: classify every anomaly against the B5-0596 / B5-0435 content-protection rule before touching any byte, repair only unambiguous mechanical defects, leave every ambiguous row byte-identical, and never invent a cell boundary. That discipline protects content. It does not, by itself, prevent a future anonymous flip because the flip is a status-cell change, not a pipe anomaly, and B5-0807's scope was pipe-shape only.

## Requirement

**Every status-cell change in `.agent/TASK_LEDGER.md` must carry a writer tag**, where a writer tag is one of:

1. the claim-owner id (the `agent_id` field of the claim file that covered the change), or
2. a human-admin note (`[human-admin: <free text>]`) when the change was made outside a claim, e.g. an admin release, a manual correction, or a human ruling.

A status-cell change is any transition between `OPEN` / `CLAIMED` / `DONE` / `BLOCKED` / `VOID` / `SUPERSEDED`. The tag must appear in the note cell of the row at the time the status cell is written — not retroactively, not in a separate file, not in a heartbeat prose field (heartbeat `notes` is prose and is never parsed; HEARTBEATS/README.md §2 rule 2).

## Doc-gate check

A doc-gate check must fail any row whose status cell was flipped (status differs from the previously recorded status for that ID) while its note cell is bare — i.e. contains neither a claim-owner id nor a `[human-admin: ...]` note. "Bare" means the note cell, after trimming, matches none of:

- a claim-owner id (resolved via the claim file or the `_registry.json` if the id is not filename-expressible),
- a `[human-admin: ...]` bracketed note.

The check is a **gate**, not a recommendation: a bare note cell on a flipped row must block the row from being treated as authoritative until a tag is added. The gate may live in the same tool that runs the ledger census, or as a standalone check invoked before any ledger-write path; the proposal does not prescribe the mechanism, only the fail condition.

## Scope of the tag requirement

- **Claimed transitions** (OPEN → CLAIMED → DONE/BLOCKED): the note cell must carry the claim-owner id. The id is the `agent_id` field of the claim file, not the filename stem (the two can differ where `_registry.json` maps them; see HEARTBEATS/README.md §Identity).
- **Admin transitions** (any status change made without a claim file, e.g. human-authorized release, manual correction under a ruling): the note cell must carry `[human-admin: <why>]`. The `<why>` is free text but must be present — an empty bracket is not a tag.
- **Same-status rewrites** (e.g. correcting a note cell without changing status): do not trigger the flip check, but if the rewrite touches a row whose status was previously flipped bare, the rewrite must not erase an existing tag.
- **Stale-reap notes** in the ledger (the prose note appended when a stale claim is reaped): these are ledger annotations, not status-cell changes, and are not subject to the tag requirement. They are, however, records of a liveness verdict and should carry the reaping agent's id as a matter of practice; this proposal does not require it but notes it as the obvious next step.

## What this proposal does NOT require

- It does not require a new ledger column. The tag goes in the existing note cell.
- It does not require tool edits today. The proposal is the artifact; a follow-up task (or the same task's second phase, if the proposer chooses to scope it) would implement the gate.
- It does not re require re-writing history. Existing bare flips stay as observed; the gate applies to future flips and to any re-verification that treats a bare-flipped row as authoritative.
- It does not change the three-signal liveness rule, the claim TTL, or the reap semantics. B5-1464's row letter says so explicitly.

## Relationship to B5-1415 and B5-0807

- **B5-1415** is the trigger: it observed the second same-day bare flip (B5-1047, BLOCKED → OPEN at 19:07:39Z) and closed as pure observation because the foreign claim was live. This proposal is the remediation that B5-1415's letter deferred — a proposal, not a repair, because the repair is a governance change, not a byte edit.
- **B5-0807** is the method model: assess-first, classify against content-protection rules, repair only unambiguous mechanical defects, leave ambiguous rows byte-identical, never invent a cell boundary, run the duplicate-ID census and ledger-query proof after any write, re-census suppressed rows after claim release. This proposal adopts that discipline for the governance layer: classify the bare-flip class as a governance defect (not a pipe defect), require a tag as the unambiguous fix, and leave rows whose note cell is ambiguous (e.g. a partial tag) byte-identical until the ambiguity is resolved.

## Rejected alternatives considered

1. **Audit log separate from the ledger.** Rejected: a separate log that is not read by the gate is a second record that can drift from the ledger. The tag must live in the row so the gate can read it without a join.
2. **Post-hoc tag after a flip is detected.** Rejected: detection is after the fact; the gate must fail the row before it is treated as authoritative, which means the tag must be present at flip time or the row is blocked regardless of later attribution.
3. **Trust the claim file as the source of truth for who flipped.** Rejected: the claim file is advisory against an out-of-band writer (B5-0344 coordination collision). A claim file can be absent, stale, or written by a session that cannot be distinguished from an unidentified writer. The tag in the note cell is the durable record; the claim file is the transient lock.

## Acceptance criteria

1. A row whose status was flipped by a claimed agent carries the claim-owner id in its note cell.
2. A row whose status was flipped by an admin carries `[human-admin: <why>]` in its note cell.
3. A doc-gate check (however implemented) fails any flipped row whose note cell is bare.
4. The gate does not fail rows whose status was not flipped (i.e. it is not a blanket note-cell requirement; it is a flip-attribution requirement).
5. The proposal itself is filed as a Markdown record under `docs/proposals/` with `author_llm` frontmatter, and no ledger row was touched in its writing.
