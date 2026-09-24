---
document:
  title: "Authenticity migration design decision"
  status: "Proposal"
provenance:
  author_llm: {name: "GPT-6 Codex", version: "GPT-6"}
  assessor_llm: []
  last_modified_by_llm: {name: "GPT-6 Codex", version: "GPT-6"}
  created_date: "2026-09-24"
  last_modified_date: "2026-09-24"
---

# B5-0388 — Authenticity migration design decision

## Recommendation

Keep the Premiere and Deluxe card pool as the game's authored design layer. Do not replace its character statistics or game text wholesale with printed-card values. Treat the SNRPG database and other scans as research references, not as a source to bulk-copy into the canonical pool.

This is a design decision for the current project direction, not a claim that the pool is a faithful transcription. It does not change card data, engine behavior, or the canonical rulebook.

## Evidence

The B5-0355 audit compared 446 Premiere records against 1,729 SNRPG rows and validated its reference join against two scan-confirmed stat anchors. Its measured results were:

- 439/446 titles matched the reference. The four ambassador designs do not map uniquely to the printed reprint variants; seven other records also lack an exact title join.
- None of the 87 title-matched character stat blocks matched. The 352/439 aggregate stat matches were all zero-stat records, so they provide no evidence that playable character stats were transcribed.
- None of the 439 matched pool texts was identical to the corresponding reference game text; 438/439 had word-set similarity below 0.5.
- The pool text contains engine-facing vocabulary and compact effects. Replacing it would therefore change the game's rules inputs as well as its wording. The audit identified one unusually close text match, Commercial Telepaths, already handled separately by the B5-0385 IP-safe paraphrase.
- The earlier cost-only backfill was separable: all 209 unambiguous costs agreed with the reference, while no existing stat or text values were changed.

These findings support the current pool as a distinct game design. They do not establish that every current value is balanced or that the reference database is an authoritative scan for every reprint.

## Options considered

| Option | Advantages | Costs and risks |
|---|---|---|
| Adopt printed stats and texts wholesale | Moves toward printed-card fidelity where the reference mapping is reliable | Replaces 87 character stat blocks and about 439 texts; ambiguous reprints remain; engine effects depend on current text hooks; copyright and transcription risks; requires broad balance and regression work |
| Keep the current design layer | Preserves tested game behavior and card-to-engine hooks; keeps uncertain mappings out of canonical data | The pool must be described honestly as a designed adaptation, not an authentic card transcription |

The second option is recommended. Its tradeoff is explicit: this project prioritizes a coherent playable design over printed-stat fidelity.

## Operating rules for the design layer

1. Keep existing Premiere and Deluxe stat and text values as the current authored game data unless a separately claimed card-specific task changes them.
2. Do not bulk-import printed card text. Do not use a title match alone to choose among reprint variants or ambassador versions.
3. Keep additive metadata work, such as the cost-only backfill, separate from stat/text migration. New data fields need their own scope and validation.
4. Preserve engine-hook vocabulary and structured fields as behavioral interfaces. Wording cleanup must retain their semantics and pass the relevant conformance coverage.
5. Handle isolated accuracy or IP-safety fixes independently. B5-0385's Zack Allan title correction and Commercial Telepaths paraphrase do not imply approval for a wholesale migration.
6. Describe the pool in project materials as an authored design layer. Do not call it a transcription of the printed game.

## Conditions for revisiting printed fidelity

A later proposal may reopen this choice if the project explicitly changes its goal to printed-card fidelity and has reliable scans for the affected records. Before any wholesale migration task is claimed, it should include:

- A per-card source mapping, including set and reprint identity; ambiguous matches stay unresolved rather than guessed.
- A reviewed list of all affected stats and text fields, with unmatched and pool-unique cards handled explicitly.
- A semantics map from each existing engine hook to the proposed rules representation, plus an audit of behavior that cannot be represented by the engine.
- A regression plan covering card loading, existing card-effect behavior, deck construction, and game-level rule conformance.
- A rights-safe text plan based on licensed material or original paraphrase; the reference site is not permission to reproduce copyrighted card text.
- A separately claimed data task, approval through the normal proposal promotion process, and a green Java 6 build and relevant regression suite before the migration becomes canonical.

Until those conditions are met, the data owner should keep the current design layer and pursue only narrow, evidence-backed corrections.

## Scope and status

This proposal records the B5-0355 findings and recommends a project direction. It edits no source code, card JSON, or rulebook text. It remains a proposal and does not itself promote any data values to canonical truth.

## References

- `.agent/REPORTS/2026-09-23-freebuff-01-B5-0355.md` — text-authenticity audit and measured comparison.
- `.agent/REPORTS/2026-09-23-freebuff-01-B5-0385.md` — completed isolated title and IP-safety hygiene work.
- `docs/DECISIONS.md` — B5-0355 and B5-0385 decision records.
