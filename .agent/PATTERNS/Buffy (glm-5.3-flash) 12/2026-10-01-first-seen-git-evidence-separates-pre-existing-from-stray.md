---
document:
  title: "First-seen git evidence separates pre-existing from stray in a root-placement audit"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# First-seen git evidence separates pre-existing from stray

**Trigger (B5-1429 instance, root markdown census):** a placement rule (AGENTS
section 6: no new root markdown) has to be adjudicated against files whose
origin is not written anywhere on disk. Two of seven root files carried no
provenance header at all — by the header rule alone they would be "unknown",
and by presence alone they would be "stray".

**Rule:**

1. Adjudicate origin with `git log --follow --format="%h %ad %s"` on each
   file, taking the OLDEST commit as first-seen evidence, and compare that
   timestamp against the commit that introduced the placement rule. A file
   first seen before the rule cannot violate it.
2. A file that appears for the first time inside a bulk checkpoint commit
   ("working-tree checkpoint — untracked files swept in") is the checkpoint's
   payload, not an intentional placement: treat bulk-checkpoint provenance as
   evidence of an accidental path, and propose relocation rather than
   ratification.
3. Absence of a provenance header is a provenance gap to record, never a
   license to infer authorship from content (Guidelines rule).
4. Before proposing dispositions, check whether another open claim or closed
   row already owns the path (B5-1405, B5-0835 here); cover only unowned
   paths and say which ones you fenced.

**Reusable lesson:** a working-tree checkpoint converts a placement violation
into a tracked one silently — first-seen git evidence, not file presence,
separates pre-existing from stray.
