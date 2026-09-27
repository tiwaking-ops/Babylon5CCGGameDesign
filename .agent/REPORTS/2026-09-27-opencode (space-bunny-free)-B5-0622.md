---
document:
  title: "B5-0622 — claim-protocol and duplicate-ID gaps closed (both human-approved)"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0622 — two governance gaps closed

Both items were put to the human as two numbered proposals after the seed-wave-11
collision and reap, and were approved verbatim. This row exists because both parts
edit `.agent/00_BOOT.md`, and one writer per file is the rule — so they were
seeded and executed as a single task with two independently verified parts.

Toolchain: `javac 1.8.0_292`. Gate: `compile.bat` → **Build successful**.

## PART 1 — a claim must name a row that is still OPEN

**The defect, measured not anticipated.** The reap authorised at the end of the
previous pass was supposed to be routine and was not. Both claim files reaped
(`B5-0577.json`, `B5-0587.json`) belonged to `solar-pro4:free`, whose heartbeat
was **13 minutes old** at reap time. The owner was not stalled and had broken no
rule. The actual situation: rows `B5-0577` and `B5-0587` had already been closed
`DONE` by a **different** agent, Buffy (glm-5.3-flash), with reports timestamped
`2026-09-26T23:06:22Z` and `2026-09-27T00:34:48Z` — while solar-pro4 wrote its
claim files against those same two IDs *afterwards*, at `00:16:17Z` and
`00:32:00Z`.

So the protocol had a hole with no guilty party. `00_BOOT.md` step 6 and
`CLAIMS/README.md` both said to create the claim file "if the file already
exists, abort" — testing only the **claim file**. Nothing anywhere required
checking the **row**. A live, rule-abiding agent could therefore accumulate
claims that are simultaneously unworkable (the task is closed) and unofferable
(no runner offers a non-`OPEN` row), and nothing would ever surface the
contradiction.

**The fix.** Both documents now state that absence of the claim file is
*necessary but not sufficient*, that the candidate row must be re-read
immediately before writing and must still read `OPEN`, and that a claim against a
`DONE`/`VOID`/`SUPERSEDED`/`BLOCKED` row is an orphan to be released rather than
worked. Each names the B5-0622 instance so the rule is anchored to evidence
rather than to authority.

## PART 2 — a duplicate task ID must not be silent

**The defect.** `.agent/run-queue.ps1` builds its prerequisite and lane tables
from `$statusOf[$r.Id] = $r.Status`. With two rows sharing one ID the second
silently overwrites the first, so one task stops existing as far as the scheduler
is concerned: it can never be offered and its status is never read.

The runner already had a self-check, and it is the wrong shape for this class. It
compares the number of rows *parsed* against the number of lines *matching the row
pattern* — two independent counts of **rows**. Two well-formed rows sharing an ID
pass it, and the previous session's own comment records the identical mistake one
layer up ("compared a set against itself and could never detect a row the regex
could not see"). The check verifies that rows are *visible*, never that they are
*distinct*.

This was not theoretical. Earlier the same day, two seeders both measured
`B5-0618` free inside the same window; only a post-write duplicate census caught
it, and resolving it took three renumbers because the obvious repair deadlocked.

**The fix.** A distinct-ID assertion now sits beside the existing permissive
self-check in `Get-LedgerRows`, naming the offending IDs and the count, and
warning that a duplicate silently drops a task and that the repair must diverge
to a non-adjacent ID. `00_BOOT.md` step 9 now carries the post-write duplicate
census as a required command, states that a pre-write free-ID check is necessary
and **not** sufficient because the thing it races is another reader rather than a
stale file, and documents the empty-output pass condition.

## Verification

The runner change is `+12` lines and nothing else. Tested against an **isolated**
synthetic ledger in a temp directory — the live ledger was never used as a test
fixture:

| case | result |
|---|---|
| healthy live ledger, `-DryRun` | exit 0, **no** warning of any kind, unchanged behaviour |
| synthetic ledger, `B5-0001` twice | `DUPLICATE TASK ID: B5-0001 x2`, exit 0 |
| synthetic ledger, `B5-0202c` twice (suffix class) | `DUPLICATE TASK ID: B5-0202c x2` — assertion tolerates the suffix class |

`compile.bat` green on the exact tree. No `b5ccg/src` or `b5ccg/resources` edit;
every change is Markdown plus one PowerShell script.

## Finding left open — a pre-existing crash, deliberately not fixed

The suffixed-duplicate case exits 1, and the cause is **not** this task's change:

```
At .agent\run-queue.ps1:162 char:23
+ ...   $sorted = @($free | Sort-Object { [int]($_.Id -replace 'B5-', '') })
+ CategoryInfo          : InvalidOperation: (:) [], RuntimeException
```

`[int]'0202c'` throws. The identical line exists in `HEAD`, so this is
pre-existing. The row **parser** was taught to tolerate the letter-suffixed ID
class by B5-0613 (`^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|`), but the **numeric sort and
lane casts** ten lines later were never updated to match. Any `OPEN` row with a
letter suffix therefore crashes the runner outright.

It is latent today only because every suffixed row currently on disk (`B5-0202c`,
`B5-0329a`, `B5-0330a`, `B5-0331a`) happens to be `DONE` and so never reaches the
`OPEN` filter. The suffix class is demonstrably in active use, so this is a
loaded gun rather than dead code.

**Not actioned.** It is outside this row's approved scope, and the honest
disposition for a defect found while working is to report it rather than absorb
it. It is listed to the human as a candidate; the fix is a sort key that strips a
non-numeric suffix rather than casting the raw ID. It is also the same *shape* of
half-done repair as B5-0613, which fixed the parser and left the sort.

## Finding left open — the heartbeat migration is half-finished

Not mine, not actioned, but recorded because it is in the file I edited:

`.agent/00_BOOT.md` step 3 (added by another session under this same
`agent_id`, human-approved 2026-09-27) states that
`.agent/HEARTBEATS/README.md` is binding and enumerates a schema requiring
`schema_version`, `agent_id`, `utc`, `state` and `live_claims`. Two problems:

1. **The cited document does not contain that schema.** `HEARTBEATS/README.md` is
   still the original unmodified file (author Muse Spark, `last_modified_date`
   2026-09-21) and documents a different, simpler format. The schema actually
   enforced lives in `.agent/tools/validate-heartbeats.ps1`. The step-3
   enumeration is accurate about the *validator* and inaccurate about the
   *document it cites* — the exact shape of the self-certifying-citation defect
   recorded in the B5-0606 pattern.
2. **The migration is incomplete.** `validate-heartbeats.ps1` exits **1** against
   the live store, reporting legacy timestamp keys and registry-resolved
   `agent_id`s across essentially every file, with a `_quarantine/` directory and
   several heartbeats already deleted in the working tree.

A binding pointer to a specification that does not exist, backed by a validator
that fails on the whole store, is worse than no pointer. That session's claim
(`heartbeat-identity-resolution.json`) is now released and the work is
uncommitted, so someone must decide whether to finish it or revert it. I left
every byte of it untouched, and this task's own heartbeat was written to the
*validator's* schema so as not to deepen the inconsistency.

**Reusable lesson:** a protocol that tests one artefact when the invariant is
about another will pass forever. "The claim file is absent" is a true statement
about the filesystem and a false statement about whether the task is claimable;
only reading the row answers the question that was actually being asked. Pair
that with the companion from this pass: a self-check must assert the property you
care about, and counting rows never establishes that rows are distinct.
