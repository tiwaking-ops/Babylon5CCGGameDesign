---
author_llm: {name: "GitHub Copilot (Auto mode) 1701", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 1701", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# B5-1523 - deluxe-wins title dedup audit

## Method

Read the production Premiere and Deluxe card JSON through the same title-key rule implemented by `b5ccg/src/b5ccg/engine/DeckLoader.java`: start with all Deluxe records, then retain only Premiere records whose title is absent from Deluxe. This was read-only; no card JSON, deck JSON, source, or claim owned by another agent was changed. The audit was fenced against OPEN B5-1343 as requested.

## Result

* Premiere: 446 records and 446 unique titles.
* Deluxe: 383 records and 383 unique titles.
* Premiere/Deluxe overlap: 383 titles; every overlap is one record in each set.
* Deluxe-wins pool: 446 records and 446 unique titles (383 Deluxe plus 63 Premiere-only).
* Duplicate title groups: Premiere 0, Deluxe 0, resulting pool 0.
* Type/subtype/faction divergences among dropped Premiere records and surviving Deluxe twins: 0.
* Image-key mismatches across the 383 overlap titles: 0.

The required 383 Deluxe plus 63 Premiere-only membership and 446-title total are exact.

## Every dropped Premiere record and Deluxe survivor

| Title | Dropped Premiere ID | Winning Deluxe ID |
|---|---|---|
| A Brighter Future | conf_a_brighter_future | de_conf_a_brighter_future |
| A Good Bluff | event_a_good_bluff | de_event_a_good_bluff |
| A Rising Power | agenda_a_rising_power | de_agenda_a_rising_power |
| Accident | event_accident | de_event_accident |
| Adira Tyree | char_adira_tyree | de_char_adira_tyree |
| Affirmation of Power | conf_affirmation_of_power | de_conf_affirmation_of_power |
| Alliance | conf_alliance | de_conf_alliance |
| Approval of the Grey | aftermath_approval_of_the_grey | de_am_approval_of_the_grey |
| Armed Resistance | enh_armed_resistance | de_enh_armed_resistance |
| Armistice | event_armistice | de_event_armistice |
| As It Was Meant To Be | agenda_as_it_was_meant_to_be | de_agenda_as_it_was_meant_to_be |
| Assault Troops | enh_assault_troops | de_enh_assault_troops |
| Attacking Pawns | conf_attacking_pawns | de_conf_attacking_pawns |
| Avert Incident | event_avert_incident | de_event_avert_incident |
| Babylon 5 Unrest | enh_babylon5_unrest | de_enh_babylon5_unrest |
| Backroom Dealing | enh_backroom_dealing | de_enh_backroom_dealing |
| Balance | event_balance | de_event_balance |
| Battle Tested | aftermath_battle_tested | de_am_battle_tested |
| Bester | char_bester | de_char_bester |
| Bio-Weapon Discovery | conf_bio_weapon_discovery | de_conf_bio_weapon_discovery |
| Black Market | conf_black_market | de_conf_black_market |
| Blockade | conf_blockade | de_conf_blockade |
| Blood Oath | aftermath_blood_oath | de_am_blood_oath |
| Border Raid | conf_border_raid | de_conf_border_raid |
| Campaign for Support | conf_campaign_for_support | de_conf_campaign_for_support |
| Carpe Diem | event_carpe_diem | de_event_carpe_diem |
| Casualty Reports | aftermath_casualty_reports | de_am_casualty_reports |
| Catherine Sakai | char_catherine_sakai | de_char_catherine_sakai |
| Censure | enh_censure | de_enh_censure |
| Centauri Agent | char_centauri_agent | de_char_centauri_agent |
| Centauri Aide | char_centauri_aide | de_char_centauri_aide |
| Centauri Captain | char_centauri_captain | de_char_centauri_captain |
| Centauri Telepath | char_centauri_telepath | de_char_centauri_telepath |
| Change of Plans | event_change_of_plans | de_event_change_of_plans |
| Changing Opinion | event_changing_opinion | de_event_changing_opinion |
| Chaos Reigns | event_chaos_reigns | de_event_chaos_reigns |
| Chrysalis | event_chrysalis | de_event_chrysalis |
| Colonial Fleet (Centauri) | fleet_colonial_centauri | de_fleet_colonial_centauri |
| Colonial Fleet (Human) | fleet_colonial_human | de_fleet_colonial_human |
| Colonial Fleet (Minbari) | fleet_colonial_minbari | de_fleet_colonial_minbari |
| Colonial Fleet (Narn) | fleet_colonial_narn | de_fleet_colonial_narn |
| Combat Experience | aftermath_combat_experience | de_am_combat_experience |
| Commerce Raiding | enh_commerce_raiding | de_enh_commerce_raiding |
| Commercial Telepaths | group_commercial_telepaths | de_group_commercial_telepaths |
| Compatible Goals | conf_compatible_goals | de_conf_compatible_goals |
| Competing Interests | event_competing_interests | de_event_competing_interests |
| Complete Support | conf_complete_support | de_conf_complete_support |
| Concealed Weapon | enh_concealed_weapon | de_enh_concealed_weapon |
| Concentrated Effort | event_concentrated_effort | de_event_concentrated_effort |
| Condemn Deportations | conf_condemn_deportations | de_conf_condemn_deportations |
| Conflicting Desires | event_conflicting_desires | de_event_conflicting_desires |
| Conflicting Loyalties | event_conflicting_loyalties | de_event_conflicting_loyalties |
| Confusion in Chaos | event_confusion_in_chaos | de_event_confusion_in_chaos |
| Consolidated Position | conf_consolidated_position | de_conf_consolidated_position |
| Contact with Shadows | event_contact_with_shadows | de_event_contact_with_shadows |
| Contact with Vorlons | event_contact_with_vorlons | de_event_contact_with_vorlons |
| Coordinated Fire | event_coordinated_fire | de_event_coordinated_fire |
| Counterintelligence | group_counterintelligence | de_group_counterintelligence |
| Court the Rebellious | conf_court_the_rebellious | de_conf_court_the_rebellious |
| Covert Allies | enh_covert_allies | de_enh_covert_allies |
| Crisis of Self | aftermath_crisis_of_self | de_am_crisis_of_self |
| Crusade | conf_crusade | de_conf_crusade |
| Cut Supply Lines | event_cut_supply_lines | de_event_cut_supply_lines |
| Cynthia Torqueman | char_cynthia_torqueman | de_char_cynthia_torqueman |
| Damage Control Team | group_damage_control_team | de_group_damage_control_team |
| Dan Randall | char_dan_randall | de_char_dan_randall |
| Declaration of War | event_declaration_of_war | de_event_declaration_of_war |
| Deep Space Fleet (Centauri) | fleet_deep_space_centauri | de_fleet_deep_space_centauri |
| Deep Space Fleet (Human) | fleet_deep_space_human | de_fleet_deep_space_human |
| Deep Space Fleet (Minbari) | fleet_deep_space_minbari | de_fleet_deep_space_minbari |
| Deep Space Fleet (Narn) | fleet_deep_space_narn | de_fleet_deep_space_narn |
| Defame Ambassador | event_defame_ambassador | de_event_defame_ambassador |
| Defense in Depth | agenda_defense_in_depth | de_agenda_defense_in_depth |
| Delenn | char_delenn | de_char_delenn |
| Delenn Transformed | char_delenn_transformed | de_char_delenn_transformed |
| Demonstrative Victory | conf_demonstrative_victory | de_conf_demonstrative_victory |
| Despair | aftermath_despair | de_am_despair |
| Destiny Fulfilled | event_destiny_fulfilled | de_event_destiny_fulfilled |
| Develop Relationship | aftermath_develop_relationship | de_am_develop_relationship |
| Dhaliri | char_dhaliri | de_char_dhaliri |
| Diplomatic Advantage | aftermath_diplomatic_advantage | de_am_diplomatic_advantage |
| Diplomatic Blunder | event_diplomatic_blunder | de_event_diplomatic_blunder |
| Diplomatic Corps | group_diplomatic_corps | de_group_diplomatic_corps |
| Diplomatic Immunity | event_diplomatic_immunity | de_event_diplomatic_immunity |
| Disaffected Centauri | char_disaffected_centauri | de_char_disaffected_centauri |
| Disaffected Human | char_disaffected_human | de_char_disaffected_human |
| Disaffected Minbari | char_disaffected_minbari | de_char_disaffected_minbari |
| Disaffected Narn | char_disaffected_narn | de_char_disaffected_narn |
| Disenchantment | aftermath_disenchantment | de_am_disenchantment |
| Disgrace | aftermath_disgrace | de_am_disgrace |
| Draft | enh_draft | de_enh_draft |
| Drazi Sunhawk | fleet_drazi_sunhawk | de_fleet_drazi_sunhawk |
| Du'Nar | char_dunar | de_char_dunar |
| Durlan | char_durlan | de_char_durlan |
| Du'Rog | char_durog | de_char_durog |
| Early Warning | event_early_warning | de_event_early_warning |
| Elric | char_elric | de_char_elric |
| Emergency Military Aid | event_emergency_military_aid | de_event_emergency_military_aid |
| Emperor Turhan | char_emperor_turhan | de_char_emperor_turhan |
| Energy Mines | enh_energy_mines | de_enh_energy_mines |
| Enrage | aftermath_enrage | de_am_enrage |
| Establish Base | conf_establish_base | de_conf_establish_base |
| Exploit Opportunities | aftermath_exploit_opportunities | de_am_exploit_opportunities |
| Exploitation | enh_exploitation | de_enh_exploitation |
| Extended Contacts | group_extended_contacts | de_group_extended_contacts |
| Extreme Sanction | conf_extreme_sanction | de_conf_extreme_sanction |
| Finish the War | agenda_finish_the_war | de_agenda_finish_the_war |
| First Battle Fleet (Centauri) | fleet_first_battle_centauri | de_fleet_first_battle_centauri |
| First Battle Fleet (Human) | fleet_first_battle_human | de_fleet_first_battle_human |
| First Battle Fleet (Minbari) | fleet_first_battle_minbari | de_fleet_first_battle_minbari |
| First Battle Fleet (Narn) | fleet_first_battle_narn | de_fleet_first_battle_narn |
| Fixed in Their Ways | enh_fixed_in_their_ways | de_enh_fixed_in_their_ways |
| Fleet of the Line | fleet_fleet_of_the_line | de_fleet_fleet_of_the_line |
| Fleet Support Base | enh_fleet_support_base | de_enh_fleet_support_base |
| Fleets on the Border | event_fleets_on_the_border | de_event_fleets_on_the_border |
| Focus Your Efforts | aftermath_focus_your_efforts | de_am_focus_your_efforts |
| For My People | event_for_my_people | de_event_for_my_people |
| For the Common Good | event_for_the_common_good | de_event_for_the_common_good |
| For the Good of All | event_for_the_good_of_all | de_event_for_the_good_of_all |
| Forced Commitment | enh_forced_commitment | de_enh_forced_commitment |
| Forced Evolution | agenda_forced_evolution | de_agenda_forced_evolution |
| Forces Collide | event_forces_collide | de_event_forces_collide |
| Frederick Lantz | char_frederick_lantz | de_char_frederick_lantz |
| Garrison Fleet | fleet_garrison_centauri | de_fleet_garrison_centauri |
| G'Drog | char_gdrog | de_char_gdrog |
| General Franklin | char_general_franklin | de_char_general_franklin |
| General Hague | char_general_hague | de_char_general_hague |
| G'Kar | char_gkar | de_char_gkar |
| Glory | aftermath_glory | de_am_glory |
| Government Opposition | group_government_opposition | de_group_government_opposition |
| Grey Council Fleet | fleet_grey_council_fleet | de_fleet_grey_council_fleet |
| Grievance | aftermath_grievance | de_am_grievance |
| Growth in Chaos | agenda_growth_in_chaos | de_agenda_growth_in_chaos |
| G'Sten | char_gsten | de_char_gsten |
| Guilt | aftermath_guilt | de_am_guilt |
| Gunboat Diplomacy | conf_gunboat_diplomacy | de_conf_gunboat_diplomacy |
| Harvest Souls | aftermath_harvest_souls | de_am_harvest_souls |
| Hate Crime | conf_hate_crime | de_conf_hate_crime |
| Heavy Fleet | fleet_heavy_fleet | de_fleet_heavy_fleet |
| Hidden Agent | aftermath_hidden_agent | de_am_hidden_agent |
| Hidden Knowledge | event_hidden_knowledge | de_event_hidden_knowledge |
| Higher Calling | agenda_higher_calling | de_agenda_higher_calling |
| Hire Raiders | event_hire_raiders | de_event_hire_raiders |
| Hour of the Wolf | event_hour_of_the_wolf | de_event_hour_of_the_wolf |
| Human Agent | char_human_agent | de_char_human_agent |
| Human Aide | char_human_aide | de_char_human_aide |
| Human Captain | char_human_captain | de_char_human_captain |
| Humanitarian Aid | conf_humanitarian_aid | de_conf_humanitarian_aid |
| Hunted | aftermath_hunted | de_am_hunted |
| Hunter, Prey | conf_hunter_prey | de_conf_hunter_prey |
| Immolan V | loc_immolan_v | de_loc_immolan_v |
| Imperial Telepaths | group_imperial_telepaths | de_group_imperial_telepaths |
| Imperialism | agenda_imperialism | de_agenda_imperialism |
| In the Line of Duty | aftermath_in_the_line_of_duty | de_am_in_the_line_of_duty |
| Inevitable Destiny | aftermath_inevitable_destiny | de_am_inevitable_destiny |
| Infiltrate and Exploit | agenda_infiltrate_and_exploit | de_agenda_infiltrate_and_exploit |
| Influential Lords | group_influential_lords | de_group_influential_lords |
| Internal Strife | event_internal_strife | de_event_internal_strife |
| Interstellar Corporation | group_interstellar_corporation | de_group_interstellar_corporation |
| Intolerable Interference | aftermath_intolerable_interference | de_am_intolerable_interference |
| Intrigues Mature | event_intrigues_mature | de_event_intrigues_mature |
| Ipsha Battleglobe | fleet_ipsha_battleglobe | de_fleet_ipsha_battleglobe |
| ISN | group_isn | de_group_isn |
| Isolated | enh_isolated | de_enh_isolated |
| Isolationism | enh_isolationism | de_enh_isolationism |
| It Will Be His Undoing | aftermath_it_will_be_his_undoing | de_am_it_will_be_his_undoing |
| Ja'Doc | char_jadoc | de_char_jadoc |
| Jeffrey Sinclair | char_jeffrey_sinclair | de_char_jeffrey_sinclair |
| John Sheridan | char_john_sheridan | de_char_john_sheridan |
| Judgment by Success | enh_judgment_by_success | de_enh_judgment_by_success |
| Kha'Mak | char_khamak | de_char_khamak |
| Knowledge is Power | agenda_knowledge_is_power | de_agenda_knowledge_is_power |
| Knowledge of Shadows | event_knowledge_of_shadows | de_event_knowledge_of_shadows |
| Knowledge of the Soul | event_knowledge_of_the_soul | de_event_knowledge_of_the_soul |
| Kosh Naranek | char_kosh_naranek | de_char_kosh_naranek |
| Lack of Subtlety | event_lack_of_subtlety | de_event_lack_of_subtlety |
| Lady Ladira | char_lady_ladira | de_char_lady_ladira |
| Lamentations | aftermath_lamentations | de_am_lamentations |
| Latent Telepath | enh_latent_telepath | de_enh_latent_telepath |
| Leading the Races | agenda_leading_the_races | de_agenda_leading_the_races |
| Learning Experience | aftermath_learning_experience | de_am_learning_experience |
| Left Vulnerable | aftermath_left_vulnerable | de_am_left_vulnerable |
| Liquidating Assets | event_liquidating_assets | de_event_liquidating_assets |
| Lockdown | event_lockdown | de_event_lockdown |
| Londo Mollari | char_londo_mollari | de_char_londo_mollari |
| Long Term Investment | event_long_term_investment | de_event_long_term_investment |
| Lord Kiro | char_lord_kiro | de_char_lord_kiro |
| Lord Refa | char_lord_refa | de_char_lord_refa |
| Lord Valo | char_lord_valo | de_char_lord_valo |
| Loss of Face | aftermath_loss_of_face | de_am_loss_of_face |
| Loss of Support | conf_loss_of_support | de_conf_loss_of_support |
| Lovell | char_lovell | de_char_lovell |
| Luis Santiago | char_luis_santiago | de_char_luis_santiago |
| Luxuries of Homeworld | enh_luxuries_of_homeworld | de_enh_luxuries_of_homeworld |
| Lyndisty | char_lyndisty | de_char_lyndisty |
| Maintain the Peace | agenda_maintain_the_peace | de_agenda_maintain_the_peace |
| Marcus Cole | char_marcus_cole | de_char_marcus_cole |
| Markab Fleet | fleet_markab_fleet | de_fleet_markab_fleet |
| Martyr | aftermath_martyr | de_am_martyr |
| Mary Ann Cramer | char_mary_ann_cramer | de_char_mary_ann_cramer |
| Mass Drivers | enh_mass_drivers | de_enh_mass_drivers |
| Meddling with Others | agenda_meddling_with_others | de_agenda_meddling_with_others |
| Medical Assistance | event_medical_assistance | de_event_medical_assistance |
| Meditation | event_meditation | de_event_meditation |
| Merchandising B5 | event_merchandising_b5 | de_event_merchandising_b5 |
| Military Cadre | group_military_cadre | de_group_military_cadre |
| Military Telepaths | group_military_telepaths | de_group_military_telepaths |
| Minbari Agent | char_minbari_agent | de_char_minbari_agent |
| Minbari Aide | char_minbari_aide | de_char_minbari_aide |
| Minbari Captain | char_minbari_captain | de_char_minbari_captain |
| Minbari Protectorate | loc_minbari_protectorate | de_loc_minbari_protectorate |
| Mines | enh_mines | de_enh_mines |
| Minister Malachi | char_minister_malachi | de_char_minister_malachi |
| Moral Quandary | event_moral_quandary | de_event_moral_quandary |
| Morden | char_morden | de_char_morden |
| Motivated Leaders | group_motivated_leaders | de_group_motivated_leaders |
| Mr. Adams | char_mr_adams | de_char_mr_adams |
| Muddy the Waters | conf_muddy_the_waters | de_conf_muddy_the_waters |
| Na'Far | char_nafar | de_char_nafar |
| Na'Ka'Leen Feeder | conf_nakalen_feeder | de_conf_nakalen_feeder |
| Narn Agent | char_narn_agent | de_char_narn_agent |
| Narn Aide | char_narn_aide | de_char_narn_aide |
| Narn Captain | char_narn_captain | de_char_narn_captain |
| Narn Rabble | group_narn_rabble | de_group_narn_rabble |
| Na'Toth | char_natoth | de_char_natoth |
| Negotiated Surrender | aftermath_negotiated_surrender | de_am_negotiated_surrender |
| Neroon | char_neroon | de_char_neroon |
| Neutrality Treaty | conf_neutrality_treaty | de_conf_neutrality_treaty |
| News of Defeat | aftermath_news_of_defeat | de_am_news_of_defeat |
| News of Galactic Import | event_news_of_galactic_import | de_event_news_of_galactic_import |
| Nightmares | aftermath_nightmares | de_am_nightmares |
| No Escape | aftermath_no_escape | de_am_no_escape |
| Non-Aggression Pact | conf_non_aggression_pact | de_conf_non_aggression_pact |
| Non-Aligned Support | conf_non_aligned_support | de_conf_non_aligned_support |
| Not Meant to Be | event_not_meant_to_be | de_event_not_meant_to_be |
| Observers | group_observers | de_group_observers |
| Older but Wiser | aftermath_older_but_wiser | de_am_older_but_wiser |
| Order Above All | agenda_order_above_all | de_agenda_order_above_all |
| Overworked | enh_overworked | de_enh_overworked |
| Parliament of Dreams | conf_parliament_of_dreams | de_conf_parliament_of_dreams |
| Paying for Sins | aftermath_paying_for_sins | de_am_paying_for_sins |
| Peace In Our Time | agenda_peace_in_our_time | de_agenda_peace_in_our_time |
| Peacekeeping | conf_peacekeeping | de_conf_peacekeeping |
| Personal Enemies | aftermath_personal_enemies | de_am_personal_enemies |
| Personal Involvement | aftermath_personal_involvement | de_am_personal_involvement |
| Personal Protection | enh_personal_protection | de_enh_personal_protection |
| Personal Sacrifice | aftermath_personal_sacrifice | de_am_personal_sacrifice |
| Picket Fleet (Centauri) | fleet_picket_centauri | de_fleet_picket_centauri |
| Picket Fleet (Human) | fleet_picket_human | de_fleet_picket_human |
| Picket Fleet (Minbari) | fleet_picket_minbari | de_fleet_picket_minbari |
| Picket Fleet (Narn) | fleet_picket_narn | de_fleet_picket_narn |
| Planetary Defenses | enh_planetary_defenses | de_enh_planetary_defenses |
| Political Realignment | event_political_realignment | de_event_political_realignment |
| Power Posturing | enh_power_posturing | de_enh_power_posturing |
| Precision Strike | conf_precision_strike | de_conf_precision_strike |
| Prolonged Talks | event_prolonged_talks | de_event_prolonged_talks |
| Prophecy | enh_prophecy | de_enh_prophecy |
| Protests | aftermath_protests | de_am_protests |
| Proxima III | loc_proxima_iii | de_loc_proxima_iii |
| Psi Attack | conf_psi_attack | de_conf_psi_attack |
| Psi Bodyguard | enh_psi_bodyguard | de_enh_psi_bodyguard |
| Psi Interrogation | conf_psi_interrogation | de_conf_psi_interrogation |
| Public Apology | aftermath_public_apology | de_am_public_apology |
| Pulling Strings | enh_pulling_strings | de_enh_pulling_strings |
| Purge the Disloyal | conf_purge_the_disloyal | de_conf_purge_the_disloyal |
| Quadrant 14 | loc_quadrant_14 | de_loc_quadrant_14 |
| Quadrant 37 | loc_quadrant_37 | de_loc_quadrant_37 |
| Rabble Rousers | group_rabble_rousers | de_group_rabble_rousers |
| Racial Hatred | aftermath_racial_hatred | de_am_racial_hatred |
| Ragesh III | loc_ragesh_iii | de_loc_ragesh_iii |
| Raid Shipping | conf_raid_shipping | de_conf_raid_shipping |
| Rally the People | conf_rally_the_people | de_conf_rally_the_people |
| Rally to the Cause | event_rally_to_the_cause | de_event_rally_to_the_cause |
| Ramming | event_ramming | de_event_ramming |
| Ranger Strike Team | group_ranger_strike_team | de_group_ranger_strike_team |
| Rangers Surveillance | group_rangers_surveillance | de_group_rangers_surveillance |
| Rathenn | char_rathenn | de_char_rathenn |
| Recalled to Service | event_recalled_to_service | de_event_recalled_to_service |
| Refugees | aftermath_refugees | de_am_refugees |
| Religious Caste | group_religious_caste | de_group_religious_caste |
| Renowned Victory | aftermath_renowned_victory | de_am_renowned_victory |
| Repairing the Past | aftermath_repairing_the_past | de_am_repairing_the_past |
| Rescue | aftermath_rescue | de_am_rescue |
| Reserve Fleet | fleet_reserve_human | de_fleet_reserve_human |
| Retribution | aftermath_retribution | de_am_retribution |
| Reverse Advances | aftermath_reverse_advances | de_am_reverse_advances |
| Rise to Power | aftermath_rise_to_power | de_am_rise_to_power |
| Rivalry | aftermath_rivalry | de_am_rivalry |
| Rogue Soul Hunter | char_rogue_soul_hunter | de_char_rogue_soul_hunter |
| Saber Rattling | conf_saber_rattling | de_conf_saber_rattling |
| Sabotage | conf_sabotage | de_conf_sabotage |
| Salvage Yard | enh_salvage_yard | de_enh_salvage_yard |
| Sanctions | event_sanctions | de_event_sanctions |
| Sandra Hiroshi | char_sandra_hiroshi | de_char_sandra_hiroshi |
| Sarah | char_sarah | de_char_sarah |
| Second Battle Fleet (Centauri) | fleet_second_battle_centauri | de_fleet_second_battle_centauri |
| Second Battle Fleet (Human) | fleet_second_battle_human | de_fleet_second_battle_human |
| Second Battle Fleet (Minbari) | fleet_second_battle_minbari | de_fleet_second_battle_minbari |
| Second Battle Fleet (Narn) | fleet_second_battle_narn | de_fleet_second_battle_narn |
| Secondary Control | enh_secondary_control | de_enh_secondary_control |
| Secondary Experience | aftermath_secondary_experience | de_am_secondary_experience |
| Secret Police | group_secret_police | de_group_secret_police |
| Secret Strike | event_secret_strike | de_event_secret_strike |
| Secret Vorlon Aid | event_secret_vorlon_aid | de_event_secret_vorlon_aid |
| Security Training | enh_security_training | de_enh_security_training |
| Seduction | event_seduction | de_event_seduction |
| Seizing Advantage | agenda_seizing_advantage | de_agenda_seizing_advantage |
| Self Doubt | event_self_doubt | de_event_self_doubt |
| Senator Voudreau | char_senator_voudreau | de_char_senator_voudreau |
| Servants of Order | agenda_servants_of_order | de_agenda_servants_of_order |
| Shadow Assault | conf_shadow_assault | de_conf_shadow_assault |
| Shadow Strike | event_shadow_strike | de_event_shadow_strike |
| Shakat | char_shakat | de_char_shakat |
| Shal Mayan | char_shal_mayan | de_char_shal_mayan |
| Short Term Goals | event_short_term_goals | de_event_short_term_goals |
| Short Term Investment | event_short_term_investment | de_event_short_term_investment |
| Shunned | enh_shunned | de_enh_shunned |
| Skeletons in the Closet | aftermath_skeletons_in_the_closet | de_am_skeletons_in_the_closet |
| Sleeper Personality | conf_sleeper_personality | de_conf_sleeper_personality |
| Sleeping Z'ha'dum | loc_sleeping_zhadum | de_loc_sleeping_zhadum |
| Sneak Attack | event_sneak_attack | de_event_sneak_attack |
| Sortie | event_sortie | de_event_sortie |
| Soul Hunter | char_soul_hunter | de_char_soul_hunter |
| Special Ops | event_special_ops | de_event_special_ops |
| Spin Doctors | group_spin_doctors | de_group_spin_doctors |
| Sponsor Rebels | conf_sponsor_rebels | de_conf_sponsor_rebels |
| Stealth Technology | enh_stealth_technology | de_enh_stealth_technology |
| Stop Hostilities | conf_stop_hostilities | de_conf_stop_hostilities |
| Strafing Run | event_strafing_run | de_event_strafing_run |
| Strategic Reassignment | event_strategic_reassignment | de_event_strategic_reassignment |
| Strength in Adversity | agenda_strength_in_adversity | de_agenda_strength_in_adversity |
| Strike Fleet | fleet_strike_fleet | de_fleet_strike_fleet |
| Subliminal Influence | event_subliminal_influence | de_event_subliminal_influence |
| Successful Manipulation | aftermath_successful_manipulation | de_am_successful_manipulation |
| Support Babylon 5 | event_support_babylon5 | de_event_support_babylon5 |
| Support of the Mighty | agenda_support_of_the_mighty | de_agenda_support_of_the_mighty |
| Technological Espionage | conf_technological_espionage | de_conf_technological_espionage |
| Telepathic Scan | conf_telepathic_scan | de_conf_telepathic_scan |
| Temptations | conf_temptations | de_conf_temptations |
| Terrorist Bombings | conf_terrorist_bombings | de_conf_terrorist_bombings |
| Test Their Mettle | conf_test_their_mettle | de_conf_test_their_mettle |
| The Hope of Peace | agenda_the_hope_of_peace | de_agenda_the_hope_of_peace |
| The Opposition Rises | event_the_opposition_rises | de_event_the_opposition_rises |
| The Price of Power | event_the_price_of_power | de_event_the_price_of_power |
| Thenta Makur | group_thenta_makur | de_group_thenta_makur |
| Third Battle Fleet (Centauri) | fleet_third_battle_centauri | de_fleet_third_battle_centauri |
| Third Battle Fleet (Minbari) | fleet_third_battle_minbari | de_fleet_third_battle_minbari |
| Total War | agenda_total_war | de_agenda_total_war |
| Trade Pact | conf_trade_pact | de_conf_trade_pact |
| Trade Windfall | event_trade_windfall | de_event_trade_windfall |
| Transfer Point Io | loc_transfer_point_io | de_loc_transfer_point_io |
| Triluminary | enh_triluminary | de_enh_triluminary |
| Tu'Pari | char_tupari | de_char_tupari |
| Under Pressure | aftermath_under_pressure | de_am_under_pressure |
| United Front | aftermath_united_front | de_am_united_front |
| Universe Today Feature | event_universe_today_feature | de_event_universe_today_feature |
| Unrecognized Data | event_unrecognized_data | de_event_unrecognized_data |
| Utility Fleet (Centauri) | fleet_utility_centauri | de_fleet_utility_centauri |
| Utility Fleet (Human) | fleet_utility_human | de_fleet_utility_human |
| Utility Fleet (Narn) | fleet_utility_narn | de_fleet_utility_narn |
| Vendetta | aftermath_vendetta | de_am_vendetta |
| Victory in My Grasp | event_victory_in_my_grasp | de_event_victory_in_my_grasp |
| Vital Interests | enh_vital_interests | de_enh_vital_interests |
| Vorlon Enhancement | enh_vorlon_enhancement | de_enh_vorlon_enhancement |
| Vorlon Rescue | event_vorlon_rescue | de_event_vorlon_rescue |
| Vree Saucers | fleet_vree_saucers | de_fleet_vree_saucers |
| War by Popular Decree | event_war_by_popular_decree | de_event_war_by_popular_decree |
| War College | group_war_college | de_group_war_college |
| War Hero | aftermath_war_hero | de_am_war_hero |
| Warleader Shakiri | char_warleader_shakiri | de_char_warleader_shakiri |
| Warleader's Fleet | fleet_warleaders_fleet | de_fleet_warleaders_fleet |
| Warren Keffer | char_warren_keffer | de_char_warren_keffer |
| Warrior Caste | group_warrior_caste | de_group_warrior_caste |
| Wear and Tear | aftermath_wear_and_tear | de_am_wear_and_tear |
| What Do You Want? | event_what_do_you_want | de_event_what_do_you_want |
| Who Are You? | event_who_are_you | de_event_who_are_you |
| Wind Swords | group_wind_swords | de_group_wind_swords |
| Witness Protection | conf_witness_protection | de_conf_witness_protection |
| Working Relationship | enh_working_relationship | de_enh_working_relationship |
| Wounded | aftermath_wounded | de_am_wounded |
| You Are Not Ready | event_you_are_not_ready | de_event_you_are_not_ready |
| You Know My Reputation | event_you_know_my_reputation | de_event_you_know_my_reputation |
| Zack Allan | char_zack_allen | de_char_zack_allen |

## Loader contract check

`DeckLoader.loadBothSets()` uses the same ordered construction: all Deluxe cards first, followed by Premiere cards only when `hasTitle(deluxe, title)` is false. The live data counts therefore match the loader contract: 383 Deluxe records + 63 non-overlapping Premiere records = 446 records, with no duplicate titles.

**Reusable lesson:** A title-dedup audit is complete only when the aggregate membership, zero-duplicate invariant, and every dropped-to-winning ID pair agree with the loader rule; a matching total alone cannot prove the right records survived.
