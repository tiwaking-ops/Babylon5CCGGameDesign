---
document:
  title: "B5-1061 close-out — the content-pipe disposition policy"
  status: "Close-out report (observation, no authority)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
---

# B5-1061 — Disposition policy for permanently-flagged content-pipe rows

Deliverable: `docs/proposals/content-pipe-disposition-policy.md`. Ledger read
only; detector untouched; no row edited.

## The class, measured (instrument: `ledger-query.ps1 -Status "*"`, 09:28Z)

Exactly **10 defective rows**: 9 content-pipe rows (B5-0316/8, B5-0449/8,
B5-0490/10, B5-0568/15, B5-0593/9, B5-0596/15, B5-0613/11, B5-0614/9,
B5-0616/9 — all DONE, UNCLAIMED, doubleLead no, excess pipes inside quoted
content) + **B5-1022** (8 + doubleLead yes — genuinely structural, owned by
the OPEN B5-1039). The claims-first footer showed 4 live claims, all excluded
from the count. B5-1020 (OPEN) owns the detector; B5-1000 (DONE) already
adjudicated this class once, repairing 2 of 12 and leaving 10 byte-identical
with notes.

## The three answers, costed

1. **Detector exemption list**: ~5 lines to build; a second hand-maintained
   list; hides a real regression on an exempted row; still requires B5-1020.
2. **In-row marker the detector reads — RECOMMENDED**: `ADJUDICATED-KEEP:`
   token inside the existing verdict cell (no pipe added, gate untouched).
   Self-maintaining — a real repair rewrites the cell and drops the marker,
   re-entering the defect population automatically. Build: 10 one-line cell
   edits + ~10 lines of parse in B5-1020's claimed change.
3. **Permanent noise**: zero build; permanent census tax + alert fatigue —
   the next real defect (exactly the B5-1022 doubleLead class) hides among
   ten known ones.

Re-adjudication hours already spent on this class: ~2 full sessions
(B5-1000's wave + the B5-0568-era sweep), plus the per-census tax the detector
re-paid in every run this session (8+).

## The one slice (proposed, not applied)

Marker on the 9 content rows in one seed wave; B5-1022 excluded (B5-1039
repairs it); B5-1020 adds the parse and a separate footer count. Nothing else
changes — the 7-pipe/lead-pipe gate, dup census, and suppressed-live-claim
logic stay as they are.

## Gates

No detector or ledger-row edit beyond this close-out; no src/data edits;
run-dup-census exit 0 post-write; own row 7 pipes / doubleLead no; claim
released; no commit, no push.

## Reusable lesson

A defect report that cannot be acted on trains the reader to ignore defect
reports — when a class is permanently flagged and unrepairable by rule, the
policy answer is a marker that distinguishes "adjudicated, keep" from
"unexamined," so the signal-to-noise of the defect channel survives its own
honesty.
