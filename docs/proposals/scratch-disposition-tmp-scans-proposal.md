---
document:
  title: "Scratch disposition — tmp-scans inventory, classification, and the standing ignore rule"
  status: "PROPOSED (B5-1008; nothing deleted, moved, or ignored by this row — deletion is a separate claim)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  measured_on_this_tree: "2026-09-30T03:41-04:05Z"
---

# Scratch disposition: `tmp-scans/` + `b5ccg/probe-b5-0956.txt`

## 0. Measured manifest (this tree, 2026-09-30, `find -printf %s/%T@`; not recalled from the row)

`tmp-scans/` holds **306 files / 127,593,237 bytes** recursive: **28 top-level
files** plus two subdirectories (`b50939/` 276 files / 126,362,816 B;
`b50960/` 2 files / 23,354 B). The row measured "27 files" = the top level
only, before B5-1006 (2026-09-29) added its 142-byte preservation copy
`nul-file-2026-09-29.txt` (27+1=28, delta fully explained). The probe file
`b5ccg/probe-b5-0956.txt` (531 B) sits inside the source tree — the row's
worst-placement case.

All mtimes cluster at 2026-09-29T21:46:17–18Z (bulk restore; content dates are
in the names, B5-0965..B5-0998).

### Top-level units (28)

| Unit | Bytes | Class | Citation / content |
|---|---|---|---|
| DECISIONS-before-b50989.md | 1,080,520 | **EVIDENCE** | cited `docs/DECISIONS.md:9097` (B5-0989 diff provenance, the row's own model case) |
| b50965-aiplayer.diff | 14,997 | ORPHAN (reconstructible) | `git diff` snapshot, AIPlayer.java; both index blobs reachable — see §2 |
| b50970-guide.diff | 19,323 | ORPHAN (unique +side) | snapshot, playtest-guide.md; +side blob ABSENT from object DB |
| b50971-nps.diff | 3,965 | ORPHAN (unique +side) | snapshot, negative-power-split proposal; +side ABSENT |
| b50971-trc.diff | 3,827 | ORPHAN (unique +side) | snapshot, tool-rule-convergence proposal; +side ABSENT |
| b50973-utc.txt | 21 | ORPHAN (trivial) | single timestamp receipt `2026-09-28T19:41:26Z` |
| b50973-validate.txt | 13,695 | ORPHAN (reproducible) | validate-heartbeats output (69 files/68 conforming/1 known) |
| validate-out.txt | 13,350 | ORPHAN (reproducible) | same validator, later count (72/71/1) |
| nul-file-2026-09-29.txt | 142 | **EVIDENCE** | B5-1006's preservation copy, cited `docs/DECISIONS.md:9428` |
| b50975/978/980/982/984/986/987/988/989/991/992/993/994/995/996/997/998-swap.pl (17 files) | 2.4–3.9 kB each | ORPHAN (instrument) | ledger-row mutation scripts; the 09-29 ledger state they produced predates the newest commits (p100 = 09-28), so they are the only surviving "how" of that surgery |
| b50977-reap.pl | 2,462 | ORPHAN (instrument) | claim-reap script, same family |
| b50980-census.pl | 3,880 | ORPHAN (instrument) | read-only card-data family census (premiere/deluxe JSON) |
| b50939/ (dir, 276 files) | 126,362,816 | **EVIDENCE** | cited `docs/reports/aftermath-card-image-diff-2026-09-28.md:203` — OCR/crop pipeline workspace (264 PNGs, probe scripts, ocr-pass.json); membership inherits the dir citation |
| b50960/ (dir, 2 files) | 23,354 | **EVIDENCE** | cited `docs/reports/java6-construct-census-2026-09-28.md:21` |

Plus `b5ccg/probe-b5-0956.txt` (531 B, ORPHAN/misplaced): B5-0956 headless
harness receipts — six probe lines `exit=0` + `ALL-COMPLETE`; full content
quoted in the B5-1008 report. Referenced by nothing tracked; the B5-0956
report does not name the file.

**AMBIGUOUS (unreadable): 0.** Every unit opened and was classified on
content, not name.

## 1. Part 1 verdict

4 evidence units (1.06 GB: the DECISIONS backup, the nul-file preservation,
`b50939/`, `b50960/`) are cited by tracked reports and are the B5-0989-style
diff-provenance the culture depends on — KEEP, never propose deletion. 24
top-level files + the probe are orphans, but the orphan class splits by the
§2 test. Nothing was unreadable; classification needed no guessing.

## 2. The decidable test for diff snapshots: blob reachability

`git cat-file -e <blob>` on each `index a..b` line of a `git diff` snapshot
decides its information content **without judgement**:

| Snapshot | old blob | new blob | Verdict |
|---|---|---|---|
| b50965-aiplayer.diff | 8b679412 REACHABLE | 7811b5f2 REACHABLE | reconstructible from git alone → **propose DELETE** |
| b50970-guide.diff | 0da91e22 REACHABLE | 78d5f0fc ABSENT | +side exists nowhere else → attribute, keep |
| b50971-nps.diff | 4862d6da REACHABLE | f32a4898 ABSENT | same → attribute, keep |
| b50971-trc.diff | e9400c56 REACHABLE | f0dcb413 ABSENT | same → attribute, keep |

(The three ABSENT +sides are exactly the proposals/edits that were later
superseded in the working tree without an intervening commit — the snapshot
is their only record.)

## 3. Part 2 findings — the ignore rule per path

- `git check-ignore -v` on `DECISIONS-before-b50989.md`, `b50939/analyze.py`,
  `b50960/census.py`, `b5ccg/probe-b5-0956.txt`: **exit 1 — none ignored.**
  `git status --porcelain`: `?? tmp-scans/`, `?? b5ccg/probe-b5-0956.txt`.
  A plain `git add -A` checkpoint would absorb 127.6 MB of scratch including
  a 120 MB PNG workspace, and the probe file would land among shipped
  sources.
- `.gitignore` (129 lines) carries **both models already**: wholesale lines
  (`/Pene/`, `/ledger.bak`, `/loop-prompt.md`, two fullwidth-colon dirs) and
  the B5-0955 per-path block (lines 75–93, ten artifacts, each with an
  attribution comment).
- **The per-path rule has demonstrably rotted:** every one of the 29 units
  here was created 2026-09-28/29 — after B5-0955 — and not one qualified for
  an ignore line. One day of queue output outgrew the model.

## 4. Part 3 — the ONE structural proposal

**Add a single wholesale line `/tmp-scans/` to `.gitignore`** with a short
attribution comment naming this proposal, and make **promotion the
compensating control**: a scratch file earns tracked status only by being
promoted into `.agent/REPORTS/` or `docs/reports/` by the task that cites it
(the B5-0986 flow); `tmp-scans/` is never read by git again.

Costs, on the page: wholesale ignore makes `git status` blind *inside*
`tmp-scans/` — new litter accumulates unseen. That is accepted because (a)
the B5-0430 culture already forces every scratch-producing task to name its
artifacts in a report, so evidence cannot become anonymous without breaking
a rule that already exists; (b) a disposition sweep can census the directory
directly, exactly as this row did — blindness to `git status` is not
blindness to measurement; (c) the per-path alternative is measured rotting
(§3), and an ignore model that needs daily maintenance is a queue task
generator, not a rule.

Rejected alternative — folding scratch under `.agent/`: mixes 120 MB of raw
image scratch with governance tooling, and `.agent/` subdirectories carry
meaning (REPORTS schema, PATTERNS namespaces, CLAIMS liveness semantics).
No benefit: neither location is tracked. A secondary note for the next human
amendment of AGENTS §6: name `tmp-scans/` explicitly, as §6 names
`investigations/` — agents do not amend governance, so this stays a note.

**Ready-to-apply patch:** a drafted, verified hunk for this rule lives at
[b5-1008-gitignore-patch-draft.md](b5-1008-gitignore-patch-draft.md) —
`git apply --check` proven green against the working tree 2026-09-30T04:30Z
(with the pre-existing +115/-1 dirty state of `.gitignore` measured and
disclosed there). Applying it remains behind the human approval sentence.

## 5. Per-orphan disposition proposals (deletion is a SEPARATE claim; this row deletes nothing)

| Orphan(s) | Proposal |
|---|---|
| b50965-aiplayer.diff | DELETE — both blobs reachable; reconstruct with `git diff 8b679412 7811b5f2` |
| b50973-utc.txt | DELETE — 21-byte timestamp, zero residual information |
| b50973-validate.txt, validate-out.txt | DELETE — reproducible validator receipts; counts quoted here |
| b5ccg/probe-b5-0956.txt | DELETE after this report (content fully quoted) — misplaced in the source tree; re-runnable probes |
| b50970/nps/trc .diff (3) | KEEP + attribute via this proposal (unique +side blobs) |
| 19 .pl instruments | KEEP + attribute (only surviving record of the 09-29 ledger surgery mechanics; git history does not cover it) |
| 4 EVIDENCE units | KEEP, untracked, covered by the proposed `/tmp-scans/` line |

Net if adopted as proposed: 5 files / ~35 kB deleted, 24 files + 1 dir kept
under one rule, `b5ccg/` source tree returned to source-only.
