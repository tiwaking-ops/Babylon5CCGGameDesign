---
author_llm: {name: "GitHub Copilot (Auto mode)", version: "Auto mode"}
assessor_llm: []
last_modified_by_llm: {name: "GitHub Copilot (Auto mode)", version: "Auto mode"}
created_date: "2026-10-02"
last_modified_date: "2026-10-02"
task: "B5-1675"
status: "proposal"
---

# CharacterCard generic-play sink guard

## Scope and gate

This is the exact, Java 6-compatible engine diff for the post-B5-1047
CharacterCard routing fix. It is a proposal only: B5-1675 makes no source
change, and the implementation remains gated behind the engine-owner chain
recorded by B5-1321. The guard belongs in `GameController.java`, so it must be
implemented by the engine scope owner after that gate is clear.

The source census in B5-1341 and the UI close-out in B5-1171 establish that
caller-side `instanceof CharacterCard` checks currently close the production
doors, but `processAction` can still accept a directly injected generic
`PLAY_CARD` action. `applyGenericCardPlay` charges raw `card.getCost()`, removes
the card, and otherwise discards it because no CharacterCard arm exists.

## Exact insertion point and diff

Insert the following branch immediately after the existing null guard and
before the influence-cost calculation:

```diff
     private void applyGenericCardPlay(Player p, Card card, boolean hidden) {
         if (card == null) return;
+        if (card instanceof CharacterCard) {
+            state.log(p.getName() + " cannot play a character as a generic card.");
+            return;
+        }
         // B5-1038: charge card cost BEFORE hand removal, following the
         // RECRUIT_CHARACTER affordance pattern (applyInfluence, log, break
         // without removing the card when unaffordable). Cost-0 cards behave
```

The insertion is at the sink's current line 632, between `if (card == null)
return;` and the `B5-1038` cost block. The surrounding source is:

```java
private void applyGenericCardPlay(Player p, Card card, boolean hidden) {
    if (card == null) return;
    // B5-1038: charge card cost BEFORE hand removal, following the
    // RECRUIT_CHARACTER affordance pattern (applyInfluence, log, break
    // without removing the card when unaffordable). Cost-0 cards behave
```

The guard must precede cost payment, because refusal is intended to preserve
both the card in hand and the player's influence. A branch placed after the
cost block would preserve the card but still consume influence, creating a
new partial-side-effect defect.

## Acceptance checks for the gated implementation

1. `CharacterCard` passed to `processAction(..., GameAction.playCard(card))`
   returns without throwing, without removing the card from hand, without
   discarding it, and without changing influence.
2. The refusal log identifies the player and card as a generic-play rejection.
3. Existing Enhancement, Location, Group, Agenda, Event, hidden-Agenda, and
   unaffordable-card behavior remains unchanged.
4. `b5ccg/compile.bat` is green with `javac -source 6 -target 6`.
5. The engine implementation is separately claimed and logged after the
   B5-1047 -> B5-1087 -> B5-1088 gate is resolved.

## Ranked follow-ons

1. **Decide `RulesEngine.canPlayCard` semantics.** B5-1341 found that the
   predicate is type-blind and can return true for an affordable CharacterCard.
   Decide whether it should exclude characters or remain a generic predicate
   whose callers must route by card type; do not silently change it as part of
   the sink guard.
2. **Narrow `GameAction.playAgendaFaceDown` from `Card` to `AgendaCard`.** Its
   current parameter is broader than its behavior and can mint another
   type-confused `PLAY_CARD` action. The existing caller passes an agenda, but
   the signature change needs its own compile and call-site review.
3. **Retain the UI guards.** B5-1171's `MainWindow` checks remain useful
   affordance protection, but no caller-side guard should be treated as a
   substitute for the sink invariant.

## Fenced inputs

This proposal read B5-1349, B5-1321, B5-1341, and B5-1171. B5-1321 remains
blocked and its engine claim is not reopened here. No source, card data, deck
data, conformance suite, or commit was changed.

