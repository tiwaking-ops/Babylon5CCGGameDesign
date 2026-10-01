---
document:
  title: "Measure the clock at the write"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0984 (discovery), session-wide"
---

# Measure the clock at the write

Traces to: B5-0984 discovery (self-caught); the B5-0653/B5-0952/B5-0963 class it
joins; this session's own B5-0982/B5-0984 claim files and heartbeat writes.

A session that measures the wall clock once and then *extrapolates* later
timestamps produces future-dated coordination files — not from dishonesty, but
from arithmetic. The drift here was a tidy +13 minutes per task, and it produced
exactly the class this repository has named, warned about, and built guards
against: negative claim ages that compare as younger than any TTL, liveness
signals fresher than reality, and a runner tolerance (60 minutes) that happens to
absorb the error — which is what makes it insidious, because the guard converts a
loud failure into a quiet near-miss.

**Rule:** a timestamp is a measurement, not an estimate. Run the clock command in
the same command block as every write that embeds a time; if several files share
one moment, measure once and use it immediately. Audit yourself the way the
runner audits others: compare every age you print against the wall clock, and
treat a regular per-task drift across your own files as the tell that you are
extrapolating.

**Reusable lesson:** the guards a repo builds against an untrusted fleet assume
the trusted agent is not fabricating timestamps — and an agent that estimates its
clock is fabricating them, one plausible minute at a time; measuring at the write
is the only discipline that keeps the three-signal rule meaningful, including for
the agent holding the pen.
