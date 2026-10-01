---
document:
  title: "Disposition policy for permanently-flagged content-pipe ledger rows (proposal, B5-1061)"
  status: "Proposal (not truth until merged; B5-1020 owns the detector change this contract describes)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1061"
---

# Disposition policy for content-pipe ledger rows

Measured census (instrument: `ledger-query.ps1 -Status "*"`, 2026-09-29T09:28Z,
post-claims-first footer checked): exactly **10 rows** read non-7/doubleLead —
**9 content-pipe rows** (B5-0316/8, B5-0449/8, B5-0490/10, B5-0568/15,
B5-0593/9, B5-0596/15, B5-0613/11, B5-0614/9, B5-0616/9 — all DONE, UNCLAIMED,
doubleLead no, excess pipes strictly inside quoted content per the B5-0568
classification) **plus the one structural row** B5-1022 (8 + doubleLead yes),
which B5-1039 (OPEN) already owns for repair. B5-1020 (OPEN) owns any detector
change. B5-1000 (DONE) already adjudicated the 12-row ancestor of this class,
repairing 2 and leaving 10 byte-identical with a long note each.

## The three answers, costed in re-adjudication already spent

- **Already spent on this class: ~2 full sessions** (B5-1000's adjudication
  wave; the B5-0568-era sweep that named the pattern), plus a per-census noise
  tax: the detector re-flagged the same rows in **every** ledger-query run
  this session (8+ runs), and every future census re-pays it.
- **(a) Detector exemption list** — hardcoded row IDs read as
  `EXEMPT-ADJUDICATED`. Build ~5 lines; but it creates a second hand-maintained
  list, hides a real regression on an exempted row (a future bad edit reads
  clean), and *still* touches B5-1020's file. Cheap once, expensive forever.
- **(b) In-row adjudication marker the detector reads — RECOMMENDED.** Each
  content-pipe row's verdict cell gains the token `ADJUDICATED-KEEP: <one-line
  reason>` (a content edit inside the existing cell; **no pipe added, so the
  7-pipe gate is untouched**). The detector prints `adjudicated` instead of a
  defect for rows carrying it. Self-maintaining: any genuine repair rewrites
  the cell and drops the marker naturally, so the row re-enters the defect
  population the moment it actually changes. Build: one migration wave (10
  one-line cell edits, half a session) + one parse (~10 lines) inside
  B5-1020's already-claimed detector change.
- **(c) Permanent reportable noise** — zero build cost; permanent ~10-row tax
  on every census plus alert fatigue, the failure mode where the next *real*
  defect (like B5-1022's doubleLead, which IS real) hides among ten known
  ones. Measured spend says this is the most expensive option long-term.

## The one slice (proposed, not applied)

1. One seed-wave row: add `ADJUDICATED-KEEP: <reason>` to the 9 content rows'
   verdict cells (byte-preserving otherwise), B5-1022 excluded (B5-1039
   repairs it, after which it needs no marker).
2. B5-1020's detector change adds: `if verdict-cell contains
   ADJUDICATED-KEEP → report class 'adjudicated', not 'defect'` (and the
   CLAIMS-FIRST-style footer counts them separately).
3. Nothing else changes: the 7-pipe/lead-pipe gate, dup census, and
   suppressed-live-claim logic are untouched.

## Related

B5-1020 (detector owner), B5-1000 (the adjudication this policy encodes),
B5-1039 (the structural row), B5-0568 (the classification rule the marker
cites), `docs/proposals/blocked-prereq-token-action-ready-proposal.md` (the
same pattern: make a prose convention machine-readable).
