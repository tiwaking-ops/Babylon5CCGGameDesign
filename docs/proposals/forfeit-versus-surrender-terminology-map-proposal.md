---
document:
  title: "Forfeit versus surrender — terminology map and vocabulary proposal"
  status: "Proposal (candidate, never truth until merged + compiled per AGENTS.md section 4)"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2181", version: "stealth-space-bunny-alpha"}
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2181", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-02"
  last_modified_date: "2026-10-02"
---

# Forfeit versus surrender: a terminology map

B5-2181. Read-only pass over `b5ccg/src`, no `src` file created, edited or
deleted. Row gate is `gated none`; the build gate is RED from a foreign in-flight
`ui/` edit and the row is marked BLOCKED per `.agent/00_BOOT.md` step 8 — see
*GATE* at the end. Everything below is a source trace; nothing was verified by
execution, because the compile cannot currently run.

## 1. The problem, stated exactly

The word **forfeit** carries three distinct rulebook-adjacent meanings and one of
them has no rulebook clause at all, while **surrender** carries a fourth. Four
mechanics, two words, one of which is overloaded three ways.

| # | Term | Rulebook clause | Status |
|---|---|---|---|
| M1 | forfeit by deck-out | `:460` "he must forfeit the game" | wired, involuntary |
| M2 | `FORFEIT` action (voluntary concede) | **none** | wired, unreachable |
| M3 | surrender (at-war, unconditional) | `:815`–`:825` | wired, reachable both seats |
| M4 | ISA "forfeit his ability to draw cards" | `:1120` | unrelated sense, same word |

M4 is listed so the map is exhaustive over the rulebook's uses of the word; it
is not a game-out mechanic and needs no engine term.

## 2. The state model: two flags, three producers

`b5ccg/src/b5ccg/model/Player.java`

* `:38` `private boolean hasForfeited = false;` — no provenance comment at all.
* `:43` `private boolean hasSurrendered = false;` — B5-0661, commented
  "Distinct from forfeiture" (`:42`).
* `:200-201` `hasForfeited()` / `setHasForfeited(boolean)` — both public.
* `:205-206` `hasSurrendered()` / `setHasSurrendered(boolean)` — both public.

`hasForfeited` has **three** writers in the tree:

1. `Player.drawCards` `:190` — `hasForfeited = true;` on deck-out with no
   non-ambassador Inner Circle character left. This is M1, rulebook `:460`.
2. `RulesEngine.executeForfeit` `:1160` — `p.setHasForfeited(true);` on the
   voluntary path. This is M2, which the rulebook does not describe.
3. `HeadlessConflictResolutionProbe` `:192` and `:220` — test fixtures calling
   the public setter directly.

`hasSurrendered` has exactly **one** writer: `RulesEngine.executeSurrender`
`:1076` `p.setHasSurrendered(true);`. No test writes it directly; the conformance
suite drives it through the real entry point at
`HeadlessConformanceTest.java:6030`.

`b5ccg/src/b5ccg/model/GameState.java` adds a third, redundant record of the same
fact: `:88` `Set<Player> surrenderedPlayers`, `:89-91` the accessors.
`executeSurrender :1077` writes **both** `p.setHasSurrendered(true)` and
`state.markSurrendered(p)`, and nothing ever removes a player from that set. So
surrender is stored twice and forfeit once.

## 3. The action vocabulary

`b5ccg/src/b5ccg/model/GameAction.java`

* `:45` `SURRENDER` — B5-0661, commented as a DRAW/discard-round negotiation
  that grants the opponent 3 influence plus an asylum ambassador copy (`:42-44`).
* `:54` `FORFEIT` — B5-1979. Its own comment at `:48-53` states the
  distinction: "Distinct from SURRENDER … and distinct from the involuntary
  draw-deck forfeit".
* `:192-194` `surrender(Player target)` — carries a target.
* `:199-201` `forfeit()` — no card, no target.

Dispatch: `GameController.java:463-473` (SURRENDER) and `:476-497` (FORFEIT).

## 4. The legality predicates, and where they genuinely differ

`RulesEngine.canSurrender` `:1046-1059`

```
:1047  phase must be GamePhase.DRAW
:1048  actor neither surrendered nor forfeited
:1049-1050  target non-null, not self, also still in the game
:1054  state.isAtWar(myFaction, theirFaction)      <- rulebook :817
:1057  actor has an ambassador in play             <- rulebook :819
```

`RulesEngine.canForfeit` `:1124-1133`

```
:1126  phase must be GamePhase.ACTION
:1127  game not already over
:1128  state.isPlayerActive(p)
:1129-1131  at least one OTHER player is still active
```

These are not the same rule wearing two names. The phases are **disjoint** — a
player can never be offered both at the same moment — the target requirement is
absent from forfeit, and forfeit has a game-over precondition surrender lacks.
So the overlap the row's premise asserts is an overlap of *meaning*, not of
*reachability*: the two actions are mutually exclusive by construction.

