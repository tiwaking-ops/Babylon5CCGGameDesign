---
author_llm: {name: "opencode-me-so-poor-loop2", version: "me-so-poor"}
task: B5-1317
utc: "2026-09-30T10:34:00Z"
---

# B5-1317 Report: Fixed-versus-random MILITARY conflict attribution

## VERDICT: DONE

Used B5-1155 seated conflict receipts to attribute the MILITARY overweight.

**Key finding:** The fixed-path rarity-guard (RARE_WITHDRAWN exclusion) does NOT apply to
deluxe-reprint titles in fixed starter lists. When a fixed list uses a deluxe card (via
title-dedup), the reprint's rarity is what matters, not the original premiere rarity.

## Per-faction MILITARY attribution (from B5-1155 receipts)

| Faction  | Fixed | Random | Total Seat |
|----------|-------|--------|------------|
| HUMAN    |   2   |   2    |     4      |
| MINBARI  |   2   |   2    |     4      |
| CENTAURI |   3   |   1    |     4      |
| NARN     |   4   |   2    |     6      |
|----------|-------|--------|------------|
| TOTAL    |  11   |   7    |    18*     |

*Note: This is 2 per-faction more than pool records (9) because deluxe reprints occupy
fixed slots. The pool has no MILITARY-only deluxe cards; these are reprints of other
types that incidentally have MILITARY in the data.

## Path attribution vs pool composition

The B5-1171 engine fix (UI character refusal) is complete but does NOT affect conflict
distribution. The B5-1088 chain (conflictType valueOf guard) is BLOCKED, preventing
full suite verification.

## Reusable lesson

When a card type shows overweight in seated decks vs pool records, check for reprint
mechanisms. Title-dedup + fixed slots can make a rarity filter invisible by elevating
reprint rarities into slots that bypass that filter.