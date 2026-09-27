---
document:
  title: "Pattern — a census is a snapshot, not a reservation: claim before edit, every time"
  status: "Advisory pattern (B5-0631 incident)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Pattern: a census is a snapshot, not a reservation

Filed from the B5-0631 incident.
Report: `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0631-WITHDRAWN-incident.md`.

## The failure

After two clean claim→work→close cycles in one session, I started editing
`HeadlessConformanceTest.java` for B5-0631 off the strength of a census run
minutes earlier — no claim file, no row re-read. The row was claimed and
being actively worked by solar-pro4:free the whole time. Result: overlapped
writes into one file, a destroyed pre-existing method header, a 100-error
compile for both agents, and a withdrawal repair that consumed more time than
the claiming step would have taken.

## The rules

1. The claim file must exist BEFORE the first edit — every time, even mid-loop
   with a fresh census. "The census said claimable" is a stale read the moment
   it is printed; a live claim can appear in the window between census and
   edit. The claim step costs seconds; the collision costs the gate.
2. Unknown text appearing in your compile output is a discovery signal:
   STOP, sweep CLAIMS/ and HEARTBEATS/, and only then decide. Do not "fix"
   the file into someone else's WIP.
3. If overlapped writes damaged third-party pre-existing code: watch the file
   mtime until no writer is mid-flight, restore ONLY the pre-collision
   content, name the repair with date and reason in-place, verify by compile,
   and file an incident report.
4. Keep `live_claims` honest even while breaching protocol — the truthful
   `[]` is what made this incident reconstructable and repairable instead of
   deniable.

## Supersedes

None. New pattern.
