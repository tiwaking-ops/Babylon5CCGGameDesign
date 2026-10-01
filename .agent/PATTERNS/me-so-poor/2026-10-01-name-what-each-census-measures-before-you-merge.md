---
author_llm: me-so-poor
task: B5-1349 pattern
utc: "2026-10-01T00:24:11Z"
status: "advisory"
supersedes: none
---

# Name what each census measures before you merge the censuses

## Pattern

Before merging several censuses into one ranked queue, write down **what each one
measures**. Axes read alike and rank alike until they sit side by side — and at
least one of them is frequently not a gap at all.

Second half of the rule: re-derive every figure that arrived **via a chain of
reports** rather than from source, even when the chain is short and its links are
DONE.

## Why

B5-1349 merged four completed censuses. Two findings came out of the merge that no
single census could see:

1. **An axis that is not a coverage gap.** The four inputs measured registration (is
   this id a dispatch-table key?), trigger eligibility (does this aftermath record
   become playable?), cost composition (does `sponsorCost` waive a third term?), and
   type routing (can a `PLAY_CARD` carrying the wrong card type reach the generic
   sink?). Only the first and fourth are coverage gaps. B5-1127 had already warned
   that AFTERMATH's "0 registered" is *correct* because those records key on
   `triggerCondition` — but that warning only became load-bearing once the four axes
   had to share one ordering. The proposal now carries a "Read this before claiming
   anything" section for exactly this reason.

2. **A number that arrived by relay was wrong in its own task text.** The row text
   said "12 values covering 103 records undispatched", citing B5-1153. My B5-1347
   recount had already measured 113 over 14, because `WON_DIPLOMACY` is only half
   covered (`aftermath_united_front`, `de_am_united_front` in neither dispatch set).
   A ranking built on 103/12 would have silently deprioritised a half-covered value —
   and the wrong figure was in the instructions for the task that consumes it.

## Check to run

Two questions, in order:

- *What does each input actually measure?* If the honest answer for one of them is
  "a feature that does not exist yet" or "correct code over an empty set", it does
  not belong in the same ranked list as "missing row in a table" without a note.
- *Which of these figures came from source and which came from a report?* Re-derive
  the second group. The relay is trusted by everyone downstream, which is exactly why
  an error in it is invisible and expensive.

## Related

Pairs with the B5-1347 pattern (recount the derived quantities, not the raw ones).
That one covers re-deriving within a single census; this one covers the case where
the figure reaches you through another agent's prose and nobody re-ran it.