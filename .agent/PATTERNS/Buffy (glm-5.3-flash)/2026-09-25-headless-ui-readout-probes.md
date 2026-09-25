---
document:
  title: "Pattern — headless UI-readout probes: fixtures, assertions, accessors"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: verifying a Swing readout without showing a window

Filed under the standing "Reusable lesson" convention (00_BOOT step 10).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0427.md`.

## The lessons

1. **Fixtures need FULL player state.** A scratch `GameState` with bare players
   NPEs in `GameBoardPanel.drawZone` (deck-count line reads `p.getDeck().size()`).
   Real games always deal a deck; synthetic ones must too — `p.setDeck(new
   Deck(cards))` for every player before `update()`/`printAll`. Reusable lesson
   from 0427: seeded via Deck, not a hand trick.

2. **Assert presence AND absence.** A marker check that only asserts "renders
   somewhere" passes even when the marker bleeds onto wrong cards. Assert the
   marker pixels in the target card's rect, zero marker pixels on a sibling
   control card, and zero on a face-down card (identity opacity). Presence-only
   assertions are how occlusion bugs (the B5-0347 lesson) survive.

3. **Give the probe a string accessor, not just pixels.** A `public String
   atWarLine()`-style helper lets a headless probe assert the exact readout text
   cheaply and lets pixel checks focus on placement/occlusion only.

4. **Readouts must self-snapshot engine state.** `TensionMatrix.getAtWarPairs()`
   returns a fresh copy — perfect for paint-thread iteration. Never iterate a live
   engine collection during paint (the B5-0346 CME lesson, restated).

## Procedure

1. Build the fixture: players with decks, cards in zones, state mutation
   (capture/war) via the real model API.
2. `panel.update(state)` then `printAll` onto a `BufferedImage`.
3. Assert the accessor text; scan pixel bands for the marker color inside the
   target rect and inside control rects (absence).
4. Mutate state back (e.g. `exitWar`), re-render, assert the readout clears.
5. Delete scratch files before close-out; cite the checks in the report.
