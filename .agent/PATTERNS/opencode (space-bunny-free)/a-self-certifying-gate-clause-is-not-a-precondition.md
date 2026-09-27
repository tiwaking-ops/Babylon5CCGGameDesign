---
document:
  title: "A self-certifying gate clause is not a precondition"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A self-certifying gate clause is not a precondition

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

**The rule.** A gate that writes its own satisfaction into its own text —
`gated: claim ONLY after X is DONE — gate satisfied` — is not a precondition. It is a
claim about the state of the world wearing the costume of one, and it is uniquely
dangerous because it is the only kind of claim in the ledger that no artifact has to
back. Every other row asserts something a reader can go and check. This one asserts the
check has already been done, and skipping the check feels like the efficient path.

**The worked instance.** Row B5-0606 read:

> `(gated: claim ONLY after B5-0594 is DONE — gate satisfied; B5-0594 report on disk at
> .agent/REPORTS/2026-09-26-opencode (me-so-poor)-B5-0594.md defines the ... rule)`

Both halves were false. The cited report did not exist, and row B5-0594 was `OPEN`. The
row was the queue's only substantive *game-code* task in its band — one of two `OPEN` rows
out of thirty that touched `b5ccg/src` — and its entire value was closing a coverage gap
measured by an audit. An agent trusting the gate would have written a conformance section
against an audit nobody performed, then reported coverage that nothing established.

**Why it survived.** The seed note that created it was, on its own terms, careful: it
cited a file path and named a prerequisite ID. Both citations were individually
*checkable* and nobody checked them, because the sentence containing them also contained
the words "gate satisfied", and that read as though the checking had been done. A
defect that arrives pre-certified is much harder to catch than one that arrives
uncertified, because the surrounding prose is shaped exactly like a verified claim.

**The check, in one command.** Resolve the named ID against the actual status cell before
acting on any gate:

```powershell
rg -o --no-filename '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|\s*([A-Z]+)\s*\|' --replace '$1 $2' .agent/TASK_LEDGER.md
```

If a gate names an ID that is not `DONE`, the gate is not satisfied no matter what its
own text says. If a gate names an ID with no row at all, that is worse — it is a
prerequisite that does not exist.

**The generalisation.** Any artifact that grades its own homework is not evidence. This
applies beyond gates: a report that cites a prior report which does not exist, a census
that asserts its own row count, a note that says a task "is DONE" without reading the
status cell. The tell is the same in every case — the sentence making the claim is
adjacent to a citation that *would* have supported it, so reading the citation feels
like reading the evidence.

**Companion, from the same pass — a listed set is not evidence of its own cardinality.**
The B5-0613 seed note said 8 of its 14 missed rows were `OPEN`, then listed exactly 7
identifiers. The list was right; the count was not. That number was going to be copied
forward by the task owner into an actual repair, so an arithmetic error in a seed note
propagates into someone else's diff. When a number and a list disagree, the list is the
measurement and the number is the claim — recount against disk before the number reaches
a row another agent will build from.

**Applies to:** any autonomous multi-agent ledger, any gate/precondition/dependency
scheme, any seeded task queue where a later agent acts on prose rather than on state.

**Read before trusting:** any row in `.agent/TASK_LEDGER.md` containing `gate satisfied`.
