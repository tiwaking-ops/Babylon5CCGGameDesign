---
document:
  title: "Autonomous agent loop — operating procedure"
  status: "Operating procedure (not governance; .agent/00_BOOT.md and AGENTS.md win on conflict)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm:
    - {name: "opencode (big-pickle-free)", version: "big-pickle-free", passes: 1, last_pass: "2026-09-27", note: "edit: qualified the bare .agent/00_BOOT.md in the status line, the string an unattended agent copied verbatim and resolved against the repository root (B5-0693)"}
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# AGENT_LOOP

The operating procedure for a headless loop that drives this repo. Human-approved
2026-09-27. **This is a procedure, not governance:** where it disagrees with
`AGENTS.md` or `.agent/00_BOOT.md`, those win and this file is the thing that is
wrong.

```
BOOT (once per session)
  - Read AGENTS.md, .agent/00_BOOT.md (all 11 steps; the pattern skim is step 11),
    .agent/TASK_LEDGER.md, docs/DECISIONS.md.
  - Get the OPEN census ONLY from the shared tool, never by reading or grepping
    the ledger:
      powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun
  - Adopt ONE agent_id spelling and keep it for the life of the repo. A new
    spelling is a NEW agent, not a variant: two sessions sharing one spelling
    cannot see each other's in-flight intent.

IDENTITY AND FILENAMES  (the rule that breaks things)
  - Windows forbids ":" and "/" in filenames. Sanitise ONLY those two, to "-".
    Preserve every other character exactly, including spaces and parentheses.
        solar-pro4:free             ->  solar-pro4-free
        Kilo (kilo-auto/free)       ->  Kilo (kilo-auto-free)
        opencode (space-bunny-free) ->  unchanged
  - Use the SANITISED form in every filename: heartbeat, report, pattern. Keep
    the true id in the file's "agent_id" field; _registry.json maps the rest.
    One agent_id must never be claimed by two files.
  - Check the store:  powershell -NoProfile -ExecutionPolicy Bypass
    -File .agent/tools/validate-heartbeats.ps1        (exit 0 = every file conforms)

LOOP until STOP
  1. Pick the next OPEN row with no live claim. If none, seed ONE genuinely useful
     task (problem statement + acceptance criteria) as an OPEN row; self-seeding
     is permitted only as an OPEN row taken through this same normal cycle.
  2. Claim it: create .agent/CLAIMS/<task-id>.json. If the file exists it is taken
     -- take another. Absence of the file is NECESSARY BUT NOT SUFFICIENT: re-read
     the row and confirm it still reads OPEN. A claim on a DONE/VOID/SUPERSEDED/
     BLOCKED row is an orphan; release it, do not work it.
  3. Heartbeat on the binding schema (.agent/HEARTBEATS/README.md): schema_version,
     agent_id, utc, state (active | idle | busy), live_claims (always present; []
     asserts you hold nothing). notes is prose and is never parsed.
  4. Work only inside the claimed scope. Log the change in docs/DECISIONS.md.
  5. Every row you write takes a single leading pipe and exactly 7 pipes, with no
     "|" character inside any note cell. After writing, run the duplicate-id census:
       (Select-String -Path .agent/TASK_LEDGER.md -Pattern '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|' -AllMatches).Matches |
         ForEach-Object { $_.Groups[1].Value } | Group-Object | Where-Object Count -gt 1
     Empty output is the pass condition. On a collision, leave the other writer's
     row byte-identical and renumber YOURS to a NON-ADJACENT id -- never into the
     slot they just vacated, which deadlocks. A duplicate id silently drops a task,
     because status is keyed by id and the last row wins.
  6. Run compile.bat/sh (JDK 8, -source 6). If red, log the failure and STOP that
     item only.
  7. Mark DONE, then write .agent/REPORTS/<date>-<sanitised-agent-id>-<task-id>.md
     carrying one "Reusable lesson" line, filed as a NEW file under
     .agent/PATTERNS/<agent-id>/ (supersede-never-rewrite: a corrected pattern is a
     new file linking the old one).
  8. Re-boot (re-read ledger + newest patterns across ALL namespaces) before the
     next iteration.

STOP when: no OPEN row remains AND you choose not to seed; or a compile is red; or
you would reuse a task id already in the ledger; or a duplicate id appears that is
not yours to renumber. Then report the run: iterations, tasks closed, and every file
you left uncommitted. **This loop does not commit** — step 7 has no commit step and
the runner's task template has none either, so there is no commit hash to report.
Committing and pushing stay human decisions; a run that ends with a dirty tree is a
correct run, not a failed one.
```

## Why each rule is here

Every clause above traces to a recorded failure. This section exists so the
procedure can be argued with rather than merely obeyed.

| Clause | Traces to |
|---|---|
| census only via `.agent/run-queue.ps1` | B5-0613 — a hand-rolled census missed 14 rows and the runner's own single-leading-pipe regex could not see them |
| one spelling per agent, for life | B5-0337 — twin sessions under one id could not see each other's in-flight intent |
| sanitise only `:` and `/` | Amendment A1.1 — `agent_id` == filename stem is unsatisfiable on Windows; `solar-pro4:free` is cited in 114 ledger rows and cannot be changed to suit a filename |
| one id claimed by one file | A1.1 — two files asserting one identity make `live_claims` ambiguous however well-formed either is |
| claim-file absence insufficient | B5-0622 — a live agent held two claims on rows another agent had already closed `DONE` |
| row re-read must still be `OPEN` | same |
| 7 pipes, single leading, no `\|` in notes | B5-0568, B5-0611 — the leading double-pipe class recurred after being declared retired three times |
| post-write duplicate census | B5-0618 — two seeders both measured one id free in the same window; only the census caught it |
| non-adjacent renumber | B5-0618 — renumbering into the slot the other writer just vacated deadlocked; both landed on the same id |
| `live_claims` always present | B5-0609 — a reader matched task ids in a heartbeat's prose and reported 4 claims held when the array was `[]` |
| pattern skim is step 11 | B5-0614 inserted the census step and renumbered 5–11 to 6–11 |
| STOP on "no OPEN row", not "empty ledger" | the ledger accumulates forever; hundreds of closed rows is not an empty ledger |

## The three ways this loop has lied to itself

Recorded because they are all green-passing, and a procedure that only encodes the
happy path is a procedure that will encode the next mistake too.

1. **A pre-write check that races another reader is not sufficient.** "Is this id
   free?" passed for two writers simultaneously. Only a *post-write* census of
   distinct ids notices a collision.
2. **Absence of an error is not presence of a value.** Fixing the `B5-0624` crash
   with an unanchored digit regex made every id score `5`, silently degrading task
   ordering, while every regression case still reported no crash. Assert the value
   the code produces.
3. **A test never observed red is not evidence.** `validate-heartbeats.ps1` had
   never been seen to pass, because it had never been run against a conforming
   store.

**Reusable lesson:** a loop specification is a set of checks, and a check is only
worth its cost if it can fail. Every clause here names the failure that made it
necessary; a clause without a failure behind it is decoration.
