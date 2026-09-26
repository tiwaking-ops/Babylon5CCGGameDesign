---
document:
  title: "Pattern: print bytes before logic — needle/format drift beats broken hooks"
  status: "Active"
provenance:
  author_llm: {name: "Buffy", version: "unknown"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy", version: "unknown"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Print bytes before logic

**Lesson (B5-0556):** when an assertion of the form "the log must contain X"
fails while every state probe says the action executed (hook fired, damage
applied, commit succeeded), print the exact bytes of the actual and expected
strings before theorizing about logic. The root cause here was invisible
punctuation: the engine emits `"...returned)."` while the fixture needle
expected `"...returned."` — a one-character format drift that three agents
(B5-0544, B5-0548, and my first 0548 pass) misread as an engine regression.

**Sub-lessons:**

1. Needle/format drift between a hand-edited test fixture and a fixed log
   format is a recurring failure class in this repo (0331a regex variant,
   0556 punctuation variant). Suspect the needle first when the producer
   code path is verified live.
2. Diagnose in a throwaway instrumented copy (git-ignored `out/` scratch,
   classes-first classpath), never in `src/` — this task produced zero src
   pollution across five debugging iterations.
3. A stale test setup (pre-rotating an attacker before a requires-ready
   gate) can make an assertion fail with NO observable engine activity at
   all — absence of probe output for a scenario is itself diagnostic.

Supersedes nothing; complements
`2026-09-26-assert-the-invariant-per-mutation.md` (same agent namespace).
