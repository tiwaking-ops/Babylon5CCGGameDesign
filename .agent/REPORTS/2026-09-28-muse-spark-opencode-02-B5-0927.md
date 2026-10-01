---
document:
  title: "B5-0929 follow-up batches — transcribe-and-diff remaining 419 titles by type"
  status: "Report (no authority; observations and test results only)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0927"
---

# B5-0927 close-out — pilot diff, first batch (18 alias-resolved cards)

**Claim:** `.agent/CLAIMS/B5-0927.json` (`muse-spark-opencode-02`, 08:06:27Z). Released on close-out.
**Scope held:** `sorted/` reads + this report + one pattern + heartbeat. No card JSON, no src,
no existing-file edits, no commit.
**Batch definition:** the 18 face-verified alias cards from B5-0929 (8 Minbari fleets, 5 typo
filenames, Reserve/Strike race-suffixed, Level the Playing Field, 2 Psi Corps arts). All 18
faces were legible; nothing below is guessed. The remaining 419 titled scans are seeded as
type-batches below — this pilot proves the method and measures the divergence shape first.

## Verdicts

- **CHANGED (text): 18 of 18.** Every pool `text` is a placeholder paraphrase or describes a
  different effect; no printed rules text matches verbatim.
- **UNCHANGED (type line / faction): 18 of 18** at coarse granularity (Minbari Fleet,
  Event, Lost Aftermath, Faction Enhancement, Agenda, Human/Narn Fleet, Human Group all
  agree with pool type/faction).
- **Title drift: 9 of 18.** Pool `(Minbari)` suffixes (8 fleets) and `3+9` (Level) are absent
  on the printed faces.
- **ADDED: 0.** Every scan maps to a pool title. **REMOVED (no scan): 10** (B5-0929 list).
- **Rarity: UNVERIFIED** on all 18 (no legible rarity marking at scan resolution) — recorded
  as unverifiable, not as agreement.

## Per-card transcription vs pool (rules text exact as read; flavor condensed)

| id (pool) | Face title / type line | Printed rules text | Pool text verdict |
|---|---|---|---|
| de_fleet_colonial_minbari | Colonial Fleet / Minbari Fleet, orb 6 | "Can only participate in conflicts targeting you." | CHANGED (pool: "Minbari colonial defense fleet.") |
| fleet_expeditionary_minbari | Expeditionary Fleet / Minbari Fleet, orb 10 | "If you have tension toward another race at 5, you may rotate this fleet to attack any fleet from that race. Fleets from that race may attack this fleet in return." | CHANGED (pool: "Minbari expeditionary fleet.") |
| de_fleet_picket_minbari | Picket Fleet / Minbari Fleet, orb 4 | "Multiple. Can only participate in conflicts targeting you." | CHANGED (pool: "Minbari border patrol.") + schema gap: pool has no multiplicity field |
| fleet_homeworld_minbari | Homeworld Fleet / Minbari Fleet, orb 8 | "Can only participate in conflicts targeting you." | CHANGED (pool: "Minbar defense fleet.") |
| de_fleet_first_battle_minbari | First Battle Fleet / Minbari Fleet, orb 10 | No rules line; stealth-technology paragraph reads as flavor | CHANGED (pool: "Minbari primary battle fleet.") |
| de_fleet_second_battle_minbari | Second Battle Fleet / Minbari Fleet, orb 11 | No rules line; Black Star paragraph reads as flavor | CHANGED (pool: "Minbari reserve battle fleet.") |
| de_fleet_third_battle_minbari | Third Battle Fleet / Minbari Fleet, orb 10 | No rules line; most-advanced-military paragraph reads as flavor | CHANGED (pool: "Minbari veteran battle fleet.") |
| de_fleet_deep_space_minbari | Deep Space Fleet / Minbari Fleet, orb 7 | "Multiple." + alert-against-threats flavor | CHANGED (pool: "Minbari deep-space warships.") + multiplicity gap |
| de_event_moral_quandary | Moral Quandary / Event, orb 1 | "Target a character. The target character is rotated for no effect. The owner of the character may negate this event by applying influence equal to the character's cost." | CHANGED ENTIRELY (pool: discard-1-or-lose-1-Influence — a different effect) |
| de_am_personal_enemies | Personal Enemies / Lost Aftermath | "Target the ambassador. The ambassador's controller must apply 1 influence to rotate his ambassador during his action." | CHANGED ENTIRELY (pool: +1-to-all-stats-when-opposing — a different effect). Subtype AFTERMATH_LOST + trigger LOST agree with "Lost" |
| de_enh_vital_interests | Vital Interests / Faction Enhancement, orb 1 | "Target your faction. If you initiate a war conflict, and the conflict is successful, your target loses 1 influence. Apply 5 influence during the draw round, or discard this enhancement." | CHANGED ENTIRELY (pool: once-per-round force-support — a different effect). Pool cost 1 vs printed upkeep 5 — cost-field semantics unclear |
| de_agenda_peace_in_our_time | Peace In Our Time / Agenda | "Count every 3 points of Babylon 5 influence as 1 power. Target a race. You may apply 10 influence plus 1 per fleet of that race in play to lower that race's tension toward one other race by 1." | CHANGED ENTIRELY (pool: INFLUENCE_20 win + no-military-ongoing — a different agenda; no win condition on face) |
| de_agenda_defense_in_depth | Defense in Depth / Agenda | "All of your fleets gain +2 Military while you are the target of a Military conflict. All of your locations gain +5 Military. Your influence rating is increased by 1 for each location of your race you control." | CHANGED SUBSTANTIALLY (pool: INFLUENCE_20 win + rotate-to-add-2-defense; numbers and structure differ; no win condition on face) |
| event_level_the_playing_field | Level the Playing Field / Event, orb 1 | "Target character or fleet may apply its highest ability to support or oppose one conflict of your choice (no matter which ability would normally be appropriate.)" (Bester flavor) | CHANGED ENTIRELY (pool: most-gives-3-to-least Influence — a different card) |
| de_fleet_reserve_human | Reserve Fleet / Human Fleet, orb 5 | "You may only sponsor this fleet if you have a tension of 5 toward another race." | CHANGED (pool: aftermath-phase play — absent on face) |
| de_fleet_strike_fleet | Strike Fleet / Narn Fleet, orb 6 | "Multiple." + formidable-opponents flavor | CHANGED (pool: Intrigue-participation — absent on face) + multiplicity gap |
| group_psi_corps_intelligence (Psi art, primary) | Psi Corps Intelligence / Human Group, orb 6 | "Rotate this group to look at the top two cards of your deck. Or, rotate this group to initiate an additional Psi conflict with another player. If successful, look at the top four cards of his deck. In either case, you may place one card you have seen at the bottom of that deck." | CHANGED ENTIRELY (pool: add-2-to-Intrigue-or-Psi-total — a different effect) |
| group_psi_corps_intelligence (Bester art, `_2`) | Same title/type, orb 6 | Same deck-look frame with Psi-Corps-sponsor preamble ("sponsored as if it were not a Psi Corps card… half cost… during the Conflict Round…") | Same pool record matches neither printing exactly — two printings, one paraphrase |

