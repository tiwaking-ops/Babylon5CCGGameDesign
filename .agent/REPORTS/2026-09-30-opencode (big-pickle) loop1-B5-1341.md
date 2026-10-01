---
document:
  title: "B5-1341 close-out - the B5-1109 UI guards are the SOLE remaining door to the corrupting generic dispatch: all 7 construction paths for PLAY_CARD are closed, but every one is closed by caller-side instanceof routing with no engine-side backstop, and reflection-injecting playCard(character) into processAction completes without throwing"
  status: "Report (no authority; path census, no src edits)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1341"
  instrument: "read-only grep census over b5ccg/src plus one git-ignored probe b5ccg/out/b51341/b51341/B51341CharPlayPathProbe.java (javac -source 6 -target 6) that reflection-injects playCard(CharacterCard) into processAction; receipts b5ccg/out/b51341/b51341-receipt.txt and b51341-stderr.txt"
---

# B5-1341 - every path that can hand a CharacterCard to the generic dispatch

**Claim:** `.agent/CLAIMS/B5-1341.json`, `opencode (big-pickle) loop1`, started
2026-09-30T23:51:01Z, released at close-out. Row is ungated, read-only plus grep.
**No src file, no suite file, no card data edited; no commit; no push.** This is a
path census; it does not re-audit the B5-1109 UI guards themselves.

## The sink, unchanged since B5-1109

`GameController.java:190-192` - the `PLAY_CARD` branch of `processAction`:

```java
case PLAY_CARD:
    applyGenericCardPlay(p, action.getCard(), action.isHidden());
    break;
```

No type guard. `applyGenericCardPlay` (`:630-684`) charges `card.getCost()` raw
(`:636-641`), removes the card from hand (`:651`), and a `CharacterCard` matches none
of the `instanceof` arms at `:654-680`, so it falls to the final `else` and is
discarded (`:681-683`). Raw cost instead of `sponsorCost`, no supporting-role entry,
no promotion path - exactly the corruption B5-1109 measured.

## Census: 7 construction paths, all closed, none by the engine

| # | path | file:line | verdict | closed by |
|---|---|---|---|---|
| 1 | production UI Play button | `MainWindow.java:2163` | **closed** | double guard: enablement `!(selectedCard instanceof CharacterCard)` at `:2130`, and dispatch returns for ConflictCard/CharacterCard at `:2147-2148` |
| 2 | UI censure / fleet seams (4 sites) | `MainWindow.java:700, 744, 1982, 2020` | **closed** | `if (!(selectedCard instanceof EnhancementCard)) return;` then cast - type-disjoint |
| 3 | production AI action factory | `AIPlayer.java:212, 215` | **closed** | `AIPlayer.java:191-194` catches `c instanceof CharacterCard` first and emits `recruitCharacter` under `canRecruit`; the two `playCard` factories are downstream of that branch |
| 4 | human-seat offer loop | `HeadlessHumanSeatProbe.java:344` | **closed** | `:322-324` mirrors path 3 exactly (CharacterCard -> `recruitCharacter`); the generic `else` at `:343-344` sees only Enhancement/Location/Group/Event/Fleet |
| 5 | human-seat reflection sites | `HeadlessHumanSeatProbe.java:561, 634` | **closed** | type-disjoint: `ag` is an `AgendaCard`, `censure` an `EnhancementCard` |
| 6 | conformance reflection sites (9) | `HeadlessConformanceTest.java:1612, 1849, 2976, 5025, 5069, 6281, 6288, 6295, 6302` | **closed** | type-disjoint: 4 agendas (incl. `playAgendaFaceDown(hidden)`), 5 enhancements |
| 7 | AI scoring arms | `AIPlayer.java:704, 935` | **unreachable** | `case PLAY_CARD:` in scoring reads `a.getCard()`; no such action is ever constructed with a character, so these arms are defensive only |

