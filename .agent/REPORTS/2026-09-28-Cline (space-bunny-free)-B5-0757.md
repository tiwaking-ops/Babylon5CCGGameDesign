---
document:
  title: "B5-0757 close-out: heartbeat-store health report (report only, no edits)"
  status: "Report (no authority per AGENTS.md section 3)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-28"
---

# B5-0757 - Heartbeat-store health report

Claim `.agent/CLAIMS/B5-0757.json`, `started_utc 2026-09-28T05:32:41Z`, TTL 30.
The claim file was absent before I created it, and the row was re-read
immediately before and after claiming and read `OPEN` both times (ledger line
910). It was never a DONE/VOID/BLOCKED row, so the claim was not an orphan.

## Gate

`gated- none, claimable immediately`. Nothing was gated, and the gate, once
exercised anyway, was green:

| Gate | Reading |
|---|---|
| `b5ccg/compile.bat` (JDK `1.8.0_292`, `-source 6`) | **exit 0**, 1 benign warning (`bootstrap class path not set in conjunction with -source 1.6`) plus the `unchecked` note |
| `run-dup-census.ps1` | `PASS (0 duplicate task IDs)`, exit `0` |
| `ledger-query.ps1 -Status "*"` | `B5-0757 / OPEN / 7 / no / UNCLAIMED` pre-claim, `LIVE` under my own claim post-claim |

`compile.bat` was run even though the row's scope is report-only. Three
uncommitted `b5ccg/src` edits by other agents are in the tree
(`ai/AIPlayer.java`, `ui/GameBoardPanel.java`, `ui/MainWindow.java`), so a red
reading would not have been attributable to me; a green reading still had to be
recorded rather than assumed, because the loop's own lesson (B5-0771) is that a
gate which never ran reads identically to one which passed.

## The store, measured

`validate-heartbeats.ps1`, **exit 1**: 31 files, 30 conforming, 1 non-conforming;
30 distinct `agent_id`, **1 identity collision**.

The single NON-CONFORMING file and the single identity collision are **the same
file**, and it is not the one this row names:

    solar-pro4<U+F05A>free.json    agent_id "solar-pro4:free"    NON-CONFORMING (3 issues)

It collides with `solar-pro4-free.json`, which carries the *same*
`agent_id: "solar-pro4:free"`. Three findings on the colliding file: missing
`schema_version` (pre-schema), `updated_utc` instead of canonical `utc`, and the
free-text `compile_state` / `conformance` / `smoke` keys. Its `state` is the
free-text string `BLOCKED`, which is not one of the three legal enum values.

### The row's premise is stale - and the name is the finding

The row asks me to name **`me-so-poor.json.bak`** as the single non-conforming
file and the **`me-so-poor`** collision across "the live file and its backup".
Neither exists in the store any more. `me-so-poor.json.bak` was moved to
`.agent/HEARTBEATS/_quarantine/me-so-poor.json.bak` by `opencode (big-pickle-free)`
under **B5-0773**, which closed at `2026-09-27T18:58Z` and took the store to
exit 0. `README.md` is now the only non-JSON file in the store root. The row I
closed names a file that has not existed for hours.

**The interesting part is the mechanism, and it is a different bug from the one
B5-0773 fixed.** B5-0773 quarantined a *backup artifact*: a stale extra file
that a `*.json`-shaped tool could not ignore. This one is two files that are
**both well-formed `.json`**, both parsed, both reported as resolving, and both
carrying one `agent_id`. No glob, no extension filter and no parse-success test
can separate them. Only the validator's cross-file `agent_id`-uniqueness check
catches it, and the one thing that produced it is a **filename**.

The colliding filename is `solar-pro4` + **U+F05A** (a Unicode Private Use Area
code point, decimal `61498`) + `free.json`, decoded from the git status escape
`\357\200\272` = `EF 80 BA` = U+F05A. U+F05A is a **private-use colon
lookalike**: it satisfies every filename constraint Windows imposes (`:` and `/`
are the two forbidden characters, and this is neither) while producing a stem
that is not `-`, so it is not what the sanitisation rule in `AGENT_LOOP.md`
prescribes. `_registry.json` maps `solar-pro4:free` to `solar-pro4-free.json`
only, so the lookalike resolves to the same `agent_id` through **no registry
entry at all**: the collision is invisible to the registry precisely because the
registry has nothing to say about it.

The store also contains a `.agent/PATTERNS/solar-pro4<U+F05A>free/` directory
(two pattern files in it), which is the same misspelling propagated into a
second directory. **Scope note:** I have not edited, rewritten, moved or deleted
either heartbeat, or that pattern directory, or anything else in the store.
`.agent/HEARTBEATS/README.md` says *never edit another agent's file*, and this
row's own scope says the same. Both files are left byte-identical; the SHA256 of
the colliding file is
`6936A5C7C0EFEFA3B82C68E7EDCCCAB9C51A8EEE0888AF441D4E4D8402502FF9` for whoever
takes the fix.

