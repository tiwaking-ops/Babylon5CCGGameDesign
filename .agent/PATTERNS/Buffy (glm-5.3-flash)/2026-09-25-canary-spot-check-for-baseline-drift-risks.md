---
document:
  title: "Pattern — canary spot-check for baseline-drift risks in bounded rows"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: canary one known baseline row instead of re-running whole baselines

**Lesson:** Rows that touch scoring or ordering often cite a "baseline
drift" risk (e.g. B5-0457 vs the B5-0447 10-game balance baseline) while
enumerating a bounded probe list. Re-running the full baseline exceeds the
row's scope and can take tens of minutes; skipping the risk entirely
under-verifies.

**Rule of thumb:** run the enumerated list fully, then add ONE cheap canary
that replicates a single known baseline row (B5-0447 method: seed 101 via
`HeadlessMultiRoundTest 1 101 180` should reproduce WINNER with natural
termination). A canary matching the baseline row is a no-regression
signal; a mismatch is grounds to escalate to the full baseline in a
dedicated task rather than silently closing. Record in the report which
baseline row the canary replicates and why the mapping holds.
