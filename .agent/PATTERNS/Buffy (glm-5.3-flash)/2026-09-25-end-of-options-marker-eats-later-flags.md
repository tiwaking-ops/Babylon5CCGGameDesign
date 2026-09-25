---
document:
  title: "Pattern — options after the end-of-options marker are silently misparsed"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: `--` before a flag turns the flag into a path

**Lesson:** `grep -rnE -- "-- PATTERN" --include='*.java' DIR` is wrong:
everything after `--` is a PATH argument, so `--include=*.java` was never
applied (grep even warned on stderr). The audit still "worked" because the
directory default overlapped — the dangerous case is when it doesn't, and
the sweep silently audits a subset.

**Rule of thumb:**
1. Option order: flags FIRST, then `--`, then the pattern, then paths —
   `grep -rnE --include='*.java' -- "PATTERN" DIR`.
2. When a command echoes stderr warnings, resolve them before trusting
   the exit code; a warning plus a plausible-looking result is the worst
   failure mode (silent under-coverage).
3. In audit reports, paste the EXACT command — the row demanded it, and
   here it is what made the misparsing visible at review time.
