---
document:
  title: "B5-0315 claim report — cost-field design proposal (C1/D13)"
  status: "Claim"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-21"
---

# B5-0315 claim report — cost-field design proposal (C1/D13)

## Repo state at claim time

- **`javac -version`:** `javac 1.8.0_292` (JDK 8; the only toolchain that accepts `-source 6`).
- **`git pull`:** not re-run at claim time — I re-baselined against HEAD `ca66ac5` ("Add human playtesting + joining conflicts report (docs/reports)") and confirmed the committed ledger there.
- **Committed ledger at `ca66ac5`:** B5-0001 → B5-0313 are DONE; OPEN rows are B5-0315, B5-0316, B5-0317, B5-0318. B5-0314 is VOID.
- **Claims dir on disk:** empty (only `README.md`). No live claim file exists for any OPEN row. (The committed tree has a `B5-0313.json`, but it is not on disk — `git status` shows no `.agent/CLAIMS/B5-0313.json`, and `ls .agent/CLAIMS/` returns only `README.md`.)
- **Freebuff-01 heartbeat on disk:** stale-by-12-hours (`utc: 2026-09-21T08:55:00Z`), but it records B5-0313 DONE with claim released and points at B5-0316/B5-0317/B5-0318 as the OPEN rows — consistent with the committed ledger. The B5-0313 report is committed. So B5-0313 is closed; this stale heartbeat is a leftover artifact, not a live claim.
- **Highest OPEN row with no live claim file:** B5-0315.

Note on my own working-tree state: at claim time my working tree has uncommitted `M .agent/TASK_LEDGER.md` (I previously set B5-0315 → DONE in an earlier turn this session, before HEAD moved), `M docs/DECISIONS.md` (I previously appended a B5-0315 entry), and `M .agent/HEARTBEATS/solar-pro4.json`. None of those uncommitted edits are authoritative. At claim time I anchor on the committed baseline at `ca66ac5`, where B5-0315 is OPEN with no claim. My uncommitted DONE row is stale (the commit re-opened B5-0315). I will re-close B5-0315 against the committed text.

## Task selection rationale (per `00_BOOT.md` step 4)

1. Read HANDOFF.md (done).
2. Read TASK_LEDGER.md (done, committed baseline at `ca66ac5`).
3. Checked `.agent/CLAIMS/` on disk — empty (no live claim files).
4. Highest OPEN row with no live claim = B5-0315 (B5-0313 is DONE in the committed ledger and has no claim file on disk).
5. B5-0316/B5-0317/B5-0318 are lower OPEN rows; B5-0315 is highest.

## Scope (read from committed TASK_LEDGER.md row `ca66ac5`)

> B5-0315 | OPEN | Cost-field design proposal, report-only (C1/D13): JSON schema + model field + double-cost rule wiring plan; NO implementation, proposal file only | design doc, no code/data edits | none | —

## What I read before writing the design doc

1. Commitment baseline: `git show ca66ac5:.agent/TASK_LEDGER.md` lines 40–58 (committed ledger rows B5-0313–B5-0318 + rules).
2. Commitment baseline: `git show ca66ac5:docs/DECISIONS.md` tail (committed decisions tail, including the Muse Spark fabrication-closure entry and the B5-0313-claimed note).
3. B5-0203 report (D13 note: "needs a cost field the model does not carry") — `.agent/REPORTS/2026-09-21-freebuff-01-B5-0203.md`.
4. B5-0305 report (D13 note re double-cost rule) — `.agent/REPORTS/2026-09-21-freebuff-01-B5-0305.md`.
5. B5-0311 report (C1: no cost field on any card in either file) — `.agent/REPORTS/2026-09-21-solar-pro4-B5-0311.md` (the C2 retraction is still on disk from my earlier re-read; not re-reading it here, it's already correct).
6. Rulebook §Anatomy item 2 (Influence Cost), §Influence, §V Sponsor a Supporting Card (double-cost / restricted-to-race clause), §V Promote a Character (cost + IC-count formula; sponsor-discount-does-not-apply-to-promote note), §V Build Influence (context only).
7. Model surface: `Player.influence` (single merged number, no apply/restore cycle — B5-0203 noted), `Faction.isPlayableBy` (D13 already fixed — NON_ALIGNED is a normal race), no `cost` field anywhere in `model/`.

## What the design doc contains (proposal only, no edits)

Same contents as my earlier B5-0315 proposal from this session (which I wrote before HEAD moved), reproduced here because the committed baseline re-opened the task:

- **Schema:** add `cost` (non-negative int) to the card base type the loader hydrates; loader maps missing key → 0; expose `getCost()` on Character/Enhancement/Group/Location/Fleet only (not Conflict/Aftermath/Agenda today). Backward-compatible because no card currently has the key.
- **Double-cost + race restriction stay Faction-side computations**, not card properties.
- **Wiring plan** for where Sponsor/Promote would consult `cost`, with the explicit flag that both actions are NOT YET IMPLEMENTED (no GameAction.Type, no processAction branch), so the field has zero runtime effect until they exist.
- **Influence-pool semantics** and **card-race definition** flagged as open prerequisites for whoever implements Sponsor/Promote.
- **Explicit "do NOT" list:** do not mass-assign cost values now (no IP-safe source), do not redesign the influence pool, do not add a "double cost" boolean to the card.

## Finish plan (per `00_BOOT.md` step 8)

After writing the design doc:
1. Patch TASK_LEDGER.md B5-0315 row OPEN → DONE (anchored on committed text at `ca66ac5`, not on my stale uncommitted row).
2. Append docs/DECISIONS.md (anchored on committed tail).
3. Write `.agent/REPORTS/2026-09-21-solar-pro4-B5-0315.md` (the proposal doc).
4. Delete `.agent/CLAIMS/B5-0315.json`.
5. Refresh `.agent/HEARTBEATS/solar-pro4.json` (tasks_in_progress: []).

No `compile.sh` re-run needed (no `b5ccg/src/` file touched); build gate stays green from prior verification.

