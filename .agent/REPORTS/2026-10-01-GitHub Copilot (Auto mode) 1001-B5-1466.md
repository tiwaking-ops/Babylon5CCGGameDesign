---
document:
  title: "B5-1466 close-out report — future-dated claim census refusal"
  status: "DONE"
provenance:
  author_llm: {name: "GitHub Copilot (Auto mode)", version: "Auto mode"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# B5-1466 — Future-dated claim census refusal

## Change

`ledger-query.ps1` now classifies a parseable claim whose `started_utc` is more
than 60 minutes ahead of the sampled UTC clock as `UNKNOWN`, with a reason that
names the overshoot, tolerance, and never-LIVE policy. Claims within the
60-minute tolerance retain the existing reported-age behavior. The
`census-crosscheck.ps1` replica applies the same boundary so parity remains
measurable. The existing `run-queue.ps1` B5-0952 offer refusal was left
unchanged.

## Verification

An isolated fixture with a shared heartbeat produced `LIVE` for a claim 30
minutes ahead and `UNKNOWN` for a claim 90 minutes ahead. The latter reason
included the 60-minute tolerance and stated that it is never LIVE. The
cross-check reported `CONSISTENT` for both rows. PowerShell parse checks,
duplicate-ID census, ledger pipe inspection, and `b5ccg/compile.bat` all passed.
The live cross-check retains its pre-existing B5-1109 pipe-shape disagreement;
no unrelated row was changed.

## Reusable lesson

A refusal boundary is incomplete until the reporting census emits the same
`UNKNOWN` verdict as the offer path; otherwise a negative age remains visibly
`LIVE` even though it cannot be offered.
