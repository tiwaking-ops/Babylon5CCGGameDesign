---
document:
  title: "B5-0939 close-out — transcribe-and-diff batch AFTERMATH (59 titles, 59 faces read)"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0939"
---

# B5-0939 close-out — AFTERMATH (59 titles, all 59 faces legible)

**Claim:** `.agent/CLAIMS/B5-0939.json` (`Buffy (glm-5.3-flash)`, 2026-09-28T17:48:39Z).
Released on close-out. The claim re-takes the row from a dead future-dated ghost
(`started_utc 2026-09-28T22:05:00Z` against a ~10:14Z wall clock — the B5-0952/B5-0653
implausible-timestamp class), whose file had already vanished from `.agent/CLAIMS/` by
boot; the row itself was re-read and still `OPEN` immediately before claiming.
**Scope held:** `investigations/card-images/sorted/` reads + pool JSON read-only + this
report + one pattern + heartbeat + DECISIONS entry + the ledger row. No card JSON, no
`b5ccg/src`, no commit.

## Method (same pipeline as B5-0935, run at full batch width)

Each of the 59 face scans (`de_am_*.gif` ×58 + `aftermath_assigning_blame.gif`; the
`de_am_crisis_of_self.jpg` duplicate of the gif is the same scan in a second codec) was
cropped into a rules-text band (y 52–90%) and a header band (y 30–52%), upscaled ×5
LANCZOS, and read by Windows.Media.Ocr (`.agent/ocr-batch.ps1`) under **two independent
preprocessings** (color and grayscale-autocontrast). Corroboration: the two passes agree
≥0.90 (first 120 normalized chars) on **58 of 59** rules bands, mean 0.995, min 0.894 —
the single 0.894 case is a flavor-string difference below the rules text, not a rules
difference. Face text below is quoted from the reads; where OCR garbles a word the sense
is carried by the surrounding legible text and the garble is left visible rather than
silently corrected.

## Headline verdicts (59 titles / 117 pool records)

- **Batch size: 59, not the row's estimated 58** (58 `de_am_` titles + premiere-only
  `Assigning Blame`; pool: 59 PREMIERE + 58 DELUXE records = 117 records, all 59 ids
  distinct). Deluxe ids mirror the scan filenames exactly (`de_am_*`), so the
  scan↔pool match is exact in both directions.
- **Subtype/trigger header: printed Lost/Won/Participant line vs `subtype` +
  `triggerCondition` — 56 of 59 AGREE.** Every one of the 56 prints a kind word
  (Lost/Won/Participant) exactly matching the pool trigger, and every qualifier the
  faces print (Diplomacy/Intrigue/Military/Minbari) matches the pool's encoding
  (e.g. face "Intrigue Participant Aftermath" ↔ pool `AFTERMATH_INTRIGUE_PARTICIPANT`).
  The pool subtype and triggerCondition fields never disagree with each other
  (14 distinct subtype values, each paired 1:1 with its trigger).
- **3 SUBTYPE MISMATCHES:**
  1. **Glory** — face prints "**Won Diplomacy or Military** Aftermath"; both pool
     records carry `AFTERMATH_WON`/`WON`. The face's dual-conflict qualifier is
     strictly narrower than the pool's plain won-any. (The pool's own `text` field
     keeps the narrower trigger — "Play after winning a Diplomacy or Military
     conflict" — so the pool contradicts *itself* between `subtype` and `text`.)
  2. **Wounded** — face prints "**Lost Intrigue or Military** Aftermath"; pool
     `AFTERMATH_LOST`/`LOST`. Pool `text` again agrees with the face's narrower
     reading, not with the `subtype` field.
  3. **Secondary Experience** — face prints "**Won** Aftermath"; pool
     `AFTERMATH_WON_PARTICIPANT`/`WON_PARTICIPANT`. Here the pool's `text` agrees with
     the *subtype field* ("participated as a supporter and the initiator won") and
     **disagrees with the face**, and the pool's own deluxe annotation layer even says
     "*(Deluxe text change: now classified as Aftermath - Won rather than Aftermath -
     Won Participant)*" while both sets' `subtype` fields still read
     `AFTERMATH_WON_PARTICIPANT` — the annotation layer and the structured fields
     contradict each other in the pool itself.
  All three also diverge in rules text (below), so the subtype finding is corroborating,
  not load-bearing alone.
