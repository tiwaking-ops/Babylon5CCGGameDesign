---
author_llm: {name: "Cline (claude-4-sonnet)", version: "claude-4-sonnet"}
created_date: "2026-10-03"
status: "Proposal"
---

# New-Card Onboarding Field Checklist

**Proposal**: Turn the six field censuses into required-versus-optional field lists per card type with the loader mechanism per field.

**Fenced by**: OPEN B5-2095 (AGENDA census), OPEN B5-2097 (AFTERMATH census — BLOCKED at gate, incomplete)

**Scope**: docs/proposals only — no card JSON edits, no src edits, no commit, no push.

---

## Purpose

This checklist consolidates the findings from six read-only field censuses (B5-2091 LOCATION, B5-2093 FLEET, B5-2095 AGENDA, B5-2097 AFTERMATH, B5-1981/B5-2067 CHARACTER, B5-2069 CONFLICT) plus the combined ENHANCEMENT/GROUP/EVENT census (B5-2071) into a single reference for anyone adding new card data. Each field is graded by **loader mechanism** — the code path that handles its presence or absence — because that determines the real-world symptom of a missing or malformed value.

---

## Loader Mechanism Legend

| Mechanism | Code Location | Symptom of Absence | Census Grade |
|-----------|---------------|-------------------|--------------|
| **THROWS (card-dropped)** | `DeckLoader.validateFields` → `parseCards` catch (lines 206-209) | Card silently dropped from pool; one stderr line "Skipping card, parse error" | **REQUIRED** — absence loses the card |
| **SILENT DEFAULT** | `DeckLoader.getOrDefault` / `boolVal` / `intVal` (lines 313-322, 343, 567-572) | Card loads clean; field takes a default value (empty string, 0, false, COMMON, ANY, PREMIERE, id) | **OPTIONAL** — absence is invisible at load |
| **VALIDATED BUT DEFAULTED** | `validateFields` checks presence but builder provides default | If missing, throws (card dropped); if present with typo, may be accepted or reported as UNKNOWN | **REQUIRED** — validated by loader |
| **NOT IN EXPECTED SET** | `validateFields` expected-key set (lines 416-418, 488-490, 517-521) | Field not in expected set → "UNKNOWN FIELD" printed to stderr on every load | **FORBIDDEN** — presence is noisy |

---

## Per-Type Field Checklists

### 1. AGENDA (47 records — B5-2095)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | Unique across corpus |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `AGENDA` |
| `isMajorAgenda` | VALIDATED BUT DEFAULTED (boolVal default false) | **YES** | Loader throws if missing; default never used |
| `winCondition` | VALIDATED BUT DEFAULTED (getOrDefault "INFLUENCE_20") | **YES** | Loader throws if missing; must be enum: INFLUENCE_20, MILITARY_SUPREMACY, MOST_INNER_CIRCLE |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | Observed: AGENDA, AGENDA_MAJOR, AGENDA_MINBARI, AGENDA_HUMAN, AGENDA_CENTAURI, AGENDA_NARN |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | Must match file |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | Blank renders as empty rules box |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | Present as empty string on all 47; loader treats as optional |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | Present as empty string on all 47 |

**Critical Finding (B5-2095)**: Four AGENDA_MINBARI cards have `isMajorAgenda=False` despite subtype implying major status — data entry error, not a schema issue.

---

### 2. LOCATION (21 records — B5-2091)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `LOCATION` |
| `influencePerRound` | THROWS (LOCATION-specific) | **YES** | Loader throws if missing; integer, not quoted |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `military` | SILENT DEFAULT (absent) | NO | **Optional** — absent on 21/21; not in expected set |

**Finding (B5-2091)**: Zero violations. `military` is optional and absent on all records — legal but means engine sees no military ability.
---

### 3. FLEET (80 records — B5-2093)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `FLEET` |
| `military` | THROWS (FLEET-specific) | **YES** | Loader throws if missing; integer 1-8; builder defaults to 1 but validation runs first |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | Optional; missing on 438/829 cards corpus-wide |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |
| `fleetClass` | SILENT DEFAULT (getOrDefault "") | NO | **Optional but fully populated** — 80/80 present |

