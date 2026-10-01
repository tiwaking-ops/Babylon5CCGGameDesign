---
document:
  title: "B5-1531 close-out — OWNER-marker coverage census on added lines only"
  status: "Report — observation only, no authority"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1531
method_source: docs/proposals/hunk-ownership-marker-proposal.md (B5-1465, DONE)
---

# B5-1531 — OWNER-marker coverage on added lines only

**Scope honoured:** read-only. No src, suite, harness, data, ledger-row or
coordination file was edited except my own ledger row, my own report, my own
pattern and my own heartbeat. No hunk was retagged. No commit, no push.

## Method (taken, not re-derived)

Per B5-1465 §*The triage procedure this enables*, with the B5-0755 added-lines-only
rule: for each hunk of `git diff -U0`, collect the row ids appearing on **added**
lines only, then classify zero / singleton / multi. Context lines are excluded
because they carry the old file's comments and would attribute new bytes to
unrelated old tasks.

Two axes are reported because they answer different questions:

- **Strict axis** — `/OWNER:\s*(B5-\d{4}[a-z]?)/`, the reserved token B5-1465
  proposes. This is the procedure as specified.
- **Baseline axis** — any `/B5-\d{4}[a-z]?/` on an added line, i.e. presence of a
  row id in any comment form. This is the axis B5-1465 measured its 73%/33%
  headline on, so it is the comparable series.

## One methodology correction, self-caught

The first run passed `-c core.autocrlf=false -c core.safecrlf=false` to silence
git's CRLF warnings. That flag **rewrites the diff**: it turned a 49-hunk,
1251-added-line tree into a 66-hunk, 18972-added-line tree across 50 files,
because with autocrlf disabled git compares bytes and every LF/CRLF difference
becomes a whole-file rewrite. The inflated numbers were discarded, not reported.
The published measurement is from an unflagged `git diff -U0`, cross-checked
against `git diff --stat` (8 files, 1251 insertions, 24 deletions) and
`git status --porcelain` (8 modified, 2 untracked probes).

The untracked probes (`B51047ConflictTypeProbe.java`, `BioWeaponProbe.java`) are
excluded: an untracked file has no diff hunk and therefore no `git diff` output
to label.

## Measurement

Tree at claim time (2026-10-01T04:39:42Z tool-stamped claim): **8 modified
`b5ccg/src/` files, 49 hunks, 1251 added lines.**

| File | Hunks | Added | ≥1 row id | 0 | exactly 1 | >1 |
|---|---|---|---|---|---|---|
| `ai/AIPlayer.java` | 16 | 153 | 12 | 4 | 10 | 2 |
| `ui/MainWindow.java` | 14 | 276 | 9 | 5 | 2 | 7 |
| `engine/CardEffects.java` | 5 | 150 | 4 | 1 | 0 | 4 |
| `engine/HeadlessConformanceTest.java` | 4 | 465 | 4 | 0 | 3 | 1 |
| `engine/DeckLoader.java` | 4 | 162 | 3 | 1 | 1 | 2 |
| `engine/GameController.java` | 2 | 33 | 2 | 0 | 2 | 0 |
| `ui/GameBoardPanel.java` | 2 | 5 | 1 | 1 | 1 | 0 |
| `engine/RulesEngine.java` | 2 | 7 | 2 | 0 | 1 | 1 |
| **total** | **49** | **1251** | **37 (75.5%)** | **12 (24.5%)** | **20** | **17** |

### Strict axis: `OWNER:` adoption is zero

**49 of 49 hunks are `UNLABELLED` on the strict axis.** Not one `OWNER:` marker
exists anywhere in the dirty tree. This is the expected and correct result — the
convention is a proposal, its adoption step is a *separate, later, claimed* task
(B5-1465 §*What adoption looks like* step 1), and nothing in this row's scope
could have produced one. The strict number is reported because it is what the
proposed procedure would emit today, and it is the number a future triage tool
would print: **100% unlabelled**.

### Baseline axis: the comparable series

| Quantity | B5-1465 baseline (2026-10-01) | This pass (04:39Z) | Δ |
|---|---|---|---|
| Files | 7 | 8 | +1 |
| Hunks | 45 | 49 | +4 |
| Added lines | 792 | 1251 | +459 |
| ≥1 row id | 33 (73%) | 37 (75.5%) | +2.5 pts |
| 0 row ids | 12 (27%) | 12 (24.5%) | −2.5 pts |
| >1 row id | 15 (33%) | 17 (34.7%) | +1.7 pts |

