---
document:
  title: "Heartbeat identity resolution — Groups 1-4 applied, and a filesystem constraint that made the plan unexecutable as written"
  status: "Report (observations only, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Heartbeat identity resolution

**Author:** opencode (space-bunny-free), version `space-bunny-free`
**Authority:** human approval, 2026-09-27, of all four groups, Group 3 as **Option A**
(merge). Human also confirmed `solar-pro4:free` (Hermes) has looped and died, removing
the live-writer race on Group 2.
**Scope:** `.agent/HEARTBEATS/` and this report. `b5ccg/` untouched.

Backup: `C:\Users\TIWAPE~1\AppData\Local\Temp\opencode\hb-backup-preidentity-20260927-160505\`

---

## 1. Headline: the approved plan was partly unexecutable

**Windows forbids both `:` and `/` in filenames.** Verified, not assumed:

```
CREATE FAILED: "The given path's format is not supported."
kilo (nvidia/nemotron-3-ultra-550b-a55b:free).json    ILLEGAL on Windows
kilo-auto (nvidia/nemotron-3-ultra-550b-a55b:free).json ILLEGAL on Windows
solar-pro4:free.json                                    ILLEGAL on Windows
```

So §6.4's rule — *`agent_id` must equal the filename stem* — is **unsatisfiable** for four
of the 27 agents, because their ids contain a colon, a slash, or both. And `solar-pro4:free`
is cited in **114 ledger rows and 154 report files**, so its id cannot be changed to make
the filename work. The two requirements are mutually exclusive.

**Resolution applied, minimal and reversible:**

1. **No `agent_id` was ever altered for a rename.** `solar-pro4:free` is stored exactly as
   written, so all 268 existing citations keep resolving.
2. Filenames sanitised `:` and `/` → `-`, which is **this repo's own existing convention** —
   e.g. `2026-09-25-kilo-nvidia-nemotron-3-ultra-550b-a55b-free-B5-0401.md` and
   `2026-09-27-solar-pro4-free-B5-0613.md`.
3. **`.agent/HEARTBEATS/_registry.json`** created: 27 `agent_id` → filename entries. The
   filename is no longer authoritative; the registry is.
4. The validator now resolves identity in three steps: exact stem match → registry entry →
   sanitised match. Four ids resolve via sanitisation:

| `agent_id` (authoritative) | filename |
|---|---|
| `solar-pro4:free` | `solar-pro4-free.json` |
| `Kilo (kilo-auto/free)` | `Kilo (kilo-auto-free).json` |
| `kilo (nvidia/nemotron-3-ultra-550b-a55b:free)` | `kilo (nvidia-nemotron-3-ultra-550b-a55b-free).json` |
| `kilo-auto (nvidia/nemotron-3-ultra-550b-a55b:free)` | `kilo-auto (nvidia-nemotron-3-ultra-550b-a55b-free).json` |

**This means the schema rule must be amended:** *"`agent_id` equals the filename stem" is
wrong as an invariant. The correct invariant is "`agent_id` resolves to exactly one file,
via exact match, registry, or documented sanitisation."* Without that amendment any future
agent will re-derive this same dead end.

---

## 2. What was applied

### Group 1 — renames

| From | To | Note |
|---|---|---|
| `kilo.json` | `kilo (nvidia-nemotron-3-ultra-550b-a55b-free).json` | `:` and `/` sanitised |
| `kilo-auto.json` | `kilo-auto (nvidia-nemotron-3-ultra-550b-a55b-free).json` | `:` and `/` sanitised |
| `kiro-pi.json` | `Kiro (pi-coding-agent).json` | clean rename |
| `Kilo (kilo-auto-free).json` | **unchanged** | its id sanitises to exactly this name — matches its own report `2026-09-25-Kilo-kilo-auto-free-B5-0394.md` |

### Group 2 — the `solar-pro4` pair

Mangled `solar-pro4⟨U+F03A⟩free.json` (fresher) became **`solar-pro4-free.json`**, the
canonical file. `solar-pro4.json` quarantined.

**One deliberate deviation from the literal approval.** The plan was to merge
`state: active` from the older file. Since the human then confirmed Hermes is dead, writing
`active` would assert liveness that does not exist — the precise false-assertion failure
this whole exercise exists to eliminate. So `state` is `idle`, and the historical
`active` is preserved in `state_history` keyed by its timestamp. The approved *intent* —
"keeping the older file's stale `active` would make a live-looking record" — is honoured
more strictly than the literal instruction.

### Group 3 — Option A, merge

`freebuff-01/02/03.json` merged into **`Buffy (deepseek-v4-flash).json`**. All three
sessions' `notes` and `last_completed` preserved verbatim in `session_notes_history` with
their source filename and timestamp; all three originals quarantined. Verified: **0 merged
notes lost.**

### Group 4

`buffy-unknown-loop2.json` → `agent_id: buffy-unknown-loop2`. No rename, no collision.
Rationale recorded in the file: `Buffy (unknown).json` already holds that id and conforms,
so this file is a distinct session, not a duplicate.

---

## 3. Result

| Metric | Before this pass | After |
|---|---|---|
| Files conforming | 20 / 30 | **26 / 27** |
| Files failing on `agent_id` | 10 | **0** |
| Registry entries | — | **27** |
| Keys lost | — | **0** |
| Files deleted | — | **0** |
| Quarantined (preserved) | 2 | **6** |

Quarantine now holds: `freebuff-01/02/03.json`, `solar-pro4.json`, `kiro-pi.timestamp`,
`solar-pro4`. All recoverable from the backup.

---

## 4. The one remaining violation is a live agent's file — left alone deliberately

```
opencode (me-so-poor).json   agent_id=opencode (me-so-poor)   state=busy   age=10min
    - state 'busy' not in enum (active|idle)
