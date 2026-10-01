---
document:
  title: "Measure the curve, not the endpoint — a collapse and a steep ramp live in the same number"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1113"
---

# Pattern: one bad-looking ratio is two different games

B5-1113's seed said "71 percent of the costed pool is unplayable on the
opening turn" — a number that reads as breakage. The cumulative curve over
appliedPool 4..13 told the other half: 66%→89% by pool 8, saturation by pool
10, total by 13. The same dataset describes both "a game that starts locked"
and "a game that ramps hard for four turns and then opens fully". The
difference is the *shape*, and only a curve shows shape.

1. **Deliver the sweep, not the headline.** When a task quantifies a
   condition, the deliverable is the function over the controlling variable
   (here pool size), not the value at one point. One extra loop, an order of
   magnitude more meaning.
2. **Name your frame before comparing to anyone else's.** The seed counted
   829 raw records; the loader serves 446 distinct titles. Both are honest;
   comparing across frames manufactures a fake discrepancy (377 vs 206
   costed). State the frame in the table header and reconcile in one line.
3. **Exonerate the data before indicting the design.** Three candidate
   culprits (data, constant, intent) — the printed-basis checks from earlier
   rows (B5-0943) cleared the data in one citation, which narrowed the
   question to the engine constant vs designer intent, and the curve's
   saturation is what keeps "intended ramp" alive. An adjudication that only
   indicts is half an adjudication.
4. **Human-only questions get named, not answered.** Whether turn-1 scarcity
   is *wanted* is play-balance — no in-repo evidence can rule on it, and the
   row explicitly forbade changing anything. The close-out ends at the
   measurement plus the named ruling owner, which is the whole job.

Reusable lesson: a ratio invites a verdict; a curve invites a design
conversation. Deliver the curve.
