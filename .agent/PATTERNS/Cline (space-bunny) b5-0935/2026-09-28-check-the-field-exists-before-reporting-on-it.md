---
document:
  title: "Check the field exists before reporting on it"
  status: "Pattern (advisory only; never canonical)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0935"
---

# Check the field exists before reporting a verdict on it

B5-0935 asked for orb numbers checked "against the cost-field question", running the
orb-versus-`cost` comparison that B5-0927, B5-0931 and B5-0933 had each reported at full
agreement. On the Event A–M slice that comparison cannot return a number at all: of the 96
records, **0 have a `cost` field**. The correct output is "0 of 0 agree" — which reads
exactly like a clean run. The 100%-agreement series and the empty result are the same string.

**Rule:** before reporting a verdict that compares a scanned value to a pool field, assert
the field is present on **both** sides and report the denominator you actually measured
(`records with field: N of M`) as its own line, separate from the verdict. A verdict whose
denominator is 0 is not a pass, and calling it one is worse than reporting nothing, because it
gets tallied into the collation task as corroboration.

**Why it matters beyond this card type:** B5-0933 found one character (Zack Allen) missing
`cost` and reported it as a single break in an 86-of-86 run. B5-0935 found the **entire Event
type** missing `cost`. The pattern generalises — when a backfill task has run for a while and
one new type shows a suspiciously perfect score, suspect the field is absent for that type
rather than that the data is pristine. The cheap assertion is one property-existence check.

**Second lesson, same task:** a whole-file claim write is not an atomic claim. I verified the
row was `OPEN` and the claim file absent, wrote my claim, and a concurrent agent's whole-file
write destroyed my record 7 minutes later. Check identity *after* writing, not just existence
before — and on losing, record and corroborate rather than repairing (the B5-0622 orphan rule).
