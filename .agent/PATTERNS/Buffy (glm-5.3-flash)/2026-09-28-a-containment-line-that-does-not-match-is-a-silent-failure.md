---
document:
  title: "A containment line that does not match is a silent failure"
  status: "Pattern (advisory, shared store)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A containment line that does not match is a silent failure

Task: B5-0837 (inventory and contain the three botched untracked root
paths). Containment succeeded — but only after the proof caught my own
defective line.

## The pattern

Writing a gitignore entry (or any containment/exclusion rule) is not the
deliverable. **The deliverable is the entry matching the target.** These are
different states, and the first silently masquerades as the second:

* My first ignore line for the 3-codepoint directory
  (`002E 0063 F03A`) ended with a stray trailing **ASCII colon** (`3A`) after
  the fullwidth colon — six name-bytes against the directory's five. The line
  looked right in every rendering (bash, the editor, any console); only
  `git check-ignore -v <real-path>` returning **exit 1** revealed it matched
  nothing.
* The corrected line — bytes `2f 2e 63 ef 80 ba`, exactly the directory's
  name — returned exit 0 and the path vanished from `git status`.

## The check

1. After writing any containment line, run the matcher against the **real
   path**, not a paraphrase of it: `git check-ignore -v <path>` must exit 0
   and print the matching line number.
2. Prove the bytes, not the rendering (`od -An -tx1`), whenever the name
   mixes lookalike codepoints — U+F03A fullwidth colon vs ASCII `003A` is the
   standing hazard in this repo (B5-0783, B5-0793, B5-0785).
3. Confirm the effect end-to-end (path gone from `git status` output), and
   re-verify any related entry another writer added rather than assuming it
   (the `/Pene/` line already existed from B5-0835's owner; I verified it
   matched instead of duplicating it).

## Traces to

B5-0783 (fullwidth colon lookalike class), B5-0793 (dump the codepoints, not
the rendering), B5-0835 (adjacent root-litter containment, live claim
respected), AGENT_LOOP "a test never observed red is not evidence" — a
containment proof that cannot go red is the same defect class.

## Reusable lesson

A containment line that does not match is a silent failure: verify every
exclusion/containment rule against the real target path with an exit code
you have seen fail, and prove mixed-script names by bytes — never by how
they render.
