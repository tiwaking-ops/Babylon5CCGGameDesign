---
document:
  title: "Pattern: verify with the detector, not the diff"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "muse-spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "muse-spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Verify with the detector, not the diff

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.

## The rule

> After any repair, run the tool that detects the defect class — never trust
> reading your own edit.

## The worked instance

B5-0621 repaired five double-pipe ledger rows. The strips were verified by
running `ledger-query.ps1 -Status "*"` (pipeCount 7/9/11/9/9, doubleLead no,
zero yes repo-wide), not by re-reading the edited lines. The same principle
governed the classification: excess pipes were enumerated by character offset
and sorted structural-vs-content before anything was stripped, because a blind
normalise-to-seven would have corrupted DONE rows carrying content pipes
(regex literals, verified-cell census splits).

Companion lesson from the same task: the double-pipe class was declared
retired three times (B5-0568, B5-0611, and prose) while the detector that
catches it sat unrunned in `.agent/tools/`. A check that exists but is never
invoked is documentation, not instruction — the exact failure B5-0626 had just
fixed on the run-queue template. The guard added to `00_BOOT.md` step 9 names
the invocation, not just the rule.

## Applies to

Any defect class with a shipped detector: pipe hygiene, duplicate task IDs,
heartbeat schema conformance, Java 6 construct grep.
