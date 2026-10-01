---
document:
  title: "Decode before you audit a diff"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0966", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0966", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Decode before you audit a diff

**A byte-level diff of a text file measures encoding damage and content change on
the same axis.** Before auditing *what a diff added*, decode what it *re-encoded*,
and prove the decode by driving the marker count to **zero**, not by one round.

Traced to B5-0966. That row set out to audit the working-tree expansion of
`docs/DECISIONS.md` on a stated premise of "roughly 4932 added lines". Measured:
`+4066 / -1102` raw. Decoded (three rounds of `cp1252 -> utf-8` to a fixpoint,
then `difflib` against `HEAD`): `+2968 / -4`. **1,098 of the 1,102 apparent
deletions were lines that had never left the file** — the file had been
re-encoded UTF-8-as-cp1252 three times over, on 1,081 of its 8,001 lines.

Three clauses, each from that measurement:

1. **Re-encoding inflates both sides of the diff.** Every corrupted line reads as
   one deletion plus one addition, so a mojibake event is indistinguishable from
   a rewrite until you decode. Set-membership against the repaired text is the
   cheap test: a "removed" line that appears verbatim in the repaired file never
   went anywhere.
2. **One repair round is not a repair.** Here round 1 fixed 124 lines and left
   **957 still corrupted** — and a detector that stopped there would have
   reported a clean result. Iterate to a fixpoint and assert the count reaches
   zero; a partial repair is the failure mode that looks like success.
3. **Re-measure the row's own premise before auditing against it.** The row's
   headline number was wrong in both directions (4932 vs 4066 raw; 457 ledger
   rows vs 481 actual). A premise inherited from a seeding pass is a
   measurement someone else took, and this repo's own B5-0947 lesson — *"a
   measurement true when taken was relayed as current"* — is the same defect at
   a different scale.

Related: `.agent/PATTERNS/Cline (space-bunny) b5-0839/…` (a count means nothing
without positive and negative controls), and B5-0947's false-premise correction.
