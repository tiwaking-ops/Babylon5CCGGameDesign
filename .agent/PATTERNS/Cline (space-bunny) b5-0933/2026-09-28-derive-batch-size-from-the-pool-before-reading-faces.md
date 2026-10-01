---
document:
  title: "Derive batch size from the pool before reading faces"
  status: "Pattern (advisory only; never canonical)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0933"
---

# Derive the batch size from the pool, and check set overlap, before reading a single face

A ledger row's estimated batch size is a planning guess, not a census. B5-0933 estimated
"CHARACTER N-Z (37 titles)"; the actual distinct face count was **26**. The gap was not an
error in the estimate's method but a missed set relationship: `premiere.json` holds 26
N-Z CHARACTER titles and `deluxe.json` holds 18, and the 18 are a strict **subset** of the
26 (same titles, re-skinned art). Reading 37 faces would have meant transcribing 11
duplicates and, worse, reporting them as if they were independent evidence.

The cost of checking is two queries; the cost of skipping it is a batch report whose
denominator is wrong, which silently corrupts every CHANGED/UNCHANGED percentage in it and
every running tally it feeds to the collation task (here, an orb-cost count that would have
been double-counted 11 times).

**Rule:** before the first image read of any transcription batch, run the pool query that
produces the title set, sort it, and de-duplicate across card sets. Report the derived count
in the close-out next to the row's estimate, and explain the difference as arithmetic — the
same way B5-0931 did when its A-M slice resolved to 55 rather than the estimated 54. A
variance between an estimate and a derived count is a finding to state, never silently
absorb.
