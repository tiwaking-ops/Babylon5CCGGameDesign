---
document:
  title: "B5-0471 pattern: agenda-face-up install token probe"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
---

# Agenda-face-up install token probe (B5-0471)

**Applies to**: Synthetic scenario tests in `HeadlessHumanSeatProbe.java` that need to
verify engine log emission for a specific action path.

## Pattern

To probe an engine-side side effect from a driver action:

1. Create a synthetic fixture (fresh `GameState` with minimal players).
2. Build the subject card into the hand of the fixture's human player.
3. Capture `initialLogSize` if needed for incremental output checks.
4. Use reflection to invoke `GameController.processAction(Player, GameAction)` directly,
   avoiding the driver thread's wait-window constraints.
5. Assert the engine-side effect(s) using standard `check()` calls.
6. Scan `GameState.getLog()` for the expected token/line.

## Code template

```java
private static void scenarioAgendaFaceUpInstall(GameState fstate, Player human,
                                                 RulesEngine rules) {
    AgendaCard ag = agendaCard("id", false, Faction.HUMAN);
    human.getHand().clear();
    human.getHand().add(ag);

    try {
        java.lang.reflect.Method handler =
                GameController.class.getDeclaredMethod(
                        "processAction", Player.class, GameAction.class);
        handler.setAccessible(true);
        GameController tempCtrl = new GameController(fstate, new ArrayList<AIPlayer>(),
                new GameStateCallback() { public void accept(GameState gs) { } });
        handler.invoke(tempCtrl, human, GameAction.playCard(ag));
    } catch (Exception e) {
        check("SYN: processAction invocation successful", false);
        return;
    }

    check("SYN: agenda is now set", human.getAgenda() == ag);
    boolean foundToken = false;
    for (String line : fstate.getLog()) {
        if (line.contains(human.getName() + " sets agenda: " + ag.getTitle())) {
            foundToken = true; break;
        }
    }
    check("SYN: face-up install emits 'sets agenda:' token (B5-0464)", foundToken);
}
```

## Reusable lesson

For tests that need to verify a log emission that only occurs on a specific code path
(face-up agenda install vs hidden agenda sponsor), a synthetic fixture that bypasses
the driver thread's game flow is the cleanest approach. The `GameAction.playCard()`
factory method defaults `hidden=false`, which is exactly what B5-0464 expects.

See `HeadlessConformanceTest.java` `testAgendaInstallLog()` for a similar pattern using
reflection in a test-only fixture.
