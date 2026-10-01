---
document:
  title: "Ready-to-apply .gitignore patch for the B5-1008 scratch-disposition proposal"
  status: "DRAFT — NOT APPLIED. One approval sentence (\"Approve B5-1008: apply the patch.\") turns DRAFT into APPLIED and runs the post-write checks below."
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1008 (follow-through)"
---

# Patch: add the `/tmp-scans/` wholesale ignore (B5-1008 proposal, §4)

One hunk, appended at end-of-file after the `/node_modules/` block
([.gitignore](../../.gitignore) currently ends at line 129). The comment
follows the file's house style (B5-0972 block): what the rule is, which task
measured it, what the compensating control is, what the rule does NOT do,
and how to reverse it.

```diff
--- a/.gitignore
+++ b/.gitignore
@@ -127,3 +127,15 @@
 # command-plus-rollback in .agent/REPORTS/2026-09-28-Buffy (glm-5.3-flash)-B5-0972.md
 # and is NOT run here: `git rm` in any form stays behind the human gate.
 /node_modules/
+
+# tmp-scans scratch disposition (B5-1008, 2026-09-30). Wholesale ignore per
+# docs/proposals/scratch-disposition-tmp-scans-proposal.md: measured 306
+# files / 127.6 MB on 2026-09-30, none ignored, so a plain `git add -A`
+# checkpoint would absorb the pile — including a 120 MB card-image OCR
+# workspace (tmp-scans/b50939/) cited as raw-pipeline evidence by
+# docs/reports/aftermath-card-image-diff-2026-09-28.md and a DECISIONS
+# pre-repair backup (tmp-scans/DECISIONS-before-b50989.md, DECISIONS.md:9097).
+# The B5-0955 per-path model measurably rotted: every unit here postdates it
+# and none qualified for a line. COMPENSATING CONTROL: promotion is the only
+# door into the tracked tree — a task that cites a scratch artifact promotes
+# it into .agent/REPORTS/ or docs/reports/ (the B5-0986 flow). This rule does
+# NOT delete anything, does not touch tracked paths, and is reversible by
+# deleting this block. Deletion of individual orphans remains a separate,
+# individually-claimed action per the same proposal.
+/tmp-scans/
```

## Why this shape

- **Wholesale line, not per-path** — the proposal's measured core: ten
  hand-maintained per-path lines (B5-0955, 2026-09-28) failed to cover a
  single one of the 29 units created the next day. The house already uses
  wholesale lines (`/Pene/`, `/ledger.bak`, `/loop-prompt.md`,
  `/node_modules/`), so this is the dominant local idiom, not a new one.
- **Attribution comment is part of the patch** — an ignore line without its
  measured justification is indistinguishable from a leak-hiding rule; every
  neighbouring block in this file carries one.
- **Nothing else changes** — no tracked path is affected (gitignore is inert
  on tracked files), no file is moved or deleted, the evidence units keep
  living on disk exactly where their citations point.

## Pre-application state of .gitignore (measured 2026-09-30T04:30Z)

`.gitignore` is ALREADY an uncommitted-modified file before this patch: the
working tree carries +115/-1 versus HEAD (the B5-0835, B5-0955, B5-0957 and
B5-0972 blocks are prior agents' uncommitted work — the dirty-tree run
convention). Two consequences, both on the page:

- This patch is written against the WORKING TREE (hunk context = current EOF
  lines 127–129), and `git apply --check` is proven green against exactly
  that state.
- Whoever later commits `.gitignore` commits the stack: prior agents' 115
  lines plus these 12. That is a commit-shape decision for the committer and
  is NOT this patch's business.

## Post-write checks for whoever applies it (copy-paste)

```bash
git apply --check <patch>   # must pass BEFORE applying (proven green 2026-09-30T04:30Z)

git check-ignore -v tmp-scans/DECISIONS-before-b50989.md tmp-scans/b50939/analyze.py tmp-scans/b50960/census.py
# expect: three .gitignore:NNN:/tmp-scans/ hits

git status --porcelain -- tmp-scans
# expect: empty (the ?? tmp-scans/ line disappears)

git check-ignore -v b5ccg/probe-b5-0956.txt ; echo "exit=$?"
# expect: exit=1 — the probe file is NOT covered by design (misplaced in the
# source tree; its deletion is separately proposed so git must keep seeing it)

git diff --stat -- .gitignore
# expect: the working-tree delta grows from +115/-1 to +131/-1 versus HEAD
# (prior agents' 115 uncommitted lines plus this patch's 16: one blank
# separator, a 14-line attribution comment, and the /tmp-scans/ rule itself;
# do NOT expect a clean +16/-0 — the base was already dirty)
```

## Rollback

Delete the appended block (16 added lines). Nothing else on disk changes;
untracked contents reappear in `git status` exactly as before. The prior
agents' 115 uncommitted lines are untouched by both the patch and its
rollback.
