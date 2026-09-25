---
document:
  title: "B5-0423 — UI defect-fix slice (B5-0414 P0/P1/P2), close-out report"
  status: "Report (ui/ only; no engine or model edits)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  last_modified_date: "2026-09-25"
---

# B5-0423 — UI defect-fix slice (B5-0414 P0/P1/P2)

**Agent:** opencode (me-so-poor / big-pickle). Claimed 2026-09-25T05:59:00Z
after verifying the row's gate (B5-0414 DONE) and reading the B5-0414 audit
report first, as the row requires. Scope honored: `b5ccg/src/b5ccg/ui/` only.

## Starting state (why the file looked "already done")

The board panel already *called* `handleClick` from its mouse listener and
`drawSelectionMark` from five draw sites, and already had the supporting-role
chip row plus a `setOnBoardCardSelected` / `clearSelection` surface — but it
defined **none** of them. A single-file grep for the definitions returned only
HandPanel's unrelated `handleClick`. So the file did not compile: the board
half of B5-0423 had been half-landed by an earlier pass and left dangling.
Defining the missing methods was step one, not an optional extra.

## What landed

### P0 — five selection-driven controls had no board-side source

`GameBoardPanel` gained the click chain `handleClick -> resolveCardAt -> hit`
plus `drawSelectionMark` (4-arg and 5-arg large forms). The hit-test geometry
is copied verbatim from `drawZone` so the clickable area cannot drift from the
painted area:

| Row | Card size | Origin | Step |
| --- | --- | --- | --- |
| Ambassador | 60x84 | (x+8, y+58) | — |
| Inner Circle | 46x64 | (x+8, y+175) | 52 |
| Supporting role | 46x16 | (x+8, y+269) | 50 |
| Fleets | 46x64 | (x+8, y+310) | 52 |
| Groups, then Locations | 46x64 | (x+8, y+430) | 52 |

`MainWindow` now wires `boardPanel.setOnBoardCardSelected` to a new shared
`applyCardSelection(card, fromHand)`, which the hand listener also calls. One
handler means target population, assistant tracking, enablement, cost preview
and the Tier-1 remainder controls behave identically for hand and board.

Face-down cards are deliberately unresolvable — that preserves the B5-0381
boundary where the model exposes a host's contingency count without exposing
the card's identity.

**Interface interpretation:** a board selection does *not* populate the human
target dropdown. A board card is never an attackable ConflictCard and conflicts
are not board-resident, so `fromHand` alone gates the dropdown. Miss clicks
clear only the local highlight, so a hand selection survives a stray click on
empty board space (mirrors `HandPanel.handleClick`).

### P1 — stale selectors survived clearSelection

`clearSelection` reset the card fields and the two Tier-1 selectors but left
`warTargetSelector`, `mercenaryBidAmountSelector` and `contingencySelector`
showing the previous turn's values. All three now reset. The bid selector
goes to **index 0, not -1**: `refreshMercenaryBid` parses
`getSelectedItem()` with no null guard, and a -1 index makes it null — a -1
"reset" would have traded a stale value for an NPE. `clearSelection` also
calls `boardPanel.clearSelection()` so the lime highlight cannot outlive the
selection that drew it.

### P2 — heal enablement disabled a legal move

The hand-rolled predicate omitted `!isRotated` and the undamaged-IC aid path.
The engine's own rule (`RulesEngine.canHealCharacter:33`, the HLR "undamaged IC
member may rotate as ambassador aid" case) allows both. The action handler
already gated on the engine predicate, so enablement now uses the same one —
for Heal *and* Repair. The duplicated partial copy is gone rather than patched,
so the two cannot drift apart again.

### Regression guard (not in the audit, found while wiring)

With board selection live, `canPlay` had no hand containment: an IC, fleet or
location card is not a ConflictCard, so it would light "Play Card" and dispatch
an illegal `playCard` for a card not in hand. `updatePlayInitiateButtons` now
requires `humanPlayer().getHand().contains(selectedCard)`.

## P3 residuals — recorded, not fixed

- **Attack auto-targets** the first valid participant, and the control is
  structurally gated anyway: `activeConflict` is never non-null at human
  decision time (B5-0409 finding A). Closing that needs an engine change, and
  this row's scope is `ui/` only.
- **Bid hardcodes `offers.get(0)`** — unreachable while the mercenary pool is
  empty, so it has no live effect to fix yet.

Both are logged rather than silently widened into scope.

## Gates

| Gate | Result |
| --- | --- |
| `compile.bat` (javac 1.8.0_292, `-source 1.6 -target 1.6`) | PASS |
| `HeadlessConformanceTest` | 360/360 PASS, exit 0 |
| `HeadlessSmokeTest` | PASS, exit 0 |
| `HeadlessAIDifficultyContractTest` | 10/10 PASS, exit 0 |

Suite count 360 unchanged, matching the B5-0426 refresh. Java 6 held
throughout: no lambdas, no diamond, no method references.

**Reusable lesson:** when wiring a hit-test, copy the paint geometry into one
table and keep the click and draw paths reading the same numbers — a hit-test
written from memory of a layout is a second, silent source of truth. And when
an audit says a control is "always dark", check whether the *callback* is
missing or the *plumbing* is: here the callback surface existed and looked
finished, while the methods it called did not exist at all.

## Files changed

- `b5ccg/src/b5ccg/ui/GameBoardPanel.java` — handleClick, resolveCardAt, hit,
  drawSelectionMark (previously called but undefined).
- `b5ccg/src/b5ccg/ui/MainWindow.java` — board callback wiring, shared
  applyCardSelection, engine-authority Heal/Repair enablement, clearSelection
  selector resets + boardPanel.clearSelection(), canPlay hand containment,
  removed the now-dead `fStatusLabel` local.
