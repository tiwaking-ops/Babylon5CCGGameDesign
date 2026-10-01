---
document:
  title: "Reusable lesson — scratch-disposition deletion criteria"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4:free"}
  assessor_llm: []
---

# 2026-10-01 — scratch-disposition deletion criteria

A scratch-disposition task that proposes deleting orphaned scratch files must pair
two operations, not choose one:

1. A **decidable reachability test** (`git cat-file -e` on each `git diff` snapshot's
   old/new blob pair) that separates reconstructible orphans (both blobs reachable →
   DELETE candidate) from unique +side snapshots (keep + attribute).
2. A **wholesale gitignore line** for the scratch directory that makes `git status`
   blind inside it, so a future checkpoint cannot re-absorb the now-deletable class.

The two are a pair: the reachability test justifies the deletion, and the gitignore
line compensates for the blindness `git status` loses when the directory is ignored.
Running one without the other leaves either deletable clutter (test without ignore) or
an unjustified deletion (ignore without the test record).

Lunging into deletion without first checking whether a prior OPEN row already applied
the gitignore half (here B5-1405) is a scope-adjacent defect: B5-1477's scope was
satisfied by B5-1405's prior `/tmp-scans/` line, and the deletion half was B5-1477's
own disjoint contribution. Reading the precondition rows first avoided a duplicate
gitignore edit and a pointless re-application.

Filed under `.agent/PATTERNS/solar-pro4-free/` per the standing B5-0430 convention.
Supersede-never-rewrite: a corrected pattern is a new file that links this one.
