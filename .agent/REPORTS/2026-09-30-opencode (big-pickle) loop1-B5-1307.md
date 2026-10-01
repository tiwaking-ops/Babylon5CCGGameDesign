---
document:
  title: "B5-1307 close-out - the B5-1149 reprint dominance reproduces exactly (109 of 200, delta 0 in every faction, seed-independent), and the cost key does not differ between the twins on any seated reprint slot, so the substitution is behaviour-neutral"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "opencode (big-pickle) loop1", version: "big-pickle"}
  created_date: "2026-09-30"
  task: "B5-1307"
---

# B5-1307 close-out - does the reprint fallback change what the fixed halves cost?

**Claim:** `.agent/CLAIMS/B5-1307.json` (`opencode (big-pickle) loop1`, started
2026-09-30T23:14:28Z), released at close-out. Row re-read `OPEN` before claiming;
no live claim on the row (`B5-1047`, `B5-1313`, `B5-1331` held by others).
Gate `b5ccg/compile.bat` green (exit 0, "Build successful") after the probe;
`run-dup-census.ps1` PASS with 0 duplicate task IDs. Scope held: read-only probe
under the git-ignored `b5ccg/out/b51307/`, this report, one pattern, heartbeat,
DECISIONS, the row flip. **No deck JSON, card JSON, or src edit; no commit; no
push.**

## Verdict

1. **The B5-1149 baseline reproduces exactly, per faction, with delta 0** -
   HUMAN 25 DELUXE / 25 PREMIERE, CENTAURI 30/20, MINBARI 28/22, NARN 26/24,
   total **109 DELUXE of 200** (54.5 %). This is a *delta report*, not a
   replacement census, and the delta is zero everywhere. The fixed half is also
   **seed-independent** (identical `id:cost` fingerprints at seeds 1 and 987654321
   for all four factions), so unlike B5-1303's random-half figures these numbers
   are not draw-dependent.
2. **The cost key does not differ between the twins on any seated reprint slot.**
   All **91** distinct titles seated as a Deluxe reprint have identical `cost`
   on their Premiere record and their Deluxe record - 91 equal, 0 different.
   Slot-level counterfactual over all 200 fixed slots: **55 of 200 cost more than
   the starting pool of 4 as seated, and 55 of 200 would if the other set's record
   were seated; 0 slots change cost.**
3. **Consequence: the 55-of-200 cost pressure is a property of the printed fixed
   lists, not of the reprint substitution.** B5-1149 charged each reprint slot at
   "the Deluxe record's cost"; that charge is not a distortion. Any proposal that
   avoids the title fallback - preferring the printed Premiere record, adding
   premiere-only decks, or "correcting" the set origin - would change the cost
   surface of the fixed halves by **zero slots**.

## Instrument

`b5ccg/out/b51307/b51307/B51307ReprintCostProbe.java` (Java 6, stdlib only,
git-ignored), run as
`java -cp b5ccg/out;b5ccg/out/b51307 b51307.B51307ReprintCostProbe`, receipt
`b5ccg/out/b51307-receipt.txt`, stderr empty. Everything is read through the
production path - `DeckLoader.loadBothSets` for the pool,
`DeckLoader.loadFromResource` per file for the two raw sets (so the twin
comparison never relies on the deduped pool), and `StarterDeckBuilder.build` for
seating.

## A. Set origin per faction, versus the B5-1149 receipts

| faction | DELUXE | PREMIERE | baseline DELUXE | delta | baseline PREMIERE | delta |
|---|---|---|---|---|---|---|
| HUMAN | 25 | 25 | 25 | **0** | 25 | **0** |
| CENTAURI | 30 | 20 | 30 | **0** | 20 | **0** |
| MINBARI | 28 | 22 | 28 | **0** | 22 | **0** |
| NARN | 26 | 24 | 26 | **0** | 24 | **0** |
| **total** | **109** | **91** | **109** | **0** | 91 | 0 |

Seed-independence of the fixed half (`deluxe=N` plus a hash over the seated
`id:cost` sequence): HUMAN deluxe=25 hash 1525229437, CENTAURI deluxe=30 hash
551799126, MINBARI deluxe=28 hash 720416273, NARN deluxe=26 hash 1032140432 -
identical at both seeds.

