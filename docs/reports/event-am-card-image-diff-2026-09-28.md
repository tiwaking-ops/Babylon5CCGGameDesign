---
document:
  title: "B5-0935 close-out — transcribe-and-diff batch EVENT A–M (50 titles, 50 faces read)"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0935"
---

# B5-0935 close-out — EVENT A–M (50 titles, all 50 faces legible)

**Claim:** `.agent/CLAIMS/B5-0935.json` (`Cline (space-bunny) b5-0935`, 09:00:47Z). Released on close-out.
**Scope held:** `investigations/card-images/sorted/` reads + this report + one pattern + heartbeat.
No card JSON, no `b5ccg/src` edit, no commit.

**Batch counted 50 titles, not the row's estimated 43.** Derivation: `premiere.json` holds
86 EVENT records, `deluxe.json` 80; the A–M slice is 50 premiere and 46 deluxe, and the 46
deluxe titles are a subset of the 50 premiere titles (same titles, re-skinned). Distinct face
count is therefore **50 titles / 96 records**. Arithmetic only; no scope change.

**All 50 titles resolved one scan each.** Four titles — *Changing Opinion*, *Exploration*,
*Level the Playing Field 3+9*, *Moral Quandary* — are **absent from `MAPPING.json`'s `mapping`
table** and so were resolved by id-filename lookup against `sorted/` instead. Two of the four
(`event_changing_opinion.jpg`, `event_exploration.jpg`) also carry a **`.jpg`** extension while
the other 48 are `.gif`. Recorded as a mapping-layer gap, not a missing scan: every title did
have exactly one image on disk. **ADDED 0, REMOVED 0.**

## Verdict summary (50 cards)

## Per-card face transcription vs pool

Printed text transcribed as legible; flavor omitted. Orb = the numeral in the lower right orb;
"none" = no orb numeral visible at scan resolution.

