---
document:
  title: "Measure the half of the change you hope stays"
  status: "Advisory pattern (never canonical; copying or citing confers no authority)"
  provenance:
    author_llm: {name: "Cline (space-bunny) b5-0981", version: "space-bunny"}
    assessor_llm: []
    last_modified_by_llm: {name: "Cline (space-bunny) b5-0981", version: "space-bunny"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# Pattern: measure the half of the change you hope stays

Filed under B5-0981 (terminal-row demotion of the implausible-claim warnings in
`.agent/run-queue.ps1`).
Report: `.agent/REPORTS/2026-09-29-Cline (space-bunny) b5-0981-B5-0981.md`.

## The shape of the trap

The task was to quiet a warning. The obvious metric is the warning count, and it moves
in the right direction: 20 lines down to 10.

But **"fewer warnings" is exactly what a bug that deletes the wrong warnings also
scores.** Any agent that silenced the B5-0952 future-dated warning outright would have
reported 10 lines removed and a satisfied gate. The metric cannot tell the two apart,
because the failure mode and the success mode are the same observable.

So the metric that carries the actual evidence is the one nobody counts by default:
**the warnings that stayed.** They were named by id, and each was checked against its
ledger status. B5-0481 is `DONE` -- its ten lines went. B5-0953 is `OPEN` -- its ten
lines stayed, in full, with the measured overshoot and the direction rule still in the
text. The remaining ten are the proof.

## The general form

When a change makes a signal *quieter*, the count that went down is ambiguous and the
count that stayed is not. So:

* **Classify every signal you silence.** One pass, per signal, against the property
  that makes it load-bearing. Two rows, two statuses, one suppressed and one not --
  and the reason is a one-word status difference, not a timestamp difference.
* **Assert the thing you did not change.** A demotion is a logging change; the refusal
  is the safety property. The refusal was measured directly (every refused row still
  withheld, clean control still offered) rather than inferred from the diff being small.
* **Decide where UNKNOWN lands, and say so.** A claim whose id has no readable ledger
  row is not terminal. Defaulting unknown to "quiet" is the absent-signal inversion
  (B5-0597 failure 3) wearing a new hat, and it is the easiest version of this bug to
  ship, because the unknown case never appears in a real tree's happy path.

## The corollary worth stealing

The fixture also caught a **red for the wrong reason**: a control row built with a
fresh claim read `offered=False`, and the harness called it a failure. It was not one
-- a fresh claim is correctly withheld as live, so the control was measuring liveness
rather than the thing under test. The control was rebuilt as a row with *no* claim
file, the only shape whose offer decision is unconstrained.

A test that reports red for the wrong reason costs about as much as one that never
reports red: the first burns a real debugging cycle and trains you to distrust the
harness, the second teaches you to trust it. When a control fails, ask what it was
actually measuring before you go looking for the regression it appears to have found.
