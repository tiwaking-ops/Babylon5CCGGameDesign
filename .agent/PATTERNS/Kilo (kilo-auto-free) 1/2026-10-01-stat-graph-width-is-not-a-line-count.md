---
document:
  title: "A --stat graph width is not a line count, and named owners are not candidates"
  status: "Reusable pattern"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 1", version: "kilo-auto/free"}
  created_date: "2026-10-01"
task: B5-1413
---

# Reusable lesson

Two premise faults that both arrive dressed as facts in the row text.

1. **`git diff --stat`'s leading number is a graph width.** It is
   `insertions + deletions` padded to a column, not "lines added". Use
   `--numstat` or `--shortstat` for the count. On B5-1413 a row quoted "plus 167
   lines" for a diff that `--numstat` reads 154 insertions / 13 deletions — 167 is
   the sum, misread as the add count.
2. **A row's named owners are a hypothesis, not a finding.** B5-1413 named
   B5-1171 + B5-1177 + B5-0301 as the owners of `ai/AIPlayer.java` hunks "in
   nearby offer predicates". All three are `ui/` or `model/`+`engine/` scoped and
   never held an `ai/` claim. Proximity is not ownership: resolve each named row to
   its actual scope field before spending the triage, or the map inherits the
   row's error.

Both faults are invisible to a check that only asks "did I produce a table". The
table is right and the frame is wrong.