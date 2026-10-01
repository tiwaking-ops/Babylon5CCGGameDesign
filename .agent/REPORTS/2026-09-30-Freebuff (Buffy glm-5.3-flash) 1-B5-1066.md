---
document:
  title: "B5-1066 — completing the half-closed row: status cell only, foreign close-out byte-identical"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T05:17:56Z"
---

# B5-1066 — finish the row through the cycle, not around it

## The state found

The Verified cell already carried opencode (big-pickle)'s complete 2026-09-30
DONE close-out — proposal filed, 17-component manifest re-verified, claim
released — while the **status cell still read OPEN** and the Claim cell was
empty. This is the exact half-closed-row class the ledger's B5-0807 note
records (B5-1003 closed the same shape in the seed wave).

## What this pass did

1. Claimed the row (OPEN status + no claim file = claimable; the census had
   offered it all session).
2. **Verified their evidence on disk, not from the cell:** the report
   [2026-09-30-opencode (big-pickle)-B5-1066.md](../REPORTS/2026-09-30-opencode%20(big-pickle)-B5-1066.md)
   exists; [lookalike-filename-r5-exception-proposal.md](../../docs/proposals/lookalike-filename-r5-exception-proposal.md)
   exists with `author_llm: opencode (big-pickle)` and every row-demanded
   section present — the 17-component manifest (§2), the four
   ruling-settles items (§3), the B5-0773/B5-0793 precedents (§3.4/§4), the
   case for granting (§4) and the stronger case against (§5).
3. Flipped **only the status cell** OPEN→DONE; appended one attribution
   sentence inside the Verified cell naming who completed what and when.
   Zero bytes of their close-out text altered.
4. The 17 U+F03A components: untouched, no renames, no moves; ruling (b)
   stays with the human exactly as the proposal records.

Post-write gates: row reads `7 | no`, run-dup-census exit 0, claim released.

## Convention drift observed (recorded, not repaired in-claim)

This session's own 2026-09-29 flips put close-out text in the **Claim**
cell with a bare date in **Verified** — the reverse of the canonical
agent-named-Claim + dated-Verified shape. Structurally legal (7 pipes,
doubleLead no) and now historical record; left byte-identical and flagged
in DECISIONS for whoever owns the next row-convention repair.

**Reusable lesson:** a half-closed row is not a permission slip to redo the
work — it is an instruction to verify the existing close-out and finish the
mechanics; and evidence lives on disk, not in cells.
