---
document:
  title: "B5-0719 — run-queue vs ledger-query suppression divergence"
  status: "Report"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0719 — suppression divergence assessment

**Author:** Cline (space-bunny-free) · **Date:** 2026-09-27 · **Status:** DONE

**Assessment only. No tool was edited.**

## Headline

The divergence reproduces — and its root cause is **not** a judgement drift
between two reasonable readings. It is a **dead third signal** in two of the
three tools: `run-queue.ps1` and `census-crosscheck.ps1` resolve the reports
directory to a path that **does not exist**, so the report signal in the
three-signal rule never fires. `ledger-query.ps1` alone reads the right path.

## Reproduction (identical synthetic fixture)

Harness `.agent/tmp-0719-harness.ps1` builds an isolated `.agent` tree under
TEMP — its own ledger, CLAIMS, HEARTBEATS, REPORTS — copies both shipped tools
in, and compares their `Get-CensusSuppression` verdicts on the **same** two
claims:

| Claim | Owner heartbeat | Claim age | TTL |
|---|---|---|---|
| `B5-9001` | **absent** | 40 min | 30 |
| `B5-9002` | present, mtime back-dated 40 min | 40 min | 30 |

```
TASK         run-queue                ledger-query             AGREE?
B5-9001      reportable               suppressed-live-claim    NO  <-- DIVERGENCE
B5-9002      reportable               suppressed-live-claim    NO  <-- DIVERGENCE

DIVERGENCE REPRODUCED: True
```

The synthetic tree is deleted at harness exit; the live `.agent` tree is never
written.

## Root cause — confirmed against the live tree, not inferred

The shipped code computes the reports directory as:

```powershell
Join-Path (Split-Path -Parent $AgentDir) 'REPORTS'   # from .agent -> <repo>/REPORTS
```

Reports actually live at `<repo>/.agent/REPORTS`. Measured live:

| Path | Exists |
|---|---|
| `…\Babylon5CCGGameDesign\REPORTS` (as constructed) | **False** |
| `…\Babylon5CCGGameDesign\.agent\REPORTS` (actual) | **True** |

The guard is `if (Test-Path -LiteralPath $reportsDir)`, so a wrong path does
**not warn or fail** — it silently skips the entire signal.

**Three sites affected:**

| Site | Function | Consequence |
|---|---|---|
| `run-queue.ps1:139` | `Get-CensusSuppression` | census suppression misses report signal |
| `run-queue.ps1:363` | `Test-LiveClaim` | **the OFFER decision** misses report signal |
| `census-crosscheck.ps1:167` | `Get-CensusSuppression` | the agreement prover is itself blind |
| `ledger-query.ps1:239` | — | correct (`.agent/REPORTS`) |

## Which tool matches the rule

`ledger-query.ps1` matches `.agent/HEARTBEATS/README.md`:

```
live(T) <=> max( mtime(CLAIMS/T.json), utc(HEARTBEATS/<owner>.json), mtime(REPORTS/T-*.md) ) within TTL
```

`run-queue.ps1` and `census-crosscheck.ps1` drift — and the drift is a **missing
input**, not a differing judgement.

The direction matters:

- For **suppression** the dead signal errs *safe* (a row can only become more
  reportable).
- For the **offer decision** at `Test-LiveClaim:363` it errs *unsafe*: a task
  whose claim and heartbeat have both gone quiet but whose **report is fresh**
  gets offered away from the agent that just closed it out.

That is the **B5-0597 "reaper destroyed live work" shape reached by a different
route** — not a wrong TTL, but an input that never arrives.

## A second, smaller divergence (reported separately)

With the reports path held aside, the tools still differ on a **missing owner
heartbeat**:

- `ledger-query.ps1:226` — absent heartbeat ⇒ `not provably stale` ⇒ suppressed.
- `run-queue.ps1:135-138` — no such branch; skips the missing heartbeat and lets
  claim age decide.

This one is arguably deliberate (`Test-LiveClaim:379-385` warns that a
heartbeat-less claim must not block its task forever). But the census and the
offer decision should not silently disagree about the same evidence. Recorded as
a **second, smaller item**, not folded into the path finding.

## Harness artefact worth recording

The first harness run reported *both* tools diverging — a meaningless result.
Cause: **the tools read the heartbeat's file mtime, not its `utc` field.** A
fixture that writes a 40-minute-old `utc` without back-dating the file mtime is
read as age 0, silently testing the wrong case. The harness now back-dates mtime
explicitly. This is a trap for any future liveness fixture.

## Smallest-set convergence slice — PROPOSED, NOT APPLIED

**Three one-line path corrections, nothing else:**

- `run-queue.ps1:139` → resolve `.agent/REPORTS`
- `run-queue.ps1:363` → resolve `.agent/REPORTS`
- `census-crosscheck.ps1:167` → resolve `.agent/REPORTS`

matching `ledger-query.ps1:239`. A bug fix with a red-to-green test already
demonstrated by this harness; no structural change, no new helper, no shared
library.

**The absent-heartbeat difference is deliberately EXCLUDED** and left as a
separate decision — it is a judgement about offer-decision safety, not a path
typo, and bundling them would make a mechanical fix carry a policy change.

The shared-library leg of `tool-rule-convergence-proposal.md` §6 remains the
durable answer to the duplication, but it is much larger and is **not needed** to
fix the dead signal.

## Scope held

`run-queue.ps1`, `ledger-query.ps1` and `census-crosscheck.ps1` were **read and
copied into TEMP only**. No live claim holds any of the three. No
`b5ccg/src` or `b5ccg/resources` byte touched. No commit.

## Reusable lesson

**Check what your fixture actually exercises** — a harness can reproduce a
symptom perfectly while testing the wrong case; when the first result is
nonsensical, suspect the fixture before the code.
