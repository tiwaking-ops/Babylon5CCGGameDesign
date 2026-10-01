---
document:
  title: "B5-0977 close-out — zone-header paint-order overlap, taken over from a stale claim"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0977-take2", version: "space-bunny"}
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0977-take2", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0977 — the single top-ranked ui-only fix from B5-0964

## Verdict

**DONE.** The B5-0964 report's highest-ranked ui-only finding was the zone-header
paint-order overlap, and it is now fixed in `GameBoardPanel.drawZone` with a
paint-order-only change. Every gate named on the row is green.

## The claim was NOT absent

The runner stated the claim file had already been checked. It was **present**:
`.agent/CLAIMS/B5-0977.json`, held by `Cline (space-bunny) b5-0977`. Re-verifying
before acting is what stopped a second writer entering one `ui/` scope.

**Three-signal verdict: STALE** (measured 20:38Z, TTL 30 min):

| Signal | Value | Age | Verdict |
|---|---|---|---|
| Claim `started_utc` / mtime | `20:03:30Z` / `20:03:05Z` | 35.1 min | outside TTL |
| Owner heartbeat mtime (file present, parses) | `20:03:08Z` | 35.0 min | outside TTL |
| Report `*B5-0977*.md` | count 0, absent | — | contributes only |

Because the owner heartbeat **existed and parsed**, signal 2 is `STALE` and not
`UNKNOWN`; with no signal inside the TTL the verdict is `STALE`, which is what
makes the reap permitted. Evidence recorded in the reap note on the ledger row
*before* the reap. The predecessor's heartbeat was left byte-identical.

## The predecessor's abandoned diff was sound — and I checked before keeping it

The predecessor died mid-task leaving an uncommitted edit to `GameBoardPanel.java`.
The temptation was to revert it as abandoned. Instead I read it, and it was a
correct in-place fix of exactly the top-ranked finding:

- Three signals — the faction name, `Unrest: N`, and the `CIVIL WAR` badge — all
  drew at the identical origin `x + 8, y + 47`. Now chained horizontally via a
  running `statusX`, each label advancing it by its own measured
  `stringWidth(...) + 10`. The badge is last and advances nothing.
- `statusX` is measured under the `PLAIN 11` font that actually painted the
  faction string, so the chain cannot begin mid-glyph.
- Narrow zones degrade in a deliberate order: header row → `y + 61` right-aligned
  (clear of the 60px ambassador mini-card, guarded by `> x + 68`) → clipped at
  the border. Clipping beats overpainting, since a clipped badge still reads as a
  badge while an overpainted one destroys two other signals.

**The tell that it replaced rather than duplicated:** `git diff --stat` showed
**0 deletions**. I confirmed by search that exactly one `getUnrest()` draw and one
`CIVIL_WAR` draw exist in the file. Had the old block survived, the fix would have
doubled every readout — a defect that still compiles, still passes conformance,
and still passes the smoke test.

The guards themselves are carried over **unchanged** from B5-0715/B5-0691
(`unrest > 1`, race-level `civilWarOfRace`), so this is coordinates only: no state
read that was not already readable, nothing derived, no engine or `RulesEngine`
boundary touched.

## Gates, all measured this pass

| Gate | Result |
|---|---|
| `b5ccg\compile.bat` | **exit 0**, `Build successful`, one expected `-source 1.6` bootstrap warning, `javac 1.8.0_292` |
| `HeadlessConformanceTest` | **exit 0** — `CONFORMANCE SUITE PASSED (643 checks)` |
| `HeadlessSmokeTest` | **exit 0** — `SMOKE TEST PASSED`, 32 AI actions, 43 UI callbacks, 4 of 4 legal |
| Java 6 grep over `ui/*.java` | **1 hit**, `MainWindow.java:1745`, the tooltip string literal `" -> "` — not a lambda arrow |
| `run-dup-census.ps1` | **PASS**, 0 duplicate task IDs |
| `ledger-query.ps1` | my row `pipeCount 7 / doubleLead no` |

## Flagged, deliberately not fixed

This fix makes the **B5-0805 playtest-guide caveat stale** — the guide tells a
playtester that the badge and the unrest number "are drawn at the same board
position" and that the badge wins. They no longer share a position. The guide is
documentation scope, outside this row's `ui/` claim, so I did not edit it and am
flagging it for a follow-up row rather than fixing out of scope.

## Scope held

`b5ccg/src/b5ccg/ui/` only. `ai/`, `engine/`, `model/` and card data untouched. No
foreign claim, heartbeat, row or report edited. No commit, no push. Files I wrote:
this report, one pattern record, one `docs/DECISIONS.md` entry (plus its
provenance lines), one ledger row, one heartbeat.

## Reusable lesson

**A predecessor's abandoned diff is evidence, not litter — read it and gate it
before deciding to keep it, and read `0 deletions` in the diffstat as the tell
that an in-place fix replaced the old block instead of stacking a second draw on
top of it.** A duplicated readout compiles, passes conformance and passes the
smoke test; only the diffstat and a search for the second draw call catch it.
Pattern:
`.agent/PATTERNS/Cline (space-bunny) b5-0977-take2/2026-09-28-an-abandoned-diff-is-evidence-not-litter.md`.
