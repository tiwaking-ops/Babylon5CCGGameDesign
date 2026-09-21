---
document:
  title: "B5 CCG Premier Edition starter deck fixed lists (sourced advisory)"
  version: "1.0"
  status: "Advisory working document (not canonical, not a ruling)"
provenance:
  author_llm: {name: "Mike Prasek (fan compilation, archived)", version: "unknown"}
  assessor_llm:
    - {name: "big-pickle", version: "opencode/big-pickle"}
  last_modified_by_llm: {name: "big-pickle", version: "opencode/big-pickle"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# B5 CCG Premier Edition starter deck fixed lists (sourced advisory)

**Purpose and Origin.** The canonical rulebook (`BABYLON5_CCG_RULEBOOK.md`, "Starter
Decks") states each race-specific Premier starter deck contains **50 fixed common cards
tailored to that race + 10 random uncommons/rares**. A contemporaneous fan compilation by
**Mike Prasek** (Houston B5 CCG site, archived by the Wayback Machine, capture 2019-08-05)
provides the actual 50-card fixed list for each of the four races. This document records
those lists as **advisory material only** - not canonical, not a ruling, no task/DEC
inferred. Retrieved and verified against the repo card dataset by big-pickle
(`opencode/big-pickle`) on 2026-09-21.

**Sources** (Wayback Machine raw text):

- Human: `https://web.archive.org/web/2019id_/http://www.angelfire.com/tx/mprasek/Humanf.txt`
- Centauri: `https://web.archive.org/web/2019id_/http://www.angelfire.com/tx/mprasek/centfix.txt`
- Minbari: `https://web.archive.org/web/2019id_/http://www.angelfire.com/tx/mprasek/Minbarf.txt`
- Narn: `https://web.archive.org/web/2019id_/http://www.angelfire.com/tx/mprasek/Narnfix.txt`
- Index page: `https://web.archive.org/web/20190805230922/http://www.angelfire.com/tx/mprasek/fixed.html`

**Verification against `b5ccg/resources/cards/premiere.json` (146 `FIXED` cards):**

- Each list sums to exactly **50** cards (counts greater than 1 where the source shows them).
- **0 type mismatches** across all four decks.
- Every card title resolves to a dataset card. The only title variance is the source
  "Level the Playing Field" vs the dataset "Level the Playing Field 3+9"
  (`event_level_the_playing_field`, EVENT, FIXED) - same card; the dataset title carries a suffix.
- Each deck includes its race Ambassador among the 50 (Sinclair / Londo / Delenn / G Kar),
  matching the rulebook.

This is **not** the same as the earlier Perplexity report
(`investigations/b5-starter-deck-card-lists-research-2026-09-21.md`), whose per-deck
checkmarks were a mechanical "race-fixed + ANY + NEUTRAL" derivation of 81/79/81/82 cards -
contradicted by these real 50-card lists.

**Note on counts.** Race-specific cards with a count greater than 1 (e.g. Level the Playing
Field x3, Centauri Agent x2, Contact with Vorlons x2) are reported exactly as in the source.
The Narn list contains several x2 entries (Declaration of War, Deep Space Fleet, Limited Strike).

## Earth Alliance / Human - 50 cards

| Card | Type | Use | Count |
| :-- | :-- | :-- | :-- |
| Affirm Alliance | Event |  | 1 |
| Affirmation of Peace | Conflict | Diplomacy | 1 |
| Affirmation of Power | Conflict | Diplomacy | 1 |
| Alliance of Races | Agenda | Human | 1 |
| Assigning Blame | Aftermath |  | 1 |
| Border Raid | Conflict | Military | 1 |
| Colonial Fleet | Fleet | Human | 1 |
| Decisive Tactics | Event |  | 1 |
| Declaration of War | Event |  | 1 |
| Deep Space Fleet | Fleet | Human | 1 |
| Earth | Location | Human | 1 |
| Expeditionary Fleet | Fleet | Human | 1 |
| First Battle Fleet | Fleet | Human | 1 |
| Frederick Lantz | Character | Human | 1 |
| General Hague | Character | Human | 1 |
| Homeworld Fleet | Fleet | Human | 1 |
| Human Agent | Character | Human | 1 |
| Human Aide | Character | Human | 1 |
| Human Captain | Character | Human | 1 |
| Jeffrey Sinclair | Character | Human | 1 |
| Kidnapping | Conflict | Intrigue | 1 |
| Level the Playing Field | Event |  | 3 |
| Limited Strike | Conflict | Military | 1 |
| Lyta Alexander | Character | Neutral | 1 |
| Mars Colony | Location | Human | 1 |
| Miagi Hidoshi | Character | Human | 1 |
| Michael Garibaldi | Character | Human | 1 |
| Peace In Our Time | Agenda |  | 1 |
| Picket Fleet (Human) | Fleet | Human | 1 |
| Popular Support | Event |  | 1 |
| Power Politics | Agenda |  | 1 |
| Psi Bodyguard | Enhance | Character | 1 |
| Psi Corps Intelligence | Group | Human | 1 |
| Refugees | Aftermath |  | 1 |
| Repairing the Past | Aftermath |  | 1 |
| Second Battle Fleet | Fleet | Human | 1 |
| Secondary Experience | Aftermath |  | 1 |
| Sleeper Personality | Conflict |  | 1 |
| Stephen Franklin | Character | Human | 1 |
| Stop Hostilities | Conflict | Diplomacy | 1 |
| Support Babylon 5 | Event |  | 1 |
| Susan Ivanova | Character | Human | 1 |
| Talia Winters | Character | Human | 1 |
| Telepathic Scan | Conflict | Psi | 1 |
| Test Their Mettle | Conflict | Diplomacy | 1 |
| Trade Pact | Conflict | Diplomacy | 1 |
| Underworld Connections | Event |  | 1 |
| Upgraded Defenses | Enhance | Babylon 5 | 1 |

## Centauri Republic - 50 cards

| Card | Type | Use | Count |
| :-- | :-- | :-- | :-- |
| Adira Tyree | Character | Centauri | 1 |
| Affirm Alliance | Event |  | 1 |
| Balance | Event |  | 1 |
| Border Raid | Conflict | Military | 1 |
| Carn Mollari | Character | Centauri | 1 |
| Centauri Agent | Character | Centauri | 2 |
| Centauri Aide | Character | Centauri | 1 |
| Centauri Captain | Character | Centarui | 1 |
| Centauri Prime | Location | Centauri | 1 |
| Colonial Fleet | Fleet | Centauri | 1 |
| Decisive Tactics | Event |  | 1 |
| Declaration of War | Event |  | 1 |
| Deep Space Fleet | Fleet | Centauri | 1 |
| Destiny Fulfilled | Event |  | 1 |
| Dishonor | Conflict | Intrigue | 1 |
| Drigo | Character | Centauri | 1 |
| Expeditionary Fleet | Fleet | Centauri | 1 |
| Exploit Opportunities | Aftermath |  | 1 |
| First Battle Fleet | Fleet | Centauri | 1 |
| Gunboat Diplomacy | Conflict | Military | 1 |
| Hidden Agent | Aftermath |  | 1 |
| Homeworld Fleet | Fleet | Centauri | 1 |
| Kidnapping | Conflict | Intrigue | 1 |
| Knowledge is Power | Agenda |  | 1 |
| Lady Morella | Character | Centauri | 1 |
| Level the Playing Field | Event |  | 3 |
| Limited Strike | Conflict | Military | 1 |
| Londo Mollari | Character | Centauri | 1 |
| Personal Involvement | Aftermath |  | 1 |
| Picket Fleet | Fleet | Centauri | 1 |
| Popular Support | Event |  | 1 |
| Power Politics | Agenda |  | 1 |
| Prophecy | Enhance | Character | 1 |
| Ragesh III | Location | Centauri | 1 |
| Retribution | Aftermath |  | 1 |
| Rise of the Republic | Agenda | Centauri | 1 |
| Rise to Power | Aftermath |  | 1 |
| Rivalry | Aftermath |  | 1 |
| Sabotage | Conflict | Intrigue | 1 |
| Second Battle Fleet | Fleet | Centauri | 1 |
| Short Term Goals | Event |  | 1 |
| Test Their Mettle | Conflict | Diplomacy | 1 |
| The Price of Power | Event |  | 1 |
| Underworld Connections | Event |  | 1 |
| Urza Jaddo | Character | Centauri | 1 |
| Victory in My Grasp | Event |  | 1 |
| Vir Cotto | Character | Centauri | 1 |

## Minbari Federation - 50 cards

| Card | Type | Use | Count |
| :-- | :-- | :-- | :-- |
| Affirm Alliance | Event |  | 1 |
| Affirmation of Peace | Conflict | Diplomacy | 1 |
| Affirmation of Power | Conflict | Diplomacy | 1 |
| Approval of the Grey | Aftermath | Minbari | 1 |
| Ashan | Character | Minbari | 1 |
| Assigning Blame | Aftermath |  | 1 |
| Border Raid | Conflict | Military | 1 |
| Colonial Fleet (Minbari) | Fleet | Minbari | 1 |
| Contact with Vorlons | Event |  | 2 |
| Crystal Cities | Enhance | Minbari | 1 |
| Decisive Tactics | Event |  | 1 |
| Declaration of War | Event |  | 1 |
| Delenn | Character | Minbari | 1 |
| Draal | Character | Minbari | 1 |
| Early Warning | Event |  | 1 |
| Expeditionary Fleet | Fleet | Minbari | 1 |
| Finish the War | Agenda | Minbari | 1 |
| First Battle Fleet (Minbari) | Fleet | Minbari | 1 |
| Hedronn | Character | Minbari | 1 |
| Hidden Knowledge | Event |  | 1 |
| Homeworld Fleet | Fleet | Minbari | 1 |
| Kalain | Character | Minbari | 1 |
| Kidnapping | Conflict | Intrigue | 1 |
| Lamentations | Aftermath |  | 1 |
| Lennier | Character | Minbari | 1 |
| Level the Playing Field | Event |  | 3 |
| Limited Strike | Conflict | Military | 1 |
| Medical Assistance | Event |  | 1 |
| Minbar | Location | Minbari | 1 |
| Minbari Agent | Character | Minbari | 1 |
| Minbari Aide | Character | Minbari | 1 |
| Minbari Captain | Character | Minbari | 1 |
| Minbari Telepath | Character | Minbari | 1 |
| Personal Sacrifice | Aftermath |  | 1 |
| Picket Fleet | Fleet | Minbari | 1 |
| Popular Support | Event |  | 1 |
| Power Politics | Agenda |  | 1 |
| Repairing the Past | Aftermath |  | 1 |
| Second Battle Fleet | Fleet | Minbari | 1 |
| Servants of Order | Agenda | Minbari | 1 |
| Shal Mayan | Character | Minbari | 1 |
| Test Their Mettle | Conflict | Diplomacy | 1 |
| Trade Pact | Conflict | Diplomacy | 1 |
| Underworld Connections | Event |  | 1 |
| United Front | Aftermath |  | 1 |
| You Are Not Ready | Event |  | 1 |
| You Know My Reputation | Event |  | 1 |

## Narn Regime - 50 cards

| Card | Type | Use | Count |
| :-- | :-- | :-- | :-- |
| Affirm Alliance | Event |  | 1 |
| Assigning Blame | Aftermath |  | 1 |
| Book of G'Quan | Enhance | Narn | 1 |
| Border Raid | Conflict | Military | 1 |
| Colonial Fleet | Fleet | Narn | 1 |
| Decisive Tactics | Event |  | 1 |
| Declaration of War | Event |  | 2 |
| Deep Space Fleet | Fleet | Narn | 2 |
| Energy Mines | Enhance | Narn | 1 |
| Euphrates Treaty | Conflict | Diplomacy | 1 |
| Expeditionary Fleet | Fleet | Narn | 1 |
| First Battle Fleet | Fleet | Narn | 1 |
| Fleet Support Base | Enhance | Location | 1 |
| For the Good of All | Event |  | 1 |
| G'Kar | Character | Narn | 1 |
| Gunboat Diplomacy | Conflict | Military | 1 |
| Homeworld Fleet | Fleet | Narn | 1 |
| Hunted | Aftermath |  | 1 |
| Ja'Doc | Character | Narn | 1 |
| Kha'Mak | Character | Narn | 1 |
| Kidnapping | Conflict | Intrigue | 1 |
| Ko'Dath | Character | Narn | 1 |
| Level the Playing Field | Event |  | 3 |
| Limited Strike | Conflict | Military | 2 |
| Na'Kal | Character | Narn | 1 |
| Narn Agent | Character | Narn | 1 |
| Narn Aide | Character | Narn | 1 |
| Narn Captain | Character | Narn | 1 |
| Narn Homeworld | Location | Narn | 1 |
| Na'Toth | Character | Narn | 1 |
| Never Again | Agenda | Narn | 1 |
| N'Grath | Character | Neutral | 1 |
| Picket Fleet (Narn) | Fleet | Narn | 1 |
| Popular Support | Event |  | 1 |
| Power Politics | Agenda |  | 1 |
| Quadrant 14 | Location | Narn | 1 |
| Refugees | Aftermath |  | 1 |
| Renowned Victory | Aftermath |  | 1 |
| Revenge | Agenda | Narn | 1 |
| Second Battle Fleet | Fleet | Narn | 1 |
| Supplement Security | Conflict | Diplomacy | 1 |
| Ta'Lon | Character | Narn | 1 |
| Test Their Mettle | Conflict | Diplomacy | 1 |
| Underworld Connections | Event |  | 1 |
| War Hero | Aftermath |  | 1 |

