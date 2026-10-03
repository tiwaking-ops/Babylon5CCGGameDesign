---
document:
  title: "B5-2133 — Card cost value assignment policy proposal"
  status: "Proposal"
  provenance:
    author_llm: {name: "Cline", version: "space-bunny-free"}
    created_date: "2026-10-02"
    last_modified_by_llm: {name: "Cline", version: "space-bunny-free"}
    last_modified_date: "2026-10-02"
---

# B5-2133 — Card cost value assignment policy proposal

Proposal-only, no `b5ccg/src/` or `b5ccg/resources/` edits. This document drafts the sourcing policy, per-type default rules, and review gate for assigning concrete `cost` values to cards, as mandated by B5-0315 which accepted the cost schema but forbade mass-assigning values with no IP-safe source.

## 1. Background

B5-0315 (Cost-field design proposal, C1/D13) accepted the addition of a `cost` field (non-negative integer, absent → 0 default) to the card base type for Character, Enhancement, Group, Location, and Fleet card types. The decision explicitly **forbade mass-assigning values with no IP-safe source** and **did not redesign the influence pool** (orthogonal concern).

B5-2018 (card JSON cost field census) subsequently scanned 829 cards and found:
- 377 cards with cost values present (45.5%)
- 452 cards without cost: 430 by design (entire types AFTERMATH, AGENDA, CONFLICT, EVENT where cost is intentionally not modeled per B5-0315)
- 14 genuine anomalies where cost was expected but omitted:
  - 12 CHARACTER: 4 FIXED ambassadors (Sinclair, G'Kar, Delenn, Londo) × 2 sets + Zack Allan (UNCOMMON) × 2 + Kosh Naranek (RARE) × 2
  - 2 ENHANCEMENT: "Judgment by Success" (ENHANCEMENT_FACTION, UNCOMMON) × 2 sets
- Cost value distribution: 0–13, peak at 6 (61 cards), secondary peak at 8 (54 cards). 6 cards have explicit cost=0 (indistinguishable from absent-cost default at runtime).

**This proposal addresses the policy gap:** now that the schema exists and some values exist, how should the remaining values be sourced, defaulted, and reviewed?

## 2. Rulebook cost references

The rulebook (`BABYLON5_CCG_RULEBOOK.md`) defines cost usage but does **not** enumerate per-card values:

| Section | Lines | Content |
|---------|-------|---------|
| Sponsor action | 655–666 | "Your faction must apply the required influence cost **listed on the sponsored card**" — implies card-borne values exist |
| Double-cost rule | 663 | "Characters with a different race name... require a faction to apply **double the listed influence cost** to sponsor" |
| Neutral exemption | 663 | "Neutral characters may be sponsored by any race at **no additional influence cost**" |
| Promote action | 667–670 | "apply influence equal to the supporting character's **influence cost** (again, doubled if the character is loyal to a different race) plus one additional influence for each character already in your Inner Circle" |

**Key observation:** The rulebook treats cost as a card property but never publishes a canonical value table. Any assignment policy must therefore derive values from secondary sources.
## 3. Sourcing policy (ordered by authority)

### 3.1 Tier 1 — Rulebook-explicit values (highest authority)
If the rulebook text or an official rules supplement states a specific cost for a specific card, that value is canonical and requires no further review.

*Current status:* No such explicit values have been identified in the rulebook.

### 3.2 Tier 2 — Official product sources (IP-safe)
Values printed on physical cards, in official deck lists, or in published scenario packs from the original CCG release (Premiere, Deluxe, etc.). These are IP-safe because they are factual observations of published material.

*Action:* Catalog all Premiere and Deluxe card costs from scanned card images or verified community databases. Each value must be traceable to a specific card image or official list.

### 3.3 Tier 3 — Community consensus databases (IP-safe with attribution)
Aggregated values from community-maintained resources (e.g., Babylon 5 CCG wikis, B5 CCG Trader, SNRPG extracts) where multiple independent contributors converge on the same value.

*Requirements:*
- Minimum 3 independent sources agreeing on the same value
- Source URLs and access dates recorded
- Disagreements flagged for Tier 4 review

### 3.4 Tier 4 — Derived/playtest calibration (requires review gate)
Where no Tier 1–3 source exists, values are derived from:
- **Rarity bands:** FIXED/COMMON/UNCOMMON/RARE as rough cost tiers
- **Card text complexity:** Number of abilities, keywords, triggered effects
- **Playtest data:** Win-rate impact, pick-rate in draft/sealed, AI evaluation scores (B5-0324)
- **Analogical mapping:** Nearest neighbour with a known cost (same race, similar subtype, similar rarity)

*All Tier 4 values MUST pass the Review Gate (§5) before assignment.*

### 3.5 Tier 5 — Explicit placeholder (last resort)
`cost: 0` with a `costSource: "PLACEHOLDER"` annotation (new field, optional) indicating the value is uncalibrated. Cards with placeholder costs are legal to play (cost 0 per B5-0323 default) but flagged for future calibration.
## 4. Per-type default rules

B5-0315 specified cost applies to: **Character, Enhancement, Group, Location, Fleet**.

| Card Type | Default (absent → 0) | Rarity-band guidance (Tier 4 fallback) | Notes |
|-----------|---------------------|----------------------------------------|-------|
| **CHARACTER** | 0 | FIXED: 5–8<br>COMMON: 3–6<br>UNCOMMON: 5–9<br>RARE: 7–13 | Double-cost rule (§663) applies at sponsor time; base cost is pre-double. Ambassadors (FIXED) historically 0 — treat as special case. |
| **ENHANCEMENT** | 0 | FIXED: 2–4<br>COMMON: 1–3<br>UNCOMMON: 2–5<br>RARE: 4–8 | Restricted enhancements (race-locked) may warrant −1 vs unrestricted peers. |
| **GROUP** | 0 | FIXED: 3–5<br>COMMON: 2–4<br>UNCOMMON: 3–6<br>RARE: 5–9 | Groups represent organizations; cost scales with member capacity/effects. |
| **LOCATION** | 0 | FIXED: 2–4<br>COMMON: 1–3<br>UNCOMMON: 2–5<br>RARE: 4–7 | Locations are persistent; low base cost reflects tempo investment. |
| **FLEET** | 0 | FIXED: 4–6<br>COMMON: 3–5<br>UNCOMMON: 4–7<br>RARE: 6–10 | Fleets have military value; cost correlates with fleetClass (B5-0336) and firepower. |

**Ambassador special case:** The 4 FIXED ambassadors (Sinclair, G'Kar, Delenn, Londo) currently lack cost (B5-2018 anomaly). Rulebook §Sponsor implies they have a listed cost. Recommendation: assign **cost: 5** (mid-range FIXED character) as Tier 4 default, flagged for Tier 2 verification.

## 5. Review gate (mandatory for every assigned value)

No `cost` value may be written to card JSON without passing the following gate:

### 5.1 Gate checklist (per card or per batch)

| Step | Requirement | Evidence |
|------|-------------|----------|
| **G1** | Source tier identified | Tier 1–5 label recorded in `costSource` field (new optional string field) |
| **G2** | Traceability | For Tier 2: card image reference or official list citation<br>For Tier 3: ≥3 source URLs + access dates<br>For Tier 4: derivation method documented (rarity band, analogue card ID, playtest run ID) |
| **G3** | Cross-type consistency | No card of same type+rarity+subtype family differs by >3 cost without documented justification |
| **G4** | Gameplay sanity | Conformance suite PASS (existing tests); smoke test PASS; no regression in AI valuation (B5-1964 AIMemory) |
| **G5** | Human or overseer sign-off | For Tier 4 values: explicit approval recorded in DECISIONS.md or linked report |

### 5.2 Batch review protocol

Values may be reviewed in batches (e.g., "all Centauri COMMON characters") provided:
- Batch definition is explicit and non-overlapping
- G1–G3 are satisfied for every card in the batch
- G4 is run once per batch (full conformance + smoke)
- G5 sign-off covers the entire batch

### 5.3 `costSource` field specification (proposed)

Add optional string field `costSource` to card JSON for types supporting `cost`:

```json
{
  "cost": 6,
  "costSource": "TIER_2:premiere_card_image_Sinclair_001"
}
```

Valid prefixes: `TIER_1`, `TIER_2`, `TIER_3`, `TIER_4`, `TIER_5_PLACEHOLDER`. Free text after colon.

Loader (`DeckLoader`) treats `costSource` as metadata only — no validation, no default. Absent `costSource` implies unrecorded provenance (legacy cards).
## 6. Implementation sequence (if authorized)

This proposal records policy only. If the human authorizes implementation, the sequence is:

1. **B5-2133a** (data): Add `costSource` field to card record schema (loader accepts, no validation).
2. **B5-2133b** (data): Tier 2 cataloguing pass — extract costs from Premiere/Deluxe card images for all 377 currently-costed cards; record sources.
3. **B5-2133c** (data): Tier 3 cross-check — query community databases for the 14 B5-2018 anomalies + any uncosted cards of supported types.
4. **B5-2133d** (data): Tier 4 calibration — assign rarity-band defaults to remaining uncosted cards of supported types; run review gate per batch.
5. **B5-2133e** (engine): Extend `CardEffects` / `AIMemory` to optionally log `costSource` for debugging AI valuation decisions.

No step modifies `engine/` or `model/` logic; all are data edits or schema extensions.

## 7. Open questions (deferred to implementation tasks)

1. **Ambassador cost 0 vs 5**: Rulebook implies ambassadors have a listed cost, but they start in play (Inner Circle). Does "listed cost" apply to sponsoring them *from hand* (if a variant rule allows it)? If never sponsored from hand, cost is vestigial — assign 0 or 5?
2. **Cost 0 semantics**: 6 cards already have explicit `cost: 0`. Is "absent → 0" (B5-0323) distinguishable from "explicit 0"? Currently indistinguishable at runtime. Should `costSource` differentiate?
3. **Fleet cost vs fleetClass**: B5-0336 added `fleetClass` (FIGHTER, CORVETTE, CRUISER, DREADNOUGHT). Should cost default by class rather than rarity?
4. **Group/Location/Fleet data coverage**: B5-2018 found costs only on Character/Enhancement. Are Group/Location/Fleet costs all Tier 4/5?
5. **Retroactive `costSource` for existing 377 values**: Do we backfill sources for cards that already have cost? Recommended: yes, as a separate data hygiene pass.

## 8. Verification (proposal-only)

No code touched. Gate context: last full shared gate `compile.sh` exit 0 on JDK 1.8.0_292 with `-source 6`; Java 6 census clean. This proposal makes no claim on it.

Acceptance criteria for future implementation tasks are summarized in §6 — each slice defines its own suite assertions when implemented.

## 9. Files referenced

- `BABYLON5_CCG_RULEBOOK.md` lines 655–670 (Sponsor, Promote, double-cost, neutral exemption)
- `docs/DECISIONS.md` B5-0315 entry (cost-field design proposal, C1/D13)
- `docs/DECISIONS.md` B5-2018 entry (card JSON cost field census)
- `.agent/REPORTS/2026-10-02-kilo (kilo-auto-free) 11-B5-2018.md` (census report)
- `docs/proposals/b5-0360-economy-modeling-proposal.md` (double-cost, waiver semantics — cost is a card field; double-cost is Faction-side computation)
- `b5ccg/resources/cards/premiere.json`, `deluxe.json` (current card data)
- `b5ccg/src/b5ccg/model/card/` (Card, CharacterCard, EnhancementCard, GroupCard, LocationCard, FleetCard — cost field hydrated per B5-0323)

## 10. Proposed task breakdown (if the human wants implementation)

- **B5-2133a** (data, schema): Add optional `costSource` string field to cost-bearing card types; loader accepts, no validation.
- **B5-2133b** (data, Tier 2): Catalogue Premiere/Deluxe card costs from card images; backfill `costSource` for 377 existing costed cards.
- **B5-2133c** (data, Tier 3): Community database cross-check for 14 anomalies + uncosted supported-type cards.
- **B5-2133d** (data, Tier 4): Rarity-band default assignment for remaining uncosted supported-type cards; batch review gate.
- **B5-2133e** (engine, optional): `AIMemory` / `CardEffects` logging of `costSource` for AI valuation audit trail.

These are proposal-record entries only — no tasks are claimed or seeded here. The human decides whether to authorize any of them.