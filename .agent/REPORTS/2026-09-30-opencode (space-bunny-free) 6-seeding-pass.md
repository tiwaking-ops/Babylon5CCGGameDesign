---
author_llm: opencode (space-bunny-free) 6
version: space-bunny-free
task: seeding-pass
utc: "2026-09-30T06:05:00Z"
---

# Seeding pass — B5-1109..B5-1127 (10 rows)

User order: seed a lot of tasks, then show the task list. Seeding only: no claim
held at any point, none of the 10 rows worked, own rows only.

## Verification first, on the exact working tree

| Check | Instrument | Result |
|---|---|---|
| Toolchain | `javac -version` | 1.8.0_292 |
| Build gate | `b5ccg/compile.bat` | GREEN, "Build successful", 1 expected bootstrap warning |
| Duplicate IDs | `.agent/tools/run-dup-census.ps1` | PASS, 0 duplicates over 549 rows (pre-append) |
| Pipe census | `.agent/tools/ledger-query.ps1 -Status "*"` | 549 rows, 11 reportable non-7, 6 content-exempt |
| Heartbeats | `.agent/tools/validate-heartbeats.ps1` | exit 1, standing-red on 3 pre-existing identity collisions + legacy keys |
| Append guard | SHA-256 of the ledger | `0C38B10D…7879F`, 1430577 bytes, 1211 lines |

## Two of my four findings were already owned — deliberately not seeded

I measured both before writing anything, then checked ownership and dropped them:

1. **The 2699-line `UNKNOWN FIELD` flood** — already owned by **B5-1068** to fix.
2. **The `influenceReward` drop causing the `HeadlessConformanceTest:507`
   ClassCastException** — **B5-1087** triaged it, **B5-1088** owns the fix.

Seeding either would have created a second owner for live work. B5-1111 instead
*feeds* the B5-1068 owner with a decomposition it does not have, rather than
competing with it.

## The two unowned findings that carry the wave

**Gate-versus-charge cost divergence (B5-1109).** `RulesEngine.canPlayCard`
gates on `p.getAppliedPool() < c.getCost()` (line 908) while the
`RECRUIT_CHARACTER` branch charges through `SponsorCost` wrapping
`baseRecruitCost` (`GameController:229`). Two different cost notions for the
same card. Whether the gate is even *reached* on the recruit path is the first
question, and it may turn out to be a gate that does not cover the path it
appears to cover — a worse and differently-shaped defect than two disagreeing
prices. B5-1121 consumes that verdict.

**The cost field is now populated (B5-1113).** 377 of 829 records carry `cost`
against **0** at the B5-0311 C1 census. Starting `appliedPool` is 4
(`Player.java:12`). **268 of 377** costed records cost more than 4 — 71% of the
costed pool is unplayable on turn one. B5-1119 adds the third leg neither
B5-1092 (win band) nor B5-1113 (curve) covers: the AI *action mix*, because a
band result can be green for the wrong reason.

Also measured and seeded: per-type `CardEffects` fallthrough (B5-1127 — EVENT
16 registered of 166, AFTERMATH 0 of 117, 40 ids total against 829 records);
whether location income is actually paid (B5-1115); the cost-0 vs absent-key
distinction under B5-0968 (B5-1123); a durable standing-red gate receipt
(B5-1117); a reproducible priority order for the coverage backlog (B5-1125).

## The collision, and why the pre-write check was not enough

I planned 19 rows and pre-checked all 19 IDs free. Between that pre-check and my
SHA-guarded append, a concurrent seeder wrote its own **B5-1107** row at line
1211. My post-write census caught it:

```
B5-1107 x2
FAIL
duplicate-ID census: see duplicated IDs above
```

Per **B5-0622** I diverged *my* row to non-adjacent **B5-1127** and left the
foreign row byte-identical with its status untouched, so its owner is not
orphaned. Re-ran the census to PASS.

This is the `00_BOOT` step 9 rule demonstrated rather than asserted: a SHA guard
detects that the *file* moved, but it cannot reserve an *ID*. The pre-write check
is necessary and not sufficient, and the only thing that caught this was the
post-write check.

I also planned 19 and shipped 10. The queue note says 10 and explains why, rather
than quietly matching the plan.

## Post-write gates

| Gate | Result |
|---|---|
| `run-dup-census.ps1` | **PASS**, exit 0, 0 duplicate IDs |
| `ledger-query.ps1 -Status OPEN` | all 10 own rows at **7 pipes, doubleLead no**, UNCLAIMED, reportable |
| Same census footer | 0 further non-7 rows, 0 content-exempt — append added no new defect class |

## One correction worth recording

My own split-based pipe recount read 571 non-7 rows; the shipped detector's footer
read 11 reportable and 6 content-exempt. **The tool is right and my ad-hoc
recount was wrong** — it counted the detector's own output columns. I used the
shipped tool's reading. This is the same lesson the repo has already recorded
about hand-rolled censuses, re-learned the cheap way.

## Boundaries held

No claim held. No row worked. Own rows only. No foreign row, claim, or heartbeat
edited — the foreign B5-1107 row is byte-identical. No `b5ccg/src/` or card-data
edit (the scratch probe under `b5ccg/tmp_seedprobe/` and the census script were
git-ignored and **deleted** after use). No commit, no push.

## Reusable lesson

A SHA-guarded append makes the write atomic against a *moving file*; it does not
make the chosen *ID* atomic. Two agents can both measure an ID free and both
write it. The only instrument that catches this is the post-write duplicate
census — so treat it as mandatory after every ledger write, not as a formality,
and when it fires, diverge rather than renumber into the slot the other writer
just vacated.
