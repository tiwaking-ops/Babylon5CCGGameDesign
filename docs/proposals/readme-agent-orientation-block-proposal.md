---
document:
  title: "A minimal agent-facing block for the root README — three additions to an existing section, not a new section"
  status: "Proposal — confers no authority until adjudicated and merged"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1511
---

# Minimal agent-facing orientation block for the root README

## The premise this proposal was written under is false, and that changes its shape

B5-1511 asks for "a minimal agent-facing orientation block for the root README",
on the DONE B5-1429 census's finding that `README.md` is pre-governance
Figma-Make material, first seen 2026-09-21T03:30 before governance landed, and
therefore **"orients nobody"**.

Measured on the live file at 2026-10-01T05:1xZ: **that finding is stale.** The
README has since been reconciled — its own front matter says *"Reconciled
2026-09-28 (B5-0925)"* and names `Cline (space-bunny) b5-0925` as the assessing
LLM. It is 143 lines and already carries a complete agent-coordination section.

`README.md` lines 102-116, verbatim:

```
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
```

## Point-by-point against the five items B5-1511 names

| Requested pointer | Present? | Evidence |
|---|---|---|
| `.agent/00_BOOT` as the entry procedure | **YES** | L107 pointer + L116 imperative *"Read `.agent/00_BOOT.md` before touching anything here."* |
| `AGENTS.md` as governance | **YES** | L91 pointer; L109 names its precedence over `AGENT_LOOP` |
| The canonical rulebook | **YES** | L26-28 with the frozen-body rule and the DECISIONS interpretations rule |
| The queue runner command | **NO** | `run-queue` returns **0** matches in the whole file |
| The no-new-root-markdown rule | **NO** | the only `root` hits are L118 and L133, both about the Figma web scaffold |

**Three of five already exist.** Adding a new "orientation block" would duplicate
L102-116 and produce two competing entry points on one page — the ambiguity the
proposal is supposed to remove. So the proposal is **three additions inside the
existing section**, not a new section.

## The three additions

Each is written to be pasted at the marked anchor. No anchor is invented: all
three are verified present in the live file.

### 1. The queue runner command — insert after L116

```
Claim work by asking the queue which rows are open. This is the only sanctioned
way to find them; hand-rolled scans of the ledger miss malformed rows.

    powershell -NoProfile -ExecutionPolicy Bypass -File .agent/run-queue.ps1 -DryRun
    bash .agent/run-queue.sh -DryRun

Then claim the highest-priority OPEN row no live claim covers, by creating
`.agent/CLAIMS/<task-id>.json`. If that file already exists the row is taken.
```

*Why here and not elsewhere:* it belongs directly under the boot pointer,
because it is the step between "read the boot file" and "do the work", and L116 is
the end of the coordination list.

### 2. The no-new-root-markdown rule — insert as a new bullet after L114

```
* New markdown belongs in a directory, never at the repository root:
  proposals to `docs/proposals/`, observations to `docs/reports/` or
  `.agent/REPORTS/`, incoming material to `investigations/`. The root holds
  governance, the rulebook, and this file (`AGENTS.md` §6).
```

*Why:* `AGENTS.md` §6 is titled *"Self-seeding and file placement"* and carries
the rule at line 111-124, including the `investigations/` tier and the
"never mark work DONE that was never OPEN+claimed" clause. The README currently
points at `AGENTS.md` without naming the section, so an agent reading only the
README cannot find the placement rule without reading all 131 lines.

### 3. The status model in one line — insert after L100, before the coordination heading

```
Nothing in `docs/proposals/` is true until it is merged and compiled. `canonical/`
plus this rulebook plus `b5ccg/src/` are current truth; `docs/reports/` and
`investigations/` are observations and never confer authority.
```

*Why:* the existing "Where the rules for working here live" list already carries
`docs/proposals/` and `docs/reports/` per-file descriptions (L99-100), so this is
the status **model** those descriptions presuppose but never state. It is the one
sentence that stops a reader treating a proposal as settled.

## Provenance the edit would carry

The README's front matter already satisfies AGENTS §1; an edit needs only the
assessor line, appended per §1a (one entry per agent per file, `passes` counted):

```yaml
assessor_llm:
    - {name: "Cline (space-bunny) b5-0925", version: "space-bunny", passes: 1, last_pass: "2026-09-28", note: "edit: replaced the Figma Make scaffold front matter with a reconciled description of the Java 6 game this repository actually contains; every claim below cites the file it was measured from"}
    - {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free", passes: 1, last_pass: "2026-10-01", note: "edit: added the queue runner command, the AGENTS section 6 no-new-root-markdown rule, and the one-line status model to the Agent coordination section (B5-1511)"}
last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
last_modified_date: "2026-10-01"
```

`author_llm: {name: "figma[bot]", version: "unknown"}` is **not** touched —
AGENTS §1 forbids overwriting original authorship, and the reconciliation author
who follows is not the original creator.

## "A pre-existing file gaining content is an edit, not authorship"

Per B5-1511's explicit instruction, stated plainly: this is an **edit**. The file
has an `author_llm` because `figma[bot]` created it; adding governance-derived
orientation text does not make this session its author, and it must not be
recorded as one. The author line stays; this session enters as an **assessor**.

## What this proposal does not do

- **It does not touch `README.md`.** The row's scope is docs-only plus one
  proposal; the README edit needs its own claimed row.
- It does not re-open B5-1429's stray classification of `QWEN.md`,
  `koda-memory.md` or `loop-prompt.md`, and it does not contest the
  *first-seen* evidence in that census — only its "orients nobody" conclusion,
  which the B5-0925 reconciliation superseded.
- It proposes no gate, no check, and no enforcement, per the B5-1465
  counter-argument that a rule producing only findings trains a fleet to ignore
  findings.

## Reusable lesson

**A census's conclusion has a shelf life even when its evidence does not.**
B5-1429 measured README's git history correctly and drew an orientation
conclusion that a later merge invalidated; acting on the row unexamined would
have produced a second entry point on a page that already had one. When a
downstream row inherits a finding, re-measure the *conclusion* against the
current file — the evidence is what the census guarantees, and conclusions
expire silently. The cheap check is a single grep for the thing the proposal
would add, and it took one command.