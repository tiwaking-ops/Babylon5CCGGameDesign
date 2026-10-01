---
document:
  title: "A table row is a format contract"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A table row is a format contract

**Task:** B5-0970 (playtest-guide fidelity review, 2026-09-28).

**The trap.** Reviewing a docs expansion for *fidelity* (are the claims true?) caught
every rulebook anchor — and would have shipped a corrupted document, because the
defect was *structural*: a replacement table row opened with a double pipe (the
ledger's own B5-0568 double-lead class, migrated into a guide), two orphan `||` rows
trailed it, and a new blockquote was inserted mid-table, splitting the table so every
following row rendered as a headerless fragment. Content-true, layout-broken.

**Two passes, different instruments.**
1. *Claims pass:* re-read every anchor against the canonical source (nine rulebook
   lines, including one long line whose exact phrase — "average tension (rounded
   up)" — settled a paraphrase question). Citations in this repo's docs cite by line
   number, so they are mechanically checkable, and checking them is cheap.
2. *Structure pass:* for any edited table or blockquote region, verify the row's
   pipe count and lead, that inserted prose did not land between rows, and that the
   table still has one header. The same defect class the ledger guards against with
   pipe censuses lives in prose documents with no census tool — the reviewer is the
   census.

**And a date-scoped guide inherits the working tree, not the last commit.** "Describes
the working tree as of 2026-09-28" includes *uncommitted* bytes — so an absolute
sentence like "victory and scoring paths still read getInfluence()" can be stale on
arrival against dirty-but-DONE work. Prefer scoped sentences ("no *economy* call site
was switched; AI scoring reads follow B5-0703") that survive the next landed row.
