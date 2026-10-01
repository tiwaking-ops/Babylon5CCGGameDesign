---
document:
  title: "Autonomous Project Management System — Replication Manual (2026-09-21 to 2026-09-29)"
  status: "Report (observation tier — describes, does not govern; copying confers no authority)"
  audience: "Autonomous AI agents provisioning this system in a new repo"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
source_system: "Babylon5CCGGameDesign — AGENTS.md + guidelines/Guidelines.md + .agent/ protocol + docs/DECISIONS.md (494 ledger rows, 593 close-out reports at time of writing)"
---

# Autonomous Project Management System — Replication Manual

## 0. How to read this report

- **Tier:** `docs/reports/` = observations, test results, no authority (AGENTS.md §3). This manual describes the source system; it does not govern it and does not govern any target. Promotion in any repo happens only through that repo's own `AGENTS.md` + gate + decision log.
- **Author field:** `author_llm: Muse Spark (muse-spark-1.3-contributor-free)` — required by §1 provenance.
- **Conventions:** MUST = load-bearing (violating corrupts coordination); SHOULD = strong convention, deviations recorded; MAY = optional.
- **Guesses are flagged** inline as `[GUESS]` — see §14.

## Tailored prompt this report answers

> **Role:** Systems documentarian writing for autonomous AI agents (not humans) who must provision and run this system without asking questions.
> **Context:** Reverse-specify the live autonomous PM system in `Babylon5CCGGameDesign` (2026-09-21→2026-09-29) so it can be replicated into new game-development repos, both documentation-only and documentation+coding variants.
> **Audience:** Autonomous AI worker agents bootstrapping a new repo headlessly; secondary reader is the single human owner who approves dependencies and rulings.
> **Format and length:** Full ops manual in Markdown: directory map, hierarchy, authority tiers, documentation system, coordination protocol, gates, task lifecycle, dated improvement history, failure→fix table, copy-paste replication kit with two variants (doc-only / doc+code) plus game-dev tailoring, templates and checklists.
> **Success criteria:** (1) An agent with only this manual can provision a working `.agent/` + governance + decision log and complete one OPEN→DONE cycle without corrupting a concurrent writer. (2) Every authority claim names the file that confers it; no date/length/repetition authority. (3) Every post-creation improvement is dated, failure-linked, and actionable (what to copy vs. what to avoid).
> **Constraints:** No new `.md` at repo root; every LLM-created `.md` carries immutable `author_llm` + append-only `assessor_llm`; Git is change-tracking only, never authority; report tier never promotes itself; do not edit foreign claims/heartbeats; hard build rules stay in target's `AGENTS.md`, not here.
> **Examples:** No user-supplied example; source references are `AGENTS.md`, `guidelines/Guidelines.md`, `.agent/00_BOOT.md`, `.agent/AGENT_LOOP.md`, `.agent/CLAIMS/README.md`, `.agent/HEARTBEATS/README.md`, `.agent/PATTERNS/README.md`, `docs/DECISIONS.md`, `docs/proposals/*`, prior art `docs/reports/multi-agent-documentation-workflow-system-architecture-2026-09-26.md` (observation tier).

## 1. Executive summary

The system treats **the repo as the message bus and the filesystem as the only coordination protocol**. There is no inter-agent messaging. Concurrent LLM sessions (dozens observed; 88 heartbeat files, 60 pattern namespaces) avoid corrupting each other via:

1. **Boot contract** (`.agent/00_BOOT.md`, 11 steps) — fixed ordered startup; no session skips setup.
2. **Authority tiers by file location** — truth vs. candidates vs. observations separated by directory.
3. **Immutable provenance** — every generated `.md` names its author forever.
4. **Atomic claim primitive** (`.agent/CLAIMS/<id>.json`) — existence is the lock; one writer per scope.
5. **Objective gate** — compile/harness green (code) or checklist green (docs) before truth.
6. **Append-only decision log** (`docs/DECISIONS.md`) — every promotion recorded with reason.
7. **Close-out report + reusable lesson** — every DONE carries evidence; lessons filed to `.agent/PATTERNS/<agent-id>/`.

