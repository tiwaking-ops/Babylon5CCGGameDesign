---
document:
  title: "A measurement's age is part of its meaning"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0821"
---

# A measurement's age is part of its meaning

**Pattern.** A measurement has an expiry, and the expiry is set by **what was moving**,
not by how old the document is. A claim about *rules or history* can be cited on age. A
claim about *what a codebase currently contains* cannot: the code is the thing that moved,
and a result quoted after the code changed is a stale fact wearing the authority of a
fresh one.

**The instance, and it nearly cost real damage.** A 2026-09-27 proposal recorded
"a case-insensitive search for `power` over `Player.java` returns 0 occurrences". That
was **true when taken**. A later task, B5-0677, added `StatKey.POWER` and
`Player.getPower()`. On 2026-09-28 I relayed the 0 as current — in plain prose, to a
human, without running the search — and the human issued a directive to implement the
missing seam. Obeying that directive would have **reimplemented a shipped,
regression-tested feature** and left two divergent `getPower()` methods. The four
conformance checks that would have caught it already existed and passed.

**Why age-detection is the wrong tool.** The result was not in an old file that looked
old; it was in a section of a live document, adjacent to current material, in the same
register. Nothing about its presentation signalled staleness. The only reliable defence
is to **re-query at the moment of use**.

**The rule that follows, and it is short.** Before you state a fact about the current
state of a *system*, run the query. Before you state a fact about a *rule, a decision or
history*, cite it. Both are one action; conflating them is the whole error. And when a
human issues a directive built on your fact, **re-verify before implementing it** — a
directive is a request for a *state*, not for the *action* you assumed it implied. The
gap between those two is where duplicated work lives.

**A second-order form.** This is the same defect as
`a-measurement-you-did-not-take-is-a-claim-you-have-adopted` (this session's namespace)
with time added: that pattern is "I did not measure it"; this one is "I measured it, and
the world moved". Both end identically — a false statement to a human — and both are
invisible in review, because in each case the number looked solid.

**How to apply.** Tag the claim as you make it: *rule* (citable) or *state* (re-query).
When you catch yourself about to say "the engine has no X", that phrasing is itself the
warning: it is a claim about a tree, and trees change.

**Supersedes / relates.** Extends `a-measurement-you-did-not-take-is-a-claim-you-have-adopted`
and `re-ground-a-triage-against-the-data` (this session's namespace). Per `AGENTS.md` §6
this record is advisory and confers no authority by being cited.
