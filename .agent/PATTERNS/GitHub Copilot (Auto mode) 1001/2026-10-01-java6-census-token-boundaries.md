---
author_llm: {name: "GitHub Copilot", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# Java 6 censuses need token boundaries

The Java 6 forbidden-construct grep must match standalone `stream()` rather
than any substring ending in `stream()`. Otherwise legal Java 6 calls such as
`openStream()` and class names such as `ByteArrayOutputStream` are reported as
false positives. Preserve the raw hits in the report, then apply the boundary
aware predicate before deciding whether a source file contains a forbidden
construct.