One near-miss worth naming: `HeadlessConformanceTest.java:925-926` declares
`CharacterCard costly = charCard("ast_costly", ...)` and
`CharacterCard free = charCard("ast_free", ...)`, and the CPC section at `:6255-6259`
redeclares both names as `EnhancementCard`. Different scopes, so the CPC
`playCard(costly)` / `playCard(free)` calls at `:6281-6302` are enhancements. The AST
characters never reach `playCard` - that section exercises `canRecruit`, `recruitCost`
and a manual `placeInSupportingRole`. The name collision is the only reason this pair
looks like an open path on a name-only grep.

## The structural finding: the invariant lives entirely in the callers

Every one of those closures is a caller-side `instanceof` chain. There is no
engine-side backstop anywhere, and two places actively *look* like they should be
one:

* **`GameController.submitHumanAction` (`:716-747`) validates nothing about the
  card.** It checks only the join/attack decision windows and drops stale join
  clicks; the action is stashed for the waiting loop to hand to `processAction`.
* **`RulesEngine.canPlayCard` (`:904-910`) is type-blind.** It checks hand
  membership, `appliedPool >= cost`, and faction playability - nothing about type -
  so it returns **true** for an affordable in-hand character. This is load-bearing
  for the audit above: the predicate B5-1171 wired into `MainWindow:2134` enablement
  would happily enable Play on a character. What actually closes the production UI
  path is the explicit `instanceof` guard at `MainWindow:2130`, one line above it.

**Verified empirically, not just read.** A git-ignored probe
(`b5ccg/out/b51341/b51341/B51341CharPlayPathProbe.java`, `javac -source 6 -target 6`,
exit 0) took a real HUMAN `CharacterCard` (`de_char_bester`) from the production pool,
put it in hand with enough influence, and reflection-invoked
`processAction(p, GameAction.playCard(ch))`:

```
found human char: de_char_bester
processAction(playCard(char)) threw=false (completed)
```

The engine accepts the corrupting dispatch without complaint. Today only caller
discipline prevents it; that is a convention, not an invariant.

## Answer to the row's three questions

1. **Are the B5-1109 UI guards the sole remaining door?** Yes - and they are the only
   *load-bearing* one. `MainWindow:2130` is the single line whose removal reopens the
   class; `MainWindow:2147-2148` is a redundant second guard for the same door.
2. **Do smoke, conformance, or multi-round harnesses still reach it?** No. All 11
   harness dispatch sites (paths 4-6) pass agendas or enhancements only, and both
   harness offer loops route characters to `recruitCharacter`.
3. **Ranked input for the post-B5-1047 engine fix.** The open-path list is empty, so
   the ranking is of where the backstop belongs:

   1. **`GameController.java:190` / `applyGenericCardPlay:630`** - add a
      `CharacterCard` branch to the generic dispatch (refuse-and-keep-in-hand, as the
      `AgendaCard` arm at `:646-650` already does) so the routing invariant is enforced
      where the damage happens rather than at six distant call sites. This is the
      single seam that closes the class permanently.
   2. **`RulesEngine.canPlayCard:904`** - decide deliberately whether the predicate
      is type-blind by design; if it should exclude characters, say so there, and if
      not, document that every caller must add its own type guard.
   3. **`GameAction.playAgendaFaceDown:123`** - narrow the parameter from `Card` to
      `AgendaCard`. It currently mints a `PLAY_CARD` from an untyped card with no
      validation; its one caller passes an agenda, so nothing breaks, but the
      factory is one careless caller away from the same corruption.
   4. **No caller change is needed.** Paths 1-7 are already closed; changing them
      would only add redundant guards.

Not actioned here: engine is one-writer at a time and B5-1047 holds the live engine
claim, and this row forbids src edits.

**Reusable lesson:** a path census must enumerate the *construction sites of the
action*, not the sink - the sink is one line and always looks closed, while the doors
are wherever a value is minted, and when the doors are closed by convention rather
than by a guard the honest finding is "closed by N independent instanceof chains with
no backstop". Also: a guard that lives next to a predicate the audit assumes is
type-aware needs proving, because the two lines read as one gate and only one of
them is.**