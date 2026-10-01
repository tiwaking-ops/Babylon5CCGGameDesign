---
document:
  title: "B5-1315 close-out - static fixed-50 cost curve: 145 of 200 slots are affordable at the starting appliedPool of 4 (HUMAN 34, MINBARI 38, CENTAURI 39, NARN 34), the curve is bimodal with a 27-32 slot zero-cost block and nothing between 0 and 2, and the >4 tail reconciles exactly to B5-1149's 16/12/11/16"
  status: "Report (no authority; static input, no adjudication)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1315"
  instrument: "b5ccg/out/b51315/b51315/B51315CostCurveProbe.java (javac -source 6, production DeckLoader.loadBothSets + StarterDeckBuilder.build, no game played, smoke harness not run); receipts b5ccg/out/b51315-receipt.txt and b5ccg/out/b51315-stderr.txt"
---

# B5-1315 - the static cost-curve input B5-1141 needs

**Claim:** `.agent/CLAIMS/B5-1315.json`, `opencode (big-pickle) loop1`, started
2026-09-30T23:36:32Z, released at close-out. Row re-read `OPEN`, claim cell `-`, no
claim file on disk. **No deck JSON, no card JSON, no src file edited; the smoke
harness was NOT run (the row forbids it); no commit; no push.**

## Coordination first, because the row's own instructions were stale

Three facts had to be settled before delivering anything, and one of them changed
what "deliver" means:

1. **The consumer is blocked.** B5-1141 (`TASK_LEDGER.md:1236`) is `BLOCKED`,
   gated on B5-1088 being DONE. The static input is still deliverable and is
   exactly what the row asked for; nothing here waits on the gate.
2. **The row this row defers to does not exist.** B5-1315 says "read OPEN
   B5-1265 first and if it owns the per-faction fixed-50 histogram deliver only the
   pool-4 affordability counts it does not cover plus cite it". There is no
   B5-1265 row: the string `B5-1265` appears twice in the whole ledger, both times
   inside B5-1315's own line. Dangling cross-reference.
3. **The real histogram owner is B5-1345, and it has not published.** B5-1345
   (`TASK_LEDGER.md:1280`) is "Pin the per-faction cost histogram of the fixed 50",
   `OPEN`, claimed live by `solar-pro4:free` since 23:27:32Z. Its report
   (`.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1345.md`) exists but is an
   **unfilled template** - section header "Per-faction cost histogram (to be filled
   by probe)", the receipt line "Fresh recompute must reconcile to these four
   numbers exactly before the histogram is credited", and empty playability
   sections. No numbers at all.

