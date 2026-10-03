---
document:
  title: "Structured game-log format (F12) — a typed entry behind the string log, and the measured finding that the existing F12 framing code can never fire"
  status: "Proposal — candidate, never truth until merged and compiled"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2159", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2159", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-02"
  task: "B5-2137"
---

# Proposal: a structured game-log entry format (B5-0310 F12)

Authored for B5-2137, which asked for the structured entry format behind B5-0310
finding F12 ("log is a raw dump") with `phase`, `actor`, `action` and `result`
fields, plus before/after examples. Sources: the original finding
`.agent/REPORTS/2026-09-21-solar-pro4-B5-0310.md:316-325`, its rulebook anchor
"Babylon 5 CCG" §III *Narrating the Story*
(`BABYLON5_CCG_RULEBOOK.md:332-338`), the framing rows DONE B5-0331 and
B5-0331a, the ordering row DONE B5-2135 which recorded F12 as "CLOSED for
framing, structured-entry question is B5-2137's", and a read-only trace of
`GameState.log`, `GameController.processAction`, `RulesEngine`, and every
`getLog()` consumer taken this pass.

**This is a docs-only proposal. No source file was read-modified or written.**

## 0. The measured finding that changes the premise

B5-2135 closed F12 "for framing" on the strength of two helpers,
`MainWindow.isRoundPrefix` / `isPhasePrefix`
(`MainWindow.java:2384`, `:2400`) called from the log-rendering loop
(`MainWindow.java:1451-1452`). **Re-measured this pass, that framing code cannot
fire on a single line, and never has.**

The loop builds its `candidate` prefix by splitting a log line at its first
colon, and only if that colon sits at index 1..19 (`MainWindow.java:1445-1446`):

```java
int colonIdx = line.indexOf(':');
if (colonIdx > 0 && colonIdx < 20) {
    String candidate = line.substring(0, colonIdx).trim();
    if (MainWindow.isRoundPrefix(candidate) || MainWindow.isPhasePrefix(candidate)) { ... }
}
```

But **every** line in the log is prefixed by `GameState.log`
(`GameState.java:615`):

```java
public void log(String msg) { log.add("[R" + roundNumber + "] " + msg); }
```

So a candidate is always the literal text `[R3`, `[R12`, … — which
`isRoundPrefix` rejects at `MainWindow.java:2385` (`!candidate.startsWith("Round ")`)
and `isPhasePrefix` rejects at `:2401` (`!candidate.startsWith("Phase ")`).
The two header branches at `MainWindow.java:1462` and `:1468-1470` are
unreachable.

The consequence is stronger than "framing is off". The two round/phase banner
emitters in the engine do exist and produce lines whose *content* would match the
predicates if they were not for the prefix:

| Emitter | Line produced | Matches a predicate? |
|---|---|---|
| `RulesEngine.java:1940` | `[R3] === Round 3 begins ===` | no colon before index 20 → not even a candidate |
| `GameState.setPhase` (`:69`) | `[R3] Phase → ACTION` | no colon before index 20 → not even a candidate |
| `GameController.processAction` (`:182`) | `[R3] Londo: INITIATE_CONFLICT: Border Raid` | candidate `[R3` → rejected |

So B5-0331a fixed a real regex-escaping bug and the code it left behind still
does nothing, because the fix addressed the matcher while the *producer* emits a
different shape than the matcher was written for. DONE B5-1157 recorded F12 as
holding "F12 Round-prefix detection 1403-1408" — a line range that no longer
contains that code; B5-2135 cited the helpers as evidence the finding was closed.
Three passes, two of them reading receipts, agree on a framing that measurement
says is dead. **This is a finding, recorded and not fixed here**: `ui/` is
outside this row's `docs/proposals/` scope, and the natural repair is an engine
change (see §5), not a UI one.

The rest of this proposal is therefore written against the *real* log shape, and
§5 specifies the framing as a byproduct of the structured entry rather than as a
separate UI fix.

## 1. What exists today

The log is a `List<String>` on `GameState` (`GameState.java:616`, returned
unmodifiable), appended by a single method (`:615`). There are 156 `.log(` call
sites across `model/` and `engine/`. Four shape families are in use today:

| Family | Example as emitted | Emitter |
|---|---|---|
| Round banner | `[R3] === Round 3 begins ===` | `RulesEngine.java:1940` |
| Phase banner | `[R3] Phase → ACTION` | `GameState.java:69` |
| Intent line | `[R3] Londo: INITIATE_CONFLICT: Border Raid -> Narn` | `GameController.java:182` via `GameAction.toString` (`GameAction.java:218-237`) |
| Result line | `[R3] Londo targets Narn with Border Raid.` | `GameController.java:209-210` |

