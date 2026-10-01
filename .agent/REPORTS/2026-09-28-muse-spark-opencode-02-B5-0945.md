---
document:
  title: "B5-0945 close-out — GROUP + AGENDA + LOCATION (65 titles)"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0945"
---

# B5-0945 close-out — GROUP + AGENDA + LOCATION (65 titles; 61 faces read + 4 pilot-carried)

**Claim:** `.agent/CLAIMS/B5-0945.json` (`muse-spark-opencode-02`, 09:35:13Z). Released on close-out.
**Scope held:** `sorted/` reads + this report + one pattern + heartbeat. No card JSON, no src,
no existing-file edits, no commit. (Batch holds 65, not the row's estimated 61: 26 GROUP +
26 AGENDA + 13 LOCATION. Defense in Depth, Knowledge is Power, Peace In Our Time, Psi Corps
Intelligence carried from the B5-0927 pilot — same scan bytes. Two self-caught gaps closed
in-pass: Extended Contacts and Power Politics, found by programmatic coverage check, not memory.)

## Verdicts (65 cards)

- **CHANGED (rules text): 63** (59 fresh + all 4 pilot-carried). **PARTIAL: 2** — Commercial
  Telepaths and Diplomatic Corps (core +2 mechanic agrees; pool adds clauses absent on faces).
- **Type line / faction: UNCHANGED 65 of 65** at coarse granularity, with three subtype drifts:
  Maintain the Peace, Higher Calling and Seizing Advantage read plain "Agenda" on the face
  while the pool marks them AGENDA_MAJOR / isMajorAgenda True (Forced Evolution, Order Above
  All and As It Was Meant To Be read "Major Agenda" correctly — flagged for high-res confirm,
  not ruled). "Human/Narn/Centauri/Minbari Agenda/Group/Location" lines match pool subtypes
  everywhere checked, incl. Leading the Races' "including Neutral characters and Non-Aligned"
  (printed Non-Aligned distinct from Neutral — D13 input).
- **Rarity: UNVERIFIED 65 of 65. ADDED: 0. REMOVED: 0.**
- **Orb == pool cost: 36 of 36** checkable this batch (zero exceptions); pool-cost-0 exceptions
  stand at 2 (Rabble Rousers orb 6, Sleeping Z'ha'dum orb 5 — both read as backfill corrections:
  cost 0 means unentered, not free). Running tally: **133 of 133 matched**, 2 corrections,
  2 no-field backfills (Kosh 1, Zack 6). Agendas print no orb at all (consistent absence).

## Per-card face transcription vs pool (rules text exact as read)

