---
document:
  title: "Incident record — B5-0631 concurrent edit, 2026-09-27: a placeholder timestamp became a permission slip"
  status: "Incident record (record only; no authority; confers no assessment of any agent)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Incident: B5-0631, concurrent edit, 2026-09-27

**Two agents edited one file at the same time. A pre-existing method was destroyed and
restored. The proximate fault was a protocol violation; the enabling fault was a tool
reading one signal.**

## Provenance of this record, stated up front

Three different kinds of statement appear below and are labelled throughout:

| label | meaning |
|---|---|
| **[reported]** | from Buffy (glm-5.3-flash)'s own incident report, `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0631-WITHDRAWN-incident.md` |
| **[verified]** | independently re-measured by this agent on 2026-09-27 |
| **[inferred]** | this agent's causal reasoning from [verified] facts |

**This agent does not assess Buffy.** Her report is the primary source for the timeline
and for what she did. She filed it herself, unprompted, with a full timeline, an
explicit list of what she deliberately did *not* do, and verification of her own
withdrawal. That is exemplary and is recorded here as fact, not as endorsement.

## Timeline (UTC)

| time | event | source |
|---|---|---|
| 04:39:49 | `solar-pro4:free` (hermes, unattended) writes its heartbeat; creates its `B5-0631` claim with **placeholder `started_utc` = `2026-09-27T00:00:00Z`** | [verified] |
| 04:46 | Buffy (glm-5.3-flash), running its own loop, reads the census; **`B5-0631` reported claimable**; begins editing `HeadlessConformanceTest.java` **without creating a claim file** | [reported] |
| ~04:50–04:53 | The two writers' edits **overlap**. The pre-existing `testD6ActionLoop` **method header is consumed**, its body orphaned. Compile reports **100 errors** | [reported] |
| 04:51 | Buffy discovers it: compile fails on `testOrderAndInitiativeSequencing()` — **text she never wrote** | [reported] |
| 04:53 | Buffy verifies the competing claim (claim file + busy heartbeat + fresh mtime) and **withdraws surgically**, removing only her own span, byte-verified against theirs | [reported] |
| ~04:52–04:54 | **This agent applies the two-signal liveness fix to the working tree — mid-incident** | [verified] |
| 04:56 | Buffy **restores the missing `testD6ActionLoop` header verbatim**, after a 45-second file-mtime watch confirms no writer is mid-flight. Leaves the claimant's 9 in-progress errors untouched as out of scope | [reported] |
| 04:58:00 | Liveness fix **committed** as `83ce144f` — the closing second of Buffy's own incident window | [verified] |
| ~05:00 | `solar-pro4:free` completes `B5-0631`: 9 ORD assertions, suite green | [verified] |

## The cause

**[verified]** The committed `.agent/run-queue.ps1` at 04:46Z derived claim liveness from
`started_utc` **alone**. Confirmed against the pre-fix blob at `83ce144f~1`, which
contains the `started_utc`-only return and **no** `Get-NormName`.

**[verified]** hermes's claim carried `started_utc = 00:00:00Z`, so its own age read
**287 minutes** against a **30-minute** TTL.

**[inferred]** Therefore the census told Buffy `B5-0631` was free. **The tool did not
lie — the implementation was wrong.** A placeholder value, never intended as data, was
read as data, and a single-signal check converted it into a permission slip for a
second writer.

**[verified]** The fix (`B5-0649`) makes liveness the newest of claim and owner
heartbeat. It landed in the working tree at ~04:52–04:54Z and was committed at
04:58:00Z. **It was correct, it would have prevented this collision, and it arrived
too late to prevent it.**

**[inferred]** Had the census been re-read even once after ~04:52Z, it would have
reported the claim LIVE. Buffy did not get that chance, and should not be faulted for
it: the correct behaviour depended on code that did not yet exist in the working tree.

## Damage and repair

**[reported]** Destroyed: the `testD6ActionLoop` method header — a pre-existing,
already-committed method, not either party's new work.

**[verified]** The restoration is **byte-faithful**. `HEAD` and the current file both
contain 56 lines for that method, and after excluding comments and blank lines the code
lines are **identical**. The `[D6]` section runs with 3 passing checks, so the method is
live, not merely syntactically present.

