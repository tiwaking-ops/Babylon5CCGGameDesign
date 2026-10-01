---
document:
  title: "B5-0783 close-out: instance-qualified agent_id"
  status: "Report (observation, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# B5-0783 — instance-qualified `agent_id`

**Row:** B5-0783, seeded and closed in one session, self-seeded per `AGENTS.md` §6.
**Result:** DONE. One proposal, one DECISIONS entry, one ledger row, one claim, one
heartbeat, this report, one pattern. **No tool, governance file, registry entry, foreign
heartbeat or `src` file was edited. No commit.**

## What was asked

User question, 2026-09-28: *if two Agents use the same model, is the model the name used
for the files like heartbeat and such? is there a way to fix this?*

Short answer, now a written rule: **yes, the model is effectively half the filename, and
the fix is to make the identity name the *session* rather than the model.** The full
argument is `docs/proposals/agent-instance-identity-proposal.md`; the rule is R1–R6 in its
§3. This report records what was measured, what was done, and what was left undone.

## The measurement

`validate-heartbeats.ps1` at 05:34Z, **exit 1**:

```
files     : 32   conforming: 31   non-conforming: 1
identity  : 31 distinct agent_id   collisions: 1
  solar-pro4:free
      -> solar-pro4-free.json
      -> solar-pro4?free.json
```

The second stem is not ASCII. Char codes read straight off the filename:
`115,111,108,97,114,45,112,114,111,52,**61498**,102,114,101,101` — **U+F03A FULLWIDTH
COLON** in place of U+003A. `Cline (space-bunny-free)` recorded this same live collision
at the close of B5-0753 and wrote that it needed its own row; no row existed, so B5-0783
is that row.

**One correction carried forward.** The B5-0757 DECISIONS entry names this code point
**U+F05A**. A direct read of the same filename gives **61498 = 0xF03A**. Both cannot
describe the same bytes. This row measured rather than transcribed, which is the only
reason the discrepancy is visible at all — and the DECISIONS entry for B5-0783 records
the correction so the next reader does not have to re-derive it.

**Why it is a live hazard, not a cosmetic duplicate.** `Get-NormName`
(`run-queue.ps1:261`, `ledger-query.ps1:85`) keeps only letters and digits:

```
N "solar-pro4:free"        => solarpro4free
N "solar-pro4<U+F03A>free" => solarpro4free      SAME KEY? True
N "opencode (space-bunny-free) 2" => opencodespacebunnyfree2   (distinct)
N "opencode (space-bunny-free) _" => opencodespacebunnyfree   (collapsed — no discriminator)
```

`Get-HeartbeatIndex` (`run-queue.ps1:277`) holds `key -> NEWEST mtime`, one value per
key. So the two files are not two readings a human must disentangle — they are **one
key**, and the quieter instance's heartbeat is *absent from the index*. The three-signal
liveness rule is then evaluated against the wrong instance's heartbeat, and a live claim
can read `STALE` on that signal: the B5-0597 false-reap shape reached by a route nobody
has walked before. The validator's exit 1 is currently the only thing preventing it.

## What was done

1. **Seeded** row B5-0783 `OPEN` with a `QUEUE 0783` grounding note, after checking the
   ID free (`rg` zero matches) and choosing a non-adjacent odd ID per the B5-0618
   divergence rule. Verified immediately: `run-dup-census.ps1` `PASS (0 duplicate task
   IDs)` exit 0, and `ledger-query.ps1` read `7` pipes / `doubleLead no` /
   `reportable`.
2. **Claimed** atomically at `2026-09-28T05:37:10Z` after re-reading the row as `OPEN`
   (the B5-0622 absence-is-not-sufficient precondition).
3. **Wrote** `docs/proposals/agent-instance-identity-proposal.md` — the rule, the
   measurement, five rejected alternatives (status quo, retro-rename, `session_uuid`
   field, UUID filenames, runtime handles), and an explicit list of what it does *not*
   fix.
4. **Appended** the DECISIONS entry.
5. **Adopted** the rule for this session only: claim and heartbeat under
   `opencode (space-bunny-free) 2`. This needed **no `_registry.json` edit**, because the
   validator resolves exact stem match *first* (R4) — the file read `CONFORMS` on the
   first run. The prior session's `opencode (space-bunny-free).json` is left
   byte-identical and stale; R5 cuts both ways, a new instance does not get to tidy the
   old one.
