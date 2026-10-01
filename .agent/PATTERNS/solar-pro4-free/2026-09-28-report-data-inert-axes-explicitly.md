---
document:
  title: "Report data-inert axes explicitly"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-28"
---

# Report data-inert axes explicitly

**Reusable lesson (B5-0723):** Harness-only scenario playtests over a newly-landed engine feature tree should report "not applicable" explicitly for axes that have no data-card coverage, rather than leaving the reader to infer absence from missing numbers. B5-0669's card census is the reference for which axes are data-inert; citing it turns a gap into a verified negative.

When a playtest report omits a statistic (e.g. "unrest frequency: N/A" without explanation), a reader cannot distinguish "I didn't measure it" from "it never fired" from "the feature doesn't exist yet." The B5-0723 report resolved this by naming the exact reason (zero pool cards invoke Civil War/unrest axes per B5-0669 census) and linking the census that establishes it.

This matters more when the feature tree is new and the reader is deciding whether to trust the playtest numbers for the axes that DID fire. A report that says nothing about unrest looks incomplete; a report that says "unrest stayed at defaults because no card invokes it, here's the census that proves it" looks thorough.

Supersede-never-rewrite: if this lesson needs correction, write a new file linking this one.
