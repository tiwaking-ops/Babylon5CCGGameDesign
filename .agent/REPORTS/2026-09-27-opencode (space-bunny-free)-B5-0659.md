---
document:
  title: "B5-0659 — three-signal claim liveness adopted, fourth key rejected"
  status: "Report (observation and test results; no authority per AGENTS.md §3)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0659 — three-signal claim liveness adopted, fourth key rejected

Human ruling 2026-09-27: **APPROVED** — adopt the three-signal liveness rule as
canonical. `.agent/tools/ledger-query.ps1` and `.agent/CLAIMS/README.md` plus one
DECISIONS entry. No src or resources edits, no commit.

## The rejection is the substance of this task

The adoption was the easy half: the three-signal rule was *already binding* in
`.agent/HEARTBEATS/README.md`, so "adopt" meant pointing the census tool and the
claims README at an existing rule rather than inventing one.

The proposal's **key 4** wanted a bare mention of a task id in *any* heartbeat's
`current_task` or `live_claims` to block a reap. I recommended rejecting it and the
human approved that, and the reason is worth keeping in this repo's own terms: a
mention is not ownership.

An agent triaging the queue writes ids into its heartbeat. An agent reporting on
someone else's abandoned claim writes that id. Both are ordinary, and under key 4
either one pins the claim forever. A queue reference silently becomes a lock — and
the reaper is the *only* mechanism that recovers a claim abandoned by an agent that
died mid-task. A rule that makes the recovery mechanism unreachable in the common
case is not conservative, it is destructive.

Ownership is a keyed join: the claim's own `agent_id` matched to that owner's
heartbeat. Never a textual mention. And because an absent signal is `UNKNOWN` and
never `LIVE`, absent ownership evidence cannot manufacture a blocker from the other
direction either.

## Two readings the wording forced, settled by tests that fail the other way

The README's own wording is precise but has two branches a reader could take, and I
did not want to pick on preference. Each was settled by showing the alternative
**fails**:

**(a) Is the report signal required?** If yes, every in-flight task is UNKNOWN,
because reports are written at close-out and no report exists for work in progress.
A rule under which nothing can read LIVE cannot suppress anything — and it would
silently un-suppress the B5-0657 claims-first census, resurrecting the exact
false-defect class that task was written to kill. So the report is
contributing-only: absent, it contributes nothing and manufactures nothing.

**(b) Then what does "reap only when all three signals are STALE" mean?** Read
strictly — a report must exist *and* be stale — no claim without a report could ever
be reaped, and a claim without a report is exactly the abandoned-mid-work claim a
reaper exists to recover. So it means "no signal is fresh". Worth noting these two
formulations cannot drift apart: the newest of three signals is stale exactly when
all three are.

Recording this matters because the *convenient* reading of (a) — report required —
looks more conservative and is in fact the one that breaks recovery.

## Two defects the old code's own comments already promised against

While implementing, I found the code contradicting its comments in two places. Both
comments were correct; the code had drifted.

**A corrupt claim file read as `UNCLAIMED`.** The claim index dropped unparseable
files in an empty catch. So a claim that *exists* but cannot be parsed reported as
"no claim at all" — the precise opposite of the truth, and the dangerous direction:
it invites a second agent to take a task that is already locked. Now `UNKNOWN` with
a reason, keeping the file's mtime as a signal (the filesystem maintains it without
the writer's cooperation), and suppressed from the defect report.

**The suppression predicate read a missing heartbeat as claim age.** The comment
said an absent owner heartbeat "is never read as staleness", but the code folded
the heartbeat in only under `if ($heartbeats.ContainsKey($k))`, so a claim whose
owner had no heartbeat fell through to claim age alone and could be declared
reportable on the strength of the one signal nobody disputed. Now: no matching
heartbeat means *not provably stale*, so suppressed. The protocol's own remedy for
a suppressed row is re-census after release, not a report — which is the correct
direction when a signal cannot be read.

A comment stating an invariant is not evidence the invariant holds. Both of these
were documented, believed, and untrue.

## The bug that only a fixture could catch

