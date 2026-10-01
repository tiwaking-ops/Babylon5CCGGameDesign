---
document:
  title: "Measure recovery coverage per row, and tolerate layout variants before counting prose"
  status: "Pattern — advisory only, never canonical"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1509
supersedes: null
---

# Measure recovery coverage per row; tolerate layout variants before counting prose

Two independent traps from the same census (B5-1509, the DECISIONS truncation
reconstruction inventory), both of which produce a confident wrong number.

## 1. Source-class presence is not recovery coverage

The incident entry named four surviving sources. Checked one at a time, every one
of them is present and healthy:

- HEAD blob: 5036 lines, parses, ends `2026-09-28`.
- Ledger: 718 rows, 614 closed, 97% carrying close-out prose.
- Reports: 792 files, 297 distinct task ids in the loss window.
- Patterns: 521 files across 113 namespaces.
- The restoration block: 148 entries, each with a named provenance line.

Every class-level check passes. The number that matters is different: **35 closed
rows appear nowhere in DECISIONS at all**, and 13 rest on exactly one source.
Neither is visible to a check that asks "does this source survive?" — both are
invisible precisely *because* each source survived.

The rule: **the unit of a recovery census is the row, not the class.** Ask "for
how many individual items do at least two sources survive", which yields 239/252
and a named 13-row backlog. Class presence yields a boolean that was already
known and carries no work.

Corollary for any incident recovery: a source class being intact says nothing
about whether the *event* was reconstructed from it. Compilation and use are
separate steps and only the second produces coverage.

## 2. A shared table written by many writers has layout variants — tolerate them first

The same census read the ledger's close-out prose from a fixed cell index. **19
closed rows carry a shifted layout**: the close-out text sits in the owner cell
and a bare date (`2026-09-30`) sits in the note cell. Read positionally, those
rows' notes measure 10 characters — empty.

The first run therefore reported **31 single-source rows**; re-measured with a
layout-tolerant read it reported **13**. The error was 18 rows of manufactured
crisis, in the pessimistic direction, and nothing about the run looked wrong.

The rule: **before counting prose in a shared table, census the layout variants.**
If writers disagree about which cell holds what, a positional reader silently
converts "recorded elsewhere" into "not recorded". Taking
`max(len(cell5), len(cell6))` is a crude fix; the right fix is to know why the two
layouts exist and report the variant count alongside the prose count, so the next
reader can see that 19 rows were handled rather than assumed uniform.

The failure mode is shared with the first lesson and worth naming as a pair:
**both traps make the measurement look complete while being wrong.** Class
presence overstates coverage; positional reading understates it. Neither produces
an error, a null, or an implausible number. A census that reports only its
headline is reporting whichever of the two it happened to be.

**Reusable lesson:** in a recovery census the unit is the row and the reader must
tolerate every layout its writers produced; a boolean about a surviving source is
not a number about surviving content, and prose that moved cells has not
disappeared.