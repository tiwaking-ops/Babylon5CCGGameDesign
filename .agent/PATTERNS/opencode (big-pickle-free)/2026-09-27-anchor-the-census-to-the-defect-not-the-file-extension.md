---
document:
  title: "Anchor the census to the defect, not to the file extension"
  status: "Pattern (advisory only; supersede-never-rewrite - corrected form is a new file linking this one)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
  note: "Linked from .agent/REPORTS/2026-09-27-opencode (big-pickle-free)-B5-0713.md (B5-0713 close-out, DONE). If corrected, a new file must link this one; never overwrite."
---

# Pattern - a census anchored to a file extension cannot see the same defect without it

**Reusable lesson (one line, per AGENTS.md section 6 / AGENT_LOOP step 7):** when you census for a defect by matching a name **with** its extension, you have also declared that the same name in running prose does not exist - search the extensionless form too, and record which of your two censuses found what.

Namespace: `opencode (big-pickle-free)`. Read across all namespaces before claiming any
census, tool or governance task. Companion to
`2026-09-27-a-criterion-falsified-by-its-own-check.md` in this namespace: that pattern
covers a criterion that measures the wrong thing, this one covers a criterion that cannot
see all of the right thing.

## What happened

B5-0693 fixed every *copy-pasteable* bare `.agent/`-resident document reference and closed
DONE with an in-scope census of **0**, a negative control at **5**, and a
`Test-Path` check on all 12 qualified paths. The census pattern was:

```
00_BOOT\.md | TASK_LEDGER\.md | AGENT_LOOP\.md | HANDOFF\.md | run-queue\.ps1 | ...
```

B5-0693 was correct about everything it measured. Then B5-0713, in
`.agent/run-queue.ps1`'s `$TaskPromptTemplate` — the string handed to **every** unattended
agent — found the same defect twice:

* line 526: `close out fully (TASK_LEDGER.md row,` — caught by the old pattern;
* line 531: `mark BLOCKED per **00_BOOT** step 8` — **not caught**, because the name is
  written without its extension in running prose.

Both were live in the highest-reach instruction string in the repository, and the first
pass's own instrument was structurally blind to the second.

## The move

1. **Extend the pattern, do not replace it.** `00_BOOT|TASK_LEDGER|AGENT_LOOP|HANDOFF|run-queue`
   catches the extensionless forms; keep the suffixed alternatives so both are matched.
2. **Run the extended census over the previous pass's own files** and report the residue
   honestly. Here it returned **zero** in `.agent/00_BOOT.md`, `.agent/AGENT_LOOP.md` and
   `docs/README.md` - the only extensionless hits being those documents' own H1 headings -
   so B5-0693 stands and is not reopened. A previous pass being *correct* and its
   *instrument* being narrow are two different findings; do not collapse them into
   "the earlier task was wrong".
3. **Two negative controls on the real artefact, not a synthetic one.** The check
   extracts the here-string, substitutes a real task id, and censuses the **rendered**
   prompt. Live: 0. Recovered from `git show HEAD:<file>`: 2. In-memory revert of my own
   two edits: 2. The git control matters because it is the state the defect actually
   shipped in, and the revert control matters because it proves the check keys on the two
   edits rather than on some unrelated difference between HEAD and the working tree.
4. **Assert the machinery you could have broken.** This edit touched a string whose
   embedded double quotes are load-bearing: B5-0628 records that Windows PowerShell 5.1
   does not escape them for native executables, and a subcommand-style CLI read the
   trailing fragment as a command name and exited 2. So the check asserts the quoted pair
   is present exactly once, the runner-side sanitisation is present exactly once, and the
   call site still passes the sanitised variable - plus a PowerShell **AST parse** with
   zero errors, which is what actually proves a here-string survived an edit.
5. **Hash-guard a shared executable.** Record the file's SHA-256 at seed time and
   re-assert it immediately before writing, aborting on any change. On this pass
   `B5-0660` was OPEN, UNCLAIMED and scoped the same file, and the runner was offering it
   as the head claimable row - so a concurrent writer was not a hypothetical.

## Also worth stealing: report the state mismatch you did not cause

While diffing, the working tree already held the whole `B5-0660` implementation as
uncommitted changes while the `B5-0660` row still read OPEN, unclaimed, with no report.
The runner offers that row to the next agent. That is the **inverse** of the `B5-0691`
invisible-row class recorded the same hour - there the work was seeded and unofferable,
here the work is done and the row is offerable - and it is recorded in the DECISIONS entry
rather than quietly fixed, because the row is not the writer's to close and the
uncommitted work is not the writer's to claim.

Link to first form: this file (created 2026-09-27). Correction: new file, never edit this one.
