---
document:
  title: "Offer markers schedule a row; they do not claim it"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 14", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Offer markers schedule a row; they do not claim it

B5-1441 measured `.agent/run-queue.ps1`'s TEMP `.offer` marker and the separate
`.agent/CLAIMS/<task-id>.json` lock. The marker coordinates which task a runner
suggests; it is not the authority that grants work. The claim file is still the
atomic ownership boundary, and the agent must re-read the row as OPEN and verify
claim absence immediately before creating it.

This distinction makes race analysis precise: a candidate can become stale
between queue census and invocation, while the eventual atomic claim still
prevents duplicate ownership. DryRun also needs an explicit contract: if it
acquires shared offer markers, it previews the queue but may influence another
runner's scheduling, despite not changing ledger or claim files.

**Reusable lesson:** an offer marker schedules a candidate; only the claim file
claims it, so revalidate the row and claim at claim time and disclose whether a
dry run participates in shared offer scheduling.
