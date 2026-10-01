---
document:
  title: "B5-1008 administrative release of a future-dated claim (human-authorized)"
  status: "Report (observations, no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle) rel1008", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle) rel1008", version: "big-pickle"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# B5-1008 administrative release — future-dated claim, deleted on human order

**Agent:** `opencode (big-pickle) rel1008` · **Model:** big-pickle
**Started:** 2026-09-29T09:31:38Z (heartbeat written in the same command block as
this clock sample) · **Finished:** 2026-09-29T09:41:38Z
**Authority:** explicit user order in conversation, following the B5-0953
admin-release precedent. **Claim held:** none, by design.

## What was asked

Delete `.agent/CLAIMS/B5-1008.json`, record the release in
`docs/DECISIONS.md`, and add a dated note under the `B5-1008` row in
`.agent/TASK_LEDGER.md` so the task returns to `OPEN` and offerable.

## Why the owner could not simply re-claim it

The claim's owner is `me-so-poor`. R1 in `.agent/HEARTBEATS/README.md` makes an
`agent_id` name a running session **instance**, not a model. A new session
writing as `me-so-poor` would be impersonating a live id and would trip
`validate-heartbeats` with an IDENTITY COLLISION. The `CLAIMS` README forbids
editing another agent's claim. So the B5-0976 "owner re-claims" route was closed
and only the human route remained — which is the route taken.

This session therefore wrote its own distinct heartbeat
(`opencode (big-pickle) rel1008`, verified collision-free against all 94 existing
files on a normalised key) and held **no** claim: releasing a row is not work on
that row.

## Evidence (all measured at 2026-09-29T09:31:48Z)

| Signal | Reading | Verdict |
|---|---|---|
| `started_utc` | `2026-09-29T20:51:47Z` | 680 min **ahead** of the clock; 60-min B5-0952 tolerance blown by 11x |
| `ttl_min` | 30 | unreachable — negative age compares younger than any TTL |
| owner heartbeat payload `utc` | `2026-09-29T08:35:00Z` | genuine past value, 56.8 min old, itself past the 30-min TTL |
| reports matching `B5-1008` | 0 | no work product at risk |
| git tracking | untracked | deletion leaves no tracked history |

### The forgery is arithmetic, not drift

```
claimed value as written        : 2026-09-29T20:51:47Z
host local offset               : +13:00  (780 min)
implied real write (local - 13h): 2026-09-29T07:51:47Z
overshoot at the write instant  : 780 min  == exactly the local offset
elapsed since then              : 100.5 min
780 - 100.5                     : 679.5 min == the 679.5 min measured now
```

The overshoot equals the local offset at the write instant and decays at exactly
real-time rate. The writer stamped local wall-clock time and appended a `Z`. Same
defect as the B5-0430 measure-the-clock-at-the-write pattern; worse than a missing
timestamp, because a well-formed wrong one needs counter-evidence to disbelieve.

### Note: the owner heartbeat was *not* forged

`08:35:00Z` is a real past timestamp. The claim was forged, the heartbeat was not
— so the session genuinely stopped, and the heartbeat's silence is real evidence
rather than a symptom of the same bug. The 08:35Z stop is also consistent with the
07:51Z claim time implied by the 13-hour correction.

## Correction to my own first reading

I initially cited the claim file's mtime (`2026-09-29T08:44:38Z`) as evidence of
a 38-minute-old live claim. **It is not evidence.** 90 of the 94 heartbeat files
share that identical mtime to the millisecond, while the two live claims written
minutes apart carry genuinely distinct mtimes. `08:44:38Z` is a bulk checkout
stamp on this tree. A timestamp that ~95% of a directory shares describes the
directory, not the file. Recorded in the ledger note and the decision entry so a
later agent does not repeat the reading.

## Actions taken

- **Deleted** `.agent/CLAIMS/B5-1008.json` — whole file, never edited (B5-0653).
- **Wrote** `.agent/HEARTBEATS/opencode (big-pickle) rel1008.json`.
- **Appended** a dated, zero-pipe note below the `B5-1008` row, then a dated
  correction note below that (both adjacent to the row). Row left byte-identical
  at 7 pipes / 1882 chars, still `OPEN`. File's CRLF endings preserved
  (1153 CRLF / 0 bare LF, was 1149 / 0 — exactly the 4 lines added).
