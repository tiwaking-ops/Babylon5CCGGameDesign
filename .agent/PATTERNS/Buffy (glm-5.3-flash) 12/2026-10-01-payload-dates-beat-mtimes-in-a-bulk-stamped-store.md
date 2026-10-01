---
document:
  title: "Payload dates beat mtimes when indexing a store with bulk-touch stamps"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: payload dates beat mtimes when indexing a store with bulk-touch stamps

**Task:** B5-1427 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

When a pattern-store index (or any "newest X per group" census) must rank
records by recency, filesystem mtime and payload dates disagree in three
measured ways in this store: bulk checkout stamps move many files' mtimes
forward together; a file's mtime can be *newer* than its own frontmatter dates
by days; and namespaces exist whose "newest" file looks fresh while every
payload date inside is stale. Ranking by mtime would have reported zero stale
namespaces; ranking by payload dates reported 6 of 108.

## Practice

1. Take the record date from the filename date where the filename carries one;
   fall back to frontmatter `created_date`/`last_modified_date`; use mtime only
   as a last resort and say so.
2. Read the payload of every undated candidate before trusting its position in
   a recency ranking — 5 files in this index needed it.
3. Indexing passively surfaces governance residue (a hyphen-variant namespace
   twin, a frontmatter-less record); report what the pass sees, fence what
   other rows own (B5-1401/B5-1403 own heartbeat-file retirement), and touch
   nothing the row does not claim.

## Related

* Supersedes nothing; third pattern in this namespace.
* Traces to: B5-1427 report; the rel1008 mtime/checkout-stamp pattern (its
  "prefer payload" rule applied to an index rather than a liveness verdict);
  the space-bunny-free 7 cluster-exclusion refinement (its anchor half is why
  payload dates still get cross-checked against mtime when both exist).
