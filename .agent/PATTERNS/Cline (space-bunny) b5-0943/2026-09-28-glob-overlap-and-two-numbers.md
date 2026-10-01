---
document:
  title: "Two overlapping filename globs silently double-count — and a face can carry two numbers"
  status: "Pattern (advisory only, no authority)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0943"
---

# Reusable lesson: a headline count that is the sum of two globs needs a de-dup check

Two independent counting mistakes in one batch, both of which looked like correct arithmetic.

**1. Overlapping globs.** I counted `*_enh_*` = 37 and `*_fleet_*` = 37, and the row's
expected total was 72. But the union was 73, not 74: `de_enh_fleet_support_base` matches *both*
patterns, because a fleet-themed enhancement legitimately contains both substrings. A sum of two
globs is only valid if you have checked the intersection is empty — and here the intersection was
non-empty in the one case nobody would think to check. **When a count is derived by adding two
directory globs, take the de-duplicated union as the real number and treat the difference as a
finding about the naming scheme, not as rounding.**

**2. A card face can carry two numbers, and they mean different things.** Every FLEET face has a
military value in the top-left corner *and* an orb in the bottom-right circle. My only two
`cost` mismatches in the whole batch were both **exactly +1 high**, and on both cards the
military value was transcribed correctly. That asymmetry is the tell: whoever wrote those
`cost` values almost certainly read the corner glyph. I have recorded this as a plausible
mechanism, not a proven one — I did not find the commit.

The generalisable form: **when a numeric field disagrees with the card by a small constant, check
whether the card has a second number of the same shape before hunting for arithmetic error.** A
uniform ±1 across a subset of records points at transcription-from-the-wrong-position, not at a
calculation bug, and the two hypotheses call for completely different fixes.

**Applied:** B5-0943 closed at 73 unique scan-backed titles (36 ENHANCEMENT, 37 FLEET) instead of
the row's 39+33, with the two `FIRST_BATTLE` fleet `cost` values flagged rather than "corrected",
since correcting them would encode a hypothesis as fact in card JSON.
