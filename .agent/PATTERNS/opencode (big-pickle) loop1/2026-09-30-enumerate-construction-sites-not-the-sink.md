---
document:
  title: "A path census enumerates construction sites, not the sink - and when every door is closed by caller-side instanceof chains the honest verdict is 'closed by convention, no backstop', proven by injecting the bad value rather than by reading the guards"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1341"
  supersedes: null
---

# Enumerate construction sites, not the sink

**Observed in:** B5-1341, asked to census every call path that can hand a
`CharacterCard` into `applyGenericCardPlay` after a UI door-close. Seven
construction paths, all closed; the sink had no guard at all.

## Why the sink is the wrong place to look

`GameController.java:190` - the branch that routes `PLAY_CARD` into the corrupting
dispatch - has **no type guard**, and `submitHumanAction` validates nothing about the
card either. Read only the sink and the correct answer looks impossible to reach.
Read the sink and conclude "closed" and the answer is wrong for the opposite reason:
the sink is unguarded and only the callers save it.

A sink is one line. The doors are wherever a value of the dangerous type is *minted*,
which in an action-based engine means every `GameAction.playCard(...)` call site plus
every factory that can produce one. Grep the construction, not the consumption.

## The verdict that actually transfers

All seven sites were closed, six by an `instanceof` chain and one by unreachability.
The reusable output is not "closed" - it is:

> **closed by N independent caller-side `instanceof` chains, with no engine-side
> backstop**; here the single load-bearing line is `MainWindow:2130`, and removing it
> reopens the class.

That phrasing is what a later agent can act on. "Closed" is a property of today;
"closed by convention in six places" tells the next reader where the risk sits and
which line to protect during a refactor.

## Prove the absence of a backstop by injection, not by reading

Reading six `instanceof` chains and concluding "no engine-side guard" is an argument.
The cheap argument-killer is to hand the engine the value it should refuse:

```java
// production CharacterCard from the pool, in hand, with influence to spare
handler.invoke(gc, p, GameAction.playCard(ch));
// -> threw=false (completed)
```

That one line converts a claim about code you did not write into an observation. It
also reframes the finding: the engine does not merely fail to guard, it *accepts the
corrupting dispatch without complaint* - which is precisely why caller discipline is
carrying the whole invariant.

## Two traps in the same census

1. **The guard that looks load-bearing may not be.** `RulesEngine.canPlayCard` is
   type-blind - hand, pool, faction, no type - and it sits one line below the
   `instanceof CharacterCard` guard in the UI enablement expression. Read as a block
   they look like one gate. Only the explicit `instanceof` closes the door. The
   general form: **when two adjacent conditions look like one gate, check whether
   each one alone is sufficient.**
2. **Name-only greps invent open paths.** `HeadlessConformanceTest` declares
   `costly`/`free` as `CharacterCard` in one section and as `EnhancementCard` in
   another. A grep for `playCard(costly)` looks like the character path. Resolving
   the *declaration* at each site - not the call name - is what separates a real door
   from a coincidence of variable naming.

## The order that produced the inventory

1. Grep the sink to learn the exact method and its signature.
2. Grep the action-type enum to find every factory that can mint the action, including
   untyped ones (`playAgendaFaceDown(Card)` mints `PLAY_CARD` and validates nothing).
3. Grep every factory call, then classify each site by the **declared type of its
   argument** at that site.
4. Separately classify the places that read the action (`case PLAY_CARD:` in scoring
   arms) as unreachable rather than open - a read site cannot create the value.
5. Then check the two "should-be guards": the public submit entry point and the
   shared legality predicate. If neither is type-aware, say so explicitly, because
   that is what makes the doors caller-owned.
6. Only then inject the value to confirm.

Steps 1-5 are read-only and produce the inventory; step 6 is what makes the verdict
checkable. Doing 6 first wastes the effort of a reflection setup before you know what
to inject.