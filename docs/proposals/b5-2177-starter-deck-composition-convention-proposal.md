---
document:
  title: "Starter-deck composition convention — quota + ambassador-pin (draft)"
  status: "Proposal (docs/proposals; not truth until merged per AGENTS.md §3–4)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5-3-flash"}
  assessor_llm:
    - {name: "Kilo (kilo-auto/free) 31", version: "kilo-auto/free", passes: 1, last_pass: "2026-10-03", note: "edit: re-measured all 20 cited anchors byte-exact against the live tree, added the rulebook anchors that make the harness-only claim provable, and recorded four measured findings of which F1 and F2 contradict claims this document previously made (B5-2177 close-out pass)"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 31", version: "kilo-auto/free"}
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
  task: B5-2177
---

# Starter-deck composition convention (draft)

## What this documents

The harness in `b5ccg/src/b5ccg/engine/HeadlessSmokeTest.java` builds the four
all-AI decks heuristically (fallback when the B5-0319 printed-deck resource is
absent), and two B5-0313 invariants keep that fallback *exercises the conflict
pipeline* rather than degenerating into filler-only decks:

1. **Quota pass (type floor):** each deck is guaranteed **at least 12
   `CONFLICT` cards and at least 2 `AGENDA` cards** before neutral/ANY
   fillers top it up. Cap **60 cards**.
2. **Ambassador pin (position pin):** after deck construction and shuffle, the
   faction's ambassador is **pinned to the draw pile's top**
   (`Deck.addToTop`) so the ambassador is guaranteed in the opening hand.

Neither invariant is game-logic; both are harness-side (test/`engine` files,
not `model/`), so this convention is a **harness composition rule**, not a
deck-building rule for human play. It is recorded here so a future edit that
touches `buildFactionDeck` or the deck-construction loop knows which invariants
must be preserved.

## Why this is a harness rule and not a rulebook rule

*Added 2026-10-03 by the B5-2177 close-out pass. The original draft asserted
"neither invariant is game-logic" without citing a rulebook line, which left its
central claim unprovable from the document. These are the measurements that
settle it.*

The rulebook constrains **play decks**, and it constrains them differently from
this harness:

| Rulebook | Says | Consequence for this convention |
|---|---|---|
| `:132`, `:197` | "Each player must play with a **minimum of 45 cards**… There is **no maximum** number of cards that may be in a play deck." | The 60 in this harness is **not** a rules ceiling. Play decks have a floor and no cap. |
| `:134` | Starter decks are sold as **60 cards** — 50 fixed commons (including the Ambassador) plus 10 random uncommon/rare. | 60 is the **retail product size**, which is why the harness borrowed it. Nothing in the rules requires a player deck to be 60. |
| `:193`, `:203`–`:206` | Build by choosing agenda card(s) first, then cards that support them; race-restricted cards; off-race characters cost double. | The rules give a **method**, not a conflict/agenda ratio. No rule mandates any number of conflicts. |
| `:199`, `:230` | "Must contain **one** Starting Ambassador." | Read by this project's own validator as **at least** one — `DeckLoader.java:73` and `:103`–`:108` set `hasAmbassador` and never count. See finding **F2**. |
| `:266` | "Next, all players should play their starting Ambassador cards down in front of them." | Under the rules the ambassador goes **straight into the faction**, not through the deck. The harness pin exists only because `GameController.setupGame()` extracts it from the hand instead. |

So: the **12 + 2 quota is pure harness heuristic** with no rulebook basis at all,
the **60 ceiling is a borrowed product size** rather than a rule, and the
**ambassador pin is a harness simulation of `:266`** compensating for a
setup shortcut. A future editor should preserve the quota because the smoke
scenario needs a conflict, not because any rule requires it.

## File + line evidence (measured 2026-10-03 against the working tree)

All line numbers cite `b5ccg/src/b5ccg/engine/HeadlessSmokeTest.java` unless
stated otherwise.

### Invariant 1 — quota pass

| Line | What it shows |
|---|---|
| 251–259 | Javadoc on `buildFactionDeck`: "Faction cards first, then quota guarantees, then neutral/ANY fillers, capped at 60 cards" + B5-0313 rationale (B5-0312 finding: 108 conflict cards all faction ANY, filler position #42+, so file-order filler cut excluded every conflict card). |
| 275 | Pass 1 loop cap: `deck.size() < 60` — own-faction cards in file order. |
| 279 | Comment: `// 2. Quota guarantees: 12 conflicts + 2 agendas per deck.` |
| 281 | Quota loop 1: `conflicts < 12`, adds every `CardType.CONFLICT` in file order. **No `deck.size()` test.** |
| 286 | Quota loop 2: `agendas < 2`, adds every `CardType.AGENDA` in file order. **No `deck.size()` test.** |
| 291 | Pass 3 loop cap: `deck.size() < 60` — neutral/ANY fillers top up the cap. |

**Correction (2026-10-03, B5-2177 close-out pass).** This table originally
listed `:275` and `:291` as "the" caps without noting that the quota pass
between them is **uncapped**, and the paragraph below repeated "60-card cap" as
though the code enforced it. Both statements are wrong; see finding **F1**. The
accurate statement: the quota pass is bounded by *card supply* (12 conflicts, 2
agendas) and not by *deck size*, so the 60 figure constrains passes 1 and 3 only.

Ordering consequence, restated accurately: pass 1 and the quota pass walk `all`
independently with separate bookkeeping and share no membership test, so the
quota cards are appended **after** pass 1 has already filled to its cap. The 60
is therefore **not a ceiling on the finished deck** — it is a ceiling on each
capped pass, and the total is `pass 1 (≤60) + 14`.

### Invariant 2 — ambassador pin

| Line | What it shows |
|---|---|
| 87–91 | Comment: "B5-0313: guarantee the faction ambassador is in the opening hand — `GameController.setupGame()` extracts it from the hand only, and without it every conflict resolves 0 vs 0 (B5-0312 playtest finding). `Deck` shuffles in its constructor, so pin the ambassador to the draw pile's top AFTER construction." |
| 92 | `CharacterCard amb = findAmbassadorCard(deckCards, FACTIONS[i]);` |
| 93 | `if (amb != null) p.getDeck().addToTop(amb);` — issued AFTER `new Deck(deckCards)` (line 86), i.e. after `Deck`'s constructor has already shuffled. **This inserts a second copy rather than moving the card; see finding F2.** |
| 303 | `findAmbassadorCard` helper: mirrors `GameController.findAmbassador`'s rule — first `CharacterCard` in the list with `isAmbassador() == true` and `getFaction() == faction`. |

Supporting rule cited at line 88: `GameController.setupGame()` (see
`b5ccg/src/b5ccg/engine/GameController.java:100`) extracts the ambassador
from the hand only; an ambassador that is not in the opening hand is simply
not in the conflict pipeline. This is why the pin is mandatory for the smoke
scenario to exercise any conflict at all.

### Why AFTER construction

`Deck`'s constructor calls `shuffle()`
(`b5ccg/src/b5ccg/model/Deck.java:9–12`), and `addToTop` is `drawPile.addFirst(c)`
(`Deck.java:54`). If the ambassador were not pinned after the shuffle, it
would be somewhere in the 60-card deck and could be on top or bottom with no
guarantee; `setupGame` only looks at the opening hand, so an unpinned
ambassador means a 0-vs-0 conflict (B5-0312 headline).

## Interaction with the B5-0319 printed-deck path

`buildFactionDeck` now short-circuits when the B5-0319 `StarterDeckBuilder`
resource is available (`HeadlessSmokeTest.java:262–268`):

```
if (StarterDeckBuilder.isAvailable()) {
    try {
        return StarterDeckBuilder.build(faction, all);
    } catch (Exception e) { ... }
}
```

The comment at line 263–264 says it plainly: "the fixed lists already contain
conflict cards + an agenda, so the B5-0313 quota passes are only needed for
the heuristic fallback." The ambassador-pin loop (lines 92–93) still runs
after **either** path — it is independent of which builder produced the deck.
If a future edit makes the printed-deck path stop guaranteeing a conflict or
agenda card, the quota floor for that path must be re-verified separately.

## Findings from the B5-2177 close-out verification pass (2026-10-03)

The original draft's **20 cited anchors were re-measured against the live
working tree and all 20 hold byte-exact** — not one line drifted, even though
`engine/` was under an active foreign claim (`B5-2001`, `pi (poor-pi)`) while
this pass ran. The evidence table is sound. What the draft did *not* do is
measure the invariants it was recording, and two of them do not hold.

The card census below is over all **829** live records —
`premiere.json` 446 + `deluxe.json` 383 — read-only, no card JSON touched.

### F1 (P1) — the 60-card cap is not enforced; the live harness deck is 74 cards

`buildFactionDeck`'s Javadoc at `:251`–`:252` says "capped at 60 cards" and the
pass-3 comment at `:290` repeats "capped at 60". Pass 1 (`:275`) and pass 3
(`:291`) both test `deck.size() < 60`; the quota pass (`:281`, `:286`) tests
only its own counters, so it appends 14 cards regardless of deck size.

Measured own-faction card counts, which are exactly what pass 1 admits:

| Faction | own-faction records | pass 1 result |
|---|---|---|
| MINBARI | 72 | saturates the cap at 60 |
| CENTAURI | 62 | saturates the cap at 60 |
| HUMAN | 61 | saturates the cap at 60 |
| NARN | 61 | saturates the cap at 60 |
| LEAGUE (`NON_ALIGNED`) | 8 | 8, then quota, then fillers |

All four factions that own a printed ambassador saturate pass 1, so the quota
pass then appends its 12 conflicts and 2 agendas past the stated ceiling, and
pass 3 adds nothing at all because `deck.size()` is already 74. **The harness
deck is 74 cards, not 60** — and 75 in the draw pile once F2 is included.

This violates no rule: `:132` sets a 45-card floor and explicitly no maximum.
It matters because this document's whole purpose is to tell a future editor
which invariants are load-bearing, and it was recording a ceiling the code does
not have. `engine/` scope, so **reported and seeded, not fixed here**.

### F2 (P1) — the ambassador pin duplicates the card instead of relocating it

The draft calls `addToTop(amb)` a "pin" and describes it as guaranteeing the
ambassador is in the opening hand. It does — by **inserting the same card object
a second time**:

1. `:86` `p.setDeck(new Deck(deckCards))`, and `Deck`'s constructor does
   `drawPile.addAll(cards)` (`Deck.java:10`) — so every card in `deckCards`,
   **including the ambassador**, is already in the draw pile.
2. `:92` `findAmbassadorCard(deckCards, FACTIONS[i])` searches **that same
   list**, so it can only succeed by returning a card already in the pile.
3. `:93` `addToTop(amb)` is `drawPile.addFirst(c)` (`Deck.java:54`) — an
   **insert**, with no removal from the old position.

`Deck.draw(int)` (`Deck.java:22`–`:28`) removes from the front, so the inserted
copy is guaranteed in the opening four and the original stays buried mid-deck.
The live card data makes the condition unconditional rather than hypothetical:
**10 records carry `isAmbassador: true`** (`char_jeffrey_sinclair`, `char_gkar`,
`char_delenn`, `char_londo_mollari`, `char_delenn_transformed`, and the five
`de_`-prefixed deluxe twins), all `type: CHARACTER` with a faction, so
`findAmbassadorCard` always finds one and the double-add always runs.

**This is a harness-fidelity defect, not a rules violation, and the distinction
is load-bearing:** `:199`/`:230` read "must contain one Starting Ambassador",
but this project's own validator implements that as *at least* one —
`DeckLoader.java:73` and `:103`–`:108` set a `hasAmbassador` flag and never
count — so `validatePlayDeck` accepts the doubled deck. The real damage is that
the smoke test plays a deck holding two copies of one card, which no physical
deck can be, and every card-count assertion made against it is off by one.

### F3 (P2, refuted — recorded because a refutation is evidence too) — the quota pass cannot duplicate cards *on today's data*

The quota loops walk `all` independently and never test membership, so a quota
pick already added by pass 1 would be added twice. That does not happen now,
and the reason is an accident of ordering rather than a guard:

* all **108** `CONFLICT` records are `faction: ANY` — the draft's "all 108 are
  faction ANY" is correct — and pass 1 admits only own-faction cards, so the two
  passes cannot collide;
* the first two `AGENDA` records in load order are `agenda_a_rising_power` and
  `agenda_as_it_was_meant_to_be`, **both `ANY`**;
* 12 of 47 agendas are faction-specific (`MINBARI` 4, `HUMAN` 3, `CENTAURI` 3,
  `NARN` 2) but never reach the quota loop, because file order puts the `ANY`
  agendas first.

So the no-duplicate property is **data-dependent, not enforced**. Reordering the
card files, or promoting a faction-specific agenda above the first two `ANY`
ones, silently introduces duplicate cards with no gate to catch it.

### F4 (P2) — the printed-deck path's quota is asserted by comment, never measured

`buildFactionDeck` short-circuits to `StarterDeckBuilder.build` at `:265`–`:267`
whenever `StarterDeckBuilder.isAvailable()`. The claim that this path needs no
quota rests entirely on the comment at `:263`–`:264` — "the fixed lists already
contain conflict cards + an agenda". No assertion anywhere checks it. The
ambassador pin at `:92`–`:93` runs after **either** path, so F1 and F2 apply to
the printed-deck path as well; whether *it* independently satisfies 12 + 2 is
**unverified**, and this pass did not measure it because `StarterDeckBuilder`
is `engine/` and read-only here.

### Not done, and why

`engine/` is held by a live foreign claim (`B5-2001`), so F1, F2 and F4 are
reported and **not fixed**. The fix for F2 is small and is stated precisely
because naming it is cheaper than a wrong guess: remove the card from its old
position before re-inserting, or build the deck list with the ambassador
excluded and let `addToTop` be the only insertion of it. F1's fix is a
`deck.size() < 60` test on the quota loops — but note that would then *starve*
the quota for factions that saturate pass 1, which is presumably why the cap
was omitted, so it is a design decision for an `engine/` owner rather than a
mechanical patch.

## Promotion criteria (per AGENTS.md §4, no committee needed)

This proposal becomes canonical when:

1. The quota and pin are the **intended** standing convention, not just a
   bug-fix byproduct of B5-0313.
2. A future harness edit to `buildFactionDeck` or the construction loop
   re-applies or explicitly re-scopes both invariants.
3. `compile.bat` / `compile.sh` stay green (`-source 6`, stdlib only).
4. No `engine/`/`model/`/`ai/`/`ui/` file outside the claimed scope is touched.

Nothing here changes game-logic files. The harness files it cites are
in `engine/`, so a *code* edit to apply or re-scope this would need a fresh
claim on `engine/`; this proposal itself is docs-only and needs no claim on
`engine/` to land.
