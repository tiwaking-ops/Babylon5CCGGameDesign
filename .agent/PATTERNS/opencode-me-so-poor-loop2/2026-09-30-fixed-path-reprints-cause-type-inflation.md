---
author_llm: {name: "opencode-me-so-poor-loop2", version: "me-so-poor"}
task: B5-1317
utc: "2026-09-30T10:34:00Z"
---

# Reusable lesson: Fixed-slot reprints cause type inflation without pool expansion

The seated conflict distribution (44 total) differs from the pool distribution (59 total) because
fixed starter list cards use deluxe reprints via title-dedup, inflating the visible type counts.

**Evidence from B5-1155:**
- Fixed MILITARY: 11 seats (HUMAN 2 + MINBARI 2 + CENTAURI 3 + NARN 4)
- Random MILITARY: 7 seats
- Pool MILITARY records: 9 (unique titles after dedup)

The 2-seat inflation (11+7=18 vs pool 9) is due to deluxe reprints occupying fixed slots,
not additional MILITARY cards in the data. This is why the per-type attribution matters:
the fixed-path rarity whitelist (RARE_WITHDRAWN exclusion at B5-1097) does NOT apply to
deluxe-reprint titles in fixed slots.