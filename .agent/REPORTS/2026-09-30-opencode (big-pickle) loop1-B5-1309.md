---
document:
  title: "B5-1309 close-out - the B5-1094 residue is unchanged (9 non-conforming files, 3 collisions, zero new, zero retired, +22 conforming files since), but 8 of the 9 already sit in _quarantine inside the audited tree so the proposed _retired/ destination would not clear them, and the validator report is keyed on file name so a name collision hides which copy is defective"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1309"
  instrument: ".agent/tools/validate-heartbeats.ps1 (UTF-8 explicit), two runs at ~23:21Z and ~23:26Z, cross-referenced against .agent/REPORTS/2026-09-30-Freebuff (Buffy glm-5.3-flash) 1-B5-1094.md"
---

# B5-1309 close-out - residue triage re-census, plus one finding B5-1094 could not have had

**Claim:** `.agent/CLAIMS/B5-1309.json` (`opencode (big-pickle) loop1`, started
2026-09-30T23:19:36Z), released at close-out. Row re-read `OPEN` before claiming;
no live claim on the row. Scope held: `.agent/HEARTBEATS` read-only, this report,
one pattern, heartbeat, DECISIONS, the row flip. **No heartbeat file edited,
moved or renamed except my own; no registry edit; no tool edit; no src or data
edit; no commit; no push.**

## Standing state (measured now, not inherited)

| | B5-1094 (06:22Z) | B5-1309 (~23:21Z) | delta |
|---|---|---|---|
| files | 112 | **134** | **+22** |
| conforming | 103 | **125** | **+22** |
| non-conforming | 9 | **9** | **0** |
| distinct agent_id | 104 | **126** | **+22** |
| collisions | 3 | **3** | **0** |
| exit code | 1 | **1** | - |
| registry entries | - | **27** | - |

Two runs ~5 minutes apart returned identical aggregates. **Every one of the 22
new files conforms**, so the residue is still a frozen residue and still not a
defect generator - B5-1094's central conclusion holds over a further ~17 hours and
22 more sessions.

## Classification: 0 newly appeared, 0 retired, 9 unchanged

Each item is compared against B5-1094's table by file name, issue count and issue
text. All nine match exactly.

| # | file (relative to `.agent/HEARTBEATS/`) | location now | issues | vs B5-1094 |
|---|---|---|---|---|
| 1 | `Cline (space-bunny) b5-0941.json` | **live root** | 1 - `state 'released'` out of enum | unchanged |
| 2 | `freebuff-01.json` | `_quarantine/` | 1 - agent_id `Buffy (deepseek-v4-flash)` matches no file | unchanged |
| 3 | `freebuff-02.json` | `_quarantine/` | 1 - same | unchanged |
| 4 | `freebuff-03.json` | `_quarantine/` | 1 - same | unchanged |
| 5 | `kiro-pi.timestamp` | `_quarantine/` | 2 - parse failed + extension | unchanged |
| 6 | `me-so-poor.json.bak` | `_quarantine/` | 2 - unregistered stem + extension | unchanged, **liveness concern retired** (below) |
| 7 | `solar-pro4` (0 bytes) | `_quarantine/` | 2 - empty + extension | unchanged |
| 8 | `solar-pro4.json` (quarantined copy) | `_quarantine/` | 1 - agent_id `solar-pro4:free` matches no file | unchanged |
| 9 | `solar-pro4?free.json` (U+F03A) | `_quarantine/` | 4 - pre-schema, unregistered stem, `BLOCKED` out of enum, lookalike name | unchanged |

**Collisions - same three identities, same membership:**

1. `Buffy (deepseek-v4-flash)` - registered `Buffy (deepseek-v4-flash).json`
   plus `freebuff-01/02/03.json` (4 files).
2. `me-so-poor` - `me-so-poor.json` plus `me-so-poor.json.bak`.
3. `solar-pro4:free` - registered `solar-pro4-free.json`, `solar-pro4.json`,
   `solar-pro4?free.json`.

