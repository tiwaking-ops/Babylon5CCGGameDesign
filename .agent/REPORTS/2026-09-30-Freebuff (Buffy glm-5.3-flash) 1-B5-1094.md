---
document:
  title: "B5-1094 — heartbeat validation residue triage: 9 breaches, 3 collisions, zero repairs applied"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:21:53Z"
  instrument: "validate-heartbeats.ps1 (UTF-8 explicit), fresh runs at 06:22Z; cross-referenced against the 03:57:52Z full-table run"
---

# B5-1094 — the residue, itemised; nothing repaired

## Standing state (measured, not inherited)

`files: 112, conforming: 103, non-conforming: 9, distinct agent_id: 104,
collisions: 3`. Cross-referenced against this session's 03:57:52Z run
(103 files / 94 conforming / 9 / 3): **nine new heartbeat files appeared in
~2.5 h and every one conforms; the non-conforming set and the collision set
are byte-for-byte invariant.** The red is a frozen residue, not a live
defect generator.

## The 9 breaches, each with its smallest NON-APPLIED repair

| # | file | exact breach | smallest repair (NOT applied) | who |
|---|---|---|---|---|
| 1 | `Cline (space-bunny) b5-0941.json` | state `released` out of enum | none by agents — R5/R6 protected tombstone; human either ratifies `released` into the enum or retires it under the policy | human |
| 2–4 | `freebuff-01/02/03.json` | agent_id `Buffy (deepseek-v4-flash)` matches no stem (unregistered twin files) | batch-retire the three via the heartbeat-retirement policy (registered live file: `Buffy (deepseek-v4-flash).json`) | owner/human |
| 5 | `kiro-pi.timestamp` | parse failure (bare `2026-09-24T07…` text) + non-`.json` extension | move out of HEARTBEATS — it is a timestamp fragment, not a heartbeat | owner |
| 6 | `me-so-poor.json.bak` | unregistered stem + non-`.json` extension | move out of HEARTBEATS (it is a `.bak` of live `me-so-poor.json`, and its mtime is *newer* than the live file's — see collisions) | owner |
| 7 | `solar-pro4` (no extension) | EMPTY (zero content) + extension | owner deletes it — an empty file has nothing to archive | owner |
| 8 | `solar-pro4.json` | agent_id `solar-pro4:free` matches no stem (registered file is `solar-pro4-free.json`) | retire via policy; the registered file is authoritative | owner/human |
| 9 | `solar-pro4?free.json` | pre-schema; out-of-enum state `BLOCKED`; unregistered stem; **U+F03A lookalike filename** (R7) | retire via policy move — a rename is exactly what B5-1066's declined R5 exception forbids | human |

## The 3 collisions (members + newest mtimes from the full-table run)

1. **`Buffy (deepseek-v4-flash)`**: registered file (mtime 03:57 batch, active
   earlier) vs `freebuff-01/02/03.json` (same batch) — resolved by the
   #2–4 retirements.
2. **`me-so-poor`**: `me-so-poor.json` (mtime older, 1068 batch) vs
   `me-so-poor.json.bak` (**newer**, 1153 batch) — the *backup* is the
   younger file, which means the live heartbeat has gone stale-stale since
   the backup was taken; retirement of the `.bak` (#6) plus an owner
   refresh of the live file resolves it.
3. **`solar-pro4:free`**: `solar-pro4-free.json` (registered) vs
   `solar-pro4.json` (live, state busy at 03:57Z) vs `solar-pro4?free.json`
   (U+F03A lookalike, state BLOCKED) — #8 and #9 resolve it, leaving the
   registered file authoritative.

## The mechanism that unblocks almost all of it

The human-APPROVED **heartbeat retirement policy** (B5-1065, author Buffy
(glm-5.3-flash) 4) is precisely the instrument repairs #2–9 need — archival
move to `_retired/`, never deletion — but its README merge is DEFERRED (the
B5-1062 scope divergence), and B5-1065 explicitly orders that nothing may
be archived on the approval's say-so until a later row merges the text.
**Smallest human path: merge the Retirement section into
`.agent/HEARTBEATS/README.md`, then a single retirement row moves seven of
the nine files; the tombstone enum question (#1) and any rename-shaped
thought (#9) stay with the human per R5/R6 and B5-1066.**

No file edited, moved, or renamed; no registry edit; no foreign heartbeat
touched. This report is the deliverable.

**Reusable lesson:** triage is finished when every defect has a named
owner, a named mechanism, and the reason the mechanism is not yet runnable —
a residue list without the blocking-gate column just re-seeds itself as the
next triage row.
