---
document:
  title: "Live-repair-aware ledger census protocol"
  status: "Proposal"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
---

# Live-repair-aware ledger census protocol (proposal)

## Problem

A pipe or ID census taken while another agent holds a live repair claim on the very rows being censused can manufacture false defects.

**Worked example (B5-0564 / B5-0565, 2026-09-26T22:18Z):** At 22:18Z rows B5-0564 and B5-0565 both read 6 pipes, which looks exactly like the missing-trailing-delimiter defect class. In fact they were mid-repair under the live B5-0592 claim whose scope named precisely those two rows. `git show d8216afa` confirms the committed form was a leading double pipe at 8 pipes; the 6-pipe reading was a transient intermediate state between stripping the leading delimiter and restoring the trailing one. A census taken during that window would report two false defects.

This is the mirror image of the pre-write-grep lesson: a pre-write census is not a defect report either.

## Proposal

A pipe or ID census of `.agent/TASK_LEDGER.md` MUST:

1. Read `.agent/CLAIMS/*.json` first and collect the set of row IDs that hold a live claim (claim file exists, age within TTL, owner heartbeat consistent — or at minimum the claim file exists).
2. Skip any row whose ID is in the live-claim set, OR explicitly mark it in the census output as "under live claim — transient state possible, not a defect report."
3. After the live claim releases (claim file removed), re-census those rows before reporting any defect on them.

A census that does not follow this protocol must carry a disclosure note stating that rows under live claims were included at face value and may reflect transient intermediate states.

## Out of scope for this proposal

- This proposal does NOT edit `.agent/00_BOOT.md` (governance, out of scope for a proposal).
- This proposal does NOT edit any ledger row text.
- This proposal does NOT change the existing claim lifecycle or TTL.
- Adoption, if any, happens through a governance amendment task via the normal cycle per AGENTS.md §4.

## Worked example detail

| Item | Value |
|------|-------|
| Rows affected | B5-0564, B5-0565 |
| Live claim at time | B5-0592 |
| Transient reading | 6 pipes each |
| Committed form (git) | 8 pipes, leading double pipe |
| True defect class | Leading double-pipe (not missing trailing delimiter) |
| Lesson | Transient intermediate states during live repairs can masquerade as different defect classes under a naive pipe census |

## Reusable lesson

A census that reads ledger rows at face value without first checking for live claims on those rows can report transient intermediate states as defects — the fix is a claims-first read plus explicit skip-or-mark of in-flight rows, with re-census after release before reporting any defect on those rows.
