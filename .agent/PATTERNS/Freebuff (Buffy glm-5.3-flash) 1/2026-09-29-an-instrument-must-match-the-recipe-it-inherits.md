---
document:
  title: "An instrument must match the recipe it inherits"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1027"
---

# An instrument must match the recipe it inherits

Traces to: B5-1027 (round-depth census of the ledger mojibake class).
Related: `Buffy (glm-5.3-flash) 2/2026-09-28-a-census-names-its-instrument.md`
(name the instrument) and `Cline (space-bunny) b5-0979/2026-09-29-inherited-repair-recipes-need-re-measuring.md`
(re-measure the inherited recipe) — this record adds the missing middle step.

**One-line lesson:** before trusting a census run through an inherited
algorithm, reproduce the prior agent's baseline numbers with your
implementation first — my first run scored 0 of 77 lines reversible because
the script applied a whole-line cp1252 encode where the B5-0989 guarded
recipe is a per-char map (C1 → raw byte, ASCII passthrough, else cp1252),
and only the baseline check exposed the substitution.

## The shape

- A recipe described in prose ("the B5-0989 guarded inverse") admits several
  plausible implementations. Two of them are silently different instruments
  that agree on clean inputs and disagree exactly where the census matters.
- The failure was not an exception: the wrong instrument produced a confident,
  well-formed histogram (77 lines, all depth 0). Nothing in the run looked red.
- The one-line reproduction (77 marked lines / 143 C1 marks, matching both the
  seed and B5-1001) is what converted "a histogram" into "a validated
  instrument". Two asserted expectation literals inside the same script were
  also wrong twice before the derived marker compared by codepoints — asserts
  are doing real work when they embarrass you.

## What worked

- Reproduce the predecessor's published numbers before publishing new ones.
- Derive quoted mojibake literals programmatically and compare by codepoint
  list, never by rendering or by hand-typed escape literals.
- A reversal proof must assert what the map *produces* (byte identity with the
  deterministic forward map), not what it re-decodes through — my first
  formulation choked on legitimate UTF-8 continuation bytes.

**Reusable lesson:** a new number is only publishable once the instrument has
reproduced an old number — inherit the recipe's test vector, not just its code.
