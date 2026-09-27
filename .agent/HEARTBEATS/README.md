---
document:
  title: "Heartbeats directory — binding liveness schema"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" - schema proposal and Amendment A1, human-approved 2026-09-27"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" - rewritten to carry the binding schema the boot pointer and validator already enforce, human-approved 2026-09-27 (B5-0623)"}
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-21"
  last_modified_date: "2026-09-27"
---

# HEARTBEATS

One file per agent at `.agent/HEARTBEATS/<agent_id>.json`, refreshed roughly every
5 minutes while active. **This file is binding** — it is the schema
`00_BOOT.md` step 3 points at, and the schema
`.agent/tools/validate-heartbeats.ps1` mechanically enforces. If this document and
the validator ever disagree, the validator is what runs, and the disagreement is a
defect in one of them: fix it, do not pick a winner silently.

**Never edit another agent's file.** Write only your own.

## Why the schema exists

A heartbeat converts **unresolvable silence into evidence**. In a network you cannot
distinguish *crashed* from *slow* from *partitioned* from *idle*; a periodic positive
assertion of liveness is the standard remedy. An agent fleet has the same problem,
and a claim file with a 30-minute TTL cannot tell "the agent died" from "the agent is
running a long verification".

Three failures already occurred here, all of them schema failures rather than logic
failures:

| # | Failure | Record |
|---|---|---|
| 1 | A reaper keyed on claim mtime deleted claims **under a live worker** | B5-0597 |
| 2 | A reader matched task IDs inside a heartbeat's **narrative** field and reported 4 claims held when `live_claims` was `[]` | B5-0609 |
| 3 | A lookup matching no heartbeat file returned `-1`, and `-1` compares as **younger** than the TTL, so 4 claims rendered LIVE with zero verification | B5-0597 |

Failure 3 is the worst class: the tool manufactured a positive liveness claim out of
an **absent** signal. An absent signal is `UNKNOWN`, never `LIVE`.

## The file

Strict JSON, UTF-8, no frontmatter, no trailing commas.

```json
{
  "schema_version": 1,
  "agent_id": "opencode (space-bunny-free)",
  "utc": "2026-09-27T23:58:00Z",
  "state": "active",
  "current_task": "B5-0623",
  "live_claims": ["B5-0623"],
  "javac": "1.8.0_292",
  "notes": "free text, never parsed"
}
```

## Field contract

| Field | Required | Type | Rule |
|---|---|---|---|
| `schema_version` | **yes** | int | `1`. Absent means pre-schema. |
| `agent_id` | **yes** | string | Your stable identity for the life of the repo. Must resolve to exactly **one** file — see *Identity* below. |
| `utc` | **yes** | ISO-8601 UTC, `Z` suffix | **The only canonical timestamp field.** `updated_utc`, `heartbeat_utc` and `last_heartbeat_utc` are tolerated for one migration cycle and must not be added. |
| `state` | **yes** | enum | `active`, `idle`, or `busy`. **Never** free text. |
| `current_task` | no | string or `null` | Single task ID. Use an array only if a session genuinely holds 2+. |
| `live_claims` | **yes** | array of strings | **Always present, even when empty.** `[]` is a *positive assertion* that you hold nothing. Omission means `UNKNOWN`. |
| `javac` | no | string | Toolchain version; required in the heartbeat by the build rules. |
| `notes` | no | string | **Prose. Never parsed for structure.** See the two rules below. |

Unknown keys are reported by the validator as informational notes, not violations,
which is what lets an old reader keep working during migration.

## The two rules that matter most

1. **`live_claims` is structured and always present.** `[]` means *I hold nothing*.
   Omission must never be read as "probably nothing" — omission is `UNKNOWN`.
2. **`notes` is never a data source.** No reader may extract a task ID, a claim, or a
   status from prose. If a value must be machine-read it gets its own field; prose is
   for humans. A JSON document can be schema-validated and prose cannot, and once a
   field is declared prose every later reader will treat it as structured because the
   file *looks* structured. Failure 2 above happened exactly here.

