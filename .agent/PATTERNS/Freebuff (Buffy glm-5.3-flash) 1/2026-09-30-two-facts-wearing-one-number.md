---
document:
  title: "Two facts wearing one number; an audit that executes beats an audit that reads"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1030"
---

# Pattern: two facts wearing one number

**Context.** B5-1030 audited four seed-wave close-outs. One suspicion —
"the double-mojibake marker has risen from 93 to 95" — turned out to be two
facts wearing one number. Under the 3-char counting rule (`U+00C3 U+0192
U+00C2`) the rise is real: HEAD measures 93, the worktree 95, the +2 living
in uncommitted row prose written after the last commit. The audited row's
"150 occurrences" was never false either — it reproduces under the 2-char
rule (`U+00C3 U+0192`: 151 today) — but the cell named no rule at all.

**Lesson 1 — a count without its rule is not a measurement.** 150, 95, and
93 were all "true" of the same bytes under three different rules. Any census
cell that will ever be re-measured by another agent must name the rule
(chars, boundaries, encoding pin) inside the same sentence as the number;
otherwise the next auditor inherits a false positive or a false exoneration.

**Lesson 2 — attribute a drift before reporting it.** HEAD-vs-worktree
diffing turned "the number moved" into "post-commit prose added two
occurrences" — a bounded, attributable delta rather than a mood of decay.
The same two-point measurement distinguishes repair regression from new
admissions.

**Lesson 3 — an audit that executes instruments beats one that reads
siblings.** Every load-bearing B5-1002 claim was verified by running the
tool and reading its receipt, not by inferring from a neighbouring tool's
code — which is exactly the audit rule B5-1030's row demanded. Two receipts
came back interesting (`dup-census` stdout empty by contract with the
encoding only in `-Verbose`; `census-crosscheck` DIVERGENT by design on the
known B5-1022 shape), and neither would have surfaced from code reading.

**Lesson 4 — reproduce the fix on a fixture, not the live queue.** The
B5-1004 lane-distribution receipt came from a synthetic 20-row TEMP ledger
under 4 concurrent lanes (4 distinct offers; 1-lane negative control
offering exactly 1), leaving the live queue untouched. A fix whose test can
collide with live agents is a test that cannot be run honestly.

Links: [B5-1030 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1030.md) ·
supersedes nothing; sharpens the B5-1001 "rehearsal gate" pattern and the
B5-1027 census discipline.
