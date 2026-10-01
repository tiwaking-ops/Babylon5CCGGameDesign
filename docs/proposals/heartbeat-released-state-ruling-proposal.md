---
document:
  title: "The `released` heartbeat state: a ruling, and why the schema is not amended"
  status: "Proposal (candidates only, never truth until merged + compiled)"
provenance:
  author_llm: {name: "Cline", version: "space-bunny"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline", version: "space-bunny"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0951"
---

# The `released` state: ruling and no-amendment

## The question B5-0951 asks

`.agent/HEARTBEATS/Cline (space-bunny) b5-0941.json` carries `state: "released"`,
outside the `active | idle | busy` enum that `.agent/HEARTBEATS/README.md` binds and
that `.agent/tools/validate-heartbeats.ps1` enforces. `validate-heartbeats.ps1` exits 1
on exactly this one file (60 files, 59 conforming, 1 non-conforming, 0 identity
collisions).

The row deliberately does not assume the file is wrong. It asks whether `released` is a
real lifecycle state the schema is missing, and requires the branch taken to be stated
with its reason. This document records the ruling: **the schema is not amended, and the
file is not edited.**

## Measurement 1 — `state` has no behavioural consumer

Every executable reader of a heartbeat in this repository was enumerated. `.agent/`
holds 15 script files; a recursive search for `state` across all of them returns three
kinds of hit, and in heartbeat-payload terms only these:


## Measurement 2 — the fact `released` denotes is already carried, in the right field

The offending file, read whole:

```json
{ "schema_version": 1, "agent_id": "Cline (space-bunny) b5-0941",
  "utc": "2026-09-28T22:40:00Z", "state": "released",
  "current_task": null, "live_claims": [], "javac": "1.8.0_292", "notes": "B5-0941 CLOSED..." }
```

"I have released my claim and hold nothing" is expressed **twice already, and correctly**:
`live_claims: []` — the positive assertion that the agent holds nothing, which the
binding README calls the single most important field — and `current_task: null`. Both are
in enum-free, machine-readable form. The information content of `released` is therefore
**redundant**, not missing. There is no fact that only `released` can express.

## The ruling: branch 2, the file is non-conforming

Three findings converge on the same answer.

1. **No expressive gap.** Had the author wanted to record claim release and the schema
   permitted it, `live_claims: []` + `current_task: null` already does so. The gap
   branch rests on — "a real state the schema cannot express" — is not present.
2. **The `busy` precedent does not transfer.** `busy` was added to this enum by human
   approval (Amendment A1, B5-0623) after a *live working* agent wrote it, because
   `active` and `busy` were genuinely indistinguishable and a working agent was being
   reported invalid. The rule recorded there is "a schema that a working agent cannot
   satisfy is a schema that gets abandoned." `released` is the opposite situation: the
   file's own payload is fully schema-satisfying, and the non-conforming token sits in
   the one field where the payload already said the same thing correctly.
3. **The row forbids the forbidden remedy.** B5-0951 states: do *not* widen the enum
   merely to make the validator green without recording the lifecycle meaning, and do
   *not* edit a heartbeat you do not own. Since Measurement 1 shows widening has no
   functional effect, widening here would be a pure cosmetic purchase of a green exit
   code — the exact trade the row names and refuses.

**Therefore: no amendment to `.agent/HEARTBEATS/README.md`, no change to
`$ValidState` in `validate-heartbeats.ps1`, and no edit to
`Cline (space-bunny) b5-0941.json`.** The file's owner, or a human, may set
`state: "idle"`, which is conformant and loses nothing, since the claim-release fact
lives in `live_claims` and `current_task` either way.

## Finding 2 (out of scope, reported not fixed): the migration tool's enum has drifted

`migrate-heartbeats.ps1` l.55 carries its **own** copy of the enum:

```powershell
$ValidState = @('active', 'idle')            # migrate-heartbeats.ps1:55
$ValidState   = @('active', 'idle', 'busy')  # validate-heartbeats.ps1:59
```

The migration tool was not carried along by the `busy` amendment. Its l.199 coerces any
value outside its own list, defaulting to `idle` (l.201). So **running
`migrate-heartbeats.ps1` for real would silently rewrite every `busy` heartbeat in the
store to `idle`** — the 2 files currently carrying `busy` today — and would silently
normalise this `released` file to `idle` as well, which is precisely the edit no agent is
authorised to make by hand. This is the B5-0623 lesson recurring in the same directory:
*two copies of a list is one copy too many.*

`migrate-heartbeats.ps1` is **not** in B5-0951's claimed scope, so it is reported here and
left untouched. It deserves its own row: the fix is to delete the second copy and read
the canonical enum, or to have the tool refuse to run when the two disagree.

## Finding 3: this row's success criterion is unreachable inside its own scope

B5-0951's title asks to "resolve the one non-conforming heartbeat so
`validate-heartbeats.ps1` can exit 0." Measured, the validator **still exits 1** after
this pass, and cannot be made to exit 0 by any action B5-0951 authorises:

* editing the offending file is forbidden (foreign file, and the README says never edit
  another agent's file);
* widening the enum is forbidden without recorded lifecycle meaning, and Measurement 1
  shows there is no lifecycle meaning to record.

The residual red is therefore **correct output, not an unfinished task**. A green
validator here could only have been bought by one of the two forbidden actions. This
residual is stated plainly in the ledger note and the report so that the next agent does
not re-open the row believing there is a permitted fix left.

## Reusable lesson

Before widening an enum to make a validator green, find out **what reads the value**. A
field with no behavioural consumer is documentation, and the cost of admitting an
undocumented value is a permanently unenforceable enum.


| Reader | Use of `state` |
|---|---|
| `validate-heartbeats.ps1` | enum membership check (l.145) + a display column (l.201) |
| `migrate-heartbeats.ps1` | coercion input (l.199) — see finding 2 |
| `run-queue.ps1` | **never reads it**; liveness from claim `started_utc`/mtime |

`run-queue.ps1` and `ledger-query.ps1`, the two tools that actually decide who owns
what, derive liveness from the **three-signal rule** — claim `started_utc`/mtime,
owner's heartbeat mtime, report mtime — and from the `live_claims` array. Neither
branches on `state`. So `state` is a **human-facing documentation field**, not a
machine-decision field.

This is the load-bearing fact. Widening the enum to admit `released` would change **no
verdict produced by any tool in this repository.** The only thing it would change is
`validate-heartbeats.ps1`'s exit code — that is, it would make the validator stop
reporting a file it has correctly identified as out of contract.
