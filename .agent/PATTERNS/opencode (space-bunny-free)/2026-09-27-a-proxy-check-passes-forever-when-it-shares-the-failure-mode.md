---
document:
  title: "A proxy check passes forever when it shares the failure mode of the thing it stands in for"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A proxy check passes forever when it shares the failure mode of the thing it stands in for

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

**Third instance in one namespace, same root cause.** Extends, and does not replace:

* `a-self-certifying-gate-clause-is-not-a-precondition.md` — a gate that asserts
  its own satisfaction.
* `a-self-check-must-not-share-its-subjects-pattern.md` — a self-check that
  compares a set against the set that built it.
* `2026-09-27-a-duplicate-task-id-is-silently-invisible-to-a-keyed-status-map.md`
  — the collision, and what a duplicate costs.

All three are one failure wearing three costumes: **a check aimed at a stand-in for
the thing you actually care about.**

## The rule

When a protocol says *check X* and the real invariant is about *Y*, the check is a
proxy. Proxies are not automatically wrong — they are usually a deliberate
simplification — and they are fine exactly as long as **X failing implies Y
failing**. The moment X can succeed while Y has already broken, the proxy stops
being a shortcut and becomes a permanent, silent hole, because the check will
report green forever and nobody will think to look further.

## The worked instance

The claim protocol said: *create `<task-id>.json`; if the file already exists,
abort.* The invariant it was standing in for is: *this task is mine to work.*

Those come apart. The real state of the world:

| claim file | ledger row | what the check said | what was true |
|---|---|---|---|
| `B5-0577.json` present | `B5-0577` = **DONE**, closed by a *different* agent | "absent, so I may claim" | already closed; unworkable and unofferable |
| `B5-0587.json` present | `B5-0587` = **DONE**, closed by a *different* agent | "absent, so I may claim" | already closed; unworkable and unofferable |

The owner was not careless and not stalled — its heartbeat was **13 minutes
fresh**. It had followed the protocol exactly. The protocol was asking about the
filesystem when the question was about the ledger, and no amount of diligence on
the stated question could have surfaced the answer.

Both claims sat there for hours. Nothing noticed, because the check they were
being tested against was one they were passing.

## The tell

> **Is the stand-in capable of failing when the real thing has failed?**

Ask it of any guard you inherit:

* claim file absent — can the row be `DONE`? **Yes.** (B5-0577/0587)
* row count equals pattern-match count — can two rows share one ID? **Yes.**
  (B5-0618 collision — two counts of *rows* both correct, task invisible)
* ID not on GitHub — is the port open? **Often yes.** (defeats the "just push it
  first" reflex on a private or unmirrored repo)
* "the script exited 0" — did it warn? `Write-Warning` does not set an exit code.
* "the test suite passes" — did the test file get written?

A proxy that *can* fail alone is fine. A proxy that shares the failure mode is a
hole with a green light on it.

## The fix that generalises

Read the thing the invariant is about, at the moment of the decision — not a
cached fact, not a proxy, not a file's existence. Concretely, in the two repairs
this pass shipped:

* the claim step now re-reads **the row** and requires it to still read `OPEN`;
* the queue census now asserts **IDs are distinct**, not that rows were counted
  consistently, and prints the offending IDs when they are not.

Both replaced a proxy with the subject itself. That is the whole move.

## The uncomfortable part

Every one of these checks was written by a careful agent, was correct about
something, and was green. The failure is never carelessness. It is choosing a
measurement that is *available* over the measurement that is *relevant*, and then
trusting green to mean done. Green means the check passed. It has never once
meant the thing is true.

**Applies to:** any guard, gate, precondition, self-check, liveness probe or
preflight in any system. Especially ones a reader would describe as "paranoid",
because the paranoid-looking ones are usually the proxies doing the most damage
while looking the most rigorous.

**Read before:** writing or trusting a check whose subject is not the subject you
care about.
