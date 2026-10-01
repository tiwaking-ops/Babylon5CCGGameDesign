---
document:
  title: "Compile-verdict-in-heartbeat convention — a prose line, not a schema change"
  status: "Proposal (advisory, no authority until merged)"
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Proposal: the compile-verdict heartbeat line

**Status: proposal, adopted by its author only.** Advisory, same tier as
`investigations/` and the rest of the proposal store. This document confers no
authority. It changes **no** schema, **no** tool, and **no** governance file — not
`AGENTS.md`, not `.agent/00_BOOT.md`, not `.agent/HEARTBEATS/README.md`, and not
`.agent/tools/validate-heartbeats.ps1`. Promotion beyond the author's own practice
requires a human decision and, if ever wanted, a separate row that actually edits
the tooling.

**Origin.** B5-0769, a proposal-and-report-only row citing the tool-rule-convergence
precedent B5-0499. The question it asks is small and real: a heartbeat carries the
`javac` **version** but nothing saying whether the last gate run **passed**, so a
liveness reader cannot tell green from red without re-running the build itself.

---

## 1. The observation, measured at 2026-09-28T05:55Z

Thirty-two heartbeat files (33 including `_registry.json`, which is not a
heartbeat). JDK `1.8.0_292`. Measured, not estimated:

| Reading | Count | Of |
|---|---|---|
| carries a `"javac"` field | **26** | 32 |
| omits `"javac"` entirely | **6** | 32 |
| mentions the build at all in `notes` (compile / Build successful / gate / exit 0) | **17** | 32 |
| carries the literal verdict string `Build successful` | **2** | 32 |
| uses a recognisable `Gates:` / `Gate:` label | **1** | 32 |

The six files with no `javac` field: `cline-01.json`,
`deepseek-harness-b5ccg-01.json`, `kilo (nvidia-nemotron-3-ultra-550b-a55b-free).json`,
`kilo-auto (nvidia-nemotron-3-ultra-550b-a55b-free).json`, `mimocode-0.1.15.json`,
`mimocode-agent-01.json`.

So the honest size of the gap is **not** "nobody reports build state" — 17 of 32
already do, informally. The gap is that the reporting is **unstructured**: many
different phrasings across 17 files, two of which use the exact string
`Build successful`, and only one file labels its gate block at all. A reader
cannot distinguish "built and green" from "built and red" without parsing English
prose, and cannot distinguish "mentioned the build" from "reported a verdict" at
all. That is the B5-0609 failure class in embryo: narrative text mistaken for a
structured field.

**Correction to the row's premise, recorded because the row is the kind that
inherits it.** The row says heartbeats "carry the javac version but nothing saying
whether the last gate run passed". The first half is true at 26/32 and the second
half is true *as a convention* — but "nothing" overstates it. Seventeen files do
record build state in prose, and two of them carry the literal green-verdict
string. The row's conclusion survives; its evidence for the gap is weaker than it
looks, and a proposal built on "nothing" would have been refuted by a reader who
opened two files.

---

## 2. Why the convention must live in prose, not in the schema

`notes` is the correct carrier, and the reason is mechanical rather than
stylistic. From `.agent/HEARTBEATS/README.md`: *"`notes` is prose and is never
parsed."* And from `.agent/tools/validate-heartbeats.ps1` line 61, `notes` is in
`$KnownKey`, and the collector at line 148 only ever reports **unknown** keys as
informational notes. A reader that is already inside the schema can therefore not
be broken by a new sentence in a field the schema explicitly declares opaque.

Adding a real `compile_verdict` field would be the opposite: it would land a new
key in a store that has already produced a **known unresolved** identity problem
(`.agent/HEARTBEATS/README.md` § *Known unresolved*), it would need a validator
change, and it would create a second canonical field that agents could start
disagreeing about. A gate that is *not* a gate — a field nobody validates, with an
unknown number of holders, in a store where 6 files already skip an optional
field — reads as authoritative and is not. Prose that nobody parses can only ever
be a convention, which is exactly the authority level this row is allowed to have.

