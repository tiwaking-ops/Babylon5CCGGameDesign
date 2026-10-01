---
document:
  title: "Clock integrity in agent coordination files — failure modes, measurement, and prevention"
  status: "Report (observation tier — describes, does not govern; copying or citing confers no authority)"
  audience: "Autonomous AI agents provisioning or operating an agent-coordination system with file-based liveness"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 7", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 7", version: "space-bunny-free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
measured_on: "Babylon5CCGGameDesign, 2026-09-30T18:17Z, 125 coordination files, PowerShell 5.1 / Windows, host TZ New Zealand Standard Time"
companion: "docs/reports/autonomous-pm-system-replication-manual-2026-09-29.md (see its §7.5 for what this report amends)"
---

# Clock integrity in agent coordination files

## 0. How to read this report

- **Tier.** `docs/reports/` is the observation tier (`AGENTS.md` §3). This document records
  what was measured and what follows from it. It governs nothing, here or in any other repo.
  Promotion happens only through a target repo's own `AGENTS.md` + gate + decision log.
- **Relationship to the replication manual.** The manual's §7.5 specifies liveness as the
  newest of three signals, and its §11 failure table carries one row for a *placeholder* or
  *midnight* `started_utc`. It has **no row for a future-dated timestamp**, and no row for the
  trust relationship between an agent-written payload and the filesystem. This report is the
  amendment to those two gaps, not a restatement of the manual.
- **Conventions.** MUST = load-bearing; violating it corrupts coordination. SHOULD = strong,
  deviation recorded. MAY = optional.
- **Two corrections to earlier statements** are recorded in §3.4 and §4.3, because both were
  wrong in ways that would have made this report actively harmful if repeated.

## 1. Executive summary

Agent coordination systems that lock work via files almost always record a
self-reported timestamp alongside the lock. **The self-reported timestamp is the weakest
link in the entire design**, because a language model can produce a plausible-looking ISO
timestamp without ever reading a clock, and nothing downstream can tell the difference.

The single rule:

> **Never let an agent author a timestamp that a decision depends on. Either the tool
> writes it, or the decision reads the filesystem instead.**

Measured on this repository, 2026-09-30T18:17Z, 125 claim and heartbeat files:

| Class | Count | Meaning |
|---|---|---|
| `DISAGREE` | 11 | payload and file mtime differ beyond tolerance — clock-integrity signal |
| `IN-BULK-CLUSTER` | 87 | mtime was bulk-assigned by one operation; gap is uninformative |
| `idle` | 25 | payload agrees with mtime, both old — **correct** for a stopped session |
| `ok (live)` | 2 | payload agrees with mtime, inside TTL |

One agent carries a genuinely broken clock (**+741.8 min** against its own file). Two more
are moderately off (+186 to +215 min). The remaining eight are sloppy within ±36 min. This
is a **graded** table on purpose: a binary pass/fail sweep over the same data produces either
98 false alarms or misses the one real defect.

## 2. Why a wrong timestamp is worse than a typo

Liveness is arithmetic: `age = now - payload`, then `age < TTL`. A payload dated in the
**future** yields a **negative** age, and negative compares as *maximally fresh*. The lock
therefore never ages out. It reads live forever.

In the tool that implements this (`ledger-query.ps1:383`):

```powershell
$verdict = if ($newestMin -lt $TtlMinutes) { "LIVE" } else { "STALE" }
```

A negative age sails through that test. Observed output: `claimAge -618.6 min | verdict LIVE`.

The system then does the worst possible thing with the bad value: **agrees with itself.**

```
.agent/CLAIMS/B5-0953.json    started_utc 2026-09-29T07:04:00Z
.agent/HEARTBEATS/solar-pro4.json  utc    2026-09-29T07:04:00Z   ← same wrong value
```

Both signals agree because both were written by the same broken clock. **The natural
cross-check — do the signals corroborate each other? — cannot detect this class at all.**
Consistency is exactly what you would be testing for, and here it is perfectly consistent.

### 2.1 The damage is not the timestamp, it is the un-clearable lock

The consequence is not a wrong number in a report. It is that **the lock cannot be released
by anyone.** The usual safety rule — never edit or reap another agent's claim — is correct
and must be kept, and it means a lock held by a dead or skewed session is *stuck*. When the
claim gates other work, one bad timestamp cascades.

Measured on this repo: `B5-1047` (engine scope, one-writer-per-scope) was the chain head for
**13 rows** that all read `BLOCKED` awaiting it. Each blocked row was independently diagnosed
by a different agent, and each closed rather than guess. Four separate close-outs record the
same wall in consecutive minutes. The only resolutions found were both human escalations.

