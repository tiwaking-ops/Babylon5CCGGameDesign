---
document:
  title: "Repo state audit — formal project status (2026-10-03)"
  status: "Report (advisory, no authority)"
provenance:
  author_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  assessor_llm: []
  created_date: "2026-10-03"
  last_modified_date: "2026-10-03"
---

# Repo state audit — Babylon 5 CCG Game Design (2026-10-03)

Formal project-state report. Advisory only: per `AGENTS.md` §3,
`docs/reports/` carries observations with no authority. Canonical truth
remains `canonical/` + root rulebook + `b5ccg/src/` code; candidates live in
`docs/proposals/`. No source, card, ledger, claim, or governance file was
edited to produce this report (read-only audit).

Author: **Muse Spark (muse-spark-1.3-contributor-free)**.

## 1. Method

Measured from the live working tree on 2026-10-03 (host UTC+13; UTC date
2026-10-02/03 boundary — all wall-clock reads below are working-tree reads,
not claims):

- `git status --short --branch`, `git log --oneline -10`, `git remote -v`
- `git ls-files` vs on-disk census of `b5ccg/src/**/*.java`
- `javac -version` + `cmd /c "cd b5ccg && compile.bat"` (exit code recorded)
- `py` JSON load of both card pools
- `.agent/tools/ledger-query.ps1 -Status "*"` status histogram +
  `.agent/run-queue.ps1 -DryRun` claimable-OPEN census
- Directory counts: `.agent/CLAIMS`, `.agent/HEARTBEATS`, `.agent/REPORTS`,
  `docs/reports`, `docs/proposals`

## 2. Repo identity

| Item | Value |
|---|---|
| Branch | `main` |
| Remote | `https://github.com/tiwaking-ops/Babylon5CCGGameDesign.git` (fetch + push) |
| HEAD | `b7353e06` — "B5-1928/B5-1929 seeded: repo verification receipt, DECISIONS encoding finding, size-delta pattern" |
| Prior checkpoint | `15ded4ad` — user-ordered working-tree checkpoint 2026-10-01 (all except `docs/DECISIONS.md`, truncation fenced per B5-1435, compile green then) |
| Toolchain | `javac 1.8.0_292` (JDK 8, the only toolchain accepting `-source 6 -target 6`) |

## 3. Working-tree health: DIRTY, large

- `git status --short` count: **1274 paths** (staged + unstaged + untracked).
- `git diff --stat` (tracked delta only): **54 files changed, 7634 insertions, 527 deletions**.
- Largest tracked delta is `docs/DECISIONS.md` (+3198 lines in the stat window);
  the file has a known truncation/re-growth history (B5-1439 incident,
  2026-10-01) and remains append-only by convention.
- Build outputs are present in-tree: `b5ccg/b5ccg/**/*.class` and
  `b5ccg/src/**/*.class` (e.g. `DeckLoader.class`, model `*.class`) show as
  added (`A`) in status — compiled artifacts sitting inside source-adjacent
  paths, not a clean tree.
- Root hygiene: the Figma Make web scaffold (`src/`, `node_modules/`,
  `package.json`, `index.html`, `vite.config.ts`, `dist/`) is still present
  and untouched by this audit; per `README.md` (B5-0925) it is not part of the
  game and a second-web-frontend decision is escalated to the human.

## 4. Build gate: RED (known cause, out-of-scope in-flight work)

- Command: `cmd /c "cd b5ccg && compile.bat"`.
- Result: **exit 1 — compilation failed, 2 errors, 1 expected warning**.
- Errors (both same root cause):
  - `b5ccg/src/b5ccg/ui/MainWindow.java:2922: error: cannot find symbol —
    class DeckBuilderDialog` (declaration site and `new` site).
  - `DeckBuilderDialog` is declared nowhere: zero `git ls-files` paths and no
    on-disk file (matches the B5-2217 read in `docs/DECISIONS.md` tail).
- Expected warning still present (not a failure):
  `bootstrap class path not set in conjunction with -source 1.6`.
- Cause attribution (from ledger/DECISIONS, not re-derived here): fenced OPEN
  **B5-2007** mid-flight under a live claim holds the `ui/` scope that owns
  this hunk; the error site moved from `:1048 showDeckBuilder` (older
  reports) to `:2922`. At least 12 prior BLOCKED rows cite the same cause.
- Consequence: conformance (`HeadlessConformanceTest`) and smoke
  (`HeadlessSmokeTest`) probes could not compile/run in this tree state; no
  probe was attempted beyond the compile itself.

## 5. Source census

| Scope | On-disk `.java` | Tracked (`git ls-files`) | Note |
|---|---|---|---|
| `b5ccg/src` total | **78** | **67** | 11 untracked sources (incl. `B51823DeckCensus.java`, `AIDecisionEngine.java`, `AIMemory.java`, probes) |
| — `engine/` | 28 | — | — |
| — `model/` | 25 | — | — |
| — `model/enums/` | 15 | — | — |
| — `ui/` | 5 | — | breaks the build at `MainWindow.java:2922` |
| — `ai/` | 3 | — | — |
| — `util/` / root | 1 + 1 | — | — |
| `b5ccg/src-java8-archive/` | **32** | 32 + README | frozen, never edited (this audit edited nothing there) |

