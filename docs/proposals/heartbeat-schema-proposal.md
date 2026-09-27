---
document:
  title: "Heartbeat schema and liveness protocol proposal"
  status: "Proposal (advisory, no authority until merged)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Heartbeat schema and liveness protocol

**Status: proposal.** Advisory, same tier as `investigations/`. This document confers no
authority. It does not change `00_BOOT.md`, which is governance and out of scope for a
proposal (per the B5-0593 precedent). Promotion requires a human decision.

**Origin.** User request, 2026-09-27: *write a schema doc for heartbeats.* The request
followed a survey that found the heartbeat store implemented 32 times over with no common
schema, and a documented canonical format with roughly 10% adoption. Measurements are in
`docs/reports/heartbeat-terminology-and-liveness-findings-2026-09-27.md`.

---

## 1. The problem this solves

A heartbeat exists to convert **unresolvable silence into evidence**. In a network you
cannot distinguish *crashed* from *slow* from *partitioned* from *idle*; a periodic
positive assertion of liveness is the standard remedy. An agent fleet has the identical
problem, and worse: a claim file with a 30-minute TTL cannot distinguish "agent died" from
"agent is running a long verification".

Three failures have already occurred in this repo, all recorded:

| # | Failure | Record |
|---|---|---|
| 1 | A reaper keyed on claim mtime would delete claims **under a live worker** — claims 0572/0573/0574 read 63–85 min against a 30-min TTL while their owner was actively writing reports | B5-0597 |
| 2 | A reader matched task IDs inside a heartbeat's **narrative** field and concluded 4 claims were held when `live_claims` was an empty list | B5-0609 |
| 3 | A lookup that matched no heartbeat file returned `-1`, and `-1` compared as **younger** than the TTL, so 4 claims were reported LIVE with zero verification | B5-0597 |

Failure 3 is the worst class: the tool manufactured a positive liveness claim out of an
**absent** signal. An absent signal is `UNKNOWN`, never `LIVE`.

---

## 2. Canonical file

One file per agent, at `.agent/HEARTBEATS/<agent_id>.json`. Strict JSON, UTF-8, no
frontmatter, no trailing commas.

```json
{
  "schema_version": 1,
  "agent_id": "opencode (space-bunny-free)",
  "utc": "2026-09-27T23:58:00Z",
  "state": "active",
  "current_task": "B5-0613-status-residual",
  "live_claims": ["B5-0613-status-residual"],
  "javac": "1.8.0_292",
  "notes": "free text, never parsed"
}
```

### Field contract

| Field | Required | Type | Rule |
|---|---|---|---|
| `schema_version` | **yes** | int | `1`. Absent means pre-schema, see §6. |
| `agent_id` | **yes** | string | Must equal the filename stem exactly. See §4. |
| `utc` | **yes** | ISO-8601 UTC, `Z` suffix | **The single timestamp field name.** This is the field `README.md` already specifies; this proposal makes it binding and removes the three variants. |
| `state` | **yes** | enum | `active` or `idle`. **Never** a free-text status. |
| `current_task` | no | string or `null` | Single task ID. Array only if a session genuinely holds 2+. |
| `live_claims` | **yes** | array of strings | **Structured. Always present, even when empty.** `[]` is a positive assertion of holding nothing. |
| `javac` | no | string | Toolchain version; §6 of the architecture doc requires it. |
| `notes` | no | string | **Prose. Never parsed for structure.** See §3. |

### The two rules that matter most

1. **`live_claims` is structured and always present.** An empty array means *I hold
   nothing*. Omission must never be read as "probably nothing" — it means `UNKNOWN`.
2. **`notes` is never a data source.** No reader may extract a task ID, a claim, or a
   status from prose. Failure 2 above happened exactly here, and it is unrecoverable
   because prose has no schema.

---

## 3. Why prose fields are excluded from the contract

`.agent/PATTERNS/opencode (me-so-poor)/prose-in-a-json-field-is-not-a-field.md` already
records this. The reasoning generalises: a JSON document can be schema-validated, and
prose cannot. Once a field is declared prose, every future reader will treat it as
structured, because the file *looks* structured. The Deepseek Harness transcript read
`last_completed` — a sentence listing task IDs — and reported a claim set that did not
exist.

**Rule: if a value must be machine-read, it gets its own field. Prose is for humans.**

---

## 4. `agent_id` is a namespace identity, and it is currently fragmented

The directory currently contains **multiple spellings per agent**:

| Agent | Spellings on disk |
|---|---|
| solar-pro4 | `solar-pro4`, `solar-pro4:free`, `solar-pro4⟨U+2028⟩free` |
| Buffy | `Buffy (glm-5.3-flash)`, `Buffy (unknown)`, `Buffy-(glm-5.3-flash)`, `buffy-unknown-loop2` |
| kilo | `kilo`, `kilo-auto`, `Kilo (kilo-auto-free)` |
| GPT-6 | `GPT-6 Codex (GPT-6)`, `GPT-6-Codex`, `codex-gpt6-01` |
| opencode | `opencode (me-so-poor)`, `me-so-poor` |
| Kiro | `kiro-pi`, `kiro-pi.timestamp` |

An exact-string join on `agent_id` **cannot match across these**, so a liveness scan
silently concludes "no heartbeat found" for an agent that is demonstrably alive. This is
the same failure as B5-0597's second finding.

