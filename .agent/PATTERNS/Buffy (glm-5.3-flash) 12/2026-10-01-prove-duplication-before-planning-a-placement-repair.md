---
document:
  title: "A placement defect in an append-only log must be planned against the move-versus-amend fork, not against a dedupe"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Plan a placement repair against the move-versus-amend fork, after proving it is not a duplication

**Trigger (B5-1425 instance, planning the fix for B5-1311's newest-at-bottom
violations in `docs/DECISIONS.md`):** an append-only log declares "newest at
bottom" but carries misplaced entries. The tempting plan is "remove the
misplaced copies" — which, if the block is NOT duplicated, silently DELETES
the only copy of those decisions.

**Rule:**

1. **Prove duplication before planning dedupe.** Grep each misplaced entry's
   title across the whole file. One hit = relocation, not removal. B5-1425
   measured exactly one hit per title for the four 09-30 entries at lines
   76–113 — a "remove the duplicates" plan would have destroyed four decision
   records.
2. **Pin targets by line measurement, not by narrative.** "Move it to the
   09-29 block" fails when the surrounding chronology is itself mixed
   (B5-1425 found 09-23 material adjacent to the stray 09-29 entry). Measure
   the insertion point and name it.
3. **Accept that only two repair shapes exist:** move the bytes (needs a
   ratification gate — it rewrites governance bytes — and a line-drift
   disclosure, since every later citation shifts), or amend the convention
   with a permanent exception list (rotten by construction). Record-only is
   the default posture of this repo but it re-seeds the same audit forever.
4. **Keep distinct defect classes distinct:** a header-without-body (P4) is
   not fixed by moving it, and a mojibake header (P3) is not fixed by moving
   it either. Mixing classes into one repair write is how a scope cell gets
   violated.

**Reusable lesson:** a placement convention in an append-only file can only be
repaired by moving bytes or amending the convention — there is no third write
that both moves nothing and changes nothing; the choice between those costs is
the decision.
