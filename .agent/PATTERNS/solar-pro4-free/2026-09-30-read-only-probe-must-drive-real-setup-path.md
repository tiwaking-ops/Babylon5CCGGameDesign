---
document:
  title: "Read-only probe must drive the real setup path, not a partial state"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
created_date: "2026-09-30"
---

# Read-only probe must drive the real setup path, not a partial state

**Reusable lesson:** A read-only invariant probe that manually sets a field
(e.g. `Player.setAmbassador()`) without reproducing the setup path that populates
the dependent state (e.g. the ambassador in the hand so `GameController.findAmbassador()`
can find it) will report a false breakage. The B5-0321 seat (`innerCircle.add(amb)`)
sits behind `setupGame()` → `findAmbassador()` → hand search; setting the ambassador
field directly bypasses all three and leaves the IC empty, which then cascades into
trivial failures on every assertion that depends on a non-empty IC.

**What I did:** Drove `setupGame()` via reflection and manually set
`setAmbassador()` on each player before the call, but never placed the ambassador
card in the player's hand. `findAmbassador()` searches the hand and returned null
for all four factions, so the `if (amb != null)` seat block was skipped and the IC
stayed empty. The conflictTotal skip passed (it is independent of the seat) but
canBuildInfluence and canPromote failed trivially.

**Correct probe design:** Either (a) place each ambassador card in its player's hand
before calling `setupGame()` so the real `findAmbassador()` path seats it, or (b)
call the real game setup end-to-end via `GameController.runGame()` or a headless
harness that builds faction decks the way the loader does, so the ambassador arrives
in the hand naturally. A probe that asserts the B5-0321 invariant must exercise the
B5-0321 seat path, not a shortcut that bypasses it.

**Filed:** `.agent/REPORTS/2026-09-30-solar-pro4-free-B5-1159.md`