## Finding 1 - 8 of the 9 are already quarantined, and `_quarantine/` is *inside*
the audited tree

B5-1094 proposed "move out of HEARTBEATS" for seven items and named `_retired/`
as the destination. Measured now: **eight of the nine already sit in
`_quarantine/`**, which is a subdirectory of `HEARTBEATS/`. They still produce
findings because B5-1062 deliberately added `-Recurse` to `validate-heartbeats.ps1`
- correctly, since that same change is what made the U+F03A lookalike collision
visible at all.

Two consequences, both decision-relevant:

* **The `_retired/` destination B5-1094 named would not have worked.** Moving
  files into `.agent/HEARTBEATS/_retired/` leaves them inside the scanned tree, so
  the findings survive the move intact. The archival move has to land in a
  **sibling** directory outside `HEARTBEATS/` (for example
  `.agent/HEARTBEATS_RETIRED/`), or the tool needs an explicit exclusion list -
  and a blanket directory exclusion is what B5-1062 showed is dangerous, because
  that is precisely how the lookalike collision went unreported for one run.
* **B5-1094's proposal was location-blind, and location is now the whole
  question.** Its #2-#9 repairs are not stale, they are *mis-targeted*: the
  bytes are already out of the live root, and what remains is that the quarantine
  is a live part of the audit.

The gate is also confirmed still closed, by direct read rather than by
inheritance: **`.agent/HEARTBEATS/_retired/` does not exist**, and
`.agent/HEARTBEATS/README.md` contains **no Retirement section** - the only
related line (165) references the "quarantine-not-delete precedent". B5-1065's
policy text is still unmerged, so the archival-move instrument remains not
runnable, exactly as B5-1094 said.

## Finding 2 - the validator's report is keyed on file name, so a name collision
hides which copy is defective

**New; not in the B5-1094 baseline.** `solar-pro4.json` exists **twice** in the
audited tree:

```
.agent/HEARTBEATS/solar-pro4.json             800 bytes  agent_id = solar-pro4        (CONFORMS)
.agent/HEARTBEATS/_quarantine/solar-pro4.json  1028 bytes  agent_id = solar-pro4:free   (NON-CONFORMING)
```

The validator reports `$r.File`, which is `$f.Name` - the **name**, not the path.
So both runs printed a single row:

```
[solar-pro4.json]  (1 issue)
    - agent_id 'solar-pro4:free' matches no known file (stem='solar-pro4', ...)
```

which is the **quarantined** copy's content. The live-root copy is never named,
and anyone who follows B5-1094's item #8, opens `solar-pro4.json`, and finds a
perfectly conforming file with `agent_id: solar-pro4` will conclude the report is
wrong. The same ambiguity sits in the collision listing (`-> solar-pro4.json`
under `solar-pro4:free`).

Scope of the defect class: the **aggregate counts are unaffected** - the validator
reads all 134 files and the totals (134/125/9/126/3) are correct - but the
**per-file attribution is ambiguous**. Across the store there are 134 files and
**133 distinct names**, one duplicated name, and that one duplicate is exactly the
case a triage reader is most likely to act on.

Smallest repair, **not applied**: report the path relative to the heartbeat
directory in the `FILE` column and the JSON `File` field, i.e. print
`_quarantine/solar-pro4.json` distinct from `solar-pro4.json`. That is a display
change in `validate-heartbeats.ps1`; it moves no file and edits no heartbeat. It
is outside this row's scope (`.agent/HEARTBEATS` read-only, no tool edit), so it
is recorded as a proposal only.

A related non-defect worth naming so nobody "fixes" it: the live-root
`solar-pro4.json` conforms **by design**. Its `agent_id` (`solar-pro4`) is absent
from `_registry.json`, but the registry is a resolution aid for ids a filename
cannot express, not an allow-list - stem equals `agent_id`, so the file is valid.
There are two identities here (`solar-pro4` and `solar-pro4:free`), not one
identity with a broken file.

