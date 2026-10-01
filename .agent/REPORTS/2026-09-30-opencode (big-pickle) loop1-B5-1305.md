---
document:
  title: "B5-1305 close-out - the NARN conf_limited_strike pair is the only duplicated conflict id across the four fixed lists, StarterDeckBuilder seats both copies, and the double is a source-transcribed fixed-list entry that is load-bearing for the 50-card total"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1305"
---

# B5-1305 close-out - double-seated conflict: intended double or accidental duplication?

**Claim:** `.agent/CLAIMS/B5-1305.json` (`opencode (big-pickle) loop1`, started
2026-09-30T23:05:03Z), released at close-out. Row re-read `OPEN` before claiming;
no live claim on the row. Gate `b5ccg/compile.bat` green (exit 0) after the
probe, `run-dup-census.ps1` PASS with 0 duplicate task IDs. Scope held:
read-only probe under the git-ignored `b5ccg/out/b51305/`, this report, one
pattern, heartbeat, DECISIONS, the row flip. **No deck JSON, card JSON, or src
edit; no commit; no push.** Rows `B5-1449` and `B5-1459` fence this row and own
the adjacent questions (fixed-list order semantics, deck-total arithmetic); both
were left alone.

## Verdict, answering the row's three questions

1. **Confirmed.** `premiere-starter-decks.json` line 167 -
   `{"deck": "NARN", "id": "conf_limited_strike", "count": 2}` - is the **only**
   conflict id carrying more than one copy anywhere in the four faction fixed
   lists. The fixed halves seat **32 conflict slots over 31 distinct conflict
   ids**, and that single excess slot is NARN's Limited Strike.
2. **Seats both copies.** `StarterDeckBuilder` does not dedupe. The fixed-list
   loop adds the same `Card` instance `count` times, so the NARN fixed 50 holds
   Limited Strike twice (verified by reference identity, not by title counting).
3. **Intended double, per the in-repo transcription** - not an accidental
   duplication, and not a bug. The advisory fixed-list investigation records the
   NARN pair explicitly as coming from the source, and the `count: 2` is
   *load-bearing*: it is what makes the NARN fixed list reach exactly 50.

## Instrument

`b5ccg/out/b51305/b51305/B51305DoubleSeatProbe.java` (Java 6, stdlib only,
git-ignored), run as
`java -cp b5ccg/out;b5ccg/out/b51305 b51305.B51305DoubleSeatProbe`, receipt
`b5ccg/out/b51305-receipt.txt`, stderr `b5ccg/out/b51305-stderr.txt` (empty). The
census is read through the **production path only** -
`DeckLoader.loadFlatObjects(StarterDeckBuilder.DECK_RESOURCE)` for the declared
lists, `DeckLoader.loadBothSets` for the deduped pool, `StarterDeckBuilder.build`
with `setRandomSeed(42)` for seating.

## A. The four fixed lists, as declared

| deck | entries | sum of `count` | conflict slots | distinct conflict ids | entries with `count` > 1 |
|---|---|---|---|---|---|
| HUMAN | 48 | 50 | 10 | 10 | `event_level_the_playing_field` x3 |
| CENTAURI | 47 | 50 | 7 | 7 | `char_centauri_agent` x2, `event_level_the_playing_field` x3 |
| MINBARI | 47 | 50 | 7 | 7 | `event_contact_with_vorlons` x2, `event_level_the_playing_field` x3 |
| **NARN** | **45** | **50** | **8** | **7** | `event_declaration_of_war` x2, `fleet_deep_space_narn` x2, `event_level_the_playing_field` x3, **`conf_limited_strike` x2** |

Every `count` > 1 entry in the whole resource, with the type resolved through the
pool (no untyped counts):

```
deck=HUMAN    event_level_the_playing_field x3  EVENT
deck=CENTAURI char_centauri_agent            x2  CHARACTER
deck=CENTAURI event_level_the_playing_field  x3  EVENT
deck=MINBARI  event_contact_with_vorlons     x2  EVENT
deck=MINBARI  event_level_the_playing_field  x3  EVENT
deck=NARN     event_declaration_of_war       x2  EVENT
deck=NARN     fleet_deep_space_narn          x2  FLEET
deck=NARN     event_level_the_playing_field  x3  EVENT
deck=NARN     conf_limited_strike            x2  CONFLICT/MILITARY
```

Multiplicity is therefore normal in this resource - nine entries, four decks, and
it is present in three of the four lists. It is not a NARN quirk.

### An instrument that could not have gone red

The first pass of the duplicate census asked a narrower question than the row
does: it counted conflict ids that appear on **more than one line**, and it
reported `0` for every deck including NARN. That was wrong, and wrong in the way
worth recording: the duplication lives in a `count` field on a single line, so a
line-duplication instrument is structurally incapable of detecting the phenomenon
under audit, and its clean result carried no information at all. Corrected to sum
`count` per id:

```
HUMAN    conflictIdsWithMoreThanOneCopy=0
CENTAURI conflictIdsWithMoreThanOneCopy=0
MINBARI  conflictIdsWithMoreThanOneCopy=0
NARN     conflictIdsWithMoreThanOneCopy=1   [conf_limited_strike x2]
TOTAL conflict ids with more than one copy across the four fixed lists = 1
```

Had the narrow instrument been the only one run, this row would have closed with a
fabricated "no duplicates" finding. Filed as the pattern for this task.

## B. Seating: two copies, one object, one exclusion

`StarterDeckBuilder.java:121-127`:

