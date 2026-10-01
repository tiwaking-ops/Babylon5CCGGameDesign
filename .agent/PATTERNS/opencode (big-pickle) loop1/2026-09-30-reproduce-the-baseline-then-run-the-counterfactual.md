---
document:
  title: "Reproduce the baseline, then run the counterfactual - a matched census proves stability, not that the mechanism matters"
  status: "Pattern (advisory only; never canonical, citing it confers no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1307"
---

# Reproduce it, then break it on paper

A cross-check of a predecessor's number has two halves, and only the first one is
usually done. B5-1307 was handed a settled measurement - "Deluxe reprints fill
109 of 200 fixed slots" - and asked for a per-faction delta plus one new
question about the cost key.

**Half one, the reproduction.** Re-measuring gave 25/25, 30/20, 28/22, 26/24 and
a total of 109: delta zero in every faction, plus the fixed half proved
seed-independent, so the number is not a lucky draw. That half is necessary and
it is also nearly content-free - a census that agrees with a census has found
nothing new.

**Half two, the counterfactual.** The useful move is to ask what the world looks
like if the mechanism under audit is switched off: seat each fixed slot's
same-title twin from the *other* set and recount. Result: 55 of 200 cost more than
the starting pool either way, and **0 slots change cost** - because all 91
reprinted titles carry identical costs on both records. So the 55-card opening
pressure belongs to the printed list, and the entire reprint-substitution
machinery is a measured no-op on the cost surface. That half answers a question
the reproduction cannot: not "is the number still true" but "does the thing you
are describing matter".

## The two questions to add to any "verify the prior number" row

1. **Is the number deterministic?** If it depends on a random draw, a matched
   rerun is luck, not verification - say which. (Fixed halves: deterministic.
   Random halves: seed-dependent, and two runs legitimately disagree - see
   B5-1303, where 1 of 40 seated conflicts on a different draw was not a
   contradiction of 2 of 44.)
2. **What changes if the mechanism is removed?** Run the counterfactual and
   report the number of affected items, including zero. "Zero slots change" is
   the finding; it is what stops a fixer spending a task on a no-op.

## Where this bites in this repo

Any row phrased as "cross-check X" is at risk of closing with a table that
reproduces X. The structure of the reprint path makes the trap sharp: every
Deluxe record is a reprint of a Premiere title, so the fixed list's Premiere ids
can *never* resolve for a reprinted title - the fallback is unconditional, and
delta zero is the only arithmetically possible outcome. An instrument that
compares the same derived quantity twice will report agreement forever and tell
you nothing about whether the fallback is correct, faithful, or harmful.

The counterfactual is also the cheapest way to find out whether a "faithfulness"
mechanism earns its keep: when the substitute and the original agree on `cost`,
`influenceReward`, `rarity`, `subtype` and `faction`, the mechanism is an
**identity** substitution, and every proposal to change it should be costed as a
no-op until someone shows a field where they disagree.

**Provenance note:** filed under `opencode (big-pickle) loop1`; supersede, never
rewrite - a sharper version of this is a new file linking this one.