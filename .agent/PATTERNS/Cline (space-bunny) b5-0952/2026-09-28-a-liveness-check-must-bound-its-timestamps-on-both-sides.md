---
document:
  title: "A liveness check must bound its timestamps on both sides"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0952", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0952", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Bound a timestamp on BOTH sides, and justify the tolerance from topology

**Reusable lesson (B5-0952):** a liveness check must bound its timestamps on BOTH
sides, and the asymmetry is the whole point. A past-bad timestamp ages out on its own
and merely hides a claim. A future-bad timestamp produces a **negative** age, and
negative compares as younger than *every* TTL — so it reads live indefinitely and
cannot age out. Any "is this fresh?" test that subtracts two timestamps needs an
explicit future bound, and the tolerance must be justified from the write/read
**topology** (same host ⇒ NTP drift only), not from the widest timezone offset you can
think of: a tolerance wide enough to absorb every real timezone also absorbs the very
failure you are fixing.

Three corollaries worth carrying:

* **Set the tolerance from where the value is born, not from the worst case you can
  imagine.** Written and read on one host, the only absorbable skew is write-to-read
  drift plus a resync — measured, not guessed. Everything larger is a data-entry defect
  and should be refused loudly.
* **One guard per direction beats one guard for both.** Past-tense rejection and
  future-terse rejection are separate tests with separate error costs; a combined guard
  has to reason about cases it was never written to exercise.
* **The identical-looking branch in a neighbouring function may be correct.** In
  `run-queue.ps1`, `Get-CensusSuppression` branches on the same `$started -gt $now`,
  but suppression is the fail-safe direction there (it makes a row *not reportable*),
  so it needed no bound. Leave it alone and **write down why**, or the next reader
  will "fix" it.

Related, from the same pass: **a check that never fired proves nothing.** The
verification carried an explicit anti-vacuity assertion (the guard *did* fire on a
+12h synthetic claim) alongside the six "stayed quiet" assertions, because a guard
that silently never triggers passes every quiet case and reports green forever.

**Reusable lesson:** before trusting a passing test, confirm the check was *able* to
fail — and when one does fail, establish which side is wrong before changing anything,
or you will ship a regression to make a broken test pass.
