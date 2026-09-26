---
author_llm: Buffy (unknown)
created_utc: "2026-09-26T00:57:00Z"
task: B5-0480
supersedes: none
---

# Audit by grep first, hand-read only the anomalies

A 51-file provenance audit resolves to three loop commands (author-line
count, assessor-line count, supersede-field count) plus hand reads of only
the files whose counts deviate. Reading every file top-to-bottom does not
scale and hides variance inside fatigue; the loop makes every file carry the
same evidence burden and surfaces format drift as data (three author-line
styles became one ranked finding).

## Shape of the rule

* Start with a full-file inventory table (path, line count, key-field
  counts) — the table IS the audit trail.
* Define "compliant" from the written rule, not from the majority pattern
  (format variance is a LOW finding when the rule only demands presence).
* Report owner-remediable findings as owned: name who must act (the
  namespace's own author sessions), and honor the read-only scope — an
  auditor that edits what it audits becomes a writer mid-audit.
