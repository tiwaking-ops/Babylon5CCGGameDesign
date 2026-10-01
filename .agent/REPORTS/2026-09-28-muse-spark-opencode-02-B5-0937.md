---
document:
  title: "B5-0937 close-out — transcribe-and-diff EVENT N–Z (40 titles)"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0937"
---

# B5-0937 close-out — EVENT N–Z (40 titles; 38 faces read + 2 pilot-carried)

**Claim:** `.agent/CLAIMS/B5-0937.json` (`muse-spark-opencode-02`, 09:03:26Z). Released on close-out.
**Scope held:** `sorted/` reads + this report + one pattern + heartbeat. No card JSON, no src,
no existing-file edits, no commit. (Batch holds 40, not the row's estimated 38. Moral
Quandary and Unrecognized Data carried from the B5-0927 pilot — same scan bytes, verdicts
cited not re-read. B5-0935 was deliberately NOT touched: live foreign claim at 09:02Z.)

## Verdicts (40 cards)

- **CHANGED (rules text): 38** (incl. the 2 pilot-carried). Pool text is a placeholder or a
  different effect in every case.
- **PARTIAL: 2** — Short Term Goals (net +1 Influence agrees; face adds Purge-a-Destiny-Mark
  cost) and You Are Not Ready (cancel-a-conflict agrees; face adds 3-Vorlon-Mark requirement
  + Vorlon tax).
- **Type line / faction: UNCHANGED 40 of 40** (all "Event", all ANY). **Rarity: UNVERIFIED.**
- **ADDED: 0. REMOVED: 0.** Title drift: pool "Victory in My Grasp" vs face "Victory In My
  Grasp" (case only).
- **Orbs:** pool EVENT records carry no cost field at all, so every legible orb is a backfill
  datum, not a match: 1×28ish observed (1 on most faces; 5 on Political Realignment, Recalled
  to Service, Secret Strike, War by Popular Decree, Trade Windfall; 3 on Prolonged Talks;
  7 on Price of Power; 9 on Subliminal Influence; 2 on Vorlon Rescue). Not legible on 6 faces
  (Rally, Ramming, Secret Vorlon Aid, Seduction, Sneak Attack, What Do You Want?, Who Are
  You? — bottoms cropped or glyph unclear; recorded per card, never defaulted to 1).

## Per-card face transcription vs pool (rules text exact as read)

