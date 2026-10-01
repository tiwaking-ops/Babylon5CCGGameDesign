---
document:
  title: "A gate naming a predecessor must be re-read at the moment of action"
  status: "Pattern (advisory; .agent/PATTERNS store is never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-30"
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  last_modified_date: "2026-09-30"
---

# Re-read a predecessor gate at the moment of action

**Pattern.** A task row whose gate names a predecessor ("claim ONLY after
B5-XXXX is DONE") must have that predecessor re-read *immediately before the
claim is written*, not once during boot. Boot-time reading answers the question
"was this claimable when I started?"; the claim needs the answer to "is this
claimable *now*?".

**Observed (B5-1045, 2026-09-30).** At boot the gate was closed: B5-1043 read
`OPEN` under a live claim from another agent. Within the same boot census —
before I had written a single line of code — B5-1043 flipped to `DONE`. Both
readings were correct about 90 seconds apart. The task I had been told to start
was not claimable at boot and was claimable moments later.

**Why it matters.** The failure is silent in both directions. Check too early
and you claim a task whose predecessor is still live, colliding with the
one-writer-at-a-time scope the gate exists to protect. Check too late, or not at
all, and you either stall on work that is now legitimately yours or skip it
entirely because the boot snapshot said "gated". Neither produces an error; both
produce a fleet that has quietly lost a task.

**The rule it generalises.** Any precondition that another agent can change is
a *volatile* input, and volatile inputs are read at the point of use. This is
the same discipline as the three-signal liveness rule in
`.agent/HEARTBEATS/README.md`: never decide liveness from one timestamp taken
at a convenient moment. A boot snapshot is one timestamp.

**Cheap implementation.** Re-read the predecessor row and the candidate row in
the same command that writes the claim file. It costs one `Select-String` and it
converts a whole class of race into a check that either passes or fails loudly.

**Counter-pattern to avoid.** Treating the boot census as authoritative and
either (a) claiming anyway because "I was told to", or (b) refusing and exiting
because "the gate was closed at boot". Both are decisions made on a stale fact;
the second one is the more tempting because it looks like discipline.
