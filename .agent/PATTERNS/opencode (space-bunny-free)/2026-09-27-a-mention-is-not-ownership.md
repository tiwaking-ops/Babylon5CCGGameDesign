---
document:
  title: "A mention is not ownership"
  status: "Advisory pattern (same tier as investigations/; never canonical per AGENTS.md §6)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-27"
---

# A mention is not ownership

Learned from B5-0659 (claim liveness, human-approved 2026-09-27), while rejecting
one clause of an otherwise good coordination proposal.

## Text mentions become locks, and locks strand abandoned work

The proposal would have had a reaper skip any task whose id appeared in *any*
heartbeat's `current_task` or `live_claims`. Every one of those mentions is
innocent and routine: an agent triaging the queue lists ids it is *considering*; an
agent filing a report on someone else's abandoned claim names it. Under that clause
either one pins the claim indefinitely.

The failure is not a false negative. It is that the clause makes the recovery
mechanism unreachable in precisely the case it was built for — a claim whose owner
died mid-task is exactly the claim a reaper must recover, and it is also the claim
most likely to have been *mentioned* by a bystander who noticed it was stuck.

**Ownership is a keyed join, never a textual mention.** The claim file's own
`agent_id`, matched to that owner's heartbeat. The general rule: when a rule asks
"did anyone say anything about T?", it is measuring conversation, not possession, and
the two come apart the moment a second agent has an opinion.

The same reasoning shows up elsewhere and is worth recognising: a task id inside a
narrative cell, a name in a prose field, a path in a log line. `ledger-query.ps1`
already refuses to treat those as rows for this reason. Consistency about what
counts as an identity claim is the whole defence.

## Absent evidence must not manufacture a blocker either

The rejected clause errs toward "never touch". The opposite error is just as real:
a lookup that matches nothing and then returns an age of `-1` or `0` compares as
*younger* than any TTL and reads as LIVE (the B5-0609 defect). So a rule about stale
work needs both halves stated — an absent signal is `UNKNOWN`, never `LIVE` and
never `STALE`. Absent ownership evidence cuts both ways: it cannot prove a blocker
and it cannot manufacture one.

## "Conservative" is not a synonym for "safe"

The intuitive appeal of the rejected clause was that it refuses to act. It reads as
cautious, and it is the opposite: a clause that blocks reaping in the abandoned-claim
case guarantees the collision the reaper was meant to prevent, while looking
conservative on paper. **Ask what the rule makes impossible, not only what it
permits.** A rule whose effect is "this can never be recovered" deserves more
scrutiny than one that permits a destructive action, not less.

Related:
[an incomplete rule is worse than a slow one](2026-09-27-a-rule-written-down-and-implemented-once-is-a-rule-one-component-follows.md)
— half an adopted rule in the tree is the state that needs naming, not the state
that needs hiding.

## A comment stating an invariant is not evidence the invariant holds

Two comments in the code I was editing promised "an absent owner heartbeat is never
read as staleness" and, in effect, that an unreadable claim is not an absent claim.
Both promises were **false in the code directly beneath them**: the heartbeat was
folded in only under a `ContainsKey` guard, and a corrupt claim file was dropped in
an empty catch and so read as `UNCLAIMED`.

Documentation of an invariant is a claim about code, and it decays exactly like one.
The only evidence is a test that fails when the invariant breaks.

## A green run against real data is evidence about the data, not the code

The live tree contains no corrupt claim file. So after I introduced the corruption
handling — and a real bug in it — the tool ran green against the whole ledger while
being broken against the case it exists to handle. Only a fixture containing
`{ this is not json` surfaced it.

**"It works on the real data" means the real data does not contain the defect.** For
a defect defined by an unusual input, the absence of that input is not evidence and
is in fact the reason the defect survived. The corollary: when a fix targets a
malformed-input path, a fixture for the malformed input is not extra rigour, it is
the only thing testing the change at all.

Related: [a stub in the calling language confirms what the real call does
not](2026-09-27-a-stub-in-the-calling-language-confirms-what-the-real-call-does-not.md)
and [a test that only asserts absence of a symptom passes a worse
replacement](2026-09-27-a-test-that-only-asserts-absence-of-a-symptom-passes-a-worse-replacement.md).

## Fixtures fail quietly in the direction that flatters the rule

Three fixture bugs in one harness, and the dangerous one did not announce itself:
all fixtures shared a single heartbeat file, so the last mtime assignment won for
every case and each intended "stale heartbeat" fixture was silently fresh. The suite
was not red — it was *green about the wrong thing*, in the direction that made the
new rule look better than it was.

Isolate fixture state per case, and read the printed table rather than only the
PASS/FAIL summary: a shared-resource bug shows up as a plausible number, not an
error. Related:
[a self-check must not share its subjects](a-self-check-must-not-share-its-subjects-pattern.md).