**Where the premise is half right and half wrong.** `RulesEngine.executeSurrender`
`:1070-1072` and `executeForfeit` `:1138-1147` both assert in prose that the two
are distinct, and both are correct about the ambassador. But the *flag* they set
is not what distinguishes them: `executeForfeit` sets `hasForfeited`, the same
flag the deck-out path sets at `Player.drawCards:190`. The distinction the code
actually enforces lives in the ambassador handling and in the victory path, not
in the flag. A future edit that sets `hasForfeited` for a reason other than
"this player has left the game" would silently become a third forfeit flavour.

## 5. Consumers: the asymmetry is the finding

Whole-tree census, `-rn "hasForfeited"` and `-rn "hasSurrendered"` under
`b5ccg/src --include=*.java`: **49** sites vs **23**. The gap is not random; it
clusters.

### 5.1 Places that read BOTH (correct)

* `GameState.isPlayerActive` `:293-295` — the consolidated definition.
* `RulesEngine.canSurrender :1048`, `:1050`.
* `RulesEngine.checkVictory :808` (the second, active-player count).
* `RulesEngine.checkVictoryPath :927`, `:939`.
* `RulesEngine.buyMoreCards :2094`, `isEligibleAgendaVoter :2315`.
* `Conflict.canJoinConflict` `:153-154` — via `isPlayerActive`, with a
  `Player`-flag fallback for an unwired back-reference.
* `MainWindow:1789` — the surrender readout refuses either flag.

### 5.2 Places that read ONLY `hasForfeited` (a surrendered player is still
"in the game" here)

| Site | What it means |
|---|---|
| `RulesEngine:792` `checkVictory` last-standing count | Compensated at `:805-814` by a second, surrender-aware count that routes through `majorVictory`. Deliberate and commented. |
| `RulesEngine:831` agenda/major/standard scan | A surrendered player's agenda condition is still evaluated for a win. Not obviously wrong, not obviously right. |
| `RulesEngine:884` `checkVictoryPath` last-standing | Deliberate; the MAJOR fallback at `:920-923` is commented to cover it. |
| `RulesEngine:899`, `:927`, `:939`, `:971`, `:1015`, `:1027`, `:1772`, `:2029`, `:2116` | Influence banding, next-highest calculations, presence tests. |
| `RulesEngine:2048` `discardNeutralizedSupporting` | A surrendered player's neutralized supporting cards are still discarded. Harmless — they are leaving anyway. |
| `GameController:604`, `:622` | Pre-filters on the human conflict-join and attack windows. See 5.3. |
| `AIPlayer:639`, `:643`, `:660` | **Live scoring defect, see 5.4.** |

### 5.3 Dead conjunct in `GameController`

```
:604   if (uiCallback != null && human.isHuman() && !human.hasForfeited()
        && human != conflict.getInitiator()
        && rules.canJoinConflict(human, conflict)     <- both flags
        && conflict.canJoinConflict(human)) {         <- both flags
```

The `:604` conjunct is redundant: both downstream predicates already consult
`isPlayerActive`, which rejects a surrendered human, so no window opens for one.
The behaviour is right and the pre-filter is simply incomplete — it names only
one of the two flags. Cosmetic, but it is exactly the shape that misleads the
next reader into thinking surrender does not close the conflict window.

### 5.4 The one substantive consumer bug: `AIPlayer` scores surrendered rivals
as live

`AIPlayer:639` and `:660`

```
:639   if (q == p || q.hasForfeited()) continue;
:643   if (closestGap == Integer.MAX_VALUE) return 0;   // every rival forfeited
:660   if (q == p || q.hasForfeited()) continue;
```

The leader-gap calculation that drives both the SURRENDER score
(`:1002-1015` MEDIUM, `:1246-1260` HARD) skips forfeited rivals but **not**
surrendered ones. A surrendered player keeps their influence
(`executeSurrender` never zeroes it; it grants the target `+3` at `:1082`), so a
surrendered rival is counted as a live competitor whose influence raises the
gap the AI must close. The AI therefore under-values its own legal SURRENDER
precisely when a rival has already left — the one moment surrender is most
attractive. `AIPlayer:629` and `:643` even carry the comment "Forfeited rivals
are excluded", establishing the intent; the intent is simply not carried to the
second flag.

Scope: `ai/`. Recorded, not fixed.

## 6. Two findings the rulebook does not settle, stated as questions

**Q1 — the voluntary forfeit discards the ambassador.** `executeForfeit`
`:1161-1168` removes the ambassador from the Inner Circle, calls
`deck.discard(amb)`, and clears `setAmbassador(null)`. Rulebook `:460` says
"Ambassador cards may never be discarded", and `Player.drawCards:184` honours
that by skipping the ambassador when it picks a discard victim. There is no
rulebook clause for a voluntary concede at all, so the engine invented one — and
the invention discards the one card class the rulebook says can never be
discarded. Compare the surrender path, which leaves the ambassador in place and
hands over an asylum *copy* (`:1086-1101`, rulebook `:823`). Two exits, two
treatments of the same card, one of them rulebook-forbidden in the only clause
that speaks to it. A human ruling is required: does a voluntary concede forfeit
the ambassador, or leave it out of play the way surrender does?