- **Rules text: CHANGED 59 of 59.** Every pool `text` is a short generic template
  ("Play after losing any conflict. One opponent loses 1 Influence."); every face
  carries specific, longer mechanics. The *theme* (lose/win/participate timing,
  influence/discard/damage/demote flavor) tracks the pool in most cases — this looks
  like the same authored-vs-printed two-layer design split the sibling batches
  (B5-0931, B5-0941, B5-0943, B5-0945) recorded — but the printed mechanics are not
  representable in the pool's schema in the large majority of cases.
- **Faction: Approval of the Grey** prints "Minbari Won Aftermath" and "Grey Council
  Member" targeting — consistent with pool `faction: MINBARI` (the only non-ANY
  AFTERMATH record, 2 of 2 records). No other face prints a faction restriction.
- **Orb/cost: UNTESTABLE 0 of 0.** `cost` is absent from all 117 AFTERMATH records
  (`COST_PRESENT: 0`) — AFTERMATH is a third uncosted type after EVENT (B5-0935) and
  CONFLICT (B5-0941); no orb-vs-cost or orb-vs-military check is executable from the
  pool side, and no orb digit was read from any AFTERMATH face band attempted.
- **Rarity: UNVERIFIED 59 of 59** (no legible rarity marking at scan resolution).
- **ADDED: 0, REMOVED: 0** (scan set and pool match 1:1 in both directions).

## The deluxe annotation layer is real and structured

26 of 58 shared titles carry a parenthetical suffix in the deluxe pool record, e.g.
Hidden Agent "*(Deluxe text change: may now take from any participant, not just the
loser.)*", Martyr "*(Deluxe text change: gain 4 Influence if it was your ambassador.)*",
Secondary Experience "*(Deluxe type change: …)*". So for AFTERMATH the pool *does*
encode its own set-to-set design delta — the two-layer split here is not
premiere-vs-deluxe but template-vs-printed. (B5-0941's CONFLICT batch found no such
layer; this is a per-type difference in how the pool was authored, worth B5-0947's
attention when collating.) No face was read twice across sets, so per-set printed
differences remain unmeasured by this report.

## Per-card transcription (face kind line + rules text; flavor omitted)

