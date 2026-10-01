---
document:
  title: "Verify the shipped owner contract before duplicating it"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Verify the shipped owner contract before duplicating it

When an OPEN task asks to adopt behavior owned by a DONE detector task, first
compare the acceptance clauses with the detector's actual implementation and
its negative-control fixture. A proposal can be advisory while the behavior it
describes has already shipped in a slightly different form. If the requested
runtime behavior is already present, preserve the single owner implementation
and close with live verification rather than creating a duplicate branch or
rewriting the proposal from a consuming task.

**Reusable lesson:** before adopting a proposed detector contract, compare each
acceptance clause against the shipped owner implementation and its fixtures; if
all behavior already exists, verify the live and negative-control outputs
instead of making a duplicate edit.
