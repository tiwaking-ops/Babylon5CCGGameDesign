---
document:
  title: "B5-1313 verification close-out - the interpretation log is accepted with one substantive correction: the +1-per-Inner-Circle-member term is in promotionCost (RulesEngine.java:160), the PROMOTE_CHARACTER path, not in the sponsor/recruit cost the row exists to document"
  status: "Report (verification and adjudication; no authority beyond what the row granted)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1313"
  assesses:
    - ".agent/REPORTS/2026-09-30-solar-pro4-free-B5-1313.md (author_llm: solar-pro4:free)"
  instrument: "read-only source verification against b5ccg/src at 2026-09-30T23:30-23:33Z; no src edit"
---

# B5-1313 - independent verification of the sponsor-cost vs canPlayCard interpretation

**Claim:** `.agent/CLAIMS/B5-1313.json`, `opencode (big-pickle) loop1`, started
2026-09-30T23:31:55Z, released at close-out. Row re-read `OPEN`, claim cell `-`, no
claim file on disk - claimed legitimately. **Docs only: no src or suite file
edited, no commit, no push.**

## Why this row needed a closer at all

`solar-pro4:free` **completed the work already** - report
`.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1313.md` (23:18:37Z), pattern
`.agent/PATTERNS/solar-pro4-free/2026-09-30-B5-1313-sponsor-cost-vs-canPlayCard.md`
(23:18:52Z), DECISIONS entry at `docs/DECISIONS.md:10933` - and their heartbeat
records "B5-1313 DONE", yet the ledger row was still `OPEN` with an empty claim
cell and no claim file. The row flip was missed. Closing it was therefore an
assessment task, not a production task: the question was never "can I write this
interpretation" but "is the interpretation already on disk correct", because
closing someone else's DONE work unverified would make the ledger's DONE column
mean nothing.

## Verified against the live tree - all of these are exact

| claim in the report | live tree | verdict |
|---|---|---|
| `RulesEngine.sponsorCost` at 242-247 | declaration at 242, body 243-247 | **confirmed** |
| `RulesEngine.canRecruit` at 252-254, `return p.getAppliedPool() >= sponsorCost(p, ch).getAmount();` at 254 | exactly so | **confirmed** |
| charge `sponsorCost.getAmount()` at `GameController.java:231` | exactly so | **confirmed** |
| `applyGenericCardPlay` at `GameController.java:630` | exactly so | **confirmed** |
| raw `int cost = card.getCost();` at `GameController.java:636` | exactly so | **confirmed** |
| `canPlayCard` has **one** production caller, `MainWindow.java:2134`, and it is excluded for characters at line 2130 | `MainWindow.java:2134` is the only non-test, non-definition reference in the tree; 2130 reads `&& !(selectedCard instanceof CharacterCard)`; `playOnly` refuses `selectedCard instanceof CharacterCard` at 2147-2148 | **confirmed** |
| their correction that B5-1109's "zero production callers in MainWindow" went stale after B5-1177 | B5-1177's comment sits directly above line 2134 | **confirmed - the correction is the right one, and it is current** |

Full call-site census for `canPlayCard`, all of `b5ccg/src`: definition
`RulesEngine.java:904`; harness `HeadlessConformanceTest.java:6269, 6271, 6275,
6276` and `HeadlessHumanSeatProbe.java:344, 626`; production
`MainWindow.java:2134` only.

## Correction 1 - the `+1 per IC member` term is not in the sponsor cost

The report's section 1 says, of line 246:

> `baseRecruitCost(p, ch)` (line 246): `card.getCost()` doubled if
> `ch.getFaction() != p.getFaction()` (other-race loyal doubling, rulebook
> §Sponsor), **plus `1 per existing IC member`** (B5-0321 seam -
> `innerCircle.size()`, since the ambassador is seated in IC).

Two errors, and the second is the one that would mislead:

1. **Wrong call at that line.** Line 246 reads
   `return new SponsorCost(recruitCost(p, ch), true, false);` - it calls
   `recruitCost`, not `baseRecruitCost`.
2. **Wrong method entirely for the IC term.** `baseRecruitCost` (212-217) is cost
   plus other-race doubling and nothing else. `recruitCost` (233-234) is
   `Math.max(0, baseRecruitCost(p, ch) - p.getSponsorDiscount())`. The
   `innerCircle.size()` term lives in **`promotionCost`**, `RulesEngine.java:152-161`,
   at line 160 `return base + p.getInnerCircle().size();` - the
   **PROMOTE_CHARACTER** action, a different action from recruiting, gated by
   `canPromote` and charged at `RulesEngine.java:195`.

So the live sponsor/recruit composition is exactly: **card cost, doubled if
other-race loyal, minus the ambassador's assistant sponsor discount floored at 0**,
or wholly waived under `FREE_SPONSOR`. IC size does not enter it.

Why this matters more than a typo: the row's purpose is to tell a future reader
which number to check a new `canPlayCard` caller against. A reader who trusts the
quoted composition would expect the recruit charge to grow as the Inner Circle
fills, and would compare a candidate caller's result against the wrong baseline -
and would look for the growth in the wrong place, since promotion cost is a
separate charge on a separate action. The report's own conclusion section is
unaffected and correct: a new caller must be checked against `sponsorCost` /
`canRecruit`, not `card.getCost`, because `recruitCost` subtracts the sponsor
discount while `canPlayCard` reads raw cost.

Recorded as a docs-only correction in `docs/DECISIONS.md` and as an
`assessor_llm` entry on the assessed report. **The assessed report was not
rewritten** - only its provenance ledger was touched, per the repo's
supersede-never-rewrite convention.

## Correction 2 - the harness call-site line numbers quoted are stale

The report quotes B5-1109's census as `HeadlessConformanceTest` 6166, 6181, 6183,
6187, 6188 and `HeadlessHumanSeatProbe` 623-626. Those were true when B5-1109
closed; the live lines are 6269, 6271, 6275, 6276 and 626. Benign - harness drift
in the ~17 h since - and immaterial to the row's conclusion, but a reader who
jumped to those lines would have found nothing there. Worth noting because
"zero production callers" is the load-bearing claim and the harness lines are the
ones that had moved.

## Verdict

**ACCEPTED WITH CORRECTION.** The row's three requirements are met by the work on
disk:

1. which cost notion each path charges and gates - `sponsorCost` (242-247) gates
   via `canRecruit` (252-254) and charges at `GameController:231`;
   `applyGenericCardPlay` (630) charges raw `card.getCost` (636);
   `canPlayCard` (904) gates nothing in production on this path;
2. the exact condition under which `canPlayCard` becomes load-bearing - its first
   production caller dispatching a CharacterCard; enumerated as three triggers;
3. what that caller must be checked against - `sponsorCost`/`canRecruit`, never raw
   `card.getCost`, with the discount-direction false-negative named.

One sentence of the composition walkthrough is wrong in a way that would misdirect
requirement 3's user; it is corrected here and in DECISIONS. The row closes DONE
with the correction attached, and the interpretation stands.

**Reusable lesson:** when closing another agent's finished work, the value you add
is not a second opinion on the conclusion - it is checking the arithmetic
walkthrough the conclusion depends on. Here the conclusion was right and the
derivation one paragraph earlier named the wrong method and the wrong action.