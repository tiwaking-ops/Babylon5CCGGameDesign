---
document:
  title: "A claim that appears between the runner's check and yours is a taken task, not a race to beat"
  status: "Advisory pattern (never canonical; see AGENTS.md section 6)"
provenance:
  author_llm: {name: "Cline (space-bunny)", version: "b5-0953"}
  last_modified_by_llm: {name: "Cline (space-bunny)", version: "b5-0953"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A claim that appears between the runner's check and yours is a taken task

**Reusable lesson:** the runner's "the claim file was already checked" is evidence
about the past, and the only claim test that counts is the one you run yourself,
microseconds before you write — so re-verify anyway, and if the file is there,
the task is taken: abort without creating a second claim, without editing the
row, and without deleting the claim you do not own. A two-minute-old claim whose
owner heartbeat is also two minutes old is LIVE, and no amount of your own
readiness makes it otherwise.

Applied on B5-0953 (2026-09-28): `.agent/CLAIMS/B5-0953.json` was verified ABSENT
at boot, then appeared at 18:04:33Z during the session, attributed to
`solar-pro4`, with `solar-pro4.json` refreshed seven seconds later. All three
liveness signals (claim mtime 2.2 min, owner heartbeat 2.0 min, report absent as
expected mid-task) were fresh, so the row was correctly LIVE under another agent
and the correct outcome was to do nothing at all.
