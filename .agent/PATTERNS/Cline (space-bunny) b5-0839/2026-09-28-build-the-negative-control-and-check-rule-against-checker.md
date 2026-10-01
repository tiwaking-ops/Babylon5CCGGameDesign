---
document:
  title: "A finding that names a large number is still one measurement, and a checker can enforce a different rule from the one written down"
status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0839", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0839", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: "B5-0839"
---

# Build the negative control first, then check that the rule and the checker agree

Filed from B5-0839, which was asked to measure rather than believe a single number:
`verify_task.py` reported **166 `.md` files missing `author_llm` provenance**, and the
row correctly refused to triage the number because a mass governance gap and a
detector defect produce the same integer.

**The controls ran first and they passed**, which is the part that made the rest
possible: three known-good files were detected as having provenance, and one scratch
file this session created — so its "no provenance" status is a fact, not a memory — was
detected as lacking it. A positive-only suite would have "passed" a detector that flags
everything.

**Then the number did not survive.** Reported 168 (live, and drifting), **100 distinct
files, 61 of them false positives.** Two independent defects, each sufficient on its own:

1. **The count was of enumerations, not files.** `.agent/REPORTS/*.md` is a subset of
   `.agent/**/*.md`, so the third glob re-added every REPORTS file the second had
   already collected. Arithmetic proves it rather than a hunch: REPORTS reads 68
   distinct and 136 enumerated; the namespaces *not* in the third glob read the same
   either way.
2. **The checker enforced a rule nobody wrote.** `AGENTS.md` §1 requires
   `author_llm: <name> (<version>)`. The regex demanded a YAML flow-map with two quoted
   literals. **45 of the 61 false positives satisfy the written clause verbatim** — a
   format mismatch, escalated by a detector into a governance crisis.

## The transferable shape

* **A negative control must be a file you made, not a file you remember.** Its status is
  then a fact. A remembered "known-bad" is the same class of defect as the premise
  under test.
* **A large plausible number is the signature of a broken counter.** 166 → 100 was
  found by asking "how many *distinct* paths produced this count", which is a one-line
  question and needs no domain knowledge.
* **When a checker fails something, read what the rule says before reading what the
  checker said.** If those two differ, the finding is a finding *about the checker*.
* **Check the error's direction, not just its size.** A leakage test — "does it report
  every file that truly lacks the field?" — returned 0 misses. The detector is
  *over-inclusive only*, which is the safe direction and makes the fix a tuning problem
  rather than a trust problem. A detector that both over- and under-reports could not be
  repaired by reading its output more carefully.
* **Concentration is a second signal, and it is often the loudest.** 98 of 100 sat in
  `.agent/REPORTS` + `.agent/PATTERNS`, the directories that accumulate fastest and
  matter least. The 2 files in the governed `docs/proposals` tier were the real signal —
  and they were a *different defect class* (prose provenance in the body, not
  frontmatter), not a bigger instance of the same one.

**A finding that concentrates entirely where nobody is looking is usually the checker
looking at itself.**

Related, not superseded: B5-0683 (a remembered premise is not a measurement) and
B5-0831 (a control that can never fail is decoration) — this pattern is those two
applied to a counter rather than a boolean.
