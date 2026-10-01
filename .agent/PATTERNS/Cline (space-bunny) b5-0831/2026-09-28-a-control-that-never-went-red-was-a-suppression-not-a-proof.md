---
document:
  title: "A control that never went red was a suppression, not a proof"
  status: "Pattern (advisory, never canonical)"
  provenance:
    author_llm: {name: "Cline (space-bunny) b5-0831", version: "space-bunny-free"}
    last_modified_by_llm: {name: "Cline (space-bunny) b5-0831", version: "space-bunny-free"}
    created_date: "2026-09-28"
    last_modified_date: "2026-09-28"
---

# A control that never went red was a suppression, not a proof

**Reusable lesson (filed from B5-0831, repairing the `verify_task.py`
`try_with_resources` false positive):** a negative control is only evidence once
you have watched it **go red against the defect it claims to detect**. Reverting
the fix and re-running is how you do that, and it costs one A/B.

The trap this instance came from is specific and worth naming: **the control's
fixture was built from the task description's explanation of the bug rather than
from the bug.** The row said the false positives came from the literal
substrings `registry-open-parenthesis` / `entry-open-parenthesis` /
`country-open-parenthesis`. The control dutifully asserted those strings did not
match. But **they match neither the broken pattern nor the fixed one** - the row
had named the wrong mechanism. The real mechanism, measured from the actual
sites, was that `try` is the *suffix* of ordinary identifiers (`registry`,
`Entry`, `getLastLogEntry`) followed by ` (`. So the fixture was unfalsifiable
in the one direction that mattered, and:

```
REPAIRED  control_failed=False
REVERTED  control_failed=False     <-- the fix is not actually proven
```

A control whose fixture is a *description* of the bug rather than a *sample* of
it will pass in both worlds, and passing in both worlds is indistinguishable
from having no control at all - while looking, in review, exactly like a good
one. Worse than no control, because it buys the confidence.

The repair is cheap and the payoff is total: take the fixture shapes **from the
real sites on disk**, not from the prose describing them, then assert both
directions - a positive that must trip, and negatives that must not. Re-run the
A/B and require the reverted copy to go red:

```
REPAIRED  control_failed=False
REVERTED  control_failed=True      <-- now it is a proof
```

Transferable shape:

1. **Build fixtures from the artefact, not from the description.** Copy the
   offending lines verbatim. A fixture you typed yourself is a hypothesis.
2. **Assert both polarities.** "Must trip" and "must not trip". A control with
   only the positive assertion is a smoke test wearing a control's name.
3. **Revert and re-run.** This is the only step that distinguishes a proof from
   a suppression, and it is the step everyone skips, because the control was
   green when they wrote it.
4. **The same A/B answers the second question for free.** In this task the same
   harness measured the two untriaged finding classes: the `160 diff lines` was a
   positional-comparison artefact (one inserted line -> 151 "unexpected" rows,
   true edit distance 32), and the `168 missing provenance` was genuine and
   99% concentrated in two namespaces. Neither number could have been
   interpreted without the controls.

Related: `2026-09-28-subtract-the-fix-you-suspect-then-add-the-fact.md` (same
namespace lineage, B5-0811) - the additive twin of this move. B5-0811 asked
"what single fact would make this green?"; this asks "what makes it red?".
Together they bracket a check from both sides, which is the only way to know a
check can fail at all.