---

## 3. The convention

Append one line to the `notes` prose of every heartbeat, at the end, in this shape:

```
GATE: compile.bat exit 0 "Build successful" <javac version> at <UTC timestamp>
```

Rules, all of them about not over-claiming:

1. **A verdict, not a plan.** Write the line only after a gate run in this
   session. Never write "GATE: pending" or a predicted result. A forward-looking
   verdict is the failure this convention exists to prevent, wearing its clothes.
2. **Quote the real strings.** `exit N` is the exit code you observed; the quoted
   phrase is copied from the build output, not paraphrased. If the build printed
   something else, quote that instead — `"Build successful"` is the common case,
   not the required one.
3. **Timestamp the reading, not the session.** `<UTC>` is when the gate ran. A
   verdict without a time is exactly as useless as the missing verdict, because
   it cannot be aged out.
4. **Red is reported as red.** `GATE: compile.bat exit 1 ...` is a valid and
   valuable line. Suppressing a red verdict to keep the heartbeat looking healthy
   defeats the purpose and is the one thing this convention must never license.
5. **The line is removable.** It carries no authority, so a future reader may
   drop it, reword it, or ignore it. Do not add tooling that requires it.

**One implementation detail worth recording.** `compile.bat` must be invoked as
`.\compile.bat` from a `cmd` process started *in* `b5ccg/`. Invoked from PowerShell
it still exits 0, but PowerShell promotes the build's benign
`bootstrap class path not set in conjunction with -source 1.6` warning on stderr
into a `NativeCommandError`, and a wrapper that only inspects stderr reads a
**green build as a failure**. A bare `compile.bat` under `cmd /c` from a
different working directory is likewise not found, which is a *different* red.
Both were hit while measuring §1; neither is a build defect, and both would
otherwise be filed as one.

---

## 4. What this proposal deliberately does NOT do

* **No schema field.** §2.
* **No validator change.** `validate-heartbeats.ps1` must keep exiting exactly as
  it does today. Measured before and after this row's close-out: **exit 1** both
  times, for the same pre-existing and foreign reason — the `solar-pro4:free`
  two-file identity collision (`solar-pro4-free.json` and `solar-pro4free.json`).
  That exit code is unchanged by anything here, and no file involved in it was
  touched.
* **No edit to `AGENTS.md`, `00_BOOT.md`, or the heartbeats README.** Those are
  governance; a proposal does not amend governance, per the B5-0593 precedent
  already recorded in `heartbeat-schema-proposal.md`.
* **No tooling edit of any kind**, per the row's scope column.
* **No new task row for spread.** Adoption is observed by later runs. If a future
  agent wants a *tool* that reads this line, that is a different row with a
  different scope, and it would be a schema change in disguise.

---

## 5. Adoption

Adopted by its author: the closing heartbeat for B5-0769 carries the line. That is
the whole of this row's adoption claim, and it is deliberately modest — one
heartbeat, written by the agent that wrote the convention, which is the weakest
form of adoption available and the only one this scope permits. The
`Build successful = 2` count in §1 is expected to rise as other agents
independently find the line useful. If it does not rise, the convention has
failed and the correct response is to let it lapse, not to add a row enforcing it.

## 6. Honest limits

* The evidence is **one store, one moment, one agent's reading of 32 files.** The
  counts in §1 are reproducible with the commands in the report, but they are not
  a survey of intent, and a file that mentions the build in prose is not thereby
  reporting a verdict — the 17 and the 2 are different things and are not in
  conflict.
* The `javac` field being 26/32 is reported here as **context**, not as a defect.
  It is an optional field per the schema and 6 omissions is not a violation; the
  validator exits without complaint on it.
* Whether prose conventions spread at all in a fleet with no enforcement
  mechanism is an **open empirical question**, and this proposal is the experiment,
  not the answer. If a reader eventually wants the verdict machine-checkable, the
  honest route is a schema change with a validator and a migration story, taken
  through a human decision — deliberately out of scope here.
