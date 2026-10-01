---
document:
  title: "Count, reconcile, then classify — the total that doesn't add up IS the finding"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 7", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1127"
---

# Pattern: reconcile the census total before publishing the breakdown

B5-1127 counted 40 registered dispatch ids and matched them against the
record pool — and the per-type table summed to 39. Publishing the table with
the discrepancy unexplained would have invited two wrong conclusions ("the
census is sloppy" or "a record is missing"). Chasing the one-id gap found a
third answer: `mer_metric_fixture` is registered in CardEffects and consumed
by HeadlessConformanceTest's in-memory card factory — a **synthetic
registration with a named consumer**, neither dead code nor data.

The second instance: AFTERMATH reads "0 registered ids" — which in a
per-type fallthrough table *looks like* 117 uncovered records. Reading the
dispatch site showed the type is keyed on `triggerCondition` at eligibility
time (AftermathCard.isEligible) plus two bespoke id sets at resolution time.
Same number, opposite meaning: keyed differently, by design.

Rules this pattern fixes:

1. **A total is a checksum.** When the parts don't sum, the difference is a
   finding to chase *before* publication, not a footnote after. Here the
   39-vs-40 gap identified a registration class (synthetic fixtures) no prior
   row had named.
2. **A zero in a census column is ambiguous between three states** — nothing
   exists, it exists but is keyed elsewhere, or it is synthetic. Only reading
   the dispatch site distinguishes them; a grep census cannot.
3. **"Uncovered" and "keyed differently" demand different follow-ups.**
   Uncovered = the D15 effect-dispatch epic (design work, per-type priority
   order). Keyed differently = the existing key is load-bearing and any
   future dispatch work must preserve it. Misclassifying one as the other
   sends the next implementation row after the wrong target.

Reusable lesson: every census has three layers — the count, the checksum
against a second source, and the semantics of the zeros. Publish all three
or the readers will invent the missing ones.
