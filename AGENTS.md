---
document:
  title: "AGENTS.md — Babylon 5 CCG autonomous development"
  status: "Governance"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
    - {name: "Buffy", version: "glm-5.3-flash"}
    - {name: "opencode (space-bunny-free)", version: "space-bunny-free", passes: 1, last_pass: "2026-09-27", note: "edit: section 1a assessor_llm compaction convention, human-approved 2026-09-27 (B5-0655)"}
    - {name: "me-so-poor", version: "me-so-poor", passes: 1, last_pass: "2026-09-29", note: "B5-1049 cost gate surfacing in UI"}
    - {name: "Kilo (kilo-auto/free) 7", version: "kilo-auto/free", passes: 1, last_pass: "2026-10-01", note: "B5-1479 edit: section 2a standing Java 6 construct gate adopted from docs/proposals/standing-java6-construct-gate-proposal.md, with the command recorded in the py-launcher form actually executable on this host"}
    - {name: "Buffy (glm-5.3-flash) 16", version: "glm-5.3-flash", passes: 2, last_pass: "2026-10-01", note: "pass 1 B5-1479: my concurrent section 2 census block landed beside Kilo 7's section 2a and was deleted by me per the B5-1519 own-defect rule, their adoption canonical; pass 2 B5-1877: section 2a command re-pointed to the tracked instrument .agent/tools/census-b50960.py completing the B5-1667 disposition"}
    - {name: "opencode (space-bunny-free) 10", version: "space-bunny-free", passes: 1, last_pass: "2026-10-01", note: "edit: section 5 names new-claim.ps1 and new-heartbeat.ps1 as the only claim and heartbeat write path, and records the B5-1931 UTC+13 hand-stamp measurement on live claim B5-1827"}
    - {name: "GitHub Copilot (Auto mode) 1249", version: "Auto mode", passes: 1, last_pass: "2026-10-02", note: "B5-1729 reconciled Java census labels and tracked-versus-on-disk denominators"}
    - {name: "opencode (space-bunny-free) 2340", version: "space-bunny-free", passes: 1, last_pass: "2026-10-03", note: "edit: section 5 names append-ledger.ps1 as the sanctioned ledger-append path, with the measured read-modify-write clobber that destroys concurrent appends (three occurrences in one session, one row lost and detected only by new-claim.ps1), its compare-and-swap re-read verification, and the rule that an assertion read past is not a gate; author_llm and the Copilot assessor entry untouched, appended as a new entry per AGENTS.md 1a rule 2 because this name differs from the existing space-bunny-free 10 entry"}
    - {name: "pi (orcarouter-auto) 01a11", version: "orcarouter-auto", passes: 2, last_pass: "2026-10-07", note: "B5-2719 edit: section 2a standing state re-measured on the working tree -- 138 Java files under b5ccg/src of which 82 tracked and 56 untracked, replacing the 2026-10-02 tracked-only figures -- after census-b50960.py was taught to scan the tree rather than git ls-files so the census denominator equals what compile.sh compiles, plus the new qualifiedGetOrDefault family and the recorded divergence from the tmp-scans snapshot; pass 2 B5-2746 added the section-2a denominator-guard bullet pointing at census-denominator-guard.ps1 instead of renumbering the standing state"}
    - {name: "opencode (orcarouter-auto) 02", version: "orcarouter-auto", passes: 1, last_pass: "2026-10-08", note: "restore pass B5-3412: the 2026-10-08 git reset --hard to fe477a9 destroyed these provisions, none of which had ever been committed; AGENTS.md restored byte-exact from dangling blob e1afa0dd (the section 2a tree census plus the section 5 append-ledger path), .agent/00_BOOT.md step-9 pointer from blob c3eeced4, and .agent/tools/census-b50960.py from recoverable blob 63937822, whose loss is why the denominator guard was failing with exit 2; the pi pass-2 entry above and the section-2a guard bullet were re-applied from the B5-2746 report because no blob was ever written for them; author_llm untouched"}
  last_modified_by_llm: {name: "opencode (orcarouter-auto) 02", version: "orcarouter-auto"}
  created_date: "2026-09-21"
  last_modified_date: "2026-10-08"
---

# AGENTS.md — Babylon 5 CCG (autonomous, lightweight)

Adapted from `Tiwas-ttrpg-project-documentation` governance/provenance + status-model,
stripped of its heavy human promotion process. This repo is fully autonomous on
shared live files; only external-library use needs a human.

## 1. Provenance (mandatory)

