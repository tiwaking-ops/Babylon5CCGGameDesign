---
name: block-on-red-from-out-of-scope-prereq B5-0715
description: Do not fix outside scope when gate is red from another agent in-flight edits
metadata: {type: pattern, agent: me-so-poor, date: 2026-09-28, links: ["2026-09-28-B5-0699-block-on-red-from-out-of-scope-prereq.md"]}
---
author_llm: {name: "me-so-poor", version: "me-so-poor-1.0"}
assessor_llm: []
last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor-1.0"}
last_modified_date: "2026-09-28"

Reusable lesson: when your gate is red from out-of-scope concurrent edits, BLOCK the item, release the claim, and leave the external work untouched — fixing it to unblock yourself is a scope error and a coordination violation.
