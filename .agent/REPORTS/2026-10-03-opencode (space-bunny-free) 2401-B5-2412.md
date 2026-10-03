---
document:
  title: "B5-2412 report - one Starting Ambassador per faction player at setup"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2401", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2401", version: "space-bunny-free"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# B5-2412 - one Starting Ambassador per faction player at setup

Claim: `.agent/CLAIMS/B5-2412.json`, tool-stamped `started_utc` 2026-10-03T11:37:14Z,
scope `b5ccg/src/b5ccg/engine/` only. Agent `opencode (space-bunny-free) 2401`.

## The defect, and where the extra copy is born

The row's premise was that `GameController.findAmbassador` removes only the first
copy. True, and incomplete: the copy is not *found* twice, it is **created**.

`b5ccg/Main.java` lines 55-66, the real boot path:

```java
List<Card> factionCards = buildFactionDeck(allCards, p.getFaction());
Deck deck = new Deck(factionCards);          // shuffles in the constructor
// B5-0319: pin the race ambassador to the draw-pile top ...
CharacterCard amb = StarterDeckBuilder.findAmbassador(factionCards, p.getFaction());
if (amb != null) deck.addToTop(amb);         // <-- same object, still in factionCards
p.setDeck(deck);
p.drawCards(4);                              // initial hand
```

`StarterDeckBuilder.findAmbassador(List<Card>, Faction)` walks `factionCards` and
returns a reference **from that very list** (`StarterDeckBuilder.java:297-303`). The
card is therefore already inside `deck`, and `addToTop` puts the *same object* in a
second time. `new Deck(...)` shuffles on construction, so the pin is added after the
shuffle - the top card is the ambassador, and the original copy sits at a uniformly
random position in the remaining pile.

Then `p.drawCards(4)` deals four cards, and `GameController.setupGame` extracts one
ambassador from the hand. When the shuffle put the original copy at position 2, 3 or
4, the dealt hand contained **two Jeffrey Sinclairs** and only the first was removed.
That is the reported screenshot. Measured consequence: the surviving copy is not merely
inert in hand, it is a legal character to Sponsor or Promote, and the same defect also
sat in the draw pile for the whole game waiting to be drawn.

