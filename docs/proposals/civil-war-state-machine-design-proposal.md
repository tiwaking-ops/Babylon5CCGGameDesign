---
document:
  title: "Civil War state machine — race-to-factions split, feasibility and design proposal"
  status: "Proposal (never truth until merged + compiled)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Civil War state machine — design proposal (B5-0639 remainder R13)

Task: **B5-0669**. Report-only deliverable — this file plus a
`docs/DECISIONS.md` entry. No model, engine, ai or ui code was touched.

## 1. The rule, verbatim (rulebook :990–:1009, §VI "Civil War")

- **:992** — "If the tension of any faction toward another faction of the same
  race is at 5 at the end of any turn, then that faction could play a
  'Declaration of War'. This causes every faction of the race to enter a state
  of Civil War. Any other effect which triggers a war between two (or more)
  factions of the same race has also initiates this state of Civil War."
- **:994** — "The unrest of every faction of a race increases by 1 when a race
  enters Civil War. Each faction of a race in Civil War is in a state of War
  with each other faction of that race."
- **:996** — end-war targeting with more than two factions: the card must end
  the conflict between the two factions with the **highest (sum of) tensions
  toward each other**; exception: a card ending a war *you are involved in*
  must target the faction with the highest tension toward **you**; ties → the
  card's player chooses.
- **:998** — "If the war ends, the Civil War ends … and the unrest of every
  faction of that race is lowered by 1."
- **:1000** — the split: "its various factions are from that point forward
  treated as separate races, for as long as the Civil War lasts. This applies
  to cards which alter tensions, influence … unrest, states, etc. … Each other
  race now has a tension toward each of the new 'races' equal to their old
  tension toward the race as a whole. While in Civil War, tension for each
  'new race' is tracked individually. If the Civil War ends other than due to
  unconditional surrender, the tensions merge; **average tension (rounded up)**
  becomes the new tension for the reunified race."
- **:1002/:1004** — ongoing states (war, alliances) that war would cancel are
  *suppressed* during Civil War and restored at its end; states entered *during*
  Civil War are suppressed while the race is reunified and re-activate on a
  later split.
- **:1006** — exit by unconditional surrender: when only one faction of the
  race remains, the Civil War ends, unrest lowers, pre-war states restore,
  Civil-War-era states cancel (subject to the re-initiation clause).
- **:1008** — "War, and any state not cancelled by war, transfer from a unified
  race to every breakaway faction, and from any breakaway faction to the
  unified race."

Supporting definitions the machine depends on: **:956** "A dual race is any
race represented by more than one player faction"; **:968** unrest is per
faction, race tension is per race, plus per-faction tension toward each other
faction of the same race; **:972** same-race faction tension begins at 2;
**:974** same-race tension ≤ 3 (and no Civil War) grants an automatic
Non-Aggression state; **:278** unrest starts at 1; **:888** Non-Aligned unrest
starts at 2; **:280** tension and unrest range 1–5 with hard clamps both ends.

## 2. Premise re-verified against the code (my own greps, this pass)

- `civil war` — **0 occurrences** over `b5ccg/src/b5ccg` (case-insensitive,
  all Java).
- `unrest` — **0 occurrences** over the same tree. Neither the state machine
  nor the axis it shifts exists anywhere in the engine.

## 3. Structural finding: unreachable in the standard game, not merely unimplemented

This is the load-bearing correction to the row's framing, and it changes the
shape of the work:

1. **Civil War is not gated behind an expansion chapter — it is gated behind
   *multi-faction play*.** `:956` (Dual Races) and `:990` (Civil War) both sit
   under **§VI Additional Rules** (beginning :741). The row's parenthetical
   inferred an expansion gate from "the same multi-faction engine that rulebook
   :980 Joint Effects already assumes" and from the Psi Corps row's
   "expansion-structure" note; greping the section headers shows the nearest
   literal "war" before :990 is **:1018 "More Factions"**, a Great-War-era
   *reference inward*, not a gate. (Recorded here as a report note — the
   B5-0639 report is not edited, per supersede-never-rewrite.)
