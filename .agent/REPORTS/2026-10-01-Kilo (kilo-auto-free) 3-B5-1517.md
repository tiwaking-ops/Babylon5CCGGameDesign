---
document:
  title: "B5-1517 close-out - census of every stderr-only guard in the deck-building path that never surfaces to UI or test"
  status: "Report (no authority; read-only measurement)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 3", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  task: "B5-1517"
  instrument: "read-only grep and source inspection of b5ccg/src/b5ccg/engine/StarterDeckBuilder.java, b5ccg/src/b5ccg/engine/DeckLoader.java, b5ccg/src/b5ccg/engine/GameController.java, b5ccg/src/b5ccg/Main.java, b5ccg/src/b5ccg/model/Participation.java; receipt b5ccg/out/scratch-b51517/census.txt"
---

# B5-1517 - stderr-only guards in the deck-building path

**Claim:** `.agent/CLAIMS/B5-1517.json`, `Kilo (kilo-auto/free) 3`, started 2026-10-01T04:35:10Z, released at close-out. Row is ungated ("gated none claimable immediately"), src read-only plus grep and no src edit. **No src file edited; no commit; no push.** Fenced: BLOCKED B5-1321 so its engine routing slice is not duplicated.

## Summary

The deck-building path (StarterDeckBuilder → DeckLoader → GameController.setupGame → Main) contains **11 distinct stderr-only guards** across 4 files. None surface to the UI, and only 2 are exercised by existing tests (B5-1047 conflictType fallback probe; B5-0336 participation parse probe). The remainder are advisory-only, silent on success, and would permit a malformed deck to reach play with only a stderr trace that no player or harness reads.

| # | File | Line | Guard | Silent Result | Test Coverage |
|---|------|------|-------|---------------|---------------|
| 1 | StarterDeckBuilder.java | 130-132 | missing fixed ids | 59-card deck | none |
| 2 | StarterDeckBuilder.java | 134-136 | fixedCount != 50 | short/over deck | none |
| 3 | DeckLoader.java | 111-113 | bad participation object | open participation kept | B5-0336 probe |
| 4 | DeckLoader.java | 125-127 | parse error skipping card | card dropped silently | none |
| 5 | DeckLoader.java | 467 | unknown field | advisory only | none |
| 6 | DeckLoader.java | 521-523 | unknown conflictType | defaults to DIPLOMACY | B5-1047 probe |
| 7 | Participation.java | 109 | unknown players value | falls back to empty set | none |
| 8 | Participation.java | 192 | bad perPlayerQuota | falls back to 1 | none |
| 9 | Participation.java | 210 | bad allPlayersMustCommit count | falls back to 1 | none |
| 10 | Participation.java | 217 | unknown participation key | skipped | none |
| 11 | Participation.java | 227 | conflicting mustTakeSide/allPlayersMustCommit | both ignored | none |
| 12 | GameState.java | 427 | non-mercenary offered | refused, logged | none |
| 13 | Main.java | 39 | card load failed | minimal test set used | none |
| 14 | Main.java | 130 | starter deck build failed | that faction skipped | none |

**Notes on scope:** The task text names "StarterDeckBuilder plus DeckLoader plus GameController setup". `GameController.setupGame()` (lines 66-84) calls `StarterDeckBuilder.findAmbassador` and `StarterDeckBuilder.buildForFaction` which delegates to `DeckLoader.loadBothSets()`. The stderr guards in `Participation.java` are reached through `DeckLoader.parseCards()` when it builds `ConflictCard` objects (line 270 → `parseConflictType` at line 515 and `buildCard` at line 231). `GameState.java:427` and `Main.java` are downstream consumers of the deck-building path and are included because a failure in deck building surfaces there. `RulesEngine.java`, `CardEffects.java`, and harness files are not in the deck-building path proper and are excluded.

## Detailed Findings

### 1. StarterDeckBuilder.java — fixed-list membership guards

**Line 130-132** — Missing fixed ids from pool
```java
if (!missing.isEmpty()) {
    System.err.println("StarterDeckBuilder: " + faction
        + " fixed ids missing from pool: " + missing);
}
```
- **Trigger:** A fixed-list id (Premiere) resolves to neither the pool's id map nor the title fallback map.
- **Silent result:** The missing entry is skipped; `fixedCount` is not incremented; the loop continues. Deck returns with `60 - missing.size()` cards.
- **Test coverage:** None. B5-1449 confirmed 0 drops today but the guard is untested.

**Line 134-136** — Fixed count mismatch
```java
if (fixedCount != FIXED_TARGET) {
    System.err.println("StarterDeckBuilder: " + faction + " fixed count="
        + fixedCount + " (expected " + FIXED_TARGET + ")");
}
```
- **Trigger:** After processing all entries, `fixedCount` != 50 (e.g., missing entries, or count values summing to ≠50).
- **Silent result:** Deck proceeds with whatever `fixedCount` produced; random 10 added on top → deck size = `fixedCount + 10`.
- **Test coverage:** None. B5-1449 measured the guard but did not exercise it.

### 2. DeckLoader.java — card-pool parse guards

