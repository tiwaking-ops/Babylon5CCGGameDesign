---
document:
  title: "Sweep hygiene needs a tracked-check before it needs an ignore line"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Sweep hygiene needs a tracked-check before it needs an ignore line

**Task:** B5-0955 (scratch-artefact disposition, 2026-09-28).

**The trap.** The row's premise measured ten "sweepable" artefacts — but three of the
paths it names are **TRACKED** (`b5ccg/compile-afterfix.txt`, `conformance-final.txt`,
and sibling `conformance-afterfix.txt`). A tracked path cannot be swept by `git add -A`
at all, so an ignore line written for it is inert decoration: gitignore does not apply
to tracked paths. Acting on the premise as stated would have "fixed" a hazard that
does not exist while leaving the count wrong in a tracked, forever-readable file.

**The check that costs one command.** `git ls-files --error-unmatch <path>` before
`git check-ignore <path>`. Tracked beats ignored beats sweepable, in that order —
each answer changes the disposition:
- *tracked* → the real remedy belongs to whoever owns untracking; record the intent
  (belt-and-braces entry, the `/.qwen/` precedent) and move on;
- *untracked + un-ignored* → the actual sweep hazard; an ignore line fixes it and is
  reversible;
- *already ignored* → verify and say so; do not double-add.

**The disposition rule the row asked for, answered with measurements.** Prefer IGNORE
for anything any report cites as evidence (citation grep is cheap and settles it),
and for anything non-reproducible (tuning state, scratch harnesses whose value is the
dead ends they encode). DELETE only what is reproducible *and* worthless — and even
then weigh it: deleting a 102-byte failed-invocation log buys nothing an ignore line
doesn't, while an ignore line is reversible and deletion of an untracked file is not.

**Rule of thumb:** a hygiene task inherits its premise from a measurement pass that
ran hours earlier; re-measure the premise first, because the tree moved — three claim
races happened in this repo while this row sat OPEN.