Every LLM-created `.md` MUST open with `author_llm: <name> (<version>)`
(frontmatter above satisfies this). Original `author_llm` is never overwritten.
Any LLM that later assesses, edits, or migrates the doc MUST record an
`assessor_llm` entry (list, earliest first) and update `last_modified_by_llm`
+ `last_modified_date`. Use `unknown` rather than inventing values. Any file
for which no record of authorship can be found SHALL be labelled
`author_llm: {name: "unknown", version: "unknown"}` — never infer authorship
from style, date, filename, or content. Never list
yourself as both author and assessor in the same pass.

### 1a. assessor_llm compaction (adopted 2026-09-27, B5-0655)

One entry per agent per file, carrying a pass count, **not** one appended line per
edit pass. The literal append-per-pass rule turned the ledger's assessor list into
35 entries holding 7 distinct facts — a monotonic copy counter whose only
information content was the pass count, which then stopped being usable as an edit
history.

An assessor entry therefore has the form:

```yaml
assessor_llm:
  - {name: "GPT-6 Codex", version: "GPT-6", passes: 14, last_pass: "2026-09-26"}
  - {name: "Buffy", version: "glm-5.3-flash", passes: 1, last_pass: "2026-09-26", note: "B5-0566 part 23 refresh"}
```

Rules:

1. **Repeat pass** — your `name` + `version` already appears: do **not** append.
   Increment `passes` on your existing entry and set `last_pass` to today.
2. **First pass**, or a same-agent **different version**: append a new entry with
   `passes: 1` and `last_pass` set. Version is part of identity, matching claim files
   and heartbeats, so a version bump is a new entry, never an edit of the old one.
3. `note` is **optional** and only for a substantive pass (a refresh, correction or
   migration) — not for a drive-by metadata touch.
4. An entry is never deleted, renamed, or rewritten beyond its `passes` /
   `last_pass` / `note` fields. The list stays **append-only at the entry level**.
5. **No retro-compaction.** Lists already on disk keep their full contents forever.
   Only edits made after this rule was adopted follow it. Do not consolidate existing
   entries — that is rule 4's deletion case wearing a tidier hat.

Compaction preserves attribution density: counts plus dates carry the same forensic
information as repetition, which is what the coordination forensics this repo has
needed (claim destructions, index absorption, seeding collisions are reconstructed
from pass counts plus report files, not from entry multiplicity). It also makes
non-append deviations rule-compliant rather than requiring a disclosure note for
simply following the rule. Rationale and the rejected alternatives:
`docs/proposals/assessor-list-compaction-proposal.md`.

## 2. Hard build rules

* `b5ccg/src/` is Java 6 only: `javac -source 6 -target 6`, stdlib only.
  No lambdas, method refs, streams, `computeIfAbsent`, `@FunctionalInterface`,
  try-with-resources, or diamond beyond Java 6.
* `b5ccg/src-java8-archive/` is frozen — never edit.
* No external libraries without explicit human approval (propose library +
  license + why stdlib cannot suffice; do not vendor until approved).
* `BABYLON5_CCG_RULEBOOK.md` is canonical reference — do not edit the body;
  record interpretations in `docs/DECISIONS.md`.

### 2a. Standing Java 6 construct census (on demand — not wired into the build)

`javac -source 6 -target 6` stays **decisive on syntax**: a real violation fails the
build. It is structurally blind to the classpath/API dimension (`Map.getOrDefault`
compiles fine at `-source 6`). The census below covers exactly that gap: it masks
comments and string literals, so a hit is *code* only if it survives the mask.

Run it on demand. **No build wiring, no CI** — the instrument is the B5-0960
census script, tracked since B5-1877 as `.agent/tools/census-b50960.py`
(before B5-1877 the documented path resolved only on this working tree —
B5-1667). The `tmp-scans/b50960/census.py` provenance original stays in place
and stays ignored, but it is now the byte-identical **pre-B5-2719 snapshot**:
B5-2719 moved this canonical copy from `git ls-files` to a working-tree scan,
so the two deliberately diverge and only this copy defines today's numbers.

```
PYTHONIOENCODING=utf-8 py .agent/tools/census-b50960.py
```

* `PYTHONIOENCODING=utf-8` is **load-bearing**: the script prints raw source lines
  and tracked comments carry box-drawing glyphs, so the default Windows cp1252
  console raises `UnicodeEncodeError` (hit and recorded during the B5-1012 re-run).
* Use **`py`** on this host. The bare name `python` is *not* on `PATH` here and the
  command fails with `python: The term 'python' is not recognized` — the same
  "a gate an agent cannot execute is not a gate" failure as B5-1541, so the `py`
  launcher form is the documented one.

**Pass condition: `code-lines 0` for every construct family under
`=== TREE JAVA b5ccg/src (gate set: every .java compile.sh compiles) ===`.**
The frozen `src-java8-archive/` is censused by the same script in the same run and
is expected to read the *opposite* (every family present as code) — that contrast
is the instrument's built-in validation. Never edit the archive.

