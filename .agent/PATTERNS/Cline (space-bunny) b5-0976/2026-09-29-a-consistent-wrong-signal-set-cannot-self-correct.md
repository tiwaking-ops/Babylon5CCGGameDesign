---
document:
  title: "A consistent wrong signal set cannot self-correct — only an external clock can"
  status: "Pattern (advisory; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0976", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0976", version: "space-bunny"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
---

# A consistent wrong signal set cannot self-correct

**Rule.** Validate every liveness timestamp against an **external** clock, never
against the other members of its own signal set. A claim, its owner's heartbeat
and a report written in the same session share one mistaken instant, so they
agree with each other and are all wrong; internal consistency is a check that
cannot fail.

**Why.** B5-0953's claim carries `started_utc 2026-09-29T07:04:00Z` and its
owner `solar-pro4`'s heartbeat carries `"utc": "2026-09-29T07:04:00Z"` — the
*same* future instant, against a real clock of `2026-09-28T19:57Z`. Because age
is `now - started_utc`, the age is negative, and negative compares *younger* than
any TTL, so the lock could not age out for ~11 hours. Every consistency check
passed. Only comparing against the wall clock exposed it. This is B5-0597
failure-3 (a liveness signal wrong in the dangerous direction) reached through a
*wrong* signal rather than an absent one.

**How to apply.**
1. Compare every `started_utc` / `utc` to the wall clock at read time; flag
   future-dated values explicitly. Absent-signal handling (`UNKNOWN`) and
   future-date handling are **different defects** needing different fixes.
2. A negative age is not a small number to be tolerated — it is an unbounded
   lock, and the fix is a human decision, never an agent guessing a reap.
3. Expect the shipped tools to **disagree in direction** about such a claim
   (`run-queue.ps1` refuses to offer, `ledger-query.ps1` reports `LIVE` +
   suppressed). Quote both; do not pick a winner silently.
4. **The reporter must hold itself to the rule it reports.** Guessing
   `started_utc` reproduces the defect at small scale — I wrote a claim at
   `19:58:12Z` against a `19:57:12Z` clock and the detector printed `-1.1` for my
   own row, the same failure the row was filed about.

**Corollary.** "All signals agree" is not evidence of liveness; it is evidence
only that one writer's clock was used repeatedly. The single-source assumption
is invisible precisely because it is shared.

Related: `.agent/HEARTBEATS/README.md` § *Liveness: three signals, never one*;
`.agent/CLAIMS/README.md`; `00_BOOT.md` step 6; report
`.agent/REPORTS/2026-09-29-Cline (space-bunny) b5-0976-B5-0976.md`.
