---
document:
  title: "B5-1000..B5-1019 seed wave — boot verification, 20 grounded OPEN rows"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 4", version: "space-bunny-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 4", version: "space-bunny-free"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# 2026-09-29 — opencode (space-bunny-free) 4 — boot verification + seed wave

Seeding pass only, on user order. Rows left `OPEN` and `UNCLAIMED` for the queue.
I did not work any of them and held no claim while writing. No commit, no push.

## 1. Verification battery (all six instruments, each with its receipt)

| # | Instrument | Verdict | Receipt |
|---|---|---|---|
| 1 | `javac -version` | **green** | `javac 1.8.0_292` / `openjdk 1.8.0_292-b10` AdoptOpenJDK — the expected JDK 8 |
| 2 | `b5ccg/compile.bat` | **green** | `Build successful. Run with: run.bat`, exit 0. One expected warning: `[options] bootstrap class path not set in conjunction with -source 1.6` — that is the `-source 6` gate announcing itself, not a defect |
| 3 | `run-dup-census.ps1` | **green** | `duplicate-ID census: PASS (0 duplicate task IDs)`, exit **0** (the designed clean value; `1` = duplicate, `2` = unreadable) |
| 4 | `ledger-query.ps1 -Status "*"` | **green, 12 known defects** | 494 rows at read time; 482 read `7`/`no`; **12 read a non-7 pipeCount** — enumerated in §2 |
| 5 | `validate-heartbeats.ps1` | **red by design** | 86 files, 85 conforming, **1 non-conforming**, **0 identity collisions**, exit 1 |
| 6 | `run-queue.ps1 -DryRun` | **empty at read time** | `Queue drained: no OPEN task without a live claim.` — a statement about *claims*, not about the ledger. **OPEN count was 0.** This is why the queue was empty, not because work was finished |

**Repo verdict: build green, IDs unique, no identity collisions, queue genuinely
drained.** The queue was empty because there were zero `OPEN` rows — a seed wave was
the correct response, not a hunt for existing work.

## 2. The 12 pipe-count defects, as measured

All `doubleLead = no`, all `UNCLAIMED`/`reportable` (the detector's claims-first
footer reported **0 rows under a non-stale claim** at census time, so none of these
is a transient mid-repair reading).

| Row | pipeCount | Class |
|---|---|---|
| B5-0675 | **6** | **additive** — a pipe is genuinely *missing* |
| B5-0316, B5-0449, B5-0941 | 8 | excess |
| B5-0202c, B5-0593, B5-0614, B5-0616 | 9 | excess |
| B5-0490 | 10 | excess |
| B5-0613 | 11 | excess |
| B5-0568, B5-0596 | 15 | excess, largest |

B5-0675 is the only additive case. Seeded as **B5-1000**.

## 3. A measurement trap I fell into, and why it is now a seeded task

This is the most useful thing the pass produced.

I first measured DECISIONS.md mojibake with PowerShell `Get-Content -Raw`:

```
DEC_C1 = 1600
```

That would have seeded a repair task for a file that is **clean**. Re-measuring the
same bytes with an explicit UTF-8 decode:

```
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::ReadAllText("$PWD\docs\DECISIONS.md", $enc)
DEC_C1 = 0        DEC_MojibakeMarker = 0
```

B5-0989 did its job; the file is fixed. The 1600 was a **host-default decode
artifact**, not a finding. The ledger showed the same split — 1600-plus naive versus
**143 real** under UTF-8, and **93** occurrences of the `ÃƒÂ` double-mojibake marker
across **77 lines**.

So the ledger mojibake is real and survives B5-0989 (which never had the ledger in
scope), while the DECISIONS "defect" was never there at all. Both halves are seeded:
**B5-1001** (guarded ledger repair, C1 only, deep class reported not repaired) and
**B5-1002** (make the tools pin UTF-8 and *name their encoding in output*, because a
census that does not name its instrument cannot be reproduced).

## 4. Claims state at read time

| Claim | Owner | Payload `started_utc` | Three-signal verdict |
|---|---|---|---|
| B5-0481 | `solar-pro4` | `2026-09-28T00:00:00Z` | **STALE** — newest signal (claim mtime 17:33 local) ≈35 min vs 30 TTL; owner heartbeat 664 min; B5-0481 report 3 days |
| B5-0990 | `Buffy (glm-5.3-flash) 4` | — | **LIVE** — appeared mid-pass; row flipped BLOCKED→OPEN under that claim |

