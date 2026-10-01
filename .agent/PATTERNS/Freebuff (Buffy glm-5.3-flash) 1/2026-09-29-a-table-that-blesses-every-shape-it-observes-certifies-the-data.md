---
document:
  title: "A table that blesses every shape it observes certifies the data"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1025"
---

# A table that blesses every shape it observes certifies the data

Traces to: B5-1025 (card-record schema contract).

**One-line lesson:** a schema contract earns its existence only at the rows
where it says *no* — an inventory of observed shapes is a census, not a
contract, and the UNEXPECTED list is the deliverable.

## The shape

- 13 observed shapes reduce to 9 types with 1–2 shapes each; describing all
  13 as though each were intended would have certified exactly the three
  records that should not exist (2 uncosted enhancements, 1 `timing`-bearing
  event) plus the 12 genuinely ambiguous ambassador rows — and a later
  validating loader built on that description would enforce the accidents
  along with the design.
- The interesting classes were *small*: 12, 2, and 1 records. The dominant
  class (829 conforming to the base) is the one part of the census a reader
  could have guessed. Contracts live in the tails.
- The per-file counting instrument (12 + 13 = 25) double-counts field-sets
  that cross files; harmless here, but a headline built that way will always
  overstate the shape space to whoever sizes a validator from it.

## What worked

- Derive the table from the *consumers* (buildCard's req/intVal/boolVal
  behaviour), not from the data alone — required is what makes the loader
  drop or silently default, forbidden is what no consumer reads.
- Publish the ambiguity instead of resolving it: the ambassador cost rows are
  recorded as "either a deliberate exemption or a gap, engine bills 5 either
  way," with the ruling left to a human — the contract constrains, it does
  not adjudicate.
- Name the loader's two failure modes in the same breath as the table, so the
  validating-loader row inherits requirements, not vibes.

**Reusable lesson:** write the table so that some present record fails it —
that is what makes it a contract rather than a description, and the failing
rows are the finding the whole exercise exists to surface.
