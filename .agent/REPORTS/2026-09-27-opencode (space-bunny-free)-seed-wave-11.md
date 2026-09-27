---
document:
  title: "Seed wave 11 - red-tree triage, three verified follow-ons, seven seeds"
  status: "Report"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Seed wave 11 - 2026-09-27

Agent id: `opencode (space-bunny-free)`.
User order: *verify repo. seed lots of tasks. list open tasks.*
Machine: javac 1.8.0_292 (JDK 8).
Claims held this pass: **none**. No source, resource, data, proposal or tool file
was edited. The only shared-file writes are the ledger QUEUE paragraph plus seven
rows, this report, one DECISIONS entry, one pattern record and my own heartbeat.

## 1. Verification (gate-first, on the exact working tree)

| Gate | Result |
|---|---|
| `compile.bat` | **RED** - missing-symbol errors at `HeadlessConformanceTest.java:4831` and following |
| `validate-heartbeats.ps1` | 27/27 conforming, 27 distinct `agent_id`, 0 collisions |
| `ledger-query.ps1 -Status "*"` | all rows `pipeCount 7` / `doubleLead no` |
| `census-crosscheck.ps1` | CONSISTENT, 354 rows before seeding / 361 after |
| `run-queue.ps1 -DryRun` | exit 0, no warnings |

The red gate is **another agent's in-flight work, recorded not fixed**. The tree
carries uncommitted B5-0661 bytes: new untracked
`b5ccg/src/b5ccg/model/AsylumCharacterCard.java` plus edits to GameController,
RulesEngine, CharacterCard, GameAction, GameState, Player and a suite section
referencing engine APIs mid-landing. Fixing it would mean editing inside another
agent's scope; reporting it as repo state would be a B5-0615-class false gate
finding. The QUEUE 0629 lesson applies verbatim: a red reading on a tree with live
writers is the concurrent-writer signature, not repo state.

## 2. Census at seed time

Seven OPEN rows: B5-0660 suppressed-live-claim (Buffy, live - not a candidate),
B5-0661 STALE under the three-signal rule (started_utc 2026-09-27T06:38:15Z at 46
min vs 30 min TTL, owner solar-pro4:free heartbeat at 52 min, no report file),
plus five unclaimed (B5-0663, B5-0665, B5-0671, B5-0673, B5-0675). B5-0667 and
B5-0669 from wave 10 were already DONE by Buffy as docs-only proposals; their
DECISIONS entries were read this pass and are this wave's grounding.

## 3. The census moved twice while this pass ran - both disclosed

1. **B5-0661 was claimed by Buffy (glm-5.3-flash) at 07:23:30Z**, after my seed
   text was written but before close-out. The stale solar-pro4:free claim file was
   replaced with a scoped claim (surrender legality+execution, SUR section, plus
   the B5-0661 ledger row only). Whether the stale-claim reap was ledger-noted is
   the new owner's business, not this pass's. Consequence for this wave: B5-0685
   (stale-claim assessment) is now a stand-down probe by construction - its row
   text orders a fresh liveness census at claim time, which will read LIVE under
   Buffy, and orders standing down without touching anything in exactly that case.
   The row survives contact with the new facts because it was written against a
   re-read, not against my census.
2. **B5-0663 was claimed by solar-pro4:free at 07:26:16Z** (model + engine + suite
   section scope). Consequence: B5-0665's gate (claim only after B5-0663 DONE) now
   has a live owner working toward it, which is the gate functioning as designed.

Neither event touched my seven rows: post-write duplicate-ID census returned
**empty**, every new row reads `7` / `no` / UNCLAIMED / reportable, and both
census tools agree at 361 rows.

## 4. Grounding (absence proofs and artefact reads, this pass)

| Seed | Grounding |
|---|---|
| B5-0677 | B5-0667 DECISIONS entry rules Power COMPUTED (`getPower() = getInfluence() + POWER-tagged StatBonus`), never stored; `getPower` over `b5ccg/src` = **0** hits |
| B5-0679 | B5-0661 lands SURRENDER/asylum; `surrender`+`asylum` over `b5ccg/src/b5ccg/ai` = **0** hits |
| B5-0681 | B5-0669 proposal :203-204 states steps 2-4 implementable with NO card-data change on synthetic fixtures; `civil war`+`unrest` over `b5ccg/src` = **0** |
| B5-0683 | 0489/0500/0643 precedent; gated so one sweep covers the whole chain |
| B5-0685 | the section-2 STALE reading with all three timestamps |
| B5-0687 | guide section-6 history ends at part 26 (`playtest-guide.md:752`), so part 27 is next |
| B5-0689 | B5-0647 precedent; gated on everything so the checkpoint stays ordered |

Deliberately NOT seeded: the tiebreak-agenda-victory recommendation, because the
QUEUE 0654 note records its Option B as conditional on playtest evidence rather
than a pending decision. Nothing on B5-0388 (ruled) or the B5-0454 data backlog
(parked). No external libraries anywhere, so the human gate is untouched.

## 5. Composition

Three code rows (0677 model+engine+PWR, 0679 ai+SUR-AI, 0681 engine+model+CWR),
one execution-only re-sweep (0683), one coordination-scope assessment (0685), one
docs-only refresh (0687), one git-only checkpoint (0689). Every code row is gated
on B5-0661 DONE - it owns the engine, model and suite files right now - with the
suite file serializing lowest-number-first. IDs continue the odd spacing so a
concurrent even-sequence seeder cannot collide.

## 6. Reusable lesson

Write gate clauses against a **re-read at claim time**, never against the seeding
pass's census. B5-0685 was written while B5-0661 was stale and is being closed
while B5-0661 is live, and it is still correct - because its operative sentence is
"re-census liveness FRESH at claim time and act on that reading, not on this
row's". A seeded row outlives the facts it was seeded from within minutes on a
tree with live writers; the only part of the row that survives is the part that
orders the worker to measure again.
