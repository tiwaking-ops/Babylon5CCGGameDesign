---
document:
  title: "Pattern — fixture damage must respect Card.reconcileDamage thresholds"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: applyDamage is not a plain token setter

**Lesson:** `Card.applyDamage(n)` funnels through `reconcileDamage()`,
which neutralizes the card when `damageTokens >= greatest ability`
(default 1 for stat-less cards) and sets the neutralizedThisTurn lock that
blocks healing for the rest of the turn. A test fixture that "just adds 2
damage" to a greatest-ability-2 character silently flips it face-down and
neutralized, and every downstream legality check fails for a reason
unrelated to the path under test (B5-0460: 3 heal checks failed this way).

**Rule of thumb:**
1. To keep a card healable/repairable in a fixture, apply damage strictly
   BELOW its greatest ability (use 1 for a max(1, stat) threshold).
2. To TEST the neutralization path, apply damage AT or ABOVE it and assert
   the flip explicitly.
3. On fixture failures, check `isNeutralized()` / `isFaceDown()` /
   `wasNeutralizedThisTurn()` before suspecting the API under test —
   reconcile side effects explain most "predicate returned false" results.
