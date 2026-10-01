---
document:
  title: "Authored card pool — frozen baseline and acceptance specification"
  status: "Report (observation, no authority; B5-0801 deliverable 1 and 3)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0801"
---

# Authored card pool — baseline and acceptance spec

**Read this before supplying any card data.** This file freezes the current pool so that
whatever arrives next can be *diffed* rather than eyeballed, and specifies the exact shape
supplied data must match. It is an observation with no authority. Nothing here was
fetched, scraped, vendored or imported; no card JSON was edited.

## 1. Frozen baseline

| File | Set | Cards | Bytes | SHA-256 |
|---|---|---|---|---|
| `b5ccg/resources/cards/premiere.json` | PREMIERE | **446** | 450 076 | `FAC0747AE0EFC9EBD6D7D0E4392FD3AE9537A89ECDFEA5365AEBD3424CD57CFE` |
| `b5ccg/resources/cards/deluxe.json` | DELUXE | **383** | 381 604 | `2F74FA4D2F0ECDF0AD2891625F250B38DA2139AEB9CEA705027D0F7E461D611` |

Total **829** records. `git HEAD` at freeze: `02716363`. Both files are tracked, so the
baseline is recoverable from git as well as from these hashes — the hashes are the
*assertion* that it has not moved.

## 2. The completeness gap, established from internal sources only

**There is no full card-text database in this repository, and there never was.** Three
independent checks, all run rather than cited:

1. **The canonical rulebook contains no card data.** A case-insensitive search of
   `BABYLON5_CCG_RULEBOOK.md` for `A Rising Power`, `Alliance of Races` and
   `Peace in Our Time` returns **zero matches**. The one canonical document in the repo
   carries rules, not cards.
2. **No card dataset exists anywhere in the tree.** The only other data files are
   `b5ccg/resources/decks/premiere-starter-decks.json` (12 KB, 446-card deck lists) and
   their `out/` copies. The only external references in the repository are the rulespal
   rulebook URL cited as the rulebook's own source and an unrelated Figma link in
   `README.md`. **Neither is a card dataset**, and no card images exist —
   `b5ccg/resources` contains **0** image files of any format.
3. **Git history forecloses recovery.** `premiere.json` holds **exactly 446 cards at
   every one of the eight commits** that ever touched it (2026-09-21 → 2026-09-28). Only
   whitespace and deluxe annotations move the byte count (172 731 → 174 160 chars). There
   is **no larger, earlier or more complete version to recover**.

## 3. Evidence that this pool is hand-authored, not imported

| Observation | Count | Why it matters |
|---|---|---|
| Records carrying an inline `(Deluxe text change: …)` annotation | **110** | A scraped or licensed dataset carries printed text. Editorial asides about what *was* changed are the signature of a human rewriting a card. |
| Deluxe titles absent from Premiere | **0** | The deluxe set is a strict reprint subset, not an independent pool. |
| Premiere-only titles | **63** | |
| Titles in both sets | **383** | 383 + 63 = 446. The arithmetic closes exactly. |
| Distinct `id` values across all 829 | **829** | No duplicates. |

Type distribution: EVENT 166, CHARACTER 161, AFTERMATH 117, CONFLICT 108, FLEET 80,
ENHANCEMENT 78, GROUP 51, AGENDA 47, LOCATION 21.

## 4. The 13 cards carrying "power" in title or text — verbatim

This is the set the human's correction was about. **Every one resolves to Influence,
Leadership or card-draw. None grants the rulebook's derived Power stat.**

| Set | Title | Type | Current text |
|---|---|---|---|
| PREMIERE | A Rising Power | AGENDA | Win Condition: Reach 20 Influence. Ongoing: Gain 1 additional Influence whenever you win a Diplomacy conflict. |
| DELUXE | A Rising Power | AGENDA | *identical to Premiere* |
| PREMIERE | Knowledge is Power | AGENDA | Win Condition: Reach 20 Influence. Ongoing: Once per round, draw 1 extra card. |
| DELUXE | Knowledge is Power | AGENDA | …draw 1 extra card. **(Deluxe text change: draw 2 extra cards if you have 3 or more characters in Inner Circle.)** |
| PREMIERE | Power Politics | AGENDA | Win Condition: Reach 20 Influence. Ongoing: When you play an Event card during another player's turn, gain 1 Influence. |
| PREMIERE + DELUXE | Affirmation of Power | CONFLICT | Diplomacy conflict. Winner gains 2 Influence. |
| PREMIERE + DELUXE | Power Posturing | ENHANCEMENT | Attach to a Character. That character gains +3 Leadership. While this Enhancement is in play, your Military conflicts gain +1. **(Deluxe: +2.)** |
| PREMIERE + DELUXE | The Price of Power | EVENT | Play any time. The player with the most Influence loses 2 Influence. If there is a tie, each tied player loses 1 Influence. |
| PREMIERE + DELUXE | Rise to Power | AFTERMATH | Play after winning a Military conflict. Gain 2 Influence. |

