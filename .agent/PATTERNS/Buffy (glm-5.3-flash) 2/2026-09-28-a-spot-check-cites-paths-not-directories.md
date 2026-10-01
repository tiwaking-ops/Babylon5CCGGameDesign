---
document:
  title: "A spot-check cites paths, not directories"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0975"
---

# A spot-check cites paths, not directories

Traces to: B5-0975 (close-out completeness spot-check of B5-0956, B5-0959, B5-0960,
B5-0961, B5-0962, B5-0963, B5-0964).

A completeness check that scans directories for plausible names gets two kinds of
verdict wrong at once. A directory-level scan of `.agent/REPORTS/` found no report
for B5-0959 or B5-0960 and would have called them misses; both reports exist — in
`docs/reports/`, at exactly the paths their rows cite, because two of the nine rows
filed their reports there deliberately. The same scan would then have found
`docs/reports/java6-construct-census-2026-09-28.md` for B5-0960, seen that its
`author_llm` is `me-so-poor` while its ledger owner is `Buffy (glm-5.3-flash)`, and
called it a provenance defect; reading the file shows it is a deliberate
cross-authored record with the census work credited in `assessor_llm`, consistent
with its row.

**Rule:** when a row promises an artifact, verify the artifact at the path the row
itself names, and read provenance conflicts in the file's own frontmatter before
classifying them. Directory presence is a weaker signal than path agreement, and
author-vs-owner disagreement is a finding to interpret, not automatically a defect.

**Reusable lesson:** a completeness verdict is only as strong as its locator — cite
each artifact by the exact path its row promised, because two of nine legitimate
records here would have failed a directory-level check and one would have been
falsely accused of provenance fabrication.