## Finding 3 - the `me-so-poor` staleness inversion has retired itself

B5-1094's collision #2 carried a liveness claim: `me-so-poor.json.bak` was
**newer** than the live `me-so-poor.json`, implying the live heartbeat had gone
stale. Measured now:

```
.agent/HEARTBEATS/me-so-poor.json            mtime 2026-09-30T09:42:13Z  (payload utc 09:45:00Z)
.agent/HEARTBEATS/_quarantine/me-so-poor.json.bak  mtime 2026-09-29T08:44:38Z
```

The live file is now ~25 h newer; the inversion is gone. The collision itself
persists (both files still assert `me-so-poor`) and the static findings stand, but
the *reason* it mattered - a stale live heartbeat - no longer applies, so the
repair for #6 is now purely archival.

## Finding 4 - an mtime-keyed liveness rule misreads one file by 12 hours

Live-root `solar-pro4.json` carries `utc: 2026-09-30T22:45:00Z` while its
filesystem mtime is `2026-09-30T10:23:11Z` - the payload timestamp is **12 h 21 m
ahead of the file's own mtime**. Nothing here is a schema breach (the validator
passes the timestamp), but both the claim-liveness rule (`.agent/CLAIMS/README.md`)
and the validator's `MTIME_MIN` column are mtime-keyed, so they will report this
file as roughly half a day stale while its payload asserts recent work. Recorded as
an observation of the same class B5-0952 self-caught, not as an adjudication: a
payload stamp ahead of its own mtime is either an unbacked future stamp or an mtime
lost in a checkout/copy, and this row cannot tell which from a read-only
measurement.

## The 9 smallest per-file repairs (proposed, NONE applied)

| # | file | smallest repair | owner |
|---|---|---|---|
| 1 | `Cline (space-bunny) b5-0941.json` (live root) | unchanged: either ratify `released` into the state enum or retire the file. R5/R6 protected tombstone - no agent edit | human |
| 2-4 | `freebuff-01/02/03.json` | archival move to a **sibling** directory outside `HEARTBEATS/` (not `_retired/` inside it), after the Retirement section is merged | owner, gated |
| 5 | `kiro-pi.timestamp` | move out of `HEARTBEATS/` entirely - a 22-byte timestamp fragment is not a heartbeat, quarantined or not | owner |
| 6 | `me-so-poor.json.bak` | archival move out of `HEARTBEATS/`; the staleness rationale is retired, the file is redundant | owner |
| 7 | `solar-pro4` (0 bytes) | delete - an empty file has no content to preserve, so the archival-move policy does not apply to it | owner |
| 8 | `_quarantine/solar-pro4.json` | archival move out of `HEARTBEATS/`; the registered `solar-pro4-free.json` remains authoritative | owner, gated |
| 9 | `_quarantine/solar-pro4?free.json` | archival move out, **no rename** (B5-1066 declined the R5 exception); sequence it last, because it is the only cross-directory evidence of the `solar-pro4:free` collision and a partial move that drops the count to 0 is not clearance | human |

Collision-level: no repair is available at the identity level - all three resolve
to file-level moves above, and no file may be merged, rewritten or re-identified
by an agent (R5/R6).

## Not done

No heartbeat file other than mine was edited, moved or renamed; no registry edit;
no `validate-heartbeats.ps1` edit; no README merge; no src or data edit; no commit;
no push. All 9 non-conforming files and all 3 collisions were left byte-identical.

**Reusable lesson:** a triage list is only actionable if each item carries its
**location**, because the validator recurses - a file already moved out of the live
root can still be a live finding, and a repair aimed at the wrong directory
silently achieves nothing; and any report keyed on a file's **name** rather than
its path will, on a store that contains a duplicated name, point the reader at the
copy that is not defective.