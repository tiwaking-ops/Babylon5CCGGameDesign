---
document:
  title: "B5-0693 close-out — qualify every copy-pasteable .agent/-resident document reference"
  status: "Report (observation and verification record; no authority)"
provenance:
  author_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle-free)", version: "big-pickle-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0693 — bare `.agent/` document references qualified

**Task:** self-seeded row B5-0693 (see the `QUEUE 0693` note), scope `.agent/AGENT_LOOP.md`,
`.agent/00_BOOT.md`, `docs/README.md`, documentation only. Claimed 2026-09-27T09:39:52Z,
closed 10:04Z. `javac 1.8.0_292` recorded; no Java touched, so no compile gate applies.

## 1. The trigger was a real failure

A `pi cli` run on 2026-09-27 was pointed at `.agent/HANDOFF.md` — superseded per
B5-0626 — and exited with:

```
ENOENT: no such file or directory, access 'c:\temp\projects\Babylon5CCGGameDesign\00_BOOT.md'
```

The agent resolved a **bare filename against the repository root**, where no `00_BOOT.md`
exists. The string it was working from is the precedence clause *"AGENTS.md and 00_BOOT.md
win on conflict"*, which is the frontmatter `status:` line at `.agent/AGENT_LOOP.md`
line 4 and was the **only** unqualified `00_BOOT.md` reference in that file — line 17 of
the same document had it right as `.agent/00_BOOT.md`. Two forms of the same sentence in
one file is what made the bare one read as normal.

## 2. Census before the edit

Negative-lookbehind scan for bare `.agent/`-resident document names, so hits inside
`.agent/TASK_LEDGER.md` and `docs/DECISIONS.md` (which legitimately contain the
filenames) are not counted:

| File | qualified | bare |
|---|---|---|
| `.agent/00_BOOT.md` | 6 | 3 |
| `.agent/AGENT_LOOP.md` | 5 | 1 |
| `AGENTS.md` | 2 | 0 |
| `docs/README.md` | 0 | 2 |

`AGENTS.md` is the canonical style: fully qualified, zero bare. Sharpest instance:
`.agent/00_BOOT.md` step 4 carried the bare form at line 36 and the qualified form at
line 38 — two lines apart, inside one step.

## 3. What changed (6 references, 3 files)

* `.agent/AGENT_LOOP.md:4` — status line `00_BOOT.md` → `.agent/00_BOOT.md`.
* `.agent/AGENT_LOOP.md:89` — table clause `` `run-queue.ps1` `` → `` `.agent/run-queue.ps1` ``.
* `.agent/00_BOOT.md:36` — step 4 `` `TASK_LEDGER.md` `` → `` `.agent/TASK_LEDGER.md` ``.
* `.agent/00_BOOT.md:73` — step 9, same substitution.
* `.agent/00_BOOT.md:113` — step 10, same substitution.
* `docs/README.md:22-23` — the two references qualified, and the `../.agent/`
  file-relative anchor replaced by the repo-root-relative `.agent/` form that
  `AGENTS.md` and `.agent/00_BOOT.md` already use. **Recorded as a deliberate style
  change**, because it alters how that bullet resolves when read from inside `docs/`.

Provenance per `AGENTS.md` § 1a: one appended entry per file, `passes: 1`,
`last_pass: 2026-09-27`, `last_modified_by_llm` / `last_modified_date` updated. Original
`author_llm` preserved byte-identical (`Muse Spark` ×2, `opencode (space-bunny-free)` ×1).
A **new** entry was appended rather than an existing one incremented: version is part of
identity and `big-pickle-free` differs from every entry already on disk. No entry deleted,
renamed, or rewritten beyond its own `note` field.

## 4. The acceptance criterion was falsified by its own check — reported, not hidden

The row as seeded required a census of **ZERO** bare instances across the four live
coordination docs. The first post-edit run returned **8**:

```
.agent\00_BOOT.md:14          BARE [TASK_LEDGER.md]     <- my own assessor note
.agent\AGENT_LOOP.md:8        BARE [00_BOOT.md]          <- my own assessor note
.agent\AGENT_LOOP.md:89       BARE [run-queue.ps1]       <- prose table-cell label
.agent\run-queue.ps1:67       BARE [TASK_LEDGER.md]      <- Join-Path argument, CORRECT as written
.agent\run-queue.ps1:231      BARE [00_BOOT.md]          <- console warning string
.agent\run-queue.ps1:440      BARE [TASK_LEDGER.md]      <- comment
.agent\run-queue.ps1:526      BARE [TASK_LEDGER.md]      <- INSIDE $TaskPromptTemplate
.agent\HEARTBEATS\README.md:19 BARE [00_BOOT.md]         <- prose, anchored next line
```

Classification by **position**, not by string match, is what separates the defect from the
mentions: the defect is *a string that, copied out of the document, resolves to a path
that does not exist*. `run-queue.ps1:67` is the instructive one — the bare name is right
there, because `Join-Path $AgentDir` supplies the directory, and "qualifying" it would
break the script.

Actions taken, in order:

1. The two self-inflicted instances (my own assessor notes naming the documents they were
   fixing) were fixed. A rule broken in the act of documenting it is the strongest
   evidence the rule is real.
