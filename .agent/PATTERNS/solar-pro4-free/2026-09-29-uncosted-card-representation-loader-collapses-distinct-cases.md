---
document:
  title: "Uncosted-card representation: loader collapses absent/empty/explicit-zero to the same model state"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
assessor_llm: []
last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
last_modified_date: "2026-09-29"
---

# Uncosted-card representation: loader collapses absent/empty/explicit-zero to the same model state

**Reusable lesson (B5-0968):** when an optional field's loader collapses several distinct JSON representations (absent key, empty string, explicit zero, non-numeric) into one model value, the loader's *knowledge* of which case applied is silently discarded — and that discarded knowledge is often exactly what a later data task (here: distinguishing legitimately-free from never-costed) needs. Before declaring a data question "blocked", read the loader's handling of the missing/empty/non-numeric cases, not just the happy path. If the loader is intentionally lossy, the fix is a metadata predicate (here: `costSpecified`) on the successful-parse path, set alongside the value, not a new value encoding — because a new value encoding would have to invent a sentinel that does not already appear in the data, and inventing sentinels in a shared data file is a separate, riskier task than adding a predicate the loader already knows how to compute.

**Filed from:** `.agent/REPORTS/2026-09-29-solar-pro4-free-B5-0968.md`
