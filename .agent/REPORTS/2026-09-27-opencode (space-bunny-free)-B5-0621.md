---
document:
  title: "B5-0621 seed report — queue census, pipe-class seed, and a live two-writer ID collision"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0621 — seed wave 11 close-out

User order: "verify repo. list open tasks", then approval to seed one task.
Toolchain: `javac 1.8.0_292` (recorded in the claim and heartbeat).

## 1. Repo verification

* `b5ccg/compile.bat` — **Build successful**, `-source 6 -target 6`, only the
  expected `bootstrap class path not set` warning. Seventh consecutive green.
* Census run three ways, all concurring that the queue was empty:
  * `.agent/run-queue.ps1 -DryRun` → `Queue drained`.
  * `.agent/tools/ledger-query.ps1` (default `OPEN`) → `No rows match status filter 'OPEN'`.
  * Independent tolerant row scan → 317 rows = 309 DONE, 6 SUPERSEDED, 2 VOID,
    0 OPEN, 0 CLAIMED, 0 BLOCKED, and **no row with an unreadable status cell**.
* Two orphaned claim files, `B5-0577.json` and `B5-0587.json`, both on tasks
  already `DONE`, both ~120 min old against a 30 min TTL. **Not reaped** — per the
  filed B5-0597 lesson, liveness is the newest of claim / heartbeat / report, and
  reaping is the one shared-state deletion that hands a live task to a second
  claimant. Removing them is a cleanup, not a seeding duty.

## 2. Discovery run *before* seeding — it changed the answer twice

Both candidate seeds were killed by measurement, which is the point of measuring first:

* **Damage / neutralization subsystem** — B5-0345 recorded this as "the deepest
  gap, gating attack/heal/repair". It has since landed (B5-0370 / B5-0371).
  `AIPlayer.java:203` carries the subsystem header and `canHealCharacter` /
  `canRepairCard` are live. Seeding it would have been pure waste.
* **Sustained Actions** (rulebook §III, its own section at :352) — the string
  `sustained` occurs **0 times** across `b5ccg/resources/cards/*.json`. A rulebook
  mechanic with no card data is data-gated, not implementable.
* **Action-surface coverage** — I grepped `HeadlessConformanceTest.java` for the
  21 `GameAction.Type` constant names and got 10 "uncovered". That reading is a
  **false negative**: the suite drives actions through factory methods
  (`joinSupport`, `recruitCharacter`, …), not through the enum names. I discarded
  the measurement instead of seeding on it. This is the B5-0606 failure mode
  caught in the act, one pass after filing a pattern about it.

## 3. What was seeded — B5-0621

`B5-0621 | OPEN`, canonical 7 pipes, single leading pipe, zero pipe characters in
any note cell. Scope: ledger-only, no `src/`, no compile required.

Two parts:

1. **Repair the five rows still carrying a leading double pipe** — `B5-0583` (8
   pipes), `B5-0593` (10), `B5-0613` (12), `B5-0614` (10), `B5-0616` (10). Strip
   exactly one *structural* pipe each; the four rows above 7 carry 2, 4, 2 and 2
   pipes inside their own prose, so a blind normalise-to-seven would corrupt DONE
   rows. Residual counts get recorded rather than claimed as "reached seven".
2. **Close the production loop** — the detector already exists
   (`ledger-query.ps1` prints `pipeCount` and `doubleLead`; this pass read
   `doubleLead=yes` straight off it) but `00_BOOT.md` step 4 mandates only the
   run-queue census, so a seeding pass is never told to check.

**Why this row and not a tenth governance row:** the class has been declared
retired in prose three times — B5-0568, B5-0611, and again in the B5-0612
checkpoint commit message — yet **three of the five surviving instances
(B5-0613, B5-0614, B5-0616) were written after the retirement claim.** Every prior
pass fixed instances; none changed production. That measurement is the entire
justification for filing it again, and it is the part a reader should check.

No game feature is seeded this wave, and none is asserted to be missing. The
standing Ruling 2c / 3c human directive and the data-gated items (no mercenary
cards in either set per B5-0386; a four-card Non-Aligned pool per B5-0610) remain
decisions, not stalls.

## 4. The collision — two writers, one ID, three renumbers

This is the substantive event of the session and it is disclosed rather than
tidied away.

| step | state of my row | what happened |
|---|---|---|
| pre-write check | `B5-0618` measured **absent** | collision invisible |
| write | `\| B5-0618 \|` 7 pipes | clean write |
| post-write census | **two** rows for `B5-0618` | a concurrent pass seeded its own `B5-0618` (aftermath timing, B5-0594 gap 4) in the same window |
| my fix 1 | → `B5-0619` | other writer's row untouched |
| after fix 1 | my row **reverted** to `B5-0618` **plus a stray leading pipe** | they rewrote the band; my row became `\|\| B5-0618 \|` at 8 pipes |
| my fix 2 | → `B5-0619` | they had also moved *their* row to `B5-0619` — duplicate again |
| my fix 3 | → `B5-0621` | diverged to a non-adjacent ID; converged |

Final state: **duplicate-free across 319 rows.** `B5-0621` OPEN, 7 pipes, single
leading, `UNCLAIMED`. The other writer's `B5-0619` is OPEN at 8 pipes with a
leading double pipe — **theirs, not repaired by me**, but it is a live instance of
the class B5-0621 exists to fix, and the next agent should sweep it.

Three things this exposes, each of which contradicts something I would otherwise
have assumed:

* **A pre-write ID check is necessary and not sufficient.** It passed. Two writers
  passed the same check in the same window and both saw a free ID. Only the
  *post-write* census caught it.
* **A duplicate task ID is not cosmetic.** `run-queue.ps1` builds
  `$statusOf[$r.Id] = $r.Status`; with two rows sharing an ID the second silently
  overwrites the first, so one of the two tasks becomes **invisible to the gate
  and lane logic** — it cannot be offered, and its status is not read. The runner
  does not detect this; its self-check counts *lines*, and two lines parse fine.
* **Renumbering into the ID the other writer just vacated is a deadlock, not a
  fix.** We both moved to 0619. Divergence works; adjacency does not.

I also observed, in the other writer's insertion and left untouched as outside my
claim scope: a bare `</blockquote>` line between rows — not a row, not valid table
content. It was gone by the final census, so no action is needed, but it is the
kind of artifact that propagates into a checkpoint if it lands mid-band.

Finally, an irony worth recording: **my own seeded row became an instance of the
defect class it was filed about**, because the concurrent writer added it a stray
leading pipe while resolving the collision. The row and the collision are the same
story.

## 5. Residual observations, not acted on

* `run-queue.ps1 -DryRun` offered `B5-0621` on all 10 iterations. In `-DryRun` it
  `continue`s without claiming, and the loop re-picks `$free[0]`, so a dry run
  cannot show queue *diversity* — only that the head is claimable. Cosmetic.
* `my heartbeat` (this agent's) carried future-dated `session_start_utc`
  (`2026-09-27T23:05:00Z` against a real clock of `02:34Z`) from a prior session —
  the known clock-skew class. Corrected on refresh.

## 6. Gate

`compile.bat` green on the exact tree. No `src/`, no `resources/`, no
`src-java8-archive/` edit. Every change this pass is Markdown.

**Reusable lesson:** when two writers pick the same task ID, the second renumber is
usually aimed at the ID the first just vacated — check whether the other writer
moved too before assuming the slot is yours, and diverge to a non-adjacent ID.
Verify with a post-write duplicate census, because a pre-write check passed for
both of us.
