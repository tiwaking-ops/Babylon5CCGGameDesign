---
document:
  title: "Human decision brief — pending rulings consolidated (0422-A, mercenary data, contingency data)"
  status: "Proposal (advisory; decisions requested from the human overseer)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Human decision brief — three rulings consolidate the open queue

This brief consolidates the exact rulings the agents cannot make themselves.
Each item names the options, the working recommendation, and the file
pointers. Nothing here re-litigates the underlying reports; it only stacks
them in one place so one ruling session can unblock the queue.

## Ruling 1 — EASY pass bias: accept the retune, or accept the stall?

**The question.** EASY's designed pass bias (~53% observed, inside the
B5-0351 contract band 0.35–0.70) cascades into slow all-AI games and
harness "stall" labels. A retune would make EASY act more; not retuning
keeps today's behavior and the honest labeling.

**Options (from the 0422 proposal, docs/proposals/2026-09-25-solar-pro4-free-B5-0422.md):**

* **A — Retune the EASY pass-bias floor down** (0.35 → ~0.15–0.25). Cheapest
  design-level fix; widens the contract band; blurs the EASY/MEDIUM
  distinction slightly. Medium recommendation strength in the proposal.
* **B — Change harness/Main seat mix** — **DEAD as written** per the B5-0431
  reconciliation (no all-EASY default exists anywhere: Main is
  human/MEDIUM/HARD/EASY; all three full-game harnesses are
  EASY/MEDIUM/HARD/MEDIUM, census-verified with file-and-line evidence).
* **C — Un-pass incentives on zero-cost actions** — requires a B5-0351
  contract rewrite (spread and band checks would fail); not recommended.
* **D — MEDIUM/HARD termination caps** — wrong target; the cascade is
  EASY-pass-driven, not MEDIUM/HARD-overaction-driven.

**Working recommendation.** Accept the stall for now and record it as
natural termination (the B5-0444 runner fix already labels terminator =
WINNER/ROUND_CAP/TIMEOUT honestly, and natural termination was observed at
round 11 / ~142s). If the human wants faster EASY games, rule for **A** with
a floor of 0.20 and re-run the B5-0447 balance probe as acceptance evidence.
Either ruling unblocks: nothing else in the queue is gated on this, but the
0422 thread stays open until one is made.

**Files:** docs/proposals/2026-09-25-solar-pro4-free-B5-0422.md;
.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0431.md (option B
fatality); .agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0444.md
(runner honesty fix); docs/proposals/b5-0422-pass-bias-cascade-design-proposal.md
(parallel advisory proposal).

## Ruling 2 — Mercenary cards: engine exists, data does not

**The finding.** B5-0386 returned NO EVIDENCE: the literal string
"mercenary" occurs 0 times in premiere.json and deluxe.json. The mercenary
engine surface exists and is data-gated (B5-0395 bidding + B5-0403 AI
bidding + B5-0404 bid UI), so it stays inert until real mercenary records
exist.

**Options for the human:**

* **2a — Import mercenary card data** from a licensed source set (requires
  the usual IP-safe sourcing discipline; SNRPG decode tables from B5-0334
  can locate candidates in the Great War-era material).
* **2b — Author original mercenary cards** (IP-safe, invented stats/texts)
  as a data task.
* **2c — Leave data-gated** and stop seeding follow-ups on the mercenary
  mechanic until a source set is approved.

**Working recommendation.** 2c for now: both Ruling-3 evidence (below) and
the B5-0386/B5-0418 reports agree the pool is Premiere + Deluxe only and
the mechanic's data belongs to Great War. Importing or authoring cards is a
content decision with IP implications, which is exactly the human gate.

**Files:** .agent/REPORTS/2026-09-23-freebuff-03-B5-0386.md (0-occurrence
census); .agent/REPORTS/2026-09-23-mimocode-0.1.15-B5-0334.md (SNRPG decode
tables — where mercenary candidates would live in the source material).

## Ruling 3 — Contingency cards: engine fully live, data absent by set coverage

**The finding.** B5-0418 returned NO EVIDENCE: zero contingency cards in
either pool; contingencies belong to Great War, which has never been
imported. Unlike mercenaries, the engine side is already fully implemented
(B5-0365/0377/0381) — the mechanic is live the moment real data arrives,
and no backfill can manufacture records the source sets do not contain
(B5-0424 already closed as the row-mandated no-op).

**Options for the human:**

* **3a — Import Great War set data** (same sourcing discipline as 2a; would
  also likely answer Ruling 2 in the same pass, since mercenary candidates
  sit in the same expansion-era material).
* **3b — Author original contingency cards** (Premiere/Deluxe-compatible).
* **3c — Leave as is** — engine waiting on data is a stable, honest state.

**Working recommendation.** 3c, or 3a as a single combined "Great War
import" ruling that resolves both 2 and 3 together. Note the SNRPG decode
tables (B5-0334) mark several Set columns unconfirmed, so any import needs
a decode-verification pass first.

**Files:** .agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0418.md (and
the parallel .agent/REPORTS/2026-09-25-solar-pro4-free-B5-0418.md);
.agent/REPORTS/2026-09-25-solar-pro4-free-B5-0424.md (mandated no-op close).

## What each ruling unblocks

| Ruling | If approved | If declined |
|---|---|---|
| 1 (EASY retune) | retune task + B5-0351 band edit + re-probe | stall accepted; B5-0447 reports against today's baseline |
| 2 (mercenary data) | source-verification + data-import task | mechanic stays data-gated; no further mercenary seeds |
| 3 (contingency data) | Great War import track (covers 2) | engine waits; no further contingency seeds |

No code in the current queue is hard-blocked on any of the three; they are
recorded here so the next governance pass can close their threads with one
ruling each.
