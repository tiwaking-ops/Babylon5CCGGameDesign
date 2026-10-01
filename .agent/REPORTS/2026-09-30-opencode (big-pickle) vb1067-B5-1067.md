---
document:
  title: "B5-1067 close-out report — exact set gate for the validate-heartbeats declared red"
  status: "Report"
task_id: B5-1067
agent_id: "opencode (big-pickle) vb1067"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle-free"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# B5-1067 — exact set gate for the `validate-heartbeats` declared red

## What was wrong

B5-1019 made the battery compare each instrument's exit code against a *declared
expected red*. For `validate-heartbeats` that declaration was a single scalar:

```
StandingObserved = 1
```

and the gate was `$observed -ne $ExpectedRed[$Name].StandingObserved`.

A scalar cannot see inside a red. The instrument stayed at exit `1` whether the
store held the nine non-conforming files that were declared, or ninety-nine, or
none of them. `StandingObserved = 1` was a statement that *a* red was expected, not
that *this particular* red was the expected one, and the battery reported the
difference as agreement.

Measured on a TEMP copy of the live store (B5-1019 baseline reproduced first):
`9` non-conforming files and `3` colliding `agent_id`s, exit `1`. Injecting `5`
broken files into the **live** tier: `14` non-conforming, exit **still** `1`. The
old gate read both as declared. Only the exit-code column moved; nothing could.

## What I changed

Only `.agent/tools/run-verification-battery.ps1`.

- Declared the red as a **set**: `StandingNonConforming` (9 names) and
  `StandingCollisions` (3 `agent_id`s), instead of one exit code.
- Added `Get-HeartbeatInventory`, which reads the instrument's own `-Json` output
  and returns rows, non-conforming names, colliding `agent_id`s, exit code, and
  readability.
- Added `Get-HeartbeatSetGate`, which compares the observed inventory to the
  declared one in **both directions** and returns human-readable divergence
  strings. A name observed but not declared, and a name declared but no longer
  observed, are each a divergence. B5-1020's conservatism applies: the gate
  reports more, never less.
- `Test-Instrument` gained `-SetGate`; `Show-Verdict` prints the divergences.
  The scalar `StandingObserved` check is retained as an additive fast path, so a
  set comparison can never convert a genuine code change into agreement.
- `-HeartbeatDirectory` for pointing the battery at a fixture store.

Unchanged: every instrument exit code, threshold, TTL and verdict; the battery's
`0` GREEN / `1` RED / `2` layout-broken contract; `validate-heartbeats.ps1`;
`ledger-query.ps1`; all of `b5ccg/src`; card data. No self-healing, no commit,
no push.

## Findings, in order of severity

### 1. The console codepage silently destroys non-ASCII filenames — repo-wide

The first set-test run flagged dozens of files I knew to be conforming, and
reported one name as both present and absent at once. The cause was not the
comparison logic:

`[Console]::OutputEncoding` on this host is **ibm850**. On ibm850, a non-ASCII
filename is **lost, not merely mistranslated**, in transit through a native
command's stdout. Measured on the same instrument in the same session:

```
default (codepage 850):  solar-pro4?free.json   cps=[]          <- U+F03A gone
forced UTF-8:            solar-pro4?free.json   cps=[U+F03A]   <- intact
```

A lost codepoint is also a lost `agent_id` — the lookalike exists precisely to
give one identity a different stem. This is not specific to my row: **any
PowerShell tool in this repo that captures native command output silently
corrupts the U+F03A filenames that the 17-component B5-1066 manifest is built
from**, and will report a name that does not exist on disk.

The battery now pins `[Console]::OutputEncoding` to UTF-8 before any native
invocation, prints an `encoding :` receipt line, and — because a battery that
cannot measure should say so rather than guess — `Get-HeartbeatSetGate` reports
a divergence when the pin failed. Pinning failure is deliberately **not** fatal;
making it exit 2 would report a layout break for a condition still measurable.

**This deserves its own row.** The repair I shipped is local to the battery, and
a per-tool patch does not fix the class.

### 2. `ConvertFrom-Json` hands back a top-level array as ONE pipeline object

```
$prefix | ConvertFrom-Json | Where-Object { $null -ne $_ }   # rows = 1
$prefix | ConvertFrom-Json                                    # enumerated, rows = 109
```

Filtering *inside* that pipeline passed the whole array through as a single row:
`Rows = 1`, and `Verdict` became an `Object[]` of all 109 verdicts, so every
`$_.Verdict -ne 'CONFORMS'` test behaved nonsense. Normalising after assignment
is the fix. The same line also had to handle the empty store, where
`ConvertFrom-Json` returns `$null` and `@($null)` is a **one-element array** — an
empty store would otherwise have read as one conforming row, i.e. green.

### 3. `validate-heartbeats.ps1 -Json` is not parseable as JSON

The collision banner (lines 252-260) is written after the JSON array, outside the
`if ($Json)` branch, so `-Json` output ends in non-JSON text:

```
ConvertFrom-Json : Invalid JSON primitive: == IDENTITY COLLISIONS (one agent_id claiming multiple files) ==
```

The consumer must slice the prefix before the first `^===` line. I did not edit
the instrument — out of scope — but the battery now depends on this, and any
future consumer will hit it. Logged for its own row; a parse failure is treated
as RED, never green.

## Proof

`validate-heartbeats` is deliberately registered under its **real declared name**
in the self-test, not a fixture name. That is what makes it differential: the
scalar check sees observed `1` against declared `1` and passes, exactly as it
would in production, so the **only** thing that can turn the row red is the set
gate. A fixture name absent from `$ExpectedRed` would have gone red on the scalar
branch too and would have proved nothing.

The proof states that differential in its own output and warns if it does not
hold, so it cannot degrade into a green stub:

| Path | Input | Exit | Verdict |
|---|---|---|---|
| main | live store, 109 rows / 9 non-conforming / 3 collisions | `0` | GREEN, no divergences |
| `-SelfTest` | duplicate-ID fixture in `%TEMP%` (scalar proof, unchanged from B5-1019) | `1` | RED |
| `-SelfTestSet` | TEMP copy + 5 broken live-tier files, `14` non-conforming | `1` | RED, exactly the 5 injected |
| `-HeartbeatDirectory` | clean TEMP store, `0` non-conforming, `0` collisions | `1` | RED, all 12 declared entries reported resolved |

```
differential: observed 1 vs declared 1 -> scalar-only verdict would be GREEN
differential holds: scalar-only GREEN, so the RED below is attributable to the set gate alone
```

The last row is the reverse direction, and it is the half a scalar can never
provide: when the declared inventory stops matching, the gate says so and asks
for re-declaration instead of going quietly green.

## Reusable lesson

**A set comparison is only as trustworthy as the strings it compares — verify the
transport before trusting the predicate.** This gate was correct, exercised on
real data, and still wrong, because the console codepage ate a codepoint between
the instrument and the comparison. When a comparison reports something
impossible (one name both present and absent), suspect the encoding and the
parsing before the logic, and pin the encoding to UTF-8 before the first native
invocation rather than at the point of use. Related: `ConvertFrom-Json` in PS 5.1
delivers a top-level array as a single pipeline object, so normalise after
assignment, never by filtering inside the pipe.
