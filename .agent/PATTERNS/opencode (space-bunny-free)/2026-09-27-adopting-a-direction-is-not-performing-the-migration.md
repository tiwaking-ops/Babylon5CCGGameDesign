---
document:
  title: "Adopting a direction is not performing the migration it describes"
  status: "Advisory pattern (same tier as investigations/; never canonical per AGENTS.md §6)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-27"
---

# Adopting a direction is not performing the migration it describes

Learned from B5-0654 (B5-0388 authenticity migration, human ruling 2026-09-27), a
decision whose entire content was *"we are not doing the big rewrite"*.

## A decision log entry titled after the work is read as the work

The entry heading was `B5-0388 authenticity migration ADOPTED`. A reader scanning
headings — which is how decision logs are actually consumed — sees "authenticity
migration" and "ADOPTED" on the same line and reasonably concludes 87 stat blocks and
439 card texts were rewritten. They were not. Nothing about the pool changed.

So a ruling that *declines* an action must state, in the record, **what it leaves
unchanged**. Not as a caveat at the end, but as part of the ruling, because the
skimming reader never reaches the caveat. The concrete form used:

> What this ruling does **not** do, stated explicitly because the temptation to
> over-read it is the whole risk: it promotes **no card values**. Every stat, text and
> cost in the pool remains exactly as authored.

The general rule: **the most important sentence in a "we chose not to" record is the
enumeration of what is now deliberately frozen**, because it is the sentence that
stops the next agent from re-raising the question as if it were new, and stops a
reader from hunting for a diff that does not exist.

## Separate the task from the decision; both can be closed while one is open

The proposal's own ledger row read `DONE` — the work of writing it was finished. The
*decision* it recommended had been open for three days and was recorded as "deferred
pending a human goal decision" in four separate seeding passes by different agents.

Two states, one ID, opposite meanings. Writing "B5-0388 DONE" after the ruling would
have been false twice: the task was already done, and the decision had not been made.

When auditing "what is still un-adopted", check whether the *task* row is closed
**and** whether the document's recommendation was ever ratified. A `DONE` row is
evidence that a deliverable was written, never evidence that its recommendation is in
force. This is the same shape as
[a rule written down and implemented once is a rule one component follows](2026-09-27-a-rule-written-down-and-implemented-once-is-a-rule-one-component-follows.md):
the artifact existing is not the decision being in force.

## Do not rewrite the history that records the question was open

Four seeding-pass notes recorded B5-0388 as deferred. Editing them to say "deferred
pending a now-given ruling" would have made the log tidier and destroyed the fact that
three days and four independent agents treated this as genuinely undecided. A log's
value is proportional to how faithfully it records what was believed at the time.
Append the resolution; leave the deferrals.

## Ratify the prior art explicitly

A new ruling looks like it invalidates whatever was done under the old ambiguity, and
a reader who does not check will either redo finished work or distrust the record. So
the entry names the affected work and says how the ruling relates to it — here, that
the already-landed IP-safety paraphrases and the cost-only backfill are exactly the
shapes the adopted rules bless, and that a flag left by an earlier task ("flagged for
B5-0388's migration audit") is now *answered* rather than dangling.

## A document that contradicts itself three lines apart is worse than either version

The proposal's "Scope and status" said "It remains a proposal"; the Disposition section
added below it said ADOPTED. Both were individually defensible, and together they read
as a defect in the record. Reconciling the older sentence — rather than leaving it as
historical text — was correct here precisely because it was *scope* language, not a
historical claim. Scope statements describe the present and must be true now;
historical claims describe the past and must be left alone. Knowing which one you are
looking at is the whole skill.

## Do not let the ruling age into a myth in either direction

Adopting the current direction does not make the pool faithful. The evidence against
fidelity (0 of 87 stat blocks matching, 0 of 439 texts identical) was restated in the
entry so the decision cannot later be misread as a finding of authenticity — and the
conditions for *revisiting* printed fidelity were left explicitly intact, so adopting
the present goal does not read as a permanent refusal. Both halves are needed: one
prevents a false claim of fidelity, the other prevents the settled question from being
re-opened as if unanswered.
