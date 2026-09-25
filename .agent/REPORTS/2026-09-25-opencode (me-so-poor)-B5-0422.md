---
document:
  title: "B5-0422 collision observation — dual-attempt record"
  status: "Report"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  last_modified_date: "2026-09-25"
---

# B5-0422 — pass-bias cascade proposal: concurrent-session collision

**Status:** OBSERVATION ONLY — B5-0422 was completed by another session.
**Agent:** opencode (me-so-poor) / big-pickle

> **This report does not update the B5-0422 ledger row, DECISIONS, or the
> canonical proposal.** The row is DONE by solar-pro4:free at
> 2026-09-25T05:51:00Z–05:52:00Z UTC (delivery:
> `docs/proposals/2026-09-25-solar-pro4-free-B5-0422.md`). One-writer-per-scope
> and never-touch-another-agent's-close-out both apply. This is a collision
> record plus an independent cross-check of the delivered analysis.

## 1. Sequence of events (UTC)

| When | Who | What |
|---|---|---|
| ~05:52:38Z | me | Claimed B5-0419 DONE was already released; CLAIMS dir contained only `B5-0419.json` (mine, pre-release). |
| ~05:52:48Z | me | Ledger grep showed B5-0422 still `| OPEN |` with `-` claims. |
| 05:53:30Z | me | Wrote `.agent/CLAIMS/B5-0422.json` (stale read — the row had just flipped or flipped within the next seconds). |
| 05:51–05:52Z | solar-pro4:free | Worked + delivered B5-0422 proposal; ledger row flipped to DONE. |
| ~05:56Z | (external) | My `.agent/CLAIMS/B5-0422.json` was deleted by another session before my close-out (same externally-deleted-claim pattern Buffy logged for B5-0418). I never reaped anyone — my own claim disappeared. |
| ~05:57Z | me | Observed the ledger row already DONE (solar-pro4:free) mid-work; stopped the item and released intent. |

Conclusion: a close-out write landed in the ~50-second gap between my OPEN
grep and my claim write. My claim was on an already-DONE task. The DONE row is
authoritative; my claim was an artifact of the race.

## 2. My work product in the race

I authored a second, independent proposal during the overlap:

`docs/proposals/b5-0422-pass-bias-cascade-design-proposal.md`
(author_llm: opencode (me-so-poor) / big-pickle).

It is a **parallel candidate**, not a superseding one, and not merged into the
row's DONE claim. It stands only as sourced analysis for whoever integrates
B5-0422 (see §4).

## 3. Independent cross-check — where the delivered analysis and mine diverge

Tree truth (read from source, 2026-09-25): the default seat mixes are NOT
all-EASY. `HeadlessMultiRoundTest.java:71`, `HeadlessSmokeTest.java:41`,
`HeadlessReportingTiebreakTest.java:438` all use `{EASY, MEDIUM, HARD,
MEDIUM}`; `Main.java:70-72` uses Delenn MEDIUM / G'Kar HARD / Londo EASY.

1. **Option B premise (divergent).** The delivered proposal argues the default
   is all-EASY and recommends B (seat-mix change) as the cheapest first step.
   The tree already runs 3/4 non-EASY seats in every harness and 2/3 in Main.
   An all-EASY baseline is not the status quo, so "B first" does not change
   today's default behavior — it would either remove the single EASY coverage
   slot (information loss) or change nothing material. My analysis instead
   treats B as a metrics-label change with a real cost (harness stops sampling
   EASY for balance reporting) and recommends it last, and only for
   `HeadlessMultiRoundTest` + `HeadlessSmokeTest`, never the B5-0350 tiebreak
   fixture (whose EASY slot is a reporting-position fixture).
2. **Option A contract approach (divergent).** The delivered proposal re-pins
   the B5-0351 band (floor 0.35 → 0.15) so the retuned rate stays inside.
   Mine keeps the **existing** band fixed and retunes the blind-pass coin
   (30% → 10-15%): with the contract fixture's legal set, expected pass rate
   = `X + (1-X)/3`, giving 0.40-0.43 at X = 10-15% — inside [0.35, 0.70] with
   ≥ 2σ margin, so **no contract edit is needed at all**. A 0% blind pass
   would dip to 0.333 (below floor) — that is the failure boundary.
3. **Recommendation (divergent).** Delivered: B first, then A, C/D rejected.
   Mine: C1 (round-scoped repeat-pass suppression — the 30% roll fires once
   per seat per round, killing the ~9% back-to-back blind-pass pairs) + D1
   (game-level liveness governor) first, then A-lite (30% → 15%) as its own
   ai/ task, then B1 as a docs/harness task. Note that **both** of us reject
   Option D-as-cap (the B5-0409 verdict explicitly marked a per-player cap
   STALE) and both flag that the "stall" label is a 60 s-window artifact of a
   natural ~118 s termination.

Where we agree: the B5-0409 verdict stands (EASY pass-bias cascade inside the
band; not a cap; not a seat gap); EASY bias is a deliberate design decision
(B5-0202c Finding 3) so no change may land as a stealth retune; every landing
needs its own claimed task with the standing gates (compile + RUN_TESTS=1 +
HeadlessAIDifficultyContractTest 10/10 + Java 6 grep).

## 4. Recommendation to the integrator

Treat `2026-09-25-solar-pro4-free-B5-0422.md` as the row's canonical delivery
(unchanged). Before implementing Option A, reconcile the band question with
§3.2 above (retune inside [0.35, 0.70] avoids a contract edit entirely); before
implementing Option B, re-read the actual seat arrays (§3.1) — the premise
"all-EASY default" does not match the tree. My candidate proposal is available
at `docs/proposals/b5-0422-pass-bias-cascade-design-proposal.md` and is
intended to be read as cross-check material, not as an additional authority.

## 5. Stop item

Per protocol ("if in doubt, log the ambiguity in the report and STOP that item
only"), B5-0422 is STOPPED for this session. No ledger/DECISIONS change was
made for it by this session. Unrelated work continues.