---
document:
  title: "B5-0950 — repo verification and seeding pass, 12 rows"
  status: "Report"
provenance:
  author_llm: {name: "opencode (big-pickle-free) bp4", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free) bp4", version: "big-pickle-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0950 — verification and seeding pass (2026-09-28)

Agent: `opencode (big-pickle-free) bp4` (model `big-pickle-free`).
Human order: *verify repo. seed lots of tasks. list open tasks.*
Scope: ledger rows B5-0951..B5-0961, this row, one DECISIONS entry, this report,
one pattern, my own heartbeat. No `b5ccg/src` edit, no card-data edit, no commit, no push.

## 1. Verification, run first on the exact working tree

Boot ran to completion **before any write**, per `00_BOOT.md` step 4. Every seed
below is grounded in a measurement taken in this pass.

| Gate | Command | Result |
|---|---|---|
| Toolchain | `javac -version` | **1.8.0_292** — expected JDK 8 |
| Build | `b5ccg/compile.bat` | **exit 0**, "Build successful", 63 files at `-source 6 -target 6`, 1 expected bootstrap-classpath warning |
| Conformance | `java -cp b5ccg/out b5ccg.engine.HeadlessConformanceTest` | **exit 0**, `CONFORMANCE SUITE PASSED (643 checks)` |
| Smoke | `java -cp b5ccg/out b5ccg.engine.HeadlessSmokeTest` | **exit 0**, `SMOKE TEST PASSED`, round 1 in 16710 ms, 27 AI actions, 38 UI callbacks, 4/4 AI decisions legal |
| Dup-ID | `run-dup-census.ps1` | **exit 0**, PASS, 0 duplicate task IDs |
| Heartbeats | `validate-heartbeats.ps1` | **exit 1** — 56 of 57 conforming, 1 non-conforming, 0 identity collisions |
| Cross-check | `census-crosscheck.ps1` | **exit 1** — `DIVERGENT`, 2 disagreements across 445 rows |
| Queue | `run-queue.ps1 -DryRun` | lane offered, 2 claimable OPEN |

Both harness classes were invoked **directly**, not via a gate, because
`compile.bat` carries no `java` branch at all (the B5-0915 finding). A green
`compile.bat` on this platform attests compilation and nothing more, so
conformance and smoke were run by hand and their real output is quoted above
rather than their existence being assumed from the build.

**The queue was not drained.** The runner offered a lane with 2 claimable OPEN
rows, so this pass is additive backlog top-up, not the gap-fill the protocol
calls for, and the pick-and-work branch of `00_BOOT.md` step 5 applied.

## 2. Open-task census at seed time

`ledger-query.ps1 -Status "OPEN"` — 3 rows:

| ID | Claim | Liveness | Reportable? |
|---|---|---|---|
| B5-0699 | `me-so-poor` | STALE | yes |
| B5-0939 | `solar-pro4:free` | LIVE | **no — `suppressed-live-claim`** |
| B5-0947 | — | UNCLAIMED | yes |

Per the claims-first rule (B5-0657), B5-0939's structural reading is **not a
defect report**: its 7-pipe/doubleLead-no reading was taken while another agent
held a live claim on that row. It is a *transient* state belonging to a
different defect class than its committed form.

## 3. The queue moved during the pass — recorded, not smoothed

Between the seed-time census and the post-write gate, **two rows changed state
under me, both by other agents**:

* **B5-0699 → BLOCKED**, claim `Buffy (glm-5.3-flash)`.
* **B5-0947 → claimed** by `Cline (space-bunny) b5-0947`, which did not exist as
  a claim at seed time.

The QUEUE note records the census as **at seed time** and this report states the
drift, because a census quoted without its timestamp reads as current. The
post-write `OPEN` count is **13**, not 3: my 11 new rows, plus B5-0939 and
B5-0947 (B5-0699 having left the OPEN set).

Worth naming: B5-0947's owner resolves to **no heartbeat file**, so its liveness
reads `UNKNOWN` rather than LIVE. Under the three-signal rule that is *correct*
— an absent signal is UNKNOWN, never LIVE — but it means a brand-new claim from
a brand-new session renders as `suppressed-live-claim` on a row no tool can
positively confirm. That is the schema working as designed, not a defect, and it
is why B5-0953's fix is a behaviour change rather than a verdict change.

## 4. What was seeded — 11 rows, and why each is real

Every row names a file, a byte count or an exit code **measured in this pass**.
None is a generic backlog placeholder.

**Governance defects (3)**

* **B5-0951** — the one non-conforming heartbeat, `Cline (space-bunny) b5-0941.json`,
  carries `state: "released"`, outside the `active|idle|busy` enum. Scoped as a
  *decision* row, not a repair: the honest question is whether `released` is a
  real lifecycle state the schema is missing. Either branch forbids editing the
  foreign file.
