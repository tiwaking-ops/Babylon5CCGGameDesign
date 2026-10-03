---
document:
  title: "B5-1724 record — the B5-0960 census placement decision, verified"
  status: "Proposal (advisory record; decision already executed by DONE B5-1877, verified by DONE B5-1724)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 18", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-02"
---

# B5-1724 — the B5-0960 census placement decision, verified

B5-1724 asked a either/or question: does the AGENTS.md section 2a
standing Java 6 construct census **move to a tracked tools path**, or
is the section **amended to state that it is an on-demand local
instrument requiring `tmp-scans` present**? The defect it named was
real as of its seeding: the instrument lived only at
`tmp-scans/b50960/census.py`, fully ignored under `.gitignore` line
152 (`/tmp-scans/`), with zero tracked copies, so a fresh checkout
could not run the gate the section calls authoritative.

## The decision (executed by B5-1877 before this claim)

**Move to a tracked tools path.** The census now lives at
`.agent/tools/census-b50960.py`, and AGENTS.md section 2a's standing
command was re-pointed to it in the same change. This proposal records
that decision and the verification of it; it does not propose a new
edit.

## Verification (B5-1724, 2026-10-01T18:44-18:53Z)

* **Unignored and index-tracked.** `git check-ignore -v --
  .agent/tools/census-b50960.py` exits 1 (not ignored); `git
  ls-files -- .agent/tools/census-b50960.py` lists it; `git status
  --short` shows `A` (staged as a new file).
* **Staged, not committed.** `git cat-file -e HEAD:.agent/tools/
  census-b50960.py` fails — the file is in the index but not in HEAD.
  Of the 13 files in the `.agent/tools` index, 11 are in HEAD; the
  staged-only three are `census-b50960.py`, `verify_task.py` and
  `_retired/B5-1014-harness.ps1`. This loop never commits (AGENT_LOOP:
  "This loop does not commit"), so staged-in-index is this repo's
  operational meaning of "tracked"; the commit that makes a fresh
  clone carry the file belongs to a human. That is a governance fact
  to state, not a defect of this task.
* **Instrument integrity.** The tracked copy is the
  `tmp-scans/b50960/census.py` provenance original plus a 10-line
  provenance header; `diff` reports only `1,10d0`, so body lines
  11-98 are byte-identical. The tmp-scans original stays in place,
  untouched, as B5-1877 prescribed.
* **It runs from its new path.** `PYTHONIOENCODING=utf-8 py
  .agent/tools/census-b50960.py` (repo-root cwd) exits 0.
* **Standing numbers reproduce exactly** and match the AGENTS.md
  section 2a standing table: 65 tracked src files (root 1, ai 1,
  engine 21, model 37, ui 4, util 1) and 32 archive files; every
  violation family (arrow, methodref, stream, computeIfAbsent,
  computeIfPresent, compute, merge, @FunctionalInterface,
  try-with-resources, diamond, forEach, removeIf) at code-lines 0;
  arrow prose 50 lines / 64 occurrences; `getOrDefault` 14 hits, all
  the project's own unqualified `DeckLoader.getOrDefault(Map, key,
  def)` helper, with the qualified `Map.getOrDefault(` API signal 0;
  the frozen archive reads the opposite (arrow 14, methodref 9,
  stream 8, computeIfAbsent 2, @FunctionalInterface 1,
  try-with-resources 1, diamond 32, getOrDefault 14), which is the
  instrument's built-in validation.

## Residual caveats (recorded, not repaired)

1. A fresh clone of HEAD lacks the file until a human commits the
   staged index. Section 2a's "tracked since B5-1877" therefore means
   *staged in the index, unignored, present in the working tree*. The
   same caveat covers the other two staged-only tools files.
2. The script feeds `git ls-files` output straight to `open()`, so it
   resolves source paths relative to the current working directory and
   must be run from the repo root — the same constraint as the
   tmp-scans original, not a regression of the move.
3. AGENTS.md's "byte-identical" claim holds for the instrument body;
   the 10-line provenance header is intentional (B5-1877's row
   prescribed it) and is the only difference from the original.

## Disposition

No amendment to an "on-demand local instrument" wording is needed: the
tracked-path decision is executed, sound, and re-measured. No
`.agent/tools`, AGENTS.md, `b5ccg/src` or build-wiring edit was
required by this verification. Recorded in `docs/DECISIONS.md` under
the 2026-10-02 B5-1724 entry; report at `.agent/REPORTS/
2026-10-02-Buffy (glm-5.3-flash) 18-B5-1724.md`; pattern at
`.agent/PATTERNS/Buffy (glm-5.3-flash) 18/2026-10-02-verify-a-promoted-gate-by-running-it-from-its-new-path.md`.
