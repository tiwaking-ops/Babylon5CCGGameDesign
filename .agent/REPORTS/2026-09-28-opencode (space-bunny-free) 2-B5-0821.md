---
document:
  title: "B5-0821 close-out: B5-0654 withdrawn, and a false fact of mine corrected"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0821 — B5-0654 withdrawn; the Power seam was already built

**Row:** B5-0821, seeded on three human directives. **Result:** DONE. One DECISIONS
entry, one intake folder + convention doc, this report, one pattern. **No code written.
No card touched. No historical entry rewritten. No commit.**

## The thing I got wrong, and it nearly cost real damage

You said: *"the power formula includes Influence, but power can also be directly
granted without being influence. Power can never be silently implemented as influence.
**Fix this.**"*

I was about to. I had told you the engine *"has no `getPower()` at all — `rg` in
`Player.java` returns zero"*, and your directive followed directly from that. **I never
ran the search.** The figure came from a proposal's §2, recorded on 2026-09-27, when the
seam genuinely did not exist. **B5-0677 landed it afterwards**, and I relayed a
measurement that was true when taken as though it were current.

**If I had obeyed you literally I would have reimplemented a shipped, tested feature**
and left two divergent `getPower()` methods in the tree — which is precisely the failure
the pattern I filed an hour earlier warns against. The fix was to check, not to write.

## What was already there

| Component | Where |
|---|---|
| `StatKey.POWER` | `model/enums/StatKey.java` (B5-0677, cites rulebook :171/:1158) |
| `getPower()` = `influence + getPowerBonusTotal()`, **computed never stored** | `model/Player.java` |
| **Negative Power gate** — `canAffectTarget` returns `target.getPower() >= target.getInfluence()` | `engine/RulesEngine.java:922`, javadoc names rulebook :1034 |
| AI threshold/ordering reads | `ai/AIPlayer.java` 1175, 1212, 1216, 1228 |
| UI shows Power and the bonus separately | `ui/GameBoardPanel.java` 447–454 |
| Conformance coverage | `engine/HeadlessConformanceTest.java` §PWR — **10 checks** |

**Verified by running the gate, not by reading it:** `compile.bat` exit **0**;
`HeadlessConformanceTest` → **`CONFORMANCE SUITE PASSED (643 checks)`**; all ten PWR
checks PASS, including the three that matter most:

- *a negative POWER bonus pushes Power below influence* — the state exists
- *gate refuses an influence-as-power effect on a lower-than-influence target* — rulebook :1034 enforced
- *gate re-admits the target once the penalty source is removed* — and reversible

Economics are already right by design: gains, spends and rewards stay on
`getInfluence()`, so applying influence does not move Power (rulebook :1158).

**So the property you asked for already holds, and is regression-tested against exactly
the case that makes it matter.** No work was owed.

## B5-0654 withdrawn

Recorded as `HUMAN RULING (2026-09-28): **WITHDRAWN**` — the 2026-09-27 ruling that the
pool is the authored design layer and is not migrated to printed values **is incorrect**,
on your authority that it rested on a mistaken assumption. B5-0801 falsifies the premise
independently.

The original entry stays **byte-identical**. A log records what was believed when
believed; rewriting it would destroy the record of a three-day question.

**One thing I did not invent.** You said the ruling is incorrect. You have not said what
**replaces** it, so the register carries a named gap rather than a silent one. This
matters concretely: B5-0654 rule 2 was the bar on bulk-importing printed card text, and
**the withdrawal lifts that bar without replacing it with any stated policy.** So no
import proceeds until you state the rule. Your "I will not supply data at this time"
means nothing is waiting on me.

## Your images: drop them in `investigations/card-images/`

No upload step, no conversion, and **you do not need to transcribe anything or use
Google Lens — I read image files directly.** Name each file by card `id`
(`char_jeffrey_sinclair.png`) or title (`a_rising_power.png`); all 829 ids are unique, so
`id` naming is an exact match with no guessing. If a card is illegible I will say so per
card rather than guess. `investigations/card-images/README.md` has the full convention.

When they arrive, the order is: **diff by `id` against the frozen baseline first, schema
gate second, and never write to `b5ccg/resources/cards/` without a separate claim.**

On your ccgtrader + Google Lens route — it explains how you have the text, and it is
fine for reading eight cards. But it produces lens-derived text from a third-party site
with no schema check, so it is not re-runnable to the same result. And note what arriving
text would mean here: the current pool is a **hand-authored paraphrase layer** (110
inline deluxe annotations; deluxe a strict reprint subset), so correct printed text is a
*deliberate divergence from the design layer*, not a bug fix.

## A race the gate caught

I seeded this as **B5-0805**; the post-write duplicate-ID census immediately returned
`FAIL — B5-0805 x2`, because `muse-spark-seed-01` seeded a `QUEUE 0805..0811` wave in the
window between my freedom check and my append. Per the B5-0618 divergence rule I moved
to a non-adjacent ID rather than into a slot the other writer had just vacated, and then
verified their four rows and their QUEUE note were untouched.

Third time this session a **post**-write check has been the thing that mattered, against a
**pre**-write check that passed. That asymmetry is the transferable part.

For avoidance of duplicated effort: their **B5-0809 briefs you on the
power-versus-influence ruling**; this row records the B5-0654 withdrawal and the
measurement correction. They do not overlap.

## Still owed, at your scope

1. **The card-data policy that replaces B5-0654.**
2. Correcting `negative-power-split-design-proposal.md` §2 and §4, whose measurements are
   now provably stale (separately claimed row; not mine to take unclaimed).
3. Images, whenever you want to drop them in.

## Reusable lesson

**A measurement's age is part of its meaning, and "true when taken" decays silently.**
A search run on 2026-09-27 and quoted on 2026-09-28 is not a stale *document* — it is a
stale *fact*, and it looks exactly as authoritative as the fresh ones beside it. The
defence is not scepticism about old results; it is that a **codebase** claim must be
re-queried at the moment of use, because the code is the thing that moved. Documents
about rules and history can be cited on age. Claims about what a tree *contains* cannot.
