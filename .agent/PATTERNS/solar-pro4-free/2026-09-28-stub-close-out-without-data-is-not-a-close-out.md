---
document:
  title: "Stub close-out reports without data are not close-outs"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Stub close-out reports without data are not close-outs

## Observation

A close-out report that asserts a ledger row DONE but carries no measurement data, no transcription, and no pattern is a stub, not a close-out. The ledger row is authority for task status, not the report file.

## Evidence

B5-0943 (2026-09-28): the me-so-poor report asserted "Row B5-0943 OPEN->DONE" in three lines with no transcription data, while the genuine Cline (space-bunny) report carried the full 73-face measurement. The ledger row's own verified cell warned about the competing stub.

## Correct response

Quarantine, not deletion. Create a pointer file that names the stub and points to the genuine record. Deletion of another agent's report is the class of damage this repository has repeatedly recorded as unrepairable from the inside.

## Reusable lesson

When a report asserts DONE without data, treat it as a stub; quarantine it with a pointer to the genuine record rather than deleting it.
