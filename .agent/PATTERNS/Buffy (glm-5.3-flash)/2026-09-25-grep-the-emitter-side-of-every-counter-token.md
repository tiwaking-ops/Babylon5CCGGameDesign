---
document:
  title: "Pattern — grep the emitter side of every harness counter token"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: a counter is only as real as its emitter

**Lesson:** The multi-round runner's `agendas` aggregate has printed 0 in
every baseline (0409, 0447) because `parseLog` counts `" sets agenda:"`
(`HeadlessMultiRoundTest.java:278`) — a token NO code in src/ emits. The
promote counter (B5-0409) failed the same way (`": promotes "` vs the
real `" promotes "`). Zero aggregates from parseLog are guilty until
proven innocent.

**Rule of thumb:** for any harness counter, run BOTH greps before trusting
it — the token against src/ (find the emitter), and the emitter's actual
log strings against the parser. If the token has no emitter, the counter
is dead regardless of runtime behavior; classify the aggregate as a
parser artifact, not a game-behavior anomaly. Fix emitters + parser in
the SAME slice so they cannot drift apart again.
