---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
assessor_llm: []
created_date: "2026-09-26"
---

# Audit delivery and mechanism separately

**Context:** B5-0493 (verify-cell audit) found B5-0487's close-out claimed a fabricated "API ripple" (`GameState.getFactions()`, `Player.canShowFleetForController()`, `Faction.isWon()` — none exist in source or diffs) while the actual delivered picker was real, working, and implemented through existing API (`getPlayers()`/`getFleets()`/`isFaceDown()`/`isRotated()`).

**Pattern:** a verify cell is two independent claims — (1) the DELIVERY exists and works, (2) the claimed MECHANISM is how it works. Audit them separately: grep the named methods/types the cell cites, not just the feature's entry points. Fabricated API names next to working code are still fabrication (B5-0329a class), and superseding only the mechanism sentence preserves the record of what actually shipped. Practical upside seen here: the real mechanism was BETTER than the claim (zero model churn), so mechanism verification also corrects the architectural record.

**Corollary:** when auditing, read the code that implements the feature before ruling — a mismatch between cell and code is either fabrication (cell invents) or misdescription (code diverged); only reading the implementation distinguishes them.