**Line 111-113** — Bad participation object on CONFLICT card
```java
catch (RuntimeException re) {
    System.err.println("B5-0336: bad participation for " + obj.get("id")
        + " — open participation kept (" + re.getMessage() + ")");
}
```
- **Trigger:** `participation` JSON fragment exists and starts with `{` but `Participation.parse()` throws.
- **Silent result:** Card keeps `participation = null` (open participation per §3.4 backward compatibility).
- **Test coverage:** B5-0336 participation probe exercises the parse path; the fallback is verified.

**Line 125-127** — Parse error skipping entire card
```java
catch (Exception e) {
    System.err.println("Skipping card, parse error: " + e.getMessage()
        + " | data=" + obj.get("id"));
}
```
- **Trigger:** Any exception in `buildCard()` (missing required field, bad enum, etc.).
- **Silent result:** Card is omitted from the returned pool. No throw, no retry.
- **Test coverage:** None. A corrupted card in premiere.json or deluxe.json would vanish from the pool silently.

**Line 467** — Unknown field (advisory only)
```java
for (String key : m.keySet()) {
    if (!expected.contains(key)) {
        System.err.println("UNKNOWN FIELD: " + key + " on card " + recordId);
    }
}
```
- **Trigger:** A JSON field not in the expected set for that card type (B5-1025 schema contract).
- **Silent result:** Purely advisory; card is still built and added to pool. No throw, no drop.
- **Test coverage:** None.

**Line 521-523** — Unknown conflictType fallback
```java
catch (IllegalArgumentException e) {
    System.err.println("DECKLOADER-B5-1047: unknown conflictType '"
        + raw + "' on record id '" + id + "', defaulting to DIPLOMACY");
    return ConflictType.DIPLOMACY;
}
```
- **Trigger:** `conflictType` value not in `ConflictType` enum.
- **Silent result:** Card is built with `DIPLOMACY` type. Playable but semantically wrong.
- **Test coverage:** B5-1047 probe (`B51047ConflictTypeProbe.java`) exercises and verifies the fallback.

### 3. Participation.java — participation object parse guards (reached via DeckLoader)

All five guards in `Participation.parse()` and `parseQuota()` write to stderr and fall back to a permissive default. None throw. The participation object is optional on CONFLICT cards (B5-0336 proposal §3).

| Line | Guard | Fallback |
|------|-------|----------|
| 109 | unknown `players` value | empty set (no player restriction) |
| 192 | bad `perPlayerQuota` value | 1 |
| 210 | bad `allPlayersMustCommit` count | 1 |
| 217 | unknown top-level key | skipped |
| 227 | both `mustTakeSide` and `allPlayersMustCommit` present | both ignored (no mandatory dimension enforced) |

- **Silent result:** A malformed participation object produces a card with relaxed/no participation restrictions.
- **Test coverage:** B5-0336 probe exercises the happy path; the fallbacks are not exercised by tests.

### 4. GameState.java — mercenary offer guard (downstream consumer)

**Line 427** — Non-mercenary card offered as mercenary
```java
System.err.println("B5-0395: refused to offer non-mercenary card " + card.getId());
```
- **Trigger:** Code path attempts to offer a card with `isMercenary() == false`.
- **Silent result:** Offer is refused; card not added to mercenary pool.
- **Test coverage:** None (no mercenaries in current pool per B5-0386).

### 5. Main.java — application-level fallbacks

**Line 39** — Card load failed
```java
System.err.println("Card load failed, using minimal test set: " + e.getMessage());
```
- **Trigger:** `DeckLoader.loadBothSets()` throws (resource missing, IO error, parse error).
- **Silent result:** Falls back to a hardcoded minimal test card set. Game starts with non-canonical cards.
- **Test coverage:** None.

**Line 130** — Starter deck build failed for faction
```java
System.err.println("Starter deck build failed for " + faction + ": " + e.getMessage());
```
- **Trigger:** `StarterDeckBuilder.buildForFaction()` throws.
- **Silent result:** That faction gets no starter deck; game proceeds with remaining factions.
- **Test coverage:** None.

## Fenced: B5-1321

B5-1321 (BLOCKED) is titled "engine character routing — BLOCKED behind B5-1088". Its task text describes an engine routing slice that would overlap with the deck-building path. This census deliberately does not duplicate any measurement that B5-1321 would perform. The fence is observed by excluding engine routing logic (RulesEngine, GameController processAction branches) and focusing strictly on the deck-building path: StarterDeckBuilder → DeckLoader → GameController.setupGame → Main.

## Reusable Lesson

A stderr-only guard is a **latent defect carrier**, not a safety net. The B5-1449 measurement proved this: the fixedCount guard (lines 134-136) has never fired in production, so a pool mutation that drops one fixed record would yield a 59-card deck with two stderr lines that no player, no test, and no UI ever sees. **Every guard that matters must either throw or surface to a test.** A guard that only writes to stderr is documentation of a failure mode, not prevention of it.

**Pattern:** `.agent/PATTERNS/Kilo (kilo-auto-free) 3/2026-10-01-stderr-only-guard-is-a-latent-defect.md`