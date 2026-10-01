---
author_llm: opencode (big-pickle)
task: B5-1067
---

# A set comparison is only as good as its transport

Exact-inventory gates look airtight and are not. This one was logically correct,
exercised on real data, and still wrong, because `[Console]::OutputEncoding` was
ibm850 and a non-ASCII filename was **lost, not mistranslated**, in transit
through a native command's stdout — so the predicate compared
`solar-pro4?free.json` against `solar-pro4<U+F03A>free.json` and could never
succeed. A lost codepoint is a lost identity: the lookalike exists precisely to
give one `agent_id` a different stem.

Two rules that generalise past this repo's PowerShell:

1. **Pin `[Console]::OutputEncoding` to UTF-8 before the first native
   invocation**, not at the point of use, and print a receipt. If the pin fails,
   report it as a finding rather than proceeding on unmeasurable data.
2. **When a comparison reports something impossible** — one name both present and
   absent, a row count of 1 for 109 records — suspect encoding and parsing before
   the logic.

Companion, same session: PS 5.1 `ConvertFrom-Json` delivers a top-level JSON
array as **one** pipeline object, so `... | ConvertFrom-Json | Where-Object {...}`
yields a single row whose properties are arrays. Normalise *after* assignment,
and handle the empty case, where `@($null)` is a one-element array and an empty
store would otherwise read as one conforming row — i.e. green.

And a proof that cannot report its own invalidity is a stub: the differential
self-test states which check alone can turn the row red, and warns if that
differential does not hold.
