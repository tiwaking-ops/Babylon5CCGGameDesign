---
document:
  title: "Subtract the fix you suspect, then add the fact that would make it green"
  status: "Pattern (advisory, never canonical)"
  provenance:
    author_llm: {name: "Cline (space-bunny) b5-0811", version: "space-bunny-free"}
    last_modified_by_llm: {name: "Cline (space-bunny) b5-0811", version: "space-bunny-free"}
    created_date: "2026-09-28"
    last_modified_date: "2026-09-28"
---

# Subtract the fix you suspect, then add the fact that would make it green

**Reusable lesson (filed from B5-0811, re-verification of the
`census-crosscheck.ps1` red after B5-0775's `Get-Verdict-L` third-signal
fix):** a red cross-check names a *disagreement*, not a *cause*. The cheapest
way to learn which is to subtract the fix you suspect, and then to **add** the
single fact that would make the checker green.

B5-0811 inherited a red harness: `census-crosscheck.ps1` exited 1 with the
B5-0751 two-disagreement baseline still on the board, four days and several
prior reports after the fix that was supposed to have cleared it. The obvious
hypothesis was that the fix was incomplete, ineffective, or had been reverted.

Two cheap experiments killed it:

* **Subtract the hunk, from a copy of the *current* tree.** Remove exactly the
  four lines the prior task added and run the copy. The output was identical -
  same 2 disagreements, same row, same counts. Diffing against `HEAD` would
  have been the wrong instrument here, because `HEAD` also carries other
  agents' uncommitted work and will happily attribute their behaviour to your
  hypothesis. Subtracting your own suspected lines is the only A/B whose
  control is *exactly* one variable.
* **Then add, instead of subtracting again.** The question "what single fact, if
  it changed, would make this green?" has a better answer than any further
  removal. Dropping a *fixture* heartbeat that made the one unresolvable owner
  id resolve turned all 432 rows `CONSISTENT` at exit 0 - against the same live
  ledger, the same live claims, and the **unmodified** live tool. That located
  the fault in the *data* (a foreign claim naming an agent that does not exist
  under that spelling) rather than in either tool's logic, which no amount of
  code-reading had established in three prior reports.

The transferable shape:

1. **A red cross-check localises, it does not explain.** Read it as "these two
   rules disagree here", not "this rule is wrong".
2. **Subtract, don't compare against HEAD.** The working tree carries other
   agents' uncommitted edits; `HEAD` is not a control, it is a confounder.
3. **Ask the additive question.** "What one fact, if true, makes it green?" is
   a stronger probe than "what did I break?", and the answer is a *fixture* you
   can build in a temp tree without touching a shared file.
4. **A green result from an added fixture is the strongest evidence available**,
   because it shows the checker still knows how to pass - it is the "a test
   never observed red is not evidence" rule run in reverse.

Related: `2026-09-28-re-running-a-sweep-to-document-it-is-how-the-documentation-defect-surfaces.md`
(same namespace, B5-0805) - the sibling move of re-running rather than trusting
a recorded number. Both are instances of **measure the tree, do not inherit a
claim about the tree**, including claims your own prior sessions left behind.
