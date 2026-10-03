---
document:
  title: "F-series fix ordering — sequencing the B5-0310 UI findings, and the measured finding that twelve of thirteen are already closed"
  status: "Proposal — candidate, never truth until merged and compiled"
provenance:
  author_llm: {name: "Hermes (stealth-space-bunny-alpha) 2135", version: "stealth-space-bunny-alpha"}
  assessor_llm: []
  last_modified_by_llm: {name: "Hermes (stealth-space-bunny-alpha) 2135", version: "stealth-space-bunny-alpha"}
  created_date: "2026-10-02"
  task: "B5-2135"
---

# Proposal: an implementation order for the B5-0310 F-series

Authored for B5-2135, which asked for the 13 UI findings of B5-0310 sequenced
into implementation order with dependency notes. Sources: the original report
`.agent/REPORTS/2026-09-21-solar-pro4-B5-0310.md`, the re-audit
`.agent/REPORTS/2026-10-02-GitHub Copilot (Auto mode) 1237-B5-1817.md`, the
per-finding mapping rows B5-2115 through B5-2129, and a read-only grep of
`b5ccg/src/b5ccg/ui/` taken this pass.

**The proposal does not sequence 13 fixes, because 12 of them are not defects
any more.** It sequences 1. The re-audit and the tree agree, and this pass
re-measured the tree rather than trusting either.

## 1. The measured state of the F-series

Every finding below was checked against the working tree this pass. The
"evidence" column is a `file:line` in the current `ui/` sources.

| Finding | Severity at B5-0310 | Measured state | Evidence |
|---|---|---|---|
| F1 no Support/Oppose UI | P0/MED | **CLOSED** — buttons exist and submit `GameAction.joinSupport()` / `joinOppose()` | `MainWindow.java:577`, `:584` |
| F2 no conflict target selection | P0/MED | **CLOSED** — `targetSelector` populated for a selected `ConflictCard` | `MainWindow.java:82`, `:554` |
| F3 Action-round actions unreachable | P0/HIGH | **CLOSED** — Sponsor, Promote, Build Influence, Play Card, Initiate Conflict, Lead Fleet, Support, Oppose, Attack, Heal, Repair, mercenary Bid, contingency Reveal | `MainWindow.java:331`, `:344`, `:361`, `:314`, `:322`, `:508`, `:577`, `:584`, `:625`, `:642`, `:667`, `:458` |
| F4 Play/Initiate conflated | P1/MED | **CLOSED** — split into `playCardOnlyButton` and `initiateConflictButton` | `MainWindow.java:102`, `:103` |
| F5 no cost/affordability feedback | P1/HIGH | **CLOSED** — live sponsor/promote cost preview and an affordability precheck naming the unaffordable cards | `MainWindow.java:1394`, `:1399`, `:1375` |
| F6 no phase-aware gating | P1/MED | **CLOSED** — enablement keys on `GamePhase.ACTION` plus turn/legality | `MainWindow.java:1360`, `:1512` |
| F7 no participation visualisation | P2/MED | **CLOSED** — per-participant committed breakdown on the board and a read-only participant list in the sidebar | `GameBoardPanel.java:299`, `MainWindow.java:93` |
| F8 no initiative order display | P2/LOW | **PARTIAL — the one residual** | `MainWindow.java:1510` |
| F9 no conflict-type legend | P2/LOW | **CLOSED** — four-row legend, one per conflict type and its resolving ability | `MainWindow.java:261`, `:268` |
| F10 assistant effect not surfaced | P2/LOW | **CLOSED** — `A+` and `A$` markers on the assistant mini-card | `GameBoardPanel.java:758`, `:764` |
| F11 zone overflow clips silently | P3/LOW | **CLOSED** — shared `(+ N more)` overflow note drawn below the row | `GameBoardPanel.java:910`, `:920` |
| F12 log is a raw dump | P3/LOW | **CLOSED for framing** — round and phase headers inserted via `isRoundPrefix` / `isPhasePrefix`; the structured-entry question is B5-2137's, not this row's | `MainWindow.java:1451`, `:2400` |
| F13 hand card omits cost | P3/LOW | **CLOSED** — `Cost: N INF` on the card face | `HandPanel.java:253`, `:436` |

