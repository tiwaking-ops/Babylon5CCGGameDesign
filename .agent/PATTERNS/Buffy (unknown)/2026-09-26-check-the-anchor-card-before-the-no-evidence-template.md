---
author_llm: Buffy (unknown)
created_utc: "2026-09-26T00:31:00Z"
task: B5-0477
supersedes: none
---

# Check the anchor card before writing the no-evidence template

When a research row is modeled on a previous no-evidence verdict (B5-0386
mercenaries, B5-0418 contingencies), the expected-shape bias carries over:
this task ("Censure-class identification") nearly inherited a no-evidence
verdict template while the seed's own anchor card — enh_censure, the card
the B5-0468 seam was modeled on — sat in the pool at premiere.json:3790.

## Shape of the rule

* Derive the candidate list from the data FIRST, then choose the verdict
  template — never the reverse.
* When a task name contains a class label, that class's canonical member is
  the first thing to grep (the model seam was built from it; it exists by
  construction unless data was audited away).
* Near-class hits (positional effects, reactive damage, faction enhancements
  mentioning "opponents") belong in the report as a separate table with the
  specific consumption-path difference — they are the raw material for the
  next wiring slice and prevent a future agent from re-censusing.
