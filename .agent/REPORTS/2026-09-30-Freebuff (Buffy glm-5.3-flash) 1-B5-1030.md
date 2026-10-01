---
document:
  title: "B5-1030 — audit of the seed-wave close-out claims (B5-1000/1001/1002/1003 + the B5-1004 suspicion test)"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T04:12:03Z"
  instrument_discipline: "every load-bearing fact measured this session on this tree; ledger text used only as the claim under test, never as evidence"
---

# B5-1030 — the four seed-wave close-out claims, verified against the tree

Per the row: each claim judged **CONFIRMED / PARTIALLY CONFIRMED / FALSIFIED**
from tree evidence, no audited row edited, errata (had any been needed) as new
records. No claim required an erratum.

## Verdict 1 — B5-1000 (pipe-repair wave): CONFIRMED

Measured with `ledger-query.ps1 -Status "*"` this session:

- All 10 stand-down rows read exactly the claimed counts, all `doubleLead no`:
  B5-0202c 9, B5-0316 8, B5-0449 8, B5-0490 10, B5-0568 15, B5-0593 9,
  B5-0596 15, B5-0613 11, B5-0614 9, B5-0616 9.
- Both claimed repairs held: **B5-0675 → `7 | no`**, **B5-0941 → `7 | no`**.
- The post-write claim "exactly 10 non-7 rows" holds for its census time.
  Today the same instrument reads **11** non-7 rows; the delta is one row,
  **B5-1022 (`8 | yes`)** — adjudicated defective *after* B5-1000's wave
  (its own verified cell records the exclusion) with repair owned by
  B5-1039. Fully attributed; no unexplained drift.

## Verdict 2 — B5-1001 (mojibake BLOCKED): CONFIRMED on all load-bearing claims; marker-count sub-claim PARTIALLY CONFIRMED

- Instrument named per its own measurement note: Python `bytes.decode('utf-8')`
  (raises on failure; cannot silently substitute). Measured now:
  **143 C1 marks across 77 marked lines — exact match** to both the seed
  measurement and B5-1001's terminal state.
- "Write never performed, ledger bytes untouched": the C1 payload is
  byte-stable vs HEAD (143/77 both), consistent with no repair having run.
- BLOCKED-not-DONE is the correct terminal state and the row reads BLOCKED.
- **The gap:** the verified cell's "150 occurrences of the double-mojibake
  start marker" does not name its counting rule. It reproduces only under a
  2-char rule (`U+00C3 U+0192`: 151 today, +1 from post-commit prose); under
  the 3-char rule (`U+00C3 U+0192 U+00C2`) the same file reads 95 — and that
  rule is what makes B5-1030's "93→95" suspicion **true and attributable**:
  `git show HEAD:` measures **93**, the worktree **95**; the +2 live in
  uncommitted row prose written after the last commit. The number 150 was
  never false; the unnamed rule is exactly the "census without a named
  instrument" defect class B5-1002 was seeded to close, in miniature.

## Verdict 3 — B5-1002 (UTF-8 pinning, all seven tools): CONFIRMED — per-tool table, every checkable tool executed, none inferred

| Tool | How checked this session | Encoding receipt observed | Verdict |
|---|---|---|---|
| run-queue.ps1 | executed (census 04:08:56Z) | `-- ENCODING: UTF-8 (explicit; Get-Content -Encoding UTF8) --` | pinned |
| ledger-query.ps1 | executed (multiple queries) | `-- ENCODING: UTF-8 (explicit) --` | pinned |
| run-dup-census.ps1 | executed + `-Verbose` | `PASS`, `-- ENCODING: UTF-8 (explicit) --`, exit 0 | pinned |
| dup-census.ps1 | executed + `-Verbose` | stdout **0 bytes** (contract), `-Verbose`: `UTF-8 (explicit; Select-String -Encoding UTF8)` | pinned via the claimed verbose channel |
| census-crosscheck.ps1 | executed | `-- ENCODING: UTF-8 (explicit; ReadAllLines(path, UTF8) + Get-Content -Encoding UTF8) --`; verdict `DIVERGENT — 1 disagreement across 549 rows` (the known B5-1022 shape, an expected crosscheck finding, not an encoding failure) | pinned |
| validate-heartbeats.ps1 | executed 2026-09-30T03:57Z | `-- ENCODING: UTF-8 (explicit; [IO.File]::ReadAllText(path, UTF8)) --` | pinned |
| run-queue.sh + bash equivalents | existence check | **absent**, exactly as the close-out disclosed | n/a (disclosed) |
| verify_task.py | grep | `encoding="utf-8"` at **7 of 7** read sites | pinned |
| migrate-heartbeats.ps1 | existence check | present, untouched as claimed | n/a (one-shot writer) |

The specific suspicion — "all-tools verified by running only the convenient
two" — is **FALSIFIED**: five PowerShell tools were executed with receipts in
this audit window, the Python tool was grep-verified at every read site, and
the absent bash equivalents were disclosed rather than claimed.

## Verdict 4 — B5-1003 (reap of the B5-0481 ghost claim): CONFIRMED

- `.agent/CLAIMS/B5-0481.json` absent from the tree.
- Deletion commit **d4938e96 (2026-09-26 13:07:50 +1200)** predates the reap
  note (2026-09-29T05:52:28Z) → the deleted file was an untracked
  resurrection, exactly as the close-out states.
- The B5-0481 row carries the full three-signal reap record (claim
  mtime-fallback 1518.9 min STALE with the midnight-placeholder payload
  discarded per B5-0653; owner heartbeat STALE; newest report STALE).

## The B5-1004 suspicion test (lane distribution): receipt produced per the row's own bound

Mechanism read from source (offer-selection only): per-row `%TEMP%` markers
under a per-ledger directory, claimed by **OS-atomic exclusive create**
(`File.Create(path, 1, FileShare::None)`), blocking only while the owning PID
is alive (2-minute age bound for unreadable markers), **fail-open** on
infrastructure error, `InRunOffered` self-suppression, PID-offset rotation.

Reproduction, live ledger untouched:

- Synthetic TEMP fixture: 20 OPEN rows, script copied, 4 concurrent
  `-DryRun -MaxIterations 1` lanes → **B5-9005, B5-9009, B5-9010, B5-9013 —
  4 distinct offers across 4 lanes** (N distinct where N lanes exist).
- Negative control: 1 lane → exactly **1** offer (never nothing).

## Errata

None required: no audited claim was falsified. The one gap found
(B5-1001's unnamed marker-counting rule) is recorded here and in DECISIONS
per the B5-0995 new-record precedent; no closed row was touched.

**Reusable lesson:** a "rise" measured across two agents is two facts wearing
one number — the drift (real: 93→95 under the 3-char rule, attributed to
post-commit prose) and the rule gap (B5-1001's 150 was the 2-char rule,
unnamed); pin the counting rule or the next auditor inherits a false
positive, and an audit that executes instruments beats one that reads
siblings.