Seven primitives; everything else is elaboration. The 2026-09-26 architecture report in `docs/reports/` already specifies this as seven primitives — this manual adds the dated improvement history and the copy-paste kit that report lacks.

## 2. Design goals / non-goals

- **Goals:** parallel LLM workers with zero direct messaging; no agent can promote its own work by placement; crashed/slow/partitioned/idle are distinguishable via positive liveness assertions; failures leave forensic traces (reports + pass counts + decision entries).
- **Non-goals:** human committee review (replaced by gate + log); Git-based authority (Git is explicitly non-authoritative); cross-repo identity (one stable `agent_id` per repo, for life).

## 3. Directory structure (annotated)

```text
<repo>/
  AGENTS.md                        # autonomous governance (this system's constitution)
  guidelines/Guidelines.md         # build + provenance rules (mirrors AGENTS.md §§1-2)
  BABYLON5_CCG_RULEBOOK.md         # canonical domain reference — DO NOT EDIT BODY (game-dev analog: keep one)
  b5ccg/
    src/b5ccg/{engine,model,ai,ui,util}/ + Main.java   # canonical code (63 .java files)
    src-java8-archive/             # frozen original — NEVER EDIT
    resources/cards/*.json + decks/ # data layer (829 cards loaded; 446-title dedup pool; 377 costed)
    compile.bat / compile.sh       # BUILD GATE: javac -source 6 -target 6, stdlib only (JDK 8)
    run.bat / run.sh               # run gate
  canonical/                       # [GUESS: named in AGENTS.md §3 as truth tier; no such dir on disk 2026-09-29 — treat as "root rulebook + src truth" until created]
  docs/
    DECISIONS.md                   # append-only promotion log (7906 lines; newest at bottom)
    proposals/                     # candidates, never truth (25 files)
    reports/                       # observations, no authority (12 files + this manual)
    archive/                       # superseded, retained (named in §3; empty/absent on disk — create on first supersession)
    README.md
  investigations/                  # incoming/external advisory, never canonical (8 entries)
  .agent/
    00_BOOT.md                     # cold-boot sequence, 11 steps — READ FIRST EVERY SESSION
    AGENT_LOOP.md                  # headless loop procedure (procedure, NOT governance; BOOT+AGENTS win)
    TASK_LEDGER.md                 # shared live task table (494 ID rows at census)
    TASK_LEDGER.md.bak             # backup that saved the ledger after a 380→1 truncation (B5-0733)
    CLAIMS/<task-id>.json + README.md   # atomic locks (1 live claim file at writing: B5-0481)
    HEARTBEATS/<agent_id>.json + README.md + _registry.json + _quarantine/
    REPORTS/<date>-<agent>-<task>.md + README.md  # 593 close-out evidence files
    PATTERNS/<agent-id>/*.md + README.md          # advisory self-improvement store (60 namespaces)
    tools/{validate-heartbeats,ledger-query,run-dup-census,dup-census,census-crosscheck,suite-coverage,migrate-heartbeats}.ps1 + verify_task.py
    run-queue.ps1                  # shared OPEN census + offer tool (has -DryRun and opt-in -MaxSeedInvocations)
    HANDOFF.md                     # SUPERSEDED 2026-09-27 (B5-0626) — retained history, DO NOT FOLLOW
    SCOPE_RELEASES/  HEARTBEATS.json files, etc.
```

**Placement law (AGENTS.md §6):** no new `.md` at root. Incoming/external → `investigations/`; proposals → `docs/proposals/`; observations/tests → `.agent/REPORTS/` or `docs/reports/`. Root holds only governance, rulebook, code.

## 4. Hierarchy (who outranks whom)

