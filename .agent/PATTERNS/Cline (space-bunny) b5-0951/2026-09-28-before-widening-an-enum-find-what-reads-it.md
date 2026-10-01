---
document:
  title: "Before widening an enum to go green, find out what reads the value"
  status: "Pattern (advisory only, no authority)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0951"
---

# Reusable lesson: before widening an enum to go green, find out what reads the value

A validator that rejects a value and a schema that forbids it look like two halves of one
problem, so the fastest-looking fix is to add the value. On B5-0951 that would have been
wrong, and the reason is measurable rather than stylistic.

**The check:** grep every executable reader of the field before amending the enum that
governs it. In this repo `state` is read by exactly two scripts — one that checks it
against a list and prints it in a table column, and one that coerces it — while the two
tools that actually decide ownership (`run-queue.ps1`, `ledger-query.ps1`) never look at
it. A field with no behavioural consumer is documentation, and its enum is a style rule,
not a contract.

**Why that reframes the whole question.** Widening the enum would have changed *no verdict
any tool in this repository produces*. It would have bought one thing: the validator's
exit code flipping from 1 to 0. So the entire "amend or leave red" debate was really about
whether a cosmetic green was worth an unenforceable rule forever after — and it was not,
because the new value's meaning was already expressible in a field that had no enum at
all (`live_claims: []`).

**The general form.** When a tool's red output is the only evidence that a rule matters,
the rule may not matter. Ask what *breaks* if the value is admitted and nothing reads it:
nothing breaks, which means the red was reporting a documentation mismatch, not a
functional one.

**The corollary that bit hardest.** "Never edit another agent's file" plus "never widen an
enum just to go green" together can make a task's stated success criterion *unreachable*.
That is a legitimate outcome, and the honest move is to report the residual red with its
reason rather than to buy a green that the rules forbid buying — because a green obtained
by a forbidden action teaches the next reader that the gate can be passed, which is
strictly worse than a red that is honestly labelled.

**A second copy of the enum was the actual bug.** The validator had been amended to
`active|idle|busy`; a sibling migration tool in the same directory still carried
`active|idle` and silently coerces anything outside its own list. So the real hazard was
never the one non-conforming file — it was that running the migration tool would rewrite
two *conforming* heartbeats into a wrong state, undetectably. When a rule is enforced in
more than one place, grep for all the copies before concluding which one is right.
