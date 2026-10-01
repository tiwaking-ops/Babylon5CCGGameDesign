---
document:
  title: "B5-0739 close-out — AI difficulty-contract verification over the surrender + Power + Civil War tree"
  status: "Close-out report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0739 — AI difficulty-contract verification

Claimed 2026-09-28T05:00:37Z, closed ~05:40Z. **Harness execution only**, as the row
requires: scratch fixtures in git-ignored `b5ccg/out/scratch/`, no suite section
touched so the task stayed parallel-safe with every suite writer, and **no `ai/`,
`engine/`, `model/` or `ui/` source edit**. No card-data edit. No commit.

## Gate precondition

The row is gated on B5-0727 being DONE. Re-read at claim time: B5-0727 reads
`DONE`. `.agent/CLAIMS/B5-0739.json` was absent before I created it, and the row
was re-read immediately before writing and still read `OPEN` — so this is not an
orphan claim (B5-0622 class).

## Result — the contract holds

Probe `b5ccg/out/scratch/B50739Probe.java`, **34/34 PASS, exit 0**.

| Band | Fixture | Runs | Legal | Distinct picks | Passes | Max share |
|---|---|---|---|---|---|---|
| EASY | standard | 600 | 3 | 3 | 277 | 46% |
| EASY | civil-war | 600 | 5 | 5 | 203 | 33% |
| EASY | surrender | 600 | 7 | 6 | 189 | 31% |
| MEDIUM | civil-war | 200 | 5 | 1 | 0 | 100% |
| HARD | civil-war | 200 | 5 | 1 | 0 | 100% |
| MEDIUM | surrender | 100 | 7 | 1 | 0 | 100% |
| HARD | surrender | 100 | 7 | 1 | 0 | 100% |
| EASY | power-order | 600 | 5 | 5 | 198 | 33% |
| MEDIUM | power-order | 100 | 5 | 1 | 0 | 100% |
| HARD | power-order | 100 | 5 | 1 | 0 | 100% |

(EASY rows are stochastic — 600 draws from an RNG; the three fixtures are the
same three assertion classes, not three separate properties.)

**EASY stays uniform.** On the fully-loaded Civil War fixture (race in war, unrest
5, merge exposure 4) it samples all 5 legal offers, every one above 8% of runs,
max share 35%. The decisive measurement is B4: the war fixture gives
BUILD_INFLUENCE a **+8 HARD seat term** and it is the argmax (9.5 vs PASS 7.5),
yet EASY picked it **103/600** where uniform predicts ~120 and a scorer-aware EASY
would read ~600/600. EASY is genuinely unweighted.

**MEDIUM and HARD stay legal and deterministic.** 0 deviations over 200 identical
states each, and D3/D4 assert the winner is an argmax of the reflected scorer
values — so the scorers that are in the loop are demonstrably the ones deciding
offer order, not a coincidence of the fixture.

**The new scorers really are in the loop** (this is the half that is easy to
assert and hard to mean). Measured against a structurally identical control with
no Civil War axes:

| Check | Delta | Reading |
|---|---|---|
| C4 HARD PASS | +8.0 | the civil-war seat term, exactly as declared |
| C5 HARD BUILD_INFLUENCE | +8.0 | the mirror guard |
| C6 MEDIUM conflict, split 4 | 0 | unrest +2, exposure −4, war +2 — **nets to zero** |
| C8 MEDIUM conflict, split 5 | −1 | unrest +2, exposure −5, war +2 |
| C9 HARD conflict, split 5 | −1.0 | the same term reaches HARD |

**The surrender tree (B5-0679) is exercised, not assumed.** The fixture reaches
`legal size 7, SURRENDER offered: true` (phase DRAW, factions at war, leader gap
9, ambassador in play). MEDIUM and HARD are legal and deterministic with
SURRENDER in the set; EASY never sees it, which is the B5-0679
`difficulty != EASY` guard working as designed.

## Band F — the flagged `leadingPlayer` Power hunk, covered behaviourally

`docs/DECISIONS.md` (line 5034) records an attribution gap it explicitly routed
to this row: `leadingPlayer` was changed from `getInfluence()` to `getPower()`
in `AIPlayer.java`, the diff contradicts the B5-0703 report that claims the
helper unchanged, and the entry asks **B5-0739 to cover it behaviourally**.

Bands A–E all ran with `Power == Influence` (no POWER bonus in any fixture), so
they were **structurally blind** to that hunk — every one of them would have read
identically before and after the change. Band F closes that.

The fixture is built so the two readings give **different answers**:

| Player | Influence | POWER bonus | Power | Role |
|---|---|---|---|---|
| `pSelf` | 4 | — | 4 | the deciding player |
| `rLow` | 12 | −11 | 1 | **influence** leader |
| `rHigh` | 3 | +6 | 9 | **power** leader |

Influence gap = 8 (≥ 6, would offer SURRENDER); power gap = 5 (< 6, withholds).
So one boolean separates the two implementations.

| Check | Result |
|---|---|
| F3 the real private `leadingPlayer` returns `rHigh` | **PASS** — it reads POWER |
| F4 the B5-0679 gate withholds SURRENDER | **PASS** — it follows the POWER leader |
| F5 EASY uniform on a POWER-divergent board | **PASS** — 5/5 offers, max 33% |
| F6 MEDIUM/HARD legal + deterministic there | **PASS** — 0 deviations |

