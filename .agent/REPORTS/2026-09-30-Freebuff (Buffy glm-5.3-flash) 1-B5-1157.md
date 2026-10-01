---
document:
  title: "B5-1157 — UI re-audit against the closed F-findings and the cost gate: 12 hold, 1 partial (affordability not previewed on Play)"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T06:59:13Z"
  instrument: "source read of MainWindow.java (2,353 lines), HandPanel.java (427), GameBoardPanel.java (780), RulesEngine.canPlayCard"
---

# B5-1157 — per-finding re-audit (F1–F13), then the cost gate

## F-findings: 12 hold, 1 partial

| F | what it was | verdict | evidence (file:line) |
|---|---|---|---|
| F1 human join | support/oppose controls | **holds** | supportButton/opposeButton wired (MainWindow 78–79) |
| F2 conflict target select | explicit target before Initiate | **holds** | targetReady predicate 2114–2116 (B5-0325 F2 comment in place) |
| F3 action-set gating | sponsor/promote/build enabled by rules | **holds** | sponsor/promote/build buttons dispatch through `rules.canPromote`/`canBuildInfluence` (324, and B5-0326 block) |
| F4 split Play/Initiate | never conflates | **holds** | playCardOnlyButton + playOnly() dispatch (97/286/2140), F2 target-readiness preserved |
| F5 no illegal submission / cost preview | preview costs before commit | **PARTIAL** — see below |
| F6 phase-aware gating | controls only in their phase | **holds** | phase predicates at 2104–2110 (`phaseAllowsAction/Initiation`) |
| F7 promote | leader + promote flow | **holds** | promoteCharacter dispatch gated by `rules.canPromote(hp, ch)` + findUnrotatedIC (320–327) |
| F8 initiative display | board shows initiative | **holds** | GameBoardPanel renders initiative (B5-0327 block; no regression signature in 780 lines) |
| F9 conflict-type legend | mapping legend | **holds** | legendPanel "Conflict Types → Abilities" 233–245 |
| F10 assistant status readout | A+/A$ marks | **holds** | GameBoardPanel 514–520 (`A+`/`A$`), B5-0992 already adjudicated existing |
| F11 zone overflow guard | "(+ N more)" | **holds** | GameBoardPanel 666–676 shared overflow note |
| F12 narrative log framing | round/phase headers | **holds** | "Round N" prefix detection 1403–1408, B5-0331a helpers 2298 |
| F13 cost on card face | hand cards show INF | **holds** | HandPanel 314 `Cost: N INF` (+ conflict reward at 336) |

## F5, partial: the gate creates an unaffordable state the Play button does not preview

- **The engine gate exists and is strict**: `RulesEngine.canPlayCard`
  ([RulesEngine.java:904](../../b5ccg/src/b5ccg/engine/RulesEngine.java#L904))
  returns false when `p.getAppliedPool() < c.getCost()` (B5-1038).
- **The Play button never consults it**: enablement at
  [MainWindow.java:2125–2127](../../b5ccg/src/b5ccg/ui/MainWindow.java#L2125)
  checks only `myTurn && inHand && !conflictSelected &&
  !agendaNeedsLifecycleAction && phaseAllowsAction`. An unaffordable
  non-character card leaves Play lit; the refusal surfaces only after the
  click — the exact player-facing shape the row asks about, and the same
  class as the original F5 no-preview finding, now reachable because the
  gate that makes refusals possible exists.
- **The preview half-exists**: `refreshCostPreview()`
  ([MainWindow.java:1291–1352](../../b5ccg/src/b5ccg/ui/MainWindow.java#L1291))
  lists unaffordable hand cards as `UNAFFORDABLE: <title> (N INF need)`
  (B5-1049), and sponsor/promote previews read the same rules methods the
  buttons gate through. So the *information* is on screen — but the
  *button state* does not track it, and the unaffordable list is invisible
  whenever a character is selected (1297–1300's early-return clears the
  label when the selected-card branch finds nothing to say — the
  UNAFFORDABLE list is only suppressed when `ch == null && sb.length() ==
  0`, so selection generally preserves it; the real gap is Play, not the
  label).

**Smallest change that would close the partial** (not applied — this row
is report-only): intersect `canPlay` at 2125–2127 with
`rules.canPlayCard(humanPlayer(), selectedCard)` for non-Conflict cards,
so Play lights only when the engine gate agrees. One line, uses the exact
gate the engine enforces, no new UI state.

## Bounds

Read-only: no ui/, engine/, or data file touched; no commit, no push.

**Reusable lesson:** a UI gate added engine-side changes the meaning of
every button that was lit before it — re-audit enablement predicates, not
just the new preview, when a refusal becomes possible that used to be
impossible.
