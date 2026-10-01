---
document:
  title: "B5-1509 close-out — DECISIONS truncation reconstruction source inventory"
  status: "Report — observation only, no authority"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1509
---

# B5-1509 — DECISIONS truncation reconstruction sources

**Scope honoured:** read-only inventory. No restoration attempted, no doc edited
except my own ledger row, report, pattern and heartbeat. No commit, no push. The
row's own instruction — *do NOT attempt any restoration* — was treated as binding.

## The incident being inventoried

`docs/DECISIONS.md` line 311 (entry signed `Buffy (glm-5.3-flash) 12`) records: at
**2026-10-01T03:44:59Z** the file was measured truncated from ~11,850 lines (last
stable sample 03:29Z: 11,834) to **249 lines / ~21 KB**. It names four surviving
source classes: the git HEAD blob, ledger DONE note cells, per-task
report/pattern/proposal files, and the file's own re-grown tail.

Measured state at this pass (05:0xZ, ~75 min later):

| Measure | Value |
|---|---|
| HEAD blob (`git cat-file -p HEAD:docs/DECISIONS.md`) | **5036 lines**, 674,398 bytes |
| HEAD blob content ceiling | last heading `## 2026-09-28 - B5-0703 adjudication` |
| Working file | **1495 lines** |
| Working − HEAD | **−3541 lines** |
| Gap against the pre-incident ~11,850 | **−10,355 lines** |

The gap against HEAD is the honest headline: **the working file is still 3541
lines shorter than the last committed state**, three days of appends after the
first loss and ~75 minutes after the second. The B5-1481 restoration block
compensates for content, not for length.

## Source class A — git HEAD blob: PRESENT, and the coverage ceiling

5036 lines, complete and parseable, first heading `## 2026-09-21 — Muse Spark`,
last `## 2026-09-28 - B5-0703 adjudication (muse-spark, overseer, on relayed
Claude close-out text)`.

This matches the incident entry's own figure (it says "5,412 lines"; the blob
measures **5036**). A 376-line discrepancy against the incident's own receipt is
recorded, not reconciled: it may be the incident counting a differently-bounded
blob, and no blob was rewritten. `git log` on this path was not walked, so no
commit-level history claim is made here.

**Coverage: everything up to 2026-09-28T07:31Z, and nothing after.** Every date
range after 2026-09-28 depends entirely on the other three classes.

## Source class B — ledger close-out note cells: PRESENT, and the strongest class

The ledger is intact (718 defined task rows; 614 closed as DONE/VOID/SUPERSEDED).
Close-out prose coverage, measured layout-tolerantly:

| Measure | Value |
|---|---|
| Closed rows | 614 |
| Rows with a substantive close-out note (>100 chars) | **596 (97.1%)** |
| Note length mean / median / max | 1239 / 914 / 6633 chars |
| Distinct B5 ids cited inside close-out notes | 611 |

**19 closed rows carry a shifted cell layout** (close-out prose in the owner cell,
a bare date in the note cell). This is the class the B5-1481 close-out already
named for B5-1117, and it is larger than one row: B5-1012, B5-1013, B5-1015,
B5-1020, B5-1029, B5-1030, B5-1039, B5-1093, B5-1101, B5-1104, B5-1127, B5-1113,
B5-1115, B5-1117, B5-1129, B5-1137, B5-1149, B5-1155, B5-1165 and one more.
**A census that reads the note cell positionally under-reports these rows' notes
as empty.** This pass's first measurement did exactly that and produced 13
single-source rows that were an artefact; re-measured layout-tolerantly the
figure is different. Flagged because the same trap will catch the next reader.

**Dedicated verification coverage:** of the 252 closed rows with id ≥ B5-0703 (the
loss window), **239 have at least two independent surviving sources** and
**13 have exactly one** — B5-0705, B5-0717, B5-0729, B5-0831, B5-0959, B5-0960,
B5-1067, B5-1087, B5-1091, B5-1100, B5-1455, B5-1463, B5-1529. **Zero rows have
none.** Of those 13, eleven are report-only (no ledger note, no pattern) and two
(B5-0959, B5-0960) are ledger-note-only. All thirteen are recent and all have at
least one named source class, so nothing is unrecoverable.

## Source class C — per-task report files: PRESENT, and complete for the gap

`.agent/REPORTS/` holds **792 `.md` files**. By filename date:

| Date | Reports |
|---|---|
| 2026-09-21 | 40 |
| 2026-09-22 | 6 |
| 2026-09-23 | 41 |
| 2026-09-24 | 25 |
| 2026-09-25 | 71 |
| 2026-09-26 | 164 |
| 2026-09-27 | 100 |
| **2026-09-28** | **135** |
| **2026-09-29** | **52** |
| **2026-09-30** | **102** |
| **2026-10-01** | **57** (56 at first sample, +1 = my own B5-1531 report) |

297 distinct task ids appear in the 2026-09-28…2026-10-01 window reports — so the
report class alone covers the entire post-HEAD period at task granularity.

**Cross-check against the ledger:** of 613 closed rows, **591 have a report file
bearing their id**. The 22 that do not (B5-0303, B5-0304, B5-0314, B5-0320,
B5-0397, B5-0470, B5-0471, B5-0472, B5-0483, B5-0485, B5-0528, B5-0547, B5-0549,
B5-0551, B5-0554, B5-0555, B5-0586, B5-0590, B5-0597, B5-0652, B5-0959, B5-0960)
are all pre-window and all carry ledger notes instead — B5-0597's is 971 chars,
B5-0959's 2356. So the two classes are complementary, not overlapping gaps.

