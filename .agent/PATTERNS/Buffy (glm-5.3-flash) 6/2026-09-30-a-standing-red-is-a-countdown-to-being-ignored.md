---
document:
  title: "A standing-red is a countdown to being ignored — re-derive the noise's composition before tuning anything"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1093"
---

# Pattern: silence the noise by classifying it, not by suppressing it

verify_task.py had printed the same two report-only findings on every run for
days: "Main.java has N unexpected diff lines" (a positional-compare artifact
that grew to 160) and "N .md files missing author_llm" (202, mostly advisory
tiers). Exit 0 the whole time — nobody acted on them, nobody believed them.
B5-1093 fixed the instrument without going deaf:

1. **Classify before you tune.** The 160 lines decomposed into: landed
   intentional evolution (markers), formatting displacement (structural
   exemptions), and genuine positional-compare residue (stays report-only).
   One classification, three different treatments, zero blanket suppressions.
2. **Scope to the tier the rule actually governs.** The 202 provenance misses
   were 195 advisory-tier backlog items the repo already tracks as backlog.
   Scoping the check to governance tiers turned a 202-item yawn into a
   7-item actionable list — the backlog number is recorded once, in a report,
   instead of re-measured forever.
3. **Prove the repair can still fail.** The row's demand — "prove the
   try-with-resources negative control still FAILs when the repair is
   reverted" — is the anti-suppression clause: a revert-proof run from a
   scratch copy (never the live file) showed the unanchored pattern producing
   7 blocking findings and exit 1. A gate whose controls cannot fail is a
   decoration; a gate whose controls CAN fail is a gate.
4. **Markers cite their landing rows.** Every added marker group names the
   decision that landed the shape (B5-0001/B5-0103 conversion, B5-0319
   starter decks). A marker without provenance is indistinguishable from a
   suppression wearing the same keyword.
5. **Residual noise stays visible on purpose.** 34 honest residue lines beat
   0 tuned-away lines: the count is the detector defect's vital sign, and a
   future fix of the positional compare should see it, not inherit a green
   lie.

Reusable lesson: before tuning a gate, sort its noise into four boxes —
landed intent, formatting, out-of-tier backlog, real signal — and give each
box a different mechanism. A gate silenced in one motion silences all four.