**Finding (B5-2093)**: `fleetClass` 100% populated (80/80). `cost` missing on many cards including all ambassadors.

---

### 4. CHARACTER (161 records — B5-1981, B5-2067)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `CHARACTER` |
| `diplomacy` | THROWS (CHARACTER hard-required) | **YES** | Integer ≥ 0; loader throws if missing |
| `intrigue` | THROWS (CHARACTER hard-required) | **YES** | Integer ≥ 0 |
| `psi` | THROWS (CHARACTER hard-required) | **YES** | Integer ≥ 0 |
| `leadership` | THROWS (CHARACTER hard-required) | **YES** | Integer ≥ 0 |
| `isAmbassador` | THROWS (CHARACTER hard-required) | **YES** | Boolean; loader throws if missing |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault 0) | NO | **Optional** — absent on 12/161 (6 per set, same titles); defaults to 0 |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | Absent on all 161 |

---

### 5. CONFLICT (108 records — B5-2069)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `CONFLICT` |
| `conflictType` | VALIDATED (parseConflictType, default DIPLOMACY) | **YES** | Loader validates enum; missing → silent default to DIPLOMACY (retypes card) |
| `influenceReward` | VALIDATED (getOrDefault 1) | **YES** | Required by loader; integer; default 1 if missing |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | **Cosmetic for CONFLICT** — not read by conflict resolution; matches conflictType 108/108 today but no gate enforces agreement |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | All 108 are `ANY` |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |
| `participation` | SILENT DEFAULT (absent) | NO | Optional sub-object; present on 31/108 |

**Latent Gap (B5-2069)**: `subtype` and `conflictType` encode same datum twice with no cross-validation gate.

---

### 6. AFTERMATH (B5-2097 — BLOCKED, INCOMPLETE)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | Inferred from loader |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `AFTERMATH` |
| `triggerCondition` | VALIDATED (hard-required in validateFields) | **YES** | Loader throws if missing; enum values observed in corpus |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |

**Status**: B5-2097 was BLOCKED at gate (out-of-scope compile failure in ui/). Census incomplete — no report data available. This checklist records the loader contract as-read from source; a complete AFTERMATH census is needed.

---

### 7. ENHANCEMENT (78 records — B5-2071)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `ENHANCEMENT` |
| `diplomacyBonus` | THROWS (ENHANCEMENT-specific) | **YES** | Integer; loader throws if missing |
| `intrigueBonus` | THROWS (ENHANCEMENT-specific) | **YES** | Integer |
| `psiBonus` | THROWS (ENHANCEMENT-specific) | **YES** | Integer |
| `militaryBonus` | THROWS (ENHANCEMENT-specific) | **YES** | Integer |
| `leadershipBonus` | THROWS (ENHANCEMENT-specific) | **YES** | Integer |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | Observed: ENHANCEMENT_* variants |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | Optional |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |

**Finding (B5-2071)**: Zero violations. All five bonuses are JSON integers on 78/78.

---

### 8. GROUP (51 records — B5-2071)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `GROUP` |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | Optional |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |

**Finding (B5-2071)**: Zero violations. No type-specific required fields beyond alwaysRequired + common.

---

### 9. EVENT (166 records — B5-2071)

| Field | Mechanism | Required? | Notes |
|-------|-----------|-----------|-------|
| `id` | THROWS (alwaysRequired) | **YES** | |
| `title` | THROWS (alwaysRequired) | **YES** | |
| `type` | THROWS (alwaysRequired) | **YES** | Must be `EVENT` |
| `subtype` | SILENT DEFAULT (getOrDefault "") | NO | |
| `rarity` | SILENT DEFAULT (getOrDefault "COMMON") | NO | |
| `faction` | SILENT DEFAULT (getOrDefault "ANY") | NO | |
| `set` | SILENT DEFAULT (getOrDefault "PREMIERE") | NO | |
| `imageKey` | SILENT DEFAULT (getOrDefault id) | NO | |
| `text` | SILENT DEFAULT (getOrDefault "") | NO | |
| `cost` | SILENT DEFAULT (getOrDefault "") | NO | Optional |
| `mercenary` | SILENT DEFAULT (getOrDefault "") | NO | |

