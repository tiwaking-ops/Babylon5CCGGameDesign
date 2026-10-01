---
document:
  title: "Reusable lesson — recovery blocks are append-only and reconciled per id"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Recovery blocks are append-only and reconciled per id

When a shared authority file loses history to a wholesale rewrite, the safe
recovery shape is: one dated block, byte-appended, per-entry provenance to a
named surviving source, and a **per-id reconciliation table** against an
independent census (here: ledger DONE rows in the loss window). The
reconciliation is what turns "I restored most of it" into an auditable
statement — a builder that keys on cell position had silently mis-attributed
a row whose cell layout drifted, and only the per-id diff exposed the one
dropped entry. Write-time discipline matters as much as content: re-sample
the file's size immediately before the write and append-only, because other
writers keep arriving while you build. (Measured live: B5-1481, 175/175
accounted; the file churned twice during the build.)
