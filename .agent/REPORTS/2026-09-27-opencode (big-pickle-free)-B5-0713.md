---
document:
  title: "B5-0713 close-out — the runner's task prompt template had two bare document references"
  status: "Report (observation and verification record; no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0713 — `$TaskPromptTemplate` references qualified

**Task:** self-seeded row B5-0713 on explicit human approval, following the out-of-scope
finding in the B5-0693 close-out. Scope: `.agent/run-queue.ps1` `$TaskPromptTemplate`
here-string (lines 520–534) only. Claimed 2026-09-27T09:51:25Z, closed 10:22Z.
`javac 1.8.0_292` recorded; no Java touched, so no compile gate applies.

## 1. The finding

`$TaskPromptTemplate` is not documentation an agent reads when curious. It is **the string
the runner hands to every unattended agent as its entire instruction set**, so a bare
filename in it is copied verbatim into a live working session and resolved against whatever
cwd the CLI picked. That is the exact mechanism behind the `pi cli` ENOENT that started
this work.

Two instances:

```
line 526:  ... close out fully (TASK_LEDGER.md row,        <- bare filename
line 531:  ... mark BLOCKED per 00_BOOT step 8 and         <- bare EXTENSIONLESS name
```

The second is the interesting one. The B5-0693 census searched for names **carrying a file
extension** (`00_BOOT\.md`, `TASK_LEDGER\.md`, `run-queue\.ps1`, …), so a doc name written
without its extension in running prose was outside its pattern. Extending the sweep to the
extensionless forms found it.

B5-0693 is **not** reopened: re-running the extended census over its three files returns
**zero** residue, the only extensionless hits being `.agent/00_BOOT.md` line 20 and
`.agent/AGENT_LOOP.md` line 14 — those documents' own H1 headings. The earlier row was
correct about everything it measured; its instrument was narrower than the defect, and
those are two different findings.

## 2. What changed (2 lines)

| Line | Before | After |
|---|---|---|
| 526 | `close out fully (TASK_LEDGER.md row,` | `close out fully (.agent/TASK_LEDGER.md row,` |
| 531 | `mark BLOCKED per 00_BOOT step 8 and` | `mark BLOCKED per .agent/00_BOOT.md step 8 and` |

## 3. The rendered prompt, after the fix

```
$TaskPromptTemplate = @'
Boot per .agent/00_BOOT.md, then read .agent/AGENT_LOOP.md and execute its
IDENTITY AND FILENAMES rules and its close-out checklist for this one task:
B5-0713 only. (The claim file was already checked by the runner - re-verify
.agent/CLAIMS/B5-0713.json is absent before creating your own claim, and
re-read the row to confirm it still reads OPEN.) Work ONLY inside the claimed
scope, verify per the row's gate, then close out fully (.agent/TASK_LEDGER.md row,
docs/DECISIONS.md entry, .agent/REPORTS/<date>-<sanitised-agent-id>-B5-0713.md
with one "Reusable lesson" line filed as a NEW file under
.agent/PATTERNS/<agent-id>/, delete your claim file, refresh your heartbeat on the
binding schema in .agent/HEARTBEATS/README.md). Single task, then exit. If the
gate is red from out-of-scope in-flight edits, mark BLOCKED per .agent/00_BOOT.md step 8 and
release - do not fix outside your scope. Use your name and version as agent_id.
Do NOT read .agent/HANDOFF.md: it is superseded and its state section is stale.
'@
```

Every `.agent/`-resident reference in it is now a path that resolves from the repository
root, which is where the runner invokes the agent from.

## 4. Verification

The check extracts the here-string between `$TaskPromptTemplate = @'` and the closing `'@`,
substitutes a real task id, and censuses the **rendered** prompt for bare `.agent/`-resident
names plus their extensionless forms.

| Check | Result |
|---|---|
| LIVE rendered prompt | **0** bare references |
| **Negative control** — template from `git show HEAD:.agent/run-queue.ps1` | **2** (`TASK_LEDGER`, `00_BOOT`) |
| **Negative control** — in-memory revert of my two edits | **2** (`TASK_LEDGER`, `00_BOOT`) |
| AST parse of `.agent/run-queue.ps1` | **0 errors** — the here-string survived |
| `run-queue.ps1 -DryRun` | **exit 0**, 21 lines, 0 warnings |
| Splice assertion | `lines changed: 2 -> 526, 531` |
| CRLF count | 584/584, unchanged (BOM absent before and after) |
| Template `"Reusable lesson"` quoted pair | present exactly 1 |
| `$safePrompt = $prompt -replace …` sanitisation | present exactly 1 |
| Call site `$argList += $safePrompt` | present exactly 1 |

