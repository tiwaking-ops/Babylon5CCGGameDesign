---
document:
  title: "An anchor is a pointer, not a fact"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0994"
---

# An anchor is a pointer, not a fact

Traces to: B5-0994 (repairing the tool-rule-convergence proposal's stale
run-queue.ps1 line anchor).

A line-number citation in a growing file rots by design — the number is a pointer
into a moving document, and every edit above the target shifts it without any
signal at the citation. This one rotted twice between its writing and its repair:
the seed recorded 310 (already wrong; the drift began at 252), and by claim time
the comment sat at 319. A repair that trusted the seed's number would have
shipped a second wrong pointer.

The repair that lasts does three things: re-measure instead of inheriting the
seed's number; verify the pointed-to *substance* still matches (the number could
also be stale because the target moved *away*); and leave the citation dated —
"verified fresh by <row>" — so the next drift is visible as staleness rather than
silently misleading. A content fingerprint (grep-able phrasing of the target)
makes the pointer self-healing for any reader willing to re-run one command.

**Rule:** before trusting any line-number citation, re-locate the target by
content and compare; when repairing the citation, write the measured number with
its verification date, and keep the content fingerprint beside it.

**Reusable lesson:** pointers decay silently while facts decay loudly — treat a
line number in any growing file as a hypothesis to re-measure, never as data to
copy forward.