* **B5-0952** — **the B5-0653 guard has no upper bound.** `run-queue.ps1` refuses
  a `started_utc` that is exactly midnight or at/before the epoch, but nothing
  refuses a timestamp in the **future**. The live claim `.agent/CLAIMS/B5-0939.json`
  reads `started_utc: 2026-09-28T22:05:00Z` against a wall clock of
  `2026-09-28T10:14Z` — roughly **twelve hours ahead**. `ledger-query.ps1`
  consequently prints its claim age as **−707.8 minutes** and still renders
  **LIVE**. A negative age compares as *younger* than any TTL, which is the
  B5-0597 failure-3 inversion exactly. The row will read LIVE until the clock
  catches up rather than ageing out. The B5-0949 DECISIONS entry already
  recorded the same class on a heartbeat payload (`me-so-poor.json` at
  `12:15:00Z` against a wall clock of `09:52Z`), so the class is confirmed twice
  in two different files and both instances were still on disk at seed time.
* **B5-0953** — the `census-crosscheck.ps1` DIVERGENT verdict, and its **root
  cause**: `.agent/CLAIMS/B5-0481.json` carries `agent_id` `solar-pro4`, for
  which **no heartbeat file exists** (the real file is `solar-pro4:free` under a
  registry row). So the three-signal join finds no owner signal, and
  `ledger-query.ps1` correctly prints `UNKNOWN` while `run-queue.ps1` degrades to
  the claim age alone — the B5-0609 defect class in a different hat. **One root
  cause, two disagreements**; the row says so explicitly so the second is not
  fixed as a separate bug.

**Hygiene, each with a measured file and byte count (3)**

* **B5-0955** — ten sweepable artefacts, every one confirmed by
  `git check-ignore` returning not-ignored. The clearest is one byte-class miss:
  `.gitignore` lists `out/`, `out_b50102`, `out_b50103`, `out_model`, `out_test`,
  `out_ui` — **but not `out_mer_slice`**. Also `.agent/TASK_LEDGER.md.bak` at
  **756,349 bytes**, the largest sweepable artefact, whose root twin
  `ledger.bak` (767,299 bytes) the `.gitignore` already covers — so the
  `.agent/` copy escaped the very rule written for it.
* **B5-0957** — four `b5ccg/` build logs un-ignored, scoped **narrower** than
  B5-0955 and marked parallel-safe so the two rows do not fight over the same
  lines.
* **B5-0958** — the B5-0943 double-count hazard the row's *own* verified cell
  warns of, plus the measured stub (front matter, three lines, no transcription
  data) against the substantive Cline close-out. Quarantine or supersede; **do
  not delete another agent's report**.

**Tool and rule convergence (2)**

* **B5-0956** — **11 of 13** `Headless*` classes are compiled but executed by no
  shipped gate: `compile.sh` names 2, `compile.bat` names **none**. The row's
  real question is which are gates and which are probes, and it is unanswerable
  from class names — so it requires running each and reporting exit codes.
* **B5-0959** — the B5-0948 row recorded a forward rule (a commit named in an
  attribution must be shown an ancestor of both `HEAD` and `origin/main` before
  it may be called an introduction). This row **exercises** that rule over every
  attribution that exists, because a rule written and never run decays into
  decoration.

**Card-pool follow-ons (2)**

* **B5-0960** — per-file Java 6 census across 63 tracked files, whose
  substantive half is the **false-positive study**: a grep for `->` or
  try-with-resources matches comment text and string literals, and a gate tuned
  until it stops crying wolf stops detecting anything. Raw count *and* verified
  count, both reported; if they differ, the difference is the finding.
* **B5-0961** — the cost-field backfill plan for the two families proven
  uncosted (CONFLICT 0/108, EVENT 0/96), **deliberately gated on B5-0947, which
  is not DONE**, so it is not claimable now. It writes no value. Writing printed
  orbs into the pool decides which layer wins, and that is B5-0388's bar, not
  this row's.

## 5. Post-write gates

```
run-dup-census.ps1  -> exit 0, PASS, 0 duplicate task IDs
ledger-query.ps1    -> all 11 new rows read  7 pipes / doubleLead no
                       B5-0951..B5-0961 UNCLAIMED | reportable
```

Every row I wrote is judged **directly** — they are my own rows under my own
claim, which `00_BOOT.md` step 9 names as the exception that proves the
claims-first rule.

## 6. Scope held

No `b5ccg/src` edit. No card JSON edit. No commit, no push. No foreign row,
claim, heartbeat, report or pattern edited. No claim held on any row other than
B5-0950. No row marked DONE that was never OPEN and claimed. Nothing deleted.

**Reusable lesson — filed as
`.agent/PATTERNS/opencode (big-pickle-free) bp4/2026-09-28-a-negative-age-is-a-liveness-verdict.md`:**
a *negative* age is the one liveness input no existing guard rejects, and it
passes the comparison that every guard is written against.
