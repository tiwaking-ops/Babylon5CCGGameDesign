---
document:
  title: "An all-green class is not a gate - read its exit contract"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# An all-green class is not a gate - read its exit contract

**Reusable lesson (B5-0956).** A harness's exit code is a contract, and a non-zero exit only means "defect" if
the class has no other non-zero exit; `exit 2` for a bad argument and `exit 3` for a harness exception are the
two lines that most often disqualify an all-green class from being a gate.

## The shape of the mistake

Asked to split a set of harnesses into gates and probes, the cheap discriminator is "does it pass right now".
On B5-0956 that discriminator returned the *same answer for all eleven classes* - every one exited 0 on the
green tree. A rule that cannot separate the cases is not a rule, it is a coincidence. The discriminator that
worked was read out of the code rather than observed on it:

> A class is a gate only if its non-zero exit means a defect. A class that can also exit non-zero for "bad
> argument" or "harness broke" is a probe.

Two classes were all-green, all-deterministic-looking, and *structurally incapable* of ever reporting a
failure: one printed `SEEDED RUN COMPLETE` and had only `0` and `3` exits, the other printed
`STALL-SOAK PROBE COMPLETE (classifications are reporting, not failures)`. Neither has a failing state. Wiring
either into a build would have added 37 minutes and zero defect detection.

## Three transferable checks

1. **Enumerate the exit paths, do not observe one.** `System.exit` grep, then classify each code: verdict,
   argument error, or harness exception. Only a verdict qualifies a class as a gate.
2. **Read the class's own final line.** The authors had already written the answer in the output text. A class
   that says its classifications are "reporting, not failures" is telling you it is not a gate.
3. **A documented self-limitation disqualifies.** `HeadlessHumanSeatProbe` passed 3/3, but its header records
   that the model `Deck` shuffle takes no `Random` and that a coverage gate was relaxed to soft *because of
   that*. Green under a known-uncontrolled variable is luck, not a verdict.

## And the one that argues the other way

Do not read "not fully deterministic" as a reason to exclude. `Deck.java` shuffles unseeded, so the gate tier
was never running against a pinned variable - the 8-run sweep (56/56 green) exercised the real one. That is
*evidence for* gate-worthiness, not against it. Seeding it would have converted a false negative into a
deterministic one without fixing anything.

## The wiring is not the hard part

After choosing a tier, prove the gate can go red. Substituting a non-existent class for a wired one and
confirming exit 1 with no success line is cheap, and it is the only thing that distinguishes "wired" from
"wired and verified to fail". A green run cannot tell you the difference.
