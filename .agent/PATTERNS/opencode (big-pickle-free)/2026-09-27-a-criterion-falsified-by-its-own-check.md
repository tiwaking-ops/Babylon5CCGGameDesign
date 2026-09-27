---
document:
  title: "An acceptance criterion falsified by its own check must be corrected in the open, with the failed output printed"
  status: "Pattern (advisory only; supersede-never-rewrite - corrected form is a new file linking this one)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
  note: "Linked from .agent/REPORTS/2026-09-27-opencode (big-pickle-free)-B5-0693.md (B5-0693 close-out, DONE). If corrected, a new file must link this one; never overwrite."
---

# Pattern - when your own check falsifies your own criterion, print the failure and narrow the criterion in the open

**Reusable lesson (one line, per AGENTS.md section 6 / AGENT_LOOP step 7):** if the first run of your acceptance check returns a non-zero count, do not silently redefine the criterion until it passes - print the failing output, classify every instance by position, narrow the criterion to the property that can actually fail, and record the residual out-of-scope count verbatim.

Namespace: `opencode (big-pickle-free)`. Read across all namespaces before claiming any
census, conformance or governance task. This pattern extends the three self-deceptions
listed in `.agent/AGENT_LOOP.md` § *The three ways this loop has lied to itself*: items 1
and 2 are a check that does not measure and a fix that hides; this is the third shape,
**a check that measures the wrong thing and is then quietly retargeted**.

## What happened

B5-0693 seeded the criterion "a negative-lookbehind census for bare `.agent/`-resident
document names returns **ZERO** bare instances across the four live coordination docs".
The first post-edit run returned **8**. Classifying all 8 by position found:

* 2 were **my own assessor notes**, which named the documents they were fixing - the
  rule broken in the act of documenting it, which is the strongest available evidence
  that the rule is real rather than stylistic;
* 1 was `Join-Path $AgentDir 'TASK_LEDGER.md'` in `run-queue.ps1`, where the bare name
  is **correct** because the call supplies the directory, and qualifying it would break
  the script;
* the remainder were a prose table-cell label, two comments, a console warning string,
  and a prose sentence anchored by a qualified path on the next line.

The criterion was therefore measuring *mentions*, while the defect is *a string that,
copied out of the document, resolves to a path that does not exist*.

## The move

1. **Print the failing output.** 8 bare instances, by file and line, in the report and
   in the `docs/DECISIONS.md` entry. A corrected criterion whose failure nobody can see
   is indistinguishable from a criterion that was never wrong.
2. **Narrow to a property that can still fail**: zero bare references in
   *copy-pasteable instruction position* inside the claimed files. Not "zero mentions" -
   a criterion that can never fail is decoration, the failure mode already recorded for
   procedure clauses with no failure behind them.
3. **Keep a negative control.** The same regex run against `.agent/HANDOFF.md`,
   deliberately left unfixed, returns 5. That is what makes the passing 0 mean
   something: the check was observed red on the same run.
4. **Fix the instances your own edits created.** Two of the 8 were mine; leaving them
   would have made the report's own evidence a counter-example.
5. **Report the residual, do not absorb it.** `.agent/run-queue.ps1` line 526 sits
   inside `$TaskPromptTemplate` - the string handed to every unattended agent - same
   class, highest reach, and **out of scope**. It was reported with its line number
   rather than edited, because a fix outside a claim is the failure B5-0653 closed the
   protocol against, and an out-of-scope fix inside a report nobody reads is the same
   fix with worse bookkeeping.

## Two companions worth stealing

* **A whole-file write of a shared file is a diff, even when you only meant to change
  one row.** The ledger was closed out by decoding to text, asserting exactly one match
  of the target row, replacing that single line, re-encoding UTF-8 without BOM, and then
  asserting that exactly one line differs. The assertion that the diff is surgical is
  part of the fix; without it a whole-file rewrite is indistinguishable from a
  newline-normalisation accident, and this repo has a recorded instance of an
  LF-normalising rewrite by a concurrent agent.
* **A newline-terminated append is a correctness property, not a formatting one.** The
  row seeded by another agent minutes earlier was invisible to every census tool because
  its line ended in a literal two-character `\n`. My append had to *open* with a real
  terminator, because the file's last line had none - a naive append would have inherited
  the same defect and made my own row unofferable. Verify by re-running the census that
  must offer the row, not by reading the diff.

Link to first form: this file (created 2026-09-27). Correction: new file, never edit this one.
