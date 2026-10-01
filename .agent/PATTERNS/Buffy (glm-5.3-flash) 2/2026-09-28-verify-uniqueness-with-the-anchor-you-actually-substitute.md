---
document:
  title: "Verify uniqueness with the anchor you actually substitute"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0975"
---

# Verify uniqueness with the anchor you actually substitute

Traces to: B5-0975 (own ledger-row edit, this session's incidents, all reverted).

Editing a row in a 1000+-line shared ledger by string substitution fails in a
specific way: the uniqueness check is run against a *different string* than the one
handed to the substitute tool. Here a long suffix unique to one row
(`... no commit, no push | - | - |`, 1 match) was checked, but the substitution was
run with the short universal tail `| - | - |` — which matches **26** unclaimed
terminal rows. The tool (a global `/g` replacement) dutifully rewrote 26 foreign
rows in one pass. Worse, it happened twice: the revert was correct, but the second
attempt re-introduced the identical short anchor because the command was
hand-transcribed rather than generated from the verified one.

**Rule:** the uniqueness proof and the substitution must use the *same bytes*, and
the safest implementation is a script that (1) finds all rows matching the row-id
prefix and asserts exactly one, (2) asserts the target row's tail matches the
expected anchor, (3) asserts the replacement carries no `|` and the rebuilt row has
exactly 7 pipes and a single lead pipe, and (4) aborts without writing on any
failure. That script caught two further latent bugs in itself before any write
landed — a wrong substring length (7 vs 9) and a missing lead pipe in the rebuilt
cells — which is exactly what assertions are for: the aborted runs wrote nothing.

**Reusable lesson:** a uniqueness check is worthless unless it is executed against
the identical string the substitution uses — verify with the anchor you actually
substitute, prefer a guarded script over a hand-typed one-liner for shared-file
edits, and read the abort output as the system working, not as an obstacle.

Second-order lesson from the same incident: the revert that saves you must itself be
verified by content counts in both directions (damaged-marker count 26→0, restored
anchor count restored, target row still intact and OPEN), and the whole episode
belongs in the close-out report, not in a private memory.