Two controls, not one, and both on the real artefact: the git control is the state the
defect actually shipped in, and the revert control proves the check keys on my two edits
rather than on some unrelated HEAD-versus-working-tree difference.

The B5-0628 quote machinery was **verified, not assumed**. That row records Windows
PowerShell 5.1 failing to escape embedded double quotes for native executables — a
subcommand-style CLI read the trailing fragment as a command name and exited 2. An edit to
this template that dropped a quote would have re-broken the runner for every model that
uses one.

## 5. Classified non-defects, left byte-identical

Seven further bare references in the same file, with the classification recorded so a later
pass can challenge it rather than inherit it:

| Line | Instance | Why not a defect |
|---|---|---|
| 9 | header comment listing directories | comment, directory-listing style |
| 67 | `Join-Path $AgentDir 'TASK_LEDGER.md'` | **correct as written** — the call supplies the directory; qualifying it would break the script |
| 231 | `Write-Warning "… See 00_BOOT.md step 9 …"` | console warning string — **closest call, deliberately not fixed**, see below |
| 396 | `# … per 00_BOOT step 10 as amended.` | comment |
| 440 | `# … notes in TASK_LEDGER.md):` | comment |
| 584 | `Write-Output "run-queue finished."` | the tool's own name, not a path |
| `00_BOOT.md:20`, `AGENT_LOOP.md:14` | `# 00_BOOT — …`, `# AGENT_LOOP` | those documents' own H1 headings |

**Line 231 is the judgment call.** A warning string addressed to a human operator is
copy-pasteable in exactly the way the template is, and an operator who follows it from a
terminal gets the same dead end. I left it because fixing it is a scope expansion inside a
shared executable, and because the right response to noticing a borderline case is a row
that says so rather than an edit that arrives unannounced. It is a two-word change and is
listed here as the obvious next slice.

## 6. Disclosures

**Concurrency risk, live and mitigated mechanically rather than hopefully.** `B5-0660` is
OPEN, UNCLAIMED and scopes `.agent/run-queue.ps1`, and `run-queue.ps1 -DryRun` currently
offers it as the **head claimable row** — so an agent pointed at this repository right now
is handed the same file this row was editing. Guards used: the file's SHA-256
(`E5B6EB54…DBE2679`) recorded at seed time and re-asserted immediately before writing, with
the splice aborting on any change; each target string asserted to occur **exactly once**;
the write asserted `lines changed: 2 -> 526, 531`; and the shipped tool re-run afterwards
rather than the diff re-read.

**Separate finding, recorded and not acted on — needs a human.** The working tree already
contains the whole **B5-0660** implementation as *uncommitted* changes — `git diff` against
`HEAD` shows 132 insertions in this file, including the three-signal liveness triad, the
`Get-ImplausibleClaimTaskIds` candidate filter and the B5-0657 claims-first suppression —
while the `B5-0660` row still reads **OPEN**, unclaimed, with no report. The runner is
therefore offering a task whose work is already in the tree, and nothing in the row says
so. This is the **inverse** of the `B5-0691` invisible-row class found the same hour: there
the work was seeded and unofferable; here the work is done and the row is offerable. Left
alone because the row is not mine to close and the uncommitted work is not mine to claim.

## 7. Untouched, as the row required

TTL, stop conditions, lane and prereq tables, the duplicate-ID assertion, the B5-0624
suffix fix, the B5-0628 quote sanitisation, the B5-0649 two-signal liveness rule and the
B5-0653 implausible-`started_utc` refusal. No `.agent/00_BOOT.md` or `.agent/AGENT_LOOP.md`
edit, no heartbeat/report/pattern rename, no `agent_id` altered, no `b5ccg/src` or
`b5ccg/resources` touch, no commit. Claim released; heartbeat idle. Duplicate-ID census
empty, row `7` pipes / `doubleLead no`, `validate-heartbeats.ps1` exit 0.

**Reusable lesson:** a census anchored to a file extension has declared that the same name
without its extension does not exist — search both forms, and record which census found
what.
Filed as `.agent/PATTERNS/opencode (big-pickle-free)/2026-09-27-anchor-the-census-to-the-defect-not-the-file-extension.md`.