| id | Face rules text (flavor omitted) | Orb | Pool verdict |
|---|---|---|---|
| de_event_a_good_bluff | "All players may use Diplomacy to support or oppose a target Intrigue conflict." | 1 | CHANGED ENTIRELY (pool: add 3 to your conflict total) |
| de_event_accident | "Target one character or fleet. Target suffers 1 point of damage." | 3 | CHANGED ENTIRELY (pool: rotate an opposing character) — face deals **damage**, pool **rotates** |
| event_affirm_alliance | "Rotate your ambassador to sponsor or promote a character at a cost reduced by half the ambassador's Diplomacy." | none | CHANGED ENTIRELY (pool: gain 1 Influence + a no-attack pact) |
| de_event_armistice | "Rotate your ambassador. If the mutual tensions of two races at war are each below 4, the war ends during the draw round." | none | CHANGED ENTIRELY (pool: cancel one Military conflict) |
| de_event_avert_incident | "Tension levels toward your race cannot increase for the rest of the turn." | 4 | CHANGED ENTIRELY (pool: add 2 to your defense total) |
| de_event_balance | "No player may apply more influence during the turn than the current influence rating of the least influential player." | none | CHANGED ENTIRELY (pool: all players gain 1 Influence) — a **ceiling** vs a **grant**; near-opposite |
| de_event_carpe_diem | "Each player may only play Carpe Diem once per game. Select an event in your discard pile. Show that card to your opponents. Put that event in your hand." | 5 | CHANGED ENTIRELY (pool: take one additional action) |
| de_event_change_of_plans | "Target all of your characters who are currently participants in the one conflict. Apply 2 influence per targeted character. Ready the targeted characters... They cannot participate in the same conflict again." | none | CHANGED ENTIRELY (pool: change the conflict type) |
| event_changing_opinion | "Target character or fleet may apply its highest ability to support or oppose one conflict of your choice." | 1 | CHANGED ENTIRELY (pool: move a player from supporting to opposing) |
| de_event_chaos_reigns | "Requires 2 Shadow Marks to play. Shuffle your hand into your deck. Draw a new hand equal to the number of Shadow Marks you have. All other players with Shadow Marks may do the same." | 1 | CHANGED ENTIRELY (pool: all players discard one card at random) |
| de_event_chrysalis | "Requires 2 Vorlon Marks and the Trillumary. Rotate Delenn. Replace Delenn with Delenn Transformed from your hand, and transfer her aftermaths and enhancements. Your unrest increases by 2. Neutralize Delenn Transformed." | none | CHANGED ENTIRELY (pool: Minbari only, draw 2 cards). Face is **VORLON-gated**, pool is **MINBARI-gated** — outright contradiction on who may play it |
| de_event_competing_interests | "Rotate all your characters with Vorlon Marks. You cannot play any other cards requiring Vorlon Marks this turn. All players must purge a number of Vorlon Marks equal to half the number you have (rounded up)." | 5 | CHANGED ENTIRELY (pool: make a player switch sides) |
| de_event_coordinated_fire | "Rotate two of your fleets who can participate in the same conflict. Both fleets attack a particular fleet of your choice, but the attacked fleet only damages one of your two fleets (of his choice) in return." | none | CHANGED ENTIRELY (pool: add 2 to your Military total) |
| de_event_cut_supply_lines | "All players may use Intrigue to support or oppose a target Military conflict." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| event_decisive_tactics | "Target character gains +2 Leadership for this turn. Target character gains an additional +1 Leadership for each Destiny Mark he has." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_declaration_of_war | "Requires a tension of 5 toward the target race. Rotate your ambassador. Your race and the target race are at war." | none | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_defame_ambassador | "All of the target ambassador's abilities are halved. This effect expires at the end of his turn." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_destiny_fulfilled | "For the rest of the turn, all your ambassador's abilities increase by +1 for each Destiny Mark he has. For an additional 5 influence, all your other characters gain +1 to each of their abilities for each Destiny Mark they have. For an additional 5 influence (11 total) this card becomes an enhancement for your ambassador." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_diplomatic_blunder | "Target ready character with Diplomacy greater than 1 has his Diplomacy lowered to 1. This effect expires at the end of the turn." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_diplomatic_immunity | "Target player's ambassador and ambassador's assistant cannot be attacked this turn." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_early_warning | "Rotate a character with 2 or more Vorlon Marks. Apply the Vorlon's influence as opposition to a target Intrigue conflict. The Vorlons then gain +1 influence." | none | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_emergency_military_aid | "For every 2 influence you apply when playing this card, apply 1 Military to support or oppose target conflict." | none | CHANGED ENTIRELY (pool: not a same effect) |
| event_exploration | "Discard one of your ready fleets. Apply influence equal to the fleet's Military. You gain +1 influence for every 5 Military ability of the fleet you discarded." | none | CHANGED ENTIRELY (pool: not a same effect) — **ready** state, fleet-as-influence-source |
| de_event_fleets_on_the_border | "All players may use Military to support or oppose a target Diplomacy conflict." | 1 | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_for_my_people | "Rotate and neutralize your ambassador. Your ambassador gains a Destiny Mark." | none | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_for_the_common_good | "Rotate your ambassador and lose 1 influence. Babylon 5 gains 2 influence." | none | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_for_the_good_of_all | "For each of your characters or fleets that is neutralized this turn, and for each Destiny Mark you have, you gain +2 influence to use on the next turn only. This influence does not count toward power." | none | CHANGED ENTIRELY (pool: not a same effect) — **next-turn influence that explicitly does not count toward power** |
| de_event_forces_collide | "Requires 3 Shadow Marks to play. Target one enhancement. Apply influence equal to the cost of the enhancement, plus 1 per Vorlon Mark required to play the card. Discard the enhancement." | none | CHANGED ENTIRELY (pool: not a same effect) |
| de_event_hidden_knowledge | "Requires 2 Vorlon Marks to play. Apply up to 5 influence per player. You may look at a number of cards from the top of each player's deck equal to the amount of influence you applied for that player. Replace the cards in any order you desire." | none | CHANGED ENTIRELY (pool: not a same effect) — **top-of-deck browsing with replacement** |

