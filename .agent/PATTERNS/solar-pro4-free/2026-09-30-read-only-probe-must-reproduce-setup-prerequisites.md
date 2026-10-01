---
document:
  title: "Read-only probe must reproduce the setup path's full prerequisite chain"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
created_date: "2026-09-30"
---

# Read-only probe must reproduce the setup path's full prerequisite chain

**Reusable lesson:** A read-only invariant probe that sets a field directly
(e.g. `Player.setAmbassador()`) without reproducing the setup path that
populates the dependent state (e.g. placing the ambassador card in the hand so
`GameController.findAmbassador()` can find it) will report a false breakage.

The B5-0321 seat (`innerCircle.add(amb)`) sits behind three prerequisites:
`setupGame()` → `findAmbassador()` → hand search. Setting the ambassador field
directly bypasses all three and leaves the IC empty, which then cascades into
trivial failures on every assertion that depends on a non-empty IC
(canBuildInfluence, canPromote).

**What happened:** B5-1159's first probe draft called `setAmbassador()` on each
player but never placed the ambassador card in the player's hand. `findAmbassador()`
returned null for all four factions, the `if (amb != null)` seat block was
skipped, and the probe reported 12/16 failures — all of them downstream of the
missing seat, none of them real.

**Correct probe design:** Either (a) place each ambassador card in its player's
hand before calling `setupGame()` so the real `findAmbassador()` path seats it,
or (b) call the real game setup end-to-end via a headless harness that builds
faction decks the way the loader does, so the ambassador arrives in the hand
naturally. A probe that asserts the B5-0321 invariant must exercise the B5-0321
seat path, not a shortcut that bypasses it.

**Filed:** `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1159.md`