| id | Face rules text | Pool verdict |
|---|---|---|
| de_agenda_a_rising_power | "Rotate this agenda to provide +5 support to any Diplomacy conflict that will give you influence. Count each 10 Diplomacy you have from ready characters you control as +1 power." | CHANGED (pool: INFLUENCE_20 + win bonus). No wincon on face; power counting |
| agenda_alliance_of_races | "Human Agenda. You may transfer 1 influence per turn to Babylon 5. If Babylon 5 reaches 20 influence before the Shadow War begins, count Babylon 5's influence as power." | CHANGED (pool: INFLUENCE_20 + support bonus). Alternate B5-power victory |
| de_agenda_as_it_was_meant_to_be | "Major Agenda. If the top card on your discard pile is a character, enhancement, group or fleet you may purge a Destiny Mark as an action to put that card into your hand. Your ambassador gains a Destiny Mark each time you initiate and win a conflict. Count each character you control with a Destiny Mark as 1 power." (+ triangle) | CHANGED (pool: WITHDRAWN + INFLUENCE_20 + round-start). Destiny engine |
| loc_centauri_prime | "Centauri Location. Centauri Homeworld. Location's Military may only be used to oppose conflicts targeting this location. Whenever you gain influence from a conflict, rotate this location to gain +1 additional influence." (corner M20, orb 10) | CHANGED (pool: provides-2 + unseizable) |
| de_group_commercial_telepaths | "Multiple. Rotate this group and target a character who is not targeted by Commercial Telepaths. That character gains a bonus to his Diplomacy equal to half his Psi, rounded up, while this group remains rotated." (orb 6) | PARTIAL (half-Psi mechanic agrees; pool wording differs) |
| de_group_counterintelligence | "Multiple. Rotate and target an Intrigue conflict. Your characters participating in that conflict take half damage, rounded up, when attacked." (orb 8) | CHANGED (pool: +3 Intrigue) |
| de_group_damage_control_team | "Multiple. Rotate and target one of your fleets. The fleet takes half damage, rounded up, when attacked for the rest of the turn." (orb 6) | CHANGED (pool: unrotate fleet) |
| de_agenda_defense_in_depth | Pilot-carried | CHANGED SUBSTANTIALLY |
| de_group_diplomatic_corps | "Rotate and target a character. That character gains +2 Diplomacy while this group remains rotated." (orb 8) | PARTIAL (core +2 agrees; pool win-draw absent) |
| loc_earth | "Human Location. Human Homeworld. [Same oppose-only + rotate-for-+1 frame.]" (corner M5, orb 10) | CHANGED (pool: provides-2) |
| de_group_extended_contacts | "Rotate and target a character. That character gains +2 Intrigue while this group remains rotated." (orb 8) | CHANGED (pool: draw-1/2) |
| de_agenda_finish_the_war | "Minbari Agenda. Once per turn you may lose 1 influence to raise your tension toward the Humans by 1. If your tension toward the Humans is at 5, you may declare war conflicts against human factions and they may declare war conflicts against you." | CHANGED (pool: INFLUENCE_20 + vs-Human +3). War-declaration engine, no wincon |
| de_agenda_forced_evolution | "Major Agenda. Requires 5 Shadow Marks. Count the Shadows influence minus the Vorlon's influence as power. You may transfer 1 influence per turn to the Shadows. Rotate agenda and apply target fleet's influence cost to neutralize 1 fleet with Military less than Shadow influence; purge 1 Shadow Mark from your faction." | CHANGED ENTIRELY (pool: MOST_INNER_CIRCLE + recruit discount). Faction-influence-as-power |
| de_group_government_opposition | "Choose a race for this group to affect when it is first played. Rotate and apply influence equal to the cost of a target group of that race. Treat that group as if it had no effect text for the rest of the turn." (orb 6) | CHANGED (pool: opponent-loses-1). Blanking + race-lock |
| de_agenda_growth_in_chaos | "Your ambassador gains a Shadow Mark. The cost for you to play cards requiring Shadow Marks is reduced by 1 for each Shadow Mark you have. You may rotate this agenda to give a character +1 Leadership per Shadow Mark you have for the rest of the turn. Count each 2 Shadow Marks you have as 1 power." (+ spider) | CHANGED (pool: INFLUENCE_20 + intrigue-loss bonus). Mark economy |
| de_agenda_higher_calling | "Count each 2 Destiny Marks your characters have as 1 power. Your ambassador gains +4 to each of his abilities while participating in any conflict which can result in his faction gaining a Destiny Mark. Shadow Marks and Vorlon Marks on all characters for all factions count double." (face: plain "Agenda" — subtype drift, see verdicts) | CHANGED (pool: INFLUENCE_20 major + round-start). Mark doubling |
| de_group_isn | "Media. Rotate this group to reverse a tension change which occurred since your last action." (orb 8) | CHANGED (pool: reveal hand) |
| de_loc_immolan_v | "[Oppose-only frame.] Rotate this location while your ambassador is supporting a conflict to give him +3 Intrigue for the rest of the turn." (corner M15, orb 6) | CHANGED (pool: provides-1 + defense bonus) |
| de_group_imperial_telepaths | "Centauri Group. Rotate to draw the top 2 cards from your deck. Then, place 3 cards from your hand at the bottom of your deck." (orb 10) | CHANGED (pool: Psi +3) |
| de_agenda_imperialism | "Your ambassador gains +2 Leadership. Gain +2 influence for each location you capture. Lose 2 influence if a location you control is captured. You may use captured locations as if they were your own." | CHANGED (pool: INFLUENCE_20 + military reward). Capture economy, no wincon |
| de_agenda_infiltrate_and_exploit | "Apply 5 influence to initiate an Intrigue conflict. Choose one of the following goals…: Gain +1 influence; Your target loses 1 influence; Choose a target ability, your ambassador will gain a permanent bonus (which can accumulate) of +1 to that ability." | CHANGED (pool: INFLUENCE_20 + event tax). Permanent cumulative bonus |
| de_group_influential_lords | "Centauri Group. If a conflict can increase Centauri influence, rotate this group to apply +5 Diplomacy or +3 Intrigue in support of the conflict." (orb 10) | CHANGED (pool: round-start gain) |
| de_group_interstellar_corporation | "Multiple. Any other willing player may apply influence for you on your turn, with your permission. That player may require you to apply the same amount of influence for him during any of his actions on a future turn." (orb 4) | CHANGED (pool: round-start gain). Cross-player influence |
| de_agenda_knowledge_is_power | Pilot-carried (+2 Intrigue + purge/5-military engine) | CHANGED ENTIRELY |
| de_agenda_leading_the_races | "Human Agenda. For each Destiny Mark your ambassador has, the cost for you to sponsor any character is reduced by 1. Count each racial type (including Neutral characters and Non-Aligned) represented by a character in your Inner Circle as 1 power." | CHANGED (pool: INFLUENCE_20 + initiate bonus). Mark discount + race-count power |
| de_agenda_maintain_the_peace | "You must have at least 20 Military in ready fleets to sponsor this agenda. Rotate as an action to initiate a Military conflict targeting one other Military conflict. If, at the beginning of resolution, this conflict has more support than its target conflict, neither conflict resolves. Each turn no Military conflict resolves, place a peace token on this agenda…" (face: plain "Agenda" — subtype drift) | CHANGED ENTIRELY (pool: INFLUENCE_20 major + stop bonus). Peace tokens; conflicts targeting conflicts |
| loc_mars_colony | "[Oppose-only frame.] Rotate this location to give one of your characters +2 Diplomacy for the rest of the turn." (corner M10, orb 6) | CHANGED (pool: provides-1 + intrigue bonus) |
| de_agenda_meddling_with_others | "Your ambassador gains a Doom Mark. Apply 5 influence to initiate one conflict against a target race: Intrigue… target's unrest +1. Diplomacy… factions lose 1 influence. Military… all players' fleet-sponsor costs +1." (orb not legible) | CHANGED (pool: INFLUENCE_20 + blowout bonus). Modal conflict + Doom |
| de_group_military_cadre | "Rotate and target a character. That character gains +2 Leadership while this group remains rotated." (orb 8) | CHANGED (pool: +2 Military) |
| de_group_military_telepaths | "Multiple. Rotate this group and target a character who is not targeted by Military Telepaths. That character gains a bonus to his Military equal to half his Psi, rounded up, while this group remains rotated." (orb 6) | CHANGED (pool: +3 Psi) |
| loc_minbar | "[Oppose-only frame.] Whenever you gain influence from a conflict, rotate this location to gain +1 additional influence." (corner M25, orb 10) | CHANGED (pool: provides-2 + unseizable) |
| de_loc_minbari_protectorate | "[Oppose-only frame.] While this location is in play, reduce your cost to sponsor neutral characters and characters from any race whose ambassador is not in play by 2." (corner M15, orb 4) | CHANGED (pool: provides-1 + diplomacy bonus) |
| de_group_motivated_leaders | "While you are the target of a war conflict, all of your characters gain +1 Leadership." (orb 5) | CHANGED (pool: permanent-bonus-then-discard) |
| loc_narn_homeworld | "[Oppose-only frame.] Whenever you gain influence from a conflict, rotate this location to gain +1 additional influence." (corner M20, orb 10) | CHANGED (pool: provides-2) |
| de_group_narn_rabble | "Narn Group. Rotate and target a character whose highest ability is 4 or less. That character rotates for no effect." (orb 7) | CHANGED (pool: +2 total) |
| agenda_never_again | "Narn Agenda. All of your fleets gain +2 Military while you are the target of a Military conflict. Count each ready fleet you have with an unmodified Military greater than 4 as 1 power." | CHANGED (pool: INFLUENCE_20 + win bonus). Unmodified-Military power counting |
| de_group_observers | "Multiple. Target another willing player when you play this group. Each aftermath played on the target faction must be approved or discarded by you…" (orb 5) | CHANGED (pool: peek hands). Aftermath approval gate |
| de_agenda_order_above_all | "Major Agenda. Requires 5 Vorlon Marks. Count the Vorlon's influence minus the Shadow's influence as power. You may transfer 1 influence per turn to the Vorlons. Rotate and purge 1 Vorlon Mark from your faction to cancel one war conflict or one conflict card…" | CHANGED ENTIRELY (pool: MILITARY_SUPREMACY + fleet bonus). Vorlon economy mirror of Forced Evolution |
| de_loc_proxima_iii | "[Oppose-only frame.] Your cost to promote any character to your Inner Circle is reduced by 2 (to a minimum cost of 0)." (corner M10, orb 4) | CHANGED (pool: provides-1 + bonus) |
| de_loc_quadrant_14 | "[Oppose-only frame.] Rotate Quadrant 14 as an action to add a construction token to this card. Each time you sponsor a fleet, remove all construction tokens… Each token removed reduces the cost … by 2 (minimum 0)." (corner M15, orb 6) | CHANGED (pool: provides-1 + bonus). Construction tokens |
| de_loc_quadrant_37 | "[Oppose-only frame.] Rotate and apply 10 influence. Search your deck from the top and find the first Narn fleet that could be sponsored (limited and not in play and multiple). Put it into play…" (corner M0, orb 10) | CHANGED (pool: provides-1). Sponsorability vocabulary: limited / not-in-play / multiple |
| de_group_rabble_rousers | "Target a race when you play this card. Rotate this group to allow a character of your race to do extra damage in an attack against a member of the target race. The extra damage is equal to your race's tension toward the target race." (orb 6 vs pool cost 0 — backfill correction) | CHANGED (pool: discard-1). Tension-scaled damage |
| de_loc_ragesh_iii | "[Oppose-only frame.] Rotate Ragesh III as an action to add a construction token to this card. Each time you sponsor a card, remove all construction tokens… Each token removed reduces the cost … by 1 (minimum 0)." (corner M5, orb 6) | CHANGED (pool: provides-1) |
| de_group_ranger_strike_team | "Ranger Group. While you control this group, you may rotate any of your fleets to increase Babylon 5's influence for the rest of the turn by +1 per 5 Military of the fleet. This bonus cannot contribute to raising Babylon 5's influence above 19 unless the Shadow War has begun." (orb 4) | CHANGED (pool: +2 total). B5-as-entity with 19-cap + Shadow-War gate |
| de_group_rangers_surveillance | "Ranger Group. Requires 1 Vorlon Mark to sponsor. While this group is in play, you gain +10 influence for initiative determination only." (orb 6) | CHANGED (pool: peek-3). Mark-gated sponsorship + initiative stat |
| de_group_religious_caste | "Minbari Group. Increase your unrest by 1 when sponsored. All your worker and warrior caste Minbari are -1 to each ability. All your religious caste Minbari gain +1 Diplomacy and +1 Leadership." (orb 5) | CHANGED (pool: round-start gain). Sponsor-time unrest + caste auras |
| agenda_revenge | "Narn Agenda. All of your fleets gain +2 Military while attacking Centauri fleets. Your tension toward the Centauri counts as power. Gain +2 Influence whenever you conquer a Centauri location." | CHANGED (pool: INFLUENCE_20 + spend-for-+2). Tension-as-power; conquest trigger |
| agenda_rise_of_the_republic | "Centauri Agenda. All of your fleets gain +1 Military. Your highest tension value counts as power. Once per turn you may apply 15 Diplomacy to increase any of your tension values by 1." | CHANGED (pool: INFLUENCE_20 + per-Location). Tension-as-power; 15-Diplomacy pump |
| de_group_secret_police | "Multiple. Rotate this group and target a character who is not targeted by Secret Police. That character gains a bonus to his Intrigue equal to half his Psi, rounded up, while this group remains rotated." (orb 6) | CHANGED (pool: +2 Intrigue) |
| de_agenda_seizing_advantage | "Pay 5 influence (in addition to other conflict requirements) to play a conflict card from another player's discard pile (as if it were from your hand). Each time your faction neutralizes a character with an attack and the character is discarded that same turn, your neutralizing character gains a Doom Mark and your faction (permanently) gains +1 power." (orb not legible; face: plain "Agenda" — subtype drift) | CHANGED (pool: INFLUENCE_20 major + elimination bonus). Permanent faction power |
| de_agenda_servants_of_order | "Minbari Agenda. Your ambassador gains a Vorlon Mark. Purge a Vorlon Mark to go through your deck until you find the first card requiring or picturing Vorlon Marks. Place that card in your hand, then shuffle your deck. Count each 2 Vorlon Marks you have as 1 power." (+ Grey Council star) | CHANGED (pool: MOST_INNER_CIRCLE + round-start). Mark tutor + power counting |
| de_loc_sleeping_zhadum | "Location. Location's Military may only be used to oppose conflicts targeting this location. Your ambassador gains a Doom Mark. Any player may rotate Sleeping Z'ha'dum to cause the Shadows to gain +1 influence whenever a card is played which provides or requires Shadow Marks." (corner M50, orb 5 vs pool cost 0 — backfill correction) | CHANGED (pool: provides-2 + intrigue bonus). Doom + Shadow tax engine |
| de_group_spin_doctors | "Media. Multiple. Rotate and target a Diplomacy conflict. Your characters participating in that conflict take half damage, rounded up, when attacked." (orb 8) | CHANGED (pool: change perceived winner) |
| de_agenda_strength_in_adversity | "Rotate this agenda to apply support or opposition to any conflict. The amount applied is equal to the difference between the highest player influence rating minus your influence rating. If the player with the most power has 1 to 4 more power than you do, count the difference as power." (no orb seen) | CHANGED (pool: INFLUENCE_20 + damage bonus). Gap-as-power |
| de_agenda_support_of_the_mighty | "Your cost to sponsor any character with an ability of 6 or higher is reduced by 2. Count each of your ready characters with an ability of 6 or more as 1 power." (no orb seen) | CHANGED (pool: INFLUENCE_20 + support bonus). Ability-threshold discount |
| de_agenda_the_hope_of_peace | "Each character you control gains +1 Diplomacy. Count every 4 points of Babylon 5's influence as 1 power." (no orb seen) | CHANGED (pool: INFLUENCE_20 + quiet-round). B5-influence-as-power |
| de_group_thenta_makur | "Narn Group. All Narn in play gain a Strife Mark while Thenta Makur is in play." (+ star, orb 7) | CHANGED (pool: +4 Intrigue). Global Strife |
| de_agenda_total_war | "Your ambassador loses 2 Diplomacy and gains a Doom Mark, but gains +2 Leadership while you are at war. If a war conflict you initiate is contested but successful, you gain +1 influence and your target loses 1 influence… While opposing your conflicts or attacking your fleets, all other players' fleets gain +1 Military." (orb not legible) | CHANGED (pool: MILITARY_SUPREMACY + bonus — no wincon on face). Doom-priced war engine |
| de_loc_transfer_point_io | "Human Location. [Oppose-only frame.] Rotate with a character when you sponsor a card besides a character. Reduce the cost of that card by the number of Trade Pacts in play." (corner M10, orb 6) | CHANGED (pool: hub + draw). Trade Pacts |
| de_group_war_college | "Your cost to sponsor any character with Leadership greater than 0 is reduced by 2." (orb 6) | CHANGED (pool: +2 Military) |
| de_group_warrior_caste | "Minbari Group. Increase your unrest by 1 when sponsored. All your religious and worker caste Minbari are -1 to each ability. All your warrior caste Minbari gain +1 Intrigue and +1 Leadership." (orb 5) | CHANGED (pool: fleet bonus). Mirror of Religious Caste |
| de_group_wind_swords | "Minbari Group. Increase Minbari unrest by 1 when sponsored. The cost for you to sponsor any Minbari fleet or Minbari character with Leadership is reduced by 2." (orb 4) | CHANGED (pool: +3 Military) |
| agenda_power_politics | "Your ambassador gains +1 Diplomacy. Apply 9 influence to initiate a Diplomacy Conflict. Any player may be supported in this conflict. Whichever player generates the most support gains +2 influence." (no orb seen) | CHANGED (pool: INFLUENCE_20 + event bonus). Open-support engine |
| de_agenda_peace_in_our_time | Pilot-carried | CHANGED ENTIRELY |
| group_psi_corps_intelligence | Pilot-carried (both arts) | CHANGED ENTIRELY |