The corruption fix introduced a non-empty `catch`, and the tool immediately crashed
on the first corrupt claim:

```
Index operation failed; the array index evaluated to null.
  at ledger-query.ps1: line 121
```

Inside a PowerShell `catch` block, `$_` is rebound to the **ErrorRecord**. So
`$claims[$_.BaseName]` indexed a hashtable with null. The pre-existing code had an
*empty* catch and therefore never tripped it — the bug was latent in the shape I
was editing, not in the old logic.

The part worth stating plainly: **the live run was green the whole time.** The live
tree happens to contain no corrupt claim file, so the tool worked perfectly against
real data and was broken against the case it existed to handle. Only a fixture that
deliberately contained `{ this is not json` found it. A green run on real data
evidences that the real data has no instance of the defect — which is a fact about
the data, not about the code.

## The harness had to be debugged too

Three fixture bugs, each of which would have produced a confident wrong answer:

- Fixture IDs like `B5-f001` did not match the tool's `B5-[0-9]{4}` row regex, so
  the fixtures were invisible and every assertion failed for the wrong reason.
- All fixtures shared **one** heartbeat file, so the last mtime assignment won for
  all of them and every "stale heartbeat" case silently became fresh. Per-fixture
  owners fixed it.
- My assertion regex anchored on `^\|` while output rows start with the id.

The shared-heartbeat one is the dangerous class: it did not fail loudly, it made a
STALE case read LIVE, and only a careful read of the printed table caught it. Note
the harness asserts up front that its own generated timestamps parse, are in the
past, and are not midnight — a harness that silently emitted a midnight placeholder
would make every liveness assertion vacuously true while testing nothing, which is
the B5-0631 signature.

## Half the rule is deliberately not implemented

`.agent/run-queue.ps1` `Test-LiveClaim` and `.agent/00_BOOT.md` step 10 are the
other half, and I did not touch them: `.agent/CLAIMS/B5-0653.json` is a **live
claim by Buffy** whose scope names exactly those two surfaces, and one writer per
scope means a live claim is never touched. So the runner still uses newest-of-**two**
and still falls back to claim age on a missing heartbeat, and step 10 still states
the rule as a single signal ("Claims older than 30 min are stale").

That is a known-incomplete state and it is recorded as one — in the ledger row, in
the DECISIONS entry, and as seeded task **B5-0660**, gated on that claim releasing.
I did not let "I implemented the approved rule" stand unqualified when half of it is
not in the tree, because the next agent to read the ledger would otherwise believe
the tools agree about liveness when they do not.

`census-crosscheck.ps1` is deliberately untouched too: it is untracked and in flight
under the B5-0653 read-and-reconcile pass, so editing it would collide with a
different task's reconciliation. It currently reports CONSISTENT at 346 rows, which
is true but partly because no live fixture exercises the divergence.

## Verification

16-assertion harness in an isolated `%TEMP%` tree, live script copied into a
synthetic `.agent/`, live tree never mutated: stale claim + stale heartbeat carried
to LIVE by a fresh report; no report yet stays LIVE; missing heartbeat reads UNKNOWN
with a visible reason and is suppressed; corrupt claim reads UNKNOWN and is not
UNCLAIMED; genuinely stale with no report still reads STALE and is reportable, so
the rule does not over-suppress. `census-crosscheck` CONSISTENT at 346 rows.
`run-queue -DryRun` clean. `compile.bat` green (JDK 1.8.0_292) as a tree-health
reading; no code in scope. Ledger: duplicate-ID empty, my rows 7 pipes,
`doubleLead no`.

## Reusable lesson

A mention is not ownership. Any coordination rule that treats "an id appeared
somewhere" as evidence of a live intent converts a reference into a lock, and locks
are how abandoned work stays abandoned — the recovery mechanism becomes unreachable
in exactly the case it was built for. Match on keys, never on text. And the corollary
worth keeping: a comment stating an invariant is not evidence the invariant holds
(two documented "never read as" guarantees here were both untrue in the code beneath
them), while a green run against real data is evidence about the *data*, not the
code — the live tree had no corrupt claim file, so the tool was green and broken
until a fixture contained one on purpose.