**The generalisable hazard is the sanitisation rule itself, not this file.**
`solar-pro4:free` is cited in 114 ledger rows and has now produced three
filenames in the same store: `solar-pro4-free.json` (correct), the U+F05A
lookalike, and in `_quarantine/` a bare `solar-pro4` directory and a
`solar-pro4.json`. The rule "sanitise only `:` and `/` to `-`" is enforced
nowhere; it is a convention, and a writer who picks a visually similar code
point rather than ASCII `-` complies with every mechanical constraint and
violates the rule silently. A mechanical check - every heartbeat filename's
non-ASCII code points lie outside the Private Use Area - would have caught this
at write time. **That needs its own row and a human; it is not mine to seed on
the back of a report-only task.**

## Three-signal state of every claim fresh at claim time

Computed per `.agent/HEARTBEATS/README.md` (newest of claim `started_utc` or
mtime, owner heartbeat mtime, report mtime), TTL 30 min. I reaped nothing and
touched no foreign claim.

| Claim | Owner | claim (min) | heartbeat (min) | report (min) | Verdict |
|---|---|---|---|---|---|
| B5-0757 | Cline (space-bunny-free) | 0.3 | 1.9 | none | **LIVE** (mine) |
| B5-0755 | Buffy (glm-5.3-flash) | 2.8 | 2.6 | none | **LIVE** |
| B5-0699 | me-so-poor | -237 (future) | 3.5 | 1.4 | **LIVE** on two signals |
| B5-0481 | solar-pro4 | 333 | **ABSENT** | 3144.5 | **UNKNOWN** |
| B5-0737 | solar-pro4:free | -141.7 (future) | **ABSENT** | none | **UNKNOWN** |

Two claims carry a `started_utc` in the **future** (B5-0699 by 237 min, B5-0737
by 142 min, both clock-skewed writers). A naive age computation returns a
negative number, which compares as *younger* than any TTL and would render both
LIVE. That is the B5-0609 failure class arriving through a different door: a
future timestamp is not evidence of liveness, it is evidence of a wrong clock.
**Both are LIVE only because a second independent signal happened to be fresh,
not because the claim timestamp supported it.**

**B5-0481 and B5-0737 are `UNKNOWN`, not STALE, and are not reapable.** Both
owners' heartbeat files are **ABSENT** under their exact `agent_id`: B5-0481's
owner is spelled `solar-pro4` and no such file exists; B5-0737's is spelled
`solar-pro4:free`, whose only files are the two colliding ones, neither of which
sits at `<agent_id>.json`. An absent signal is `UNKNOWN`, never STALE, and a
claim may be reaped only when all three signals are STALE. I therefore did not
reap them, even though B5-0481's claim is 333 minutes old and its report is 52
hours old. This is exactly the trap the README documents: a lookup matching no
heartbeat file must return UNKNOWN with its reason, never an age of `-1` or `0`.

Note the causal link between the two halves of this report: **B5-0737 is
UNKNOWN precisely because of the U+F05A collision.** An owner whose `agent_id`
contains a `:` cannot produce `<agent_id>.json` on Windows, so the heartbeat
signal can never resolve for that owner. The naming defect does not merely add a
spurious file, it makes its own owner's claim permanently unreapable.

## Footprint

- One ledger row: line 910, `OPEN` to `DONE`, 7 pipes, `doubleLead no`, no `|`
  character inside any note cell.
- `docs/DECISIONS.md`: one appended entry, plus my existing assessor entry
  incremented `passes: 2` to `3` with `last_pass: 2026-09-28` per `AGENTS.md`
  section 1a (no new entry - same name **and** version).
- `.agent/PATTERNS/Cline (space-bunny-free)/2026-09-28-a-report-row-names-files-and-the-world-moves-them.md`.
- Own heartbeat refreshed; my claim deleted. Ledger verified after write:
  0 CR, no BOM (head bytes `45,45,45,10`).
- No `b5ccg/src` edit, no tool edit, no claim-file edit, no foreign heartbeat,
  claim, row or report touched. **No commit.**

Ledger line count reads 742 on this tree against the 926/927 quoted in the
2026-09-28 B5-0753 entry; the file was rebuilt from backup since (ledger
frontmatter line 16 records a `B5-0733` restore). Line numbers here are
current-tree readings.

**Reusable lesson:** a report-only row is still a claim about the present, and a
row written hours before the work names files that other agents move underneath
it. Re-verify every name the row asserts immediately before reporting it, and
report the divergence as the finding rather than paraphrasing the row back.
