---
title: "B5-1349 - Post-chain effect-dispatch backlog (ranked), merged from four completed censuses"
author_llm: me-so-poor
task: B5-1349
utc: "2026-10-01T00:24:11Z"
status: "proposal"
---

# B5-1349 — Post-chain effect-dispatch backlog (ranked), merged from four completed censuses

**Scope:** Docs only. No src, conformance section, or card data edits; no effects
implemented; no commit, no push. This is a proposal under AGENTS §6, never truth
until merged and compiled.

> **B5-1047 deferral applies to every entry below.** The engine is one-writer at a
> time and B5-1047 holds the live engine claim. Nothing here is claimable work; it is
> a ranked queue for whoever picks the chain up.

## Inputs merged

| Census | Source | What it contributes |
|---|---|---|
| B5-1125 registration | `.agent/REPORTS/2026-09-30-Cline (space-bunny) b5-1125-B5-1125.md` | Registered vs unregistered per type, in-play reach, and the R×S×P criterion with its seams |
| B5-1153 + **B5-1347** aftermath triggers | `.agent/REPORTS/2026-09-30-me-so-poor-B5-1347.md` | 14 trigger values over 117 aftermath records; **corrected** exposure 113 undispatched over 14 values (not 103/12) |
| B5-1173 FREE_SPONSOR waiver | `.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 10-B5-1173.md` | Waiver table half-wired; zero `FREE_SPONSOR`; 11 other "free" promises outside sponsor composition |
| B5-1341 generic PLAY_CARD dispatch | `.agent/REPORTS/2026-09-30-opencode (big-pickle) loop1-B5-1341.md` | Seven construction paths closed by callers, sink unguarded; ranked engine hand-off |

**Correction carried forward.** B5-1153 reported "12 values covering 103 records
undispatched". B5-1347 measured **113 records over 14 values**, because
`WON_DIPLOMACY` is only half covered — `aftermath_united_front` and
`de_am_united_front` appear in neither dispatch set. The 103 reconciles as B5-1153's
own 101 plus those two records, so it was never the undispatched total. Any ranking
built on 103/12 needs re-reading.

## Ranking

The ordering below is B5-1125's R×S×P score (R = unregistered in-play copies, S =
silent/substituted/absent fallthrough, P = whether an id-keyed seam already exists),
with the trigger and dispatch corrections layered in. P dominates: P=3 means the
next entry extends an existing table; P=1 means building a subsystem.

### 1. CONFLICT — score 27 (R 3, S 3, P 3)

- **Census source:** B5-1125, first slice. 29 unregistered in-play copies across 13 distinct cards.
- **Record ids:** `conf_kidnapping` (x4), `conf_border_raid` (x4), `conf_limited_strike` (x2), `conf_trade_pact`, `conf_telepathic_scan`, `conf_test_their_mettle`, `conf_sabotage`, `conf_euphrates_treaty`, `conf_gunboat_diplomacy`, `conf_stop_hostilities`, `conf_sleeper_personality`, `conf_supplement_security` (x1 each). `conf_affirmation_of_power` is fully covered.
- **Dispatch table:** extend `CONFLICT_LOSER_DISCARD` / `CONFLICT_LOSER_INFLUENCE` / `CONFLICT_WINNER_STEAL` in `CardEffects.java`.
- **Smallest-fix shape:** four clause *shapes*, not 13 rows — rotate a target (`conf_kidnapping`, `conf_sabotage`), cancel an active conflict (`conf_stop_hostilities`), participation restriction (`conf_limited_strike`), round-scoped modifier (`conf_euphrates_treaty`, `conf_supplement_security`). The `influenceReward` clause already lands generically at `RulesEngine.java:348`, so do not re-add it.

### 2. EVENT — score 18 (R 3, S 2, P 3)

