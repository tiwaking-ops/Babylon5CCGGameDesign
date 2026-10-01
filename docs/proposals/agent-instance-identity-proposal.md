---
document:
  title: "Instance-qualified agent_id: making the coordination identity a session, not a model"
  status: "APPROVED 2026-09-28 by human ruling; promoted into .agent/HEARTBEATS/README.md (Identity) and .agent/00_BOOT.md step 3 under B5-0785; canonical via docs/DECISIONS.md B5-0785"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free) 2", version: "space-bunny-free", passes: 1, last_pass: "2026-09-28", note: "edit: status set to APPROVED and the body's status paragraph replaced after the human ruling of 2026-09-28; the argument in sections 1-7 is unchanged (B5-0785). Author and assessor are the same agent here because AGENTS.md section 1 requires a dated assessor entry when an author edits its own document."}
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Instance-qualified `agent_id`

**Status: APPROVED 2026-09-28 by human ruling, and promoted.** R1–R6 are now binding
governance in `.agent/HEARTBEATS/README.md` (*Identity: `agent_id` and filenames*) with
a pointer from `.agent/00_BOOT.md` step 3; the decision is recorded in `docs/DECISIONS.md`
as B5-0785. This document is the rationale and stays on file as such — it is no longer
the authority, the two governance files are. No tool, registry entry or existing `agent_id`
was changed, and none is required: see R4.

**Origin.** User question, 2026-09-28: *if two Agents use the same model, is the model the
name used for the files like heartbeat and such? is there a way to fix this?* Seeded and
delivered as ledger row **B5-0783** (self-seeded per `AGENTS.md` §6).

## 1. The question, answered

Yes, and the answer is worse than "the model is the filename".

The heartbeat store holds **one file per `agent_id`**, and `agent_id` is de facto the
filename stem. Nothing in the protocol defines *how* to construct an `agent_id`, so each
agent improvises one, and the improvisation in this repo has converged on
`client (model)`: `opencode (space-bunny-free).json`, `Cline (space-bunny-free).json`,
`Buffy (glm-5.3-flash).json`. The **client** half is load-bearing — it is what keeps two
agents on the same model apart. The **model** half is not a discriminator at all; it is
just the second word of the same string.

So the store's identity is *per model per client*, while the thing that actually needs an
identity is a **running session**. Two live sessions of the same client on the same model
compute the same `agent_id`, and because the store holds one file per `agent_id`, the
second session overwrites the first session's `utc` and `live_claims` on every heartbeat.
Neither session can see the other. The `HEARTBEATS/README.md` *Known unresolved* section
already names this for the opencode family; this proposal gives it a fix.

Only two coordination surfaces are keyed on `agent_id` at all — claims are keyed by
**task** (`CLAIMS/<task-id>.json`, atomic by construction) and reports by
**date + agent + task**. The blast radius is therefore the heartbeat file and the
`PATTERNS/<agent-id>/` namespace, but the heartbeat is the one the liveness join reads.

## 2. The failure, measured

`validate-heartbeats.ps1` at 2026-09-28T05:34Z, exit **1**:

```
files     : 32   conforming: 31   non-conforming: 1
identity  : 31 distinct agent_id   collisions: 1

=== IDENTITY COLLISIONS (one agent_id claiming multiple files) ===
  solar-pro4:free
      -> solar-pro4-free.json
      -> solar-pro4?free.json
```

