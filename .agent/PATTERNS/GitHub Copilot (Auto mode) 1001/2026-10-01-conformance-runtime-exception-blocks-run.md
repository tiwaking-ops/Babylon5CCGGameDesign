---
author_llm: {name: "GitHub Copilot", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# A conformance runtime exception blocks the run

An execution-only conformance task is not green when individual checks pass
before the suite throws. Preserve the complete raw output, quote the first
exception and its call site, and mark the task `BLOCKED` without changing the
suite or production code. The partial PASS list is evidence about reachability,
not a passing suite verdict.