| id | face kind line | face rules text (as read) | verdict |
|---|---|---|---|
| approval_of_the_grey | Minbari Won Aftermath | Target one of your supporting characters who is a Grey Council Member. Promote that character to the Inner Circle. | CHANGED |
| assigning_blame | Lost Aftermath | Target ambassador loses 2 Diplomacy. | CHANGED |
| battle_tested | Military Participant Aftermath | Target a character who led a participating fleet. Increase the target's Leadership by 1. If the target's fleet supported a Lost Conflict or opposed a Won Conflict, the total damage he can sustain drops by 2. | CHANGED |
| blood_oath | Lost Aftermath | Target a participant character, and choose a player whose character opposed the conflict. The target participant character gains a Strife Mark when attacking characters controlled by that player. | CHANGED |
| casualty_reports | Military Participant Aftermath | Each player with a participant supporting character who led a fleet in the conflict must discard one of those characters. (Discard this aftermath after play.) | CHANGED |
| combat_experience | Military Participant Aftermath | Participant character gains +1 Leadership. | CHANGED |
| crisis_of_self | Lost Aftermath | Cannot target an ambassador. Rotate target participant character if he is ready. Owner must apply twice the character's influence cost to ready him. Discard this aftermath when this cost is applied. | CHANGED |
| despair | Lost Aftermath | Target a player with a Doom Mark. Whenever the player loses a conflict, all of his characters have -1 to each ability on the following turn (minimum 0). | CHANGED |
| develop_relationship | Participant Aftermath | Target another player's supporting character, after that character supports one of your conflicts. That character gains +1 to all his abilities, but if he ever attacks your characters or opposes one of your conflicts, discard him. | CHANGED |
| diplomatic_advantage | Won Diplomacy Aftermath | Play on your ambassador. You must have won the conflict by 10 or more strength. You may draw 2 free cards instead of 1 during the draw round. | CHANGED |
| disenchantment | Lost Aftermath | Demote target participant Inner Circle character, other than an ambassador, to a supporting character. The target character does not ready during the next ready round. (Discard this aftermath after play.) | CHANGED |
| disgrace | Lost Military Aftermath | Target a participating supporting character. Fleets led by the character may not support or attack during conflicts initiated by his controller. | CHANGED |
| enrage | Lost Diplomacy Aftermath | Target ambassador must apply 1 influence each turn at the beginning of the action round, or his Diplomacy ability for the turn is 0. | CHANGED |
| exploit_opportunities | Won Intrigue Aftermath | Target ambassador gains +1 Intrigue and +1 Diplomacy. | CHANGED |
| focus_your_efforts | Won Aftermath | Target one of your participant characters. Reduce one of his abilities (of 2 or more) by 2, and raise another of his abilities by 2. | CHANGED |
| glory | **Won Diplomacy or Military Aftermath** | Target ambassador gains a Destiny Mark. | CHANGED + SUBTYPE |
| grievance | Won Aftermath | Target the winner of the conflict. Hold a Babylon 5 vote. If the vote passes, target loses 1 influence, while you and Babylon 5 gain 1 influence. If the vote fails, you lose 1 influence. (Discard this aftermath after play.) | CHANGED |
| guilt | Won Military Participant Aftermath | Target a character who led a fleet that made an attack during the conflict. Any fleets the character leads may not attack. | CHANGED |
| harvest_souls | Participant Aftermath | Rotate a Soul Hunter you control. Your ambassador gains all the Destiny Marks from all supporting characters neutralized in the conflict. (Discard this aftermath after play.) | CHANGED |
| hidden_agent | Won Intrigue Aftermath | Target your ambassador. Select another faction that participated in the conflict. Discard any Hidden Agent that faction has affecting your faction. The selected player must choose and show you his face-down conflict card before you choose your conflict each turn. | CHANGED |
| hunted | Intrigue Participant Aftermath | Target a participant character. Two influence must be applied each time the character is readied, or he remains rotated. | CHANGED |
| in_the_line_of_duty | Participant Aftermath | Play when a limited supporting character was neutralized during an attack. Remove the character from play. (No copy of the character can reenter play.) (Discard this aftermath after play.) | CHANGED |
| inevitable_destiny | Participant Aftermath | Target a conflict that was just resolved. Go through your deck until you find the first aftermath playable on that conflict, then play it. Shuffle your deck. (Discard this aftermath after play.) | CHANGED |
| intolerable_interference | Lost Aftermath | Play after a conflict you initiated. Target one player who opposed your conflict, or attacked one of your characters, but who was not a target of the conflict. Increase your tension toward that player by 2. (Discard this aftermath after play.) | CHANGED |
| it_will_be_his_undoing | Lost Aftermath | Target player loses 1 influence for each Doom Mark he has. Purge all his Doom Marks. (Discard this aftermath after play.) | CHANGED |
| lamentations | Lost Aftermath | Target ambassador cannot participate in any conflict. Discard this aftermath at the beginning of the next aftermath round. | CHANGED |
| learning_experience | Intrigue Participant Aftermath | Participant character gains +1 Intrigue. | CHANGED |
| left_vulnerable | Lost Diplomacy Aftermath | Each turn, all players may apply influence to lower one of the target ambassador's abilities. For every 3 influence a player applies, one ability of his choice is reduced by 1 until the end of the turn (minimum 0). | CHANGED |
| loss_of_face | Lost Aftermath | If the final strength supporting the conflict was 0, the initiator of the conflict loses 1 influence. (Discard this aftermath after play.) | CHANGED |
| martyr | Aftermath (plain) | Play on yourself. Discard one of your Inner Circle characters. Convert all of your Doom Marks into Destiny Marks. (Discard this aftermath after play.) | CHANGED |
| negotiated_surrender | Military Participant Aftermath | Target both parties in a war conflict. Reduce both parties' tension toward each other to 4 and end the war. The loser of the conflict loses 2 influence, the winner gains 2 influence. If either participant has more influence than Babylon 5, he may negate this card. (Discard this aftermath after play.) | CHANGED |
| news_of_defeat | Military Participant Aftermath | Target a player who lost a fleet in the conflict. His ambassador loses 1 point from an ability of your choice. | CHANGED |
| nightmares | Lost Aftermath | Target participant character gains a Doom Mark. | CHANGED |
| no_escape | Military Participant Aftermath | Discard one participant fleet that received at least half its printed military ability in damage during the turn. (Discard this aftermath after play.) | CHANGED |
| older_but_wiser | Diplomacy Participant Aftermath | Participant character gains +1 Diplomacy. | CHANGED |
| paying_for_sins | Lost Intrigue Aftermath | Target a participant Inner Circle character, besides the ambassador, who has a Doom Mark. Discard the target. (Discard this aftermath after play.) | CHANGED |
| personal_enemies | Lost Aftermath | Target the ambassador. The ambassador's controller must apply 1 influence to rotate his ambassador during his action. | CHANGED |
| personal_involvement | Lost Aftermath | Discard a neutralized participant Inner Circle character. Cannot target an ambassador. (Discard this aftermath after play.) | CHANGED |
| personal_sacrifice | Won Aftermath | Target your participant ambassador. The amount of damage required to neutralize your ambassador drops by 2. However, apply +2 Diplomacy when your ambassador supports a conflict (this is not an ability increase). | CHANGED |
| protests | Lost Aftermath | Target ambassador's race must have unrest of 4 or 5. Target ambassador's highest ability drops to 0. Discard this card if the race's unrest drops below 4. | CHANGED |
| public_apology | Lost Military Aftermath | If the initiator of the conflict neutralized another player's fleet in an attack, his ambassador's printed Diplomacy is reduced by 2, he loses 1 influence and his unrest grows by 1. The player whose fleet was attacked gains 1 influence. (Discard this aftermath after play.) | CHANGED |
| racial_hatred | Lost Aftermath | Target player must have an unrest of 4 or more. Player must discard all characters he has in play who are not loyal to his ambassador's race, and may not sponsor any more. | CHANGED |
| refugees | Won Military Aftermath | Play after a war conflict. Target player loses 1 influence, and Babylon 5 gains 1 influence. (Discard this aftermath after play.) | CHANGED |
| renowned_victory | Won Military Aftermath | The initiator must have won the conflict by at least 5 strength and must have neutralized at least 5 Military strength of fleets with attacks. Target ambassador gains +1 Diplomacy, +2 Leadership and a Destiny Mark. | CHANGED |
| repairing_the_past | Won Aftermath | Discard one aftermath that targets a card in your faction, or that targets your faction as a whole. (Discard this aftermath after play.) | CHANGED |
| rescue | Military Participant Aftermath | The leader of a neutralized participant fleet is returned to an undamaged, but still rotated, condition. He gains a Doom Mark. (Discard this aftermath after play.) | CHANGED |
| retribution | Won Aftermath | Target participant character takes 1 damage for each Inner Circle character you control who is not neutralized. (Discard this aftermath after play.) | CHANGED |
| reverse_advances | Lost Aftermath | Discard one aftermath that targets a card in another player's faction, or that targets his faction as a whole. (Discard this aftermath after play.) | CHANGED |
| rise_to_power | Won Military Aftermath | Rotate your ambassador and one other character you control with at least 3 Intrigue. Purge up to two Destiny Marks from your ambassador. Gain 1 influence for each mark purged. Ambassador gains a Doom Mark. | CHANGED |
| rivalry | Diplomacy Participant Aftermath | Choose two non-ambassador Inner Circle characters the target controls, at least one of whom must be a participant character. The target must demote one of the two characters to a supporting character. (Discard this aftermath after play.) | CHANGED |
| secondary_experience | **Won Aftermath** | Target a participant character whose ability used in the conflict is not his highest ability. Character gains +2 in the ability used in the conflict. | CHANGED + SUBTYPE |
| skeletons_in_the_closet | Participant Aftermath | Target a participant character. All of the character's abilities drop by 1 per Doom Mark he has, to a minimum of 1. Purge all his Doom Marks. | CHANGED |
| successful_manipulation | Won Intrigue Aftermath | Target ambassador gains +2 Intrigue. | CHANGED |
| under_pressure | Lost Intrigue Aftermath | Target participant character loses 3 Intrigue (to a minimum of 1). Discard this aftermath when his faction wins an Intrigue conflict. | CHANGED |
| united_front | Won Diplomacy Aftermath | Your ambassador gains +3 Diplomacy while supporting conflicts you initiate. | CHANGED |
| vendetta | Lost Aftermath | Target one of your characters. Play only after another player has neutralized one of your supporting characters in an attack. Your target character gains a Strife Mark whenever attacking the neutralized character's attacker. | CHANGED |
| war_hero | Won Military Aftermath | Target an ambassador after his faction captures a location. The ambassador gains +2 Diplomacy and +2 Leadership. | CHANGED |
| wear_and_tear | Military Participant Aftermath | Inflict 1 point of damage on every fleet that participated in the conflict. (Discard this aftermath after play.) | CHANGED |
| wounded | **Lost Intrigue or Military Aftermath** | Target participant character is neutralized, and takes 1 point of severe damage for each Doom Mark he has. (Discard this aftermath after play.) | CHANGED + SUBTYPE |