2. The criterion was **narrowed in the open** to what can actually fail: zero bare
   references in *copy-pasteable instruction position* inside the three claimed files.
3. A **negative control** was kept so the passing zero means something:

```
IN SCOPE (.agent/00_BOOT.md, .agent/AGENT_LOOP.md, docs/README.md): 0 bare
NEGATIVE CONTROL (.agent/HANDOFF.md, superseded, deliberately unfixed): 5 bare
   .agent\HANDOFF.md:8   BARE [run-queue.ps1]
   .agent\HANDOFF.md:54  BARE [00_BOOT.md]
   .agent\HANDOFF.md:54  BARE [TASK_LEDGER.md]
   .agent\HANDOFF.md:102 BARE [TASK_LEDGER.md]
   .agent\HANDOFF.md:105 BARE [TASK_LEDGER.md]
```

A criterion never observed red is not evidence (`.agent/AGENT_LOOP.md` § *The three ways
this loop has lied to itself*, item 3). The same run observed it red.

## 5. Out of scope: found, reported, NOT edited

* **`.agent/run-queue.ps1:526`, inside `$TaskPromptTemplate`** — `close out fully
  (TASK_LEDGER.md row, ...)`. This is the string handed to **every unattended agent**, so
  it has the highest reach of any instance found. It needs a claimed row of its own; a
  fix outside a claim is the failure B5-0653 closed the protocol against, and a fix
  smuggled into a report nobody reads is the same fix with worse bookkeeping. **This is
  the recommended next task.**
* `run-queue.ps1:67` (correct as written), `:231` (warning string), `:440` (comment).
* `.agent/HEARTBEATS/README.md:19` — prose, anchored by the qualified validator path on
  line 20.
* `.agent/HANDOFF.md` — **excluded by design**: superseded, retained as history, and each
  bare reference is directory-anchored by a `.agent/` prefix in the same sentence.
  Editing a history document to look current would destroy the evidence of what the repo
  looked like on day one.

## 6. Disclosures

**ID collision, resolved by the other writer.** `muse-spark` seeded its own `B5-0693`
concurrently; its post-write duplicate-ID census found the duplicate, it diverged to
non-adjacent `B5-0711`, re-pointed its gates, and left this row byte-identical. Verified
here afterwards: exactly one `B5-0693` row exists, at 7 pipes. Correct handling on both
sides — the collision was caught by the post-write census, which a pre-write
free-ID check could not have caught.

**Side effect on another agent's row, disclosed because it is not mine to take credit
for.** The `B5-0691` row seeded minutes earlier by `Buffy (glm-5.3-flash)` was
**invisible to every census tool**: its physical line ended with a literal two-character
`\n` rather than a newline, so it was glued onto the `QUEUE 0691` narrative line and
matched no row pattern. The work was unofferable and the `B5-0683/0687/0689` chain it was
seeded to unstall could not have advanced. My append **opened with a real line
terminator** — necessary in its own right, since the file's last line had none and a naive
append would have inherited the defect and made *my* row invisible too — and that
terminator split the two. The census now reports `B5-0691 | OPEN | 7 | no | UNCLAIMED |
reportable`. **No byte of that row was edited by me.** The underlying literal-`\n` defect
class is recorded here as unfixed and unclaimed.

**Whole-file writes avoided.** The ledger row was closed out by decoding to text,
asserting exactly one match of the target row, replacing that single line, re-encoding
UTF-8 without BOM, then asserting that exactly one line differs from the intended set
(`rows changed: 1; unexpected diffs elsewhere: 0`). Note for context: a concurrent agent
normalised the ledger to LF endings during this task, so a whole-file rewrite here would
have produced a 10 KB spurious diff and could have raced their write.

## 7. Verification

| Check | Result |
|---|---|
| In-scope bare-reference census | **0** |
| Negative control (`.agent/HANDOFF.md`, unfixed) | **5** — check observed red |
| `Test-Path` on all 12 qualified paths used in the 3 edited files | all `True` |
| Post-write duplicate-ID census | empty |
| Row pipe count / `doubleLead` | `7` / `no` |
| `validate-heartbeats.ps1` | exit 0 |
| `run-queue.ps1 -DryRun` | exit 0, no warnings |
| Compile | n/a — no Java touched |

## 8. For the human

The prompt that failed is fixed at source, so re-running it as-is now resolves. Two
points still need a decision, neither of which I took unilaterally:

1. **Seed `B5-0713` (say) for `.agent/run-queue.ps1:526`** — the same defect inside the
   template every unattended agent receives. I did not self-seed it because this pass
   already consumed its one seeding action on B5-0693, and because it is a claim on a
   shared executable.
2. **`B5-0687` and `B5-0689` are UNCLAIMED but gated**, and `B5-0683` is live-claimed, so
   `run-queue.ps1` currently has no *unblocked* row to offer. A loop pointed at it will
   report a queue that is full and idle at the same time.

**Reusable lesson:** a criterion falsified by its own first run must be corrected in the
open — print the failing output, classify by position, narrow to a property that can
still fail, and keep a negative control — because a quietly retargeted check is
indistinguishable from one that was never wrong.
Filed as `.agent/PATTERNS/opencode (big-pickle-free)/2026-09-27-a-criterion-falsified-by-its-own-check.md`.
