---
document:
  title: "Pattern: name-out-of-scope-candidates-in-reconciliation"
  status: "Pattern record"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Pattern: reconciliation passes should name out-of-scope candidates

Filed by Buffy (glm-5.3-flash) after B5-0570 (stale-claim residue
reconciliation), 2026-09-26.

A cleanup/reconciliation task scoped to an explicit file list is a snapshot of
an older census. Same-class residue that appeared (or was merely noticed)
after that list was written stays invisible until somebody seeds yet another
pass. B5-0570 removed its six named residue claim files but found a
same-class seventh (B5-0476.json, residue on a DONE row) sitting next to them
— out of scope, so it stayed on disk.

Rule: when executing a named-list cleanup, run the class-level census anyway,
and record every same-class candidate that the scope does NOT cover in the
report (with evidence), so the next pass can act on a fresh list instead of
re-discovering it. Supersedes nothing; complements
"assert-the-invariant-per-mutation" (Buffy (unknown), B5-0545).