Pool shape, which explains *why* the substitution is unconditional:

```
deduped pool=446  premiere=446  deluxe=383
titles present in BOTH files=383  titles deluxe-only=0
```

Every Deluxe record is a reprint of a Premiere title, so `loadBothSets`
(`DeckLoader.java:36-46`, Deluxe wins by title) always removes the Premiere
record of any reprinted title. The fixed lists name Premiere ids
(`StarterDeckBuilder.java:116-118` says so explicitly), so for every reprinted
title the id lookup at line 114 **cannot** hit and the title fallback at line 118
**always** fires. There is no condition under which a reprinted fixed entry seats
its Premiere record - hence a delta of zero is the only possible outcome, and the
reproduction is a genuine check on the measurement path, not a coin flip.

## B. The cost key, twin against twin

`cost` is a plain field on `Card` (`Card.java:25,67-68`, `setCost` clamped at 0),
read from the record's `cost` key. Comparing the Premiere and Deluxe record for
every reprinted title actually seated in a fixed half:

```
distinct titles seated as a reprint across the four fixed halves = 91
twin pairs compared                                          = 91
costs EQUAL                                                  = 91
costs DIFFER                                                 = 0
of those pairs, deluxe cost>4 = 23   premiere cost>4 = 23
overPool flips to UNDER when the reprint is seated = 0   flips to OVER = 0
```

Slot-level counterfactual across all 200 fixed slots, swapping each seated card
for its same-title twin in the other set:

```
as seated (Deluxe wins)   cost>4 in fixed halves = 55 of 200
counterfactual (twin set) cost>4 in fixed halves = 55 of 200
slots whose cost changes if the other set's record were seated = 0
```

The 23 titles priced above the starting pool of 4 are the same 23 on both sides,
and no slot crosses the threshold in either direction. This is B5-1149's 55/200
(16 HUMAN, 12 MINBARI, 11 CENTAURI, 16 NARN) re-derived from the twin data
instead of from the seated data, and it lands on the same number for a different
reason.

## C. Adjacent fields, for the same 91 seated pairs

The substitution is not cost-specific; the fields the engine reads for play are
identical across every seated twin pair:

| field | differing pairs (of 91) |
|---|---|
| `cost` | 0 |
| `influenceReward` (conflicts) | 0 |
| `rarity` | 0 |
| `subtype` | 0 |
| `faction` | 0 |

So the reprint fallback is an **identity** substitution - `conf_border_raid`
becomes `de_conf_border_raid` - with no behavioural difference in the fields
above. The one place set origin is observable at all is the image key and any
future set-aware logic, and B5-1149's own reusable lesson already framed the
pipeline point: a fixed id may deliver a different set's record.

## Scope note on the two slot counts

109 reprint slots resolve to 91 distinct titles. The 18-slot gap is the four
duplicated Deluxe entries measured in B5-1305 (`de_char_centauri_agent`,
`de_event_contact_with_vorlons`, `de_event_declaration_of_war`,
`de_fleet_deep_space_narn`, one extra slot each) plus reprint titles seated in
more than one faction's fixed half - so slot counts and title counts are
different measures and both are reported rather than one standing in for the
other. B5-1149 counted slots; section B compares titles, and section B2 returns
to slots so the cost comparison is done at the granularity that matters.

## Not done

No deck JSON, card JSON, src, conformance-section or AI edit; no commit; no
push. No attempt to "fix" the set origin, because the measurement shows there is
nothing to fix on the cost surface. `b5ccg/resources/cards/deluxe.json` was
already modified in the working tree by another agent before this task started;
it was read, never written, by this session. Probe stderr was empty, so the
`StarterDeckBuilder` guards at lines 129-136 (missing ids, non-50 fixed count)
did not fire for any faction - consistent with B5-1149's "200/200 resolve".

**Reusable lesson:** reproducing a predecessor's number is half a cross-check -
the half that can still say something is the counterfactual, "what changes if the
mechanism were removed", because a matched census only proves the number is
stable, never that the mechanism matters; and when the substitute and the
original agree on every field the engine reads, the mechanism is a naming
difference and any fix aimed at it is a measured no-op.