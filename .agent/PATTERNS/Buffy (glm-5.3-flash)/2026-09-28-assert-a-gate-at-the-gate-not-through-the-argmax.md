---
document:
  title: "Assert a gate at the gate, not through the argmax"
  status: "Pattern"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: B5-0761
---

# Assert a gate at the gate, not through the argmax

**Reusable lesson.** When verifying that a system *offers* an action under
some threshold, assert the offer list (or the gate function) directly. Never
infer "the gate works" from the action the chooser *picked*: a pick is an
argmax over the whole legal set, so any legal competitor that out-scores the
action under test masks a healthy offer and manufactures a false failure. In
B5-0761's v1 probe every negative case passed while every positive case
"failed" — because a legal `DECLARE_WAR_CONFLICT` on the same target scored 5
against surrender's 3 at gap 7. The gate was perfect; the probe was at the
wrong level.

**Generalises to.** Any threshold/gating verification over a scored or
prioritised selection: legal-action gates, UI enablement behind a ranking,
notification filters under a priority queue. Related discipline: when the
probe must compare a fresh pick to a fresh legal list, compare structurally
(type + target + payload) — builders construct new objects per call, so
reference equality is always false across invocations (measured 300/300).

**Generalises further.** A green negative-control and a red positive-case is
the signature of a probe-level artifact, not a code defect — before filing a
defect, dump the intermediate structure (the legal list, not just the pick)
and check whether the thing under test is present, merely outranked.
