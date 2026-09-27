---
document:
  title: "B5-0624 — the suffix crash, and the silently-degenerate sort key that replaced it"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0624 — suffix crash fixed, and a worse bug caught doing it

Human order: fix `run-queue.ps1:162` second, after the heartbeat migration.

## The crash

`[int]` on a stripped task ID throws for the letter-suffixed ID class. Two sites do
it: the `Sort-Object` key over claimable rows, and the lane-assignment site. Any
`OPEN` row named `B5-nnnnX` takes the **whole runner down** — not that one task, the
tool — because the throw happens while building the candidate list.

**Latent, not live.** No `OPEN` suffixed row exists today: every suffixed row on disk
(`B5-0202c`, `B5-0329a`, `B5-0330a`, `B5-0331a`) is `DONE`, so it is filtered out
before the cast. The runner is green and stays green until someone seeds something
like `B5-0625a`. It is a loaded gun because the suffix class is demonstrably in active
use, not because anyone predicted a future row.

Root cause is a half-done repair — the same shape as B5-0613, which taught the row
*parser* to tolerate the suffix via `^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|` and left every
numeric use behind. **A parser that accepts a shape its consumer cannot arithmetic is
a defect that only fires on the day the shape is used.**

## The fix

One shared `Get-TaskNumber` helper, used at both former cast sites, so the suffix
class is handled in a single place and the next consumer cannot reintroduce the cast.
Both bare `[int]` casts of the stripped ID are gone, confirmed by grep.

## The near-miss, which is the more valuable half

My first version of the helper was:

```powershell
$m = [regex]::Match([string]$Id, '(\d+)')     # WRONG
```

That is the natural way to write it and it is **silently, totally wrong**. `Match`
finds the *first* digit run in the string — and the literal `B5` contains a `5`. So:

| ID | intended | actually returned |
|---|---|---|
| `B5-0005` | 5 | 5 |
| `B5-0202c` | 202 | **5** |
| `B5-0329a` | 329 | **5** |
| `B5-0621` | 621 | **5** |

Every ID scored 5. The sort degraded to file order, and **all five regression cases
still reported `crash=False`**. The crash I was sent to fix was gone; nothing was red;
and task delivery across the entire queue would have been silently mis-ordered.

I caught it because I ran a direct unit probe of the sort key *before* trusting the
regression suite, and the suite's first case — "numeric sorts before suffixed" —
returned the suffixed row. The fix is anchored:

```powershell
$m = [regex]::Match([string]$Id, '^B5-(\d+)')  # correct
```

The general shape of this trap: **a bug that removes a crash can hide inside the
test that was written to prove the crash is gone.** Every one of my regression cases
asserted `crash=False`; not one of them asserted the *value* of the key. Green tests
that only check the absence of the original symptom will happily pass a
replacement that is worse.

The B5-0624 regression suite therefore checks the picked task ID, not just the
absence of a crash — which is how the second case caught what the first could not.

## Verification

Unit probe of the corrected key, eight inputs:

```
B5-0005 -> 5      B5-0202c -> 202    B5-0329a -> 329   B5-0621 -> 621
B5-367  -> 367    B5-0370a -> 370    B5-1234x -> 1234   nonsense -> 0
```

Four-row sort returns exactly `B5-0005, B5-0007, B5-0202c, B5-0329a`.

Six regression cases, each against an **isolated synthetic ledger** in a temp dir —
the live ledger is never a fixture:

| case | result |
|---|---|
| suffixed `OPEN` alone → offered, no crash | PASS |
| numeric sorts before a higher-numbered suffixed row | PASS |
| suffixed row inside the engine lane range → serialised correctly | PASS |
| suffixed `DONE` → never offered | PASS |
| duplicate detector still fires on a suffixed duplicate | PASS |
| live ledger `-DryRun` → exit 0, same head task as before | PASS |

Unchanged, as required: the tolerant row regex, the prereq and lane tables, the
B5-0622 duplicate-ID assertion, and every heartbeat file. No `src/` or `resources/`
edit.

**Reusable lesson:** when a fix removes a symptom, assert the *value* the code
produces, not the absence of the error. An unanchored regex is the standing trap —
it matches the first digit run, and identifiers here all begin `B5`, so the prefix
supplies a digit that silently wins.