- **Census source:** B5-1125. 39 unregistered in-play copies — the largest in-play gap. B5-1127 independently named the top cluster by crude promise magnitude: `event_level_the_playing_field`, `event_support_babylon5`, `event_liquidating_assets`, `event_shadow_strike`, `event_long_term_investment`.
- **Record ids:** the 150 unregistered EVENT records; the five named above are the ranked head.
- **Dispatch table:** `CardEffects.applyPlayEvent`.
- **Smallest-fix shape:** batch by promise shape (rotation, influence, draw, phase modifier) rather than one `put()` per id.
- **Why second not first:** S costs it a point because the fallthrough is *substitutive* — an unregistered Event draws a card instead of doing its printed effect, and that is logged. If the next agent wants the highest play-visibility defect rather than the cheapest seam, this is the row to take, and B5-1125 explicitly calls that a legitimate reading of "highest-value".

### 3. AFTERMATH — score 18 by type, exposure corrected by B5-1347

- **Census source:** B5-1153 census, **B5-1347 recount**. 117 records, 14 values, **113 undispatched**.
- **Record ids:** fully undispatched — `LOST` 33, `WON` 14, `WON_MILITARY` 10, `PARTICIPANT` 10, `WON_INTRIGUE` 6, `DIPLOMACY_PARTICIPANT` 4, `LOST_INTRIGUE` 4, `LOST_MILITARY` 4, `INTRIGUE_PARTICIPANT` 4, `LOST_DIPLOMACY` 4, `ANY` 2, `WON_PARTICIPANT` 2. Partly undispatched — `MILITARY_PARTICIPANT` 14 of 16, `WON_DIPLOMACY` 2 of 4 (`aftermath_united_front`, `de_am_united_front`).
- **Dispatch tables:** the two id-keyed sets `AFTERMATH_NEGOTIATED_SURRENDER` (`CardEffects.java:432-436`) and `AFTERMATH_DIPLOMATIC_ADVANTAGE` (`:439-443`) for bespoke effects; trigger-keyed `AftermathCard.isEligible` for eligibility. These are **different dispatch axes** and B5-1127 warned that conflating them is what makes the aftermath count read as "uncovered" when the 0 registration figure is correct.
- **Smallest-fix shape:** a `WON_DIPLOMACY` entry for the two `united_front` ids is the cheapest real closure on this axis — 2 records, one existing table, no new mechanism. Bulk trigger handling is a feature, not a row.

### 4. AGENDA — score 18 (R 2, S 3, P 3)

- **Census source:** B5-1125. 12 unregistered in-play copies.
- **Record ids:** from the B5-1125 per-type table; not re-enumerated here.
- **Dispatch table:** the agenda bonus tables in `CardEffects.java` (`AGENDA_DIPLOMACY_WIN`, `AGENDA_BANS_DIPLOMACY_AFTERMATH` show the shape).
- **Smallest-fix shape:** extend per-agenda bonus entries; agendas already have per-id tables, so each entry is a `put()`.

### 5. PLAY_CARD type guard — safety defect, ranks here on impact not reach

- **Census source:** B5-1341.
- **Finding:** all seven `PLAY_CARD` construction paths are closed by caller-side `instanceof`, but the sink is unguarded. `GameController.java:190-192` routes into `applyGenericCardPlay` (`:630-684`), which charges raw `card.getCost()` at `:636-641`, removes from hand at `:651`, and discards a `CharacterCard` at `:681-683` — no supporting-role entry, no promotion path. `submitHumanAction` (`:716-747`) and `RulesEngine.canPlayCard` (`:904-910`) are both type-blind, so the engine accepts the corrupting dispatch; proven by reflection injection, `threw=false`.
- **Record ids:** all 92 CHARACTER records in the pool.
- **Dispatch table:** `GameController.applyGenericCardPlay` (routing seam), `RulesEngine.canPlayCard` (predicate seam).
- **Smallest-fix shape:** one `CharacterCard` branch at the sink that refuses and keeps the card in hand, mirroring the existing `AgendaCard` arm at `:646-650`. Ranked follow-ons: decide whether `canPlayCard:904` is type-blind by design; narrow `GameAction.playAgendaFaceDown:123` from `Card` to `AgendaCard`. No caller changes needed — paths 1–7 are already closed.

