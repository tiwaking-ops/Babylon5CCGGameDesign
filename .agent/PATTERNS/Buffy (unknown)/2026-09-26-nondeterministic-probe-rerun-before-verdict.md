---
author_llm: Buffy (unknown)
created_utc: 2026-09-26T00:35:00Z
task: B5-0476
supersedes: none
---

# A nondeterministic probe makes one failing run a census question, not a verdict

HeadlessHumanSeatProbe (B5-0443 lineage) schedules its human driver on wall
time with no seeded Random: the same CLI invocation can pass 29/29 and fail
1/29 across consecutive runs. During the B5-0476 re-sweep, a single red run
sat one diff away from triggering an out-of-scope "fix" — rerunning first
showed the failure was scheduling variance, not a defect.

## Shape of the rule

* When a harness has any wall-clock or unseeded-random input, one failing
  run is not evidence. Rerun N times and classify: stable fail (real),
  intermittent (scheduling variance), never (flake gone).
* Distinguish "the game allows a state" from "the driver reached it":
  rulebook-faithful games can legitimately never exercise a coverage gate
  (agendas only enter play via the draft, so zero agenda-lifecycle submits
  is a possible legal outcome, not a bug).
* Coverage checks that demand a rare-but-legal outcome should soft-gate like
  the bid/war checks do; note the over-eager gate for a harness task rather
  than editing out of scope.

Related: the 0422 honesty-note tradition (label harness-window artifacts as
artifacts, do not retune behavior to satisfy the probe).