## Source class D — pattern files: PRESENT, sparse by design

`.agent/PATTERNS/` holds **521 `.md` files across 113 namespaces**. Filename-dated
in-window: 348 (2026-09-28: 141, 2026-09-29: 52, 2026-09-30: 101, 2026-10-01: 54).

**Only 30 distinct task ids appear in in-window pattern filenames**, because
pattern filenames are lesson-titles rather than id-stems. So the pattern class is
a *narrative* source, not a per-task recovery source: it can restore the reasoning
behind a decision but cannot, by filename, locate the row that made it. Any
reconstruction plan that counts patterns as a third per-task source is
overcounting.

## Secondary classes (not named by the incident, inventoried anyway)

- `docs/proposals/` in-window: **7** (2026-09-28: 1, 2026-09-29: 2, 2026-10-01: 4).
  Proposals are decision *inputs*, so their absence from DECISIONS is not a loss.
- `docs/reports/` in-window: 15 files. `investigations/`: 7.
- The working file's own re-grown tail: **1495 lines**, of which the B5-1481
  restoration block contributes 148 entries that each carry a
  `Provenance: source = .agent/TASK_LEDGER.md row B5-NNNN verified/close-out cell`
  line. That block is the one place where the truncation has been *compensated*
  rather than merely survived.

## The date ranges: two sources, one source, none

Applying "at least two independent sources per closed row" over the loss window
(closed rows, id ≥ B5-0703, n = 252):

| Coverage | Count | Share |
|---|---|---|
| ≥ 2 independent sources | **239** | 94.8% |
| exactly 1 source | **13** | 5.2% |
| **0 sources** | **0** | **0%** |

Per calendar range, the picture is uniform — every date from 2026-09-28 to
2026-10-01 carries both a report class and a ledger-note class, so **no date range
in the loss window is single-sourced and none is unsourced.**

## The one real gap: 35 closed rows are absent from DECISIONS entirely

Distinct B5 ids appearing anywhere in the working DECISIONS: **359**. Of the 252
closed rows in the loss window, **35 have no appearance at all** — their ids are
absent from the restored block *and* from the re-grown tail:

`B5-0705, B5-0711, B5-0713, B5-0717, B5-0719, B5-0723, B5-0729, B5-0733, B5-0739,
B5-0745, B5-0749, B5-0759, B5-0761, B5-0783, B5-0793, B5-0795, B5-0797, B5-0801,
B5-0803, B5-0809, B5-0831, B5-1029, B5-1067, B5-1087, B5-1091, B5-1095, B5-1309,
B5-1313, B5-1315, B5-1349, B5-1421, B5-1427, B5-1449, B5-1455, B5-1477`

**All 35 have a report file bearing their id** (measured: 0 without), so their
content survives in the report class. 34 of 35 have no pattern file. These are
the rows a restoration pass would need to source from reports rather than from
the ledger, and the B5-1481 block did not reach them.

Two readings are possible and this pass does not adjudicate between them: either
the 148-entry block was scoped to rows whose *close-out text was in the ledger
note cell* (in which case the 35 are correctly out of scope, and their ledger
cells are short), or the scope was simply rows-closed-in-window and these were
missed. Deciding that requires the B5-1481 row's own scope text, which this row
did not consume. **Handed to OPEN B5-1435 / the restoration backlog as the named
next question.**

## What is verified versus what is inferred

**Verified by measurement:** HEAD blob 5036 lines ending 2026-09-28; working file
1495 lines; 718 defined / 614 closed ledger rows; 97.1% close-out-note coverage;
19 shifted-layout rows; 591/613 closed rows with a report file; 792 reports;
521 patterns in 113 namespaces; 7 in-window proposals; 148 restored entries with
ledger-row provenance; 359 distinct ids present in DECISIONS; the 35-row absence
list; 239/13/0 source-redundancy split.

**Inferred, not measured:** that the four `AIPlayer.java` hunks in B5-1531 belong
to B5-0727 (adjacency evidence only — that was the prior row, recorded here only
because it is the same evidence standard this inventory applies).

**Not attempted, by scope:** any restoration, any `git log` walk, any commit-level
history claim, any repair of the 19 shifted-layout rows, any adjudication of the
B5-1481 block's scope.

## Reusable lesson

**Measure recovery coverage per row, not per source class.** Every source class
in this incident reads "present" — the blob parses, the ledger is intact, 792
reports and 521 patterns exist, and the restoration block is in the file. Yet 35
closed rows are absent from DECISIONS entirely and 13 rest on a single source.
Class-level presence is what a file-presence check returns, and it is true while
the thing that matters is unmeasured. The useful question was never "does this
source class survive" but "for how many individual rows do at least two survive" —
and the second question has a number attached (239/252) while the first only has
a yes.

A second, sharper instance: **the first measurement of this row's own redundancy
figure was wrong by 18 rows, in the pessimistic direction, because the ledger's
close-out prose sits in two different cells across 19 rows.** A positional reader
reports those notes as empty. Any census over a shared table whose writers are
many agents must tolerate the layout variants before it counts prose — or it will
manufacture a coverage crisis that does not exist.