Read together: coverage improved marginally, the unlabelled backlog is
**unchanged at exactly 12 hunks**, and the ambiguous share of labelled hunks rose
from 45% (15/33) to **45.9% (17/37)**. B5-1465's central claim — that a presence
test is green on the cases it was meant to catch — is confirmed on live data, not
merely asserted: nearly half of the labelled hunks still cannot say which row
owns them.

The +459 added lines and +1 file are all `engine/HeadlessConformanceTest.java`
(4 hunks, 465 added lines vs B5-1465's 13 added), which was mid-flight when the
proposal measured. Its own coverage is 4/4 labelled, 3 single + 1 multi.

### File with the most unlabelled hunks

**`ui/MainWindow.java` — 5 of 14 (35.7%).** `ai/AIPlayer.java` is second with 4 of
16 (25%). Note the divergence: `AIPlayer.java` has the most hunks *and* the most
absolute coverage (12/16 labelled), while `MainWindow.java` has the second-most
hunks and the worst coverage rate. A ranking by hunk count and a ranking by
unlabelled count are different lists.

## Classified backlogs

### Unlabelled hunks (12) — 5 are mechanical, 7 need forensic attribution

B5-1465 explicitly exempts mechanical edits ("a pure import, a pure reformat, or
a build-tool artefact carries no marker and is reported as such"), so this census
classifies them rather than leaving them as 12 undifferentiated items:

| File | Hunk header | Added | Classification |
|---|---|---|---|
| `ui/GameBoardPanel.java` | `@@ -2,0 +3 @@` | 1 | **MECHANICAL** — pure `import b5ccg.model.CivilWarState;` |
| `ui/MainWindow.java` | `@@ -1232,2 +1291,0 @@` | 0 | **MECHANICAL** — pure deletion, zero added lines; carries nothing by construction |
| `ui/MainWindow.java` | `@@ -1255 +1342 @@` | 1 | **MECHANICAL** — comment repair, `Ã¢â‚¬â€ï¼` → `not` (mojibake fix) |
| `engine/DeckLoader.java` | `@@ -267,2 +270 @@` | 1 | **FORENSIC** — `ConflictType.valueOf(...)` replaced by `parseConflictType(m, id)` |
| `ui/MainWindow.java` | `@@ -1235 +1293 @@` | 1 | **FORENSIC** — `if (ch == null \|\| hp == null)` → `if (hp == null)`; belongs to the `ch` null-guard refactor visible in the two sibling hunks below it |
| `ui/MainWindow.java` | `@@ -1246 +1333 @@` | 1 | **FORENSIC** — adds `ch != null &&` to hand containment |
| `ui/MainWindow.java` | `@@ -1251 +1338 @@` | 1 | **FORENSIC** — adds `ch != null &&` to supporting-role containment |
| `ai/AIPlayer.java` | `@@ -771 +866,7 @@` | 7 | **FORENSIC** — surrender scoring gains `unrestPressure(p)` and a civil-war −8 |
| `ai/AIPlayer.java` | `@@ -847 +957,2 @@` | 2 | **FORENSIC** — major-proximity term gains `raceInCivilWar ? 8.0 : 0.0` |
| `ai/AIPlayer.java` | `@@ -970 +1095,7 @@` | 7 | **FORENSIC** — hard-surrender mirror of `@@ -771` |
| `ai/AIPlayer.java` | `@@ -1044 +1175 @@` | 1 | **FORENSIC** — `getInfluence() / 2` → `getPower() / 2` |
| `engine/CardEffects.java` | `@@ -1087 +1228 @@` | 1 | **FORENSIC** — closing brace of `isBannedByAgenda`, added by the B5-1089/B5-1032/B5-1045 cluster in the same file |

The four `AIPlayer.java` forensic hunks are almost certainly **B5-0727** (Civil
War context). Evidence, not inference from style: the *adjacent* hunks in the
same file that touch the same scoring functions carry explicit
`// B5-0727: Civil War context (:990–:1009) — unrest pressure adds ...` comments,
including one immediately after `@@ -771`. The hunks are the un-commented
continuation of one labelled edit. This is exactly the ambiguity B5-1465
predicted: the owner is recoverable by proximity, but only by a human reading the
file — the presence test says nothing.

So the real backlog is **7 forensic hunks, not 12**, and `MainWindow.java` holds 4
of the 7.

### Multi-row-id hunks (17) — the conflict backlog under the proposed procedure

Under B5-1465's procedure these are `CONFLICT`: two agents' work in one hunk,
needing a split before attribution. In practice most are **citations of
contracts**, not co-ownership — `engine/CardEffects.java` is 4 of 4 multi, and its
id sets (`B5-1089,B5-1032,B5-1045` / `B5-1045,B5-1032` / `B5-0990,B5-0741,B5-1051`
/ `B5-1045,B5-0364`) are the same card-effect family built by a small number of
rows over time, not four unrelated agents.

| File | Multi count | Distinct id sets |
|---|---|---|
| `ui/MainWindow.java` | 7 | `B5-0701,B5-0677,B5-0977,B5-0691,B5-0715`; `B5-0701,B5-0661`; `B5-0701,B5-0407`; `B5-1049,B5-0326,B5-1038`; `B5-0701,B5-0423,B5-0452`; `B5-1171,B5-1177,B5-1038`; `B5-1171,B5-1109` |
| `engine/CardEffects.java` | 4 | `B5-1089,B5-1032,B5-1045`; `B5-1045,B5-1032`; `B5-0990,B5-0741,B5-1051`; `B5-1045,B5-0364` |
| `ai/AIPlayer.java` | 2 | `B5-0727,B5-0691,B5-0669,B5-0351`; `B5-0703,B5-0351` |
| `engine/DeckLoader.java` | 2 | `B5-1301,B5-0556`; `B5-1055,B5-1025` |
| `engine/HeadlessConformanceTest.java` | 1 | `B5-0990,B5-0741,B5-1051,B5-1045,B5-1032,B5-0364,B5-1089` |
| `engine/RulesEngine.java` | 1 | `B5-0990,B5-1051,B5-1045,B5-1089` |

**The B5-1089/B5-1032/B5-1045/B5-0990/B5-0741/B5-1051/B5-0364 family spans four
files** (`CardEffects`, `DeckLoader`, `HeadlessConformanceTest`, `RulesEngine`) —
one logical feature whose citations land in both implementation and its test. A
split-by-hunk rule applied naively would fragment one feature across four files
on the grounds that a test cites its implementation.

This is a real weakness in B5-1465's `CONFLICT` branch, and it is reported rather
than re-adjudicated: **"more than one row id on an added line" does not mean
"more than one owner"**, because the fleet's convention is to cite the contract a
change honours inside the comment that describes the change. B5-1465's own §*The
rule* anticipates this — `OWNER:` is a reserved token precisely so a citation
never looks like an owner — but its stated procedure has no branch for the
pre-adoption corpus, where no `OWNER:` token exists and every id is a citation or
an owner by guesswork. A future adoption pass should retag by hand and let the
strict axis be the gate, which is precisely B5-1465 step 2.

## Fence and gates

- **B5-1443 fenced.** It owns the `canPlayCard` caller census across ui/engine/ai.
  This row touched no call graph; no caller was enumerated. Not duplicated.
- **B5-1465 read first, method not re-derived.** The classification procedure, the
  added-lines-only rule, the mechanical exemption, the three verdicts and the
  presence-test critique are all taken from the proposal verbatim; only the
  current-tree numbers are new.
- **Build gate:** `b5ccg\compile.bat` green on JDK 1.8.0_292,
  `-source 1.6 -target 1.6`, `Build successful`, exit 0. One pre-existing
  bootstrap warning (`[options] bootstrap class path not set in conjunction with
  -source 1.6`), which is the standing expected text.
- **No retagging.** B5-1465 permits it at zero code cost, but it is not this row's
  scope and it would make the census un-reproducible.

## Reusable lesson

**A measurement tool's own flags can define the thing being measured.**
`core.autocrlf=false` was added to silence a warning and turned 49 hunks into
18972 added lines across 50 files — a 15x inflation that every downstream
percentage would have inherited silently, since the run still *looked* like a
clean census with plausible per-file tables. The tell was not a crash but a
number that disagreed with `git status`: 50 dirty files where `git status` listed
8. Cross-check any diff-derived census against an independent source
(`git diff --stat`, `git status --porcelain`) before publishing; and when a
tolerance flag is added purely to reduce noise, ask whether it changes what
`git diff` *is* before trusting the result.