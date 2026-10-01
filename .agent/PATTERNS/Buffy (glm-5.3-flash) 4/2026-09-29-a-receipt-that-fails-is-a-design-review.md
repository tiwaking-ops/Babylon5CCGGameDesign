---
document:
  title: "A receipt that fails is a design review"
  task: "B5-1004"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# A receipt that fails is a design review

**One-line lesson:** run the falsifying receipt against *every* design
iteration, not just the last one — two discarded designs taught more than the
kept one, and each was killed by a receipt, not by an argument.

## Shape of the case

B5-1004 asked the runner's lanes to divide the offer set. Three designs were
built and measured on the same fixture:

1. **Stateless PID-modulo-N** — collided because Windows PIDs are mostly
   multiples of 4, so `% N` converges far more than intuition suggests.
2. **Mutex + JSON read-modify-write** — a lost-update window survived, and
   worse, a *lock-timeout path degraded to stateless selection*: the fallback
   re-introduced the very collision the lock existed to prevent. A TTL-era
   intermediate also offered **zero** rows on the 1-lane control — failing the
   negative control while "passing" a distinctness count.
3. **Per-row atomic exclusive CREATE with PID-liveness bound** — no
   read-modify-write step means no window; a marker dies with its owner, so
   nothing suppresses across runs; `IOException` on create means "lost the
   race" (a normal outcome), not "infrastructure error" (which fails open).

The kept design is the one where the concurrency mechanism and the
correctness mechanism are the same primitive.

## What worked

- Reproduce the defect on a fixture first — the row demanded it, and the
  before/after delta is what makes the fix provable rather than plausible.
- Read a red receipt as a *diagnosis*, not a failure: the 2-of-3 result
  pinpointed the modulo collision; the 0-offer result pinpointed the
  suppress-forever marker TTL; the degraded-fallback result pinpointed the
  lock-timeout path.
- Keep the negative control sacred: a fix that offers nothing must fail, even
  though it trivially "offers distinct rows."
- Fail open on infrastructure trouble and fail closed on lost races — the two
  cases must take opposite branches, or the mechanism either stalls or
  collides.

## Related records

- B5-0984-style negative-control discipline; B5-0775's red-green fixture
  practice; B5-0953's "prove the fix did not make everything live" corrodible
  controls — this task is those lessons applied to a scheduler instead of a
  census.
