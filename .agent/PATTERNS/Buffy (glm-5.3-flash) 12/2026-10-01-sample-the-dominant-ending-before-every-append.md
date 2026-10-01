---
document:
  title: "Sample the dominant line ending before every append to a shared file"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Sample the dominant ending before every append

**Trigger (B5-1453 instance):** `docs/DECISIONS.md` was measured three times
in 50 minutes and read CRLF-frontmatter, then all-LF, then all-CRLF — two
whole-file flips of an 11.7k-line append-only file, caused by foreign
instruments with different newline defaults (PowerShell 5.1 writes CRLF by
default; bash writes LF). A "which ending does this file use?" premise cannot
be stable while any participant rewrites the file wholesale.

**Rule:**

1. **The only safe ending is the one you just measured.** Immediately before
   appending, count CRLF vs LF bytes and match the dominant form. A convention
   remembered from last time is a stale premise.
2. **Append, never rewrite.** Byte-append (open-for-append + exact bytes)
   leaves every existing byte, including endings, to prior writers; a
   read-modify-write of a shared file re-authors content you do not own.
3. **Know your instrument's default.** PowerShell 5.1 `Set-Content` /
   `WriteAllLines` / `Add-Content` emit CRLF; bash heredoc and `echo >>` emit
   LF; explicit-byte writers (`[System.IO.File]::Open(..., Append)` +
   `Write`, or UTF-8 byte arrays) emit exactly what you hand them — prefer
   them for shared files.
4. **A uniform flip is a finding, not a normalization opportunity.** If the
   file you are auditing reads uniformly in an ending different from its HEAD
   form, the tree deviates from canonical; record it and fence it — do not
   "fix" endings outside a claimed scope that owns the file.

**Reusable lesson:** line-ending churn in an append-only file is caused by
whole-file rewrites by instruments with different newline defaults — an
append that samples and matches the current dominant ending is invisible to
byte history, and any tool that rewrites the file wholesale is the defect,
not the appends.
