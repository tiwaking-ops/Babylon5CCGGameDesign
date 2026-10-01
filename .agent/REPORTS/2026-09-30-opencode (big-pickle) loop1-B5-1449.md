---
document:
  title: "B5-1449 close-out - the 50-card fixed starter lists are POSITIONAL SEEDS the model Deck reshuffles, not strict ordered openings: the builder emits authored order 50/50, then Deck's constructor shuffles unconditionally (0 of 200 openings matched the seeded order), no position is special-cased, the ambassador is found by property not slot, 0 of 48 fixed entries are dropped, and the fixedCount guard is stderr-only so a missing record would silently yield a 59-card deck"
  status: "Report (no authority; read-only measurement)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1449"
  instrument: "b5ccg/out/b51449/b51449/B51449FixedOrderProbe.java (javac -source 6 -target 6, production DeckLoader.loadBothSets + StarterDeckBuilder.build + model Deck, read-only, no harness); receipts b5ccg/out/b51449/b51449-receipt.txt and b5ccg/out/b51449/b51449-stderr.txt"
---

# B5-1449 - seeded versus shuffled: the fixed lists are membership, not order

**Claim:** `.agent/CLAIMS/B5-1449.json`, `opencode (big-pickle) loop1`, started
2026-09-30T23:45:53Z, released at close-out. Row is ungated ("gated none claimable
immediately"), read-only plus git-ignored scratch probe. **No deck JSON, no card
JSON, no src file edited; no commit; no push.** Baselines consumed: the B5-1149 and
B5-1317 receipts. Fenced: B5-1305 (DONE, double-seated-record question) and
B5-1345 (OPEN, claimed by `solar-pro4:free`, cost histogram).

## The answer in one sentence

The authored order survives the builder perfectly and is then destroyed by the
model layer: **`StarterDeckBuilder` emits the fixed 50 in exactly the authored order
(50 of 50 positions), and `new Deck(cards)` shuffles it unconditionally**, so the
fixed lists are positional seeds that the deck constructor reshuffles - **not**
strict ordered openings. No position is special-cased. Nothing is dropped today,
but the guard that would notice a dropped entry writes to stderr only.

## Half 1: the builder preserves order exactly

`StarterDeckBuilder.build` (`b5ccg/src/b5ccg/engine/StarterDeckBuilder.java:110-128`)
walks `loadEntries()` in resource-file order, resolves each entry by id, falls back
to title for the Deluxe reprint, and appends `count` copies. There is no shuffle, no
sort, and no position-dependent branch anywhere in that loop. Measured through the
production path with HUMAN:

```
POSITION CHECK: fixed slot i holds the card authored at slot i (title match) = 50/50
  zero divergence  -> emitted order IS the authored order
```

The one thing that *does* change between authored and emitted is the record
identity, not the position: 25 of 50 positions come back with the same id and 25
with the Deluxe twin's id (authored `conf_affirmation_of_power` at pos 2 emits as
`de_conf_affirmation_of_power`). That is the expected reprint substitution from
B5-1307, and it is positional: the card at slot i is the card authored at slot i.

The builder *does* shuffle once, and it is not the fixed list -
`Collections.shuffle(candidates, rng)` at `StarterDeckBuilder.java:162` shuffles
the random-10 candidate pool before picking. Naming the seam matters: the phrase
"the builder reshuffles the fixed list" is wrong; the builder is order-preserving
and `Deck` is what reshuffles.

## Half 2: the model Deck throws the order away

`b5ccg/src/b5ccg/model/Deck.java:9-12`:

```java
public Deck(List<Card> cards) {
    drawPile.addAll(cards);
    shuffle();
}
```

Unconditional, in the constructor, with no seed or flag to suppress it. Every
consumer wraps a seeded list that way: `HeadlessSmokeTest.java:86`,
`HeadlessMultiRoundTest.java:106`, `HeadlessHumanSeatProbe.java:131`,
`HeadlessStallSoakProbe.java:157`, `HeadlessReportingTiebreakTest.java:467`.
Measured over 200 fresh `Deck` constructions of the same seeded HUMAN list:

```
openings identical to the seeded order: 0/200
mean positions agreeing with seeded order in a 10-card opening: 0/10
source slot of the first drawn card, by decile:
  seed-slot[0-9]:37  [10-19]:37  [20-29]:29  [30-39]:36  [40-49]:30  [50-59]:31
```

Zero of 200, zero of ten, and the first drawn card's origin spread across all six
deciles of the 60-card list against an expected 33.3 each. That is a uniform
shuffle of all 60 cards - the random ten included - so a card's authored position
carries **no** information about when it can be drawn.

**Consequence:** every statement of the form "card X sits at position N of the HUMAN
fixed list" is true of the data and false of play. Only membership survives.

## Special-cased positions: none

No positional logic exists to special-case. The ambassador in particular is found
by **property, not slot**: `StarterDeckBuilder.findAmbassador`
(`StarterDeckBuilder.java:178-188`) scans the entire deck for a `CharacterCard`
with `isAmbassador()` and a matching faction, mirroring
`GameController.findAmbassador`. Measured: HUMAN's ambassador is
`de_char_jeffrey_sinclair` at authored position 19 of 50 with exactly one copy -
an ordinary entry. The `de_` prefix also corroborates B5-1149's finding that every
faction ambassador seats as the Deluxe reprint.

## Dropped entries: zero today, silent by construction

All 48 authored HUMAN entries resolve; `build()` returns exactly 60 cards; no
`StarterDeckBuilder` guard fired. The mechanism is worth recording anyway, because
it is advisory rather than enforced:

* `StarterDeckBuilder.java:120` - an unresolvable entry is appended to `missing`
  and the loop `continue`s. No throw.
* `:129-132` - the missing list goes to `System.err` only.
* `:133-136` - the `fixedCount != FIXED_TARGET` guard also writes to `System.err`.

Nothing surfaces to the UI, and nothing tests these lines. A pool missing one fixed
record would produce a silent **59-card** deck with two stderr lines that no player
or test harness reads. Reported as an observation, not a finding to fix: engine is
one-writer at a time, B5-1047 holds the live engine claim, and this row forbids src
edits.

## Two smaller measurements

* **Multiplicity reaches 50, not 49 entries.** HUMAN's 50 slots come from 48
  entries because `event_level_the_playing_field` carries `count: 3`. B5-1305
  recorded the same "extra copies make the count land on 50" mechanism for NARN's
  `conf_limited_strike`; the per-faction double/triple-seat table stays with
  B5-1305's receipt and this row only notes that HUMAN is a second instance.
* **The random ten never inflate the seeded membership.** 0 title collisions with
  the fixed 50 across 200 seeds, even though fixed lists carry Premiere ids while
  the pool serves Deluxe records - because the exclusion at
  `StarterDeckBuilder.java:145-157` is by **title**, derived from `fixedIds` over the
  pool. Confirmed working.

## One dead-code observation

`build` maintains a local `fixedTitles` (`StarterDeckBuilder.java:105`, populated at
`:127`) that is never read. The title exclusion that actually runs is the separate
`fixedTitles` inside `drawRandomUncommonsRares` (`:145-149`). Harmless, but a reader
could conclude the exclusion is applied twice. Not edited on this row.

## Why the two dependent rows are unaffected

B5-1345's cost histogram and my B5-1315 affordability counts are both
order-independent by construction - they measure membership, and membership is
exactly what the shuffle preserves. The shuffle does make one of B5-1345's framing
questions simpler than it looks: under a uniform shuffle any fixed-list card can
appear in any opening, so first-turn reachability is a question about the cost
curve alone, not about which card happens to sit at slot 0.

**Reusable lesson:** "is this list an ordered thing or a bag of things" is two
questions, not one, and they can have different answers in the same call chain -
check whether the builder preserves order AND whether any consumer downstream
re-shuffles before describing the data's play semantics, and name which class in
which layer does the reordering.**