**Q2 — surrender never ends a Civil War.** Rulebook `:1004` and `:1010` both
name unconditional surrender as the way a Civil War ends, and `:970` says a
Home Faction ambassador is removed from the game when its faction surrenders.
`executeSurrender` touches neither: it sets the two surrender flags, grants the
influence, and places the asylum copy, and stops. `GameState.exitCivilWarByWarEnd`
(`:235`) is the exit path and its **only** caller in the whole tree is
`HeadlessConformanceTest:6318`. `AIPlayer:1006-1015` and `:1250-1259` show the AI
*reasoning* about surrendering inside a Civil War (it hard-discounts a
same-race target by 8), so the scenario is modelled and priced, and its
consequence is not implemented.

## 7. Vocabulary proposal

Three words for three mechanics, and no flag renaming.

| Mechanic | Proposed term | Flag | Rulebook |
|---|---|---|---|
| M1 deck-out loss | **forfeit** | `hasForfeited` | `:460` |
| M2 voluntary concede | **concede** | `hasForfeited` | none — needs a clause |
| M3 at-war exit | **surrender** | `hasSurrendered` | `:815`–`:825` |

Why this ordering:

1. **"Forfeit" stays with `:460`**, which is the only use the rulebook gives it.
   The code already does this: `Player.drawCards:190` is the canonical writer
   and its comment at `:198-199` cites the clause.
2. **"Concede" is the new word for `FORFEIT`**, because it is what the action
   does from the actor's point of view (`:1167` "picks up their cards") and it
   does not collide with either rulebook sense. It should appear in the UI label
   and in the DECISIONS entry when `FORFEIT` is given a rulebook clause.
3. **"Surrender" is untouched**, because `:815`–`:825` already owns the word.

**Flag names stay.** `hasForfeited` is cited in 49 sites and is semantically
correct as "this player has left the game", which is true of both producers. A
rename would be pure churn. What the flag does *not* carry is the *reason*, so:

**Proposal P1.** Add a short doc comment at `Player.java:38` naming both writers,
in the form of the existing `:40-43` comment above `hasSurrendered`. Cost: two
lines in `model/`. Benefit: the next reader sees at the declaration that two
different rules share one flag, which is the fact that makes 5.2 and Q1 look
like bugs rather than design.

**Proposal P2.** Document in `docs/DECISIONS.md` that `hasSurrendered` is stored
twice (`Player:43` and `GameState:88`) and that `GameState.surrenderedPlayers` is
written but never read outside `:89-91`. Either drop the set or document it as
the future home of a surrender *reason*. `model/` change, deferred to a claiming
row.

**Proposal P3.** Fix `AIPlayer:639` and `:660` to read `state.isPlayerActive(q)`
instead of `q.hasForfeited()`, which is the consolidated predicate
(`GameState:293-295`) and the one every other "is this rival still competing"
site should use. `ai/` change, deferred.

**Proposal P4.** Route Q1 and Q2 to a human as rulebook questions. Q1 changes a
player's card pool; Q2 changes the faction map. Neither is an interpretation an
agent should record in DECISIONS on its own authority.

## 8. Cross-references, not new findings

* **B5-2205 finding F5** already reported that `FORFEIT` has no caller in `ui/`
  or in either `AIPlayer` difficulty switch. Re-verified here: repo-wide grep
  for `GameAction.forfeit()` and `Type.FORFEIT` outside `GameAction.java` returns
  one hit, the `GameController:476` switch branch. **M2 is unreachable by either
  seat.** Not re-reported as new.
* **B5-2075** (OPEN) owns the forfeit-path audit proper, including whether a
  decked player loses loudly. This row maps terminology and touches no
  forfeit-path semantics.
* **B5-1979** is DONE and owns M2's implementation.
* **B5-0661** is DONE and owns M3's implementation.

## GATE

`RUN_TESTS=1 bash b5ccg/compile.sh` — **exit 1**, javac `1.8.0_292`:

```
src\b5ccg\ui\MainWindow.java:1048: error: cannot find symbol
                showDeckBuilder();
  symbol:   method showDeckBuilder()
1 error
```

`grep -rn "showDeckBuilder" b5ccg/src` returns exactly **one** hit, the call site;
there is no definition in tracked source. The line sits inside the uncommitted
`ui/` diff owned by **live** foreign claim `.agent/CLAIMS/B5-2007.json`
(Cline (space-bunny-free), `started_utc 2026-10-02T05:27:33Z`), LIVE on the
three-signal rule. `ui/` is outside this row's scope (`docs/proposals/` plus
`src` read-only). Per `AGENTS.md` section 5 and `.agent/00_BOOT.md` step 8 the
red is **recorded and not repaired**, the row is marked BLOCKED, and the claim is
released. This pass wrote no file under `b5ccg/src`, so it cannot be the author
of the break.

## Held

No file under `b5ccg/src` created, edited or deleted. No foreign claim, heartbeat
or ledger row touched. No ledger row seeded. No commit, no push.