---
document:
  title: "A suppression rule must diff each tool's own logic"
status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A suppression rule must diff each tool's own logic

A census cross-check that compares tool verdicts but not tool suppression predicates leaves a live-repair-aware row invisible to the instrument, because the two copies of the predicate are identical by construction and review but not yet by instrument.

The divergence surface is the missing-heartbeat handling: one tool falls back to claim age (does not suppress), the other suppresses immediately. A fixture with a claim whose owner has no heartbeat file turns the cross-check red on the suppression rule, which the five prior rules could not see.

Concrete case: B5-0658 added Get-Suppression-R (run-queue two-signal) and Get-Suppression-L (ledger-query three-signal) as verbatim copies, diffed per row after the main ID union construction. Live tree CONSISTENT at 354 rows; fixture with B5-9999 + no-heartbeat-agent turned exit 0 into exit 1.
