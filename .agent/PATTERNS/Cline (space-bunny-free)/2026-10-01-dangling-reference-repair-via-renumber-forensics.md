---
document:
  title: "Dangling reference repair via B5-0618 renumber forensics"
  status: "Pattern"
provenance:
  author_llm: {name: "Cline", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Reusable lesson

A dangling in-row B5-id reference is a finding, not a link to follow — when the cited id has zero row-leading grep hits, apply B5-0618 collision forensics (seed-wave notes + run-dup-census history) to determine whether the id was vacated, renumbered, or never existed; then re-point the referencing row byte-safely or record why human ruling is required.

## Context
- B5-1333 Task cell instructed: `read OPEN B5-1275 first and if it owns the post-exclusion armed re-run`
- `findstr /n "B5-1275" .agent/TASK_LEDGER.md` → zero row-leading matches (only prose in seed notes and the B5-1333 row itself)
- Seed-wave note (Buffy (glm-5.3-flash) 9, 2026-09-30) records: `first landed as B5-1261..B5-1275, then renumbered non-adjacent to B5-1341..B5-1355 per B5-0618 and B5-1107`
- Positional mapping: B5-1275 → B5-1355 (offset +80)
- B5-1355 exists and is the "Re-run the armed fixed-list probe after the pool exclusion lands" task — exactly the intended referent

## Procedure
1. **Grep exhaustively** for the cited id across the ledger (row-leading anchors only).
2. **Read seed-wave notes** in the ledger provenance / QUEUE blocks for renumbering records.
3. **Cross-reference** with `run-dup-census.ps1` history — a collision that triggered non-adjacent renumbering leaves a trace.
4. **Determine disposition**: vacated (renumbered), never existed, or genuinely lost (requires human ruling).
5. **Repair byte-safely**: edit only the referencing row's Task/Note cells, preserve 7 pipes / single leading / no `|` in cells.
6. **Record the forensics** in the repaired row's Note cell and in the repair task's report.

## Guardrails
- Do NOT create a row for the missing id.
- Do NOT edit any row other than the referencing one.
- Preserve pipe contract (ledger-query.ps1 7/no, run-dup-census.ps1 PASS).
- If disposition is "genuinely lost / ambiguous", record why and stop — do not guess.