**Finding (B5-2071)**: Zero violations. No type-specific required fields.
---

## Summary: Required Fields by Card Type

| Card Type | Loader-Required Fields (THROWS/VALIDATED) | Silent-Default Fields (Optional) |
|-----------|------------------------------------------|----------------------------------|
| **AGENDA** | `id`, `title`, `type`, `isMajorAgenda`, `winCondition` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |
| **LOCATION** | `id`, `title`, `type`, `influencePerRound` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `military` |
| **FLEET** | `id`, `title`, `type`, `military` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary`, `fleetClass` |
| **CHARACTER** | `id`, `title`, `type`, `diplomacy`, `intrigue`, `psi`, `leadership`, `isAmbassador` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |
| **CONFLICT** | `id`, `title`, `type`, `conflictType`, `influenceReward` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary`, `participation` |
| **AFTERMATH** | `id`, `title`, `type`, `triggerCondition` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |
| **ENHANCEMENT** | `id`, `title`, `type`, `diplomacyBonus`, `intrigueBonus`, `psiBonus`, `militaryBonus`, `leadershipBonus` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |
| **GROUP** | `id`, `title`, `type` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |
| **EVENT** | `id`, `title`, `type` | `subtype`, `rarity`, `faction`, `set`, `imageKey`, `text`, `cost`, `mercenary` |

---

## Loader Contract Gaps (Cross-Cutting)

1. **AGENDA**: `isMajorAgenda` and `winCondition` are validated as required but **absent from the expected-key set** (lines 488-490, 517-521) — a typo in either would be reported as UNKNOWN FIELD, not validated.
2. **CONFLICT**: `subtype` and `conflictType` duplicate the same datum with **no cross-check** — divergence would be silent.
3. **AFTERMATH**: `triggerCondition` enum values not exhaustively documented in loader; `parseConflictType` logic reused but values differ.
4. **COMMON FIELDS**: `cost`, `mercenary` appear in expected set for all types but are optional — blank values are legal but noisy.
5. **UNKNOWN FIELD NOISE**: `validateFields` prints UNKNOWN FIELD to stderr for every record on every load (B5-2071 F1) because the expected set is incomplete.

---

## Recommendations for New Card Authors

1. **Always include all loader-required fields** — omission drops the card silently.
2. **Include all silent-default fields explicitly** — even if optional, explicit values prevent confusion and make diffs readable.
3. **For AGENDA**: Set `isMajorAgenda` correctly per subtype (MINBARI → true).
4. **For CONFLICT**: Ensure `subtype` = `CONFLICT_` + `conflictType` (e.g., `CONFLICT_MILITARY` ↔ `MILITARY`).
5. **For CHARACTER**: All four ability ratings must be non-negative integers; `isAmbassador` must be boolean.
6. **For ENHANCEMENT**: All five bonus fields required; use 0 for no bonus, negative for penalties.
7. **Avoid fields not in the expected set** — they generate UNKNOWN FIELD noise on every load.
8. **Run the conformance suite** after adding cards — it exercises the loader contract.

---

## Files

- Proposal: `docs/proposals/new-card-onboarding-field-checklist-proposal.md`
- Report: `.agent/REPORTS/2026-10-03-Cline (claude-4-sonnet)-B5-2225.md`
- Pattern: `.agent/PATTERNS/Cline (claude-4-sonnet)/2026-10-03-field-checklist-consolidation-lesson.md`

---

## Gate Status

- Build gate: Not re-run (read-only proposal, no src/ or resources/ edits)
- Java 6 grep: Clean (no src edits)
- Compile: Not required for docs-only deliverable
**Finding (B5-1981)**: Row-named fields `influenceCost`, `promotionCost`, `supportingRole`, `abilities` are **not card JSON fields** — they are engine-computed or player-zone concepts. Census restated against actual schema (B5-1025) is green.