---
document:
  title: "An empty result is a verdict, never a shrug"
  status: "Pattern"
  task: "B5-1010"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# An empty result is a verdict, never a shrug

**Measured 2026-09-29, Babylon 5 CCG, while closing the ledger-query wildcard
quirk (B5-1010).**

`ledger-query -Status ALL` matched nothing and exited 0 — twice observed, twice
deferred (B5-0998, then the verification pass), because each observer filed it as
a quirk rather than a collapse. The collapse: the tool answered three different
questions with the same signal.

| Question | Correct answer | Old behaviour |
|---|---|---|
| Did it run and find nothing? | exit 0 + "0 rows" | exit 0 + "0 rows" ✓ |
| Could it not interpret the request? | a distinct failure | exit 0 + "0 rows" ✗ |
| Did the wildcard mean everything? | everything | exit 0 + "0 rows" ✗ |

A reader scripting against it cannot distinguish "ledger clean" from "typo in
the flag" — the same indistinguishability the B5-0777 exit-contract repair fixed
for the duplicate census (`0` clean / `1` duplicate / `2` unreadable), reached
from the query side. The generalisation: **any tool with a filter parameter
needs a third verdict for could-not-interpret**, or its empty result quietly
becomes the one number every caller learns to ignore.

## The design fork

When a legacy flag *silently misbehaves* rather than erroring, you can alias it
to what its users always meant (chosen here: ALL → wildcard) or fail it loudly.
Alias when the misbehaviour is a *superset* of intent users already hold (they
typed ALL wanting everything); fail it when the misbehaviour could be hiding a
mistake (a typo'd status should stop the pipeline, not return "nothing"). Both
halves were implemented here: alias for ALL, hard exit 3 for unrecognised
values — measured against the real status vocabulary first, never invented.

## Reusable lesson

Three verdicts — ran-clean, matched-nothing, could-not-interpret — need three
distinct exit codes; a filter tool that collapses them makes its empty result
worthless, and the paired test that proves the fix must assert the exit code,
not the presence of a table.
