---
document:
  title: "Seed report B5-1411..B5-1433 (12 OPEN rows, seeding only)"
  status: "Report"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Seed report — 12 rows B5-1411..B5-1433

Seeding pass only, on user order ("seed lots of new tasks"). No claim held and
none of the 12 rows worked. Own rows only, foreign rows and claims left
byte-identical.

## Pre-write verification (exact working tree)

- `javac -version`: 1.8.0_292. `b5ccg/compile.bat`: GREEN (Build successful,
  one expected bootstrap warning).
- `run-dup-census.ps1`: PASS, 0 duplicate IDs.
- `validate-heartbeats.ps1`: standing-red, 122 conforming vs 9 non-conforming
  plus 3 identity collisions (frozen residue, untouched).
- `ledger-query.ps1 -Status OPEN`: 29 rows, all 7 pipes / doubleLead no,
  0 rows under a non-stale claim.
- `git diff --stat`: 35 files; src slice 791 insertions / 24 deletions over
  7 files (AIPlayer 167, DeckLoader 182, CardEffects 149 new,
  GameController 25, RulesEngine 7, MainWindow 221, GameBoardPanel 64).
- Fresh finding grounding the wave: the working tree deletes the singleton
  `"timing": "ANY"` key on `de_event_armistice` in deluxe.json (the key the
  B5-0311 addendum called dead metadata and B5-1111 called the 1 genuine
  timing key owned by B5-1101).

## ID allocation

All 12 IDs (odd, 1411..1433) pre-checked at zero ledger matches and zero
claim files seconds before the append, diverged non-adjacent above the
B5-1405 max with a two-slot buffer per B5-0622. Pre-append ledger hash
B96B9B041DD980E422F0922BD5549C7F80B4D1F7417999B1CB45381D7DDF23B5 at
1583259 bytes so a concurrent append is detectable by re-hash.

## Post-write receipts

- `run-dup-census.ps1`: PASS, 0 duplicate IDs.
- `ledger-query.ps1 -Status OPEN`: 41 rows; all 12 new rows read
  7 pipes / doubleLead no / UNCLAIMED / reportable.
- Heartbeat `opencode (muse-spark-1.3) seed-07.json` written with
  `live_claims: []`.

## The 12 rows (all OPEN, no commit, no push, report plus pattern plus heartbeat)

- B5-1411 deluxe timing-deletion classification (read-only, fences B5-1101)
- B5-1413 AIPlayer hunk triage (read-only, fences B5-1171/B5-1177)
- B5-1415 engine hunk triage (read-only under live B5-1047 claim)
- B5-1417 ui hunk triage (read-only, fences B5-1171/B5-1177)
- B5-1419 single-round smoke profile vs 8-action baseline (execution only)
- B5-1421 Java 6 forbidden-construct sweep over the 7 dirty files (grep only)
- B5-1423 full conformance run as raw log for B5-1088/B5-1329 (no tally)
- B5-1425 DECISIONS newest-at-bottom remediation proposal (docs, fences B5-1135)
- B5-1427 pattern-store newest-per-namespace index (report-only)
- B5-1429 root stray-markdown audit (fences B5-1405/B5-0835)
- B5-1431 census-crosscheck run and classification (no tool edit)
- B5-1433 armistice timing-key disposition, gated after B5-1047 DONE plus
  B5-1101 unblocked (the only gated row; rest claimable now)

Reusable lesson: seed from the diff the tree actually carries and the wave
lands without collisions or orphans.
