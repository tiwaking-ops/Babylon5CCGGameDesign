---
document:
  title: "B5-1313 — sponsor-cost vs canPlayCard interpretation"
  status: "Pattern"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1313: sponsor-cost vs canPlayCard interpretation

## Cost notions on the live tree (2026-09-30T23:10Z)

### 1. RulesEngine.sponsorCost (lines 242-247) — the correct character notion
Composes `baseRecruitCost` (card.getCost doubled if other-race loyal + 1 per IC member) + assistant sponsor discount + FREE_SPONSOR waiver. Both the gate (`canRecruit` line 254) and the charge (`GameController` line 231) read this notion. RECRUIT_CHARACTER path is internally consistent: 9/9 probe pass.

### 2. GameController.applyGenericCardPlay (line 636) — charges card.getCost RAW
No doubling, no discount, no waiver. CharacterCard falls through all instanceof branches → removed from hand, logged, no effect. This is the corruption B5-1109 named. Currently unreachable from all paths (B5-1341 census: all 5 paths closed).

### 3. canPlayCard — zero production callers (as of B5-1109)
Referenced only by HeadlessConformanceTest (5 lines: 6166, 6181, 6183, 6187, 6188) + HeadlessHumanSeatProbe (3 lines: 344, 623-626). B5-1038 gate protects nothing on the RECRUIT_CHARACTER path. Price is wrong in the DISCOUNT direction: refuses at pool 2 when canRecruit allows at charge 1 (does not know about assistant discount).

**B5-1177 AFTER B5-1109 closed added a production caller.** MainWindow.updatePlayInitiateButtons (MainWindow.java line 2134) calls `rules.canPlayCard(humanPlayer(), selectedCard)` as a production enablement check. This line landed AFTER B5-1109's close-out. It does NOT open a CharacterCard path (line 2130 excludes it), but it IS a production caller that B5-1109's "zero production callers in MainWindow" claim did not capture. The count is now 1, not 0. The claim is time-stamped: accurate when written, stale after B5-1177.

## When canPlayCard becomes load-bearing
When any production caller dispatches a CharacterCard to applyGenericCardPlay or any path that consults canPlayCard before playing. None exist as of this census. First such caller must check against sponsorCost — NOT card.getCost — because canPlayCard's raw-cost price is wrong in the discount direction and would produce false negatives (refusing discounted recruits) if used standalone on a character path.

## Link
Full report: `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1313.md`