- Governance: `b5ccg/src/` is Java 6 only (`-source 6 -target 6`, stdlib
  only). Standing census instrument is
  `.agent/tools/census-b50960.py` (AGENTS.md §2a); it was not re-run in this
  pass — the last recorded standing state is zero `code-lines` on every
  construct family under tracked `b5ccg/src`.
- `javac -source 6` remains authoritative on syntax; the census is the
  API-level second look.

## 6. Card and deck data

- `b5ccg/resources/cards/premiere.json`: **446 cards** (JSON list).
- `b5ccg/resources/cards/deluxe.json`: **383 cards** (JSON list).
- Combined authored pool: **829 cards** (446 + 383), consistent with the
  B5-1821 re-census baseline.
- `b5ccg/resources/decks/premiere-starter-decks.json` present (not re-counted
  this pass). `b5ccg/resources/cards/*.json` show as modified (`M`) in the
  working tree vs HEAD — data drift vs the frozen baseline in
  `docs/reports/authored-card-pool-baseline-2026-09-28.md` is not adjudicated
  here.

## 7. Coordination state (autonomous system)

| Store | Count (measured) |
|---|---|
| Task ledger rows matching `*` (`ledger-query.ps1`) | **964**: DONE 801, BLOCKED 121, OPEN 26, VOID 9, SUPERSEDED 7 |
| Claimable OPEN (`run-queue.ps1 -DryRun`) | **21** (26 OPEN minus live/implausible-claim suppression) |
| Implausible-claim warnings (B5-0653 rule) | 3 NOT-OFFERED: B5-1803, B5-1980, B5-2097 (`started_utc` midnight placeholder) |
| `.agent/CLAIMS/*.json` | ~25 live claim files |
| `.agent/HEARTBEATS/*.json` | ~187 heartbeat files |
| `.agent/REPORTS/*.md` | **1097** close-out reports |
| `docs/reports/*.md` | **16** (this file becomes the 17th) |
| `docs/proposals/*.md` | **65** candidates |

Notes:

- Ledger file is 1749 lines; tail carries ordered REAP NOTEs (three-signal
  reaps per `00_BOOT.md` step 10) — the reap-before-delete ordering is
  observed in the sampled notes.
- Queue is healthy in the narrow sense: the DryRun offers 21 claimable rows
  across claiming lanes despite the red build; the build-red BLOCKED
  population (121) is dominated by the B5-1047/B5-2007 gate chains, not by
  queue corruption.
- Heartbeat/claim stores are large (≈187 heartbeats for ≈25 live claims);
  the heartbeat validator is known-red on legacy non-conforming files
  (see `00_BOOT.md` step 3) — not re-measured here.

## 8. Governance and docs

- `AGENTS.md`: autonomous lightweight governance, current (standing Java 6
  census §2a, tool-stamped claim/heartbeat §5).
- `BABYLON5_CCG_RULEBOOK.md`: canonical reference, body frozen; this report
  adds no interpretation.
- `docs/DECISIONS.md`: append-only log, very large (truncation incident
  2026-10-01 recorded under B5-1439; HEAD snapshot preserved at
  `docs/archive/DECISIONS-pre-incident-HEAD-snapshot-2026-10-01.md`).
- `README.md`: reconciled front door (B5-0925), gates table now stale on
  build status (records green 2026-09-28; tree is red today — see §4).
- `docs/playtest-guide.md` + `docs/human-decision-brief.md`: operator and
  escalation surfaces (not re-audited here).
- `investigations/`: advisory incoming material, never canonical.

## 9. Risks and recommended next steps (advisory, not directives)

1. **Land or revert B5-2007's `DeckBuilderDialog` hunk** — every `ui/`-adjacent
   row stays BLOCKED/gate-red until the missing class exists or the
   `MainWindow.java:2922` reference is removed. Single highest-leverage fix.
2. **Clean build outputs from source-adjacent paths** (`b5ccg/**/*.class`
   under `b5ccg/` and `b5ccg/src/`) or confirm they are ignored; they inflate
   `git status` by hundreds of paths and obscure real drift.
3. **Adjudicate card-JSON drift** (`premiere.json`, `deluxe.json` both `M`)
   against the frozen authored-pool baseline before further data edits.
4. **Reap or repair the 3 implausible midnight claims** (B5-1803, B5-1980,
   B5-2097) via the three-signal rule — they suppress otherwise-claimable
   OPEN rows.
5. **Refresh the README gates table** once the build is green again; it
   currently asserts a green that the tree does not satisfy.

## 10. Reproducibility

All numbers above are working-tree reads taken in this session; commands are
listed in §1 so any agent can re-measure. Ledger histogram via
`ledger-query.ps1 -Status "*"`; claimable count via `run-queue.ps1 -DryRun`;
card counts via `py -c` JSON `len()`; compile via `b5ccg/compile.bat` on JDK
`1.8.0_292`. No claim was held and no task row was touched for this report.
