---
document:
  title: "An identity must name the thing that clobbers"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0783"
---

# An identity must name the thing that clobbers

**Pattern.** When a name is load-bearing for a *store* rather than for a *label*, name it
after the thing that will be overwritten, not after the thing that is convenient to
describe. In a one-writer-per-key store the unit of identity is the **writer instance**,
even when every visible attribute of that writer is shared with another instance.

**Where it bit.** `.agent/HEARTBEATS/` holds one file per `agent_id`, and `agent_id` was
de facto `client (model)`. Two live sessions of one client on one model therefore wrote
the same `agent_id` and the second silently overwrote the first's `utc` and
`live_claims`. It stayed invisible because the client half *was* sufficient on the day it
was invented — there was only ever one session per client. The name was correct until the
fleet doubled, and nothing about the name announced the assumption it carried.

**The part that is easy to get wrong.** The obvious fix — "just make the names unique" —
is incomplete, because two *different* names can still collapse to one key downstream.
`Get-NormName` strips everything that is not a letter or digit, so `…free 2` is a
discriminator and `…free _` is not: it strips back onto the base id and the two instances
merge again. Worse, the validator that exists to catch this compares **exact** `agent_id`
strings while the liveness join compares **normalised** keys, so a punctuation-only
variant passes the one tool whose job is to catch exactly that. **A uniqueness check on
the wrong representation of the key is a check that cannot fail.** Measure what the
consumer normalises, and put the discriminator in the part that survives it.

**Corollary, and the near-miss from the same session.** The close-out write computed the
new row into a variable and then serialised the array without assigning the variable back
— a silent no-op that reported `new len=4405` and changed nothing on disk. The only thing
that caught it was reading the row back **off disk** instead of inspecting the variable
that produced it. Same root as this pattern: a check that reads the writer's own state
rather than the world cannot fail, and an identity that names the description rather than
the writer cannot stay unique. Verify against the medium, not against the intention.

**How to apply.** Before adding a key to a shared store, ask *what is the unit I am
naming?* Then check every consumer of that key for a normalisation step, and make the
discriminator survive the **narrowest** one. If the consumers disagree on the
representation, that disagreement is itself the bug — it is the same defect as two tools
computing a verdict from different signals, which this repo has now hit in three
separate places.

**Supersedes / relates.** Extends `write-gates-against-a-re-read` and
`a-self-certifying-gate-clause-is-not-a-precondition` (this session's namespace) with the
store-identity case rather than the gate-clause case. Both are the same failure: a
component that agrees with itself is not evidence. Per `AGENTS.md` §6 this record is
advisory and confers no authority by being cited.
