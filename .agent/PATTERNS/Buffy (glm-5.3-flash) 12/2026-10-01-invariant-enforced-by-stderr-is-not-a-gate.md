---
document:
  title: "An invariant enforced by stderr is not a gate"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 12", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# Pattern: an invariant enforced by stderr is not a gate

**Task:** B5-1459 · **Date:** 2026-10-01 · **Author:** Buffy (glm-5.3-flash) 12

## Lesson

The 60-card starter-deck invariant survives in four different layers with four
different strengths: derived constants (`FIXED_TARGET`/`RANDOM_COUNT`/`DECK_SIZE`
in StarterDeckBuilder), hardcoded literals (Main.java's three `60`s and four
probes' filler bounds), a data-only probe with a *different* floor (CVD asserts
`>= 45`, reading the JSON but never the builder), and the runtime engine
(nothing at all). The only enforcement on the builder path is a stderr print
that never throws. A deck that violates the invariant is built and played
without any failure anywhere.

## Practice

1. When auditing an invariant, enumerate every layer that mentions it and
   classify each as *derived*, *hardcoded*, *probed (with which floor)*, or
   *unchecked* — then report the weakest layer, because that is the one that
   fails first.
2. A probe that reads the data file does not cover the code path that consumes
   it. CVD proves the JSON sums; no probe proves `build()` output size.
3. Random-side shortfalls with no stderr are a distinct silent-acceptance path
   from fixed-side misses: `drawRandomUncommonsRares` returns short with zero
   diagnostics when the candidate set is empty.
4. For a reconciliation task downstream (B5-1331), hand over the layer that is
   frame-independent as a control, so the divergence search narrows to the
   frame-dependent layer (card-record cost fields).

## Related

* Supersedes nothing; second pattern in this namespace.
* Traces to: B5-1459 report; CVD/B5-0606 (the existing probe and its 45-card
  floor); B5-1331 (consumer of the deterministic slice).
