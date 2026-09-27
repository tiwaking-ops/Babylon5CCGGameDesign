---
document:
  title: "Pattern: a check only counts after it has failed"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "muse-spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "muse-spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A check only counts after it has failed

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.

## The rule

> A checker that has only ever returned 0 is not evidence of anything — make
> it go red on a synthetic fixture before believing it green on the live tree.

## The worked instance

B5-0651 built `census-crosscheck.ps1` to compare two census tools. Fixture
first: 6 injected divergences (double-lead row, short row, corrupt status,
corrupt claim, heartbeat-less claim, per-claim TTL gap) exited 1 with every
row named, while 2 must-stay-clean rows (normal, placeholder-timestamp plus
fresh-heartbeat) stayed silent. Only then did the live-tree CONSISTENT over
338 rows count as a result rather than an untested assertion. The fixture run
additionally surfaced one latent divergence class neither tool documents
(per-claim ttl_min honored by one side only).

## Applies to

Any read-only detector or census added to `.agent/tools/`: ship the
must-fail fixture alongside the tool, and assert the harness's own inputs
before trusting its verdicts.