```java
int n = parseInt(e.get("count"), 1);
for (int k = 0; k < n; k++) {
    deck.add(c);
    fixedCount++;
}
fixedIds.add(c.getId());
fixedTitles.add(c.getTitle());
```

The loop runs `n` times against one resolved `Card`, so both NARN slots hold the
**same instance**. Measured through the built deck at seed 42:

| deck | fixed slots | distinct fixed ids | seated ids with > 1 copy | Limited Strike copies | same instance? | also in random half |
|---|---|---|---|---|---|---|
| HUMAN | 50 | 48 | `event_level_the_playing_field` x3 | 1 | - | no |
| CENTAURI | 50 | 47 | `de_char_centauri_agent` x2, `event_level_the_playing_field` x3 | 1 | - | no |
| MINBARI | 50 | 47 | `de_event_contact_with_vorlons` x2, `event_level_the_playing_field` x3 | 1 | - | no |
| **NARN** | **50** | **45** | `conf_limited_strike` x2, `event_level_the_playing_field` x3, `de_event_declaration_of_war` x2, `de_fleet_deep_space_narn` x2 | **2** | **yes** | no |

So the builder distinguishes two things cleanly, and neither is a dedupe of the
deck: the **slots** honour `count` (`deck.add` per copy), while the
**exclusion bookkeeping** is set-based (`Set.add`, lines 126-127), so a doubled
entry still suppresses the random half exactly once. Measured: Limited Strike is
absent from the NARN random half in every run. Anyone reading the row's "seats or
dedupes" question as one thing would get the wrong answer from either half alone.

## The double is load-bearing for the 50-card total

NARN declares **45 entries summing 50**. The `count: 2` on Limited Strike supplies
the 46th-50th cards, so by arithmetic (50 - 1) a `count: 1` NARN list sums to 49
and trips the guard at `StarterDeckBuilder.java:133-136`, which reports
`StarterDeckBuilder: NARN fixed count=49 (expected 50)` on **stderr** and then
builds a 59-card deck. The duplication is not redundancy to be cleaned up; it is
one of the five slots the printed list spends there. This also means the failure
mode of "fixing" the pair would be silent to the build and loud only in a
`java.util.logging`-free stderr line - worth the adjacent rows' attention, though
the deck-total audit belongs to `B5-1459`.

## C. Intended or accidental: what the in-repo source says

`investigations/b5-premier-starter-deck-fixed-lists-2026-09-21.md` (tier:
advisory, never canonical) is the origin recorded for these lists:

* line 36: "Each list sums to exactly **50** cards (counts greater than 1 where
  the source shows them)."
* line 50: the other multi-count entries ("...Field x3, Centauri Agent x2,
  Contact with Vorlons x2) are reported exactly as in the source."
* line 51: "**The Narn list contains several x2 entries (Declaration of War, Deep
  Space Fleet, Limited Strike).**"
* line 237, in the Narn table: `| Limited Strike | Conflict | Military | 2 |`
  against `| 1 |` at lines 79 (Human), 136 (Centauri), 188 (Minbari).

So the pair was transcribed **deliberately, with its multiplicity**, from the
archived fan source named at lines 28-32 (Mike Prasek's Houston B5 CCG pages,
Wayback capture 2019-08-05). **Intended** is the correct verdict, on the
transcription's own authority.

The authority caveat is the same one B5-1303 recorded: `investigations/` is
advisory, and its underlying source is a fan compilation rather than a print
read. So "intended" here means *intended by the transcribed source*, not
independently print-verified. No deck or card edit is warranted on either
reading - correcting `count` to 1 would break the 50-card total.

## Two side findings

* **Set origin.** Limited Strike has no Deluxe reprint - the deduped pool
  contains no `de_conf_limited_strike`, and the title resolves to
  `conf_limited_strike` / `PREMIERE`. So this doubled conflict is the one
  multi-count conflict seated from the **Premiere** print, while NARN's other
  doubles resolve through the title fallback to Deluxe twins
  (`de_event_declaration_of_war`, `de_fleet_deep_space_narn`). B5-1149's
  "reprint fallback dominates" does not apply to this pair.
* **B5-1155 checks out.** Its claim - line 167, `count: 2`, "the only duplicated
  conflict id across all four decks", whole-deck census "11 conflict cards over 10
  distinct ids" - is consistent with the measured fixed half (8 cards over 7
  distinct ids) plus a random half of 3 mutually distinct conflicts. Its word
  "incidental" is a description of an oddity, not a defect claim, and nothing
  here contradicts it. No correction to that report is warranted.

## Not done

No deck JSON, card JSON, src, conformance-section or AI edit; no commit; no
push. `b5ccg/resources/cards/deluxe.json` was already modified in the working
tree by another agent before this task started; it was read, never written, by
this session. Two OpenCode-side notes: the deck resource is a JSON array of
one-object-per-line entries, so it must be parsed whole
(`Get-Content -Raw | ConvertFrom-Json`) - piping matched lines into
`ConvertFrom-Json` fails with `Invalid JSON primitive` - and PowerShell 5.1
`1>` redirection writes UTF-16, which the receipt was rewritten from.

**Reusable lesson:** a check that reports "nothing found" must be shown capable of
reporting something - my duplicate census keyed on repeated id *lines* and could
never fire on a duplication carried by a `count` field, so its clean output was a
statement about the instrument, not the data; and when a duplicate turns out to
supply a required slot, the multiplicity is data, not redundancy.