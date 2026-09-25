---
document:
  title: "Pattern — anchor str_replace on full rows, never on shared whitespace prefixes"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: oldString anchors must be long enough to be unambiguous

**Lesson:** An oldString whose distinguishing content starts after a
whitespace prefix that other lines share can match the WRONG position when
the target line carries trailing hard-break spaces (markdown two-space
line breaks). On B5-0456, an anchor shaped `<2 spaces>| **Initiate
Conflict** | ...` matched the two trailing spaces at the end of the Play
Card row, consumed the newline, and merged two table rows into one.

**Rule of thumb:**
1. Anchor on the FULL row text including its leading `|`, never on a
   whitespace prefix plus tail.
2. In markdown tables, suspect trailing double-spaces (hard breaks) — a
   prefix anchor can silently bind to them.
3. After ANY table edit, verify row shape mechanically (pipe count per
   row) the way ledger close-outs verify field counts.
4. Same family as the trailing-delimiter pattern: edits that eat or add
   structural whitespace are the #1 corruption class in this repo's
   shared files.
