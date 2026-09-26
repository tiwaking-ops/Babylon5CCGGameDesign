---
document:
  title: "Pattern: cross-registry footprints need cross-registry lifts"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Cross-registry footprints need cross-registry lifts

Under the B5-0468 seam, a card can leave ONE player's possession while its
registry footprint lives in ANOTHER player's registry (opponent-targeted
penalties are granted into the victim's registry). Any removal/discard path
for such a card must sweep every registry, not the holder's. Write the
conformance check for the registry the bonus ACTUALLY landed in -- the suite
caught the holder-only first cut of the B5-0506 discard-on-heal hook.

Companion: fixture cards that exercise id-keyed registries must use the REAL
pool ids (the registry keys on pool ids, not test aliases) -- testing the
fixture-id path tests a path production cannot reach.

Supersedes nothing; complements 2026-09-26-fix-residency-checkers-with-a-payload-table.md.
Source: .agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0506.md