- **Appended** the decision entry to `docs/DECISIONS.md` (bare LF preserved,
  0 CRLF), added the `assessor_llm` entry, retargeted `last_modified_by_llm`.
- **Wrote** pattern
  `.agent/PATTERNS/opencode (big-pickle) rel1008/2026-09-29-a-mtime-shared-by-ninety-files-is-a-checkout-stamp.md`.

## Gates run

| Gate | Result |
|---|---|
| `run-dup-census.ps1` | `PASS`, 0 duplicate task IDs, exit 0 |
| `ledger-query.ps1 -Status CLAIMED` | 0 rows, exit 0 — no row reads CLAIMED |
| `run-queue.ps1 -DryRun` (×30, ×40) | exit 0, **zero** `B5-0952` warnings, **zero** `B5-0653` warnings, 12 distinct rows offered |
| `B5-1008` gates (read-only, replicated) | all four pass — see below |
| git commit / push | none |

`B5-1008` claimability, verified gate by gate rather than inferred:

| Gate | Result |
|---|---|
| status cell | `OPEN` |
| `Get-ImplausibleClaimTaskIds` | cannot contain it — no claim file to scan |
| `Get-CensusSuppression` | cannot contain it — built from claim files |
| `Test-LiveClaim` | cannot report live — no claim file to find |
| `Prereqs` map | not a key, so no predecessor gate |

Strongest single proof: a `B5-1008.offer` marker exists in
`%TEMP%\run-queue-offers-C__temp_projects_Babylon5CCGGameDesign__agent_TASK_LEDGER_md\`.
Markers are only ever created by `Select-OfferRow` (run-queue.ps1:930), which runs
*after* `Get-ClaimableOpenTasks` has already filtered the row. The marker's
existence is therefore direct evidence the row passed all four gates.

## Correction: the row is claimable, but the offer does not reach it

My ledger note and decision entry both claimed `run-queue.ps1 -DryRun` "offers
B5-1008 again". **That is wrong**, and both entries carry an appended correction.

The `B5-0952` warning is genuinely gone, but `B5-1008` is absent from the 12 rows
the dry run offered for an unrelated reason: `Test-OfferMarkerAvailable`
(run-queue.ps1:887) found a live offer marker for the row, written
`2026-09-29T07:45:59Z` by `powershell` **pid 7216, still alive** and running since
`07:45:58Z` — about 120 minutes. Line 908 returns `false` while the owning PID
lives, so the marker suppresses the offer no matter what the ledger says.

That marker predates this release by two hours and has nothing to do with the
deleted claim. It belongs to another process, so I left it alone: clearing a
live-PID marker unilaterally is stealing a lock I do not own, which is the same
boundary the claim release itself respected.

**Practical consequence:** the queue will not hand out `B5-1008` while pid 7216
lives. The row needs either that process to exit or explicit human instruction to
clear the stale marker.

**Report-only finding, not fixed here:** the stale-marker class is a real
scheduling defect, distinct from the B5-0952 forgery. A marker whose owner is a
long-lived PowerShell that no longer intends the offer keeps a claimable row
invisible indefinitely — the PID-liveness test is correct for a short lane process
and wrong for a long-lived one. No tool edited, no row edited, left for a human
or a seeded row to route.

## Left byte-identical (deliberately)

The foreign `me-so-poor` heartbeat, every other claim and row, and all of
`engine/`, `model/`, `ai/`, `ui/` and card data. No src edit, no card-data edit,
no commit, no push. The `%TEMP%` offer marker owned by pid 7216 was also left in
place — see the correction section.

## Reusable lesson

Two, both now in the pattern file:

1. A file mtime shared by ~90 of 94 siblings in a store is a checkout stamp, not
   evidence about any one file. Always run the control — count how many siblings
   share the exact mtime — before citing a timestamp as proof of recency.
2. A per-task offer marker in `%TEMP%` can suppress a claimable row indefinitely
   while its owning PID lives. The liveness test that is right for a short lane
   process becomes a permanent lock when the owner is a long-lived shell. A
   "this row is not being offered" observation is not evidence about the row.