```

`opencode (me-so-poor)` is **live**, working `B5-0620` (`live_claims: [B5-0620]`,
`current_task: B5-0620`), and wrote `state: busy` 10 minutes ago. It has adopted the schema
(`utc`, `state`, `live_claims` all present) but chose a third enum value.

**Not edited.** It is a live writer; my write would be overwritten or would race, and
silently rewriting another agent's state field is exactly the "never edit another agent's
file" rule. Two options for you: add `busy` to the enum, or leave it as a genuine
violation so non-conformance stays visible. It is currently doing useful work as a
standing example of the validator working.

---

## 5. The live identity collision — unresolved, and it is the real blocker

While deriving `live_claims`, claims appeared under **my own agent_id** that I did not
create: `B5-0618`, `B5-0619`, `B5-0621`, all `agent_id: opencode (space-bunny-free)`.

Combined with the finding above — a *different* spelling, `opencode (me-so-poor)`, is
live on `B5-0620` — the picture is that **at least two sessions are writing coordination
files under opencode-family identities**, and they cannot see each other.

This is why the identity work matters more than its row count. `live_claims` is only
meaningful if exactly one session owns an id. Right now it is not, so `live_claims` is
*technically* derived and *semantically* ambiguous. My derivation was correct — those
files really do claim that id — and still untrustworthy, which is the whole lesson of this
pass in one sentence.

**Not actioned.** Resolving which session owns an identity is a human decision, and the
ledger was being appended to by another session throughout this pass.

---

## 6. Four defects I introduced, all disclosed

Recorded because the subject of this report is how verification fails, and mine failed four
times.

1. **Array collapse wrote 24 non-array `live_claims`.** `@($a) + @()` collapses to a
   scalar in PowerShell. Worse, my "preserve non-strings" branch would have baked the
   corruption in permanently as the literal string `"{}"`. Fixed with `List[string]` +
   `.ToArray()`; non-strings now dropped as corruption. **Caught by the validator, on the
   same pass that caused it.**

2. **My validator repeated the same class of error** — it checked array-ness on the parsed
   object, which PS 5.1 does not round-trip. Rewritten to test raw JSON text.

3. **A formatting bug aborted a summary** — `Write-Output` strings mixed with
   `Format-Table` in one pipeline.

4. **Regex surgery triplicated the validator file.** `-replace` applied to a pattern that
   recurred left three copies and the script stopped parsing. Fixed by rewriting the file
   wholesale instead of patching it further.

**The recurring root cause across all four: doing surgery on my own files instead of
re-deriving them.** And a fifth, environmental: **`Ren` and `MV` are built-in PowerShell
aliases for `Rename-Item` and `Move-Item`, and aliases outrank functions** — two runs died
on that. Any function whose name is a PowerShell alias is silently never called.

---

## Reusable lesson

**A filename is not an identity, and on Windows it cannot be one.** The schema rule
"`agent_id` must equal the filename stem" looks like a tidy invariant and is unsatisfiable
the moment an id contains `:` or `/` — both reserved. When a key cannot be represented in
the identifier you have chosen, the correct move is an explicit registry, not a rule you
quietly stop enforcing or a mangled character you hope nobody notices.

Second, and it cost four defects here: **PowerShell aliases outrank functions**, so a
helper named `Ren` or `MV` is never invoked — it silently runs `Rename-Item` and
`Move-Item` with the wrong argument shape. Pick helper names that cannot collide, or
verify the function is actually the one being called.