Twelve closed, one partial. This matches B5-1817 finding for finding, which is
the independent reason to believe it: two passes, one reading the code and one
reading the receipts, reached the same classification.

## 2. The ordering, with dependencies

### Step 1 — F8, render the full initiative chain (the only open item)

The current label reads `Initiative: <active player>` with a `(you)` marker
(`MainWindow.java:1510`). That answers *whose turn is it*, which the phase tag
already answered. B5-0310 F8 asked the different question: *where do I sit
relative to the other players?* Only the full ordered chain answers it.

Dependencies, in order:

1. **Order source — ANSWERED, and it blocks the UI row.** DONE B5-2125 mapped
   F8 and established that no initiative order is computed anywhere: turn order
   is `currentPlayerIndex` walking `state.advanceTurn()` in the fixed
   construction order of the player list (`GameState.java:9`, `:59`), and
   `RulesEngine.startRound()` does not reorder players
   (`GameController.java:1861`). The rulebook order at
   `BABYLON5_CCG_RULEBOOK.md:302`, `:316`, `:382` — lowest Influence Rating
   first, tiebroken Diplomacy, Intrigue, Psi, Leadership — is **not
   implemented**. So this is not a display gap over existing data; it is a
   missing rulebook mechanic. **An engine row must compute initiative order
   first, and any UI row is blocked behind it.** A UI row that sorted players
   itself would create a second, divergent notion of turn order, which is
   strictly worse than the current honest round-robin label.
2. **Rendering.** Append the remaining players to the existing label or a
   list, keeping the `(you)` marker and the active-player highlight. Preserve
   the existing single-player label format so nothing else that reads it
   breaks.
3. **Acceptance.** The displayed chain equals the engine's computed order for a
   4-player game including one tiebreak case, and the display updates on
   `onStateUpdate`.

Nothing else in the F-series depends on F8, and F8 depends on nothing else.
It is therefore both the first and the last item, which is the honest shape of
this proposal: there is no remaining ordering problem to solve.

### Step 2 — nothing, until a finding is re-opened by evidence

If a later re-audit contradicts §1, this proposal is superseded by a new file
rather than edited (the supersede-never-rewrite rule). The dependency notes
above survive that; the table does not, because it is a measurement and
measurements expire.

## 3. Credit and fencing

Three rows already own adjacent UI work and are credited rather than
duplicated:

- **DONE B5-2081** (`Cline (space-bunny-free)`) audited the setup dialog,
  surrender readout and the F1 shortcut overlay in `MainWindow`. It found the
  surrender control reachable and closing part of F3, and found the F1 overlay
  **missing from source** despite its DECISIONS entry. That is the one place a
  DONE row's receipt is not proof code landed, and it is worth carrying: an
  overlay credited in the ledger is not an overlay in the tree.
- **OPEN B5-2007** (`Cline (space-bunny-free)`) owns deck-construction
  feedback in the deck-builder UI. Unrelated to the F-series; fenced so no row
  claims deck-builder surfaces.
- **OPEN B5-1980** (`Inkling (me-so-poor)`) owns deck-builder search and
  filter. Unrelated to the F-series; fenced for the same reason.

This proposal's own scope is `docs/proposals/` only. It edits no `ui/` file, no
model and no engine file, and it seeds no ledger row.

## 4. What this proposal does not claim

It does not claim the UI is complete or good. It claims only that the 13
findings named in its own table are closed except F8's residual, measured this
pass. It takes no position on findings B5-0310 never raised, on the
deck-builder rows, or on the F1 overlay question B5-2081 raised — that one
belongs to whoever owns the shortcut surface.

## 5. Verification

- `javac -version` → `javac 1.8.0_292`.
- `bash b5ccg/compile.sh` → **RED**, reproduced twice minutes apart, on a
  single error at `b5ccg/src/b5ccg/ui/MainWindow.java:1048`,
  `cannot find symbol: method showDeckBuilder()`. `git show HEAD` on that file
  returns zero occurrences of the symbol, so the call site is an uncommitted
  added line in a foreign in-flight edit, and live claim
  `.agent/CLAIMS/B5-2007.json` (`Cline (space-bunny-free)`, scope `ui/`) covers
  it. Out of this row's scope; not fixed here, per AGENTS.md section 5 and
  `.agent/00_BOOT.md` step 8. B5-2135 is therefore recorded BLOCKED, not DONE.