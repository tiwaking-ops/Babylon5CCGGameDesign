---
document:
  title: "Ledger row weight — adopt a Task-cell budget for new rows; archive threshold considered and rejected"
  status: "Proposal — candidate, never truth until merged and compiled"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 6", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1015"
---

# Proposal: one structural change — a Task-cell budget for NEW rows

Authored for B5-1015. The three-part measurement lives in
`.agent/REPORTS/2026-09-30-Buffy (glm-5.3-flash) 6-B5-1015.md`. Headline
numbers (measured 2026-09-30 ~05:10Z): 549 id-keyed rows, ledger 1.38 MB,
**Task cell mean 764 / median 479 / p90 1,879 / max 4,397 chars** (419,512
chars total, 30.3% of the file); Verified cells are already cheap (median 21,
only 9 rows exceed 500 chars); the Task+Verified prose is ~32.6% of the
ledger.

## The recommendation

**Adopt a Task-cell budget of ~800 characters for NEW rows** (seeded or
self-seeded), as a writing norm enforced by seeders and made *visible* — not a
validator that rejects rows:

1. The budget covers the id, the gate (if any), the scope, and the do-NOT
   boundaries. A task needing more prose than that belongs in a seeded
   companion report or the row cites an existing one (B5-1031/B5-1032/B5-1033
   are the existing proof that grounded one-line citations work: several
   current rows already lean on them).
2. `ledger-query` already prints per-row sizes (taskLen / noteLen on my
   reads). Add nothing to enforce; the census's existing columns make drift
   observable, which is how this proposal's baseline stays honest.
3. Existing rows are untouched — no retroactive truncation, ever (same
   no-retro-rewrite principle as the assessor compaction rule).

## The options, weighed on the page as the row demanded

**Archive threshold for closed rows — REJECTED, and the rejection is the
strong half of this proposal.** The ledger's rows are the repo's coordination
forensics: reap notes with three-signal evidence, adjudication notes that
later audits (B5-1030) verified against row text byte-for-byte, gate
provenance that later claims re-derived, and the assessor/verified history
the pass counts depend on. An archive threshold would move those out of the
grep space every tool and every boot reads — the boot cost drops once and the
cost reappears as a *re-derivation* cost on every forensic question after it.
The measured downside of keeping rows (≈1.4 MB, of which the Task prose is
420 KB) is bounded; the downside of un-greppable history is unbounded.

**Short queue row + long report split — ALREADY the de facto practice, so it
is not the one change.** The evidence: 644 close-out reports exist under
`.agent/REPORTS/`, 374 pattern records, and the median close-out Verified
cell is 21 chars. The fleet already puts the long half in reports. What it
does not yet do is bound the *seed* half — that is where the weight is.

## Why a budget rather than a cap

"Budget" states the target and the reason (boot cost is paid by every agent,
every session, forever); a hard cap invites the gaming this repo has already
seen once (rows retitled to dodge a matcher). The number is a norm for
new rows; the census's printed sizes are the audit trail; and if a future row
genuinely needs 2,000 characters, the norm says *put the argument in a report
and cite it*, not *shrink the thought*.

## What this proposal does NOT do

Archives nothing, deletes nothing, truncates nothing, adds no validator, no
exit code, no tool change. It is a convention proposal: adoption is a human
ruling or a maintainer edit to `AGENTS.md`, and until then it confers no
authority.
