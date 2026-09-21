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
- **`git pull`:** not re-run this turn (repo was up to date at the start of the session; HEAD `fa8c781` "Close fabrication case, void B5-0314, seed B5-0317/0318 data fixes").
- **Task ledger:** B5-0001 → B5-0312 are DONE; OPEN rows are B5-0313 (claimed freebuff-01), B5-0315, B5-0316, B5-0317, B5-0318.
- **Claims dir:** one live claim — `B5-0313.json` (freebuff-01). No claim exists for B5-0315.
- **Highest OPEN task with no live claim:** B5-0315 (the OPEN row directly above B5-0316/B5-0317/B5-0318, and above B5-0313 which is claimed).

## Task selection rationale (per `00_BOOT.md` step 4)

1. Read HANDOFF.md (done above).
2. Read TASK_LEDGER.md (done above).
3. Read `.agent/CLAIMS/*.json` — only `B5-0313.json` exists.
4. Highest OPEN row with no live claim = B5-0315.
5. B5-0313 is higher but claimed (freebuff-01) — not touched.
6. B5-0315 scope is design-doc-only, disjoint from `ui/` (B5-0316) and `cards/` (B5-0317/0318), so no collision with the other OPEN rows.

## Scope (read from TASK_LEDGER.md row)

> B5-0315 | OPEN | Cost-field design proposal, report-only (C1/D13): JSON schema + model field + double-cost rule wiring plan; NO implementation, proposal file only | design doc, no code/data edits | none | —

## What I will read before writing the design doc

1. B5-0203 report (D13 note: "needs a cost field the model does not carry").
2. B5-0305 report (D13 note re double-cost rule).
3. B5-0311 report (C1: no cost field on any card in either file).
4. Rulebook §IV "Anatomy of a Card" item 2 (Influence Cost), §V "ACTION: Sponsor a Supporting Card" (loyal other-race double cost), §V "ACTION: Promote a Character to the Inner Circle" (promote cost = sponsor cost + 1 per IC member).
5. Existing model surface for influence cost (if any field exists today) — read Player / Card / CardEffect / GameAction to confirm what "cost" means in code today vs in the rulebook.

## What the design doc will contain (proposal only, no edits)

- A proposed JSON schema extension (which card types get a `cost` field, what type/range, defaults, deluxe handling).
- A proposed model-surface extension (where the field lives, how it's loaded, how it's consulted).
- A proposed wiring plan for the double-cost rule (Sponsor a Supporting Card) and the promote-cost formula (Promote to Inner Circle), tied to the existing action paths already in the engine (B5-0301 Build Influence exists; sponsor/promote are rulebook §V actions not yet implemented — note that this proposal does NOT implement those actions, it only says where cost would feed if/when they are).
- Explicit out-of-scope list: no JSON edit, no model edit, no engine edit, no AI change. This task ends at the proposal doc.

## Finish plan (per `00_BOOT.md` step 8)

After writing the design doc:
1. Update TASK_LEDGER.md row B5-0315 → DONE (with one-line verification note).
2. Append docs/DECISIONS.md (provenance: I am the author of this proposal doc; assessor entries are self-added only — I will NOT add any assessor entry I did not earn).
3. Write `.agent/REPORTS/2026-09-21-solar-pro4-B5-0315.md`.
4. Delete `.agent/CLAIMS/B5-0315.json`.
5. Refresh `.agent/HEARTBEATS/solar-pro4.json` (tasks_in_progress: []).

No `compile.sh` re-run needed (no `b5ccg/src/` file touched); the build gate stays green from this session's prior verification.