The second filename's stem is not ASCII. Measured code points: `s, o, l, a, r, -,
p, r, o, 4, **61498**, f, r, e, e` — U+F03A **FULLWIDTH COLON**, not U+003A. It is a
lookalike punctuation character that satisfies every constraint Windows places on a
filename while producing a different stem. Cline (space-bunny-free) recorded this same
live collision in `docs/DECISIONS.md` at the close of B5-0753 and wrote that it needed its
own row; no row existed, so B5-0783 is that row.

The measurement that makes it a hazard rather than a cosmetic duplicate:

```powershell
function Get-NormName([string]$n) {   # run-queue.ps1:261, ledger-query.ps1:85
  # keeps ONLY letters and digits, lowercased; strips : / - _ . space U+2028 U+2029
}
N "solar-pro4:free"          => solarpro4free
N "solar-pro4<U+F03A>free"   => solarpro4free
SAME KEY? True
```

`Get-HeartbeatIndex` (run-queue.ps1:277) builds `key -> NEWEST mtime` and keeps one value
per key. So the two files are not two readings a human has to disentangle: they are
**one key**, and the quieter instance's heartbeat is *absent from the index*. The
three-signal liveness rule — claim age, owner heartbeat mtime, report mtime, newest wins
(`.agent/HEARTBEATS/README.md`) — is then computed from the wrong instance's heartbeat.
A claim belonging to the quieter instance reads `STALE` on the heartbeat signal while
being genuinely live, which is the false-reap shape of B5-0597 reached by a new route.
The validator's exit 1 is currently the only thing standing between this store and that.

## 3. The rule

**R1 — an `agent_id` names a session instance, not a model.**
`agent_id` = `client (model)` + a per-instance discriminator.

**R2 — the discriminator must contain at least one letter or digit.**
This is the non-obvious constraint, and the code imposes it. `Get-NormName` strips
everything that is not a letter or digit, so a punctuation-only discriminator vanishes
and the instance collapses back onto its base id:

| `agent_id` | `Get-NormName` key | distinct? |
|---|---|---|
| `opencode (space-bunny-free)` | `opencodespacebunnyfree` | base |
| `opencode (space-bunny-free) 2` | `opencodespacebunnyfree2` | **yes** |
| `opencode (space-bunny-free)-2` | `opencodespacebunnyfree2` | **yes** (same instance, other spelling) |
| `opencode (space-bunny-free) _` | `opencodespacebunnyfree` | **no** — collapsed |

**R3 — pick the spelling once and never change it.** One instance, one spelling, for the
life of that instance. Two spellings of one instance pass the validator's collision check
(it compares exact `agent_id` strings) and are silently merged by the liveness join — the
failure is invisible in the one tool that is supposed to catch it.

**R4 — resolution is unchanged; no tooling edit is required.**
`validate-heartbeats.ps1` resolves in order: exact stem match, then a `_registry.json`
entry, then documented `:`/`/` sanitisation. Exact stem match comes **first**, so a
discriminated id whose characters are all legal in a Windows filename needs no registry
row. `opencode (space-bunny-free) 2.json` was written and read `CONFORMS` with no
registry change. Do not touch `_registry.json` for an id a filename can hold.

**R5 — never retro-rename an existing id.** `solar-pro4:free` is cited in 114 ledger rows
and 154 reports; renaming it breaks every citation to fix a defect that renaming causes.
Sanitised filenames and registry entries are the correct response to an unspellable id,
which is why that mechanism exists at all.

**R6 — the rule is prospective.** It binds new sessions. It does not require any existing
agent to change anything, and it authorises nobody to edit a foreign heartbeat.

## 4. Rejected alternatives

**Two files per `agent_id`, disambiguated by `Newest(mtime)`.** This is the status quo
and it is the bug: the index cannot represent two live values for one key, so it drops
one silently. Making the collision visible in the validator while leaving the join lossy
is the worst of both — a red tool and a wrong answer.

**Retro-rename every colliding id to an instance-qualified form.** Rejected by R5. It also
would not have helped here: the fullwidth-colon file resolves to the same `agent_id`
through no registry entry, so the defect is in the *spelling of the filename*, not in the
id, and a rename of the id would have left both files still claiming the new id.

**Add a `session_uuid` field to the heartbeat schema.** Correct in the abstract, and the
wrong cost here. It needs a schema amendment, a validator change, a migration for 32
existing files, and it fixes a problem that one character in one string already solves.
It also does not fix the filename, which is the thing two agents collide *on*. Worth
revisiting if instances ever need to be correlated across clients.

**Make the filename a UUID.** Destroys human legibility of a directory people read to
find out who is working, and the store is small enough that a human scan is a real
workflow. A trailing digit costs nothing.

**Derive uniqueness from a runtime handle (PID, port, hostname).** Non-deterministic
across reboots, so the same logical agent changes id weekly and its report history stops
joining. The store is a *long-lived* coordination record; identity must outlive the
process.

## 5. What this does not fix

Stated plainly, because a proposal that only lists its own successes is an advertisement.

1. **The live `solar-pro4:free` pair stays as it is.** Both files are foreign. The
   correct action is B5-0773's — quarantine the redundant one under its own row, do not
   delete — and it is not this row's to take. B5-0783 leaves both byte-identical and
   reports the reading.
2. **The validator checks exact ids; the join collapses normalised keys.** R3 mitigates
   this by discipline. Mechanically closing it needs a *store-level* check on
   `Get-NormName` keys, not on `agent_id` strings — a real change to
   `validate-heartbeats.ps1`, deliberately **not** in this row's scope and **not** done
   here. It is the obvious next row if this proposal is promoted.
3. **Orphaned stale files accumulate.** Every instance that ever ran leaves a file that no
   claim will ever reference. That is the correct trade — a stale file is inert, an
   overwritten one is a lie — but the store will grow, and nobody currently prunes it.
4. **Adoption is social.** Every agent picks its own discriminator. Nothing enforces that
   a new id is not a duplicate of a running one; the validator's exit 1 remains the
   detector, and the detector is a report, not a gate.

## 6. Adoption

Adopted by this session, in this row, for itself: claim and heartbeat written under
`opencode (space-bunny-free) 2`.

* `.agent/HEARTBEATS/opencode (space-bunny-free) 2.json` — new file, `CONFORMS`, no
  registry row needed (R4).
* `.agent/CLAIMS/B5-0783.json` — `agent_id` matches the stem, which is what lets
  `Get-HeartbeatIndex` find the owner at all.
* The prior session's `opencode (space-bunny-free).json` is left **byte-identical and
  stale**. R5 cuts both ways: a new instance does not get to tidy the old one.
* This session's reusable-lesson record is filed in the **new** namespace
  (`.agent/PATTERNS/opencode (space-bunny-free) 2/`), because namespace = identity and
  filing a new instance's record under the old instance's namespace would be the exact
  conflation this document exists to prevent.

Verification, all read-only, all on the real tree:

| Check | Result |
|---|---|
| `validate-heartbeats.ps1` after adding the new file | 32 files, 31 conforming, **collisions still 1** — the pre-existing solar pair, unchanged by this row |
| new heartbeat row | `CONFORMS`, identity 31 distinct |
| `Get-NormName` discriminator test (R2) | `…free` → `opencodespacebunnyfree`; `…free 2` → `opencodespacebunnyfree2`; `…free _` → `opencodespacebunnyfree` (collapse, as R2 predicts) |
| fullwidth-colon test (§2) | both spellings → `solarpro4free`, identical key |
| `run-dup-census.ps1` after seeding the row | `PASS (0 duplicate task IDs)`, exit 0 |
| `ledger-query.ps1 -Status "*"` on the new row | `B5-0783 OPEN`, `pipeCount 7`, `doubleLead no`, `reportable` |
| `compile.bat` | not run — no Java touched, no build gate applies |

## 7. Promotion

If a human promotes this, the minimum edit is one paragraph in
`.agent/HEARTBEATS/README.md` (*Identity: `agent_id` and filenames*) carrying R1–R6, plus
one line in `00_BOOT.md` step 3 telling a booting agent to pick a discriminator it has not
used before. Both files are governance and out of scope for a proposal; neither was
touched. The store needs no migration, no registry change and no tooling change, which is
the point: the fix is a naming rule, and the only thing that was ever broken was a name.
