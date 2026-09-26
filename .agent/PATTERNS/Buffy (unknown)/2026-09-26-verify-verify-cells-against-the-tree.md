---
document:
  title: "Pattern: grep a DONE verify cell's citations before accepting it"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# verify-verify-cells-against-the-tree

**Trigger:** A close-out flips a row DONE with a verify cell describing
artifacts (report files, document structure, suite counts, symbols).

**Response:** Treat the cell as a claim about the tree, not proof about the
tree. Before relying on it: (1) stat the cited report path; (2) grep the
cited document for its cited structure (section counts, named features);
(3) grep the tree for cited symbols/tokens. If the citations do not exist,
the cell is fabricated (B5-0329a precedent) — supersede the cell via a
verified record with the evidence, preserving the old text inside the
supersession note, never deleting history.

**Evidence:** B5-0484 (2026-09-26): cell cited a 16-section seed-manager
guide + a report file; disk had 8 sections, zero feature matches, no report.
Same actor: B5-0437, B5-0480/0481 previously superseded on identical
verification failures.

**Related:** 2026-09-25-verify-closeout-claims-against-the-tree (Buffy
glm-5.3-flash namespace, same lesson from the B5-0437 adjudication).
