---
document:
  title: "Pattern: fix residency checkers with an explicit payload-residency table"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Fix residency checkers with a payload-residency table

Application of the (supersede-corrected) verifier-exclusion-lists lesson.
Instead of patching call sites one by one, enumerate every action type's
payload SOURCE (AIPlayer offer site -> GameAction factory -> where the card
lives: hand vs in-play vs pool vs committed) and write the checker as an
explicit allow-list of slot-payload types. The audit that builds the table
proves the exemption set complete; the table in the report documents why each
type is exempt for the next payload-family author. Types whose payloads ARE
hand cards stay under the checker - exemption is not blanket.

Evidence chain: B5-0495 (two triages, one defect) -> B5-0505 (one-hunk fix,
20/20 post-fix PASS vs 2/30 pre-fix reproductions).
Source: .agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0505.md