6. **Filed** the pattern in the new namespace
   `.agent/PATTERNS/opencode (space-bunny-free) 2/`, because namespace = identity, and
   filing a new instance's record under the old instance's namespace would be the exact
   conflation the proposal exists to prevent.
7. **Closed** the row `DONE` with a 3608-character verification note.

## A defect in my own process, recorded not hidden

**My first close-out write was a silent no-op.** The script computed the replaced row
into `$new`, then serialised `$lines` — never assigning `$new` back into `$lines[$idx]`.
The file was rewritten byte-for-byte unchanged while the tool output cheerfully reported
`old pipes=7 new pipes=7` and `old len=1513 new len=4405`. Had I trusted that output,
B5-0783 would have been closed `OPEN` with a `DONE` note nowhere, and the next agent
would have found a row that no tool could complete.

It was caught by one thing only: re-reading the row **off disk** rather than inspecting
the variable that produced it. The check cost one command and it is now in the row's own
note, because the failure mode is general — a write that reports success and changes
nothing is indistinguishable from a write that worked, and the *only* discriminator is
reading the artefact back from the medium rather than from memory.

This is the second time this session's own namespace has produced a pattern about a gate
that reports rather than proves (`write-gates-against-a-re-read`,
`a-self-certifying-gate-clause-is-not-a-precondition`). The new record is filed as
`an-identity-must-name-the-thing-that-clobbers.md`, and the near-miss is filed inside it,
because the two share a root: a check that reads the writer's own state instead of the
world cannot fail.

## Gates (all read-only, all on the real tree)

| Check | Result |
|---|---|
| `javac -version` | `1.8.0_292` (JDK 8, as the boot step expects) |
| `b5ccg/compile.bat` | **not run** — no Java touched, so no build gate applies. Recorded rather than silently skipped. |
| `run-dup-census.ps1` after seeding | `PASS (0 duplicate task IDs)`, exit 0 |
| `run-dup-census.ps1` after closing | `PASS (0 duplicate task IDs)`, exit 0 |
| `ledger-query.ps1 -Status "*"` on my row, read from disk | `B5-0783 / DONE / 7 pipes / doubleLead no` |
| `validate-heartbeats.ps1` | 32 files, 31 conforming, **1 collision — the pre-existing solar pair, unchanged by this row**; my new file `CONFORMS`; exit 1 is the foreign pair's, not mine |
| Ledger bytes | CR 0, no BOM, LF-terminated (re-terminated by hand: a PowerShell here-string carries no trailing newline, so the append would have left the file unterminated and merged the next agent's append onto my row) |
| HEAD vs working-tree row sets | 371 → 404, **zero rows in HEAD missing from the working tree** — the read-modify-write lost no concurrent append |

## Two process notes for the next agent

**The B5-0622 race was live again, twice.** The ledger SHA256 moved between my pre-write
measurement (`E3FBA79F…`) and my append, and moved again before the close-out
(`735DB1C0…`). Both times the hash was re-taken immediately before writing. The
read-modify-write used for the close-out is a whole-file rewrite, so it has a lost-update
window that a surgical append does not; the row-set diff above is the check that it did
not fire.

**`ledger-query.ps1` reports `suppressed-live-claim` on my own row**, correctly — my claim
is live while I judge the row. Per boot step 9 that is the documented exception: your own
row, the one you just wrote under your own claim, you judge directly.

## Reusable lesson

An identity must name **the thing that clobbers**, not the thing that is convenient to
name. `client (model)` was convenient, was unambiguous on the day it was invented, and
became a silent data-loss path the moment a second session appeared — and the validator,
the schema, the registry and the liveness join all agreed it was fine until two live
files disagreed on one key.

## Cross-references

* Proposal: `docs/proposals/agent-instance-identity-proposal.md`
* Decision: `docs/DECISIONS.md`, entry `## 2026-09-28T05:40Z - B5-0783 DONE`
* Prior art: B5-0773 (quarantine a `.bak` from a live coordination directory), B5-0753
  and B5-0757 (Cline's live-collision record), B5-0597 and B5-0660 (the three-signal
  liveness rule this hazard attacks), B5-0622 (the lost-update race)
* Still open and **not mine**: the `solar-pro4:free` file pair, and a store-level
  collision check on *normalised* keys in `validate-heartbeats.ps1`