So the correct place for the fix is the reader (`setupGame`), not the writer
(`Main.java`, which is outside this row's engine-only claim). Fixing the writer too
would be a second change in a file I do not hold; the reader-side sweep makes the
writer's duplicate unreachable either way, which is why it closes the defect rather
than one path of it.

## The fix

`GameController.setupGame` (GameController.java:212-226), after the ambassador is
seated in the Inner Circle, calls a new private
`sweepDuplicateStartingAmbassadors(p, amb)` (GameController.java:251-330) and logs
the count. Engine/ only; nothing outside `b5ccg/src/b5ccg/engine/` was edited.

Three decisions in that method are the substance of the change.

**1. The predicate is the printed card, not the flag.** A duplicate is
same-faction + `isAmbassador()` + **same title** as the seated card.

*Why not the flag alone:* shipped data contains `char_delenn_transformed`
("Delenn Transformed", MINBARI, RARE) with `isAmbassador` **true**, and its Deluxe
mirror `de_char_delenn_transformed`. Rulebook :230 names exactly one Starting
Ambassador per race - Jeffrey Sinclair, Londo Mollari, Delenn, G'kar - and Delenn
Transformed is not on that list; it is a legal Minbari character. A
`isAmbassador()`-alone sweep would delete a card the rulebook never removes, from a
60-card deck, at every setup.

*Why title and not id:* the pool is deduped by set, so the same printed card exists
under `char_` and `de_char_` ids. `StarterDeckBuilder` already resolves fixed-list
slots by title for exactly this reason (`findFixedByTitle`, and the comment at
`StarterDeckBuilder.java:192-194`).

**2. Duplicates leave the game, not the discard pile.** `Deck.recycleDiscard()`
shuffles the discard pile back into the draw pile. A discarded duplicate would come
back on a later reshuffle and the defect would return a round later, so the sweep
drops them. The rulebook has no opening discard either (:228 is a *selection*, not a
draw-then-discard).

**3. The draw pile is drained and rebuilt, and that is a measured property.**
`Deck` exposes no draw-pile enumeration and no removal, and `Deck` lives in
`model/` - outside this row's claim. The sweep drains with `draw(deck.size())` and
rebuilds with `addToBottom` in the same order. `drawPile` is a `LinkedList`, `draw`
removes from the front and `addToBottom` appends at the back, so the order is
identical by construction. That is a claim about two methods, so a check pins it
rather than a comment asserting it (see `SETUP-AMB` below). Cost: one pass over a
60-card pile, once per player, at setup.

## The pinned checks - `SETUP-AMB`, 9 assertions

Driven through the **real private `setupGame`** by reflection, not by calling the
sweep helper directly, for the B5-2420 reason: a helper call asserts the helper, a
setup call asserts the wiring. One `GameState`, one `setupGame`, four seats chosen so
the predicate is pinned in **both** directions:

| seat | fixture | assertion |
|---|---|---|
| HUMAN | 4 printed Jeffrey Sinclairs (2 set ids, one dealt into the hand as in the screenshot) | 1 survives, 0 in hand + draw pile + discard pile |
| MINBARI | 2 Delenn + 2 `Delenn Transformed` | Delenn collapses to the seated one, **both Delenn Transformed copies survive** |
| NARN | no duplicate at all | pile keeps its **exact** order, losing only the 3 cards `drawCards(3)` takes |
| NON_ALIGNED | one seated species ambassador + a second species ambassador | **both survive** (rulebook :890) |

Plus: every seat's ambassador is seated and is an Inner Circle member (:266/:270),
every discard pile is empty after setup, and the sweep announces itself in the log.

The survival checks are the ones worth explaining. Rulebook :888 says the League has
no single ambassador and :890 says a Non-Aligned player *begins with a second species
ambassador in hand*. That seat is in the fixture precisely because a future reader
"simplifying" the predicate to `isAmbassador()` would delete a card the rulebook
explicitly keeps - and a green suite would not have noticed.

## Both halves proved red, separately

Per AGENT_LOOP's "a test never observed red is not evidence", and because the two
halves of this change fail in opposite directions:

| control | edit | result |
|---|---|---|
| 1 - no sweep | `isSameStartingAmbassador` forced `false` | `CONFORMANCE SUITE FAILED (2 of 892)`: *no copy of a seated starting ambassador is left in any zone* + *the sweep is announced in the game log* |
| 2 - over-broad sweep | title clause dropped, predicate `isAmbassador()` + faction only | `CONFORMANCE SUITE FAILED (2 of 892)`: *a same-fation ambassador-FLAGGED card of another name survives* + *the Non-Aligned second species ambassador survives* |

Control 1 is the defect detector. Control 2 is the only thing that can prove the
survival checks are not decoration - under control 1 they stay green, because a sweep
that removes nothing trivially removes nothing it should not have. `GameController.java`
was restored byte-identically between controls and at the end: SHA-256
`81B9418992340608879CA1F91F351AEFC187CF71677E6E6756BFB1844C219B67`, no
`NEGATIVE CONTROL` string left in the file.

**My own first assertion was red before it was right, and that is on the record.** The
fixture census read `sinclairBefore == 2 && delennBefore == 1` and printed FAIL. The
real counts are 4 and 2, because the copy that gets seated is itself sitting in the
hand, and my "duplicates" figure silently excluded it. A fixture that under-counts
its own defect is a fixture that would have passed against a no-op fix. Same species
as the B5-2363 pipe-count classifier: verify the classifier against a case whose
answer you already know.

## Gates

* `RUN_TESTS=1 sh compile.sh` exit **0** - `CONFORMANCE SUITE PASSED (892 checks)`
  (883 before, +9), smoke `PASSED`, five probes `PASSED`, javac `1.8.0_292`.
* Java 6 construct census (`.agent/tools/census-b50960.py`, `PYTHONIOENCODING=utf-8`,
  `py`): under `=== TRACKED JAVA b5ccg/src 82 files ===` **code-lines 0** for arrow,
  methodref, stream, computeIfAbsent, computeIfPresent, compute, merge,
  @FunctionalInterface, try-with-resources, diamond, forEach, removeIf; `arrow` prose
  72 lines / 88 occurrences, all comments; `getOrDefault` 14, all the project's own
  unqualified `DeckLoader.getOrDefault(Map, key, def)`; **qualified `.getOrDefault(` 0**.
  The frozen archive reads the opposite, as the instrument expects.
* Files I edited: `b5ccg/src/b5ccg/engine/GameController.java`,
  `b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java`. Nothing else. The live `ui/`
  worker (B5-2381) was left alone throughout; `ui/DeckBuilderModel.java` and
  `ui/MainWindow.java` carry other agents' diffs, not mine.

## Reported, not fixed

1. **The `char_delenn_transformed` flag is a data question.** It is a RARE Minbari
   character carrying `isAmbassador: true`, which no rulebook rule supports: :230 lists
   one Starting Ambassador per race and this is not it. I did not edit card JSON -
   out of scope, and `DeckLoader`'s deck validator treats *any* `isAmbassador` card as
   satisfying :195, so a fix there is a ruling, not a drive-by. Flagged for a ruling.
2. **A pre-existing discard pile is not swept.** `Deck.getDiscardPile()` returns an
   unmodifiable view, so no removal is possible from `engine/`. It does not matter at
   setup because setup discards nothing (asserted), but the scope needed to close it
   fully is `b5ccg/src/b5ccg/model/Deck.java`.
3. **`Main.java`'s `addToTop` pin still creates a duplicate object** and the sweep now
   absorbs it. The writer-side fix is a one-line change in a file outside this claim;
   worth doing so the defect is not created and then removed every game.

## Reusable lesson

A removal predicate needs a survival test **with its own negative control**: the
over-broad version of a removal fix passes every assertion the fix passes.