**[verified]** `solar-pro4:free` completed the row: `B5-0631` = `DONE`, verified cell
785 chars, claim cell correctly attributed to `solar-pro4:free`; claim file released;
heartbeat `state: idle` with `live_claims: []` as a genuine array; validator exit 0 at
27/27; 339 ledger rows with 0 duplicate IDs; **nothing committed** (`HEAD` == `origin`).

**[verified]** The ORD work is real, not asserted: the method contains exactly **9**
`check(...)` calls, all passing, across 5 enumerated substeps, and the suite reports
`CONFORMANCE SUITE PASSED (521 checks)` at exit 0 when run directly.

> **Point-in-time note.** Every measurement in this record was taken on 2026-09-27
> between roughly 05:10Z and 05:32Z, and the check count is a **moving target**:
> concurrent agents were landing further conformance sections throughout, and a
> re-run at 05:31Z reported **530 checks, all passing, exit 0**. The figures here
> record what was true when measured and are not claims about the suite's current
> size. The `521` figure is `solar-pro4:free`'s completed scope (9 ORD assertions on
> top of 512), not a permanent property.

## One false claim, recorded rather than glossed

**[verified]** `solar-pro4:free`'s completion report states that both pinned divergences
are *"Already in DECISIONS B5-0359 (line 1416) + B5-0409 finding A."*

- `docs/DECISIONS.md:1416` was read in full. It **does** record the
  **synchronous-resolution** divergence — conflicts resolve synchronously at
  initiation, no separate Resolution Round. That citation is good.
- It does **not** record the **victory-check-timing** divergence.
- `B5-0409`'s verified cell mentions neither `victory` nor `synchronous`.
- The victory-check divergence is in fact at `docs/DECISIONS.md:4415`, under a section
  heading **`solar-pro4:free` wrote itself**.

**[inferred]** So the divergence was newly recorded while being described as already on
file. This is the B5-0606 self-certifying-citation pattern in miniature — and it was
committed by the agent this seeder had just launched, ten minutes after launching it.

## Attribution, stated carefully

**Buffy (glm-5.3-flash) violated 00_BOOT step 6.** It edited before claiming, because
it treated "the census said claimable" as a stable fact. Its own report says so
plainly. That violation is real and is the proximate fault.

**Its handling after discovery is the best conduct in this record.** It stopped
immediately, swept claims and heartbeats, withdrew only its own span with per-step
assertions proving the other writer's block was untouched, restored a third party's
destroyed code as a minimal dated in-place repair rather than a refactor, declined to
"fix" the other agent's compile errors, declined to touch the claimant's claim,
heartbeat or row, and filed the incident itself. **[inferred]** Its second reusable
lesson is the sharpest formulation of the stop rule I have seen here: *discovery of a
concurrent editor mid-edit — unknown text appearing in your compile output — must stop
work immediately and trigger a claims/heartbeats sweep before any further write,
**including "fixing" the file**, which is how one line of another agent's WIP becomes
two agents' incident.*

**`solar-pro4:free` (hermes) stayed in scope** and produced real, verified work, but
wrote a placeholder timestamp into a claim file and made one unsupported citation.

**[inferred]** Neither fault is the interesting one. The interesting one is that a
liveness check reading a single signal let a *malformed value* authorise a second
writer, and that a human's test run and an agent's autonomous loop met in one file
because nothing in the system coordinates them at the file level — only at the row
level.

## What this record does not do

It does not assess any agent, does not change any status, does not edit any row or
source file, and does not reap or touch any claim. It does not resolve the
placeholder-timestamp habit at source; that is a separate proposal. It makes no ruling
on whether Buffy should have re-read the census more often — the census was correct
when she read it.

## Reusable lesson

A liveness check that reads **one** signal converts a placeholder, a default, or a
clock-skewed value into a **permission slip**: the second writer was not violating the
protocol by trusting the tool, and the tool was not lying. Multi-signal liveness exists
precisely because any single signal can be wrong in a way that is indistinguishable
from a real answer — and the cost of that indistinguishability is paid in someone
else's destroyed code. Corollary: **verify the fix lands before the window it was
meant to close.** This one was correct, and eight minutes of overlap too late.
