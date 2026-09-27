---
document:
  title: "B5-0623 — heartbeat migration finished: the binding document is now binding"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0623 — heartbeat migration finished

Human order: *"The heartbeat migration is half-finished. Finish this first."*

## The order's premise was half right, and the difference mattered

The migration was described as half-finished. Measured rather than assumed, it was
**functionally complete and merely uncommitted** — a different defect with a
different fix.

Measured on the live store:

| check | result |
|---|---|
| `validate-heartbeats.ps1` | **exit 0** — 27 files, 27 conforming, 0 non-conforming |
| `migrate-heartbeats.ps1 -DryRun` | `MIGRATE-JSON 28`, nothing outstanding |
| the 10 paths git shows as deleted | **not deletions** — the A1.1 sanitised renames |
| non-conforming files | 6, correctly parked in `_quarantine/` |

`git status` reporting ten `D` entries is what makes this look like data loss. It is
not: `solar-pro4-free.json`, `Kiro (pi-coding-agent).json` and both
`kilo (nvidia-nemotron-3-ultra-550b-a55b-free)` spellings are all present under their
new names, with `_registry.json` mapping the ids the filesystem cannot express.

So there was no half-migrated store to rescue. What genuinely remained unfinished was
the **document**, and it was the same class of defect I had reported against B5-0622
the day before.

## What was actually broken

`00_BOOT.md` step 3 — added by a prior session and human-approved — states:

> Heartbeat format: `.agent/HEARTBEATS/README.md` is binding — strict JSON with
> `schema_version`, `agent_id`, `utc` (the only canonical timestamp field), `state`,
> and `live_claims` …

`HEARTBEATS/README.md` was the **original 2026-09-21 file**, 22 lines long,
specifying four fields and none of the semantics:

```json
{"agent_id": "hermes-01", "utc": "2026-09-21T12:05:00Z", "current_task": "B5-0001", "javac": "1.8.0_292"}
```

No `schema_version`. No `state`. No `live_claims`. No statement that `[]` is a
*positive assertion*, no `UNKNOWN` verdict, no prohibition on returning `-1` for an
absent signal, no `notes`-is-never-parsed rule. The enforced schema existed **only
inside the validator**.

The failure mode is precise and worth naming: a boot-time reader is told to consult
one document, that document does not contain the contract, and the contract is
enforced somewhere else. A reader who trusted the document would have written
pre-schema files and been rejected by the tool, with the tool pointing at a document
that had never mentioned the requirement. This is the self-certifying-citation shape
recorded against B5-0606 and again in my own pattern store — a claim adjacent to a
citation that would have supported it, so checking the citation feels like checking
the evidence.

## A second, smaller drift in the same place

`validate-heartbeats.ps1` had `$ValidState = @('active', 'idle', 'busy')` — `busy`
added by human approval in Amendment A1, after a live agent wrote `state: busy` and
the enum was widened rather than the agent's file rewritten. But the two *diagnostic
messages* still read:

```
state missing (expected active|idle)
state 'x' not in enum (active|idle)
```

So the tool silently accepted `busy` while telling any author that `busy` was
invalid. A validator whose error text contradicts its own acceptance criteria is
worse than one with no enum at all: the author cannot tell whether to fix the file or
fix their understanding. Corrected to derive the message from `$ValidState` rather
than repeating a literal, so the two cannot drift apart again.

## What I did

1. **Rewrote `.agent/HEARTBEATS/README.md`** (22 → 8,660 bytes) as the real binding
   contract: the five required fields with the three optional ones, the
   `active`/`idle`/`busy` enum, the identity-resolution rule, the three-signal
   liveness rule with `LIVE`/`STALE`/`UNKNOWN`, the explicit prohibition on `-1`, the
   never-parse-`notes` rule with its failure-2 provenance, and the exit-code
   contract. It also states the precedence rule explicitly: if README and the
   validator ever disagree, the validator is what runs, and the disagreement is a
   defect to fix rather than a winner to pick.
2. **Fixed the two enum messages** in the validator.
3. **Verified**: validator exit 0, 27/27 conforming, 0 non-conforming; migration
   dry-run still clean; and a field-by-field cross-check confirming README, validator
   and boot step 3 now agree on all five required fields, on the enum including
   `busy`, and on the semantics.
4. **Committed the entire migration**, including the `00_BOOT.md` step 3 pointer that
   the previous checkpoint deliberately left unstaged — precisely because it cited
   untracked files. That is now resolved: the validator it names is in the same
   commit.

No `agent_id` was altered and no heartbeat file renamed beyond the A1.1 set, so all
268 existing citations keep resolving.

## Left undone, deliberately

**The opencode identity ambiguity.** At least two sessions write coordination files
under opencode-family identities (`opencode (space-bunny-free)` and
`opencode (me-so-poor)`) and cannot see each other. `live_claims` is derived
*correctly* and is still *semantically ambiguous* until one session owns one
identity. No schema change fixes this — it is a fleet identity-discipline decision,
and Amendment A1 already records it as the real blocker. I did not touch it.

**Reusable lesson:** "half-finished" names a state, not a cause. Measure which half
is missing before repairing one — here the data was finished and the *document* was
not, and a data-migration pass would have found nothing to do while leaving the one
real defect in place. The corollary: a migration is finished when the thing that
*explains* it agrees with the thing that *enforces* it, not when the data passes.