**For a new deployment this is the finding that matters most.** A liveness system that cannot
clear its own stale locks will accumulate them, and each one is a permanent tax on the rows
behind it.

## 3. Root causes

### 3.1 Extrapolation, not measurement (no dishonesty required)

The agent reads the clock once at session start, then *computes* later timestamps by
arithmetic. It is not lying; it is producing a plausible value from the most recent value in
its context, which is the cheapest available action.

Self-reported in this repo, `2026-09-28-Buffy (glm-5.3-flash) 2-TIMESTAMP-INTEGRITY-CORRECTION.md`:
after a 19:50Z reading, timestamps drifted a regular **+13 min per task**. Two of five writes
were correct and the drift began mid-session — which is precisely why spot-checking does not
catch it. One `date -u` call per write is a discipline, not an instinct.

**Tell:** arithmetically regular drift across one agent's files.

### 3.2 Host clock skew (an environment bug, not a reasoning error)

`date -u` returning local time, or a host whose clock is simply wrong. Nothing about this is
the model's fault and no prompt can fix it.

Measured, and the reason the ids matter:

| File | agent_id | payload | file mtime | gap |
|---|---|---|---|---|
| `solar-pro4.json` | `solar-pro4` | `2026-09-30T22:45:00Z` | `2026-09-30T10:23:11Z` | **+741.8 min** |
| `solar-pro4-free.json` | `solar-pro4:free` | `2026-09-30T09:57:00Z` | `2026-09-30T09:57:39Z` | −0.7 min |

**These are two distinct registered identities and only one is broken.** The remediation
target is the *session instance*, not a model or a "family" — a remediation that goes looking
for "the solar-pro4 agent" will either fix nothing or break the one that is correct.

**Tell:** a constant offset across all of one instance's files, while a sibling id from the
same client is clean.

### 3.3 Placeholders

`B5-0649`: a session wrote `started_utc: 2026-09-27T00:00:00Z` on purpose, so the claim read
292 min old against a 30 min TTL. Midnight-by-default is the characteristic value and is the
cheapest to detect.

### 3.4 Correction — a stale agent is not a skewed agent

My first pass at this reported an agent as "+726.7 min skewed" from `payload - real-UTC`.
That reasoning was **wrong**, and it is the single most common error in this area.

```
agent-id                 payload-vs-real-UTC    payload-vs-own-mtime
me-so-poor.json          -510.2 min (idle)      +2.8 min  (honest)
solar-pro4-free.json     -498.2 min (idle)      -0.7 min  (honest)
solar-pro4.json          +269.8 min             +741.8 min (BROKEN)
```

Every heartbeat in the store is hours or days behind real time simply because those sessions
stopped — that is the system working. A large `payload - real` gap is **freshness**, not
integrity. Only `payload - mtime` speaks to integrity, because mtime is recorded by the
filesystem for the same write and cannot be back-dated by the process doing the writing.

Scoring on `payload - real` marks 25 correctly-idle agents as broken and still finds the one
real defect only by accident.

## 4. Measurement

### 4.1 The discriminator

```
integrity_gap = payload_timestamp - file_mtime
freshness_age = real_now        - payload_timestamp
```

`integrity_gap` near zero means the agent's clock and the filesystem agree about when the
write happened. Large `integrity_gap` means they do not, and no amount of idleness explains
it.

### 4.2 The false-positive trap: bulk touches

The obvious next step — flag every large `integrity_gap` — manufactures false alarms at scale.

**87 of the 125 files in this store share a single mtime: `2026-09-29 08:44`.** There is no
git commit at that hour (last is `2026-09-28 07:31`). That is one operation — a checkout,
restore, copy or sync — touching 87 files, not 87 writes.

For those files **mtime is not a per-write record**, so `integrity_gap` carries no information
about any agent's clock. My first sweep scored them as defects and reported ~90 bogus
"disagreements" with gaps up to −11,422 min, which is really just "this file was written on
the 21st and its mtime was reset on the 29th."

Any sweep MUST detect shared-mtime clusters and exclude them before scoring. This also
directly limits §5.1: **mtime is unforgeable by an agent choosing a value, but it is not a
faithful per-write record if any tool bulk-rewrites the tree.** Both channels are imperfect,
which is why no single-channel design in §5 is sufficient on its own.

### 4.3 Correction — and this one breaks my own earlier recommendation

Because mtime is OS-maintained, the obvious fix is to drop the payload and use mtime for
liveness. **§4.2 shows that is unsafe here**: 87 files would read as having all been written
at one moment, so 87 agents would appear simultaneously live, and any of them could be handed
a task held by another. The recommendation survives only with the bulk-cluster exclusion
attached, and it is weaker than I first stated.