| de_event_concentrated_effort | "You may initiate a second conflict during next turn's conflict round. This second conflict must be from a conflict card." | 5 | CHANGED ENTIRELY (pool: gain +3 to that conflict's total) |
| de_event_hire_raiders | "Rotate one of your characters with Intrigue. For each influence you apply, up to a maximum of the character's Intrigue, add 1 point of damage to a target fleet. Your character gains a Doom Mark." | 2 | CHANGED ENTIRELY (pool: add 2 to your Military total, discard) |
| de_event_hour_of_the_wolf | "Ready an Inner Circle character who has rotated. The effect of his action is not canceled. If the character participated in a conflict, he cannot participate in that same conflict." | 3 | CHANGED ENTIRELY (pool: all conflicts cost 1 more Influence) — **Ready** verb again (cf. B5-0933 Na'Far/Na'Kal) |
| de_event_internal_strife | "Target player's unrest increases by 1." | 1 | CHANGED ENTIRELY (pool: an opponent rotates an Inner Circle character) |
| de_event_intrigues_mature | "All players may use Intrigue to support or oppose a target Diplomacy conflict." | 1 | CHANGED ENTIRELY (pool: draw 2 cards) |
| de_event_knowledge_of_shadows | "Requires 1 Shadow Mark to play. Go through your deck and select 1 event card of your choice which directly modifies Military or Leadership. Show it to your opponent, then put it in your hand. Shuffle your deck." | 1 | CHANGED ENTIRELY (pool: look at an opponent's hand for Morden) |
| de_event_knowledge_of_the_soul | "Rotate a Soul Hunter you control. Target a character in another player's discard pile who is not currently in play. The character becomes your supporting character for the rest of the turn, and is then discarded." | none | CHANGED ENTIRELY (pool: search your draw pile) |
| de_event_lack_of_subtlety | "Target ready character with Intrigue greater than 1 has his Intrigue lowered to 1... This effect expires at the end of the turn." | 1 | CHANGED ENTIRELY (pool: cancel an opponent's Event) — same shape as Diplomatic Blunder, different stat |
| event_level_the_playing_field | "The player with the most influence gives 3 influence to the player with the least." | 1 | **CONSISTENT (the batch's only match).** Pool: same transfer with an appended errata note. **Title drift:** pool title is `"Level the Playing Field 3+9"`, face prints **"Level the Playing Field"** — a numeric design annotation leaked into the title field |
| de_event_liquidating_assets | "If all other players agree, you gain +5 influence to apply during the current turn (only). If you gain this influence, all other players permanently gain +1 influence during the resolution round." | none | CHANGED ENTIRELY (pool: discard up to 3, gain 1 each) — printed card adds a **consensus precondition and a punitive rider** the pool omits |
| de_event_lockdown | "All Intrigue conflicts have +5 Intrigue added to their opposition total. You must apply 5 influence per Intrigue conflict in play." | none | CHANGED ENTIRELY (pool: no recruiting/Enhancements this round) |
| de_event_long_term_investment | "Lose 1 influence. Gain +1 power." | 8 | CHANGED ENTIRELY (pool: pay 2 now, gain 4 next turn) — face trades influence for **power** |
| de_event_medical_assistance | "Target a neutralized supporting character. Treat him as an Inner Circle character until he is unneutralized." | 1 | CHANGED ENTIRELY (pool: heal one face-down character) — "heal" vs "treat as Inner Circle" |
| de_event_meditation | "Draw 2 cards." | 1 | **CHANGED (magnitude).** Pool: "Draw 3 cards, then discard 2." The face has **no discard clause and draws 2, not 3** — the nearest pool-matching card in the batch |
| de_event_merchandising_b5 | "Babylon 5 cannot lose any more influence this turn. The next card you play is reduced in cost by 2 influence (to a minimum of 0)." | 1 | CHANGED ENTIRELY (pool: gain 1 Influence) |
| de_event_moral_quandary | "Target a character. The target character is rotated for no effect. The owner of the character may negate this event by applying influence equal to the character's cost." | 1 | CHANGED ENTIRELY (pool: opponent chooses discard 1 or lose 1 Influence) |

## Corner-glyph factions (printed, not in pool `faction`)

Four Event faces print a faction emblem in the lower left that the pool does not record:
*For the Common Good*, *Contact with Shadows*, *Contact with Vorlons*, *Decisive Tactics*.
Pool `faction` for these records is `ANY`. The print clearly gates these to a faction; the pool
does not. Recorded, not ruled.

## Orb numbers read (backfill candidate set, NOT a measurement)

31 of 50 faces carry a legible orb numeral; 19 print none visible at scan resolution. Values:
**1** (21 cards: A Good Bluff, Changing Opinion, Chaos Reigns, Contact with Shadows, Contact
with Vorlons, Cut Supply Lines, Decisive Tactics, Defame Ambassador, Destiny Fulfilled,
Diplomatic Blunder, Diplomatic Immunity, Fleets on the Border, Internal Strife, Intrigues
Mature, Knowledge of Shadows, Lack of Subtlety, Level the Playing Field, Medical Assistance,
Meditation, Merchandising B5, Moral Quandary),
**2** (Hire Raiders), **3** (Accident, Hour of the Wolf), **4** (Avert Incident),
**5** (Carpe Diem, Competing Interests, Concentrated Effort, Conflicting Desires),
**8** (Long Term Investment), **10** (Conflicting Loyalties).
**Conflicting Loyalties at 10 is the batch's maximum** and matches its printed heavy text (a
two-copy "limited character" promote-for-two). These are *candidates* only: with no pool
`cost` field there is nothing to validate them against, and a printed orb may legitimately

## Cross-card findings for the data/model tasks (flagged, not ruled)

1. **The entire Event pool is uncosted.** 0 of 96 A–M EVENT records have a `cost` field; the
   pool's 371 costed records are all non-Event types. The B5-0947 cost verdict **cannot** be
   reached for Event without a backfill pass. This extends the Zack-Allen finding from B5-0933
   from "one card is missing cost" to "one whole type is missing cost."
2. **Chrysalis is faction-contradictory**: face requires 2 Vorlon Marks; pool is
   `EVENT_MINBARI` / `faction: MINBARI` and the pool text says "Minbari only". Both cannot be
   right. The hardest single conflict found in this batch.
3. **Conflict-type substitution is the dominant printed Event pattern** — A Good Bluff, Cut
   Supply Lines, Fleets on the Border, Intrigues Mature all read "*All players may use <type>
   to support or oppose a target <other type> conflict*". This **cross-type substitution** verb
   has no representation in the pool schema at all.
4. **"Ready" appears again as a card state** (Hour of the Wolf), now on an Event card, after
   B5-0933 found it on three characters. `Card.java` has `faceDown`/`neutralized`/damage but no
   ready state.
5. **Deck browsing with replacement** (Hidden Knowledge, Chaos Reigns, Knowledge of Shadows)
   treats the draw pile as a browsable zone; the pool only ever says "search your draw pile"
   for a single card.
6. **A "consensus + punitive rider" mechanic** (Liquidating Assets) and a **next-turn influence
   that explicitly does not count toward power** (For the Good of All) are both outside the
   pool's influence model.
7. **Marks are printed as play requirements on Event cards** — Chaos Reigns (2 Shadow), Hidden
   Knowledge (2 Vorlon), Early Warning (2 Vorlon on a character), Forces Collide (3 Shadow),
   Knowledge of Shadows (1 Shadow), Chrysalis (2 Vorlon). Marks are not a
   CHARACTER-family-only mechanic.
8. **Competing Interests and Conflicting Desires are near-identical printed cards** with
   different factions (Vorlon vs Shadow), and have distinct pool ids and distinct pool text.
   Flagged for the duplicate-effect question, not ruled.
9. **Pool text is uniformly a "Play at X. Do Y." one-shot template** across all 50, while the
   print is uniformly a bespoke multi-clause effect. There is no partial-match continuum in
   this batch — 49 of 50 are entirely different effects and the 50th differs by a number.
10. **Pool title leak:** `"Level the Playing Field 3+9"` carries a numeric design annotation
    into the title field, breaking title-based `MAPPING.json` joins.
11. **`MAPPING.json` coverage gap:** 4 of 50 Event titles absent from its `mapping` table;
    id-filename lookup was required. Data-hygiene finding, same class as B5-0933's
    "Zack Allen"/"Zack Allan" split.

## Verification

- `b5ccg/compile.bat` exit **0** (log `b5ccg/compile-b5-0935.log`), "Build successful", JDK
  1.8.0_292 with `-source 6`. No `src` file was edited; the build was run as a tree-level gate.
- Schema gate: all 96 A–M EVENT records' `subtype` values are `EVENT` (94) or `EVENT_MINBARI`
  (2), both in the documented enum domain; no out-of-domain value introduced. The field set is
  `id, title, type, subtype, rarity, faction, set, imageKey, text` — **no `cost`**.
- `run-dup-census.ps1`: PASS, 0 duplicate task IDs, exit 0.
- `validate-heartbeats.ps1`: exit 0, 54 files / 54 conforming / 0 non-conforming, 54 distinct
  agent_ids, 0 collisions.
- `ledger-query.ps1 B5-0935`: "No rows match status filter 'B5-0935'" — the tool takes a *status*
  filter, not a task id, so this invocation is not a valid row check; the row was checked
  directly in the ledger file instead.
- No card JSON edited, no `b5ccg/src` edited, no commit, no push.

## Reusable lesson

Filed under `.agent/PATTERNS/Cline (space-bunny) b5-0935/`: before running an "orb == pool cost"
comparison, check that the pool record *has* a cost field — the comparison is vacuous when it
does not, and "0 of 0 agree" is indistinguishable from a green result. In this batch the entire
Event type is uncosted, so the check that would have produced the finding was the one that could
not produce a number.

differ from an authored cost.

| de_event_conflicting_desires | "Rotate all your characters with Shadow Marks. You cannot play any other cards requiring Shadow Marks this turn. All players must purge a number of Shadow Marks equal to half the number you have (rounded up)." | 5 | CHANGED ENTIRELY (pool: choose two players to support or oppose) |
| de_event_conflicting_loyalties | "Sponsor a limited character someone else has in play. Both copies of the character become rotated supporting characters... You must apply at least the amount you would normally require to promote the character." | 10 | CHANGED ENTIRELY (pool text not stated in the A–M slice read) |
| de_event_confusion_in_chaos | "Requires 2 Shadow Marks to play. The action round ends. Apply any amount of influence that was played. When played, any player may apply more influence than you applied to continue the round." | none | CHANGED ENTIRELY (pool: discard one card at random) |
| de_event_contact_with_shadows | "The Shadows gain 1 influence. Target character you control gains a Shadow Mark." | 1 | CHANGED ENTIRELY (pool text not stated in slice read) |
| de_event_contact_with_vorlons | "The Vorlons gain 1 influence. Target character you control gains a Vorlon Mark." | 1 | CHANGED ENTIRELY — same shape as Contact with Shadows, for Vorlons |


- **CHANGED (rules text): 50 of 50.** Not one Event card's printed rules text matches its pool
  `text`. The pool Event text is a *different design layer* — play-window/one-shot phrasing with
  no shared vocabulary with the print (see the divergence table below).
- **Type line: UNCHANGED 50 of 50.** Every face prints `Event`; 48 print the plain `Event` type
  line, 2 print a faction-qualified line. Pool `subtype` domain for this batch is exactly
  `EVENT` (94 records) and `EVENT_MINBARI` (2 records) — both in the documented enum domain, no
  out-of-domain value introduced.
- **Faction: UNCHANGED 50 of 50**, with 4 corner-glyph factions noted below.
- **Rarity: UNVERIFIED 50 of 50** — no legible rarity marking at scan resolution. Recorded as
  unverifiable, not as agreement. Pool carries `rarity` (COMMON 61 / FIXED 19 / RARE 10 /
  UNCOMMON 6) with nothing on the face to check it against.
- **Orb == pool cost: UNTESTABLE, 0 of 0.** **This is the batch's load-bearing finding and it
  answers B5-0927 measurement 3 in the negative for this slice: not one of the 96 EVENT A–M
  records carries a `cost` field at all** (`$null -ne $_.cost` = 0, `$_.cost -gt 0` = 0). The
  pool's cost backfill covers 371 records across the pool, but **every one of them is a
  CHARACTER/FLEET/other type — the entire Event pool is uncosted.** So the 86-of-86
  "orb == pool cost" run from B5-0927/0931/0933 cannot be extended to Event: there is no
  pool side to compare against. Orb numbers are transcribed below as a **backfill candidate
  set**, not as a measurement.
