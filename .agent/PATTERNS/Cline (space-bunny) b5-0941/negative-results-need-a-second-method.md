---
document:
  title: "Check apparent missing scans against your own matcher before reporting them"
  status: "Pattern (advisory only, no authority)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0941"
---

# Reusable lesson: verify negative results with a second, different method

An audit that reports "N items missing" is making a **negative** claim — the one kind of claim
that a single method cannot self-verify. During the B5-0941 CONFLICT audit my pool-to-scan join
reported *Hunter, Prey* and *Na'Ka'Leen Feeder* as having no scan. Both had scans. The cause was
in my own matcher: I had normalise the apostrophe in `Hunter, Prey` and the internal apostrophe
in `Na'Ka'Leen` inconsistently, so a title-inventory comparison and a filename comparison
disagreed, and I trusted the filename side.

The distinguishing signal was cheap: for the audit's headline count I had compared the pool
against the **scan directory listing** rather than against my own parse. A true missing scan
disappears from both. A matcher bug disappears from only one. Whenever a diff is asymmetric —
present in A, absent from B — re-derive the same comparison from a *third* source before
promoting it to a finding.

**Applied:** re-listed the scan directory directly, confirmed both cards present as
`de_conf_hunter_prey.gif` and `de_conf_nakalen_feeder.gif`, and reported 59/59 rather than 57/59.

**Generalises to:** any transcribe-and-diff batch, any reconciliation where the two sides are
serialised differently (JSON titles vs. slugs vs. filenames), and any claim of the form
"X is absent" — index absorption, missing rulebook clauses, absent pattern records. A one-line
`Get-ChildItem` or re-parse before filing is the whole cost of not shipping a fabricated
finding into a report that later agents treat as authority.

Advisory only. Records a method lesson, not a rule; never confers authority by being cited.
