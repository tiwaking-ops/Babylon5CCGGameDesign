---
document:
  title: "Run the blocked census anyway, but label it evidence and not the deliverable"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free) b5-0799", version: "space-bunny-free"}
  last_modified_by_llm: {name: "Cline (space-bunny-free) b5-0799", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Run the blocked census anyway, but label it evidence and not the deliverable

**Reusable lesson (filed from B5-0799, post-checkpoint ledger health sweep):** when a
task's gate is red, run the read-only parts anyway and file them as *evidence* — but
never let them graduate into *the deliverable*, because the gate exists precisely to
say which measurement counts.

B5-0799 is gated on "claim ONLY after B5-0699 plus B5-0737 are both DONE so the sweep
reads the post-commit tree **once**". Both read `OPEN` under live claims, the tree is
dirty with another agent's uncommitted src files, and no commit has happened. So the
row is BLOCKED. That is the correct verdict and the item stops there.

But every tool the row names is strictly read-only, and running them costs nothing and
edits nothing. The census found something a red gate would otherwise have discarded
until the checkpoints closed: **12 rows across 412 are not at 7 pipes**, none under a
live claim, so all 12 are true defect reports under the claims-first rule rather than
transient repair states. `doubleLead` was `no` on all 412, and the duplicate-ID census
exited 0.

The distinction that keeps this honest:

* **Evidence** — "here is what the tools read on the pre-commit tree at 06:32Z".
  Timestamped, scope-labelled, and explicitly not a substitute for the sweep.
* **Deliverable** — "here is the health of the post-checkpoint tree". Only obtainable
  once the gate clears.

Collapsing the two is the failure. A green-looking census on the wrong tree, filed as
though it were the sweep, would let the row be closed `DONE` on a measurement it was
written specifically to avoid making. The B5-0795 lesson covers the neighbouring case
— *a green gate is a permit for the claim holder, not an authorisation for the
auditor*. This is its mirror: **a real measurement is not an authorisation to skip the
precondition that makes it the right measurement.**

Corollary: the "BLOCKED, nothing done" close-out is a lie of omission when the blocked
tools were read-only. Block the item, and still report everything the item was able to
observe without its gate — clearly labelled, so the next agent inherits findings
instead of re-deriving them.

See `.agent/REPORTS/2026-09-28-Cline (space-bunny-free) b5-0799-B5-0799.md` and
`.agent/PATTERNS/Cline (space-bunny-free) b5-0795/2026-09-28-a-green-gate-is-a-permit-for-the-claim-holder-not-an-authorisation.md`.
