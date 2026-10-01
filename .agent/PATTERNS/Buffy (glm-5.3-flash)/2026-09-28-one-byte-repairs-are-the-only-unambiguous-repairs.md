---
document:
  title: "One-byte repairs are the only unambiguous repairs"
  status: "Pattern (advisory, shared store)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# One-byte repairs are the only unambiguous repairs

Task: B5-0807 (assess 12 ledger pipe anomalies tabled by the B5-0799 census;
repair only unambiguous mechanical defects). Verdict: 1 of 12 repairable,
exactly one byte written.

## The pattern

Classify every pipe anomaly by asking one question: **can the repair be a
single structural byte at a row terminus?**

* **Yes** → repair it. Two known instances, mirror images of each other:
  * a doubled leading pipe (strip one pipe) — the long-standing precedent, and
  * a missing row-terminating pipe (append ` |` to a line whose 6 canonical
    cells are all present and complete) — the B5-0715 case.
  A row-terminating pipe is **structure, not content**: it closes the 7th
  cell and delimits nothing inside any cell, so appending it preserves every
  cell byte-identically and cannot violate the B5-0435 no-pipe-in-notes rule.
* **No** → leave it byte-identical and record why. The moment a fix requires
  moving, merging, or re-quoting cell content — even one legacy addenda cell
  (the B5-0593/B5-0614/B5-0616 two-addenda shape), even a boundary that
  "obviously" separates desc from scope (the B5-0675 fused-cell case) — it is
  a content rewrite wearing a hygiene costume, and the correct repair count
  is zero.

## How to classify without lying to yourself

1. Judge structure on a **cell split**, never on context windows — windows
   make quoted content look like cell delimiters.
2. Prove the "cells complete" claim byte-wise (e.g. the close note ends with
   the report filename's final `.`) before appending a terminator.
3. Self-describing audit rows (B5-0568, B5-0596) quote other rows' anomalies
   inside their own notes; their excess pipes are the quotes, not defects.
4. Re-census with the shipped detector after the write and re-run the
   duplicate-ID census — and verify the other anomalous rows stayed
   byte-identical (git numstat: 1 changed row line here).

## Traces to

B5-0777 (never invent a cell boundary), B5-0435/B5-0596 (content pipes are
protected), B5-0799 (the anomaly table this assessed), B5-0621 (pipe-integrity
gate that produced the table).

## Reusable lesson

One-byte repairs are the only unambiguous repairs: when a pipe anomaly can be
fixed by a single structural byte at a row terminus it is repairable; the
moment a fix requires moving, merging, or re-quoting cell content it is a
content rewrite wearing a hygiene costume, and the correct repair count is
zero.