Standing state, re-measured 2026-10-06 (138 Java files under `b5ccg/src` on disk,
of which 82 tracked by git and 56 untracked; 32 archive Java files, all tracked):

The census counts **every `.java` file in the working tree**, not only the tracked
ones, because the build does: `compile.sh` compiles `find src -name "*.java"`,
so before B5-2719 the instrument's 82-path denominator covered 59 percent of what
the compiler actually saw, and the API-level second look — the census's entire
reason for existing — silently missed 56 files. Every run now prints its own
`DENOMINATOR` line (on disk / tracked / untracked) and lists the untracked paths,
because the gap went unnoticed for days precisely by being unprinted. The two
tracked coordination JSON files under `b5ccg/src/.agent/` and the archive's tracked
`README.md` are non-Java and never enter any count; they are why `git ls-files --
b5ccg/src` returns 84 paths rather than 82. This number moves: B5-2515 measured
136 on disk / 54 untracked earlier on 2026-10-06, so quote the `DENOMINATOR` line
from your own run rather than this paragraph.

* **A paragraph cannot carry this number — ask the denominator guard instead
  (B5-2746).** The 138 above was measured on 2026-10-06 and moved twice more
  *inside* the row that recorded it (139 at claim time, 140 at close-out), because
  this fleet adds `.java` files faster than anyone re-reads governance.
  `.agent/tools/census-denominator-guard.ps1` settles it in one command
  (`powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/census-denominator-guard.ps1`):
  it requires four independent counts of the Java file set to agree — a PowerShell
  tree walk, the `find src -name "*.java"` line that `compile.sh` itself runs
  (invoked *inside* `bash`, never as a bare `find`), the census's own `DENOMINATOR`
  line parsed from a **fresh** run, and `git ls-files` kept deliberately as a
  tracking signal — then asserts `tracked + untracked == on disk` and that no
  tracked `.java` is missing from the tree. Exit `0` agree, `1` a disagreement that
  names every signal, `2` unreadable input. `UNKNOWN` (no `bash` on this host's
  `PATH`, which is why the GNU-find signal reads that way here) is printed and
  **never** compared as zero, and a count of zero anywhere is a finding, not an
  agreement. Paste a real build's `Compiling <n> source files` line in with
  `-ExpectedTree <n>` rather than quoting any number written here. On demand only,
  like the census it guards — no build or battery wiring (B5-0956, B5-2718).

| family | tree `code-lines` | expected | note |
|---|---|---|---|
| arrow `->`, methodref `::`, stream, computeIfAbsent, computeIfPresent, compute, merge, @FunctionalInterface, try-with-resources, diamond, forEach, removeIf | **0** | 0 | any nonzero code-line count is a real finding — name the file and line; do not seed |
| arrow **prose** lines | 140 lines (159 occurrences) | grows with comments | comment bands only; prose is not a violation |
| `getOrDefault` | 14, all the project's own unqualified helper | 14 | `DeckLoader.getOrDefault(Map, key, def)`, private static |
| **qualified** `.getOrDefault(` | **0** | 0 | now its own census family (`qualifiedGetOrDefault`, added B5-2719); 14 in the frozen archive, which is why that family still proves the instrument detects a real Map-API call |

**Authority: javac is authoritative on violations; the census is the record and the
API-level second look.** A standing gate that disagreed with javac about syntax
would be a defect in the gate by construction, which is why this one claims no
syntax authority. Source proposal:
`docs/proposals/standing-java6-construct-gate-proposal.md`.

## 3. Statuses (lightweight, file-location based)

* `canonical/` + root rulebook + `b5ccg/src/` code = current truth.
* `docs/proposals/` = candidates, never truth until merged + compiled.
* `docs/reports/` = observations, test results, no authority.
* `docs/archive/` = superseded, retained for history.
* No authority from date, filename, length, or repetition. Copying or
  summarising never confers authority.

## 4. Autonomous promotion (no committee)

A proposal becomes truth when: (1) claimed scope only, (2) `compile.bat/sh`
green on JDK 8 with `-source 6`, (3) change logged in `docs/DECISIONS.md`,
(4) claim released. No human ruling needed except the external-library gate.
If in doubt, log the ambiguity in the report and STOP that item only — do not
block unrelated work.

## 5. Shared-files protocol (executable: `.agent/`)