### 4.4 The graded table

Script: `.agent/tmp-clock-audit/measure-offsets.ps1` (read-only, writes nothing). Sort by
`integrity_gap`; the head of the list is the whole finding:

| agent_id | kind | gap (min) | reading |
|---|---|---|---|
| `solar-pro4` | heartbeat | **+741.8** | **broken clock** |
| `solar-pro4:free-b51068blocked` | heartbeat | +215.0 | skewed |
| `GitHub Copilot (Auto mode)` | heartbeat | +185.9 | skewed |
| `Cline (space-bunny) b5-1045` | heartbeat | +61.4 | sloppy |
| `opencode (big-pickle) vb1067` | claim | −36.3 | sloppy (payload precedes mtime) |
| `GitHub Copilot (Auto mode) 0930` | heartbeat | −20.8 | sloppy |
| `opencode-me-so-poor-loop2` | heartbeat | −9.6 | sloppy |
| `Buffy (glm-5.3-flash) 9` | heartbeat | +8.9 | sloppy |
| `me-so-poor b5-1014` | heartbeat | −8.6 | sloppy |
| `Cline (space-bunny) b5-1119` | heartbeat | +5.7 | sloppy |
| `Buffy (glm-5.3-flash) 6` | heartbeat | +5.6 | sloppy |

Three bands, and the bands are more useful than a verdict: **>120 min** is a broken clock and
warrants a host fix; **30–120 min** is drift worth flagging; **under ~30 min** is ordinary
imprecision and flagging it trains everyone to ignore the tool. A negative gap is not
automatically a defect either — a file legitimately rewritten after its payload was set
produces one.

### 4.5 A second-order finding: the honest stale claim

`B5-1047` measured `payload 09:33:27Z` against `mtime 09:34:19Z` — a 0.9 min integrity gap,
i.e. an **honest** timestamp, 524 min old. Its owner's heartbeat was 500 min old. All three
signals stale, so the existing three-signal rule already declares it reapable, and 13 rows
are unblocked by deleting one file.

It was not deleted, because every agent correctly stood down rather than reap someone else's
claim. **The rule worked; the absence of a reap path for provably-dead claims did not.** See
§6.2.

## 5. Prevention

Ordered by how much each actually stops, not by how easy it is.

### 5.1 Demote the payload to forensic-only — MUST, with the §4.2 exclusion

Make the agent-written timestamp a record for humans, never an input to a liveness decision.
Base liveness on filesystem mtimes, **and** exclude bulk-touched files (§4.2) rather than
trusting a reset mtime.

Caveat to state in any target repo: this changes a liveness rule that a human may have
approved on the reasoning that an implausible claim should be *refused* rather than
consumed. That reasoning is sound *for the payload*. For mtime it does not transfer, because
mtime is not a claim. Record the change in the decision log; do not make it silently.

### 5.2 Have the tool write the timestamp — MUST

A `claim` / `heartbeat` helper that stamps `[DateTime]::UtcNow` itself, so the model never
types a time. This is the only recommendation that reduces the failure to zero rather than
detecting it, and it also covers the case mtime cannot: **timestamps written into ledger
cells, reports and prose**, where there is no inode to fall back on.

Cost is one script per coordination file type. It is the highest-value change on this list.

### 5.3 Require corroboration, not one signal — MUST

LIVE requires **two independent channels to agree** — claim mtime *and* owner heartbeat mtime
both inside TTL. Today any single fresh signal carries the verdict, so one wrong value is
enough. Conjunction costs almost nothing, because a live agent refreshes both every few
minutes, and it removes the single-point failure entirely.

### 5.4 Make an impossible age non-comparable — MUST

If `now - payload < 0`, the value is not a measurement and MUST NOT reach a comparison. Parse
it, mark it invalid, and fall back to a trustworthy channel. Today the negative number is
computed, printed, and compared — which is how a nonsense value becomes a safety verdict.

### 5.5 Quarantine rather than score — SHOULD

On write, if `|payload - mtime| > tolerance`, record the disagreement and use mtime. Do not
silently prefer one channel: a silent preference is indistinguishable from the bug it hides.
Tolerance must be per-file-type and stated — a heartbeat is a moment marker and earns a tight
one (seconds); a claim may be legitimately re-claimed, so it earns a loose one (one TTL).

### 5.6 Fix the host clock — MUST, and it is not optional

No prompt, guard or gate corrects a host whose clock is 12 hours wrong. Verify the host clock
at boot, on every session, and record the reading in the heartbeat alongside the timestamp it
produced. If the two disagree, that is a boot failure, not a finding to log later.

### 5.7 Record the offset, not just the time — SHOULD

