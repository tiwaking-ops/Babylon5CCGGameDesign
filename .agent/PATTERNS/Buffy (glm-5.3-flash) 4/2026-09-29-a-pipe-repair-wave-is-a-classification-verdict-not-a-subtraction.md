---
document:
  title: "A pipe-repair wave is a classification verdict, not a subtraction"
  task: "B5-1000"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A pipe-repair wave is a classification verdict, not a subtraction

**One-line lesson:** when a task enumerates N pipe-defect rows, the N rows are
suspects, not convicts — split each row on its pipes, adjudicate every extra
delimiter as content (quoted text, backticked commands, code operators) or
structural, and expect the honest outcome to be "most are protected."

## Shape of the case

B5-1000 enumerated 12 rows the census read at non-7 pipe counts. Field-split
evidence showed:

- 7 rows carry extras strictly inside quoted content — several matching the
  B5-0568 no-op verdict recorded years of ledger-time ago, and three rows even
  carry their own prior adjudication ("left intact per content-protection
  rule") which repairing would falsify;
- 1 row (B5-0675) was the only *additive* case — a missing scope delimiter,
  repaired by inserting `| - |`, the seed-wave placeholder convention;
- 1 row (B5-0941) had one genuinely structural excess — a delimiter between
  date and note cells — repairable by merging with zero text change.

The wave closed with 12 → 10 non-7 rows and ten rows byte-identical.

## What worked

- Re-derive the set with the shipped detector at claim time (the row demanded
  it; a concurrent wave made the set mobile).
- Split-and-adjudicate before editing: for every extra pipe, record the
  70-char context on both sides; let the surrounding quotes and backticks make
  the content/structural call mechanical.
- Prefer the repair that changes no text character at all (delimiter insertion
  or removal at a cell boundary) and refuse any edit that would alter quoted
  verify text.
- Stand down explicitly on rows with prior in-row adjudications, recording the
  re-census-after-release precondition check that licensed the stand-down.

## Related records

- B5-0568 (the adjudication this wave confirmed), B5-0596 and B5-0613 (rows
  whose own text is the protection evidence), B5-0621 (post-write pipe gate).
