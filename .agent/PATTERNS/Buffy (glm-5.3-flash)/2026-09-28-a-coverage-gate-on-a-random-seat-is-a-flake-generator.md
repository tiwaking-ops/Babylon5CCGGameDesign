---
document:
  title: "A coverage gate on a random seat is a flake generator"
  status: "Pattern (advisory, shared store)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A coverage gate on a random seat is a flake generator

Task: B5-0917 (run each of the 13 Headless classes standalone; disposition
each). The headline find: HeadlessHumanSeatProbe failed one of three runs —
lawfully.

## The pattern

The probe asserts a COVERAGE check ("sponsor/promote/build/lead/rotate/
attack exercised") over a game played by a random human seat. When the
unseeded draw never picks those actions (the failing run's own tally:
`promote=0 build=0 leadFleet=0 rotate=0 attack=0`), the check fails even
though the game itself was legal. Three runs of the same class produced
three different check counts (37, 36, 37) and one exit 1. The probe even
prints `Seed: 42` — but accepts no seed argument, so the printed seed is
decoration, not reproducibility.

Two failure classes hide in one file:

1. **Coverage asserted over a random draw** — a lawful fail for an illegal
   reason. The game did nothing wrong; the assertion's premise (the draw
   will visit every action type) is simply not guaranteed.
2. **A seed that is printed but not accepted** — the appearance of
   determinism without the mechanism. Worse than no seed, because readers
   assume reproducibility.

## The check

* A probe whose pass/fail depends on what a random player chose to do must
  either seed the randomness (and accept the seed as an argument) or demote
  the coverage assertion to informational output.
* Before wiring any harness into an automated gate, run it **several** times,
  not once — flake variance (here: differing check counts) is invisible to a
  single green run.
* A printed seed is not a seed. Verify the harness can actually be
  re-driven to the same state.

## Traces to

B5-0312 (24-round playtest produced zero conflicts — random draws
structurally miss paths), B5-0589 (the stall-rate soak this probe family
grew from), B5-0915 (gate wording vs what actually executes).

## Reusable lesson

A coverage gate on a random seat is a flake generator: seed the harness and
accept the seed, or demote coverage assertions to information — and never
wire a random-driven harness into a gate on the strength of a single green
run.
