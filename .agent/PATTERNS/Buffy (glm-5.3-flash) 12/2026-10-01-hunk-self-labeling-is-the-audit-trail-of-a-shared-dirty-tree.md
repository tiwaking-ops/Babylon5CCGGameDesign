---
document:
  title: "Hunk self-labeling is the audit trail of a shared dirty tree"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy", version: "glm-5.3-flash"}
  last_modified_date: "2026-10-01"
---

# Hunk self-labeling is the audit trail of a shared dirty tree

**Trigger (B5-1417 instance):** triaging +285 lines of uncommitted ui diff
from multiple sessions against two landed guards. Every hunk carried a
`// B5-NNNN:` comment naming its owning task, which turned the audit into a
label-resolution exercise instead of code archaeology.

**Rule:**

1. **Label what you land in the working tree with its task id**, in a comment
   at the hunk, so a later read-only auditor can map diff → owner row without
   guessing. The repo's dirty tree is a multi-session collaboration surface;
   labels are the only ownership signal that survives inside the file itself.
2. **Audit by resolving labels, then by receipt comparison:** grep the diff
   for the id pattern, check each id's row status, and byte-compare any
   guard-critical hunk against its close-out report's quoted text. A receipt
   that quotes exact lines makes drift detectable.
3. **Zero orphan hunks is the pass condition**; any unlabeled hunk is a
   finding to record (not repair) with its diff site, so its owner or a human
   can adjudicate.
4. Guards are the first thing to re-verify on a dirty tree: a later session's
   hunk can silently drop a conjunct. The B5-1177 receipt's verbatim
   predicate quote is what made byte-consistency provable this pass.

**Reusable lesson:** hunk self-labeling in comments is what makes a
multi-session dirty tree auditable — every dirty hunk this pass owned itself,
so zero required archaeology beyond the label.
