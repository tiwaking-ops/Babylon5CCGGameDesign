---
document:
  title: "Report: B5-0753 released as an orphan claim; premise measured void"
  status: "ORPHAN-RELEASED (row closed BLOCKED by solar-pro4:free, not edited by me)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
assessor_llm:
  - {name: "Cline (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-28"}
---

# B5-0753 — orphan claim released, premise measured void

**Agent:** Cline (space-bunny-free)
**Claimed:** 2026-09-28T05:29:00Z UTC
**Released:** 2026-09-28T05:29:29Z UTC (orphan — row closed by another agent)
**Ledger rows edited by me:** none
**Outcome:** released without work. Two independent findings recorded.

## 1. The race (4 seconds)

| time (UTC) | event |
|---|---|
| 05:28:5x | I re-read ledger line 908: still `OPEN` |
| 05:29:00Z | I created `.agent/CLAIMS/B5-0753.json` with real current UTC |
| 05:29:03Z | `solar-pro4:free` wrote their report `.agent/REPORTS/2026-09-28-solar-pro4-free-B5-0753.md` |
| 05:29:04Z | They wrote ledger line 908 `OPEN` -> `BLOCKED`; ledger mtime moved under my live claim |

`.agent/CLAIMS/README.md`: a claim on a row that is no longer `OPEN` is an
**orphan** — release it, do not work it. I deleted only my own claim file
(`Test-Path` False afterwards) and left their row and their report
byte-identical. Both writers held a correct reading of the row; neither could
see the other. No corruption resulted, because a ledger write is a whole-row
replacement keyed on line number rather than an append, so the second writer
overwrote cleanly instead of interleaving.

## 2. The finding that outranks the race: the premise was already false

The row asked for exactly seven row renumbers — "seven IDs at count 2 each"
across the 0729..0741 seed wave. Measured on this tree, before and after the
concurrent write, identically:

```
run-dup-census.ps1 -> duplicate-ID census: PASS (0 duplicate task IDs)   exit 0

B5-0729 count=1   B5-0731 count=1   B5-0733 count=1   B5-0735 count=1
B5-0737 count=1   B5-0739 count=1   B5-0741 count=1
```

All seven at `pipeCount 7` / `doubleLead no`. **There was no collision left to
renumber.** The `QUEUE 0765..0769` narrative in the same ledger names the
mechanism: a byte-exact truncation of exactly the eight Buffy lines in that
wave. Two earlier close-outs had already flagged this row as carrying a stale
premise and left it byte-identical for its own claimant (B5-0773; the B5-0751
entry). The measurement confirms both.

## 3. On the BLOCKED reason — recorded, not corrected

`solar-pro4:free` blocked on "B5-0751 reads VOID, not DONE". That is a literal
reading of the gate text and defensible. But the gate cell names its own
purpose — "so ledger writes serialize one writer at a time" — and a `VOID` row
is closed and unclaimed, so serialization was fully achieved. Under the
semantics this repo already recorded in the B5-0751 close-out ("`BLOCKED`
means a precondition is missing, `VOID` means there was never anything to
do"), the honest status is `VOID`: the gate was never red, the target had
evaporated.

I did not rewrite their row to say so. The row is closed and not mine; the
non-adjacent and byte-identical rules exist to stop exactly this kind of
well-meaning edit. This is filed as a finding for a human or a future claimant.

## Gates re-proven (JDK 1.8.0_292)

- `run-dup-census.ps1` — `PASS (0 duplicate task IDs)`, exit `0`.
- `ledger-query.ps1 -Status "*"` — `B5-0753 | BLOCKED | 7 | no | UNCLAIMED |
  reportable`; `B5-0751 | VOID | 7 | no`. `CLAIMS-FIRST` footer suppressed
  B5-0481 / B5-0699 / B5-0737 as NOT a defect report, so no defect was filed
  against any of them.
- `validate-heartbeats.ps1` — exit 0 at boot: 30 files, 30 conforming, 30
  distinct `agent_id`, 0 collisions; my own file reads `CONFORMS`.
- Ledger bytes — 927 LF, 0 CR, no BOM (head `45,45,45,10`), 811094 bytes.
  No line ending was disturbed: I made no whole-file rewrite.
- `compile.bat` **not run** — this task changed no `src/` file, and the working
  tree carries three uncommitted `b5ccg/src` edits from other agents
  (`ai/AIPlayer.java`, `ui/GameBoardPanel.java`, `ui/MainWindow.java`), so a
  red or green reading would not be attributable to me.

## 3b. A second live finding: the heartbeat store is now colliding

The post-write validator re-run came back **exit 1** — 31 files, 30 conforming,
1 non-conforming, **1 identity collision**: `solar-pro4:free` claimed by both
`solar-pro4-free.json` (mtime 05:15:42Z) and a second file whose stem uses a
*different* colon code point than ASCII `:` (mtime 05:29:03Z). That is the same
second `solar-pro4:free` filed their B5-0753 report, so the agent that raced me
on the ledger row also dropped a second heartbeat file immediately afterwards.

Out of scope and not acted on: it is a foreign heartbeat and the schema README
forbids editing another agent's file. Unlike B5-0773's `me-so-poor.json.bak`,
both files here are `.json` and both parse, so the `*.json` glob cannot
distinguish them — only the validator's cross-file `agent_id` uniqueness check
catches it. The generalisable point is the **name**: the sanitisation rule is
only as good as the exact code point used to build the filename, and a
lookalike colon passes every constraint Windows imposes while yielding a
different stem that resolves to the same `agent_id` with no registry entry.
Both files left byte-identical; this wants its own row.

## Footprint

`docs/DECISIONS.md` (one appended entry + one assessor `passes` increment on my
own existing entry, per AGENTS.md §1a), this report, one new pattern file, my
own heartbeat, and one claim file created and deleted. No ledger row, no
`src/`, no `b5ccg/`, no tool file. No other agent's claim, heartbeat, row or
report touched. **No commit** — this loop does not commit.

## Reusable lesson

A four-second window is still a window: re-reading the row and then writing the
claim are two steps, and any other writer can land between them, so the orphan
check must happen again *after* the claim exists, not only before it.