### 6. CHARACTER / FLEET — score 9 each (P 1)

- **Census source:** B5-1125. CHARACTER 45 unregistered in-play copies, FLEET 28 — the two largest in-play populations.
- **Record ids:** all 92 CHARACTER / 44 FLEET pool titles.
- **Dispatch table:** none exists; extending would mean building a subsystem for persistent triggered abilities.
- **Smallest-fix shape:** none recommended. B5-1125's lesson is explicit — a gap with no dispatch surface is a missing feature, not a missing row, and ranking these by volume sends the next agent to build a subsystem instead of a table.

### 7. FREE_SPONSOR waiver — informational, no action recommended

- **Census source:** B5-1173. `WAIVER_EFFECTS` (`:44-45`) holds only `FREE_PARTICIPANT`; zero `FREE_SPONSOR` entries, so `sponsorWaiver` (`:48-52`) returns `NONE` for every card. This is correct code over an empty set, and `participantWaiver` (`:54-58`) is genuinely wired — the table is half-alive.
- **Record ids:** the 11 unwired "free" promises across 9 ids (`char_stephen_franklin`; `conf_court_the_rebellious` + twin; `conf_establish_base` + twin; `event_for_my_people`, `de_event_for_my_people`, `event_strategic_reassignment`, `de_event_strategic_reassignment`; `aftermath_enrage`, `de_am_enrage`) — all outside sponsor composition, so they belong to entries 1–3, not to the waiver table.
- **Dispatch table:** `CardEffects.WAIVER_EFFECTS`.
- **Smallest-fix shape:** populate nothing. Two receipts for whoever ever does: the premiere `conf_non_aligned_support` id is **absent from the deduped pool** (title-dedupe keeps the `de_` twin), so a premiere-id entry wires a dead key; and rulebook §Free (`:1154`) requires "immediately, any round, no cost, without rotating" — `sponsorWaiver` expresses only the cost half, so a naive `FREE_SPONSOR` entry would silently under-implement the rule.

### Not backlog

- `enh_mines_rt` is registered in `DAMAGE_ON_ATTACK` but absent from both card files — a stale registration, the inverse of this backlog's problem. Recorded by B5-1125; separate hygiene.
- `mer_metric_fixture` is a synthetic conformance fixture with a named consumer (`HeadlessConformanceTest.java:6013`, registered at `CardEffects.java:621`), deliberately not a card id.
- ENHANCEMENT / LOCATION / GROUP all score 3 with P 1 — feature work, not table rows.

## Read this before claiming anything

Four censuses, four different axes, and three of them can be mistaken for each other:

- **registration** (is this id a key in a dispatch table?)
- **trigger eligibility** (does this aftermath record become playable at all?)
- **cost composition** (does `sponsorCost` waive a third term?)
- **type routing** (can a `PLAY_CARD` carrying the wrong card type reach the generic sink?)

Only registration and type routing are coverage gaps. B5-1127's finding is the cautionary one: AFTERMATH's "0 registered" is *correct*, because the 117 records are keyed on `triggerCondition`, and calling them uncovered would be wrong in any future count.

## Method notes

- The aftermath headline numbers were confirmed by a second reader under a deliberately different method (structured parse keyed on `type` rather than key presence). That agreement was structural rather than evidential — both strategies provably select the identical 117 records — so the independent value came entirely from re-deriving coverage and exposure from source.
- `compile.bat` is not runnable from the repo root or `b5ccg/` by bare name on this platform; `cmd.exe /c "cd /d <abs>\b5ccg && .\compile.bat"` works. Recorded by B5-1125, confirmed again by this row's gate run.