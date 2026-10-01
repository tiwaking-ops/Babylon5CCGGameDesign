---
document:
  title: "Take the free half of a gated decision"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Take the free half of a gated decision

**Task:** B5-0972 (node_modules disposition, 2026-09-28).

**The trap.** A decision row offers three options with different authority costs:
one executable now, one needing human ratification, one needing a separate explicit
instruction. Treating the row as "record a recommendation and stop" leaves the
zero-authority option unimplemented — the hazard it closes stays open for another
session — while treating it as "pick the best option and run it" oversteps the human
gate the repo's governance explicitly places on removals. Both are failure modes; the
shape that respects both is: **implement the free half, transcribe the gated half
with its rollback beside it.**

**What made "free" definable rather than convenient.** An option is free when (1) it
touches no tracked bytes (an ignore rule changes no index entry — proven by
`git ls-files node_modules` staying at 67,258 post-write), (2) it is reversible by
one command (delete the line), (3) it *composes* with the future ratification instead
of prejudging it (the ignore rule becomes load-bearing the moment the human-approved
untrack lands, the `/.qwen/` belt-and-braces precedent), and (4) it closes a measured
hazard (re-entry via `git add -A` checkpoint sweeps) rather than a hypothetical one.

**And write the gated options as commands, not prose.** "git rm the files" in prose
makes the human ask an agent to re-derive the sequence at ratification time; the
actual command line, its expected effect (68,117 → 859 tracked files), and the
rollback *beside it* (`git reset --hard HEAD~1` unpushed / `git revert <commit>`
pushed) make the human decision a one-glance read. One deliberate omission: the
history-rewrite option got no reproduced command at all — embedding a footgun in a
report is how a later session runs it by accident.
