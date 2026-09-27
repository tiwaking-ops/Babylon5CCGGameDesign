---
document:
  title: "Heartbeat schema v1 — implementation, migration, and self-inflicted defects"
  status: "Report (observations only, no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# Heartbeat schema v1 — implementation report

**Author:** opencode (space-bunny-free), version `space-bunny-free`
**Authority:** `docs/proposals/heartbeat-schema-proposal.md` **APPROVED by the human,
2026-09-27**, with all three follow-on actions authorised: build the validator, migrate the
32 files additively, and add the `00_BOOT.md` pointer.
**Scope:** `.agent/tools/`, `.agent/HEARTBEATS/`, `.agent/00_BOOT.md` step 3.
`b5ccg/` untouched — 0 files changed.

---

## 1. Result

| Metric | Before | After |
|---|---|---|
| Files conforming to schema v1 | **0 / 32** | **20 / 30** |
| Strict JSON | 28 | **30** |
| YAML frontmatter (unreadable by any JSON reader) | 2 | **0** |
| Canonical `utc` field | 3 | **30** |
| `schema_version: 1` | 0 | **30** |
| `live_claims` as a real JSON array | ~0 | **30** |
| Validator exists | no | **yes, read-only, exits 1** |
| `00_BOOT.md` states the format | no | **yes, step 3** |

The 10 remaining non-conforming files fail on **one** issue only: `agent_id` does not match
the filename stem. That is the genuine B5-0584 identity fragmentation, and fixing it needs
the §6.4 renames, which were **not** authorised. Correctly left open.

`validate-heartbeats.ps1` exits **1** — it can go red, which is the entire point of it.

---

## 2. The 10 remaining violations are the real finding

Migration surfaced concrete, enumerable duplicate-identity defects:

| File | `agent_id` inside | Problem |
|---|---|---|
| `freebuff-01/02/03.json` | `Buffy (deepseek-v4-flash)` | **3 files, one agent_id** — and `Buffy (glm-5.3-flash).json` also exists |
| `kilo.json`, `kilo-auto.json` | `kilo (nvidia/nemotron-3-ultra-550b-a55b:free)` | **2 files, one agent_id** |
| `solar-pro4.json`, `solar-pro4⟨U+F03A⟩free.json` | `solar-pro4:free` | **2 files, one agent_id** |
| `buffy-unknown-loop2.json` | `Buffy (unknown)` | mismatched stem |
| `Kilo (kilo-auto-free).json` | `Kilo (kilo-auto/free)` | mismatched stem |
| `kiro-pi.json` | `Kiro (pi-coding-agent)` | mismatched stem |

**This is worse than "names are untidy."** When two files assert the same `agent_id`, there
is no way to tell which is authoritative, so a `live_claims` derived by joining on
`agent_id` is ambiguous — exactly the false-positive class this whole exercise exists to
eliminate. The join normaliser handles spelling variants, but it **cannot** resolve two
files legitimately claiming one identity; that needs a human decision on which file wins.

---

## 3. Live identity collision found during verification

While deriving `live_claims` from `CLAIMS/`, a claim file appeared that **I did not create**:

```json
{"task": "B5-0618", "agent_id": "opencode (space-bunny-free", ...}
```

Another session is running under **my exact agent id**, seeding the ledger, minutes old.
By the time of writing, `CLAIMS/` held `B5-0618`, `B5-0619`, `B5-0621` and
`heartbeat-schema-implementation`, all attributed to `opencode (space-bunny-free)`.

This is the twin-session failure the ledger records at B5-0337: two live sessions under one
`agent_id` share one claim namespace and cannot see each other's in-flight intent. It has a
direct consequence for this schema — **`live_claims` is only trustworthy if one agent owns
one identity.** My derivation was *correct* (those files really do claim my id) and still
ambiguous, which is the point: the tool cannot distinguish "I hold these" from "a twin of me
holds these."

**No action taken.** Resolving it is a human decision, and writing to the ledger during
another session's seeding window is precisely the collision class this repo keeps recording.

---

## 4. Defects I introduced, and how they were caught

Three of my own. All are disclosed because the report's subject is how verification fails.

### 4.1 Array-collapse wrote 24 non-array `live_claims` — the worst one

```powershell
$rec['live_claims'] = @($existing) + @($derived)   # BUG
```

In PowerShell, **array-plus-empty-array collapses to a scalar.** For every agent holding no
claims, `$derived` was empty, so the result was a bare string rather than an array. 24 of 30
files were written wrong on the first pass.

It then got *worse*: my original code preserved non-string entries by JSON-stringifying
them, so a second pass turned the corruption into the literal string `"{}"` and would have
baked it in permanently. Fixed by dropping non-string entries outright — a `live_claims`
entry that is not a claim identifier is corruption, not data — and by using an explicit
`List[string]` with `.ToArray()`, which serialises `[]` for zero and `["X"]` for one.

**Caught by:** the validator reporting `live_claims is not an array` on 24 files — i.e. the
validator working exactly as intended, on the same pass that introduced the bug.

### 4.2 The validator repeated the same class of error

My first array check was `$data.live_claims -isnot [array]` on the **parsed object**. PowerShell
5.1's `ConvertFrom-Json` does not round-trip array-ness reliably, so the check reported
false violations. Fixed by testing the **raw JSON text** with a regex — the text is the
ground truth for "is this a JSON array", and it is independent of the parser's behaviour.

