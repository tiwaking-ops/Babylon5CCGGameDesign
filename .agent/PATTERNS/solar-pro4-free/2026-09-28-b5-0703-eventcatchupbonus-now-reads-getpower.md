---
document:
  title: "eventCatchUpBonus now reads getPower() — zero-cost floor must be probed at the consumer"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
---

# eventCatchUpBonus now reads getPower() — zero-cost floor must be probed at the consumer

**B5-0703** added a `getPower()` read to the AI helper `eventCatchUpBonus(GameState, Player)`, replacing a `getInfluence()` read. The B5-0677 Computed Power seam guarantees `getPower() == getInfluence()` when no POWER-tagged bonus is present, so the pre-0703 AI ordering is byte-identical at zero cost. That guarantee lives in the producer (Player), but the producer's own suite section (PWR, 10 checks) only asserts the seam — it does not assert that the AI consumer actually reads the new quantity. A regression that silently switched the helper back to `getInfluence()` would leave the suite green.

**Lesson:** when a derived quantity retroactively changes the meaning of a value a downstream consumer already reads by name, verify the zero-cost floor with a synthetic fixture that holds the bonus total at zero and asserts byte-identical equality **at the consumer** — not just "the suite still passes". The probe must call the consumer, ideally through the same access path the production code uses (here reflection into the private helper, because the helper is private and adding a test accessor would itself be a source edit outside scope).

**What the probe asserted:**
1. Producer floor: `getPower() == getInfluence()` with no bonus (22 == 22).
2. Consumer sees the divergence when a bonus is present: `getPower() == 26` with `+4 POWER`, and the helper's trailing-player catch-up tracks the Power gap (3, capped) not the influence gap.
3. Zero-cost band in the consumer: with both players at equal Power (22 == 22, no bonus divergence), the helper returns 0 — the pre-bonus behaviour.
4. Negative POWER widens the consumer's seen gap (2 vs 0 influence-only), confirming the helper truly reads Power and not cached influence.

**Filed:** `b5ccg/out/B50703Probe.java` (git-ignored scratch, 5/5 PASS). Linked from `.agent/REPORTS/2026-09-28-solar-pro4-free-B5-0703.md`.

**Supersede-never-rewrite:** if a later task corrects this lesson, write a new file linking this one.
