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
  last_modified_by_llm: {name: "GitHub Copilot (Auto mode) 1249", version: "Auto mode"}
  created_date: "2026-09-21"
  last_modified_date: "2026-10-02"
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
(byte-identical to the `tmp-scans/b50960/census.py` provenance original,
which stays in place and stays ignored; before B5-1877 the documented path
resolved only on this working tree — B5-1667).

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
`=== TRACKED b5ccg/src ===`.** The frozen `src-java8-archive/` is censused by the
same script in the same run and is expected to read the *opposite* (all families
present as code) — that contrast is the instrument's built-in validation. Never edit
the archive.

Standing state, re-measured 2026-10-02 (65 tracked Java files; 32 archive Java files):

The census counts tracked `.java` paths, not every path under each directory.
`git ls-files -- b5ccg/src` currently returns 67 paths because two tracked
coordination JSON files remain under `b5ccg/src/.agent/`; the archive returns
33 paths because its tracked `README.md` is not Java. The working tree has one
additional untracked Java source, `b5ccg/src/b5ccg/engine/B51823DeckCensus.java`,
so the live source tree has 66 Java files while the census intentionally scans
the 65 tracked Java paths. These deltas are not build outputs; the two source-tree
JSON files and archive README are tracked non-Java residue, and the extra Java
file is an untracked source.

| family | tracked `code-lines` | expected | note |
|---|---|---|---|
| arrow `->`, methodref `::`, stream, computeIfAbsent, computeIfPresent, compute, merge, @FunctionalInterface, try-with-resources, diamond, forEach, removeIf | **0** | 0 | any nonzero code-line count is a real finding — name the file and line; do not seed |
| arrow **prose** lines | 50 (64 occurrences) | grows with comments | comment bands only; prose is not a violation |
| `getOrDefault` | 14, all the project's own unqualified helper | 14 | `DeckLoader.getOrDefault(Map, key, def)`, private static |
| **qualified** `.getOrDefault(` | **0** | 0 | *this* number, not the raw 14, is the API-level signal |

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

Boot: `.agent/00_BOOT.md`. Tasks: `.agent/TASK_LEDGER.md`. Claim before edit
with `.agent/tools/new-claim.ps1` (atomic — if the claim file exists the task
is taken); heartbeat at `.agent/HEARTBEATS/<agent-id>.json`; report to
`.agent/REPORTS/<date>-<agent-id>-<task-id>.md`. One writer per
scope (`engine/`, `model/`, `ai/`, `ui/`); 30-min TTL; small diffs; never touch
another agent's claim or heartbeat. Git is change-tracking only, not authority.

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
