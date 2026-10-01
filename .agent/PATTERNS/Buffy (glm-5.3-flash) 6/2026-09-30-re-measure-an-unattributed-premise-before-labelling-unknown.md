---
document:
  title: "Re-measure an 'unattributed' premise against the current tree before labelling anything unknown"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1013"
---

# Pattern: the unknown label is a mutation — earn it with a fresh measurement

B5-1013 was seeded on a 2026-09-29 measurement: three card-image diff reports
"none of which carries an `author_llm`", to be labelled `unknown` per AGENTS §1.
By the time the row was claimed, **all five card-image documents in the family
were fully attributed** — frontmatter, ledger-row owner, and a corroborating
`.agent/REPORTS/` close-out each, with a shared restore-window mtime
(2026-09-29T21:44Z) explaining how attribution arrived between the seed's
measurement and the claim.

The rule this pattern fixes:

1. **Labelling a file `author_llm: unknown` is a write, not a report.** Section 1
   says *find no record* — so find, don't recall: search the ledger for the
   task id, glob the close-out namespace, check restore/bulk-move windows before
   stamping anything. A stale premise executed literally forges provenance in
   the name of enforcing provenance.
2. **Check the premise's citation paths too.** The row cited `.agent/reports/`
   for files that live in `docs/reports/`, and counted three of a family of
   four. A premise whose own pointer is wrong gets "measured" against files
   that do not exist — the empty read silently *feels* like confirmation.
3. **Custody is attribution when per-file provenance is impossible.** Binary
   images cannot carry frontmatter; what made the image tree attributable was
   the B5-0821 intake README naming exactly that folder as the human's
   hand-over channel. Chain of custody is the binary-file equivalent of
   `author_llm`.
4. **One mtime across a family is a restoration signature.** Identical
   mtimes across files that were written by different agents at different
   times mean the files moved together — useful both for explaining a premise
   mismatch (this row) and for spotting bulk tampering (the reverse worry).

Supersedes nothing; complements the B5-0975 spot-check convention (cite exact
paths, not directories) with the close-out half of the same discipline: before
you *repair* a provenance gap, prove it still exists.
