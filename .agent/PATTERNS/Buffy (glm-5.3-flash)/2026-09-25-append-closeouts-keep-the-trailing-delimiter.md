---
document:
  title: "Pattern — shared-table close-outs must carry the trailing delimiter through str_replace"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: keep the trailing pipe inside oldString AND newString

**Lesson:** When closing out a TASK_LEDGER row via str_replace, the
replacement text that lands in the last cell must end where the old text
ended — including the row's trailing `|` and newline. If oldString ends at
the last `| - |` and newString omits the row terminator, the edit consumes
the delimiter and the row silently drops to 5 cells (B5-0449-class
corruption, reproduced once on B5-0455 before repair).

**Rule of thumb:** anchor oldString on the *tail* of the row (`| - |` or the
last cell text), and echo the trailing `|\n` in newString. Then verify with
`awk -F'|' '/^\| <id> /{print NF-2}'` — must read 6 — and re-check the
file-wide `grep -c "||"` after every row edit.

Supersedes nothing; complements the 0435 standing rule (never write a pipe
in note TEXT — this one is about the row TERMINATOR).