B5-0481's payload is **midnight-by-default to the second** — the exact placeholder
`00_BOOT.md` step 6 and B5-0653 forbid. Its mtime and payload **disagree by over a
day**, so the two liveness signals are not merely different but mutually
contradictory. Seeded as **B5-1003**; I did **not** reap it (out of scope, unclaimed).

B5-0990 was a claim against a `BLOCKED` row when I found it — an **orphan** under
boot step 6. Seeded as **B5-1016**. Live by the time I re-censused, which is why it
reads `suppressed-live-claim` and is *not* a defect.

## 5. Root-level and tree findings (all measured, all untracked)

| Path | Measurement |
|---|---|
| `nul` | 142 B — Windows **reserved device name**, unopenable through the normal namespace → **B5-1006** |
| `tesseract-ocr-w64-5.3.3.zip` | **9 bytes** — cannot be the named artefact (real one is tens of MB) → **B5-1007** |
| `tmp-scans/` | 27 files, none claimed/dated/attributed → **B5-1008** |
| untracked total | **376** paths, 30 tracked modifications → **B5-1009** |

## 6. Observation not seeded — flagging instead

`run-queue.ps1 -DryRun` after the wave printed:

```
[1] Claiming lane for B5-1000 (20 claimable OPEN).
[2] Claiming lane for B5-1000 (20 claimable OPEN).
...
[10] Claiming lane for B5-1000 (20 claimable OPEN).
```

All **ten** lanes select the **same** row. The claim file would serialize the writes
so this is not a correctness bug, but it means N parallel lanes buy no parallelism
on offer selection. I did not seed this: `run-queue.ps1` is not in any of my 20 rows'
scopes, and seeding it opportunistically from a seeding pass is the drift AGENTS
§5 warns about. It belongs to whoever next claims runner work — and **B5-1014**
(which tests the seed branch in an isolated harness) and **B5-1019** (the standing
battery) are the natural homes for it if they find it in scope.

## 7. The 20 seeded rows

All `OPEN`, all `UNCLAIMED`, all `7` pipes / `doubleLead no`, post-write dup census
**PASS**.

| Row | Subject | Area |
|---|---|---|
| B5-1000 | Repair the 12 pipe-count rows above | ledger integrity |
| B5-1001 | C1 mojibake in the ledger, 77 lines, guarded | ledger integrity |
| B5-1002 | Make census tools pin + **name** their encoding | tooling |
| B5-1003 | Disposition the B5-0481 ghost claim (midnight payload) | liveness |
| B5-1004 | Execute the B5-0997-recorded B5-0953 reap, opening two engine gates | liveness |
| B5-1005 | Heartbeat retirement policy (86 files, 1 non-conforming) | liveness |
| B5-1006 | The `nul` reserved-name file | tree hygiene |
| B5-1007 | The 9-byte tesseract zip stub | tree hygiene |
| B5-1008 | `tmp-scans/` + `b5ccg/probe-b5-0956.txt` disposition | tree hygiene |
| B5-1009 | 376 untracked paths — inventory, not a commit | tree hygiene |
| B5-1010 | `ledger-query -Status ALL` silent-empty wildcard | tooling |
| B5-1011 | Refresh the 8-day-old headless smoke receipt | product verification |
| B5-1012 | Java 6 construct census → standing gate | build rule |
| B5-1013 | Attribution for `card-images/` + 3 unattributed diffs | provenance |
| B5-1014 | Test the untested B5-0900 self-seeding branch | tooling |
| B5-1015 | Should the ledger stop growing? measured cost | queue economics |
| B5-1016 | Claims against non-OPEN rows (B5-0990 instance) | liveness |
| B5-1017 | `ledger-query` vs `run-queue` liveness divergence | tooling |
| B5-1018 | 18 BLOCKED rows: expiry + reason taxonomy | queue economics |
| B5-1019 | Standing self-verification battery (these six) | tooling |

Every premise is a measurement from this pass, not an inherited note — the B5-0991
lesson (*a premise named zero needs a fresh grep*) applied to all 20. Rows that
depend on state that moves (the pipe set, the reap targets) instruct the claimant to
**re-measure and re-census immediately before writing**.

## 8. Reusable lessons

- **Name the instrument, or the number is not a measurement.** Two reads of one
  file gave 1600 and 143. The first would have filed a repair task against a clean
  file.
- **A drained queue is a statement about claims, not about work.** `OPEN = 0` was
  the finding; the runner's "queue drained" is not evidence that anything finished.
- **A claim's mtime and its payload can contradict each other.** B5-0481: payload
  41 h old, mtime 35 min. Judged on either alone you get a different answer; only
  the three-signal rule reads both.
