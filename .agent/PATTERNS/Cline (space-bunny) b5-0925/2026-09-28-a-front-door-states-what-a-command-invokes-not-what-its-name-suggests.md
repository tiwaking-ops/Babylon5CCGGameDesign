---
document:
  title: "A front door states what a command invokes, not what its name suggests"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0925", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0925", version: "space-bunny"}
  last_modified_date: "2026-09-28"
---

# A front door states what a command invokes, not what its name suggests

**Reusable lesson (B5-0925).** A README is a front door, and a reader who arrives
through it is holding nothing but its text. So every instruction in it must be
transcribed from the file that implements it, never paraphrased from the name.

The concrete instance: this repository's verification switch is spelled
`RUN_TESTS=1`. That name reads as "run the tests". It is in fact two named
classes — `b5ccg.engine.HeadlessConformanceTest` and
`b5ccg.engine.HeadlessSmokeTest` — and nothing else, and its Windows sibling
`b5ccg/compile.bat` has no such branch at all. Writing "run the tests with
`RUN_TESTS=1`" would have been fluent, plausible, and wrong on the platform the
previous README's readers were most likely to be on.

**Why it generalises.** The previous README's failure (`npm install` /
`npm run dev`) is the same failure with the polarity reversed: there, a
*correctly transcribed* instruction for the wrong subject; here, a correctly
named subject with an unstated body. Both are the result of writing the door
from the repository's *name* rather than from its *files*.

**How to apply.** For every command a front door recommends, open the script or
Makefile and copy the invoked targets literally, and check the sibling scripts
for the branch that is *missing* — a documented asymmetry is a fact a reader
needs, an assumed symmetry is a trap.

Supersedes nothing. Related: B5-0921 (scaffold intent escalated to the human),
B5-0923 (tracked root litter classified).
