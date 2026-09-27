---
document:
  title: "B5-0657 — claims-first ledger census protocol: implementation report"
  status: "Report (observation and test results; no authority per AGENTS.md §3)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0657 — claims-first ledger census protocol

Human ruling 2026-09-27: the live-repair-aware ledger census protocol
(`docs/proposals/live-repair-aware-ledger-census-protocol.md`, Solar Pro4) approved
and implemented. `.agent/run-queue.ps1`, `.agent/tools/ledger-query.ps1` and
`.agent/00_BOOT.md` only. No `b5ccg/src/` or `b5ccg/resources/` file touched. No
commit — `CLAIMS/` and `HEARTBEATS/` stay uncommitted per the standing checkpoint
convention.

## The problem, in one sentence

A structural census of the ledger taken while another agent holds a live claim on
the rows being censused reports the *transient* state, and the transient state
belongs to a different defect class than the committed form.

The recorded instance, from the proposal: at 2026-09-26T22:18Z rows B5-0564 and
B5-0565 both read 6 pipes under live claim B5-0592, which looks exactly like the
missing-trailing-delimiter class. `git show d8216afa` confirms the committed form
was 8 pipes with a **leading** double pipe. A census taken in that window reports
two false defects and would send an auditor to repair the wrong thing.

## What I built, and the one design choice worth arguing about

The protocol permits either skipping a live-claimed row or explicitly marking it.
I implemented **marking**, in both tools, identically.

Marking beats skipping here because the whole output of these tools *is* a defect
census. Dropping the row removes the information the reader needs; marking it keeps
the reading visible and attaches the correct epistemic status to it. The protocol's
own remedy — re-census after release — is what discharges a marked row, so the mark
is not a dead end, it is a deferral with a named next step.

The mark is a **new additive column** (`defectReport`), not a change to the printed
liveness verdict. That was deliberate. `census-crosscheck.ps1` (B5-0651) computes and
diffs the liveness verdicts of these two tools; folding a second concept into the
verdict cell would have changed a field another tool reads by position. Keeping the
two concepts in separate columns means the suppression rule cannot perturb the
verdicts the crosscheck already checks, and it means a future reader can tell a
"this claim is live" statement apart from a "do not report this row" statement.

## Predicate parity, which is the load-bearing part

A suppression rule that the two census tools compute differently would recreate
exactly the divergence class this change exists to eliminate. So the predicate is
byte-for-byte equivalent in intent, and deliberately so on four edge cases:

| case | both tools | why |
|---|---|---|
| per-claim `ttl_min` present | honoured, default 30 | a claim may legitimately set its own TTL |
| future-dated `started_utc` | **live** | clock skew is not a defect (B5-0317 precedent) |
| unreadable claim file | **suppressed** | an unreadable file is not evidence of a defect |
| absent owner heartbeat | **absent information**, not stale | the B5-0609 class: a lookup matching nothing returned `-1`, and `-1` compares as *younger* than any TTL, manufacturing LIVE from an absent signal |

That last row is the reason "provably stale" is defined as *every determinable
signal is older than the TTL* rather than *no signal is fresh*. The two phrasings
differ exactly on the absent-signal case, and the second phrasing is the bug.

The fail-safe direction is silence in every branch, because the protocol's remedy
for a wrongly suppressed row is a re-census, while its remedy for a wrongly reported
defect is an agent editing a row another agent is mid-write on.

## run-queue: the aggregate check could not be attributed

`Get-LedgerRows` emits three warnings and all three are defect reports, so all three
needed suppression. Two of them (no-recognisable-status, duplicate ID) name their
offending rows, so they partition cleanly into reported and suppressed sets.

The third — "returned N rows but a permissive scan found M candidate row lines" — is
an **aggregate count comparison**. The offending row cannot be named from it, so it
cannot be selectively suppressed. I annotated it rather than dropping it: when a
live claim exists the message says the mismatch is not a defect report and names the
suppressed IDs, so a reader knows what to re-census. Suppressing the whole warning
silently would have made a real filter bug invisible whenever anyone happened to hold
a claim.

A genuine duplicate with no live claim still prints the original unannotated
instruction, so this change cannot mask the B5-0622 collision class.

## Verification

Live tree:

- `census-crosscheck.ps1` → `CONSISTENT -- 344 row(s)`, exit 0. The two tools still
  agree *after* the change, rather than having been made to agree by assertion.
- `run-queue.ps1 -DryRun` → exit 0, zero warnings on the healthy ledger.
- `b5ccg/compile.bat` → `Build successful`, JDK 1.8.0_292. Not this task's gate (no
  Java in scope) but recorded because other agents are editing Java concurrently and
  a red reading here would have needed attributing.

Isolated TEMP fixture, never the live tree, one synthetic ledger, two claim ages:

| case | `defectReport` | footer | run-queue warnings |
|---|---|---|---|
| A, claim 2 min old | `suppressed-live-claim` on all 4 defective rows | names them | both annotated `NOT A DEFECT REPORT` |
| B, claim 3 h old | `reportable` on all 4 | `0 rows under a non-stale claim` | original unannotated, incl. `DUPLICATE TASK ID: B5-9002 x2` |

The fixture carries a truncated mid-repair row (`| B5-9001 | OPEN`, 2 pipes) that
the permissive scan sees and `Get-LedgerRows` cannot parse. Without it the count
mismatch — the one warning that cannot be attributed to a row — would never fire, and
the test would have passed without exercising the branch that most needed it.

## Known gap, filed not absorbed

`census-crosscheck.ps1` has no coverage of the suppression rule: it computes the five
rules it knows about, and suppression is a sixth. The two copies of the predicate are
identical by construction and by review, but not yet by instrument — which is the
same latent-duplication condition this repository has already paid for once (the
B5-0649 entry, and my own filed pattern *a rule written down and implemented once is
a rule one component follows*). Filed as **B5-0658**, deliberately not absorbed,
because a rule that only the new code knows how to check is a rule nobody is checking.

The shared-library leg remains un-ruled: Tasks 2 and 3 of
`docs/proposals/tool-rule-convergence-proposal.md`.

## Reusable lesson

A census warning is a defect report, so a census run during someone else's repair
manufactures false defects — and the fix is a claims-first read plus an explicit
skip-or-mark, never a silent skip. When two tools must agree on which rows are
reportable, define "stale" as *every determinable signal is old* rather than *no
signal is fresh*, because an absent signal and an old signal are different facts and
collapsing them is how an absent heartbeat becomes a live claim.

Filed as `.agent/PATTERNS/opencode (space-bunny-free)/2026-09-27-a-census-warning-is-a-defect-report.md`.
