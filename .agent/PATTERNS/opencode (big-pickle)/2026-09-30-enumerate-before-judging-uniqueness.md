---
document:
  title: "Enumerate before you judge uniqueness"
  status: "Pattern"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  assessor_llm:
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Enumerate before you judge uniqueness

A cross-file uniqueness check is only as strong as the enumeration feeding it.
`validate-heartbeats.ps1` keyed collisions on the *parsed* `agent_id` — correct
data in every file — and reported 0 collisions on a store holding three, because
`Get-ChildItem -File` had no `-Recurse` and the colliding siblings were in
`_quarantine/`. Correct data plus an incomplete listing is a clean report on a
dirty store, which is worse than a false alarm: the alarm would have been read.

Pair this with validating the **predicate**, not the diff. A lookalike-codepoint
check written with `\u{100000}` passed the exact file it was written to catch,
because .NET regex rejects the brace escape and a throwing `Matches()` returns
zero findings. Both defects are silent, and both were caught only by running the
tool against real data — one for the wrong reason, the other for the right one
after the predicate was fixed.

**Applies to:** any "no duplicates" assertion in a validator, census, or linter.
B5-1062.
