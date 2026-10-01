---
document:
  title: "Card image intake — how to hand images over, and what happens next"
  status: "Investigation (advisory, never canonical; AGENTS.md §6)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0821"
---

# Card image intake

You asked how to give me card images so I can parse them. This is the answer.
**Nothing is extracted yet** — you said you will not supply data at this time, so
this folder is empty and no extraction has been run.

## How to hand them over

Put the image files in **`investigations/card-images/`** in this repository. That is
all. Drop them in whenever you like; there is no upload step and no format conversion.

**I can read image files directly.** You do not need to transcribe anything, and you
do not need Google Lens. If a card is legible in the image, I read it; if it is not
legible, I will say so per card rather than guess.

## Naming — this is the one thing that matters

Name each file after the card it is, so it can be matched to a record:

| Preferred | Example |
|---|---|
| **card `id`** from the pool | `char_jeffrey_sinclair.png` |
| title, spaces to underscores | `a_rising_power.png` |
| deck order number if neither is known | `0042.png` |

The 829 `id` values are listed in
`docs/reports/authored-card-pool-baseline-2026-09-28.md`'s source pool and are
unique, so `id` naming gives an exact match with no guessing. `.png`, `.jpg`, `.jpeg`
and `.webp` are all fine. Multiple images of the same card are fine — suffix them
(`a_rising_power_2.png`); the clearest one wins.

**One image per card** is the ideal. A photo of a spread of cards is readable but
much slower to check, and a misread card is the expensive failure.

## What happens when they arrive

1. **Diff, do not trust.** Each image is read and the extracted text is compared
   against the frozen baseline by `id`, producing `CHANGED` / `ADDED` / `REMOVED` /
   `UNCHANGED` with the specific fields named. A card whose text is unchanged
   against the current pool needs no action at all — which is the cheapest possible
   outcome and quite likely for many of them.
2. **Schema gate before content.** The acceptance specification in the baseline
   report is checked first: the 9 always-present fields, the enum domains
   (`type` 9, `subtype` 59, `rarity` 5, `faction` 7, `conflictType` 4,
   `fleetClass` 20, `set` 2, `timing` 1). An extracted value outside a domain is
   reported, not applied.
3. **Deletions are reported loudly.** A card missing from a supplied set is a
   `REMOVED: n` line, never a silent gap.
4. **Nothing is written to `b5ccg/resources/cards/` without a separate claim.** The
   pool is frozen at SHA-256 `FAC0747A…` (premiere, 446) and `2F74FA4D…`
   (deluxe, 383). Any change is a claimed, compiled, reported task.

## About the ccgtrader + Google Lens route you have been using

Useful, and it explains how you have the text you do — but note what it produces:
text **derived from a third-party site by a lens tool**, one card at a time, with no
schema check. Two consequences worth stating plainly:

- **It is unauditable in bulk.** If it were re-run, results could differ. That is
  acceptable for a human reading eight cards and not acceptable as a data source.
- **The current pool is a hand-authored paraphrase layer, not imported text** — 110
  records carry inline `(Deluxe text change: …)` annotations and the deluxe set is a
  strict reprint subset (383 in both sets, 63 premiere-only, 0 deluxe-only). So
  correct printed text arriving here is a **deliberate divergence from the current
  design layer**, not a bug fix, and it is exactly the question the withdrawal of
  B5-0654 (2026-09-28) leaves open: what card-data policy replaces it.

Nothing needs deciding before you drop images in. The diff will tell us how much is
actually different, which is the input to that decision.

## Do not put anything here that is not card imagery

This folder is advisory (same tier as `investigations/`), and copying or citing it
confers no authority. Game data that becomes canonical belongs in
`b5ccg/resources/cards/` under its own claimed task, and rule interpretations belong
in `docs/DECISIONS.md`.
