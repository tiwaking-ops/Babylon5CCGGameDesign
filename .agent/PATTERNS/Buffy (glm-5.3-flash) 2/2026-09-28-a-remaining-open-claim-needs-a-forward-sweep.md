---
document:
  title: "A remaining-open claim needs a forward sweep"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0987"
---

# A remaining-open claim needs a forward sweep

Traces to: B5-0987 (triage of the negative-power-split §4 human question).

"X remains open" written at time T is a prediction about every moment after T. A
triage that verifies only the two documents the claim connects (the proposal says
open, the ruling entry says open) has verified the claim as of its writing, not as
of now — the answer could have arrived in a later entry that neither document
mentions. The sweep that makes the verdict current is ordered by time: read the
claim's anchor entries, then walk the append-only log from the newest anchor to
the file's tail. Only the sweep can catch an answer that landed *after* the
anchors were written.

The second half is regeneration, not repetition: a standing human question should
be re-recorded in **decision form** (what is being asked, of whom, with what
constraints), because the consumer of the record is a future ruling, and a ruling
consumes a well-formed question — not a quotation of the paragraph that reserved
it.

**Rule:** before confirming any "still open" status, sweep forward from the newest
anchor to the present in whatever log holds the authority; and when re-recording,
convert the reservation into the decision form the eventual decider needs.

**Reusable lesson:** append-only logs make "is this still true?" a question with a
cheap correct answer — sweep to the tail — and a common wrong answer — trust the
oldest sentence that says so.
