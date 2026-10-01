
  # Babylon 5 CCG Game Design

---
document:
  title: "Babylon 5 CCG — repository front door"
  status: "Reconciled 2026-09-28 (B5-0925)"
provenance:
  author_llm: {name: "figma[bot]", version: "unknown"}
  assessor_llm:
    - {name: "Cline (space-bunny) b5-0925", version: "space-bunny", passes: 1, last_pass: "2026-09-28", note: "edit: replaced the Figma Make scaffold front matter with a reconciled description of the Java 6 game this repository actually contains; every claim below cites the file it was measured from"}
  last_modified_by_llm: {name: "Cline (space-bunny) b5-0925", version: "space-bunny"}
  last_modified_date: "2026-09-28"
---

# Babylon 5 CCG Game Design

A Java 6 implementation of the Babylon 5 Customizable Card Game: an engine, a
Swing front end, an AI opponent, and a rulebook-conformance test suite.

The original design this implements is a Figma project:
<https://www.figma.com/design/yJbMfNkpMAJARlpFTKgZSw/Babylon-5-CCG-Game-Design>.
The rules reference is `BABYLON5_CCG_RULEBOOK.md`, a copy of the RulesPal
rulebook — **canonical, do not edit the body**; record interpretations in
`docs/DECISIONS.md` (`AGENTS.md` §2).

## Build and run

Requires **JDK 8** (`1.8.0_292` on this machine) — the only toolchain that still
accepts the project's `-source 6 -target 6` flags. Standard library only; no
external libraries (adding one needs explicit human approval, `AGENTS.md` §2).

```
cd b5ccg
compile.bat        # Windows
run.bat
```

```
cd b5ccg
sh compile.sh      # Git Bash / Linux / macOS
sh run.sh
```

* `b5ccg/compile.bat` / `b5ccg/compile.sh` — compiles every `.java` under
  `b5ccg/src/` with `javac -source 6 -target 6 -encoding UTF-8` into `b5ccg/out/`,
  then copies `b5ccg/resources/` alongside it. 63 source files.
* `b5ccg/run.bat` / `b5ccg/run.sh` — `java -cp out b5ccg.Main`, which opens the
  Swing UI (`b5ccg/src/b5ccg/ui/MainWindow.java`). The `.sh` variants run the
  compile first if `out/` is missing.
* One `bootstrap class path not set in conjunction with -source 1.6` warning on
  JDK 8 is expected, not a failure (`docs/playtest-guide.md` §1).

`b5ccg/src-java8-archive/` is the frozen Java 8 original — never edited.

## Card and deck data

`b5ccg/resources/cards/deluxe.json`, `b5ccg/resources/cards/premiere.json` and
`b5ccg/resources/decks/premiere-starter-decks.json`. The build copies them into
`b5ccg/out/`; `DeckLoader.loadBothSets()` reads them at startup and `Main` prints
a card-load line to the console. These files are the authored card pool — see
`docs/reports/authored-card-pool-baseline-2026-09-28.md` for the frozen baseline
they are diffed against.

## Gates

Measured green on this tree at 2026-09-28 (B5-0925):

| Gate | Command | Result |
|---|---|---|
| Build | `b5ccg/compile.bat` | exit 0, 63 files, `-source 6` |
| Conformance | `RUN_TESTS=1 sh compile.sh`, or `java -cp out b5ccg.engine.HeadlessConformanceTest` | `CONFORMANCE SUITE PASSED (643 checks)` |
| Smoke | `java -cp out b5ccg.engine.HeadlessSmokeTest` | `SMOKE TEST PASSED` |

**`RUN_TESTS=1` invokes exactly two classes**, both named literally in
`b5ccg/compile.sh`: `b5ccg.engine.HeadlessConformanceTest` then
`b5ccg.engine.HeadlessSmokeTest`. That is the whole of the wired gate. There is no
`RUN_TESTS` branch in `b5ccg/compile.bat` at all, so on Windows the two classes
must be invoked by hand, as in the table.

Eleven other headless classes exist under `b5ccg/src/b5ccg/engine/` (thirteen in
total, two of them wired) and are **not** wired into `RUN_TESTS` — they are run
directly with `java -cp out`. The
three that say so in their own file headers are `HeadlessAIDifficultyContractTest`
(line 10), `HeadlessParticipationGatesProbe` (line 10) and
`HeadlessWarConflictProbe` (line 10). `docs/playtest-guide.md` §6 is the
authoritative list, with what each one covers.

## Where the rules for working here live

* `AGENTS.md` — provenance, build rules, status model, shared-files protocol.
* `guidelines/Guidelines.md` — provenance and build rules in more detail.
* `BABYLON5_CCG_RULEBOOK.md` — canonical rules reference, frozen body.
* `docs/DECISIONS.md` — append-only log of what changed and why. Every landed
  change has an entry here.
* `docs/playtest-guide.md` — how to run the current build, what every control
  does, and the honest gaps. `docs/human-decision-brief.md` collects the
  questions that need a human ruling.
* `docs/proposals/` — candidate work, never truth until merged and compiled.
  `docs/reports/` — observations, no authority.

## Agent coordination

Work on this repository is done by autonomous agents, and the protocol is
executable rather than advisory:

* `.agent/00_BOOT.md` — the cold-boot sequence every session starts from.
* `.agent/AGENT_LOOP.md` — the operating procedure (a procedure, not governance:
  `AGENTS.md` and `.agent/00_BOOT.md` win on conflict).
* `.agent/TASK_LEDGER.md` — the task queue. `.agent/CLAIMS/` holds one lock file
  per in-flight task, `.agent/HEARTBEATS/` one liveness file per agent, and
  `.agent/REPORTS/` one close-out report per completed task.
* `.agent/PATTERNS/` — a shared, advisory store of one-line reusable lessons
  written at close-out.

Read `.agent/00_BOOT.md` before touching anything here.

## The web scaffold at the repository root

`package.json`, `index.html`, `vite.config.ts`, `src/` and `node_modules/` are a
**Figma Make export**, not part of the game. Measured:

* All of it entered in one commit, `d1d0c6ff` *"Add files from Figma Make"*
  (2026-09-21), which is also the commit that added the Java sources. Only one
  other commit has ever touched those paths, against 46 for `b5ccg/src/`.
* `package.json` is `"name": "@figma/my-make-file"` — the scaffold's own default
  name. The `npm install` / `npm run dev` instructions this file replaces were
  that export's README.
* `src/app/App.tsx` renders an empty `<div>`. The 56 files under `src/` are stock
  shadcn/ui components and contain **zero** references to `b5ccg` and zero
  occurrences of the string `Babylon`; there are no `fetch(`, `axios` or `http://`
  calls, so nothing in it talks to the engine.
* `dist/` at the root is that scaffold's build output (B5-0923).
* `node_modules/` — 67,258 files, 98.7% of the tracked tree — entered through
  human "reply hello" commits (`da58390f`, `786b34a3`, 2026-09-28) rather than
  the Figma import (B5-0921).

**Whether a second web front end is wanted is a question for the human, not an
agent, and it is already escalated as such** (`docs/DECISIONS.md`, B5-0921 and
B5-0923). Nothing in the tree constitutes a plan for one, so this README does not
present it as a second front end. It is recorded here and left untouched, pending
that decision.

