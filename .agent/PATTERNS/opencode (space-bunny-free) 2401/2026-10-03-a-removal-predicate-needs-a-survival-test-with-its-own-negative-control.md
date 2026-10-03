---
document:
  title: "A removal predicate needs a survival test with its own negative control"
  status: "Pattern (advisory only; never canonical - copying or citing confers no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2401", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2401", version: "space-bunny-free"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# A removal predicate needs a survival test with its own negative control

Filed from **B5-2412** (2026-10-03). Supersedes nothing; first record.

## The shape of the trap

The task was a removal: at setup, delete every extra copy of a player's Starting
Ambassador. The fix is a predicate - "is this card a duplicate?" - and a predicate has
two ways to be wrong that look identical from the green suite:

* **too narrow** - the defect survives, and the suite that only checks "the bad thing
  is gone" is still green on the cases you did not think of;
* **too broad** - it also deletes cards the rules keep, and *every* assertion the
  narrow version passes, the broad version passes too.

A removal fix gets a test that proves the removal happened. That test cannot tell the
two versions apart, because the broad version removes strictly more.

## What made it visible

The shipped card pool contains `char_delenn_transformed` - "Delenn Transformed",
MINBARI, RARE - with `isAmbassador: true`. It is a legal Minbari character and it is
**not** the rulebook's one Starting Ambassador per race. So the broad predicate, which
is the one you write by default because the flag is right there, deletes a real card
from a real deck.

Nothing about that is exotic. A boolean flag named after a role will eventually be set
on a card that has the role's *properties* without having the role. The rulebook said
"Jeffrey Sinclair, Londo Mollari, Delenn, G'kar" - four names, and identity is the
printed card.

Rulebook :888/:890 handed me the second instance for free: the Non-Aligned player has
no single ambassador and *begins with a second species ambassador in hand*. Two
ambassador-flagged, same-faction cards where the rules keep both.

## The rule I now use

For any fix that removes things, write two sets of checks and make each one go red:

1. **Removal checks** - the defect is gone. Control: disable the removal. These go red.
2. **Survival checks** - the named things the rules keep are still there. Control:
   *widen* the predicate, do not disable it. These go red.

The second control is the one people skip, because under control 1 the survival checks
stay green: a removal that removes nothing trivially removes nothing it should not
have. **A survival check cannot be validated by the defect's own negative control**,
which is the whole reason it needs a separate one.

Record both numbers. In this case:

| control | edit | result |
|---|---|---|
| disable the removal | predicate forced `false` | FAILED 2 of 892 - the two removal checks |
| widen the predicate | drop the title clause | FAILED 2 of 892 - the two survival checks |

Same suite, same count, opposite polarity. That symmetry is the tell: if your control
produces the same failures as the previous one, your control is not testing anything
new.

## Three corollaries I hit in the same pass

**A fixture that under-counts its own defect passes against a no-op fix.** My census
assertion read `sinclairBefore == 2` where the truth was 4 - I had excluded the copy
that gets seated, because in my head "duplicates" meant "the ones that are not the
seated one" and the seated one is itself sitting in a hand I was counting. It printed
FAIL, which is the only reason I know. Count what you constructed, and let the number
be the number.

**Verify an order-preserving rebuild with a check, not a comment.** With no API to
remove a card from a pile, I drained and re-added in the same order. That is a claim
about two methods on a `LinkedList`, and it holds - but "holds because of how those two
methods work" is exactly the kind of claim that rots when someone changes one of them.
One assertion (`the duplicate-free pile keeps its exact order, losing only the 3
drawn`) turned a comment into a gate.

**The defect's origin can be outside your scope, and the reader-side fix is still
complete.** The duplicate was *created* by a pin in another package, one directory up
the tree. Fixing the reader makes the writer's duplicate unreachable, so the change is
a fix rather than one path of a fix - and the writer-side change can be reported as a
follow-up instead of being smuggled in under a scope that does not cover it.

## One-line form

A removal fix's over-broad version passes every test the fix passes; give the survival
checks their own control, by widening the predicate rather than disabling it, and make
the two controls fail in opposite directions.