## Load-bearing measurements for the data tasks

1. **Orb == pool cost on 11 of 11 checkable cards** (10 fleets + Psi group; orb 6/10/4/8/10/11/10/7/5/6/6
   vs pool cost 6/10/4/8/10/11/10/7/5/6/6). Exact, no exceptions.
2. **Orb == pool military on 0 of 10 fleets** (pool military runs 3/5/3/7/6/6/6/4/3/4 against the
   same orbs). Open question recorded, not a verdict: whether the orb is cost (then military
   is printed nowhere legible, possibly the small top-left glyph) or military (then the pool
   cost column was read off the orb). Either way cost and military cannot both be right from
   one printed number — the cost-field backfill (B5-0315 follow-throughs) must settle layout
   semantics before assigning values.
3. **Events/agendas show orb 1** (Moral Quandary, Level, Vital Interests) with no pool cost
   field (default 0) — same cost-field question.
4. **`Multiple.` keyword** on 3 faces has no pool schema field (Picket, Deep Space, Strike).
5. Pool paraphrases sometimes describe a *different card's* effect, not a loose wording of
   the printed one (Moral Quandary, Personal Enemies, Peace In Our Time, Level the Playing
   Field, Psi Corps Intelligence) — the B5-0654 design-layer evidence, strengthened: 0/18
   verbatim, several disjoint.

## Follow-up batches seeded (OPEN, unclaimed)

Nine transcribe-and-diff rows covering the remaining 419 titled scans, split by type so no
single pass must read more than ~50 faces: B5-0931 (CHARACTER A–L), B5-0933 (CHARACTER M–Z),
B5-0935 (EVENT A–L), B5-0937 (EVENT M–Z), B5-0939 (AFTERMATH, 58), B5-0941 (CONFLICT, 56),
B5-0943 (ENHANCEMENT 39 + FLEET 33), B5-0945 (GROUP 25 + AGENDA 23 + LOCATION 13). Same
method as here: transcribe exactly as legible, mark illegible per card, never guess, diff
by id, schema-gate, no card JSON, no src, no commit.

## Verification

- `ledger-query.ps1`: B5-0927 reads 7 pipes / doubleLead no / DONE after close-out.
- `run-dup-census.ps1` PASS post-write (any foreign dup recorded in DECISIONS, untouched).
- No compile gate run: no `src` touched.

## Reusable lesson

Filed under `.agent/PATTERNS/muse-spark-opencode-02/`: pilot the diff on the awkward cards
first — the alias-resolved residue (typos, suffix conventions, dual arts) is where the
method meets reality, and 18 faces were enough to surface the orb-equals-cost correspondence
plus the placeholder-text situation before spending 400 reads.
