---
document:
  title: "Heartbeats directory — binding liveness schema"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" - schema proposal and Amendment A1, human-approved 2026-09-27"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free" - rewritten to carry the binding schema the boot pointer and validator already enforce, human-approved 2026-09-27 (B5-0623)"}
    - {name: "opencode (space-bunny-free) 2", version: "space-bunny-free", passes: 1, last_pass: "2026-09-28", note: "edit: added the R1-R6 instance-qualified agent_id subsection to Identity on human ruling 2026-09-28 (B5-0785). The two entries above keep prose fused into their version field, which is why identity cannot be matched on name+version for them; this entry follows the compaction form instead of editing them, per AGENTS.md 1a rule 4."}
    - {name: "opencode (big-pickle)", version: "big-pickle", passes: 1, last_pass: "2026-09-30", note: "edit: added R7 (ASCII-only single transliteration, PUA/separator codepoints barred from filenames) to Identity, on human-approved task B5-1062. R7 is additive to R1-R6 and does not alter any existing agent_id or rename any file."}
    - {name: "GitHub Copilot (Auto mode) 1012b", version: "Auto mode", passes: 1, last_pass: "2026-10-01", note: "merged approved B5-1065 heartbeat retirement policy and completed first archival pass"}
  last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 1012b", version: "Auto mode"}
  created_date: "2026-09-21"
  last_modified_date: "2026-10-01"
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

### The id names a session, not a model

**Adopted by human ruling 2026-09-28** (proposal
`docs/proposals/agent-instance-identity-proposal.md`, decision in
`docs/DECISIONS.md` B5-0785). Six rules, R1–R6:

* **R1 — an `agent_id` names a running session *instance*, not a model.** The
  de facto shape in this repo is `client (model)`, and the client half is what
  keeps two agents apart; the model half is not a discriminator at all. Shape:
  `client (model)` + a per-instance discriminator.
* **R2 — the discriminator must contain at least one letter or digit.** This is
  the constraint the code imposes and the one an obvious fix misses.
  `Get-NormName` (`.agent/run-queue.ps1`, `.agent/tools/ledger-query.ps1`) keeps
  **only letters and digits**, so a punctuation-only discriminator is stripped and
  the instance collapses back onto its base id: `…free 2` → `…free2` (distinct),
  `…free _` → the base key (no discriminator at all).
* **R3 — one instance, one spelling.** Pick it once. The validator's collision
  check compares **exact** `agent_id` strings while the liveness join compares
  **normalised** keys, so two spellings of one instance pass the very tool meant
  to catch them.
* **R4 — resolution is unchanged; no tooling or registry edit is needed.** Exact
  stem match is tried **first**, so a discriminated id made of characters Windows
  accepts resolves without a `_registry.json` row. Do not add a registry entry for
  an id a filename can hold.
* **R5 — never retro-rename an existing id.** `solar-pro4:free` is cited in 114
  ledger rows and 154 reports; renaming breaks every citation to fix a defect the
  rename causes. Sanitised filenames and registry entries are the correct response
  to an unspellable id, which is why they exist.
* **R6 — prospective only.** R1–R6 bind new sessions. They require no existing
  agent to change anything and authorise nobody to edit a foreign heartbeat.
* **R7 — one transliteration, ASCII-only, enforced.** *(Proposed by
  `opencode (big-pickle)`, B5-1062, on human-approved task; NOT covered by the
  2026-09-28 ruling above, which adopted R1–R6 only. R7 is enforced by
  `validate-heartbeats.ps1` and is reversible by editing the same two places.)*
  The `:` and `/` → `-`
  sanitisation above is the ONLY legal way to put an `agent_id` in a filename, and
  the `-` must be U+002D. A **lookalike** codepoint is a second, undeclared
  transliteration and is a defect even when it satisfies every Windows filename
  constraint: `solar-pro4<U+F03A>free` (U+F03A FULLWIDTH COLON, private use) is
  a legal filename that reads as `solar-pro4:free`, produces a different stem,
  resolves to the same `agent_id` with no registry entry, and is invisible to any
  `*.json` glob. **No file in this store may carry a PUA codepoint
  (U+E000–U+F8FF, U+F0000–U+FFFFD, U+100000–U+10FFFD) or a separator
  (U+2028, U+2029, U+00A0) in its filename**; `validate-heartbeats.ps1` reports
  `FILENAME LOOKALIKE` and exits 1. Three rules, and the last is the one that
  stops the next instance:
  1. Build the filename by transliterating the id — never by retyping it, and
     never by copying a name off disk that you have not checked.
  2. A stem that is *not* exactly the sanitised id, and not in `_registry.json`,
     is a defect to fix before the file is written, not after.
  3. If an id you own already has a file, **use it**. A second file for one
     identity is never the fix for a name you dislike — see R5, and B5-0773 for
     the quarantine-not-delete precedent.

A **lookalike** punctuation character is not a discriminator under any spelling:
`solar-pro4<U+F03A>free` (fullwidth colon) satisfies every Windows filename
constraint, produces a different stem, resolves to the same `agent_id` with no
registry entry, and normalises to the **same** `Get-NormName` key as the ASCII
spelling. This is a measured live condition, not a hypothetical.

The *Known unresolved* section below describes the state **before** this rule. The
rule is what a session is now required to do about it; the two opencode-family
files it names still exist and were left byte-identical, because R5 and R6 forbid
one instance tidying another. Closing them is a separate, separately-claimed action.

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

## Heartbeat retirement policy (approved 2026-09-29)

Retirement is archival, never deletion or rewriting. A retired heartbeat moves
byte-identically from `.agent/HEARTBEATS/` into `.agent/HEARTBEATS/_retired/`;
the filename and JSON remain unchanged, and `_registry.json` is not edited.

A file is retirable only when all conditions hold:

1. Its live-store mtime is older than 24 hours (48 TTLs).
2. Its payload state is `idle`, or it is a closed-session tombstone with no
   live claims (or all claims have since closed DONE). A `busy` or `active`
   payload is never archived on mtime alone.
3. No unresolved standing finding names it: it is not the subject of an OPEN
   row, a live claim, or an unadjudicated collision report.
4. The move is performed under a claimed ledger row recording the file, mtime
   age, payload state, and three-signal consequences.

The archive is `.agent/HEARTBEATS/_retired/`. Readers resolving an archived
identity treat its heartbeat signal as `UNKNOWN`, never LIVE or STALE.
Retired identities are not reusable, rewritten, or merged.

The out-of-enum tombstone `Cline (space-bunny) b5-0941.json` remains
byte-identical in place as a foreign file. R5/R6 forbid another instance from
tidying it, and no adopted home for that tombstone exists yet; its standing
validator finding therefore prevents mass retirement until separately
authorized.

This policy is binding after this merge. R1-R6 leave the existing instance
identity rules untouched.

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
