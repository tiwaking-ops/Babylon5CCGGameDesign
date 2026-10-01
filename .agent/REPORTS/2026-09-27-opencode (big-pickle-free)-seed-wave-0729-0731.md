---
document:
  title: "Seed wave 0729..0731 — B5-0660 decision item re-measured, two rows seeded"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
  note: "Meta-seeding pass on user order 'seed both'. No claim held, no src or resources edit, no commit."
---

# Seed wave 0729..0731 — opencode (big-pickle-free) — 2026-09-27

**Reusable lesson:** a decision request is a snapshot of a queue, not a standing question —
re-measure every predicate it rests on before ruling, and when the state has moved, answer
the state rather than the question.

## What was ordered, and what I did about it

The human-facing decision recorded in the B5-0713 close-out was quoted back to me verbatim:
the B5-0660 implementation sat in the tree as uncommitted changes while its row read OPEN,
unclaimed and un-offered, so the runner was offering a task whose work already existed, and
closing it "means deciding whether that uncommitted work is authoritative". The order was
`seed both` — seed the two follow-on rows I had proposed rather than rule on it myself.

The first thing this pass did was **re-measure the premise instead of seeding on top of it.**

## The premise had already resolved itself, and not by a decision

| Predicate in the quoted finding | Reading at seed time (10:2xZ) |
|---|---|
| `B5-0660` row OPEN, unclaimed, no report | **DONE** at `.agent/TASK_LEDGER.md:815`; claim file absent from `.agent/CLAIMS/`; report on disk |
| 132 uncommitted insertions in `run-queue.ps1` | **committed** at `042c3592` (the B5-0689 checkpoint); `git diff` clean for that file |
| runner offers it as head claimable row | head is **B5-0687**; 15 claimable OPEN after my two rows, `-DryRun` exit 0 |

So: me-so-poor closed the row on the normal cycle, and Buffy's B5-0689 checkpoint swept the
bytes in. `git show HEAD:.agent/run-queue.ps1` carries the B5-0660 triad with its tagged
lines 289 to 397. **The harm window closed without anyone ruling on authority**, which means
the ruling I was asked for has no subject any more. I did not write it: a decision record
about a state that no longer exists is authority from a timestamp, which `AGENTS.md` §3
forbids in as many words. Re-claiming B5-0660 to "make it official" would have redone landed
work — the B5-0711 close.

## What was still true, and is now seeded

Two things survived the re-measurement. Both were measured, not cited.

**1 — The B5-0660 record contradicts itself on PART 3 → `B5-0729`.**

* report line 3: `census-crosscheck.ps1` reconciled by being *read and not edited*, B5-0653 scope;
* `docs/DECISIONS.md:4905`: the report signal was *added* at lines 164 to 173;
* the committed file: a self-labelling `# B5-0660 reconcile` comment sits directly above that hunk.

Two records and the landed code say edited; one four-line report says not, and a reader has no
tiebreaker. The correction therefore goes in a **new** record — the report is not edited,
per supersede-never-rewrite.

**2 — Four scratch scripts were committed as if they were deliverables → `B5-0731`.**
`.agent/tmp-b5660-diag.ps1`, `tmp-b5660-harness.ps1`, `tmp-diag2.ps1`, `tmp-diag3.ps1` all
landed in `042c3592`, whose row said *commit everything EXCEPT CLAIMS and HEARTBEATS* — an
instruction with no way to tell a scratch file from a deliverable. A search for all four
filenames outside `.git` returns **zero** references, so the three harness assertions that
*are* the B5-0660 acceptance criteria are evidenced by scripts no row or report points at.
Attribution is uneven inside them: `tmp-b5660-harness.ps1` line 1 names **Buffy
(glm-5.3-flash)** as author while the row owner is me-so-poor, and the two `tmp-diag` scripts
carry no header at all. That is a forensics gap, not a style one — a harness nobody can
attribute cannot be re-run by whoever inherits the row.

## Verification, with the census that moved mid-pass disclosed

Shared tools only, nothing hand-rolled, on the exact working tree:

* `run-queue.ps1 -DryRun` — exit 0, no warnings, head B5-0687, 15 claimable OPEN after seeding (14 before).
* `ledger-query.ps1 -Status OPEN` — exit 0; `B5-0729` and `B5-0731` each read **7 pipes / doubleLead no / UNCLAIMED / reportable**; 16 OPEN rows.
* duplicate-ID census (00_BOOT step 9) — **empty**, the pass condition.
* `validate-heartbeats.ps1` — **exit 1 at seed time**: 29 files, 28 conforming, 1 non-conforming (`me-so-poor.json`, B5-0717's scope, untouched by me), 28 distinct agent_ids, 0 collisions. Re-run after my own heartbeat write: **exit 0, 29 of 29 conforming, 0 collisions** — its owner repaired it mid-pass. Recorded as two readings rather than the tidier final one.
* `git diff` clean for `.agent/run-queue.ps1`, `.agent/tools/census-crosscheck.ps1`, `.agent/00_BOOT.md`.
* Ledger bytes: 756013, **889 LF / 0 CRLF**, no BOM — the append introduced no line-ending drift.

**Census moved mid-pass, disclosed rather than smoothed:** the first `ledger-query` read listed
`B5-0689` and `B5-0695` as the two suppressed-live-claim rows; after my append it listed
`B5-0687` and `B5-0695`. `B5-0689` is now `DONE` — Buffy's checkpoint row closed between my two
reads. The claimable count moved 14 → 15, not 14 → 16, for the same reason. Neither row is mine.

## Governance disclosures

* **No claim held** for this pass, per the standing multi-wave convention of a QUEUE note plus
  a report rather than a self-seeded row. `AGENTS.md` §6 is satisfied: both rows are OPEN and
  unclaimed, which is what a seed is.
* **No commit.** B5-0721 is the open checkpoint row; committing is its scope, not this pass's.
* **No tool edit.** The two seeded rows are docs-only by construction; this pass touched the
  ledger and nothing else.
* **IDs:** `B5-0729` and `B5-0731` pre-checked free at zero matches each, continuing the odd
  spacing of the 0715–0727 wave so a concurrent even-sequence seeder cannot collide. Had the
  post-write census found a collision I would have diverged to non-adjacent IDs and left the
  other writer's row byte-identical (B5-0711's repair).
* **Clock disclosure, B5-0653 class:** my previous heartbeat recorded `utc 2026-09-27T10:26:00Z`
  while the measured clock read `10:23:50Z` on this pass, so that earlier timestamp is ~2 min
  future-dated. I wrote the **measured** value, which moves my own heartbeat backwards. Going
  backwards is correct here: a future-dated heartbeat is a known liveness hazard, and mtime —
  which the heartbeat README names the more reliable signal — updates either way.
* **Nothing of anyone else's was touched:** no foreign row, claim, heartbeat, report or pattern
  file was edited, and `me-so-poor.json` was left non-conforming for its own row to repair.

**Artifacts:** rows `B5-0729`, `B5-0731` plus the `QUEUE 0729..0731` note in
`.agent/TASK_LEDGER.md`; this report; pattern
`.agent/PATTERNS/opencode (big-pickle-free)/2026-09-27-a-decision-item-is-a-snapshot-not-a-standing-question.md`.
