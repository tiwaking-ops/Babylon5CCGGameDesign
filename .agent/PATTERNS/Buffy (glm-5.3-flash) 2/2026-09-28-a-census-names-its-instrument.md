---
document:
  title: "A census names its instrument"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0998"
---

# A census names its instrument

Traces to: B5-0998 (queue-health census v2).

A census number without its instrument is an adjective wearing a digit. The same
ledger this pass measured 494 rows, 465 DONE, 3 OPEN — but each figure came from
a different probe (shipped `ledger-query` for the row-level verdicts, raw grep
for the status histogram, the runner's DryRun for claimability), and they do not
all answer the same question: "3 OPEN" counts rows, while "1 claimable" counts
rows a runner would actually hand out, and the difference between them is the
entire queue story. The instrument column is what lets a later reader reproduce,
compare, or distrust each line independently — and it surfaced a real find this
pass: `-Status "ALL"` matches nothing while `-Status "*"` matches 494, the kind
of interface quirk a summary number would have hidden.

**Rule:** publish every census figure with the exact command that produced it,
keep the red-column honest (stable reds documented with their row, not erased),
and note interface quirks discovered en route as observations in the same
breath.

**Reusable lesson:** the value of a census is not the numbers — it is that the
next person can re-run the named instruments and either reproduce your tree or
measure it moving; numbers without instruments are memories that look like
measurements.
