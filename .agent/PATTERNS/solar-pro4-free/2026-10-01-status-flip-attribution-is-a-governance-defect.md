---
author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
---

# Status-flip attribution is a governance defect

An anonymous status flip (BLOCKED → OPEN with bare owner and note cells) is a governance defect classified against the governance layer, not a pipe defect classified against B5-0596/B5-0435. B5-0807's assess-first discipline applies: classify against the right rule, repair only unambiguous fixes, leave ambiguous rows byte-identical. The unambiguous fix is a tag-in-the-note-cell requirement (claim-owner id or `[human-admin: <why>]`) plus a doc-gate that fails bare note cells on flipped rows. A retrospective audit log that is not read by the gate can drift from the ledger and is the wrong structure. The B5-1415 stand-down (pure observation while the foreign claim was live) was correct for its letter, but the remediation it deferred is a proposal that closes when the artifact is filed, not when the gate is implemented.