**The text the human quoted is absent.** A Rising Power was quoted as *"Count each 10
Diplomacy you have from ready characters you control as +1 power"* — which is precisely
the shape rulebook:171 describes ("other cards in play may add additional points to a
player's Power total under conditions specified on the card itself"). No card text
anywhere in the pool contains "count each" or "per 10".

The four other cards the human named all **exist**, all as AGENDA cards, all written as
Influence: **Alliance of Races** ("gain 2 Influence"), **Never Again** ("Gain 2 Influence
whenever you win a Military conflict against the Centauri player"), **Peace in Our Time**
("Gain 1 Influence for each round in which no Military conflict occurs"), **Revenge**
("spend 1 Influence to add 2 to your Military conflict total").

**Correcting the record on the proposal that got this wrong.**
`docs/proposals/negative-power-split-design-proposal.md` §4 states "the word 'power'
appears in card texts **0 times**" and supports it with a grep for "text fields **opening
with** a case-insensitive *power*". That pattern **cannot match a mid-sentence
occurrence**, so the stated method was incapable of detecting what the human described.
The correct figure is **13 records carrying power in title or text, 0 of them granting a
Power stat**. The conclusion survives; the reasoning and the number do not. That
proposal is not edited here — it is a separately claimed row.

## 5. Acceptance specification for supplied data

Supplied data is checked against this, mechanically, before it is considered.

**Schema.** 27 fields. Nine are present on **all 829** records and are therefore
**required**: `id`, `title`, `type`, `subtype`, `set`, `rarity`, `faction`, `text`,
`imageKey`. The other eighteen are sparse and must stay sparse unless the field is
genuinely applicable:

`conflictType` 108 · `cost` 371 · `diplomacy` 161 · `diplomacyBonus` 15 · `fleetClass` 80 ·
`influencePerRound` 21 · `influenceReward` 108 · `intrigue` 161 · `intrigueBonus` 16 ·
`isAmbassador` 10 (Boolean) · `isMajorAgenda` 12 (Boolean) · `leadership` 161 ·
`leadershipBonus` 13 · `military` 80 · `militaryBonus` 21 · `participation` 7 (nested
object) · `psi` 46 · `psiBonus` 16 · `timing` 1 · `triggerCondition` 117 ·
`winCondition` 47.

**Enum domains — supplied values must be members, not new strings.**

| Field | Domain size | Values |
|---|---|---|
| `type` | 9 | AFTERMATH, AGENDA, CHARACTER, CONFLICT, ENHANCEMENT, EVENT, FLEET, GROUP, LOCATION |
| `subtype` | 59 | full list in §5.1 below |
| `rarity` | 5 | COMMON, FIXED, RARE, RARE_WITHDRAWN, UNCOMMON |
| `faction` | 7 | ANY, CENTAURI, HUMAN, MINBARI, NARN, NEUTRAL, NON_ALIGNED |
| `set` | 2 | DELUXE, PREMIERE |
| `conflictType` | 4 | DIPLOMACY, INTRIGUE, MILITARY, PSI |
| `fleetClass` | 20 | COLONIAL, DEEP_SPACE, DRAZI, EXPEDITIONY, FIRST_BATTLE, FLEET_OF_THE_LINE, GARRISON, HEAVY, HOMEWORLD, IPSHA, MARKAB, PICKET, RESERVE, SECOND_BATTLE, STRIKE, THIRD_BATTLE, UTILITY, VREE, WAR_CRUISER, WARLEADERS |
| `timing` | 1 | ANY |

`timing` appearing on exactly **one** record and `participation` on exactly **7** are
themselves worth a look during any correction pass: sparse fields with a single occupant
are usually a hand-edit rather than a schema decision.

### 5.1 `subtype` domain, all 59

AFTERMATH, AFTERMATH_DIPLOMACY_PARTICIPANT, AFTERMATH_INTRIGUE_PARTICIPANT,
AFTERMATH_LOST, AFTERMATH_LOST_DIPLOMACY, AFTERMATH_LOST_INTRIGUE,
AFTERMATH_LOST_MILITARY, AFTERMATH_MILITARY_PARTICIPANT, AFTERMATH_PARTICIPANT,
AFTERMATH_WON, AFTERMATH_WON_DIPLOMACY, AFTERMATH_WON_INTRIGUE,
AFTERMATH_WON_MILITARY, AFTERMATH_WON_PARTICIPANT, AGENDA, AGENDA_CENTAURI, AGENDA_HUMAN,
AFTERMATH_MAJOR, AGENDA_MINBARI, AGENDA_NARN, CHARACTER_CENTAURI, CHARACTER_HUMAN,
CHARACTER_MINBARI, CHARACTER_NARN, CHARACTER_NEUTRAL, CHARACTER_VORLON,
CONFLICT_DIPLOMACY, CONFLICT_INTRIGUE, CONFLICT_MILITARY, CONFLICT_PSI,
ENHANCEMENT_BABYLON5, ENHANCEMENT_CENTAURI_CHARACTER, ENHANCEMENT_CHARACTER,
ENHANCEMENT_FACTION, ENHANCEMENT_FLEET, ENHANCEMENT_GLOBAL, ENHANCEMENT_LOCATION,
ENHANCEMENT_MINBARI_CHARACTER, ENHANCEMENT_MINBARI_FLEET, ENHANCEMENT_MINBARI_LOCATION,
ENHANCEMENT_NARN_CHARACTER, ENHANCEMENT_NARN_FLEET, EVENT, EVENT_MINBARI, FLEET_CENTAURI,
FLEET_HUMAN, FLEET_MINBARI, FLEET_NARN, FLEET_NON_ALIGNED, GROUP, GROUP_CENTAURI,
GROUP_HUMAN, GROUP_MINBARI, GROUP_NARN, LOCATION, LOCATION_CENTAURI, LOCATION_HUMAN,
LOCATION_MINBARI, LOCATION_NARN.

*(transcribed as stored; the ordering above is `Sort-Object -Unique` output, not
significance)*

## 6. Diff rules — what a change report must be able to say

A supplied pool is compared **by `id`**, not by position, and reported as four disjoint
sets:

| Class | Meaning | Expected on a text-only correction |
|---|---|---|
| **ADDED** | `id` absent from baseline | **0** — the human is correcting text, not adding cards. Any ADDED record is a scope question, not a text fix. |
| **REMOVED** | `id` present in baseline, absent from supply | **0** — a silent deletion is the single most damaging outcome and must never pass unremarked. |
| **CHANGED** | same `id`, ≥1 differing field | the deliverable; report field-by-field, old → new |
| **UNCHANGED** | byte-identical | the bulk |

Plus four standing assertions, each of which has bitten this repo before:

* **`set` must not move.** A record's `set` changing means a reprint pair was split.
* **Reprint pairs stay paired.** 383 titles currently appear in both sets; a change that
  makes that number anything but 383 is a structural regression, not a text edit.
* **The `(Deluxe text change: …)` annotation is editorial.** It is not printed card text
  and must not be scraped into or out of a supplied file. If correct printed text arrives
  for deluxe cards, the annotation convention needs an explicit ruling — see §7.
* **No new enum values** without a separately claimed row, per §5.

## 7. What is owed, and by whom

Not decided here. Recorded so it cannot be lost.

1. **Is Power ≡ Influence permanent or per-card?** The rulebook (canonical, AGENTS.md §2)
   defines Power as Influence plus card add-ons and gives Negative Power a real rule at
   :1034. The authored layer expresses all eight "power" cards as Influence, so the rule
   has no reachable trigger *on this tree*. Either the equivalence is permanent and the
   rule is recorded as a documented non-firing interpretation in `docs/DECISIONS.md`, or
   Power is restored per card — each a separately claimed card-specific task, exactly as
   B5-0654 rule (1) already requires.
2. **Under the "permanent" option, the engine should assert the equivalence** rather than
   assume it, so a future card that says "power" cannot be silently implemented as
   influence. That failure is invisible until it is played.
3. **B5-0654 supersession** is the human's to make. The ruling's *premise* — that the
   authored pool faithfully represents the printed cards — is falsified by §2 and §4
   above. Its *conclusion* (authored layer is the design layer; do not bulk-import printed
   text) remains defensible as policy. Which of those is being replaced is a decision, not
   a finding.
4. **No images exist locally** (§2.2), so the parse-from-images route requires the images
   to be supplied. Nothing in the repo can be OCR'd today.

## Cross-references

* Ledger row **B5-0801** (this file is its deliverables 1 and 3) ·
  `docs/proposals/negative-power-split-design-proposal.md` §4 (the unsound grep) ·
  `docs/DECISIONS.md` 2026-09-27 **B5-0654** (the ruling under review) ·
  `BABYLON5_CCG_RULEBOOK.md:171` and `:1034` (Power definition and Negative Power) ·
  report `.agent/REPORTS/2026-09-28-opencode (space-bunny-free) 2-B5-0801.md`
