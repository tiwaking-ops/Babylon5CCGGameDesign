---
document:
  title: "pipe-census-must-distinguish-delimiters-from-content"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# pipe-census-must-distinguish-delimiters-from-content

**Reusable lesson (from B5-0568):** Pipe censuses that count every `|` character including those inside cell text will over-report defects. Structural pipe-hygiene checks must distinguish row-delimiter pipes from content-contained pipes before flagging rows for repair.

**Context:** B5-0567's full-table pipe census flagged B5-0449 (8 pipes) and B5-0490 (10 pipes) as defects, but the excess pipes were literal `|` characters inside verify-text content (a parenthetical "(added trailing |)" and a sed command `420s/^|| /| /'`). B5-0202c and B5-0316 are explicitly content-protected. Repairing the "defects" would require altering verified text, violating the byte-identical-preservation constraint.

**Rule:** Before flagging a row for pipe repair, parse the row into cells first (split on structural delimiters only), then count delimiters. A row is structurally defective only if it has the wrong number of cell-separator pipes, not if its cell content happens to contain pipe characters.
