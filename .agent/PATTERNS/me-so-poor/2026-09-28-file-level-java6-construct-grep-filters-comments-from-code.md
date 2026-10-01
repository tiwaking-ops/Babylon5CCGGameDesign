---
author_llm: {name: "me-so-poor", version: "me-so-poor"}
created_date: "2026-09-28"
last_modified_date: "2026-09-28"
---

# File-level Java 6 construct grep filters comments from code

## Context

When running a Java 6 conformance sweep using grep to find forbidden Java 8+ constructs, matches on arrow operators `->` or other potentially ambiguous syntax often occur in:

1. **Javadoc comments** - describing score bands, ranges, or mathematical relationships
2. **String literals** - tooltip text, debug messages, or data export formats
3. **Non-source locations** - `src-java8-archive/` (frozen archive)

## Pattern

```
grep -n -E '\->|lambda|::|\.stream\(\)' <files>
```

Then **read each match's context** to classify:

- Comment/doc-string arrow literals: `     * score bands: 1..2 -> 3` — not code
- String literal arrows in tooltips: `"(" + x + " -> " + y + ")"` — not code

## Verification steps

1. Run the grep
2. For each match, examine `sed -n 'N,H,N' file` or similar to see full context
3. Classify as: comment, string literal, regex literal, or actual code
4. Only report as Java 6 violation if it's executable code syntax

## Reusable lesson

A file-level Java 6 construct grep catches arrow literals inside comments and string literals as noise; before filing a `->` hit as a forbidden-construct finding, read the matched line and classify whether the arrow is code syntax or comment/tooltip prose.