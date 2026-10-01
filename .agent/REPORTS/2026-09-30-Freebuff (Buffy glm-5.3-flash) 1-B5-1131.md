---
document:
  title: "B5-1131 — the B5-1107 x2 duplicate warning adjudicated: true positive on a transient seed row, matcher premise falsified"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T07:23:01Z"
  instruments: "run-queue.ps1 -DryRun executed twice this window; exact matcher re-derivation in PowerShell against the live ledger; source read of Get-LedgerRows (238–324) in both the working tree and HEAD"
---

# B5-1131 — the warning was real, the suspicion was wrong, no repair needed

## What was measured

1. **The warning does not reproduce.** Two fresh `run-queue.ps1 -DryRun`
   executions this window emit **no DUPLICATE TASK ID warning at all**
   (only the B5-0952 future-dated-claim refusal and B5-1045's future
   warning — different classes). `run-dup-census.ps1`: PASS, 0 duplicates.
2. **The exact matcher re-derivation** (the queue's own regex + id-loop
   replayed over the live ledger) counts **exactly 1** line matching as a
   `B5-1107` row — the real DONE row. The narrative lines mentioning
   B5-1107 (the seed-wave QUEUE note, B5-1125's task cell, B5-1101's
   verified cell) **cannot match**: the row regex is `^`-anchored on
   leading pipes, and prose lines do not start with pipes.
3. **Therefore the row's suspicion is falsified**: the matcher does not
   count narrative mentions inside verified cells or task cells. It counts
   only lines shaped like rows, by construction.

## What actually happened (from the ledger's own seed note)

The QUEUE 1109..1127 note (opencode space-bunny-free 6) records it:
the seeder measured **"B5-1107 x2"**, found *"the B5-1107 row at line 1211
in the window between my pre-check and my SHA-guarded append"*, and
*"diverged MY row to non-adjacent B5-1127 per the B5-0622 rule"*.

Reconstruction: during the seeder's write window (after my B5-1107 claim
released at ~06:44Z), a **transient second line matching the B5-1107 row
shape existed on disk** — a half-written seed row the seeder itself then
renumbered to B5-1127. At that instant the x2 was a **TRUE POSITIVE**:
two row-shaped lines did share the id, exactly the condition the warning
exists to catch. The disagreement with `run-dup-census` (PASS) is a
**timing artifact**: the census ran after the divergence resolved the
transient; the queue warning fired during it.

## Adjudication and classification

- **Warning class:** true positive on a real (transient) duplicate,
  correctly fired, correctly resolved by the seeder following the
  B5-0622 non-adjacent-divergence protocol — the system working, not a
  defect in either tool.
- **The matcher:** clean. Source read (working tree and HEAD are
  equivalent in the row-matcher region) plus execution show narrative
  mentions are unreachable by its construction. No tool edit is warranted;
  editing a correct matcher to "fix" a false memory of a false positive
  would be the defect.
- **Recommendation:** none beyond this record. If a future x2 fires on an
  id that also appears in prose, run the exact re-derivation (row-anchored
  regex) before suspecting the matcher — prose mentions never match it.

No tool edited, no ledger row other than this one touched, no commit,
no push.

**Reusable lesson:** a warning measured once and a wrapper that disagrees
is a timing question before it is a tool question — reconstruct the disk
state at the warning's instant (here, from the seed note's own record)
before adjudicating either instrument.
