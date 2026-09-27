---
document:
  title: "A rule written down and implemented once is a rule one component follows"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A rule written down and implemented once is a rule one component follows

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

Ninth instance in this namespace. Companions: the self-certifying gate, the
self-check sharing its subjects, the duplicate ID, the proxy check, the
document/enforcer drift, the absence-only suite, the rule you just wrote, the
procedure nothing launches, the stub in the calling language.

## The rule

> A convention that is **documented in one place and implemented in another** has two
> sources of truth, and the documentation is not one of them. Partial adoption of a
> shared rule is not partial safety — it is **two systems wearing one name**, and
> which one answers depends on which component you happened to ask.

The dangerous part is not that the rule is missing somewhere. It is that the rule is
*present, cited, and working* — in one tool — so every audit that samples the
implemented one reports the system as conformant.

## The worked instance

Two shipped tools read the same claim file and disagreed about whether it was live.

A running agent wrote a **placeholder** timestamp (`00:00:00Z`) instead of the real
time, so the claim's own age read 292 minutes against a 30-minute TTL.

| tool | rule | verdict |
|---|---|---|
| `ledger-query.ps1` | newest of claim **and** owner heartbeat | **LIVE** — correct |
| `run-queue.ps1` | `started_utc` **alone** | **abandoned** — would re-offer held work |

The B5-0597 lesson ("liveness is the newest of claim, heartbeat and report") was
already written, and already **implemented in `ledger-query.ps1`**. The second tool
simply never received it.

Consequence: the queue would have handed a task held by a working agent to a second
claimant — the duplicate-delivery failure the entire claims protocol exists to
prevent — and nothing anywhere was red.

## Why the audit missed it

Every check that mattered sampled the *compliant* component:

* the lesson was filed, so "is this documented?" → yes;
* `ledger-query` was tested, so "does a tool implement it?" → yes;
* `run-queue` existed, was exercised daily, and **worked perfectly at its own job** —
  it just answered a different question.

Nothing in the system was capable of saying "these two disagree", because nothing
compared them. A rule enforced by two independent implementations is only correct
while they happen to agree, and nobody is watching for the day they don't.

## The fix, and the part that generalises

Align the second tool — and take the shared helper **verbatim** from the first:

```powershell
# Copied verbatim from .agent/tools/ledger-query.ps1 (Get-NormName) on purpose:
# the two tools must agree, and a second hand-rolled variant is how they came to
# disagree in the first place.
```

**"Ship one implementation and import it" is the actual lesson.** The bug was never
the started-utc logic. It was that the logic existed twice. Correcting both copies
would have left the same trap armed for the next rule.

Corollary for any shared rule — TTLs, ID formats, name normalisation, precedence
order: **grep for the concept, not the filename.** If two files mention
`started_utc`, or `agent_id`, or a TTL constant, they are two implementations
whether or not anyone meant them to be.

## One honest divergence, documented rather than hidden

The aligned tools are not byte-identical, deliberately. `ledger-query` prints
`UNKNOWN` when no heartbeat matches the owner, because it *reports* verdicts. The
runner's question is binary — offer, or not — so an owner who never wrote a heartbeat
falls back to claim age, because "not live" there would let one heartbeat-less claim
block its task forever. Where two consumers of one rule need different behaviour, the
divergence goes in a comment at the point of difference, naming both consumers.

**Reusable lesson:** audit a convention by finding every place it is *implemented*,
and compare those places to each other. A convention with N implementations has N-1
opportunities to disagree, and the documentation counts for none of them.

**Applies to:** any rule that more than one tool, service, script or module enforces —
TTL and liveness maths, ID and name normalisation, precedence and tie-breaking,
serialisation rules, "who wins" questions.

**Read before:** citing a shared convention as enforced because one component
implements it.
