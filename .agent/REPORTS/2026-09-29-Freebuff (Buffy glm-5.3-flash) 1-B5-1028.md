---
document:
  title: "B5-1028 close-out — disposition of the stuck proposal pool"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1028 — Disposition of the proposals sitting at bare `Proposal`

Census 2026-09-29T08:53Z (ls + status-field read of every file): **27 proposal
files in `docs/proposals/`, 4 terminal** (ADOPTED b5-0388, REJECTED
tool-rule-convergence, APPROVED agent-instance-identity, APPROVED-as-design
negative-power-split), **23 non-terminal** (row swept 25/22; the pool moved —
negative-power-split went terminal and two new proposals landed). Ages
0–8 days; none stale by the row's own threshold once mtime is read (the pool
is actively edited, not abandoned). No status field was changed; dispositions
below are a report, per row scope.

## Disposition, one line each

**Agentic-infrastructure group** (these decide coordination, not the game):

1. `assessor-list-compaction-proposal.md` — **READY, already promoted** — its
   convention is in force inside AGENTS.md §1a and Guidelines.md verbatim; the
   status field is stale, the content won. Needs a human "adopted" stamp, not a
   ruling on merits.
2. `claim-liveness-protocol-proposal.md` — **READY, already promoted** — the
   three-signal liveness rule is in CLAIMS README + HEARTBEATS README
   (B5-0659), i.e. the proposal's content was adopted under its own id;
   same stale-stamp situation.
3. `heartbeat-schema-proposal.md` — **READY, already promoted** — binding
   schema lives in HEARTBEATS README + validator (A1 amendment included);
   same class.
4. `live-repair-aware-ledger-census-protocol.md` — **READY, already
   promoted** — the claims-first suppression is implemented in the shared
   census tools and cited by 00_BOOT step 4 (B5-0657); same class.
5. `compile-verdict-in-heartbeat-proposal.md` — **DEAD** — self-declared
   author-only convention, superseded by the binding schema's `javac` field;
   nothing left to adopt.
6. `heartbeat-released-state-ruling-proposal.md` — **DEAD (answer
   recorded)** — the validator still exits 1 on the one `released` file; my
   boot logging confirmed the README's no-edit rules (R5/R6) make
   ratification moot; the honest terminal is the validator finding standing,
   not a schema change.
7. `2026-09-29-heartbeat-retirement-policy.md` — **WORTH A HUMAN RULING** —
   measured 83%-tombstone store, archival-only mechanism, and it composes
   with (but does not require) #5/#6 cleanup; a real, small, reversible
   decision.
8. `2026-09-29-orphan-claim-census-footer-proposal.md` — **WORTH A HUMAN
   RULING** — makes 00_BOOT step 6 machine-checked in a tool every close-out
   already runs; low risk, real defect class (B5-0622) behind it.
9. `2026-09-28-solar-pro4-free-B5-0743.md` (backup-pointer convention) —
   **WORTH A HUMAN RULING** — two full ledger-truncation incidents already
   happened; the .bak convention exists de facto; ratification is cheap.
10. `untrack-node_modules-one-reversible-commit.md` — **WORTH A HUMAN RULING**
    — verified current: 67,258 tracked node_modules files; a one-commit
    reversible action awaiting exactly the human sign-off it asks for.

**Game-design group** (candidates for engine work, all proposal-only):

11. `b5-0360-economy-modeling-proposal.md` — **BLOCKED-ON-D6/D9** (prio:
    WORTH A RULING only after D6/D9) — self-declared prerequisite: "B5-0342
    (D9) must be accepted/implemented first"; D9 not implemented (single
    `Player.influence` integer, no pool split in src).
12. `d6-unlimited-actions-design-proposal.md` — **READY (waiting)** —
    well-shaped, prerequisite-free, and verifiably unimplemented
    (`Player.resetActions():411` still `actionsLeft = 1`).
13. `d9-rating-vs-applied-influence-design-proposal.md` — **READY (waiting)**
    — same, and it is D6's hard ordering dependency (rulebook-cited).
14. `d10-d11-bonus-layer-expiry-design-proposal.md` — **ALREADY IMPLEMENTED
    (status stale)** — `StatBonus.java` carries `expiry`, `floor`,
    `psiFromZero`, `cumulative` verbatim; consumed in `Player.effectiveStat`.
    Nothing left to rule on.
