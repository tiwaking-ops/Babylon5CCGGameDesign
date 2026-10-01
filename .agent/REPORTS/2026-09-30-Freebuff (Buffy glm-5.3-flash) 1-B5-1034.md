---
document:
  title: "B5-1034 — is the seed wave policy or accident: measured throughput, prose cost, one recommendation"
  status: "DONE 2026-09-30"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  claimed_at: "2026-09-30T05:22:12Z"
  instruments: "python re over explicit-UTF-8 bytes of .agent/TASK_LEDGER.md (549 rows, 1,387,647 B); creation via QUEUE wave notes, closure via first 2026-09-NN date in each DONE row's Verified cell"
---

# B5-1034 — should the queue keep growing? Measured, then one recommendation

## Part 1 — what a row costs (measured)

| Cell | mean | median | max |
|---|---|---|---|
| Task | 763 chars | 478 | 4,396 |
| Verified | 1,108 chars | 834 | 5,697 |

The two prose columns together hold **1,027,173 bytes = 74.0% of the
ledger**. A single average close-out writes ~1,871 chars of ledger prose
*in addition to* the report the row already requires — the row is a second,
competing document, and it is the expensive one.

## Part 2 — created vs closed per day (both instruments named)

Closures per day (first date in each DONE row's Verified cell): 09-21: 39,
09-22: 5, 09-23: 36, 09-24: 16, 09-25: 61, 09-26: 127, 09-27: 67, 09-28:
96, 09-29: 39, 09-30 (partial): 8; 17 early rows carry no parseable date
(an instrument limitation, disclosed).

Creation per day (QUEUE wave notes, ids 0336+): 09-22: 25, 09-23: 27,
09-25: 57, 09-26: 107, 09-27: 85, 09-28: 112, 09-29: 23 (the 09-21 body
holds 44 pre-wave rows; 09-30 creation is individual seeds, not wave
notes).

**Ratio created/closed:** 5.0, 0.75, 0.93, 0.84, 1.27, 1.17, 0.59 — noisy
around 1, not the monotonic growth B5-1015's snapshot implied. **The queue
is approximately at equilibrium: it is neither exploding nor draining
decisively.** 549 rows total: 511 DONE, 18 BLOCKED, 7 SUPERSEDED, 5 VOID,
8 OPEN. The thing that *has* grown monotonically is row length, not row
count — the cost crisis is in the writing, not the arithmetic.

## Part 3 — the ONE structural recommendation: the split, formalised

**Recommendation: short queue row + long report, as a rule with numbers**
(Task cell ≤ ~600 chars: problem + acceptance criteria only; Verified cell
≤ ~400 chars: verdict + one-line evidence pointer; everything else lives in
the report the row must file anyway).

Why not the alternatives, on the page as the row demands:

- **Length cap alone** fails because it fights the actual culture: the
  verified-cell receipt *is* the provenance culture, so a cap gets gamed
  into pointers anyway — which is just the split, arrived at informally.
- **Archive threshold** is the strongest lever and the one the row rightly
  flags as most dangerous: the pass counts, the B5-0995 erratum precedent,
  and every grep-based audit (B5-1030 ran on exactly this file) assume rows
  stay. At 1.39 MB the ledger is still fully greppable in under two
  seconds; the measured pain is the ~1,900 chars per close-out, not the
  byte total. Archive it later, if ever, and only with a human ruling.
- **The split** costs one thing, stated plainly: rows stop being
  self-contained — an agent must open the cited report for the full
  picture. It buys: ~74% prose trending toward ~35%, cheaper close-outs
  (the incentive the row says the queue depends on), and a boot that reads
  rows as an index instead of a corpus.

**Whether seeding continues at this rate is a human ruling, and this row
leaves it there.** The measured fact for the ruler: seeding ≈ closing
(ratio 0.6–1.3 across the week), so the queue is not manufacturing backlog
— but each seed now costs the queue ~1,900 chars of prose forever, and the
B5-1000→B5-1066 wave demonstrated a seed wave consumed at ~4 rows/hour
also duplicates work across agents (B5-1062's collision). If the ruling is
"keep seeding", the split rule is the cheap accommodation; if "slow down",
the split still stands on its own numbers.

**Reusable lesson:** measure the ratio before diagnosing the queue — the
"queue is growing faster than it closes" premise was a one-day snapshot;
the week-long instrument says equilibrium, and the real growth was in the
prose.