2. **But reachability still fails on a different axis.** A dual race requires
   more than one player faction of one race (:956). In the standard game each
   race has exactly one faction, so there is no "other faction of the same
   race" to hold tension toward, no unrest differentiation, and no Civil War
   trigger. The whole R13/R11/R12 cluster is **unreachable until the engine
   supports multiple player factions per race** (Home Factions :950, alternate
   factions :956, More Factions :1018).
3. The code confirms the single-faction assumption structurally:
   `TensionMatrix.raiseTension` **no-ops when `source == target`** (reference
   equality), so a same-race tension slot cannot even be expressed today;
   `GameState.isAtWar(Faction)` bridges faction→race identity 1:1
   (GameState.java:92–96); `Conflict.java:140–141` compares
   `initiator.getFaction()` to `p.getFaction()` as *race* identity.

Consequence for seeding: R13 is a **design-gated** remainder, not an
implementation-sized one, and its prerequisite (multi-faction identity) is
itself larger than R13's own state machine. Nothing below seeds code.

## 4. Card-data gate (measured, not assumed)

- "Declaration of War" exists in both files — but as `event_declaration_of_war`
  with text "Play at the start of a round. For this round, all Military
  conflicts reward 1 extra Influence." A **title-only collision** with the
  :992 trigger name; it does not invoke any war-declaration mechanic.
- "Unrest" appears only as `enh_babylon5_unrest` ("Babylon 5 Unrest"), whose
  text raises Influence *costs* — unrelated to the unrest axis.
- Net: **zero cards in either file invoke or reference the unrest axis, the
  same-race tension track, or any Civil War mechanic.** Unlike R7 (whose
  grounding example did not survive the data), here the rulebook section is
  self-contained *engine* law — but it still has no exercising card today, so
  even after the engine slice lands, conformance sections must use synthetic
  fixtures, and any *card-visible* behaviour waits on data.

## 5. The state machine, as designed

### 5.1 State

Per race (not per faction): `UNIFIED | CIVIL_WAR`, plus the Civil-War-era
entry round and the pre-split tension snapshot. Unrest is per faction
(`:968`, `:278`): `int unrest = 1` on `Player` (2 for Non-Aligned, `:888`).
The unrest axis is **not** the tension matrix and must not be modelled as one
— it clamps 1–5 per `:280` and is per-faction from day one.

### 5.2 Where same-race tension lives (the identity problem)

`TensionMatrix` keys on the `Faction` enum with `source == target` guarded
away. Same-race tension needs (faction A of race R → faction B of race R).
Options measured:

- **(a) Reuse the enum with synthetic per-player instances** — rejected: every
  consumer does reference-equality on the enum (the file's own ⚠️ header
  documents this), and synthetic instances would corrupt every `==` site.
- **(b) A parallel `SameRaceTensionMatrix` keyed on stable faction/session
  identities** — recommended. It leaves the landed race-level matrix byte-
  identical (B5-0358's shipped semantics untouched), keeps the clamping and
  war-pair machinery copyable, and isolates the new axis behind its own type.
- **(c) Fold both axes into one matrix keyed on `RaceIdentity`** — cleanest
  end-state, largest blast radius today (every `getTension`/`enterWar` caller
  retypes); belongs to the multi-faction prerequisite, not to R13.

### 5.3 The split itself

"Civil War ⇒ factions are separate races" is a **data-structure change**:
race identity must become `RaceIdentity → Set<FactionIdentity>` instead of the
current 1:1 `Faction == race`. Existing per-race assumptions this invalidates
(each needs an explicit migration decision): `Faction.isPlayableBy` race
gating; the `Conflict.java:140–141` race comparisons; `GameState.isAtWar`'s
faction→race bridge; the tension matrix's race-keyed rows; racial fleet /
card-count limits (`:1000` keeps those race-wide *across* the split); and the
victory paths' non-forfeited-player scans (Civil War does not change victory,
but split-faction influence effects interact with every `conflictTotal`).

During Civil War: external races carry their old race-level tension toward
**each** new "race" (`:1000`); same-race factions are pairwise at war
(`:994`); war-cancelled states are suppressed, not destroyed (`:1002`).