15. `b5-0483-minimum-1-bonus-floor-design-proposal.md` — **ALREADY IMPLEMENTED
    (status stale)** — `StatBonus.floor` + `Player.effectiveStat:371–389`
    clamp `Math.max(raw, min(printedBase, floor))`; data verified
    (enh_censure −2, no floor field — floor is engine-side by design).
16. `conflict-participation-restrictions-data-proposal.md` — **ALREADY
    IMPLEMENTED (status stale)** — `Participation.java` + DeckLoader wiring
    (B5-0336) exist; my B5-1037 pass verified all 7 data fragments map and
    execute end-to-end.
17. `war-conflict-participation-rules-proposal.md` — **READY (waiting)** —
    `WarKind` exists and war conflicts fire (B5-0784-era probes), but the
    tension/war targeting and all-supported-uncontested clauses remain
    proposal-side; shape is good.
18. `civil-war-state-machine-design-proposal.md` — **ALREADY IMPLEMENTED
    (status stale)** — `CivilWarState.java` exists and is consumed by
    RulesEngine/AIPlayer/UI/conformance probes (B5-0691 lineage).
19. `tiebreak-agenda-victory-design-proposal.md` — **ALREADY IMPLEMENTED
    (status stale)** — `VictoryPath.AGENDA_CONDITION` + the five-path ordered
    evaluation exist (B5-0663/B5-0641 lineage).
20. `endgame-stall-risk-followup-proposal.md` — **READY (waiting)** —
    re-measures the tiebreak stall against surrender/computed-Power/Civil-War;
    depends only on already-landed mechanics.
21. `b5-0422-pass-bias-cascade-design-proposal.md` — **WORTH A HUMAN RULING
    (design-tension)** — measured tension: AIPlayer EASY currently passes
    ~20% (`rng.nextInt(10) < 2`, line 656), far from the 53% the proposal
    argues down from; the premise has moved, the question (retune band vs
    keep) is a genuine design judgement.
22. `2026-09-25-solar-pro4-free-B5-0422.md` — **DEAD (superseded twin)** —
    stray 90-line draft of the same B5-0422 by solar-pro4:free, subsumed by
    the 207-line me-so-poor proposal (#21); kept as history, disposition only.
23. `2026-09-25-solar-pro4-free-B5-0428.md` — **DEAD (orphan draft)** — stray
    122-line station-influence draft; not a duplicate of b5-0360 (verified:
    293-line delta), it is the *only* station-ratings motion design; DEAD on
    staleness, WORTH A RULING only if station readouts ever resume.

## Narrow factual answers (also appended to docs/DECISIONS.md)

- **D6 status:** not implemented — `Player.java:37,411` `actionsLeft = 1`.
- **D9 status:** not implemented — single `influence` integer; no pool split.
- **D10/D11 (bonus layer):** implemented — `StatBonus(expiry, psiFromZero,
  cumulative, floor)`; consumers in `Player.effectiveStat`.
- **B5-0483 floor:** implemented — `StatBonus.floor` +
  `Player.effectiveStat:371–389`; enh_censure data carries `militaryBonus: -2`
  and no floor field in both sets.
- **Tiebreak/agenda victory:** implemented — `VictoryPath` = {LAST_STANDING,
  STATION_CONDITION_2, AGENDA_CONDITION, MAJOR, STANDARD}, ordered evaluation.
- **Civil War:** implemented — `CivilWarState.java` in model, consumed by
  engine/ai/ui/probes.
- **node_modules:** exactly **67,258** files tracked — matches the proposal's
  premise number on current bytes.
- **Station ratings motion:** still absent — `Babylon5Station` is recorded and
  asserted in tests; no ratings-motion code (only reads) in src.
- **Duplicate check:** the two date-named 2026-09-25 files are NOT duplicates
  of the named proposals (git delta 205+/293+ lines respectively — they are
  distinct drafts; the tool's rename rendering is an artefact, noted so no
  later reader repeats that reading).

## Gates

No merge, no status-field edit, no src/data edit; DECISIONS entries limited to
the factual answers above; compile not required (no src touched; last gate
green at 08:47Z this session); claim released at close-out; no commit, no push.

## Reusable lesson

A proposal pool rots by implementation more than by neglect — the largest
disposition class was "already built, status field never moved," which no
sweep-by-age can find; disposition must diff the proposal against the tree,
not against the calendar.
