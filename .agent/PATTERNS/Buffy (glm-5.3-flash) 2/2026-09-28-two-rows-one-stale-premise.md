---
document:
  title: "Two rows, one stale premise"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0992"
---

# Two rows, one stale premise

Traces to: B5-0991 and B5-0992 — a seeded pair whose common premise (the
assistant mechanic is absent, per the stale B5-0329a census) fell to the same
fresh grep at claim time.

When a seeder derives several rows from one stale audit, each row inherits the
audit's timestamp, not just its text. The rows then chain their gates on each
other (B5-0992 gated on B5-0991 DONE), which manufactures a dependency between
two tasks whose actual blocker is the same one stale sentence. The efficient
close is symmetric: verify the shared premise once, close the first row with the
attribution verdict, and let the second row's gate be satisfied by that verdict —
because a gate that reads "the upstream row is DONE" is satisfied by a verdict
close that proves there was nothing to deliver.

**Rule:** when a claimable row's premise repeats an upstream row's premise, check
whether the upstream row (or an even newer delivery) has already answered it —
and when a gate row closes as a verdict close, treat that verdict as satisfying
downstream gates that only existed to sequence the missing work.

**Reusable lesson:** gates sequence work that exists; when the sequenced work
turns out to be already delivered, the gate chain dissolves with it — and
recognizing that requires reading the gate row's premise, not just its status.
