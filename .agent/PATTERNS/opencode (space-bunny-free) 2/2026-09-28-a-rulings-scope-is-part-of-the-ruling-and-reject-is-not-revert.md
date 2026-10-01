---
document:
  title: "A ruling's scope is part of the ruling, and reject is not revert"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0787"
---

# A ruling's scope is part of the ruling, and "reject" is not "revert"

**Pattern.** When a human rules on a **document** rather than on a **change**, the
document's parts keep separate fates. Two failure modes follow, and both look like
obedience:

1. **"Rejected" read as "revert".** A rejected proposal may have already delivered a
   component that is in daily use. Reverting it removes a working tool nobody asked you
   to remove, and the status field is where the over-application happens.
2. **"Approved" read as a licence for whatever the document deferred.** A design
   approval supplies the *design*, not the *content*. If the document explicitly
   reserved one question for a human, approving the document does not answer it.

**What separates right from wrong in both cases.** Thirty seconds of measurement plus one
sentence of explicit scope. In this instance: `rg` over the two liveness tools showed the
rejected four-key conjunction and prefix matching were **never implemented**, so the
rejection was a record rather than a reversal — while the same proposal's Task 1 cross-check
**had** landed and was in daily use, and stayed. And the approved design document's own
§4 said its data half "requires a human IP-safe data decision before any text is
authored", so the approval was recorded as design-only and the card question was named as
still open instead of being resolved by invention.

**Why scope decays silently.** A status field is one line. The exceptions are not, and
nothing reads them unless someone writes them down. The mitigation is boring and
effective: state the non-action *in the artefact itself* — the rejected proposal's status
paragraph now names the landed component that was deliberately left alone — so the
sentence that prevents over-application travels with the sentence that could cause it.

**A sharper version of the prefix case, because it is the generalisable part.** Prefix
matching was refused not on taste but on a measurement of what it would do to a rule
settled hours earlier: `opencode (space-bunny-free) 2` and `… 3` share a prefix, so a
prefix join is strictly weaker than the exact normalised join, in the one component whose
job is to resolve identity. **A proposal should be read against what has been decided
since it was written, not only against the tree it was written for.** Two rulings landed
in one session on the same subject pointing opposite ways; reading the older proposal
without that context would have produced a direct contradiction inside a day.

**How to apply.** On any ruling, write three things before acting: what exactly is
approved or rejected, **which parts are explicitly not in scope**, and what you verified
to establish that the ruling requires no reversal. Then put the non-action in the artefact,
not only in the report.

**Supersedes / relates.** Extends `a-record-whose-only-content-is-its-own-length-is-not-a-history`
and `adopting-a-direction-is-not-performing-the-migration` (this session's namespace): both
are about a status that does not match the work, this one about a status that does not
match the *scope*. Per `AGENTS.md` §6 this record is advisory and confers no authority by
being cited.
