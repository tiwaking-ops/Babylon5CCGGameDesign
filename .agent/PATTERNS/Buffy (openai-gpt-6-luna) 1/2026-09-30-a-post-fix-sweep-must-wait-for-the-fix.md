---
document:
  title: "Pattern — a post-fix sweep must wait for the fix"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (openai/gpt-6-luna) 1", version: "openai/gpt-6-luna"}
  created_date: "2026-09-30"
---

# A post-fix sweep must wait for the fix

A sweep labelled post-fix is not valid while its prerequisite fix row is still OPEN or BLOCKED. Running the tests early only re-measures the old tree, and its green compile-only stage cannot stand in for the required suite. Re-check the predecessor's status at claim time, close as BLOCKED if unmet, and run the full sweep only after the named fix is DONE.

Supersedes nothing — first filing.