| Level | Holder | Authority |
|---|---|---|
| 0 | Human (exactly 1) | Project direction, dependency approval (sole gate: external libs), escalation rulings (e.g. PRINT WINS 2026-09-28 replacing withdrawn B5-0654; R1–R6 session identity 2026-09-28) |
| 1 | Governance docs (`AGENTS.md`, `guidelines/Guidelines.md`, `.agent/00_BOOT.md`, `.agent/{CLAIMS,HEARTBEATS}/README.md`) | Bind all agents; conflicts resolved BOOT/AGENTS > AGENT_LOOP > everything else |
| 2 | Live coordination files (ledger row status, claim existence, heartbeat verdicts) | Claims are authority for "who writes now"; ledger status keys offering |
| 3 | Agents (stable per-session `agent_id`, shape `client (model) + discriminator`) | Equal workers; one writer per scope (`engine/`, `model/`, `ai/`, `ui/`); never touch foreign claim/heartbeat |
| 4 | Git | Change-tracking only, never authority |

**Identity law (R1–R6, B5-0785, human ruling 2026-09-28):** an `agent_id` names a running session instance, not a model. Discriminator MUST contain ≥1 letter/digit (else `Get-NormName` strips it and instances collapse). One instance one spelling for life; never retro-rename (e.g. `solar-pro4:free` cited in 114 rows / 154 reports). Windows-forbidden `:`/`/` sanitise to `-` in filenames only; true id stays inside the file; `_registry.json` maps the rest.

## 5. Authority system

- **Tiers by location (AGENTS.md §3):** `canonical/` + root rulebook + `src/` = truth. `docs/proposals/` = candidates (never truth until merged+gate). `docs/reports/` + `.agent/REPORTS/` + `investigations/` + `.agent/PATTERNS/` = advisory/observations (copying/citing never confers authority). `docs/archive/` + superseded docs = history.
- **No authority from** date, filename, length, repetition, or summarisation.
- **Autonomous promotion (§4):** proposal → truth when ALL hold: (1) worked inside claimed scope only, (2) gate green (`compile.bat/sh` at `-source 6` on JDK 8; doc tasks: stated checklist green), (3) change logged in `docs/DECISIONS.md`, (4) claim released. No committee. External-library addition is the ONLY human-gated promotion.
- **Interpretations** of the canonical rulebook go to `docs/DECISIONS.md`, never into the rulebook body.
- **Stopping rule:** if in doubt, log ambiguity in the report and STOP that item only — never block unrelated work.

## 6. Documentation system

