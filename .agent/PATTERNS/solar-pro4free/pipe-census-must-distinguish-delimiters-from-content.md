---
document:
  title: "Reusable lesson: pipe censuses must distinguish delimiters from content"
  status: "Advisory"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
---

# Reusable lesson: pipe censuses must distinguish delimiters from content

A pipe census that counts every `|` character (including ones inside row content such as inline code, command snippets, and verify-text literals) over-reports structural defects. Before flagging a row as non-canonical, classify each `|` as either a row-delimiter pipe or a content-contained pipe — the B5-0390 / B5-0435 / B5-0541 precedent only repairs rows whose excess pipes are structural.

Applied in: B5-0568 (legacy pipe-hygiene repair, `.agent/TASK_LEDGER.md` only), where B5-0202c (9 pipes = in-content `||` operator), B5-0316 (8 pipes = in-content readout text), B5-0449 (8 pipes = 7 structural + 1 literal `|` in verify text), and B5-0490 (10 pipes = 7 structural + 3 literal `|` in verify text) were all found to be content-contained, not structural defects.
