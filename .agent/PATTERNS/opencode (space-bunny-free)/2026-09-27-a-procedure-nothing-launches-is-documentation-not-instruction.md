---
document:
  title: "A procedure nothing launches is documentation, not instruction"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A procedure nothing launches is documentation, not instruction

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

Seventh instance in this namespace. Companions: the self-certifying gate, the
self-check sharing its subjects, the duplicate ID, the proxy check, the
document/enforcer drift, the absence-only suite, and the rule you just wrote.

## The rule

> A procedure is only *in effect* if something **invokes** it. A procedure that only
> *references* itself is documentation, and it will silently stop being true the day
> the thing it replaced changes.

The distinction is not stylistic. It decides whether a reader is *instructed* or
merely *able* to do the right thing, and only one of those survives contact with a
system that moved on.

## The worked instance

I wrote `.agent/AGENT_LOOP.md` — a full loop procedure, human-approved, with a
clause-to-failure table. Then I asked how a user would tell an agent to execute it,
and the honest answer was: **you cannot, unless you say so out loud.**

The measurement:

```
$ grep -r AGENT_LOOP .agent docs AGENTS.md
  7 references — every one a document written ABOUT the file
  0 references from any executable
```

Seven citations, all pointing at the file. Zero invocations. The file was extremely
well referenced and completely inert.

Meanwhile the repo *did* have an automated path — `run-queue.ps1`, one task per fresh
process, the right shape for unattended work — and it told every agent it invoked:

> Read .agent/HANDOFF.md

Which is stale in a way that produces a **specific wrong action**, not just old text:

| it says | reality |
|---|---|
| `b5ccg/src/` is RED at `-source 6` | green since 2026-09-21 |
| claim B5-0001 | B5-0001 DONE since 2026-09-21 |

So the automation was about to create a **claim on a closed task** — the exact orphan
class the previous task had just closed the protocol against. The best work of the
session was one automated step away from being undone by the automation, because the
automated path was the one path nobody re-reads.

## Why the reference count was a trap

Seven references *feels* like adoption. It is the same number you get from a rule that
is quoted constantly and obeyed never. The telling question is not "is this file
referenced?" but:

> **What happens if I change it? Does anything behave differently?**

If the answer is "no, but three documents will be out of date", it is documentation. If
the answer is "the runner sends a different prompt", it is instruction.

## The generalisation

For any procedure, runbook, playbook or standard:

1. **Find the invoker.** Grep the executables, not the docs. If nothing invokes it,
   say so out loud rather than assuming the humans will pass it on.
2. **Repoint the invoker, and then verify by running it.** The fix is one line in the
   launcher; the proof is that the launcher still works.
3. **Mark the superseded document with its specific lies, not its age.** "This is old"
   gets skimmed; "§7 says the build is red and it is green, §9 tells you to claim a
   task that is DONE" gets acted on. Name the false claims; a reader who knows which
   two sentences are wrong can decide, and one who does not will follow §9.
4. **Tell agents where *not* to look.** One line — "do NOT read `HANDOFF.md`" — is
   cheaper than hoping nobody remembers the old pointer.

**The automated path is the one path nobody watches, and therefore the one path
nobody re-reads.** That is not a property of this repo. It is a property of every
system where humans review what is on screen and not what gets executed at 3am.

**Applies to:** runbooks, agent prompts, CI configs, on-call procedures, onboarding
docs, any instruction whose whole value is being followed *later* by someone or
something that cannot ask you a question.

**Read before:** writing a procedure and assuming it is now in effect.
