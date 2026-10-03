$entry = @"
## 2026-02-10 - Cline (claude-4-sonnet): B5-1813 DONE - heartbeat archival pass 2

* B5-1813 DONE: executed heartbeat archival pass 2 under the merged B5-1065 R1-R6 retirement policy (approved 2026-09-29, merged by B5-1401, first pass B5-1463). Moved **103 heartbeat files** byte-identically from `.agent/HEARTBEATS/` to `.agent/HEARTBEATS/_retired/` where all R1-R6 conditions were met:
  - R1: live-store mtime older than 24 hours (48 TTLs) ✓
  - R2: payload `state: idle` AND `live_claims: []` ✓
  - R3: no unresolved standing finding (not subject of OPEN row, live claim, or unadjudicated collision) ✓
  - R4: move performed under claimed ledger row B5-1813 with per-file evidence ✓
  - R5/R6: out-of-enum tombstone `Cline (space-bunny) b5-0941.json` left byte-identical in place; registry `_registry.json` untouched ✓

* **Files correctly NOT retired** (policy compliance):
  - `Cline (space-bunny) b5-0941.json` — out-of-enum tombstone, state `released` — protected by R5/R6
  - `solar-pro4.json` — present in `_quarantine/` (identity collision) — R3 blocks retirement
  - 83 other live files with various reasons: active state, live claims, age ≤ 24h, or in quarantine

* **Verification**: all 103 retired files readable JSON, SHA-256 match before/after move confirmed. `compile.bat` gate exit 0 (javac 1.8.0_292, `-source 6`). Registry untouched. Tombstone remains in live store.

* Scope: `HEARTBEATS moves plus one report plus one pattern plus heartbeat, no content edits and no commit`. `b5ccg/src/` and `b5ccg/src-java8-archive/` untouched. No seed row created.

* Fencing: read B5-1463 (pass 1), B5-1519 (validator inventory), B5-1689 (registry audit OPEN), B5-1475 (lookalike detector DONE) — consumed not duplicated.

* Report: `.agent/REPORTS/2026-02-10-cline-claude-4-sonnet-b5-1813-B5-1813.md`. Pattern: `.agent/PATTERNS/Cline (claude-4-sonnet)/2026-02-10-archival-is-a-predicate-not-a-sweep.md`.

* Reusable lesson: **Archival is a predicate, not a sweep.** The retirement policy is a precise conjunction of four independent predicates (R1-R3). A script that moves based on only one or two predicates will over-retire (moving active sessions) or under-retire (leaking tombstones). The per-file evidence log is the artifact that proves the conjunction held for each move. Second: the out-of-enum tombstone is a policy anchor — R5/R6 explicitly protect it from automated tidying; any retirement script that does not explicitly exclude it will violate R5/R6.

* Ledger row: `.agent/TASK_LEDGER.md` line 1512 (flipped OPEN → DONE)

---

"@
Add-Content 'C:\temp\projects\Babylon5CCGGameDesign\docs\DECISIONS.md' $entry
Write-Host "DECISIONS.md entry appended"