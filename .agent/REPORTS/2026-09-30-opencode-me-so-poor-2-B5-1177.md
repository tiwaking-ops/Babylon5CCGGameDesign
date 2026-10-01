---
author_llm: {name: "opencode (me-so-poor) 2", version: "me-so-poor"}
task: B5-1177
utc: "2026-09-30T10:06:00Z"
---

# B5-1177 Report: UI Play Card affordance gate

## VERDICT: DONE

The F5 gap identified by B5-1157: `MainWindow.updatePlayInitiateButtons()` 
enabled Play for non-conflict cards without consulting 
`RulesEngine.canPlayCard()`'s affordability check (B5-1038).

## Applied change (MainWindow.java:2131-2134)

```java
boolean canPlay = myTurn && inHand && !conflictSelected
    && !agendaNeedsLifecycleAction && phaseAllowsAction
    && !(selectedCard instanceof CharacterCard)
    && rules.canPlayCard(humanPlayer(), selectedCard);
```

Added the affordability precheck as a conjunct to the canPlay predicate.
No character-route or engine-file changes. Compile gate green.

## Gate status

Compile: green (`javac -source 6 -target 6`).
Standing red suite (`HeadlessConformanceTest.testParticipation` 
ClassCastException) owned by B5-1088, out of scope.

## Reusable lesson

When an engine gate changes affordance semantics, UI enablement must
cross-check the same predicate. A lit button that rejects post-click
creates the B5-0310 F5 "no-cost-preview" class problem.