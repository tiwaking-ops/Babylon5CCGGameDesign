---
document:
  title: "A single-signal check turns a placeholder into a permission slip"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A single-signal check turns a placeholder into a permission slip

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

Tenth instance in this namespace. Companions: the self-certifying gate, the self-check
sharing its subjects, the duplicate ID, the proxy check, the document/enforcer drift,
the absence-only suite, the rule you just wrote, the procedure nothing launches, the
stub in the calling language, one implementation imported once.

## The rule

> A check that reads **one** signal cannot distinguish a real value from a placeholder,
> a default, or a clock-skewed one — and when the verdict is *permission* rather than
> *information*, that indistinguishability is spent as **authorisation**.

The second writer did not violate the protocol by trusting the tool. The tool did not
lie. Both were behaving correctly according to what was written, and a method was
destroyed anyway.

## The worked instance

An unattended agent claimed a task and wrote a **placeholder** into its own claim file:

```json
"started_utc": "2026-09-27T00:00:00Z"
```

The queue's liveness check read `started_utc` **alone**, so the claim's age read
**287 minutes** against a **30-minute** TTL — confidently, and correctly *according to
the code*. Another agent read the census 6 minutes later, was told the task was free,
and began editing the same file.

Within ~4 minutes the two writers had consumed a pre-existing method header, producing
**100 compile errors**.

The multi-signal fix existed. It was applied to the working tree **mid-incident** and
committed at **04:58:00Z** — the closing second of the other agent's own incident
window. Correct, and eight minutes of overlap too late.

## The generalisation

Any guard whose output is *permission* — take this job, reap this claim, skip this row,
trust this value — inherits every failure mode of its weakest input, with none of the
uncertainty. Information-returning checks can be wrong; permission-returning checks that
are wrong **cause damage on both sides**: the rightful owner loses their work, and the
unwarranted actor commits theirs.

So, for any such guard:

1. **More than one signal, or abstain.** Never let one field decide. And when no signal
   is trustworthy, the safe answer is *not to proceed* — not *to proceed, confidently*.
2. **Treat implausible values explicitly.** Midnight, epoch, `1970`, an empty string, a
   round number repeated — decide what each means rather than letting arithmetic consume
   it. A placeholder is not a timestamp.
3. **A permission slip is louder than a bad report.** Nobody notices a wrong number in a
   census. Nobody notices a wrong "this is free" until two agents are in one file.

And the corollary that cost the damage:

> **Verify the fix lands before the window it was meant to close.** A correct fix that
> arrives after the incident is still a correct fix, and still an incident.

## Companion, from the same incident

The agent that *did* violate the protocol — it edited before claiming, on a stale
reading of a census — handled the aftermath better than anything else in the record:
immediate stop, full claims-and-heartbeats sweep, withdrawal of only its own span with
per-step assertions proving the other writer's bytes were untouched, minimal dated
restoration of the third party's destroyed code, refusal to "fix" the other agent's
in-progress errors, and a self-filed incident report.

Its formulation of the stop rule is the one to keep:

> Discovery of a concurrent editor mid-edit — unknown text appearing in your compile
> output — must stop work immediately, **including "fixing" the file**, which is how one
> line of another agent's WIP becomes two agents' incident.

**The compile error that was not yours was the most reliable detector of the whole
event.** Not the claim registry, not the heartbeat, not the census: a name in an error
message that you did not type.

**Reusable lesson:** when a check grants permission, ask what happens if its input is
garbage — because garbage in a permission check is indistinguishable from consent, and
the bill arrives as somebody else's corrupted work.

**Applies to:** lock files, claim and lease systems, "is this free?" checks, cache
invalidation, permission gates, dedupe and uniqueness checks, anything whose answer is
*go* rather than *here is some information*.

**Read before:** writing a guard that reads a single field and then lets another actor
act on its answer.