## Identity: `agent_id` and filenames

`agent_id` must resolve to exactly **one** file, in this order:

1. exact filename-stem match, or
2. an `_registry.json` entry, or
3. documented sanitisation, `:` and `/` → `-`.

**Why not simply "stem must equal `agent_id`":** Windows forbids both `:` and `/` in
filenames, and several ids contain them — `solar-pro4:free` alone is cited in 114
ledger rows and 154 reports, so it cannot be changed to make a filename work. The
naive rule and the filesystem are mutually exclusive; that is why the rule is
*resolution* rather than *equality*. No `agent_id` was ever altered — only filenames
were sanitised, so all existing citations keep resolving.

**One agent, one spelling, for the life of the repo.** A new spelling is a *new
agent*, not a variant: two sessions under one spelling cannot see each other's
in-flight intent, which is a live hazard in this repo right now (see *Known
unresolved* below). The validator **fails** if any `agent_id` is claimed by more than
one file, because two files asserting one identity make `live_claims` ambiguous no
matter how well-formed either file is.

## Liveness: three signals, never one

```
live(T)  <=>  max( mtime(CLAIMS/T.json),
                    utc(HEARTBEATS/<owner(T)>.json),
                    mtime(REPORTS/T-*.md) )  is  within TTL
```

A task is live when the **newest** of its claim file, its owner's heartbeat, and its
report mtime falls inside the TTL. Any single signal produces a known false result:

- claim mtime alone → false reap of live work (failure 1)
- heartbeat alone → false reap when the heartbeat lags but the agent is mid-write
- report mtime alone → false reap before any report exists

| Verdict | Condition |
|---|---|
| `LIVE` | at least one signal found and within TTL |
| `STALE` | at least one signal found, none within TTL |
| `UNKNOWN` | a required signal was **absent or unparseable** |

`UNKNOWN` is a first-class verdict, not an error to swallow. A join that matches no
heartbeat file returns `UNKNOWN` **with its reason**; it must never return an age of
`-1` or `0`, because both compare as *younger* than any TTL (failure 3). A heartbeat
file that fails to parse is `UNKNOWN`, not "stale" and not "absent".

When a heartbeat's *contents* cannot be trusted its **mtime** usually can — the
filesystem maintains it without the writer's cooperation. During the 2026-09-27 survey
`LastWriteTimeUtc` was the most reliable signal available, precisely because payloads
were malformed in 19 of 32 files. Report mtime alongside the parsed value and let the
reader choose.

A reaper may remove a claim only when **all three** signals are `STALE`, and only
after recording that evidence **in the reap note itself**. When signals conflict, leave
the claim alone. A reaper that guesses is worse than no reaper: it destroys work and
invites duplicate delivery.

## Verifying

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/validate-heartbeats.ps1
```

Read-only. Exit `0` = every file conforms. Exit `1` = a non-conforming or unreadable
file, **or** an `agent_id` claimed by more than one file. Exit `2` = the directory is
missing or empty. One unparseable file is what once made a whole census silently
wrong, so a parse failure is a finding, never a crash.

## Known unresolved — a human decision, not a schema problem

At least two sessions write coordination files under opencode-family identities
(`opencode (space-bunny-free)` and `opencode (me-so-poor)`) and cannot see each other.
`live_claims` is derived correctly and still semantically ambiguous until one session
owns one identity. This is recorded rather than papered over: the schema is sound, the
fleet's identity discipline is not.

## History

* Original file specified only `agent_id`, `utc`, `current_task`, `javac`. The
  2026-09-27 survey found the store implemented 32 times over with no common schema
  and roughly 10% adoption of even that.
* `docs/proposals/heartbeat-schema-proposal.md` is the design rationale, including
  human-approved Amendment A1 (`agent_id` == stem is unsatisfiable; `state` gains
  `busy`). That document is a **proposal** and confers no authority; this README and
  the validator are what bind.
