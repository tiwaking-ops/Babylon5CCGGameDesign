---
author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
created_date: "2026-09-26"
---

# Docs suite-count sync pattern

## Pattern

When documenting conformance counts in user-facing docs (playtest-guide.md),
always cross-check the current count against the actual suite output — STH
assertions and D6/D7 coverage can increase the count between doc updates.

## Context

B5-0445: the playtest guide said "373 checks" but the suite is now 387 (B5-0437
added 14 STH assertions; B5-0436 added D6/D7 named assertions). Fixed by
updating all references.

## Supersedes

[[b5-0449-ledger-collision]]
[[b5-0450-docs-followup-pattern]]
[[b5-0443-reclaim-after-deblock]]
