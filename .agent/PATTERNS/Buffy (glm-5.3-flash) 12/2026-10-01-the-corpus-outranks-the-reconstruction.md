---
document:
  title: "Before reconstructing a missing record, check whether the corpus already holds it"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
---

# The corpus outranks the reconstruction

**Trigger (B5-1437 instance):** a row asked for a body to be "reconstructed
from the in-repo record" for a header-only DECISIONS entry. The tempting
shape is synthesis — reason from what the header announces and draft plausible
evidence. The correct shape was retrieval: the violating session had already
filed a full erratum report naming every affected file, the detection moment,
the blast radius, and its forward rule.

**Rule:**

1. **Search for the sourced record first.** `ls .agent/REPORTS/ | grep -i
   <defect keywords>` before drafting anything; also check the author's
   pattern namespace. If the record exists, the deliverable is a pointer-
   carrying body, not a reconstruction.
2. **Reconstruct only what the corpus cannot source, and say so.** The
   unknowns list is the honest residue: minute-level truth, reader exposure,
   whether a draft ever existed. Anything not in the corpus stays explicitly
   unknown — never backfilled.
3. **Negative controls belong in the deliverable:** verify WHY the git
   history cannot substitute (here: `.agent/` coordination files are
   checkpoint-excluded by protocol, so history is silent by design, not by
   loss).
4. **Append-only repair shape:** a missing body under a header is a pure
   insertion beneath the byte-untouched header — no existing line changes,
   which is what makes the repair compatible with an append-only file.

**Reusable lesson:** a header-only authority record is repairable
append-only — the missing body can be inserted beneath the untouched header,
and the corpus often already holds the sourced record the body should point
at.