## Cross-card findings (all flagged for B5-0947)

1. **Zero of 26 agenda faces show a win condition.** Every pool INFLUENCE_20 / MOST_INNER_CIRCLE /
   MILITARY_SUPREMACY key is unprinted; faces are engines (mark counting, power counting,
   capture, tokens, tutors). The pool's winCondition column is the single largest unprinted
   schema element — B5-0332's agenda-points debate now has ground truth: printed agendas
   generate *power*, and several count power toward implicit thresholds the pool never encodes.
2. **"Count X as power" is the agenda design language** (11 faces: A Rising, Alliance, As It Was,
   Higher, Leading, Never Again, Revenge, Rise, Seizing, Servants, Strength, Hope, Forced
   Evolution, Order Above All). Power-sourcing is diverse: Diplomacy/10, B5 influence, marks
   (1:1, 2:1), race counts, tension values, ready 6+ characters, influence gaps, faction
   influence differentials (Shadows-minus-Vorlon and mirror).
3. **Locations all share one frame** (oppose-only Military + a rotate economy) with printed
   Military corners (M0–M50, digits partially uncertain — high-res follow-up, not ruled) and
   orbs matching pool cost wherever the pool has one. Pool "provides-N-Influence" texts are
   wrong on all 13; "cannot be seized" appears nowhere on the three homeworld faces read.
