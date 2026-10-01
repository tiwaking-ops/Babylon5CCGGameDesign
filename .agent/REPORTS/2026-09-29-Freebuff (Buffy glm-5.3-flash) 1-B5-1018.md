---
document:
  title: "B5-1018 close-out — the BLOCKED rows, aged, classified, and one mechanism"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1018 — The BLOCKED queue: expiry and reason taxonomy

Instrument (named): Python decode of `.agent/TASK_LEDGER.md` utf-8 strict;
BLOCKED rows = lines matching `^\| B5-\d+ \| *BLOCKED`; verdict cell parsed for
the block-time UTC date and the named prerequisites; prerequisites re-checked
against their current rows. Measured 2026-09-29T09:07Z. Read-only: no row
unblocked, edited, closed, or deleted; B5-0990/B5-0969 work not performed.

## Part 1 — the inventory (17 rows; the row's 18 included B5-0990, now DONE)

| Row | Blocked (UTC) | Age | Stated reason / named prereq | Prereq now satisfied? |
|---|---|---|---|---|
| B5-0681 | 09-27 08:48 | 2.0d | scope gate red: B5-0677/B5-0679 live claims | YES (both DONE) |
| B5-0687 | 09-27 09:47 | 2.0d | prereq letters: B5-0681 BLOCKED, B5-0683 superseded-by-letter | STALE LETTERS (0683 superseded by 0695's letter) |
| B5-0695 | 09-27 10:30 | 2.0d | compile red from foreign B5-0661 concurrent edits | YES (0661 DONE) |
| B5-0697 | 09-27 10:45 | 2.0d | prereq B5-0695 BLOCKED + same external compile red | NO (0695 still BLOCKED) |
| B5-0699 | 09-28 10:20 | 0.9d | tree-sweep vs LIVE foreign claims (B5-0939/B5-0945) + 526 dirty foreign src lines; gate itself measured GREEN | claims since released; foreign dirty bytes unattributed |
| B5-0721 | 09-27 11:32 | 1.9d | prereqs 0695 BLOCKED, 0727/0715 OPEN | 0727/0715 DONE; 0695 still BLOCKED |
| B5-0725 | 09-27 14:56 | 1.8d | prereqs 0697 BLOCKED, 0727 OPEN | 0727 DONE; 0697 still BLOCKED |
| B5-0737 | 09-28 09:48 | 1.0d | in-flight foreign claims over sweep bytes; gate measured GREEN | claims since released; bytes still unattributed |
| B5-0753 | 09-28 07:58 | 1.1d | prereq B5-0751 reads VOID, not DONE | NO — permanently red letter |
| B5-0763 | 09-28 05:41 | 1.1d | prereq B5-0697 BLOCKED | NO (0697 still BLOCKED) |
| B5-0799 | 09-28 06:36 | 1.1d | prereqs B5-0699 + B5-0737 both not DONE | NO (both still BLOCKED) |
| B5-0811 | UNKNOWN (≈09-28) | UNKNOWN | census-crosscheck exits 1 DIVERGENT, 2 disagreements / 432 rows, B5-0751-baseline persisting; row carries no date and an anomalous cell layout | NO (divergence structural) |
| B5-0947 | 09-28 | 1.0d | prereq B5-0939 OPEN with no report/pattern (8-way batch conjunction) | YES (0939 now DONE) |
| B5-0961 | 09-28 | 1.0d | gate: claim only after B5-0947 DONE (declared permanently not-claimable) | transitively YES (0947's blocker gone) |
| B5-0979 | 09-28 20:18 | 0.5d | gate: no live claim on docs/DECISIONS.md (B5-0977 held one) | YES (claim gone) |
| B5-0983 | 09-28 20:31 | 0.5d | gate: B5-0953 engine claim released (it held a corrupt future-dated claim) | YES (0953 DONE, claim gone) |
| B5-1001 | 09-29 | 0.2d | its own rehearsal gate: 72/77 lines truncation-killed, needs authorised character mapping | NO (human decision; my B5-1027 measured 6 lines machine-repairable) |

## Part 2 — the taxonomy (the classes the repo already has)

At block time: **GATE-SHUT 4** (0681, 0961, 0979, 0983) · **PREREQ-RED 9**
(0687, 0697, 0721, 0725, 0753, 0763, 0799, 0811, 0947) · **SCOPE-BLOCKED 4**
(0695, 0699, 0737, 1001) · **ACTION-READY 0**.

As of today: **ACTION-READY 7** (0681, 0695, 0721, 0947, 0961, 0979, 0983 —
every stated blocker gone; the ready action is a re-verify at re-claim) ·
**GATE-SHUT 6** (0687, 0697, 0725, 0753, 0763, 0799 — a still-BLOCKED or VOID
prereq row) · **SCOPE-BLOCKED 3** (0699, 0737 — unattributed foreign bytes
under a tree-sweep letter; 1001 — human decision) · **STRUCTURAL OUTLIER 1**
(0811 — no date, no owner, anomalous cells; unclassifiable by any mechanical
reading, UNKNOWN never STALE).

The row's thesis is confirmed and quantified: ACTION-READY is 0% of the class
at block time and **41% (7 of 17) two days later**. The class is invisible
when it is created and dominant before anyone looks — that is the mechanism
by which a queue becomes a graveyard. (Block-time ACTION-READY = 0 is also
why no per-block taxonomy can catch it: the class only exists later.)

## Part 3 — the one mechanism (and its cost)

**Chosen: a required prerequisite token convention in the verdict cell** —
every future BLOCKED verdict begins `BLOCKED on B5-xxxx[,B5-yyyy]; class
GATE-SHUT|PREREQ-RED|SCOPE-BLOCKED|HUMAN`. The runner (or the close-out
census) then re-reads the named rows: a BLOCKED row whose every named
prerequisite now reads DONE (or whose blocker is verifiably gone) is surfaced
as **ACTION-READY** in the offer path — no status cell changes hands until an
agent re-claims and re-verifies, which keeps step 6/B5-0622 discipline intact.

Cost, honestly stated: one migration sweep over the 17 existing rows to add
tokens (a normal seed wave — the cells already name their prerequisites in
prose; this only makes them parseable), one 00_BOOT sentence, and one census
addition. It also has a genuine casualty the expiry proposal does not:
**B5-0811 cannot be migrated mechanically** (its verdict sits in the wrong
cell and carries no date) — the migration row must hand-repair it, which is
exactly the kind of hidden cost this row exists to surface.

**The rejected alternative — an N-day expiry surfacing old BLOCKED rows in the
offer path — costs less to build and more forever:** it resurrects rows by
calendar, and the calendar is precisely the wrong signal here. B5-1001 (0.2d)
is blocked on a human decision and would age into resurfacing while still
correctly blocked; B5-0947 was ACTION-READY within a day while a 5-day expiry
would have slept through its whole useful window. Age found 0 of the 7 ready
rows; prerequisite re-reading found all 7. Recommendation: the prerequisite
token, with the runner surfacing as a *report line first* and an offer-lane
change only if a human wants the queue pushed.

## Gates

No row unblocked/edited/closed; B5-0990/B5-0969 untouched; no src/data edits;
claim released at close-out; no commit, no push.

## Reusable lesson

A BLOCKED row's status is a fact about the day it was written — the class it
belongs to drifts under it as the tree moves, and the drift is one-directional
toward ACTION-READY, which is why a queue's graveyard grows silently;
re-read the named prerequisites, not the row, to know what a BLOCK is now.