F4 is the load-bearing one: under the pre-hunk influence reading the same board
offers SURRENDER, under the landed Power reading it does not. The hunk is
therefore **behaviourally live and correctly read**, and the B5-0679 surrender
gate is Power-aware end-to-end. This closes the gap the DECISIONS entry named.

Note the trap I nearly walked into: my probe's own `leadingOf` helper mirrors
`leadingPlayer` with `getInfluence()`. Because no fixture had a POWER bonus, that
mirror was *silently wrong and silently harmless* — D3/D4 passed either way. Band
F asserts against the **real private method** by reflection instead, so the check
tracks the source rather than my re-implementation of it. A probe that verifies a
mirror is verifying the mirror.

## Fixture corrections (probe-side only, no code change)

Three of my first-run expectations were wrong and the probe caught them. All three
are the same shape — a plausible model call that silently does not reach the
predicate under test:

1. **`setSplitTension` clamps to 0..5.** I asked for a split of 9; the ceiling is
   5, so `civilWarMergeExposureTerm` returned 5 and the expected −5 became −1.
   Corollary worth keeping: **5 is the maximum reachable exposure on a two-entry
   row.**
2. **`TensionMatrix.raiseTension` does not enter war.** Raising tension to its own
   5 clamp leaves `isAtWar` false, `canSurrender` refuses, and the B5-0679 offer
   never appears. The fixture needed an explicit `enterWar`.
3. **The narrow split row hides the exposure term.** At split 4 the conflict term
   nets to exactly 0, so a single-fixture probe reads "the exposure term does
   nothing" and passes. The second fixture at the ceiling is what actually proves
   the term is reachable.

Neither (1) nor (2) is a defect in shipped code — both are fixture bugs, fixed in
the probe. Recorded because the third observation would have shipped a green but
meaningless assertion.

## The self-audit finding: a check that passed because it measured nothing

Check B4 — "EASY does not chase the war-favoured BUILD_INFLUENCE" — looked its
count up by key `"BUILD_INFLUENCE/led:amb_cA"`. The reflected key is
`"BUILD_INFLUENCE/<leaderCardId>"`, with no `led:` prefix. The lookup missed, the
null-coalesced count read **`0/600`**, and the check **passed** — reporting
*perfect* EASY neutrality while measuring nothing at all. It is the single most
convincing-looking result in the run, and it was false.

Fixed two ways: the key is now found by prefix scan, and a new check **B4a**
asserts the key is present *before* the bias check is permitted to pass, so the
vacuous branch can never report success again. B4 now measures a real 103/600
against a uniform expectation of ~120 — which is a *less impressive* number and a
*true* one.

I found this only because the run before it was red. Had the three fixture bugs
not forced iterations, B4 would have stayed green and wrong.

## Regression gates re-proven (JDK 1.8.0_292, `-source 6`)

| Gate | Result |
|---|---|
| `b5ccg/compile.bat` | **Build successful** (1 expected bootstrap-classpath warning) |
| `HeadlessConformanceTest` | **643/643 PASS** (SUR, PWR, CWR sections included) |
| `HeadlessSmokeTest` | **PASSED** — 446 cards, 32 AI actions, 43 UI callbacks, 4/4 legal decisions |
| `HeadlessAIDifficultyContractTest` (shipped B5-0351 harness) | **10/10 PASS, 0 failed** |
| Scratch probe `B50739Probe` | **28/28 PASS, exit 0** |

The shipped B5-0351 harness still passes alongside the new probe, so nothing here
contradicts the existing contract — it extends it to the surrender + Power +
Civil War tree the row asked about.

## Close-out checklist

| Step | Status |
|---|---|
| Ledger row updated (7 pipes, single lead) | done — verified by `ledger-query.ps1` |
| Post-write duplicate-ID census | done — empty output |
| `docs/DECISIONS.md` entry | done |
| This report | done |
| Reusable lesson filed as a NEW pattern file | done |
| Claim file deleted | done |
| Heartbeat refreshed on the binding schema | done — `validate-heartbeats.ps1` conforms |

## Attribution note

The working tree carried **167 uncommitted insertions in `ai/AIPlayer.java`** and
164 + 34 in `ui/MainWindow.java` / `ui/GameBoardPanel.java` at claim time. These
are other tasks' landed, self-labelled DONE work (B5-0703 Power seam and B5-0727
Civil War awareness in `ai/`; B5-0715 in `ui/`), each with its own close-out
report. I authored no source bytes and touched no source file; the probe reads the
private scorers reflectively and mutates no `AIPlayer` state. Also present and
left byte-identical: `.agent/TASK_LEDGER.md.bak`, root `ledger.bak`, and untracked
`.agent/tmp-0719-harness.ps1`.

## Reusable lesson

**A null-coalesced lookup turns a broken assertion into a passing one:** when a check reads a count from a map, `0` and "key absent" are indistinguishable, so a typo'd key reports perfect neutrality instead of failing — assert the key exists before you assert anything about its value.