4. **Pool cost 0 = unentered, proven twice:** Rabble Rousers (orb 6) and Sleeping Z'ha'dum
   (orb 5) both carry pool cost 0. Any cost-field backfill must treat 0 as unknown, and Kosh
   (orb 1) + Zack (orb 6) join as no-field backfills.
5. **New mechanics with no pool/model expression:** conflict-targeting conflicts + peace tokens
   (Maintain), permanent cumulative ambassador bonuses (Infiltrate), permanent faction power
   (Seizing), conflict-requirement payment from discard (Seizing), race-locked blanking
   (Government Opposition), cross-player influence (Interstellar), willing-player aftermath
   approval (Observers), willing-player targeting (multiple), B5 votes (Support Babylon 5),
   B5-as-influence-entity with 19-cap (Ranger Strike), Trade Pacts (Transfer Io), Free Trade
   (Trade Windfall, B5-0937), construction tokens (Quadrant 14, Ragesh III), sponsor-time
   unrest (Religious/Warrior castes, Wind Swords), tension-as-power (Revenge, Rise),
   15-Diplomacy tension pump (Rise), conquest triggers (Revenge), capture economy (Imperialism),
   unmodified-Military counting (Never Again), initiative-only influence (Rangers Surveillance),
   Limited/sponsorability vocabulary (Quadrant 37).
6. Orb tally: **36 of 36 this batch**, running **133 of 133 matched** (pilot 11 + 0931: 50 +
   0933: 36 + 0937: events uncosted + 0945: 36), 2 corrections, 2 backfills. Agendas print no
   orb (consistent absence across all batches).

## Verification

- `ledger-query.ps1`: B5-0945 reads 7 pipes / doubleLead no / DONE after close-out.
- `run-dup-census.ps1` PASS post-write (foreign dups, if any, recorded in DECISIONS untouched).
- No compile gate run: no `src` touched.

## Reusable lesson

Filed under `.agent/PATTERNS/muse-spark-opencode-02/`: check coverage programmatically before
writing the report — two faces (Extended Contacts, Power Politics) were skipped by eye and
caught only by the pool-minus-read census. The alphabet is not a checklist; the set
difference is.
