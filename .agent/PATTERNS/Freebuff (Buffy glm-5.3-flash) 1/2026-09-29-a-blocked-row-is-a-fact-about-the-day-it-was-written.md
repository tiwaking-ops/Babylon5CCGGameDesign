---
document:
  title: "A BLOCKED row is a fact about the day it was written"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1018"
---

# A BLOCKED row is a fact about the day it was written

Traces to: B5-1018 (BLOCKED expiry + reason taxonomy).

**One-line lesson:** a BLOCK row's class drifts underneath it as the tree
moves — 0 of 17 rows were ACTION-READY when blocked and 7 of 17 were two days
later — so classify a block by re-reading its named prerequisites today, not
by the letter or the age it carries.

## The shape

- The taxonomy looked stable until it was measured twice. Block time:
  GATE-SHUT 4 / PREREQ-RED 9 / SCOPE-BLOCKED 4 / ACTION-READY 0. Two days of
  ordinary queue traffic later: ACTION-READY 7 (41%). The class "waiting on an
  action anyone could take" did not exist when the rows were written and
  cannot be seen at write time — it only exists later, which is why it
  converts a queue into a graveyard silently.
- The cheap alternative (age-based expiry) measures as exactly wrong: age
  matched 0 of the 7 ready rows and would eventually resurface the one row
  (B5-1001) that is correctly blocked on a human decision. The signal that
  actually finds ready work is "are the named prerequisites DONE now", which
  is also the cheapest thing to check mechanically — the verdicts already
  name their prerequisites; only the format is unparseable.
- One structurally unclassifiable row (B5-0811: no date, verdict text in the
  wrong cell) is the reminder that any migration of this class has a hand-only
  residue, and UNKNOWN never STALE applies to it until a human-shaped claim
  takes it.

## What worked

- Measure the taxonomy twice — at block time and now — the delta between the
  two readings is the whole finding; a single-timepoint classification would
  have reported a stable queue.
- Re-read every named prerequisite's current row rather than trusting the
  block letter's description of it.
- Cost the mechanism honestly, including its casualty (the unclassifiable
  row), against the rejected alternative's measured failure — the weaker
  mechanism won on evidence, not convenience.

**Reusable lesson:** a status is a photograph, the tree is a movie — audit
blocking states by re-reading what they were waiting on, and a taxonomy taken
once is a census of the photograph, not of the queue.
