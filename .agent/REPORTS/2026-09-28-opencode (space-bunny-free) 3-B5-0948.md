---
document:
  title: "B5-0948 — correction of the B5-0921 node_modules provenance claim"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 3", version: "space-bunny-free"}
  assessor_llm:
    - {name: "unknown", version: "unknown", note: "no independent assessment of this report exists; it is itself an assessment, so per AGENTS.md 1 it must not list itself as assessor of its own work"}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 3", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0948 — correction of the B5-0921 node_modules provenance claim

**Agent:** `opencode (space-bunny-free) 3` (instance-qualified per R1–R6 in
`.agent/HEARTBEATS/README.md`; discriminator `3` was free and carries a digit per
R2). **Claim:** filed 2026-09-28T08:52:03Z, real clock read, no placeholder.
**Role:** *assessor*, not author. I did not write B5-0921; per AGENTS.md 1 I
record myself here as assessor of `solar-pro4:free`'s work and do not list
myself as its author.

## Summary

B5-0921's headline finding — that `node_modules` entered the repository via two
human "reply hello" commits on 2026-09-28 — is false. Those two commits are
**koda local checkpoint refs, and neither is an ancestor of `HEAD` or of
`origin/main`**. The dependencies were introduced by a single real commit on
2026-09-26. I did not edit the original report, its ledger note, or its DECISIONS
entry; the correction is a new DECISIONS entry, per AGENTS.md 1a rule 4
(append-only at the entry level).

## What I measured

| Check | Command | Result |
|---|---|---|
| Ancestry of the cited commits | `git merge-base --is-ancestor <c> HEAD` | **False** for `da58390f`, `786b34a3` |
| Ancestry in published history | `git merge-base --is-ancestor <c> origin/main` | **False** for both |
| Identity of the cited commits | `git for-each-ref` | `refs/koda/checkpoints/01a0e660-cecb-…` → `da58390f`; `refs/koda/checkpoints/01a0e66e-a1d0-…` → `786b34a3` |
| The real introducing commit | `git log -- node_modules` | **Exactly one commit: `418664de`** |
| Its metadata | `git log -1 418664de` | 2026-09-26 18:49:28 +1200 · "B5-0529 checkpoint (me-so-poor unknown) — compile+RUN_TESTS=1 green; excluded .agent/CLAIMS and .agent/HEARTBEATS" · author Tiwa Pene |
| It is real history | `git merge-base --is-ancestor 418664de HEAD` / `… origin/main` | **True** for both |
| Its share of the commit | `git show --name-only 418664de` | **67,258 of 67,281 files** are `node_modules/*` |
| The Figma import, as control | `git show --name-only --diff-filter=A d1d0c6ff \| node_modules` | 0 — the original's negative claim was right |

## Why the original read the way it did

`git show --name-only da58390f` lists 67,258 `node_modules` paths. Broken out by
filter: `A` = 67,258, `D` = 0, `M` = 0. So the commit *adds* them — which is what
made the reading look safe rather than speculative. But the commit is a koda
worktree snapshot: a snapshot of a working directory that happened to contain
`node_modules` records the whole tree in its diff. Presence in a snapshot was
read as introduction into history. The distinguishing test — is this commit an
ancestor of the ref in question — was never run, and no gate in this repo runs
it: `compile.bat` does not read history, `run-dup-census.ps1` does not, and
`validate-heartbeats.ps1` does not. An error confined to git provenance is
invisible to all three, which is why this row exists rather than a rerun.

`git rev-list --all --count da58390f` returns 180, so the commit *is* reachable —
from its own koda ref. "Reachable from some ref" is not "in the project", and
that is the second-order trap worth recording.

## Blast radius — four sites, not one

1. `.agent/REPORTS/2026-09-28-solar-pro4-free-B5-0921.md` lines 24, 25, 27
   (the "When dependencies entered" table and its summary sentence);
2. `.agent/TASK_LEDGER.md` line 998, the B5-0921 row note;
3. `docs/DECISIONS.md` line 6740, the B5-0921 entry;
4. `docs/DECISIONS.md` lines 6844–6847, the **B5-0925 entry**, which cites
   B5-0921 as *counter-evidence* that "a human touched the scaffold after the
   import. That is not a plan, and it is not evidence against one."

Site 4 is the one that needed a human to notice, because it is a second-order
inference: a downstream author read the false attribution as evidence of human
intent and reasoned about intent from it. That inference is withdrawn. No human
commit introduced these files, so the human-touch signal does not exist and the
sentence "that is not a plan, and it is not evidence against one" has nothing to
be even-handed about.

## What survives

- **Unaffected:** B5-0921's Phase 1 command sequence (`git rm --cached` +
  `.gitignore` + one reversible commit). It never depended on the attribution.
- **Rebuilt:** the approval *argument* for it. "A human chose this" and "a
  checkpoint swept it in" are different provenance; only the second holds, so a
  decision leaning on the first needs re-arguing on the correct basis.
- **Unchanged:** the human-intent question (wanted second front end vs Figma
  import residue), which B5-0921 and B5-0923 escalated and which this correction
  does not answer and is not mine to answer.

## Measurements confirmed independently of the attribution

67,258 files · 201,791,663 bytes on disk · 98.7% of 68,117 tracked files ·
`.git` 68,797,708 B total · `count-objects -vH`: 4 packs, 48.38 MiB in-pack,
7.79 MiB loose, 0 garbage.

## Boundaries held

No `git rm` in any form · no `.gitignore` edit · no edit to the B5-0921 report,
its ledger note, or the existing DECISIONS entries · no commit · no push · no
`b5ccg/src` edit · no card JSON touched · no foreign row, claim, report, pattern
or heartbeat touched. The only shared-file writes were: two appended ledger rows
(B5-0948, B5-0949), one new DECISIONS entry, this report, one pattern in my own
namespace, and my own heartbeat.

## Gate (measured on this tree)

- `b5ccg/compile.bat` → **exit 0**, `Build successful`, JDK `1.8.0_292`,
  `-source 6 -target 6`, 63 source files. Only stderr line is the expected
  bootstrap-classpath warning.
- `run-dup-census.ps1` → **PASS, 0 duplicate task IDs, exit 0**.
- `validate-heartbeats.ps1` → **exit 0**, 50/50 conforming, 0 identity
  collisions.
- My rows: `pipes=7`, `doubleLead=False`, trailing pipe present.

## Reusable lesson

> **A dangling ref enumerates its whole tree, so its diff looks exactly like an
> introduction — prove a commit is an ancestor of the ref you are discussing
> before you call it the introducing commit.**
> `git show --name-only` on a `refs/*/checkpoints/*` snapshot lists every tracked
> file in the worktree, and `--diff-filter=A` still reads all-add because the
> snapshot has no parent carrying them. The single test that separates the two is
> `git merge-base --is-ancestor <c> <ref>`; run it against `HEAD` *and*
> `origin/main` when the claim is about published history. `git rev-list --all`
> reaching the commit is not sufficient — a checkpoint ref reaches itself.

Filed as `.agent/PATTERNS/opencode (space-bunny-free) 3/2026-09-28-a-dangling-ref-enumerates-its-whole-tree.md`.
