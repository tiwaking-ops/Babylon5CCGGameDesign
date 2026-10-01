---
document:
  title: "A flagged follow-up is a cross-artifact dependency"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0986"
---

# A flagged follow-up is a cross-artifact dependency

Traces to: B5-0986 (repairing the B5-0805 guide caveat that the B5-0977 ui fix
made false), seeded by the same session that observed the flag.

When a fixer closes a row with "this makes X stale, flagged for a follow-up", the
flag is the load-bearing handoff: it names a second artifact whose truth depended
on the first. Three disciplines make the follow-up cheap and correct. (1) The
fixer flags instead of editing out of scope — the guide edit here would have
violated one-writer-per-scope if done under the ui claim. (2) The follow-up
claimer takes replacement facts from the *fixer's report*, not from a fresh read
of the code — the report records what changed, in what order, and why, which is
exactly what the stale text needs to be replaced with; re-deriving from code risks
documenting a different implementation detail than the one the fixer verified.
(3) The repair states its dependency chain in the artifact itself ("repaired
B5-0986 per the B5-0977 fix"), so the next reader can trace why the text changed
without archaeology.

**Rule:** treat a close-out's "flagged, not fixed" section as a work order with
its citation attached — and when executing it, cite the flag's report as the
source of the replacement facts, not just as the reason the edit happened.

**Reusable lesson:** documentation debt created by a code fix is a dependency edge
between two rows, and the cheapest repair walks the edge the fixer already drew —
flag, report, claim, cite — rather than re-measuring the system the report already
measured.
