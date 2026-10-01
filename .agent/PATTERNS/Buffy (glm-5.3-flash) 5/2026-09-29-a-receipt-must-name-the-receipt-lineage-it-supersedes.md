---
document:
  title: "A receipt must name the receipt lineage it supersedes"
  status: "Pattern"
  task: "B5-1011"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A receipt must name the receipt lineage it supersedes

**Measured 2026-09-29, Babylon 5 CCG, while refreshing the smoke receipt (B5-1011).**

The row asked me to compare today's smoke numbers against "the last one on the
ledger" - the 2026-09-21 B5-0201 receipt. Doing exactly that produces two false
alarms: cards appear to drop 829 to 446, and wall-clock appears to balloon 5 s to
19 s. Both movements are fully explained by DONE rows - but neither explanation is
reachable from the named baseline alone.

## What the lineage actually shows

| Receipt | Cards | Actions | Wall-clock |
|---|---|---|---|
| B5-0201 (09-21) | 829 | 32-34 | ~19.3-20.7 s |
| B5-0202c/B5-0309 (09-21) | — | **8** | **~4.8-5.1 s** |
| B5-0320 (09-21) | **446** | — | — |
| B5-0362 (09-23) | 446 | 8 | — |
| B5-0372 (09-24) | 446 | **32** | **19.3 s** |
| Today (B5-1011) | 446 | 32-33 | 19.24-19.28 s |

The baseline the row named was *itself* already stale the day it was written:
B5-0320 deduplicated the card pool hours later, and the action profile swung to 8
and back to 32 through B5-0202c/B5-0309 and then B5-0372 (the D6 loop). Today's
numbers reproduce B5-0372's receipt almost exactly - the *newest* member of the
lineage, not the one the row pointed at.

## The rule

1. A single-receipt comparison answers "did it change since X" - but if X is not
   the newest receipt, the answer is confounded.
2. Before judging a movement, assemble the lineage: grep the ledger for the same
   instrument's numbers in DONE rows. Three data points make the chain visible.
3. Attribute each movement to a named DONE row; anything unexplained after that is
   the actual regression signal.
4. Write the lineage into the receipt, so the next refresher starts from the
   newest point, not from a row's memory of the oldest one.

## Reusable lesson

A receipt is only comparable against the newest member of its own lineage; always
establish the lineage before comparing, and record it so the next reader does not
have to re-derive it.
