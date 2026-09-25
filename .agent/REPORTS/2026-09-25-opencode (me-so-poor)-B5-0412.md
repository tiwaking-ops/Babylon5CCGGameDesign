---
author_llm: opencode (me-so-poor / big-pickle)
---

# B5-0412 — Playtest-guide refresh part 4

**Status:** DONE
**Agent:** opencode (me-so-poor) / big-pickle
**Date:** 2026-09-25
**Gate:** B5-0409 DONE (satisfied — dual completion, both reports on disk).

## Summary

Refreshed `docs/playtest-guide.md` with the B5-0409-verified stall/balance
findings, correcting the B5-0408-era staleness the verification confirmed.

## Changes

1. **Stall honesty note rewritten.** The old "The multi-round harness
   correctly reports 'stalled'" framing is stale. The no-timeout B5-0409
   probe proves games terminate naturally (round 12 / ~118s / standard
   victory at Rating 20); harness "stall" = its 60s window closing before a
   winner. Keep the separate truth: pass-heavy rounds ARE real (EASY ~53%),
   B5-0372 D6 loop is live.
2. **New honesty bullet — "zero promotions in harness output is a counting
   artifact."** Documents the B5-0349 `parseLog` `": promotes "` token bug
   (RulesEngine logs `" promotes "` without the colon; the action line is
   `"PROMOTE_CHARACTER"`). Playtesters should trust live IC size, not the
   harness promote figure. Fix flagged for B5-0413.
3. **New honesty bullet — Influence Rating 20 reachable.** Corrects the
   "unreachable threshold" inference: 20 was reached in ~2 min; the agenda
   economy loses the race to standard victory, not because 20 is a hard gate.
4. **New honesty bullet — initiator win-rate.** ~64% initiator wins on
   trackable lines (reproduction), consistent with B5-0309 sides rule +
   B5-0343 initiate-when-winning bias; read as normal variance, not pathology.
5. **Suite counts unchanged:** §6 still says 360 checks — verified 360/360
   PASS in this session's RUN_TESTS-equivalent run. No count edit needed.
6. Provenance updated: appended opencode (me-so-poor) to assessor_llm,
   last_modified_by_llm → opencode (me-so-poor).

Docs-only: no src/ or resources/ edits; no compile needed (docs task).

## Verification

- `git diff --stat docs/playtest-guide.md` — added ~26 lines, no deletions
  beyond the replaced stall bullet block.
- Provenance header valid; no body text of the rulebook touched.