**Proposed rule.** One agent, one spelling, for the life of the repo. A new spelling is a
**new agent**, not a variant — because in this system a twin session under one spelling
cannot see its twin's in-flight intent (the B5-0337 coordination rule). Where a legacy
spelling must be matched during migration, normalise by lowercasing and stripping
non-alphanumerics before comparison, and treat a normalised collision as `UNKNOWN`, never
as a match. B5-0584 owns the full census; this proposal only states the rule.

---

## 5. Liveness determination

### 5.1 Three signals, not one

```
live(T)  ⇔  max( mtime(claim/T.json),
                  utc(heartbeat/<owner(T)>.json),
                  mtime(report/T-*.md) )  is  within TTL
```

A task is live when the **newest** of its claim file, its owner's heartbeat, and its
report mtime falls inside the TTL (B5-0597). Any one signal alone produces a known false
result:

- claim mtime alone → false reap of live work (failure 1)
- heartbeat alone → false reap when the heartbeat is stale but the agent is mid-write
- report mtime alone → false reap before any report exists

### 5.2 A signal is LIVE, STALE, or UNKNOWN — never a default

| Verdict | Condition |
|---|---|
| `LIVE` | at least one signal found and within TTL |
| `STALE` | at least one signal found, none within TTL |
| `UNKNOWN` | a required signal was **absent or unparseable** |

`UNKNOWN` is a first-class verdict, not an error to be swallowed. Concretely:

- A join that matches no heartbeat file returns `UNKNOWN` + the reason. It must **not**
  return an age of `-1` or `0`, because both compare as *younger* than any TTL and will
  render as `LIVE` (failure 3).
- A heartbeat file that fails to parse is `UNKNOWN`, not "stale" and not "absent".
- `live_claims: []` is a **positive** assertion and yields `LIVE-idle`. A missing
  `live_claims` key is `UNKNOWN`.

### 5.3 Prefer filesystem metadata when the payload is untrustworthy

If a heartbeat's *contents* cannot be trusted, its **mtime** usually can — the
filesystem maintains it without the writer's cooperation. During the 2026-09-27 survey
the most reliable liveness signal available was `LastWriteTimeUtc`, precisely because the
JSON payloads were malformed in 19 of 32 files. A validator should therefore report
`mtime` alongside the parsed value and let the reader choose.

### 5.4 Reaping

A reaper may remove a claim only when **all three** signals are `STALE` and it has
recorded the evidence **in the reap note itself** before removing the file (B5-0597). When
signals conflict, leave the claim in place. A reaper that guesses is worse than no
reaper: it destroys work and invites duplicate delivery.

---

## 6. Migration of the 32 existing files

Additive and non-destructive; no file is deleted by this proposal.

1. **Add `schema_version` and `utc`.** Where a file already carries `updated_utc` or
   `heartbeat_utc`, copy the value into `utc` and keep the old key for one cycle so
   existing readers do not break. Remove the old key in a later pass.
2. **Make `live_claims` always present**, `[]` when empty.
3. **Add `state`.** Infer from existing `status` where present, else `idle`.
4. **Normalise filenames** to the canonical `agent_id`. This is the highest-risk step and
   should be **human-approved**, because a rename breaks every existing join until all
   readers are updated. Do it last, and never in the same pass as step 1.
5. **Move non-conforming files out.** `agent-on-deck.json` and
   `goose-b5ccg-agent.json` are YAML frontmatter, not JSON; `solar-pro4` has no
   extension; `kiro-pi.timestamp` is not a `.json`. Either convert or quarantine — a
   parser that throws on one file poisons the whole survey, and in this session a
   `ConvertFrom-Json` failure on a single malformed file silently truncated a census.

---

## 7. Validator

Read-only. Adds no file and edits none. Reports, per file: parses-as-JSON, has
`schema_version`, has `utc` and it parses as ISO-8601, `agent_id` equals filename stem,
`live_claims` present, `state` in enum, unknown keys listed. Exit non-zero if any file in
the directory fails to parse — because one unparseable file is what made a whole census
silently wrong.

```
validate-heartbeats.ps1 [-Directory .agent/HEARTBEATS] [-Json]
```

Implementation is a follow-up task, not part of this proposal.

---

## 8. What this proposal does not do

- It does **not** edit `00_BOOT.md` (governance) or `.agent/HEARTBEATS/README.md`. The
  README already names `utc` as the field; this proposal's `utc` clause aligns to the
  README rather than the reverse, and reconciling the README needs a governance decision.
- It does **not** rename any agent's file. See §6.4.
- It does **not** create a ledger row. Two checkpoint rows are gated on other work, and
  the ledger was being appended to concurrently during drafting.
- It does **not** claim any ruling on B5-0584's census, which owns agent_id fragmentation.
- It asserts **no** intent about the parked mercenary and contingency backlog (Ruling
  2c/3c).

---

## Reusable lesson

A heartbeat is a *positive assertion of liveness*, so the schema's job is to make every
possible state expressible — including **"I don't know"**. A liveness system that cannot
represent absence will always resolve absence into a confident answer, and a confident
wrong answer about whether work is live is more expensive than no answer at all: it
destroys work and manufactures duplicates.