| id | Face rules text | Pool verdict |
|---|---|---|
| de_event_medical_assistance | "Target a neutralized supporting character. Treat him as an Inner Circle character until he is unneutralized." (orb 1) | CHANGED (pool: heal face-down) |
| de_event_meditation | "Draw 2 cards." (orb 1) | CHANGED (pool: draw-3-discard-2) |
| de_event_merchandising_b5 | "Babylon 5 cannot lose any more influence this turn. The next card you play is reduced in cost by 2 influence (to a minimum of 0)." (orb 1) | CHANGED (pool: gain-1) |
| de_event_moral_quandary | Pilot-carried: rotate-target-for-no-effect event | CHANGED ENTIRELY |
| de_event_news_of_galactic_import | "No conflicts may be played next turn. News of Galactic Import cannot be played next turn." (orb 1) | CHANGED (pool: all-draw) |
| de_event_not_meant_to_be | "Reverse the printed effect text of an event played since your last chance to act. You must apply influence equal to that applied by the event's player, plus 2 influence per mark required to play the event." (orb not legible) | CHANGED (pool: negate winner's gain). Mark-referenced cost |
| de_event_political_realignment | "Increase or decrease one race's unrest by 1." (orb 5) | CHANGED (pool: move IC character). Unrest again |
| event_popular_support | "Target character gains +2 Diplomacy for the turn. In addition, he gains +1 Diplomacy for each Destiny Mark he has." (orb 1) | CHANGED (pool: gain-2-if-poorest). Destiny Marks |
| de_event_prolonged_talks | "Target Diplomacy conflict in play does not resolve this turn. Instead, the totals applied during resolution are noted, and it is reinitiated next turn with the prior totals already applied…" (orb 3) | CHANGED (pool: double reward) |
| de_event_rally_to_the_cause | "Apply any amount of influence. Remove that amount of severe damage tokens from cards you control." (orb not legible) | CHANGED (pool: +2 support) |
| de_event_ramming | "Target one of your fleets that was attacked and neutralized. Purge a Destiny Mark. The fleet that attacked your fleet takes damage equal to your neutralized fleet's printed Military." (orb not legible) | CHANGED (pool: sacrifice-for-3). "Printed Military" confirms a printed military value distinct from cost; purge mechanic |
| de_event_recalled_to_service | "Select a character in your discard pile. Show that card to your opponents. Put that character in your hand." (orb 5) | CHANGED (pool: fleet recall — wrong card type entirely) |
| de_event_sanctions | "All players may use Diplomacy to support or oppose a target Military conflict." (orb 1) | CHANGED (pool: lose-Influence) |
| de_event_secret_strike | "If you did not initiate a conflict this turn, rotate a character with Intrigue to do so immediately." (orb 5) | CHANGED (pool: hidden fleet) |
| de_event_secret_vorlon_aid | "Rotate a character with 2 Vorlon Marks. Apply +5 Intrigue to support or oppose an Intrigue conflict." (orb not legible) | CHANGED (pool: gain-2/3). Mark-counted requirement |
| de_event_seduction | "Target a character. Character's Intrigue is reduced for the rest of the turn by 1 per influence you apply (to a minimum of 1)." (orb not legible) | CHANGED (pool: side-switch) |
| de_event_self_doubt | "Target a supporting character. Rotate the character for no effect." (orb 1) | CHANGED (pool: -2 primary stat) |
| de_event_shadow_strike | "Requires 2 Shadow Marks to play. Target a fleet. Apply an amount of influence equal to the target fleet's current Military. If target fleet's current Military is not greater than current Shadow Influence, neutralize it." (orb not legible) | CHANGED (pool: +4 total). Current-value mechanics + "current Shadow Influence" — a Shadow-side quantity |
| de_event_short_term_goals | "Purge a Destiny Mark. Gain +1 influence." (orb not legible) | PARTIAL (net +1 agrees; purge cost absent in pool) |
| de_event_short_term_investment | "Apply any amount of influence. For each 3 influence you apply, you gain +1 influence for the next turn only. This additional influence does not count toward power." (orb not legible) | CHANGED (pool: pay-1-gain-2). Next-turn-only + power-exclusion |
| de_event_sneak_attack | "Play at the same time you rotate one of your fleets to support a conflict. Apply influence equal to the cost of another player's fleet. Rotate that fleet for no effect." (orb not legible) | CHANGED (pool: no-event-response) |
| de_event_sortie | "Target any location. The target location may rotate to apply opposition equal to 1/5 of its Military rating to one Military conflict of your choice which targets a card in the location's faction, or its faction as a whole." (orb not legible) | CHANGED (pool: fleet bonus). Location opposition with fractional military |
| de_event_special_ops | "All players may use Military to support or oppose a target Intrigue conflict." (orb 1) | CHANGED (pool: +3 Intrigue) |
| de_event_strafing_run | "All of your fleets do +1 damage this turn when they attack." (orb 1) | CHANGED (pool: rotate loser's fleet) |
| de_event_strategic_reassignment | "Target one of your characters leading a fleet. He is no longer leading the fleet, but he remains rotated." (orb 1) | CHANGED (pool: move supporting/IC). Leading-fleets mechanic again |
| de_event_subliminal_influence | "You may move each of your tension ratings and your unrest level up or down 1." (orb 9) | CHANGED ENTIRELY (pool: force-support). Unrest + tension dial |
| de_event_support_babylon5 | "Rotate your ambassador. Hold a Babylon 5 vote. Babylon 5 gains +1 influence for each yes vote. If the vote passes, Babylon 5 gains +1 additional influence." (orb not legible) | CHANGED (pool: gain-1 + rotate enhancement). Vote mechanic — no pool/model expression |
| de_event_the_opposition_rises | "Target a player. The cost for that player to sponsor any card or play events increases by an amount equal to his unrest for the rest of the turn." (orb 1) | CHANGED (pool: force-support/oppose). Unrest-scaled tax |
| de_event_the_price_of_power | "Target a player. He must discard a character he controls or he does not gain any influence from conflicts this turn." (orb 7) | CHANGED (pool: most-loses-2) |
| de_event_trade_windfall | "Gain +1 influence if you are in a state of Free Trade with another race." (orb 7) | CHANGED (pool: per-Location gain). Free Trade state — B5-0639 R4 alliance/trade evidence |
| event_underworld_connections | "Target character gains +2 Intrigue for the rest of the turn. He gains an additional +1 Intrigue for each Doom Mark he has." (orb 1) | CHANGED (pool: draw-2 + N'Grath clause — face has neither). Doom Marks: a fifth mark type |
| de_event_universe_today_feature | "Increase or decrease one other race's tension toward one any race by 1." (orb 1) | CHANGED (pool: reveal-for-agendas) |
| de_event_unrecognized_data | Pilot-carried: fleet-untargetable event | CHANGED |
| de_event_victory_in_my_grasp | "Rotate your ambassador. Increase all your fleets' Military ratings by +1 for each Destiny Mark your ambassador has for the rest of the turn." (face "In", orb not legible) | CHANGED (pool: +1 Influence when winning) |
| de_event_vorlon_rescue | "Requires 2 or more Vorlon Marks on the target neutralized participant Inner Circle character. The character becomes rotated (if ready), unneutralized with no damage…" (orb 2) | CHANGED (pool: discard-to-supporting) |
| de_event_war_by_popular_decree | "Target a player who has 3 or more unrest and choose a race toward whom the player's race has a tension of 5. The player's race declares war against the chosen race." (orb 5) | CHANGED (pool: everyone-military-or-lose). Unrest-gated war declaration |
| de_event_what_do_you_want | "Any player who wishes rotates his ambassador. Each ambassador who rotates gains a Shadow Mark. The Shadows gain +1 influence for each mark taken." (+ spider; orb not legible) | CHANGED (pool: discard-by-type). Shadow-Mark engine |
| de_event_who_are_you | Vorlon mirror of the above (Vorlon Marks, Vorlons gain). (orb not legible) | CHANGED (pool: reveal hand) |
| de_event_you_are_not_ready | "Requires 3 Vorlon Marks to play. Discard 1 conflict in play. It does not resolve. The Vorlons gain 2 influence." (orb 1) | PARTIAL (cancel agrees; mark requirement + Vorlon tax absent) |
| de_event_you_know_my_reputation | "For the rest of the turn, your ambassador gains Diplomacy equal to his Destiny Marks, Shadow Marks and Vorlon Marks, plus 1 per fleet you rotate when playing this card." (orb 1) | CHANGED (pool: +2 no-backup). All three mark types on one face |

## Cross-card findings (all flagged for B5-0947)

1. **Mark taxonomy now five types:** Shadow, Vorlon, Strife, Destiny, Doom — all on Premiere/
   Deluxe faces, none in pool/model. Mark verbs: gain, require-to-play, purge, count, remove.
   Three Shadow/Vorlon engines (What Do You Want? / Who Are You?) pay their faction per mark
   taken — a Shadow/Vorlon Influence economy ("current Shadow Influence" on Shadow Strike).
2. **Power vs Influence is printed:** "does not count toward power" (Short Term Investment),
   Urza Jaddo's "loses 1 power" (B5-0933) — the B5-0667 question is textual, not theoretical.
3. **"Printed Military"** (Ramming) confirms the orb-vs-military split: fleets print a Military
   value somewhere other than the orb (top-left glyphs, still unmapped) — the orb==cost
   reading (97/97) survives contact with the enemy.
4. **States engine:** Free Trade (Trade Windfall), Non-Aggression (printed on its conflict),
   Babylon 5 votes (Support Babylon 5), tension-5 + unrest-3 war declaration — B5-0639 R4
   (alliance/trade) and R13-adjacent unrest mechanics have printed corpus now.
5. **Event orbs vary 1–9** (Subliminal 9 highest seen) with pool carrying no cost field on any
   event — the backfill set is large and the values are on the faces, not invented.

## Verification

- `ledger-query.ps1`: B5-0937 reads 7 pipes / doubleLead no / DONE after close-out.
- `run-dup-census.ps1` PASS post-write (foreign dups, if any, recorded in DECISIONS untouched).
- No compile gate run: no `src` touched.

## Reusable lesson

Filed under `.agent/PATTERNS/muse-spark-opencode-02/`: on event faces, the orb is the last
thing to look at and the first thing to distrust — costs vary 1–9 with no pool field to
check against, bottoms crop, and a defaulted 1 corrupts the backfill. Legible-or-not per
card, and "not legible" is a complete answer.