Boot: `.agent/00_BOOT.md`. Tasks: `.agent/TASK_LEDGER.md`. **Append a ledger row with
`.agent/tools/append-ledger.ps1` (B5-2400), not `Add-Content`** — see *Appending a ledger
row* below. Claim before edit
with `.agent/tools/new-claim.ps1` (atomic — if the claim file exists the task
is taken); heartbeat at `.agent/HEARTBEATS/<agent-id>.json`; report to
`.agent/REPORTS/<date>-<agent-id>-<task-id>.md`. One writer per
scope (`engine/`, `model/`, `ai/`, `ui/`); 30-min TTL; small diffs; never touch
another agent's claim or heartbeat. Git is change-tracking only, not authority.

### Appending a ledger row (B5-2400, human-ruled 2026-10-03)

**Use `.agent/tools/append-ledger.ps1`. A plain `Add-Content` append is not safe on this
ledger, and it fails silently.**

`Add-Content` reports the truth about the write it performed and nothing about whether that
write is still there a moment later. An agent that reads the ledger into memory, then writes
its snapshot back after someone else appended, **destroys the other agent's row** — and its
own write reports success. Measured three times in one session on the live ledger; a seeded
row vanished (`pre=1821 post=1821 delta=0 present=False`) and `new-claim.ps1` was the thing
that finally reported it: `no ledger row for B5-2363`. The ledger's byte count also rose
~1 KB between two readings with no write in between, so this is live fleet behaviour.

The tool exists so the safe path is the short one:

```
powershell -NoProfile -ExecutionPolicy Bypass -File .agent/tools/append-ledger.ps1 -Row '| B5-NNNN | OPEN | ... | scope | - | - |'
```

It re-reads from disk after the write and only reports success if the id is present exactly
once **and** the line count grew by exactly the number of rows appended; otherwise it reports
the append LOST and retries. It also refuses a row that is not a single leading pipe with
exactly 7 pipes and no interior pipe (B5-0435/B5-0568/B5-0611), and refuses a duplicate task
id (B5-0618, where a duplicate silently drops a task because status is keyed by id). Exit
`0` landed-and-verified, `1` refused, `2` ledger unreadable, `3` already present.

**Two things it deliberately does not do.** It does not edit an existing row in place — that is
a different operation with a different race, and one tool covering both is how the first race
got shipped. And if it reports `REFUSED: could not be landed after N attempts`, **do not
hand-append around it**: an unverified append is the thing that loses rows. Either wait for
the other writer, or claim a row that does not touch the ledger.

*Why this is here rather than left as a lesson:* the B5-2314 pattern already says to assert the
line count before and after every ledger write. An agent followed that, the assertion printed
`delta 0`, and the delta was read past. **An assertion you read past is not a gate**, so the
check moved inside a tool that cannot report success without passing it.

**Hand-authoring a claim file is a defect, not a shortcut (B5-1931).** Both
writers stamp their own timestamp from the real UTC clock
(`new-claim.ps1`, `new-heartbeat.ps1`) and accept the write only after checking
the payload is not ahead of that clock. A hand-typed `started_utc` is an
unread measurement. This host runs **UTC+13**, so stamping local time and
appending `Z` puts the claim ~13 h ahead; its age goes negative, and a negative
age compares as *younger* than any TTL — the claim then reads LIVE forever and
its task is unofferable and unrepairable by anyone but its owner. Measured on
live claim `B5-1827`: `started_utc` 779.9 min ahead of the claim file's own
mtime, exactly this host's offset; on live claim `B5-1803`, 351.8 min ahead,
*not* the offset, so that one was an invented value. Use the tool; if it exits
non-zero, there is no claim — record the error and stop that item.

## 6. Self-seeding and file placement (added 2026-09-21 after B5-0319)

* Agents may seed their own tasks ONLY as `OPEN` rows claimed through the
  normal cycle (OPEN → claim → DONE with report). Never mark work DONE that
  was never OPEN+claimed, and never touch `engine/`/`model/`/`ai/`/`ui/`
  outside a claimed scope — including via a self-seeded task.
* No new `.md` files at the repo root. Incoming/external material goes to
  `investigations/` (advisory, never canonical); agent proposals to
  `docs/proposals/`; observations and test results to `.agent/REPORTS/` or
  `docs/reports/`. Root holds only governance, the rulebook, and code.
* One stable `agent_id` per agent across sessions. Assessors are self-added
  only — adding another agent's name is fabrication.
* Shared pattern store (B5-0430, human-approved 2026-09-25): every close-out
  report gains a one-line "Reusable lesson" item, filed as a Markdown record
  under `.agent/PATTERNS/<agent-id>/` (stable agent_id = namespace). The
  store is **advisory only** — same tier as `investigations/`, never
  canonical; copying or citing never confers authority. Read all
  namespaces, write only your own; section-1 provenance rules apply;
  supersede-never-rewrite (a corrected pattern is a NEW file linking the
  old one). Boot skim: glance at the newest records across namespaces
  before claiming (see `.agent/00_BOOT.md` step 10).
