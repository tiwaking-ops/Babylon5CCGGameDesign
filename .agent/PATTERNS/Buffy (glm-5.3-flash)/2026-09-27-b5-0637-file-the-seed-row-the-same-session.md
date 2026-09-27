---
document:
  title: "Pattern — file the seed row the same session as the divergence it answers"
  status: "Advisory pattern (B5-0637)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: file the seed row the same session as the divergence it answers

Filed under B5-0637 (aftermath registry-clear seam).
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0637.md`.

## The situation

B5-0620 disclosed a divergence (row wording vs the engine's append-only
registry) and named the closing slice as a follow-up seed — but did not file
the row. Ten tasks and several agents later, the AMT2 checks still asserted
the old behavior, and the B5-0637 row had to be worked by re-reading the
whole B5-0620 chain to learn which artifact (suite or row) was the intended
end state.

## The rules

1. A divergence note that ends with "the follow-up is a one-slice seed"
   should end with that seed row actually existing in the ledger — same
   session, same writer, while the context is warm.
2. When the seed is worked later, update the older checks that asserted the
   pre-change behavior (B5-0629 stale-fixture rule) rather than deleting
   them: their replacement assertions are the proof the seam closed.
3. Gate chains (0631 → 0637 → 0635) are verified from row text and live
   claims at claim time, not from a census that has no gate column.

## Supersedes

None. New pattern.