Three observations the format has to respect:

1. **Round is already structured, badly.** `[R<n>] ` is a real field, machine
   readable, and two test helpers already strip it
   (`HeadlessSmokeTest.java:314-321`, `HeadlessMultiRoundTest.java:287-295`).
   It is re-parsed by string surgery in two files.
2. **Actor and action are fused into prose.** `"<name>: " + action` at
   `GameController.java:182` is the only place the acting player is named for
   the intent line, and it is recoverable only because the tests know the
   convention `": INITIATE_CONFLICT:"` (`HeadlessMultiRoundTest.java:250`, `:262`).
3. **Result is prose, not a value.** Every "cannot …" line
   (`GameController.java:199`, `:237`, `:279`, `:419`, `:515`, …) and every
   success line is free English. Nothing records *whether* an action succeeded.

## 2. The proposed entry format

Add one immutable value type in `b5ccg/src/b5ccg/model/` — `LogEntry` — and
keep `GameState.log(String)` as the compatibility door. Java 6, stdlib only, no
generics beyond what `-source 6` allows, no external library.

```
LogEntry
  int     round        // from state.getRoundNumber()
  GamePhase phase      // from state.getPhase(); null only pre-SETUP
  String  actor       // player name, or "" for engine/system entries
  String  action      // GameAction.Type name, or a system verb
  String  target      // target name/title, or ""
  Result  result      // OK | REFUSED | INFO
  String  detail      // the human sentence, exactly the string today
```

`Result` is a small enum with three constants, named to match the existing
vocabulary rather than to invent one: `OK` for lines that report a completed
effect, `REFUSED` for the "cannot …" family, `INFO` for banners and system
lines.

**Rendering contract.** `LogEntry.render()` reproduces today's output byte for
byte: `"[R" + round + "] " + detail`. That is the whole compatibility
guarantee, and it is the property that makes this proposal safe to stage.

## 3. Before and after

**Before** — what the player sees in the log pane today:

```
[R1] Londo: INITIATE_CONFLICT: Border Raid -> Narn
[R1] Londo targets Narn with Border Raid.
[R1] G'Kar cannot support — join not allowed.
[R1] Phase → CONFLICT_RESOLUTION
[R1] G'Kar draws a free card (draw round step 3).
[R2] === Round 2 begins ===
```

Flat, undifferentiated, and — per §0 — with no banners at all.

**After**, with the structured entry and grouping derived from the fields
rather than re-parsed from text:

```
═══════════════════════════════
Round 2
═══════════════════════════════
  ACTION             Londo   INITIATE_CONFLICT   Border Raid -> Narn
      →  Londo targets Narn with Border Raid.
      !  G'Kar cannot support — join not allowed.
  CONFLICT_RESOLUTION
      —  Phase → CONFLICT_RESOLUTION
      ·  G'Kar draws a free card (draw round step 3).
```

Four things changed, and only one of them is cosmetic:

- the round banner now fires, because `round` is a field (`§0`);
- the phase divider now fires, because `phase` is a field;
- `!` marks `REFUSED` lines, which is the first time the log distinguishes
  "I did not allow that" from "that happened";
- the actor column is aligned, so a column scan replaces a prose scan.

The `detail` string is carried unchanged into both renderings. Nothing about
what the game says to the player is edited by this proposal — only how it is
addressed.

## 4. Why the change is staged and not atomic

**Nine test call sites parse the log by string surgery**, and they are the
binding constraint. Each is listed so the implementer can price them:

| Site | What it parses |
|---|---|
| `HeadlessConformanceTest.java:133-138` | `logContains(st, needle)` — substring match, 8 call sites |
| `HeadlessConformanceTest.java:1714-1717` | `"<name> sets agenda: <title>"` and `"<name> plays <title>"` substrings |
| `HeadlessConformanceTest.java:3435-3438` | `indexOf("safety cap reached")` |
| `HeadlessMultiRoundTest.java:250-282` | 7 action buckets keyed on `": INITIATE_CONFLICT:"`, `" builds influence:"`, `" promotes "`, `" plays aftermath:"`, `" sets agenda:"` |
| `HeadlessSmokeTest.java:157-158`, `:314-321`, `:325-334` | `[R` prefix strip; `logExcerpt` prints raw lines into the failure message |

Because `render()` is byte-identical, **none of these need to change in stage 1.**
They are the reason the format is additive rather than a rewrite: the string log
survives as the render target, and the nine parsers keep working untouched.

Three stages:

1. **Add `LogEntry` and `GameState.logEntry(...)`; leave `log(String)` delegating
   to it.** Render unchanged. Gate: `RUN_TESTS=1 bash b5ccg/compile.sh` green,
   with the nine parsers untouched. This is where the value is captured and the
   risk is one enum.
