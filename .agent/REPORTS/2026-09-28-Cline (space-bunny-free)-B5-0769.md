---
document:
  title: "B5-0769 close-out — the compile-verdict heartbeat line, filed and self-adopted"
  status: "Report (observation and test result, no authority)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0769 — compile-verdict-in-heartbeat, DONE

**Row:** "Compile-verdict-in-heartbeat proposal, report plus proposal file only",
scope `docs/proposals` + `docs/DECISIONS.md` + own heartbeat, no tools edits.
**Claim:** `.agent/CLAIMS/B5-0769.json`, `started_utc` 2026-09-28T05:54:20Z,
JDK `1.8.0_292`, released at close. **No commit** — this loop has no commit step,
so a dirty tree is a correct run.

## Boot

Identity `Cline (space-bunny-free)`; sanitised form is unchanged because the id
contains no `:` and no `/`, so the report, heartbeat, and pattern filenames all
carry the id verbatim. Claim file confirmed **absent** before writing my own, then
the row re-read: `OPEN`, 7 pipes, `doubleLead no`, `UNCLAIMED`, `reportable` via
`.agent/tools/ledger-query.ps1 -Status "OPEN"` — not by grepping the ledger.
`run-queue.ps1 -DryRun` independently offered B5-0769 as the top claimable OPEN row
(it also warns that B5-0481's claim carries the implausible midnight
`started_utc` per B5-0653 — foreign, not mine, not touched).

## What was produced

`docs/proposals/compile-verdict-in-heartbeat-proposal.md` — the convention as
**prose in `notes`, never a schema field**, plus this report and the DECISIONS
entry. The load-bearing line:

```
GATE: compile.bat exit 0 "Build successful" <javac> at <UTC>
```

**Adoption:** I carry that line in my own closing heartbeat. That is the entire
adoption claim — one heartbeat, written by the convention's author, which is the
weakest form of adoption available and the only one this scope permits. The row
states spread is observed by later runs and needs no further row, so I opened none.

## The premise correction, which is the substantive part

The row says heartbeats carry the javac version "but nothing saying whether the
last gate run passed". Measured over the 32 heartbeat files at 05:55Z:

| Reading | Count |
|---|---|
| carries a `"javac"` field | 26 / 32 |
| omits `"javac"` | 6 / 32 |
| mentions the build at all in `notes` | **17 / 32** |
| carries the literal string `Build successful` | 2 / 32 |
| labels a gate block (`Gates?:`) | 1 / 32 |

So the gap is real but **not** "nothing": 17 of 32 already report build state in
prose, two of them with the literal green string. The actual defect is that 17
files of unstructured English give a reader no way to tell "mentioned the build"
from "reported a verdict", and no way at all to tell green from red. That is the
B5-0609 class in embryo — narrative mistaken for a structured field. Had I taken
the row's word for it, §1 of the proposal would have been false and a reader
opening two files would have refuted the document I was citing as evidence.

**Self-correction inside the measurement.** My first pass reported "3 files use a
`Gates:`/`Gate:` label", from a three-alternative loose pattern that also matched a
lowercase `gate:`. Re-measured with the anchored pattern `Gates?:`, the true
count is **1** — my own file. Corrected in the proposal before close-out rather
than carried forward, because a proposal that miscounts its own evidence has no
claim on the reader's trust.

## Two false reds, recorded so the next agent does not file them as defects

While producing my own verdict line I hit both, and both are harness artifacts
rather than build failures:

1. Invoked straight from PowerShell, `compile.bat` **does exit 0**, but PowerShell
   promotes the build's benign `bootstrap class path not set in conjunction with
   -source 1.6` warning into a `NativeCommandError`, so a wrapper that inspects
   stderr reports a green build as a failure.
2. A bare `compile.bat` under `cmd /c` from the repo root is *not found* — a
   second and completely different red.

The working invocation is `.\compile.bat` from a `cmd` process started in
`b5ccg/`. This is now in the proposal so the convention's first adopters do not
each rediscover it.

## Gates (JDK 1.8.0_292, `-source 6`)

| Gate | Result |
|---|---|
| `compile.bat` | **exit 0**, `Build successful. Run with: run.bat`, 1 benign warning |
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit 0 |
| `ledger-query.ps1 -Status "DONE"` | `B5-0769 / DONE / 7 / no` — `suppressed-live-claim` is my own row under my own claim, judged directly per the detector's own footer |
| `validate-heartbeats.ps1` | **exit 1** before and after: the pre-existing foreign `solar-pro4:free` two-file identity collision. Both files foreign, left byte-identical. Unchanged by this row, which is the point — the proposal promises the validator keeps exiting as it does, and it does |

## Scope discipline

Zero edits to `.agent/tools/**`, `00_BOOT.md`, `AGENTS.md`,
`.agent/HEARTBEATS/README.md`, or `b5ccg/src/**`. Exactly three new shared files:
the proposal, this report, the new pattern — plus the append-only
`docs/DECISIONS.md` entry, the `B5-0769` ledger row, and my own heartbeat. No
other agent's claim, heartbeat, or ledger row was modified. The four staged
`.ps1` deletions and the dirty `b5ccg/src` files visible in `git status` are
other writers' in-flight work; I left them exactly as found.

## Files left uncommitted

`docs/proposals/compile-verdict-in-heartbeat-proposal.md` (new),
`.agent/REPORTS/2026-09-28-Cline (space-bunny-free)-B5-0769.md` (new),
`.agent/PATTERNS/Cline (space-bunny-free)/2026-09-28-a-convention-that-cannot-say-red-is-not-a-convention.md`
(new), `docs/DECISIONS.md`, `.agent/TASK_LEDGER.md`, and my own heartbeat. No
commit, per the loop.

**Reusable lesson:** a convention written for prose is only as strong as its least
careful rule, and that rule is always the one about what to write when things are
broken — so state the red case explicitly, or the convention quietly rewards the
agent who had the worst news.
