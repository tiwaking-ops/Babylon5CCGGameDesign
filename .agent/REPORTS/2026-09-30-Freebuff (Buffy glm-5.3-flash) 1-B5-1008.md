---
document:
  title: "B5-1008 — scratch disposition: tmp-scans inventory, classification, ignore-rule proposal"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T03:43:16Z"
  tree_state: "uncommitted working tree; nothing committed or pushed by this row"
---

# B5-1008 — scratch disposition for `tmp-scans/` and the misplaced probe

## 0. What the row asked, and what was measured vs recalled

Three parts: inventory + classify (EVIDENCE / ORPHAN / AMBIGUOUS), report the
per-path ignore rule, propose a disposition per ORPHAN without deleting
anything. Everything below was **measured on this tree 2026-09-30T03:41–04:05Z**
(`find -printf`, `git check-ignore -v`, `git cat-file -e`, content reads), not
recalled from the row's numbers.

**Manifest:** `tmp-scans/` = 306 files / 127,593,237 B recursive; **28
top-level files**; `b50939/` 276 files / 126,362,816 B (card-image OCR/crop
workspace: 264 PNGs, probe scripts, `ocr-pass.json`, `crops/`); `b50960/` 2
files / 23,354 B. The row's "27 files" counted the top level only; 27 + 1 =
28 — the +1 is B5-1006's 142-byte preservation copy
`nul-file-2026-09-29.txt`, restored in the 2026-09-29T21:46Z bulk restore
(all mtimes cluster there; content dates live in the names, B5-0965..B5-0998).
The probe `b5ccg/probe-b5-0956.txt` (531 B) sits inside the source tree —
the row's worst-placement case, confirmed: zero `tmp-scans` references in
`b5ccg/src`, and the file is referenced by nothing tracked.

## 1. Part 1 — classification (every unit opened on content; AMBIGUOUS = 0)

**EVIDENCE (4 units, kept, never deletion candidates):**
- `DECISIONS-before-b50989.md` (1,080,520 B) — cited at
  [DECISIONS.md:9097](docs/DECISIONS.md#L9097); the row's own model case of
  diff provenance.
- `nul-file-2026-09-29.txt` (142 B) — B5-1006's preservation copy, cited at
  [DECISIONS.md:9428](docs/DECISIONS.md#L9428).
- `b50939/` — cited by `docs/reports/aftermath-card-image-diff-2026-09-28.md:203`
  ("Raw pipeline artifacts preserved under tmp-scans/b50939/").
- `b50960/` — cited by `docs/reports/java6-construct-census-2026-09-28.md:21`.

**ORPHAN (24 top-level files + the probe), split by evidence value:**
- **Reconstructible from git alone → propose DELETE:**
  `b50965-aiplayer.diff` — both `index` blobs reachable (`git cat-file -e`
  passes for 8b679412 and 7811b5f2); reconstruct with
  `git diff 8b679412 7811b5f2`.
- **Unique +side → keep + attribute:** `b50970-guide.diff`,
  `b50971-nps.diff`, `b50971-trc.diff` — old blob reachable, **new blob
  ABSENT** from the object DB; their new-state content exists nowhere else
  (later superseded without an intervening commit).
- **Trivial/reproducible → propose DELETE:** `b50973-utc.txt` (21 B, one
  timestamp), `b50973-validate.txt` (69 files/68 conforming/1 known) and
  `validate-out.txt` (72/71/1) — heartbeat-validator receipts.
- **Instruments → keep + attribute:** 17 `b509XX-swap.pl` + `b50977-reap.pl`
  (ledger-row surgery scripts) and `b50980-census.pl` (read-only card-family
  census). Git history ends at the 2026-09-28 checkpoint commits (p100 =
  09-28); the 09-29 surgery they performed predates the newest commit, so
  these are the only surviving record of *how* it was done.
- **Misplaced probe → DELETE after this report:** `b5ccg/probe-b5-0956.txt`,
  full content (six probe lines, all `exit=0`, ending `ALL-COMPLETE`):

```
HeadlessStallSoakProbe|exit=0|ms=1086664|lines=30|last=Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8
DONE-MARKER HeadlessStallSoakProbe
HeadlessStationVictoryTest|exit=0|ms=496|lines=8|last=Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8
DONE-MARKER HeadlessStationVictoryTest
HeadlessWarConflictProbe|exit=0|ms=1274|lines=60|last=Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8
DONE-MARKER HeadlessWarConflictProbe
ALL-COMPLETE
```

(The B5-0956 report exists — `.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0956-B5-0956.md`
— but names no output file; attribution is by task id in the filename.)

## 2. Part 2 — the ignore rule, measured

- `git check-ignore -v` on `DECISIONS-before-b50989.md`, `b50939/analyze.py`,
  `b50960/census.py`, `b5ccg/probe-b5-0956.txt`: **exit 1 — none ignored.**
  `git status --porcelain` shows `?? tmp-scans/` and
  `?? b5ccg/probe-b5-0956.txt`: a plain `git add -A` checkpoint would absorb
  127.6 MB including a 120 MB PNG workspace, and the probe would land among
  shipped sources.
- `.gitignore` (129 lines) already carries **both models**: wholesale lines
  (`/Pene/`, `/ledger.bak`, `/loop-prompt.md`, two U+F03A dirs) and the
  B5-0955 per-path block (lines 75–93, ten artifacts, each attributed).
- **The per-path rule has demonstrably rotted:** every unit in this manifest
  was created 2026-09-28/29 — after B5-0955 — and not one qualified for a
  line. One day of queue output outgrew the model.

## 3. Part 3 — proposal (the deliverable)

Filed: [scratch-disposition-tmp-scans-proposal.md](docs/proposals/scratch-disposition-tmp-scans-proposal.md)
— ONE structural change: a wholesale `/tmp-scans/` ignore line with
attribution comment, plus **promotion as the compensating control** (scratch
becomes tracked only by being promoted into `.agent/REPORTS/` or
`docs/reports/` by the task that cites it; the B5-0986 flow). Secondary note
for humans: a future AGENTS §6 amendment should name `tmp-scans/` explicitly,
as §6 names `investigations/`. Net if adopted as proposed: 5 files (~35 kB)
deleted, everything else kept under one rule, `b5ccg/` returned to
source-only. **This row deleted, moved, and ignored nothing** — deletion is a
separate claim.

## 4. Scope and gates

Files touched by this row: the proposal (new), the DECISIONS append, this
report, the pattern, my claim and heartbeat. No `b5ccg/src` or card-data
edit, no foreign artifact touched, no reap, no commit, no push. Ledger row
flipped DONE with 7 pipes / single lead, verified via `ledger-query.ps1`;
`run-dup-census.ps1` exit 0 after the write.

**Reusable lesson:** a scratch rule has two halves — keeping git from seeing
the pile is worth nothing unless promotion is the only door into the tracked
tree; and "how was this produced" outranks "how big is this": a 142-byte
preservation copy is EVIDENCE while a 15 kB diff can be deletable garbage,
decided not by size but by whether its information exists anywhere else
(for diff snapshots, `git cat-file -e` on each index blob is that test).