## Cross-card findings for B5-0947's collation (no verdicts, all flagged)

1. **Marks are the AFTERMATH design language.** Doom Marks on 18 of 59 faces, Destiny
   Marks on 10 (often *conversions*: martyr Doom→Destiny, rise_to_power purge-for-gain,
   harvest_souls harvest-from-neutralized), Strife Marks on 4 (blood_oath, vendetta).
   Combined with B5-0931 (Shadow/Vorlon/Strife on CHARACTER faces) the mark system is
   printed across at least two types and exists nowhere in pool schema or model.
2. **52 of 59 faces carry "(Discard this aftermath after play.)"** — a duration
   concept (aftermath-as-lingering-effect vs one-shot) the pool `text` never encodes.
   The pool has no `duration` field; three faces (lamentations, under_pressure,
   protests) instead discard on a *condition*, and de-affixing this line is a
   representability question, not a wording one.
3. **Demotion/promotion is a real axis:** approval_of_the_grey (promote to Inner
   Circle), disenchantment + rivalry (demote to supporting). The pool has no
   promote/demote concept (cf. B5-0931's assistant/bodyguard finding).
4. **Tension and unrest are printed AFTERMATH state mechanics:** intolerable_interference
   (+2 tension), negotiated_surrender (tension to 4, "end the war"), protests (unrest
   gate 4–5), public_apology (unrest +1), racial_hatred (unrest ≥ 4 gate). This extends
   B5-0941's tension/unrest finding to a second card type.
5. **War-conflict sub-labels:** refugees ("Play after a war conflict"),
   negotiated_surrender ("Target both parties in a war conflict") — a conflict
   qualifier (war) the pool's conflict types don't carry.
6. **AFTERMATH is uncosted:** 0 of 117 records carry `cost` — third uncosted type
   (EVENT B5-0935, CONFLICT B5-0941). No orb column was read; the cost-vs-military
   verdict for this type is UNTESTABLE (missing data, not counter-evidence).
7. **The pool's deluxe annotation layer** (26 records) is a real structured delta that
   B5-0941's CONFLICT batch lacked — when B5-0947 collates "CHANGED/UNCHANGED by
   field", annotation-suffixed text fields should be counted as a deliberate design
   layer, not as noise.
8. **Printed typo faithfully noted:** assigning_blame's face flavor references
   "G'Kar" (partially legible); no rules-text typo was confidently readable — flavor
   degradation is where this OCR pipeline is weakest, and no flavor was transcribed
   beyond what a quote requires.

## Verification

- `b5ccg/compile.sh` exit 0, "Build successful" (JDK 1.8.0_292, `-source 6`) —
  measured this session, no `src` touched by this claim (sibling-batch precedent).
- OCR corroboration: two independent preprocessings agree ≥0.90 on 58/59 rules bands
  (mean 0.995, min 0.894).
- Raw pipeline artifacts preserved under `tmp-scans/b50939/` (crops, `ocr-pass.json`,
  `analysis.txt`, `analysis.json`, `detail.txt`, pool extract) — scratch, gitignored,
  cited here as provenance only.

## Reusable lesson

Filed under `.agent/PATTERNS/Buffy (glm-5.3-flash)/`: a two-field consistency check
(subtype vs triggerCondition) can pass 117 of 117 while both fields disagree with the
printed card — check each layer against the *face*, not only each layer against the
others, and let the pool's own annotation layer tell you which divergences were authored.
