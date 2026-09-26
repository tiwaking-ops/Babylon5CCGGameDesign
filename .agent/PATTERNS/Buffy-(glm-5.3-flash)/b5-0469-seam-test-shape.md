---
document:
  title: "B5-0469 pattern: seam test shape"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
---

# Seam test shape (B5-0469)

**Applies to**: conformance tests asserting a new model seam shape.

A seam test method should probe three things in one place:

1. **Default/legacy state** — the un-targeted or pre-transition values
   (nulls, false flags, null return where appropriate). This guards against
   accidental behavior changes in the existing path.
2. **Explicit-target transition** — set the new field(s) and assert the getter
   round-trip + `hasExplicitTarget()` gate.
3. **Registry probe round-trip** — if the seam produces a `StatBonus` that is
   granted into a `Player` registry, assert `grantBonus` →
   `hasAttachedBonusFrom` positive, then the three negatives (wrong source id,
   wrong target id, null args).

This keeps coverage local to the seam and makes regression isolation trivial.
See `HeadlessConformanceTest.java` `testOpponentEnhancementSeam()` for the
template.