- **Provenance (§1, mandatory):** every LLM-created `.md` opens with `author_llm: <name> (<version>)` (frontmatter satisfies). Original author never overwritten. Every later LLM assess/edit/migrate appends `assessor_llm` + updates `last_modified_by_llm`/`last_modified_date`. Unknown authorship → `author_llm: {name:"unknown",version:"unknown"}`; never infer. Never self-list as author+assessor in same pass. Assessors self-add only (adding another agent's name = fabrication).
- **Compaction (§1a, B5-0655, human-approved 2026-09-27):** one entry per agent per file with `passes` + `last_pass` (+ optional `note` for substantive passes). Repeat pass increments; new agent/version appends. Never delete/rename/rewrite beyond those fields. No retro-compaction of pre-2026-09-27 lists.
- **Decision log:** `docs/DECISIONS.md`, append-only, newest at bottom, `date, agent, what, why` per entry. Never rewrite history; forward-supersession pointers required when withdrawing (lesson of B5-0966: withdrawn CANONICAL ruling without in-entry marker).
- **Proposals vs reports:** proposals specify future work (may carry templates); reports record what happened with evidence (logs, counts, exit codes). Both advisory until promoted.
- **Patterns:** every close-out report carries a one-line `Reusable lesson`; author files it as a NEW markdown record under `.agent/PATTERNS/<agent-id>/` with `author_llm` frontmatter; supersede-never-rewrite (correction = new file linking old). Read all namespaces, write only own. Boot skim newest records before claiming (00_BOOT step 10/11).

## 7. Coordination protocol (the executable)

### 7.1 Boot (00_BOOT.md, 11 steps — condensed)

1. Read `AGENTS.md`, `guidelines/Guidelines.md`, `docs/DECISIONS.md`.
2. Read ledger + list `CLAIMS/*.json` + `HEARTBEATS/*.json`. Live claim (TTL 30 min, three-signal) = taken.
3. `javac -version` (expect JDK 8); heartbeat on binding schema; validate store with `validate-heartbeats.ps1` (read-only).
4. OPEN census ONLY via `run-queue.ps1 -DryRun` (bash equiv. if needed); manual `rg` fallback if tool unavailable. Claims-first: suppressed-live-claim rows are NOT defect reports; re-census after release.
5. Pick highest-priority OPEN with no live claim.
6. Claim atomically: create `CLAIMS/<id>.json` (real current UTC `started_utc`, never placeholder/midnight); if exists abort. Re-read row: must still be OPEN or release as orphan (B5-0622).
7. Work ONLY inside claimed scope. Small diffs. Java 6 only; archive frozen; no unapproved libs.
8. Verify: gate green or mark BLOCKED with log excerpt + release.
9. Finish in order: ledger row → DECISIONS entry → `REPORTS/<date>-<agent>-<task>.md` (with `author_llm`) → delete own claim → refresh own heartbeat. Preserve pipes exactly; post-write duplicate-ID census (`run-dup-census.ps1`: 0 clean / 1 duplicate / 2 unreadable); pipe-integrity via `ledger-query.ps1` (own row must read `7`/`no`; B5-0621).
10. Stale reap ONLY on three-signal STALE + evidence recorded in ledger note; UNKNOWN never STALE; never touch live claims/foreign heartbeats.
11. Pattern skim + file lesson (B5-0430).

### 7.2 Ledger

- Table `| ID | Status | Task | Scope | Claim | Verified |`, IDs `B5-####[a-z]?`. Statuses `OPEN/CLAIMED/DONE/BLOCKED` (+ `VOID/SUPERSEDED` for orphans/withdrawals).
- **Pipe law:** single leading pipe, exactly 7 pipes, no `|` inside note cells. `doubleLead yes` parseable but defective (runner tolerated since B5-0613). Never normalise-to-seven blindly — classify excess by offset (B5-0568).
- **Duplicate IDs are not cosmetic:** queue keys by ID; second row silently overwrites first. On collision leave foreign row byte-identical, renumber YOURS to non-adjacent ID (B5-0618 deadlock lesson).
- One writer per task; reap notes recorded in-row.

### 7.3 Claims

- Creating `<task-id>.json` IS the claim. Format: `{task, agent_id, started_utc (real now, ISO-8601 Z), ttl_min:30, scope[], javac}`.
- Absence necessary but NOT sufficient — row must still read OPEN.
- Orphan (claim on DONE/VOID/SUPERSEDED/BLOCKED) → release, do not work (B5-0622).

### 7.4 Heartbeats (binding schema v1, B5-0623 + Amendment A1)

Strict JSON, UTF-8, no frontmatter: `{schema_version:1, agent_id, utc (only canonical timestamp), state:active|idle|busy, live_claims[] (always present; [] = positive assertion of nothing held), javac, current_task?, notes? (prose, NEVER parsed)}`. Unknown keys = informational. Validator exit 0 conform / 1 non-conforming-or-collision / 2 missing-or-empty. One unparseable file once poisoned a whole census — parse failure is a finding, never a crash.

### 7.5 Liveness: three signals, never one (B5-0659 adopted; B5-0597/B5-0609/B5-0660 lesson)

`live(T) <=> max(mtime(CLAIMS/T.json), utc(HEARTBEATS/owner(T).json), mtime(REPORTS/T-*.md)) within TTL`. Verdicts: LIVE (≥1 signal found + within TTL) / STALE (≥1 found, none within) / UNKNOWN (required signal absent/unparseable). Absent → UNKNOWN with reason, never age `-1`/`0` (both compare younger than TTL — failure 3). Missing owner heartbeat = UNKNOWN. Report mtime contributing only (absent mid-task is normal). Reap only when ALL THREE are STALE + evidence in reap note. Conflicting signals → leave alone. A guessing reaper is worse than none.

### 7.6 Reports, tools, loop

- Reports: `.agent/REPORTS/<date>-<sanitised-agent-id>-<task-id>.md`, sanitise only `:`/`/`→`-`.
- Tools (all read-only except runner offer): `run-queue.ps1 -DryRun` (claims-first census), `ledger-query.ps1 -Status "*"` (pipeCount/doubleLead/defectReport + CLAIMS-FIRST footer), `run-dup-census.ps1` wrapper (prefer over inline one-liner; B5-0777), `validate-heartbeats.ps1`, `census-crosscheck.ps1`, `suite-coverage.ps1`, `verify_task.py`.
- `AGENT_LOOP.md` (human-approved 2026-09-27) is the headless driver: BOOT once → LOOP pick/claim/heartbeat/work/gate/finish/reap/re-boot until STOP (no OPEN + no seed, or foreign duplicate not yours to renumber). It does NOT commit — dirty tree at end of run is correct; committing/pushing stays human.

## 8. Gates

| Target | Gate | Green definition |
|---|---|---|
| doc+coding (Java game, this repo) | `b5ccg/compile.bat` (native Windows) / `compile.sh` (+ `RUN_TESTS=1` for suite+smoke) on JDK 8 `-source 6 -target 6`, stdlib only; `HeadlessConformanceTest` + `HeadlessSmokeTest` exit 0; `rg` for Java >6 constructs empty | exit 0, expected bootstrap warning only |
| doc-only game project | Stated checklist in the task row (e.g. sample size, cross-checks, schema conformance) + named report filed; no src gate | checklist all PASS, report on disk |
| docs refresh (playtest guide etc.) | Prior-report reconciliation + hygiene re-sweep noted | deltas adjudicated, no silent overwrite |

Red → mark BLOCKED with log excerpt, release claim, STOP that item only.

## 9. Task lifecycle

`OPEN → (atomic claim + row still OPEN) → CLAIMED → work in scope → gate → DONE (+ ledger + DECISIONS + report + pattern + claim delete + heartbeat)`. Sidelines: `→ BLOCKED` (gate red, evidence logged), `→ VOID` (premise refuted, e.g. B5-0314 LOST_DIPLOMA, B5-0751 already-repaired), `→ SUPERSEDED` (replaced). Self-seeding allowed ONLY as OPEN rows through the normal cycle (AGENTS.md §6, after B5-0319); never mark DONE what was never OPEN+claimed; never touch scopes outside claim even via self-seed.

## 10. Improvements 2026-09-21 → 2026-09-29 (what to copy)

**2026-09-21 — Foundation.** Forked Tiwas governance/provenance+status model, dropped 8-step human promotion for autonomous compile-gate promotion; Java 6-only + frozen archive + single human gate (external libs); rulebook declared canonical-input-only; provenance + file-location tiers; self-seeding + file-placement law after B5-0319; real starter decks (B5-0319), pool dedup (B5-0320), cost plumbing without invented values (B5-0323), harness quota fix so conflicts actually fire (B5-0313).

**2026-09-22 — Defect-fix pattern.** Overflow note below card band (B5-0330a); regex→char-scan for log prefixes (B5-0331a); fabricated assistant overlay removed from UI + binary grep proof (B5-0329a); orphan-claim awareness begins (B5-0317 stale-claim reap; B5-0622 later formalises).

**2026-09-23 — Data grounding.** SNRPG code-decode (B5-0334) → cost backfill with null-kept-null, scan-wins, composite-key match (B5-0335); participation/assistant/station seams land with conformance sections.

**2026-09-25 — Pattern store (B5-0430, human-approved).** `.agent/PATTERNS/<agent-id>/` advisory store; every close-out carries Reusable lesson; boot skim; supersede-never-rewrite.

**2026-09-26 — Ledger integrity + proposals.** Pipe hygiene (B5-0435/0568/0611), dup-census (B5-0618), claims-first + live-repair-aware census (B5-0657), heartbeat failure survey (B5-0597/0609), assessor-compaction + claim-liveness + live-census proposals, read-only `ledger-query` tool (B5-0609), 593-report evidence base, `TASK_LEDGER.md.bak` rescue (B5-0733).

**2026-09-27 — Human-approved hardening batch.** Row-status precondition + dup detection (B5-0622); binding heartbeat schema + validator + Amendment A1 (B5-0623); suffix-crash + silent-degenerate-sort fix (B5-0624); filename conformance (B5-0625); HANDOFF superseded, runner repointed (B5-0626); loop stop clause without impossible commit hash (B5-0627); runner quoting fix (B5-0628); pipe-integrity seeding rule (B5-0621); `started_utc` plausibility refusal (B5-0653); assessor compaction ADOPTED (B5-0655); three-signal liveness ADOPTED, fourth key REJECTED (B5-0659); AGENT_LOOP + run-queue + census-crosscheck shipped; dead third-signal corrected + lookalike heartbeat quarantined (B5-0771/0773); dup-census wrapper exit contract 0/1/2 (B5-0777).

**2026-09-28 — Identity + data honesty.** Session-not-model identity R1–R6 (B5-0785); tool-rule-convergence REJECTED / negative-power-split approved-as-design-only (B5-0787); authored-pool baseline — "no card-text DB in this repo, never was" (B5-0801); PRINT WINS replaces withdrawn B5-0654; opt-in `-MaxSeedInvocations` seed branch (B5-0900, default 0, budgeted, stagnation-excluded); verification-first seeding exposing negative-age-LIVE (B5-0950); run-queue repair: missing/unjoinable owner heartbeat → UNKNOWN never claim-age verdict (B5-0953); CRLF→`.gitattributes` (B5-0988); DECISIONS mojibake to C1 fixpoint with zero clean churn (B5-0989); timestamp-integrity self-report (Buffy 2); Java 6 hard-gate promotion (B5-0833); 56-run gate battery refuting sub-second invariant (B5-0982).

**2026-09-29 — Close-out.** B5-0953 DONE via tool repair (not verdict change); queue-health census v2 (B5-0998).

## 11. Failure classes → fixes (copy the fix, not the failure)

| Failure | Fix to replicate |
|---|---|
| Hand-rolled census misses rows / hides OPEN work | Census ONLY via shared tool; manual `rg` fallback only (B5-0613) |
| Claim on already-closed row (orphan) | Re-read row pre-write; orphans released not worked (B5-0622) |
| Two seeders, same free ID | Post-write dup census; non-adjacent renumber, foreign row byte-identical (B5-0618) |
| Double-pipe / missing-pipe rows | 7-pipe law + `ledger-query` proof; classify-by-offset, never blind normalise (B5-0568/0621) |
| Judging a row under live repair as defective | Claims-first suppression; re-census after release (B5-0657) |
| Placeholder/midnight `started_utc` | Refuse implausible timestamps, do not consume (B5-0653) |
| Narrative-field ID match reported as claims | `live_claims[]` always present; `notes` never parsed (B5-0609) |
| Absent heartbeat → age -1/0 → false LIVE | Absent = UNKNOWN with reason; all-STALE + evidence before reap (B5-0597/0659/0660) |
| Twin sessions, one id, invisible intent | Session-qualified ids R1–R6; one spelling for life (B5-0337/0785) |
| Fabrication in UI/docs | Binary+source grep proof; rulebook mechanic without model = blocked behind model task (B5-0329a) |
| Append-per-pass assessor bloat (35 entries, 7 facts) | Compaction: one entry/agent + passes/last_pass (B5-0655) |
| Withdrawal without marker (CANONICAL confusion) | In-entry supersession pointer; PRINT WINS pattern (B5-0966) |
| Untrackable encoding drift (+4066/-1102 → real +2968/-4) | Positive+negative controls before triage; guarded fixpoint repair (B5-0839/0989) |

## 12. Replication kit (minimal copy set)

Ship these 12 files (rename `<game>` tokens, never ship angle brackets):

1. `AGENTS.md` (adapt §§2–4: build rule, truth paths, promotion gate).
2. `guidelines/Guidelines.md` (provenance + build rule mirror).
3. `.agent/00_BOOT.md` (11 steps; point step 3 at target toolchain).
4. `.agent/AGENT_LOOP.md` (keep "does not commit" + failure table shape).
5. `.agent/TASK_LEDGER.md` (seed 3–5 OPEN rows, disjoint scopes; keep pipe law note).
6. `.agent/CLAIMS/README.md` + one JSON template.
7. `.agent/HEARTBEATS/README.md` + `_registry.json` (`{}` initially) + `validate-heartbeats` equivalent.
8. `.agent/tools/` (at minimum: OPEN census, dup-census with 0/1/2 contract, ledger-query pipe proof).
9. `.agent/run-queue` (DryRun census; claims-first; opt-in seeding default OFF).
10. `docs/DECISIONS.md` (append-only header + first entry adopting the system).
11. `docs/proposals/`, `docs/reports/`, `docs/archive/`, `investigations/` (empty with README tier notes).
12. Canonical domain reference (`<GAME>_RULEBOOK.md`: body immutable, interpretations to DECISIONS).

### 12a. Documentation-only game project

- Drop `src/` build gate; gate = per-task checklist (sample sizes, cross-check counts, schema conformance) recorded in the row's Verified cell.
- Keep claims/heartbeats/reports/patterns unchanged — coordination failures are identical without code.
- Scopes become doc areas (`rules/`, `lore/`, `cards/`, `playtest/`) with one writer per area.

### 12b. Documentation + coding game project

- Keep full kit. Replace Java 6 clause with target toolchain + frozen-archive analog + stdlib/external-lib gate.
- Add headless harness early (smoke → conformance → soak, cf. B5-0201/0308/0596): deck/pool quotas so content actually exercises the pipeline (B5-0312/0313 lesson).
- Data backfills: decode-then-write with null-kept-null, composite-key match, stratified samples + scan spot-checks (B5-0334/0335 pattern).

### 12c. Game-dev tailoring

- One immutable rulebook analog; card/data pool with title-keyed dedup; cost/pool semantics decided before mass value assignment; playtest guide refreshed as parts (not rewrites); balance probes recorded as reports with seeds, not as truth.

## 13. Checklists

**New-repo boot:** governance copied → toolchain pinned → 3 OPEN seeds → validator green → DryRun census lists seeds → one claim→gate→DONE cycle → DECISIONS + report + pattern on disk.
**Close-out:** ledger row flipped (pipes 7/no, dup 0) → DECISIONS appended → report with evidence + Reusable lesson → pattern filed (new file) → own claim deleted → own heartbeat refreshed.
**Reap:** all three signals STALE with evidence quoted in ledger note; else leave alone.

## 14. Guesses and limits `[GUESS]`

- `canonical/` on-disk absence: AGENTS.md §3 names it but no directory was found 2026-09-29 — replicate as a real dir or map truth to rulebook+src explicitly.
- Exact Tiwas-template lineage beyond the AGENTS.md preamble was not re-read for this manual.
- Ledger/report/heartbeat counts are point-in-time (494 rows, 593 reports, 88 heartbeats, 60 pattern namespaces) and will drift.
- B5-0654 withdrawal → PRINT WINS chain reconstructed from DECISIONS headlines; full ruling text lives in the human-decision brief + B5-0821 report.
- Windows PowerShell paths assumed for tools; bash equivalents exist but were not re-verified here.

## 15. Reusable lesson

A replicable multi-agent system is a set of checks that can each fail loudly: census, claim, schema, gate, log, report, lesson — every clause names the failure behind it, or it is decoration.

---
*End of report. Tier: observation — to adopt anything herein, promote it through the target repo's own AGENTS.md + gate + decision log.*