2. **Convert the emitters**, largest group first: the ~20 `REFUSED` "cannot …"
   lines in `GameController.processAction` and `RulesEngine`, then the success
   lines, then the banners. Each conversion is a one-line change at the call
   site because the prose is already written. Gate per group: conformance green.
   *This is the step where `§0`'s framing is fixed for free* — the round and
   phase dividers become field reads in `MainWindow.refresh`.
3. **Migrate the parsers**, highest-value first: `HeadlessMultiRoundTest.parseLog`
   (7 buckets on string fragments) should read `getEntries()` and switch on
   `action` + `result` instead of substring matching. Then the three
   `logContains` needles in `HeadlessConformanceTest`. Then delete
   `stripRoundPrefix` from both files. Gate: conformance green **and** at least
   one assertion deliberately inverted — a test that asserts a *substring is
   absent* — because as §0 shows, a parser that cannot fail is not evidence.

Stage 3 is the only one that can go wrong silently, so it goes last and it is
the only one that needs an observation of its own gates failing first.

## 5. The F12 framing fix, specified but not applied

The dead code in §0 should be **removed, not repaired**, and here is why:
`isRoundPrefix` and `isPhasePrefix` are string predicates over a format that the
structured entry makes unnecessary. Once `round` and `phase` are fields, the
correct renderer reads them, and the two predicates become exactly the kind of
"gate that cannot fail" B5-0331a already had to rescue once. Their removal is
part of stage 2.

Keeping them is worse than removing them: an agent reading
`MainWindow.java:2384` sees a round-detection helper and reasonably infers the
log is grouped. That inference is false, and B5-1157 and B5-2135 both made it.

## 6. What this proposal does not claim

- **No gameplay semantics change.** No rulebook line is interpreted. The
  rulebook anchor (§III, `:332-338`) asks players to *narrate*; this proposal
  structures what the software already says and adds no story content.
- **No `src` edit was made and none is proposed as one line.** Stages 1-3 are
  engine and UI work, both outside this row's `docs/proposals/` scope.
- **No rulebook citation is corrected here.** The `§III` reference stands as
  B5-0310 recorded it.
- **F12's original severity (P3/LOW, cosmetic) is unchanged.** §0 raises the
  *confidence* question about the F12 close-out, not the severity of the
  player-visible defect, which remains "the log is usable as a trace and does not
  read as a story".

## 7. Acceptance criteria for whoever claims the implementation

1. `RUN_TESTS=1 bash b5ccg/compile.sh` exits 0 at the end of every stage.
2. After stage 1, `render()` output is asserted byte-identical to the pre-change
   log for a fixture game — the compatibility claim must be *observed*, not
   reasoned about.
3. After stage 2, a log pane screenshot or captured text shows the round banner
   and the phase divider. This is the §0 finding closing, and it is the only
   visible payoff of the whole change.
4. After stage 2, at least one conformance assertion distinguishes `REFUSED`
   from `OK` where it previously could not.
5. After stage 3, `stripRoundPrefix` has zero remaining callers, and at least
   one migrated parser was observed failing against a deliberately altered log
   before it was trusted.

## Sources

* `.agent/REPORTS/2026-09-21-solar-pro4-B5-0310.md:316-325` — the F12 finding
* `BABYLON5_CCG_RULEBOOK.md:332-338` — §III *Narrating the Story*
* `.agent/TASK_LEDGER.md` rows B5-0331, B5-0331a, B5-1157, B5-1817, B5-2135
* `b5ccg/src/b5ccg/model/GameState.java:69`, `:615-616`
* `b5ccg/src/b5ccg/engine/GameController.java:182`, `:199`, `:209-210`
* `b5ccg/src/b5ccg/engine/RulesEngine.java:1940`
* `b5ccg/src/b5ccg/model/GameAction.java:218-237`
* `b5ccg/src/b5ccg/ui/MainWindow.java:1437-1481`, `:2384-2411`
* `b5ccg/src/b5ccg/engine/HeadlessMultiRoundTest.java:243-295`
* `b5ccg/src/b5ccg/engine/HeadlessSmokeTest.java:157-158`, `:314-321`, `:325-334`
* `b5ccg/src/b5ccg/engine/HeadlessConformanceTest.java:133-138`, `:1714-1717`, `:3435-3438`

**Reusable lesson:** a helper that is called, tested, and cited by three
separate passes can still be unreachable — when a *producer* changed its output
shape and no pass re-read the producer. Reachability is a property of the
producer and the matcher together, and either half drifting leaves green
receipts describing code that never runs.