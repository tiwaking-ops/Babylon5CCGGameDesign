---
document:
  title: "A present claim file and a green gate can still mean BLOCKED"
  status: "Pattern (advisory only, never canonical)"
  provenance:
    author_llm: {name: "Cline (space-bunny) b5-0737", version: "b5-0737"}
    assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A present claim file and a green gate can still mean BLOCKED

**Reusable lesson:** A claim file's existence is not a liveness verdict and a passing
gate is not a licence to sweep a shared tree — on B5-0737 both were true at once
(a claim file that its own owner had already released and merely failed to delete,
over a build that compiled clean and passed 643/643 plus smoke), and the correct
close-out was still BLOCKED, because the deliverable was a *tree-wide* commit and the
tree belonged to three other live claims.

**Why this is its own lesson:** the two obvious checks both came back clean, so
neither could carry the decision. "Is the claim file present?" said *taken* and would
have wedged the row forever on a ghost; "is the gate green?" said *go* and would have
committed 161 MB of another agent's half-written batch plus 352 lines of their
uncommitted `src`. A check that cannot fail is not a check, and a gate that only
measures *the code* does not measure *the tree* when the deliverable is the tree.

**How to apply:**

1. Distinguish a **live claim** from a **released ghost** by the three signals, not by
   the file's presence — and let the shipped detector answer, so the verdict is not
   your reading of a timestamp. Here `ledger-query.ps1` printed `STALE | reportable`.
2. When the deliverable is a commit of the whole tree, enumerate the **live claims
   overlapping that tree** and size the bytes under them before staging anything.
   The row's own exclusion list is not exhaustive; it named two paths, and the
   dangerous bytes were elsewhere.
3. Record reap evidence in the ledger **before** deleting, so a reap is never an
   unevidenced deletion.
4. Re-read liveness fresh at decision time; a prior report's stand-down is a
   conclusion about *its* moment, not an inherited claim.
5. A green gate you actually executed is worth stating explicitly when you block
   anyway, so the next agent does not re-derive it or misread the block as a red
   build.

Source: `.agent/REPORTS/2026-09-28-Cline (space-bunny) b5-0737-B5-0737.md`; ledger
`.agent/TASK_LEDGER.md` line 899; interpretation in `docs/DECISIONS.md` under
"2026-09-28 - Cline (space-bunny) b5-0737". Filed as a NEW record
(supersede-never-rewrite; corrects the standing-down reflex recorded in
`.agent/PATTERNS/me-so-poor/2026-09-28-blocked-release-foreign-claim-do-not-rewrite-agent-id.md`,
which was correct when its owner's heartbeat was fresh but does not cover a claim
whose three signals have all gone stale).
