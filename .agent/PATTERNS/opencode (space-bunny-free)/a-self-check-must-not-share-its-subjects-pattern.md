---
document:
  title: "A self-check must not share its subject's pattern"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A self-check must not share its subject's pattern

Advisory only, same tier as `investigations/`. Never canonical. Citing confers nothing.

**The rule.** A verification that reuses the pattern, predicate, or filter of the code it
verifies cannot fail. It compares a set with itself. A green result from such a check
carries **zero information** — not weak information, none — and it is worse than having no
check, because it converts an unknown into a false assurance, and the assurance then gets
recorded as evidence by the next pass.

**The worked instance.** A ledger-query tool shipped with a self-check that read:

```powershell
# rows were built like this:
if ($line -match '^\|+(?:\s*(B5-\d+)\s*\|)') { $rows += ... }
# and "verified" like this:
if ($ln -match '^\|+(?:\s*(B5-\d+)\s*\|)') { $matched++ }
if ($matched -lt $rows.Count) { Write-Warning ... }
```

`$rows` is a subset of exactly the lines the second loop counts. The two numbers are
equal by construction. The check reported `312==312` — green, reported as the fix's
acceptance evidence — while **9 rows carried a corrupt status** and **4 rows were
invisible to the tool entirely**. The lesson filed alongside the fix stated the defect
class would now "report itself rather than hiding." For this class it did the opposite,
and the green self-check is why nobody looked.

**The construction to memorise.** A self-check is only worth writing if it could
plausibly return false. Ask: *what input makes this red?* If the answer is "none," delete
it. The fix used here compares the parser's output against a **deliberately more
permissive, independently-written** pattern (`^\|+\s*B5-[0-9]{4}[a-z]?\s*\|` — no capture
group, no ID constraint) plus a separate "parsed with no recognisable status" warning.
Both can go red on real data; neither shares a predicate with the code under test.

**The sibling mistake, which is the one that actually bit here.** The same pass fixed
row *visibility* and left row *status* on a hard-coded cell index. Because the tool's
stop condition filtered on **status**, not on row presence, the rows became visible and
still could not be selected. Measured after the "fix": the tool reported **17** OPEN
rows; the ledger held **24**.

> A parser fix and a *consumer* fix are different tasks. Shipping only the first yields a
> tool that is worse than broken — it returns confident wrong answers instead of
> omitting rows. Do not call a query-tool fix done until the consumer's own filter has
> been re-run against the new output and reconciled against ground truth by hand.

**The human-level symptom worth recognising.** This defect presented as *agents
repeatedly reporting "no OPEN tasks remaining"*, and a human responded by hand-seeding
work to compensate. The humans were not confused and the agents were not careless; the
tool was returning a confident false negative, and every party behaved reasonably given
what they could see. When a coordination tool's output contradicts reality, suspect the
tool's *consumers* before its producers — a tool that returns wrong data is more
dangerous than one that fails, because it is believed.

**Companion, from the same session — a `-WhatIf` the script does not declare is not a
dry run.** To test the function I ran `. .agent/run-queue.ps1 -WhatIf`. The script has no
such parameter, so PowerShell ignored it and the main loop ran: it claimed a task and
launched an agent. **Test a function by extracting it, never by dot-sourcing the file** —
dot-sourcing executes every top-level statement in it, and an unrecognised argument is
not a safety mechanism.

**Applies to:** any self-check, assertion, lint rule, or "verified" claim whose predicate
resembles the thing it verifies; any query tool feeding a stop condition or gate; any
automated index, cache, or census that reports counts.

**Read before trusting:** any green "self-check", "sanity check", or "verified N==N"
result in a report or a commit message.
