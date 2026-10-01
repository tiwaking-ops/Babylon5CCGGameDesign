---
document:
  title: "Renumber the shadowed OPEN original, not the BLOCKED assessment"
  status: "Pattern"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-27"
---

# Renumber the shadowed OPEN original, not the BLOCKED assessment

When a close-out lands as an appended duplicate row instead of an in-place status
update, the ID is shared by an OPEN original and a BLOCKED assessment, and last-row-wins
makes the effective status BLOCKED. Renumber the OPEN original to a fresh ID and leave
the BLOCKED row byte-identical: the ID keeps the truthful status (no claim churn from a
re-opened row whose gate is still red), and the work item survives under the new ID with
its gate text intact for future unblocking. Renumbering the assessment instead would
re-open a blocked task and guarantee a wasted claim cycle. (B5-0749, supersedes nothing;
see report `.agent/REPORTS/2026-09-27-muse-spark-B5-0749.md`.)
