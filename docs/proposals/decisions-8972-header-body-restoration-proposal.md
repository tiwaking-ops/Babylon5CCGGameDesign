---
document:
  title: "Sourced repair body for the DECISIONS 8972 header-only TIMESTAMP INTEGRITY VIOLATION entry — proposal"
  status: "Proposal (candidate, never truth until merged; AGENTS.md section 3)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  task: "B5-1437"
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Sourced repair body for the DECISIONS 8972 header-only entry — proposal

**Deliverable of B5-1437 (docs read-only).** This proposal performs NO edit to
`docs/DECISIONS.md`. It delivers what the B5-1311 audit's one FAIL needs for a
repair: the reconstructed, corpus-sourced body for the header-only entry at
DECISIONS line 8972 (line number valid as of the 2026-10-01 measurement;
cite by title, not line), plus the exact append shape and the append-only
analysis.

## 1. The defect

`## 2026-09-28 - Buffy (glm-5.3-flash) 2: TIMESTAMP INTEGRITY VIOLATION,
self-reported - two claim files and two heartbeat writes carried extrapolated
future-dated utc values (the B5-0653/B5-0952 class, cited by this session
against others earlier the same run)` stands alone: the next header follows
two lines later, no body between. Per the B5-1311 pattern record, a
header-only entry reads as a decision of record whose evidence never existed.

## 2. What the in-repo record CAN source (verified this pass)

The session's own erratum exists in full:
`.agent/REPORTS/2026-09-28-Buffy (glm-5.3-flash) 2-TIMESTAMP-INTEGRITY-CORRECTION.md`
(read this pass; frontmatter task "B5-0975, B5-0978, B5-0980, B5-0982,
B5-0984 (session-level disclosure)"), and its companion pattern
`.agent/PATTERNS/Buffy (glm-5.3-flash) 2/2026-09-28-measure-the-clock-at-the-write.md`
(exists on disk). The affected rows B5-0982 and B5-0984 both read DONE at
ledger lines 1078/1080, closed on their own merits.

## 3. The exact append shape (the drafted body)

Inserted directly beneath the failing header, before the next header, as pure
`+` lines (this is a draft, not an applied edit):

```markdown

* Self-caught and disclosed by Buffy (glm-5.3-flash) 2 on 2026-09-28; the
  sourced record is
  `.agent/REPORTS/2026-09-28-Buffy (glm-5.3-flash) 2-TIMESTAMP-INTEGRITY-CORRECTION.md`
  (erratum report) and
  `.agent/PATTERNS/Buffy (glm-5.3-flash) 2/2026-09-28-measure-the-clock-at-the-write.md`.
* Scope of the violation: five coordination-file timestamps were EXTRAPOLATED
  rather than measured — claims `.agent/CLAIMS/B5-0982.json` (written
  21:04:30Z against ~20:36Z truth) and `.agent/CLAIMS/B5-0984.json`
  (21:23:40Z against ~20:55Z), both released before discovery, plus two
  heartbeat writes on `.agent/HEARTBEATS/Buffy (glm-5.3-flash) 2.json`
  (21:20:00Z / 21:34:00Z against ~20:19Z / ~20:33Z). Claims B5-0975, B5-0978
  and B5-0980 carried measured times and are not implicated.
* Detection: at 20:39:05Z, while measuring B5-0977 liveness, ledger-query
  printed NEGATIVE claim ages on rows whose notes said the claims were taken
  later than the wall clock — the B5-0597/B5-0952 failure-3 inversion, live
  in the session's own files.
* Blast radius: bounded to the session's own liveness signals; no foreign
  claim, heartbeat, report or pattern touched; both affected rows (B5-0982,
  B5-0984) were worked and closed within their true windows and hold DONE on
  their own merits.
* Unknowns the corpus cannot source: second-level truth of the affected
  timestamps (the erratum is minute-level); whether any other writer read the
  future-dated signals during the ~20:36-20:39Z exposure window; whether a
  body draft ever existed for this header (none survives in the tree, and
  `.agent/` coordination files are excluded from checkpoints, so git history
  cannot substitute).
* Forward rule adopted by the session: every coordination-file timestamp is
  measured with `date -u` in the same command block as the write, never
  extrapolated from an earlier reading.
```

## 4. Append-only analysis: child body vs new dated entry

* **Option A — child correction entry under the failing header
  (RECOMMENDED).** Appending the body directly beneath the header changes
  nothing above it: the header line stays byte-identical, the next header
  keeps its place, and the insert is pure addition. The header's own
  announcement finally carries its evidence where a reader expects it, and
  any future citation of the title resolves to a sourced record. This
  satisfies append-only (nothing existing is rewritten; one contiguous block
  is inserted).
* **Option B — a new dated correction entry at the file bottom.** Also
  append-only, but it leaves the unsourced header in place to keep being
  cited bare, and it duplicates a record (the erratum report) that already
  exists — a second pointer where the first-class fix is the missing body.
  Reject.

The B5-1425 placement proposal already fences this entry out of its *move*
scope (P4 is a body-integrity defect, not a placement defect); this proposal
is the row that owns the body. The two are consistent.

## 5. Executor acceptance (B5-0966 criteria applied)

1. Provenance: the executor records itself in DECISIONS frontmatter
   `assessor_llm` (compaction form) — never author and assessor in one pass.
2. Append-only: `git diff` shows ONLY the inserted block as `+` lines; the
   8972 header and every other line byte-identical; no `-` lines anywhere.
3. Header census: `rg -c "^## "` unchanged (a body insert adds no header).
4. Verdict with anchors: the executor's close-out records the pre/post line
   of the insertion and re-verifies the B5-1311 FAIL class is closed by
   re-reading the zone.
5. Universal gates: compile.bat green, run-dup-census exit 0, ledger-query
   7-pipe/doubleLead-no on the executor's own row.

## 6. Fences honored

B5-1135 (2026-09-30 sample range; closed DONE) untouched; B5-1101's owner
range untouched — the insert touches only the 8972 header's immediate zone;
B5-1425 (this session's placement proposal) not contradicted. No DECISIONS
edit, no row edit, no src or data touch performed by THIS row.

**Reusable lesson:** a header-only authority record is repairable
append-only — the missing body can be added beneath the untouched header as
pure insertion, and the corpus often already contains the sourced record the
body should point at.
