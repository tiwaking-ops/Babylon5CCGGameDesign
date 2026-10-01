---
document:
  title: "A number quoted three times is a memory, not a measurement"
  status: "Pattern (advisory, shared store)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A number quoted three times is a memory, not a measurement

Task: B5-0915 (execute the conformance suite for real; verify the
oft-repeated 643 figure; record the Windows no-test-path fact).

## The pattern

Ledger rows had been repeating "643/643" and "RUN_TESTS=1 green" from prior
reports instead of re-running the harnesses. This row executed both harnesses
directly against a fresh build and counted their output strictly:

* **643 `: PASS` lines, 0 `: FAIL` lines, exit 0** — the figure held, so this
  time the repetition was harmless. The check exists for the time it does
  not hold: any drift (a check added, removed, or silently skipped) would
  have been caught here instead of being absorbed into the next quote.
* A "skip"-flavoured line in the log turned out to be a **negative control**
  asserting skip behaviour — grep counts need reading, not just tallying.

The same row found a second instance of the class at the gate level:
multiple rows cite "compile.bat green plus RUN_TESTS=1 green", but
compile.bat contains no test path at all (the RUN_TESTS block exists only in
compile.sh). The citation chain had repeated a gate wording no Windows
execution ever satisfied on its own — a number (and a gate) quoted into
apparent existence.

## The check

1. When a row's premise cites a count or a gate result, **re-derive it from
   the artifact** (run the harness, read the script, hash the file) rather
   than from prior rows.
2. Count strictly: match the exact per-line verdict format (`: PASS`), not a
   loose substring like `PASS` that also hits banners and prose.
3. Read the full definition of anything you cite (the whole compile script,
   not the summary of it) before judging whether a gate exists.

## Traces to

B5-0683 (remembered premise is not a measurement), B5-0747 (sweep that ran
the probes for real), B5-0805 (re-ran rather than summarised), AGENT_LOOP
"absence of an error is not presence of a value".

## Reusable lesson

A number quoted three times is a memory, not a measurement: re-derive
cited counts and gate results from the artifacts themselves, strictly and
once more than feels necessary — the ~20-second re-run converts inherited
numbers into owned ones.
