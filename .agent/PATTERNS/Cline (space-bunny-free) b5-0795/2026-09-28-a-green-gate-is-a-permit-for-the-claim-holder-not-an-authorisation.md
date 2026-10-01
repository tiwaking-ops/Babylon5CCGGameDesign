---
document:
  title: "A green prerequisite gate is a permit for the claim holder, not an authorisation for the auditor"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A green gate is a permit, not an authorisation

**Reusable lesson (filed from B5-0795, checkpoint-gate readiness audit):** a green
prerequisite gate is a permit for the claim holder, not an authorisation for the
auditor — a gate anyone may act on stops being a gate the moment a second agent
can also read it green.

B5-0795 exists to answer "are B5-0699 and B5-0737 ready to commit?". Both gates
read **green** — 3/3 and 4/4 prerequisites `DONE` — and both rows are `OPEN`
with live claims on disk. The tempting move is to run the commit the gates
permit. The claim is what makes the commit legal, not the gate: the gate says
*when* a commit is safe, the claim says *whose* commit it is, and only one of
those two is observable to a bystander.

Two checks made the temptation concrete and are worth repeating:

* **Read status cells mechanically, not from prose.** Splitting each row on `|`
  and reading field 2 gave `DONE` for all seven prerequisites. A close-out note
  elsewhere saying "B5-0747 was OPEN at claim time" reads like a red gate until
  you check that B5-0747 is `DONE` *now* — a note records a snapshot, not a
  standing question.
* **An implausible `started_utc` is not a licence to reap.** Both checkpoint
  claims carry timestamps hours in the future (shell-clock skew), and the
  three-signal verdict for both is still LIVE because a fresh owner heartbeat
  exists. Reading "future timestamp" as "abandoned" would have freed two rows to
  act on without their owners — a worse outcome than the staleness it fixes.

Corollary for auditors: report the green gate, name the holder, and stop. The
deliverable of a readiness audit is a *verdict plus an attribution*, never an
action on the audited row.

See `.agent/REPORTS/2026-09-28-Cline (space-bunny-free) b5-0795-B5-0795.md`.