Store `utc` **and** a monotonic counter or sequence number. Two adjacent writes of the same
`utc` are legitimate (same second); a *decreasing* `utc` across writes is not, and a sequence
number catches clock regressions that no wall-clock comparison can see.

## 6. What does not work

### 6.1 Tightening tolerance

The runner's 60-minute tolerance absorbed a +28 min error and converted a loud failure into a
quiet near-miss. Tightening only relocates the threshold where errors get absorbed. The
defect was never the size of the error; it was that the error reached a comparison at all
(§5.4).

### 6.2 Warning louder

Reducing the runner's implausible-timestamp warnings from 20 lines to 10 (this repo, B5-0981)
removed noise and removed information at the same time, and changed no verdict. Meanwhile the
honest stale claim of §4.5 went un-reaped for hours: the *action* was missing, not the alert.

The gap that actually needed filling is a **reap path for provably-dead claims**. A bounded
rule — all three channels stale past N× TTL, reap recorded with its measurement — would have
cleared §4.5 unattended. That is worth more than every guard in §5 combined, because it is the
only one that resolves an existing stuck lock rather than preventing a future one.

### 6.3 A "does it conform?" validator

This repo's heartbeat validator has been **exit 1 with 9 non-conforming files and 3 identity
collisions** for days. An always-red gate is a dead gate: its output stops being read, so it
protects nothing. Any new gate must be **scoped to a single file** and be **green in steady
state**, or it will join this one.

### 6.4 Trusting a self-reported heartbeat

A heartbeat whose `utc` is authored by the agent is the same failure as the claim payload, in
the one file whose entire purpose is to be a trustworthy signal. Observed here: the owner
heartbeat repeated its claim's wrong value exactly, making the signal set internally
consistent and uniformly wrong.

## 7. Checklist for a new deployment

**At boot, every session:**
- Read the host clock. Record the reading *and* the timestamp derived from it in the same
  command block. If they disagree → stop, the host is broken.
- Never type a timestamp. Call the tool that writes it.

**At design time:**
- Liveness reads filesystem channels; the agent payload is forensic-only (§5.1).
- LIVE requires two channels to agree (§5.3).
- A negative age is rejected at the parse boundary, never compared (§5.4).
- Shared-mtime clusters are detected and excluded from any mtime-based logic (§4.2).
- A bounded, logged reap path exists for provably-dead claims (§6.2).

**When auditing:**
- Score `payload - mtime`, never `payload - real` (§3.4).
- Report a **graded** table with stated bands, not a binary verdict (§4.4).
- Fix the **session instance**, not the model or client (§3.2).

## 8. Reusable lesson

A coordination system that asks an agent to report *when it is* has built its weakest lock
out of its least reliable measurement. The failure is silent, it inverts the safety direction
by making a dead lock look alive, it survives cross-checks because the wrong value is written
to every channel at once, and it cannot be undone by the safety rule that protects every
other lock. So: **make the tool write the time, make the decision read the filesystem, make
liveness require two channels to agree, and give dead locks a bounded way out** — because the
last one is the only measure that clears the locks you already have.

---

*End of report. Tier: observation — to adopt anything herein, promote it through the target
repo's own `AGENTS.md` + gate + decision log.*

**Reproducing the measurements.** `.agent/tmp-clock-audit/measure-offsets.ps1` is the
read-only instrument behind §1 and §4.4; it writes nothing. It is **git-ignored** via
`/.agent/tmp-clock-audit/` in `.gitignore`, following the disposition and reasoning of the
`/.agent/tmp-0719-harness.ps1` entry already there (B5-0955): a cited measurement record is
kept on disk and kept out of git, since an ignore line is reversible and deleting an untracked
file is not. Verified after the edit: `git check-ignore` exits 0, the file is present and still
runs, and it no longer appears in `git status`, so a `git add -A` checkpoint can no longer
absorb it as a deliverable.

That same edit records, without acting on it, that the sibling probe directories
`.agent/tmp_b51043/`, `.agent/tmp_b51104/` and `.agent/tmp_b51113/` were untracked and
un-ignored in exactly the same way when this report was written. They are **not** listed in the
ignore block: they belong to other agents' closed tasks, so their disposition is theirs to
record. `docs/proposals/scratch-disposition-tmp-scans-proposal.md` already owns part of that
question.

**Counts drift.** The figures in §1 and §4.4 are a point-in-time census taken at
`2026-09-30T18:17:06Z` and are labelled as such. A re-run ten minutes later read 124 parsed
rather than 125, with the same 11 disagreements and the same 87-file bulk cluster — one claim was
released in between. Treat the *bands* in §4.4 as the finding and the totals as a census, not as
a fixed inventory.
