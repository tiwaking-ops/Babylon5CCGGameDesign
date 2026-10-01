---
author_llm: me-so-poor
task: B5-1347 pattern
utc: "2026-09-30T23:56:54Z"
status: "advisory"
supersedes: none
---

# Recount the derived quantities, not the raw ones

## Pattern

When independently re-deriving a predecessor census, spend the reader on the
**derived** numbers — coverage, undispatched exposure, helper semantics, per-set
splits — and re-derive them from source rather than from the predecessor's table.
Raw counts are usually the part that was already right.

## Why

On B5-1347 the second reader differed from B5-1153 in how it selected records
(structured parse keyed on `type` vs grep for the `triggerCondition` key). It then
confirmed every headline number: 117 records, 14 values, and all six per-value
counts. That confirmation was worth almost nothing, and section 1 of the receipt
shows why — **0** non-aftermath records carry a `triggerCondition` key and **0**
aftermath records lack one, so the two selection strategies provably select the
identical set. The agreement was arithmetically guaranteed before the recount
started.

Every real divergence sat in the derived layer:

- the predecessor's per-set split was a `57 / 60` total row contradicting its own
  per-value columns, which sum to the measured `59 / 58`;
- undispatched exposure was **113 records / 14 values**, not the reported
  **103 / 12** — the gap was two `WON_DIPLOMACY` records (`aftermath_united_front`,
  `de_am_united_front`) that the predecessor marked "4 of 4 handled" while its own
  dispatched-id row listed two;
- a "1 trigger literal, namely `LOST_DIPLOMACY`" claim traced to a **comment** on
  one line plus a **substring** `contains("DIPLOMACY")` test that actually reaches
  12 records across three values, not 4.

## Check to run before trusting a recount

Compare the predecessor's *selection predicate* against yours on the data before
comparing results. If one is a superset of the other, or the two coincide, the
headline agreement is structural, not evidence, and the remaining budget belongs
on the derived numbers and on the consumers of those numbers — here B5-1125's
ranking and B5-1349's backlog, both of which had inherited `12 values over 103
records` and now need `14 over 113`.

## Superseded-by discipline

A corrected pattern is a new file linking the old one; never rewrite the original.
The B5-1153 report was likewise left untouched — reports carry no authority and are
not edited retroactively, so the correction lives in `docs/DECISIONS.md`, in the
ledger result cell, and in this recount.