I had shipped a check that could produce a wrong answer, in a tool whose entire purpose is
to avoid wrong answers.

### 4.3 A formatting bug aborted the migration report

Mixing `Write-Output` strings with `Format-Table` in one pipeline threw
`FormatEntryData is not valid`. The writes had already completed; the summary did not print.
Fixed by emitting objects and letting the caller format them.

### 4.4 A false "28 keys lost" alarm — my own verification was wrong

My first data-preservation check treated a **present key holding a null value** as a lost
key, and reported 28 losses. Re-checked properly (key *presence* vs value): **0 keys lost.**
The 8 real value changes were 7 intentional `live_claims` normalisations plus one
type-preserving re-serialisation.

Recorded because a preservation check that cries wolf is nearly as bad as one that stays
silent: it trains you to ignore it.

---

## 5. Data safety

Full backup taken before any write: `C:\Users\TIWAPE~1\AppData\Local\Temp\opencode\hb-backup-20260927-154545\` (33 files).

| Check | Result |
|---|---|
| Keys lost across 30 compared files | **0** |
| Value changes | 8 — 7 `live_claims` normalisations, 1 type-preserving re-serialisation |
| Files deleted | **0** |
| Files quarantined (moved, preserved) | 2 |
| `b5ccg/` files changed | **0** |

**Quarantined, not deleted** (proposal §6.5, fully reversible):
`.agent/HEARTBEATS/_quarantine/kiro-pi.timestamp` — a 26-byte bare timestamp, not a
heartbeat, duplicating data in `kiro-pi.json`; and `_quarantine/solar-pro4` — 0 bytes, and
` solar-pro4.json` already exists, so a rename would have collided.

**Additive per §6.1:** legacy timestamp keys (`updated_utc`, `heartbeat_utc`,
`last_heartbeat_utc`) are **retained** alongside the new canonical `utc` for one cycle, so no
existing reader breaks mid-pass. The validator reports them as
`NON-CANONICAL TIMESTAMP … (legacy, tolerated one cycle)`.

**Race guard:** if a file's contents change between read and write, the migration reports
`SKIPPED-RACE` and leaves it alone. Overwriting a live heartbeat would silently revert a
concurrent update.

---

## 6. Tools delivered

| File | Purpose |
|---|---|
| `.agent/tools/validate-heartbeats.ps1` | Read-only schema validator. Exits 1 on any non-conforming or unparseable file. Never throws on a bad file — a parse failure is a *finding*, collected and reported at the end, because a survey using `ConvertFrom-Json` over `*.json` threw on the first YAML file and silently returned zero rows. |
| `.agent/tools/migrate-heartbeats.ps1` | Idempotent additive migration. `-DryRun` supported. Verifies every key survived each write. |
| `.agent/00_BOOT.md` step 3 | Points at `HEARTBEATS/README.md` as binding, names `utc` as the only canonical timestamp, requires `live_claims` always present, marks `notes` as prose, and names the validator command. |

The validator's independence is deliberate: it checks properties the writer does not control
— JSON parseability, `agent_id` vs filename stem, `live_claims` array-ness in raw text,
`state` enum membership. It shares no pattern with the code that writes the files, so it can
go red. That is the B5-0613 correction applied to the tool it produced.

---

## 7. Correction to my own prior documents

I asserted the malformed character in `solar-pro4⟨…⟩free.json` was **U+2028 LINE SEPARATOR**,
in both the proposal and the findings report, following the ledger's own description. The
actual codepoint is **U+F03A**, a private-use-area character. Corrected in both documents.
The substance is unchanged — it is a non-ASCII, near-invisible character inside a filename
that defeats naive string matching — but the specific claim was wrong and is now fixed.

---

## 8. What remains, and what I did not do

- **§6.4 renames: not done, not authorised.** 10 files stay non-conforming on
  `agent_id` ≠ stem. This needs a human decision, and it is genuinely delicate: three pairs
  of files assert the *same* `agent_id`, so renaming requires choosing which file is
  authoritative. Doing that automatically would pick a winner and destroy data.
- **`README.md` not rewritten.** The proposal made `utc` binding by pointing `00_BOOT.md` at
  the existing README rather than the reverse, so the spec text itself is unchanged and no
  second source of truth is created.
- **No ledger row, no DECISIONS entry.** Another session holds a live seeding claim under my
  agent id (§3). Appending now would be the collision class on record.
- **Timestamps seeded from mtime where no key existed.** 14 files had no timestamp field at
  all; their `utc` is their file mtime — an honest reading, not an invented time. It is
  flagged in the migration output as `utc seeded from mtime`.
- **The twin-session identity collision is unresolved** and is the highest-value open item,
  because `live_claims` correctness depends on one agent owning one identity.

---

## Reusable lesson

Two bugs in one pass, in opposite directions, from the same root cause: **trusting a
language primitive to preserve a type it does not promise to preserve.** PowerShell's
`@($a) + @()` silently collapses an array to a scalar, and `ConvertFrom-Json` does not
reliably round-trip array-ness — so the writer emitted a non-array and the validator
diagnosed it by a method that could not tell the difference. The first was caught only
because the second was checking; had the validator shared the writer's assumption, both
would have been green and the store would have carried 24 corrupt files indefinitely.

Verify the writer and the checker against **different** assumptions, and when a check
reports mass failure, suspect the check first — a validator that cries wolf on 24 files is
more likely to be wrong than the store.
