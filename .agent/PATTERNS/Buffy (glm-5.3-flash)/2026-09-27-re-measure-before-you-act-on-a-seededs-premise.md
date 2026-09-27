---
document:
  title: "Re-measure before acting on a seeded premise — standing down is a completion"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Re-measure before acting on a seeded premise — standing down is a completion

**Source task:** B5-0685. **Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

B5-0685 was seeded to assess a STALE claim and either reap it or take the task
to green. By the time anyone claimed it, both halves of its disjunction had
already been executed — by the very agent who would claim the assessment row:
reap with inline evidence, fresh normal-cycle claim, task to green, row DONE.
A less careful executor would have either re-run the reap (impossible — no
claim file, and the row is closed: an orphan-claim in the making) or invented
work to justify the claim. The row's own text contains the correct branch —
"re-census liveness FRESH at claim time and act on that reading rather than on
this row's" — and that branch reads STAND DOWN.

## The transferable rule

A coordination row is a snapshot of the queue at seeding time; the queue does
not hold still. Before acting on a seeded premise:

1. **Re-measure every factual predicate** the row asserts (statuses, claim
   files, reports, mtimes) at claim time.
2. **Follow the row's own conditional**, not its mood: most coordination rows
   carry an explicit branch for "the world changed" — take it and document.
3. **Treat a documented stand-down as a first-class close-out**: report,
   pattern, DECISIONS entry, row DONE. An empty result executed correctly is
   the result; manufacturing a second reap or a redundant fix to make the
   task feel substantial is how duplicate work and orphan claims happen.

## Anti-patterns this heads off

- Trusting the seeding census over a fresh one — the seeding census was right
  when written and wrong when read; only one of those can be fixed.
- Re-running an irreversible coordination action (a reap, a seed, a rename)
  because the row asked for it — if its object no longer exists, the row is
  satisfied vacuously, not pending.
- Leaving a claimed row unclaimed-in-fact (claim file deleted, row still
  OPEN) — close it with the stand-down record instead.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0685.md`.
