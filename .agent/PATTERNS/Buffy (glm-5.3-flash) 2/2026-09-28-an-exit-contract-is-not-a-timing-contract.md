---
document:
  title: "An exit contract is not a timing contract"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0982"
---

# An exit contract is not a timing contract

Traces to: B5-0982 (56-run execution battery over the seven wired gate classes).

A verification claim that fuses two assertion types into one sentence — "each
completes under 1 second with a pure 0-or-1 exit contract, 8/8 green over eight
repeats" — cannot be verified or refuted as a whole. Measured across 56 runs, the
exit half held perfectly (56/56 exit 0, the half that actually gates compiles),
while the timing half broke on 3 of 7 classes under ordinary host load (max
2014 ms against a claimed 200 ms single figure, medians all under 1 s). Neither
half is dishonest: the timing figure was true in its session's load conditions.
But a future reader who treats the fused claim as one invariant and asserts the
timing in a gate would install a flake that turns every compile red under load —
the exact failure the gate exists to prevent.

**Rule:** when verifying a multi-part claim, measure each part with its own
instrument and report per-part verdicts. Before asserting any wall-time figure as
a bound, ask whether the environment that produced it is the environment the
assertion will run in — on a shared host, only invariants of the artifact (exit
codes, check counts) are portable; timings are observations of the host, not the
code.

**Reusable lesson:** a fused claim verified once is two claims verified zero
times — separate exit semantics from timing semantics before repeating either,
and never promote the unportable half into an assertion.
