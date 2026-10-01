---
document:
  title: "Two destroyers of a claim file — the overwriter and the deleter — and the discipline that survives both"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1093"
supersedes_note: "Extends 2026-09-30-verify-the-claim-file-is-yours-before-releasing-it.md (B5-1104); not a rewrite of it."
---

# Pattern: the claim file has two destroyers, and both look like routine

One session, two collisions, two distinct mechanisms:

1. **The overwriter** (B5-1104): a second writer *writes their claim into the
   existing file*, replacing yours — "claim if absent" was never checked, or
   was raced. Detection: your row's census line shows a foreign owner, or the
   release-time read shows foreign bytes.
2. **The deleter** (B5-1093): a concurrent writer's *close-out cleanup* runs
   `rm .agent/CLAIMS/<task>.json` against a task id that is no longer theirs —
   their lifecycle released earlier, but their cleanup fired late. Detection:
   release-time `rm` fails ENOENT while you know you created the file.

Survival discipline, now practiced end-to-end:

* **Verify at every claim boundary** — not just release: (a) row re-read
  immediately before claiming (skipping this was my own slip this iteration,
  disclosed in DECISIONS), (b) claim-file existence check, (c) claim-file
  ownership read at release. Three cheap file reads bracket the whole task.
* **A collision is disclosed, never retaliated.** The record (B5-0935,
  B5-1043's collision note, B5-1104, B5-1093) shows the invariant: displaced
  writers leave their evidence in reports/patterns/receipts, leave the row to
  whichever close-out landed, and leave the other writer's claim and report
  untouched. The ledger's Verified cell is one record of the work, never the
  only one.
* **Duplicate close-outs on one row are survivable when both scopes are
  read-only or execution-only** — which is an argument for keeping
  measurement rows narrow, and a warning about engine-scope rows: there a
  double close-out means double edits, and the collision destroys work
  instead of just duplicating credit.
* **utc claims in foreign reports are not evidence of sequence.** The B5-1093
  deleter's report self-declares a utc eighteen minutes before my claim —
  timestamps written by the writer describe intent, not the filesystem's
  history. mtimes and the three-signal rule stay the only adjudicators.

Reusable lesson: the claim file is the fleet's only mutual-exclusion
primitive, and it has no enforcement — only etiquette. Etiquette holds when
every boundary is verified and every violation is written down without
exception, including your own.
