---
document:
  title: "'Ordered or shuffled' is two questions with possibly different answers in one call chain - verify the producer preserves order AND some consumer does not re-shuffle before you describe the data's play semantics, and name the layer that reorders"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1449"
  supersedes: null
---

# "Ordered or shuffled" is two questions

**Observed in:** B5-1449, asked whether the 50-card fixed starter lists are
positional seeds the builder reshuffles or strict ordered openings. Both readings
were correct about different layers.

## The split

| layer | what it does to order | what it takes |
|---|---|---|
| `StarterDeckBuilder.build` | **preserves** it - walks resource entries in file order, appends `count` copies, no sort or branch on position. Measured 50 of 50 positions holding the card authored at that position | an ordered `List<Card>` |
| `model.Deck` constructor | **destroys** it - `drawPile.addAll(cards); shuffle();`, unconditional, no seed flag to suppress | any list, at construction |

So the honest sentence is "the builder emits authored order exactly, and the model
Deck shuffles it in its constructor". Neither half alone answers the question, and
the phrase that gets written by habit - "the builder reshuffles it" - is false
about the builder and vague about where the reordering actually lives.

## Why it is easy to get wrong

The producer is the file you were sent to read. It has a `Random` in it, a
`Collections.shuffle` in it, and a `setRandomSeed` test hook - so it *looks* like
the shuffling layer, and the shuffle you can see is on the random-10 candidate list
while the fixed list walks straight through untouched. A reader who stops at the
producer concludes the data is ordered. A reader who greps for `shuffle` concludes
the builder shuffles. Both wrong.

The layer that actually decides is usually the smallest class in the chain, and it
is where nobody looks: a value-object constructor.

## The procedure that worked

1. **Find every consumer of the producer's output**, not just the producer. One
   grep for the construction type (`new Deck(`) found five call sites and one
   definition; the definition was the answer.
2. **Read the constructor, not the class name.** `Deck` sounds like a container.
   Its constructor is a shuffle with a container attached.
3. **Measure both halves separately**, and make each measurement falsifiable by
   itself: `POSITION CHECK 50/50` for preservation, `0/200 identical openings,
   0/10 mean positional agreement` for destruction. Either number alone would have
   supported the wrong conclusion.
4. **Check the distribution, not just the extremes.** 0 identical openings is the
   headline, but "0 out of 200" could also mean "order never survives for a
   structural reason". Bucketing the first drawn card's source slot by decile
   (37/37/29/36/30/31 against 33.3 expected) is what distinguishes *shuffled* from
   *rotated* or *reversed*.
5. **Expect the id-level check to be a red herring.** 25 of 50 positions came back
   with a different id because of the Deluxe reprint substitution. Measuring ids
   suggested the order was corrupted at position 2. Measuring **titles** showed
   50 of 50 aligned. When a pipeline rewrites identities, compare the field that
   carries the semantics you care about.

## The general form

Data-shape questions dissolve into per-layer questions the moment a pipeline has
more than one stage. "Ordered or shuffled" is not a property of the data; it is a
property of the data *after the last stage that touches it*. Ask which stage is
last, and answer about that one - then say which class it was, because the next
reader will otherwise re-derive the whole chain.

A related trap in the same investigation: a `while` guard that only writes to stderr
makes an invariant *advisory*. The invariant held (60 cards, 0 missing), so the
question "is it enforced" and the question "does it hold" had the same answer that
day - and would have differed the first time a card record went missing. Measure
"holds" and "enforced" separately, and never let the first answer stand in for the
second.