### 5.4 Exit and merge arithmetic

Exit by war end (`:998`) or by unconditional surrender to one remaining
faction (`:1006`). On non-surrender exit: **merge tension per target race as
the rounded-up average** of the split factions' individually tracked tensions
(`:1000`); unrest −1 for every faction of the race; states restore/cancel per
`:1002/:1004/:1006`. The rounded-up-average rule and the `:996` end-war
targeting rule (highest mutual tension, self-involvement exception, ties to
the card's player) are the two clauses most likely to be implemented wrong and
must each carry a dedicated conformance check when this lands.

## 6. Which B5-0639 remainders collapse into this one

As the row required, stated explicitly — **R11, R12 and R14 are not
separately seedable**:

- **R11** (same-race faction tension + Non-Aggression auto-state) is the
  *substrate* of 5.2 — same matrix, same identity prerequisite, plus `:974`.
- **R12** (Joint Effects, `:980`) is the *steady-state* of the same split —
  military-conflict influence loss spills to every faction of the target race
  exactly while the race is UNIFIED, and stops when CIVIL_WAR splits it.
- **R14** (Psi Corps Conspiracy Marks) needs a Psi Corps *faction of the
  Human race* to exist — i.e. the identity machinery of 5.2/5.3 — before its
  sponsor gate is even expressible. (Note, per §3: :1022 follows :1018 "More
  Factions", an immediate-adaptation rule, not an expansion gate; the sponsor
  gate itself is then ordinary mark-count logic.)

All four need the identical split architecture; four rows each implying their
own would be four partial architectures, exactly as the row warned.

## 7. Prerequisite chain and implementation order (when a human orders it)

1. **Multi-faction identity slice** — `RaceIdentity`/faction-session identity,
   dual-race setup (Home Factions), alternate-ambassador rules (`:956–:962`:
   unsponsorable, no assistant, no council vote, no asylum, removed on
   unconditional surrender). This is the big one; nothing in R11–R14 exists
   without it.
2. **Unrest axis** (per-faction int, 1–5 clamp, start 1 / 2 for LoaW) +
   same-race tension substrate (5.2 option b) + Non-Aggression auto-state.
3. **Civil War state machine** (5.3/5.4) with entry triggers per `:992`
   (tension-5 at end of turn; declaration; any same-race war effect).
4. **Joint Effects** (R12) once the split exists.
5. **Psi Corps sponsor gate** (R14) once identity exists.

**Implementable with NO card-data change:** steps 2–4 in full (engine law,
synthetic fixtures — MJR/ORD precedent). **Data-gated:** any card-visible
effect on these axes (no pool card references them, §4); the `:992` named
trigger "Declaration of War" would need a real card or an engine action
decision, which is a human data question.

## 8. Known divergences this design must not silently absorb

- The engine clamps tension **0–5** (TensionMatrix) while the rulebook states
  **1–5** for tension *and* unrest (`:280`), and unrest starts at 1/2, not 0.
  A new unrest axis must follow the rulebook; the pre-existing tension-clamp
  divergence is B5-0358's shipped semantics and is flagged here, not changed.
- Two distinct `Faction` types already exist (bare enum vs wrapper, per the
  TensionMatrix header ⚠️). Civil War multiplies identity axes; any
  implementation that papers over that distinction will corrupt war checks.

## 9. Recommendation and scope of this document

**Do not implement, and do not seed code rows on R11/R12/R14.** The single
gating decision is the multi-faction identity slice (§7 step 1), which is a
structural commitment the engine has so far deliberately avoided (single
`Faction` identity everywhere). Until a human orders that slice, R13's
correct state is *designed and parked*, with this document as the bar any
implementation must clear. No code was changed, no Power/unrest field added,
no dependent slice seeded; all evidence in §2–§4 was produced by commands run
this pass.

---
*Reusable lesson: a state machine whose actors cannot exist is not a backlog
item — find the identity prerequisite that makes its actors possible, and size
*that*; the machine itself is then a sequel, not a starting point.*
