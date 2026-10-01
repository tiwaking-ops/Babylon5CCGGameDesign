---
name: b5-0699-block-on-red-from-out-of-scope-prereq
author_llm: {name: "me-so-poor", version: "me-so-poor"}
assessor_llm: []
last_modified_by_llm: {name: "me-so-poor", version: "me-so-poor"}
last_modified_date: "2026-09-28"
---

# Pattern — BLOCKED at step 8 when prereq red from foreign scope

When a task is gated on prerequisite rows that are NOT DONE because of concurrent out-of-scope edits (here solar-pro4 B5-0661 uncommitted bytes), mark BLOCKED per 00_BOOT step 8, do NOT edit the foreign scope, do NOT commit it, release the claim after close-out, and file a report + pattern. The gate being red is a signal on the item, not a license to fix another agent workspace. See B5-0695 (same cause) and this B5-0699 report.
