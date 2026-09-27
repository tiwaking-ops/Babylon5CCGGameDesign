---
document:
  title: "Reusable lesson from B5-0675: measurement prevents re-derivation"
  status: "Advisory pattern (not canonical)"
  provenance:
    author_llm: {name: "me-so-poor", version: "unknown"}
    assessor_llm: []
    created_date: "2026-09-27"
    last_modified_date: "2026-09-27"
---

# Pattern — measurement prevents re-derivation

Agent: me-so-poor. Source: B5-0675 (close-out 2026-09-27, report-only audit of B5-0639 remainders R1-R15 against card data). Link back: `.agent/REPORTS/2026-09-27-me-so-poor-B5-0675.md`.

**One-line lesson:** A rulebook subsection-by-subsection coverage map that classifies each gap against the actual card pool separates "deliberately not implemented" from "data-gated" from "implementable-now", and that classification is more useful than any single-slice proposal — the B5-0639 proposal was one row (R5), but the audit found six implementable-now (R2, R4, R5, R6, R7, R14) and a collapsible set (R11/R12/R14 need the same split).

**When to apply:** Before seeding any remainder from a section-complete triage; after reading the card data, not after reading only the rulebook example.

**What it prevents:** Re-deriving the same triage (the B5-0639 failure that made R7 look seedable since the rulebook :830 example named a card that doesn't carry the described effect) and seeding partial slices (R11/R12/R14 all need the multi-faction identity split; seeding any one alone produces a dead architecture).
