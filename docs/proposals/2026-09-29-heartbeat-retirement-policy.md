---
document:
  title: "Heartbeat retirement policy (proposal)"
  status: "APPROVED 2026-09-29 by human ruling; recorded in docs/DECISIONS.md under B5-1065. HEARTBEATS README merge deferred while the live foreign B5-1062 claim covers that file; until merged nothing may be archived on this approval's say-so."
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
  assessor_llm:
    - {name: "opencode (muse-spark-1.3) approve-01", version: "muse-spark-1.3", passes: 1, last_pass: "2026-09-29", note: "B5-1065 approval close-out: status set to APPROVED on explicit user order, README merge deferred"}
  last_modified_by_llm: {name: "opencode (muse-spark-1.3) approve-01", version: "muse-spark-1.3"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1005"
---

# Heartbeat retirement policy (APPROVED 2026-09-29)

**Status: APPROVED 2026-09-29 by human ruling (explicit user order
`APPROVE docs/proposals/2026-09-29-heartbeat-retirement-policy.md`),
recorded in `docs/DECISIONS.md` under B5-1065.** The HEARTBEATS README
merge the R6 adoption path calls for is deferred while the live foreign
B5-1062 claim covers that file (one writer per scope); until merged,
nothing may be archived on this approval's say-so. Sections R1-R6 below
are the approved text, left intact as the record.

Written against the measured inventory in the B5-1005 close-out report
(`.agent/REPORTS/2026-09-29-Buffy (glm-5.3-flash) 4-B5-1005.md`): the live
store holds roughly 90 files of which **75 idle / 10 active / 3 busy / 1
out-of-enum tombstone**, so the directory is ~83% tombstones of sessions that
ended hours-to-days ago against a 30-minute TTL and a ~5-minute refresh
discipline.

## R1 — What retirement is

Retirement = **archival**, never deletion, never rewriting. A retired
heartbeat moves byte-identical from `.agent/HEARTBEATS/` into
`.agent/HEARTBEATS/_retired/` (same directory family as the existing
`_quarantine/` convention, sibling not child of the live store). The filename
is unchanged, the JSON inside is unchanged, and no `_registry.json` entry is
edited — the registry maps agent_id → filename forever, and resolution rules
(exact stem → registry → sanitisation) keep working because the file still
resolves inside the HEARTBEATS tree.

## R2 — When a heartbeat may be archived (all conditions, one at a time)

A file is **retirable** when ALL of the following hold:

1. **TTL-detached**: its mtime is older than 24 hours (48 TTLs — no liveness
   join can ever read it fresh again, so its removal cannot change any
   offer/reap verdict that a fresh read of the remaining signals would not
   already settle as STALE/UNKNOWN).
2. **Payload agrees**: its own `state` is `idle` **or** it is a closed-session
   tombstone whose `live_claims` is `[]` or whose claims have all since closed
   DONE. A `busy`/`active` payload is never archived on mtime alone — that is
   exactly the B5-0957/B5-0597 class of killing live work on a stale read.
3. **No unresolved standing finding names it**: it is not the subject of an
   OPEN row, a live claim, or an unadjudicated collision report.
4. **One move per row**: the archiving is performed under a claimed ledger
   row that records the file, its mtime age, its payload state, and the
   three-signal consequences of the move — the same note-is-part-of-the-work
   rule a reap follows.

A file is **never** retirable because its agent_id is old, its spelling is
inconvenient, or a human liked it better gone.

## R3 — Where it goes and what it means afterwards

`.agent/HEARTBEATS/_retired/`. Afterwards: the validator's live-store census
no longer counts it (the live directory is what the fleet's liveness reads),
and any reader that resolves an agent_id to a retired file must treat the
heartbeat signal as **UNKNOWN** — never LIVE, never STALE — exactly the
no-heartbeat-at-all answer the canonical HEARTBEATS README already defines.
R1–R6 (instance identity) are untouched: retired ids are not reusable, not
rewritten, and not merged.

## R4 — What a reader does when it finds no heartbeat

Unchanged and binding: **UNKNOWN, never LIVE, never STALE** (HEARTBEATS
README, Liveness). This proposal does not weaken that; retirement only makes
"no heartbeat" *honest* for sessions that verifiably ended, instead of
letting a days-old idle file masquerade as a signal.

## R5 — The out-of-enum tombstone (part 3 disposition)

`Cline (space-bunny) b5-0941.json` — state `released`, not in the
active/idle/busy enum — is a **foreign agent's file**: R5/R6 forbid one
instance from tidying another, so this policy's disposition is
classification only. Measured: the existing `_quarantine/` convention covers
backups (`me-so-poor.json.bak`), lookalike/colliding ids
(`solar-pro4:free.json`), and pre-schema payloads (`kiro-pi.timestamp`), but
**it does not cover** a well-formed-JSON, non-colliding, out-of-enum
tombstone — that case has no home yet. Recommended classification: leave it
byte-identical in place, log it as the standing exit-1 reason
(validate-heartbeats), and let R2 condition 3 (no standing finding names it)
prevent any mass retirement while it is the store's one documented red. A
future, separately-claimed row with human authorisation may move it under
this policy's R1 rules once a home for out-of-enum tombstones (extend
`_quarantine/` or open `_retired/`) is adopted.

## R6 — Adoption path

Proposal tier only (`docs/proposals/`, AGENTS.md §3): truth when merged into
the HEARTBEATS README by the normal claimed process, compiled gates green, and
the DECISIONS entry written. Until then nothing may be archived on this
proposal's say-so.
