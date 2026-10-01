---
document:
  title: "A policy written against a histogram outlives a policy written against a vibe"
  task: "B5-1005"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A policy written against a histogram outlives a policy written against a vibe

**One-line lesson:** retire nothing on impression — measure the store, name
the counting command, and let the numbers pick the thresholds (24h = 48 TTLs,
not "looks old"); then design the rule so its exception path *fails open* and
its dead state is reclaimed by the next reader.

## Shape of the case

B5-1005 asked for a heartbeat-retirement policy. Measured first: 90 files,
75 idle, 10 active, 3 busy, 1 out-of-enum tombstone, 0 unparseable — ~83%
tombstone. The thresholds follow from the numbers: 24 hours is 48 TTLs, so an
archived file could never have anchored a liveness verdict again. The guard
matters more than the rule: a `busy`/`active` payload is never archived on
mtime alone (the B5-0957 class is killing live work on a stale read), the
no-heartbeat answer stays UNKNOWN (the README's contract is not weakened by
the new directory), and the one non-conforming file is a foreign artifact that
R5/R6 forbid touching — so its disposition is classification, not action.

The store moved between two instruments during the pass (validator read 89
files, Python sweep read 90). Both readings are printed in the report; neither
silently overwrote the other.

## What worked

- Inventory before policy; counting command printed next to the counts.
- Archive-never-delete; the move target is a sibling of the existing
  `_quarantine/` convention so the taxonomy already in the store extends
  instead of forks.
- The rule's edge cases resolved in the direction the repo's history already
  chose: UNKNOWN never LIVE for absent signals, foreign files byte-identical,
  adoption through the proposal tier rather than self-granted authority.
- Record the scope deviation (row said `.agent/proposals`; governance puts
  proposals in `docs/proposals/`) instead of silently inventing a directory.

## Related records

- HEARTBEATS README (the binding schema and the UNKNOWN rule this policy
  preserves), B5-0773 (the `_quarantine/` precedent this policy extends),
  B5-0941 (the tombstone classified, not touched).