So the deferral target is a skeleton. Per `AGENTS.md` §4 ("log the ambiguity in the
report and STOP that item only - do not block unrelated work") I did not stall and
did not silently take the histogram: I delivered the affordability input this row
names, cited B5-1345 as the owner of the histogram shape and of the ids-at-maximum
question, and reproduced their cross-check target so they can credit against these
numbers if they prefer. **Overlap declared:** the per-faction cost histogram below
is the same table B5-1345 is measuring. B5-1345's row, not this report, should be
treated as the authority for the histogram; this report's unique content is the
pool-4 affordability count and the sorted cost vectors.

## Method

Production path, no shortcuts: `DeckLoader.loadBothSets()` (pool = 446 cards) then
`StarterDeckBuilder.build(faction, pool)`, first 50 slots of each deck, `Card.getCost()`
read off the resolved card. Java 6, stdlib only, compiled with `javac -source 6
-target 6` against `b5ccg/out`. No turn was played, no callback fired, no harness
invoked. Probe exit 0; stderr is 4 158 lines of the loader's pre-existing
`UNKNOWN FIELD` diagnostics (the same noise B5-1307 recorded), and no
`StarterDeckBuilder` guard fired (`deckSize=60` for all four factions, fixed lists
all resolved to 50 slots).

`Faction.values()` also enumerates NEUTRAL, NON_ALIGNED, VORLON and ANY, each of
which builds a 10-card deck and is **not** a starter faction - the deck data holds
only HUMAN, CENTAURI, MINBARI, NARN. Their numbers are in the receipt and are
excluded from every count below.

## The deliverable

| Faction | fixed slots | affordable at pool 4 (cost <= 4) | over 4 | reachable at 5 | max cost |
|---|---|---|---|---|---|
| HUMAN | 50 | **34** | 16 | 36 | 10 |
| MINBARI | 50 | **38** | 12 | 39 | 11 |
| CENTAURI | 50 | **39** | 11 | 40 | 10 |
| NARN | 50 | **34** | 16 | 37 | 11 |
| **total** | **200** | **145** | **55** | **152** | - |

**Sorted fixed-slot cost vectors**

```
HUMAN    0 x27, 3 x3, 4 x4, 5 x2, 6 x5, 7 x1, 8 x4, 9 x1, 10 x3        (34 <= 4)
MINBARI  0 x32, 3 x2, 4 x4, 5 x1, 6 x3, 8 x2, 9 x2, 10 x3, 11 x1       (38 <= 4)
CENTAURI 0 x30, 2 x1, 3 x4, 4 x4, 5 x1, 6 x2, 7 x2, 8 x3, 10 x3       (39 <= 4)
NARN     0 x27, 1 x1, 2 x2, 3 x2, 4 x2, 5 x3, 6 x4, 7 x1, 8 x4, 9 x1,
         10 x2, 11 x1                                                 (34 <= 4)
```

**Ids at each faction maximum**

* HUMAN, max 10: `loc_earth`, `de_char_general_hague`, `de_fleet_second_battle_human`
* MINBARI, max 11: `de_fleet_second_battle_minbari`
* CENTAURI, max 10: `loc_centauri_prime`, `de_fleet_second_battle_centauri`, `char_urza_jaddo`
* NARN, max 11: `de_char_khamak`

Full per-slot over-4 id lists with costs are in `b5ccg/out/b51315-receipt.txt`.

## Receipt: the >4 tail reconciles to B5-1149 exactly

B5-1149 cited 16 HUMAN, 12 MINBARI, 11 CENTAURI, 16 NARN slots costing more than
the starting pool of 4. Measured independently here: **16, 12, 11, 16 = 55**. The
same 55 of 200 also reproduces the counterfactual baseline measured under B5-1307
(55 of 200 slots costing more than a starting pool of 4, before and after the
reprint-cost counterfactual). Three rows, one number.

## Validity check: the zero-cost block is real data, not a resolution artifact

A 27-to-32 slot zero-cost block per faction is large enough to be a bug, so it was
checked rather than reported. Risk: `DeckLoader.loadBothSets` dedupes by title with
the deluxe record winning (B5-1307), and `cost` is an **optional** key in the card
data (`DeckLoader.java:93-99`, 209 of 446 premiere records and 168 deluxe records
carry one). A title whose deluxe twin lacks the key would silently resolve to cost
0. Measured over all 187 fixed-list entries: **105 have both twins present and all
105 agree on both the presence and the value of `cost`**; 82 have no deluxe twin at
all. There is no entry where a non-zero premiere cost resolves to 0. The zeros are
what the data says.

## What the shape is, without adjudicating DESIGNED versus defect

B5-1149 already adjudicated that question; this row only supplies numbers. Stated
as shape, so B5-1141 does not have to re-derive it:

* The curve is **bimodal, not a ramp**: a 27-32 slot zero-cost block, then almost
  nothing - across all 200 slots there are exactly three entries below 3 that are
  not zero (CENTAURI 2 x1, NARN 1 x1 and 2 x2) - then everything from 3 to 11.
* **Affordability at a 4-pool start is 68-78% per faction**, 145 of 200 overall,
  so the pay-at-play gate cannot lock a faction out of its own fixed list: every
  faction has 34+ slots playable with no Build Influence, and 37-40 with one.
* **The expensive tail is uniform in shape and faction-specific in detail**: all
  four top out at 10 or 11, and the Second Battle Fleet card sits at or near the
  maximum in all four. NARN is the widest spread (1 to 11, with four cost-6s).
* Nothing in the fixed 50 costs more than 11, so a single pool of 11 covers any
  one card in any faction - but never two of the top-band cards at once.

**Reusable lesson:** a deferral instruction naming another row is a claim about
that row's state, so check the state before deferring - a deferral target that is
an unfilled template is not a reason to stall, and not a licence to quietly take
its subject over either. Deliver your own slice, cite the owner, and name the
overlap in the report so the next reader knows which document is the authority.