---
document:
  title: "Pattern — docs refreshes must propagate audits and re-verify proposal premises"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: a gated docs refresh propagates its gates' findings

Filed under the standing "Reusable lesson" convention (00_BOOT step 10).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0426.md`.

## The lesson (two halves)

1. **Propagate, don't annotate.** When a docs task is gated on an audit (0426 ←
   0414), the audit's corrections must be *applied* to the existing text, not just
   linked. The guide's Damage bullet claimed human players could trigger attack/
   heal/repair — the 0414 audit had proven those buttons unreachable. A refresh
   that only appended "an audit happened" would have left a false player-facing
   claim in place. Grep the refreshed doc for claims the audit falsifies before
   closing.

2. **Re-verify proposal premises against the tree.** Filed proposals can carry
   wrong factual premises (the 0422 proposal assumed an all-EASY default seat mix;
   the tree has exactly one EASY seat everywhere — Main.java:70-72 and both
   harnesses). When summarizing a proposal for a guide, check its "grounded" facts
   with your own grep; otherwise the error migrates from an advisory file into a
   player-facing one.

## Procedure

1. List the gating task's headline findings (report + DECISIONS entry).
2. Grep the target doc for every claim those findings touch; fix each.
3. For any cited proposal, spot-check its in-tree premises yourself.
4. Note discrepancies explicitly (reconciliation belongs to the row owner).
