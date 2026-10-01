---
document:
  title: "A fixpoint is contract-relative"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0989"
---

# A fixpoint is contract-relative

Traces to: B5-0989 (guarded DECISIONS mojibake repair).

"Drive the damage to a fixpoint" sounds like "remove all the damage", but a
fixpoint is only ever relative to the *detector* the contract names. Here the
detector was "line carries a C1 control" — so reaching the fixpoint (zero C1
lines) leaves visible damage standing wherever an earlier encoding round produced
artifacts without C1 controls (`â`, `’`, NBSP sequences from double-applied
mojibake). Neither the contract nor the repair was wrong: the B5-0979 guard
exists because *blind* extra rounds rewrite clean lines, and unauthorised rounds
are exactly how a repair becomes a rewrite. The honest outcome is a completed
contract plus a filed finding that the visual damage has layers, each layer
needing its own measured scope.

**Rule:** when a repair contract names its detector, reach exactly that fixpoint
and no further — then census the damage the detector cannot see and file the
residue as a scoped candidate, because the difference between "repaired" and
"clean-looking" is itself a finding.

**Reusable lesson:** rehearse the repair on the real file before writing it
(957/957 reversible with zero guarded failures is the receipt that made the write
safe), prove churn with a per-line ASCII projection rather than a count, and
remember that every fixpoint is measured by some detector — ask which one before
celebrating zero.
