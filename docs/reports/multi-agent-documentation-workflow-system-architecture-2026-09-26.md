---
document:
  title: "Multi-agent documentation-and-code workflow: system architecture and replication specification"
  status: "Report (observation tier — describes, does not govern, the source system)"
  audience: "An LLM instantiating this workflow in a new project"
provenance:
  author_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (big-pickle)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
source_system: "Babylon5CCGGameDesign — AGENTS.md + .agent/ protocol + B5-#### task ledger"
---

# Multi-agent documentation-and-code workflow — system architecture and replication specification

## 0. What this document is, and what it is not

This document has two jobs.

1. **Describe** the workflow actually running in the source repository: what artifacts
   exist, what each one guarantees, how a task travels from open to done, and how
   dozens of concurrent language models avoid corrupting each other's work.
2. **Specify** that workflow as a portable system. Every artifact is reproduced as a
   fill-in template. A receiving agent should be able to provision a working system
   from this document alone, without access to the source repository and without
   asking a question.

**Authority disclaimer.** This document sits in the observation tier. It has no
authority over the source system. Where it recommends a change to the source system
(for example, §12.4 on shared build output directories), that recommendation is
advisory and takes effect only through the source system's own promotion rules. The
source system's append-only decision log, not this document, remains the record of
what is true there.

**Reading conventions.**

| Convention | Meaning |
|---|---|
| `<ANGLE_BRACKET>` | A token you must substitute. Never ship a literal angle bracket. |
| MUST / MUST NOT | Load-bearing. Violating it corrupts the system, not just its tidiness. |
| SHOULD | Strong convention. Deviating is permitted but must be recorded. |
| MAY | Optional, at your judgment. |
| "Source system" | The repository this document was written from. |
| "Target system" | A new repository you are provisioning from this document. |

---

## 1. System overview

### 1.1 The problem this solves

A single human owns a game-development project. One documentarian language model
maintains the design corpus. A large number of worker language models produce
changes. Three properties make this hard:

- **No inter-agent messaging.** Agents are separate sessions, possibly on separate
  machines, with no channel to each other. Every fact must live in a file.
- **Shared mutable state.** Many agents read and write the same working tree
  concurrently. A single mis-scoped write can silently corrupt another agent's work.
- **Asymmetric authority.** Most agents are cheap, fast, and fallible. A minority of
  changes are load-bearing. Someone must be able to say what is true.

### 1.2 The answer in one paragraph

Treat the repository as the message bus and the filesystem as the only coordination
protocol. Split state by **authority tier** so that no agent can promote its own
work by writing it in the right place. Serialize writes with a **claim** primitive
whose existence is the lock. Require an objective **gate** to pass before any change
becomes true. Make every authorship claim **immutable and attributed** in file front
matter. Log every promotion in an **append-only decision record**. And close every
task with a **report**, so that a claim without evidence is detectable as
malformed.

### 1.3 The seven primitives

Everything else in this document is elaboration of these.

| # | Primitive | Guarantee it provides |
|---|---|---|
| 1 | **Boot contract** | Every session starts from a fixed, ordered, mandatory reading sequence. No agent skips setup. |
| 2 | **Authority tiers** | Truth, candidates, observations, and archives are separated by directory, so location alone determines standing. |
| 3 | **Provenance** | Every generated document names its author, permanently. Attribution cannot be edited away. |
| 4 | **Claim** | At most one agent writes a given scope at a time. Atomic, expiring, reapable. |
| 5 | **Gate** | No change becomes true without an objective, recorded verification. |
| 6 | **Decision log** | Every promotion is recorded with its reason, permanently and in order. |
| 7 | **Report** | Every close-out carries evidence. A close-out without a report is malformed and detectable. |

---

## 2. Roles

### 2.1 Role definitions

| Role | Count | Owns | MUST NOT |
|---|---|---|---|
| **Human** | exactly 1 | Project direction, design truth, escalation, dependency approval | — |
| **Documentarian** | exactly 1 | The canonical document set, adjudication of document conflicts, the documentation gate | Write code in worker scopes |
| **Worker** | many | Claimed tasks end to end: implement, verify, report, close | Touch unclaimed scope; promote its own candidate without the gate |
| **Verifier** | 0 or more | Adversarial assessment of others' candidates. Produces contradiction reports and question lists. Authors nothing it assesses. | Be the author of the material it assesses |

### 2.2 Human

The human is the only authority that cannot be simulated, and the only escalation
point. In the source system the human's gate is deliberately narrow: it exists for
external dependency approval, and nothing else. Everything else proceeds
autonomously.

In a target system the human additionally holds two powers the source system does not
formalize, and you should formalize them:

- **Design ratification.** The human is the final authority on what the game is. The
  documentarian may propose; only the human may declare a design settled.
- **Scope redefinition.** The human may change the project's scope, which
  invalidates queued work by definition.

**Rules.**

- The human MUST be the only role that can approve an external dependency.
- The human MUST NOT be a per-task approval gate. If the human is asked to approve
  routine work, the decomposition is wrong: re-scope the task instead.
- Any ambiguity that cannot be resolved from files MUST be recorded as an open
  question in the report and the affected item MUST stop. One blocked item never
  blocks unrelated work.

### 2.3 Documentarian

The documentarian is the single writer of the canonical document set. This
concentration is the point: it is what makes "one human, one documentarian" a
workable ratio rather than a bottleneck, because the documentarian's work is
serialized by design and the workers' work is not.

**Responsibilities.**

- Maintain the canonical specification: what the game is, what its systems are, what
  the invariants are.
- Adjudicate conflicting candidate documents. When two workers derive incompatible
  interpretations, the documentarian picks one, records the ruling and its
  reasoning, and states which existing artifacts are affected.
- Own the documentation gate (§6.3) and the doc-truth refresh obligation (§12.12).
- Convert accepted candidates into canonical text, atomically, with the candidate
  preserved.
- Publish the **readiness contract** (§7.3) — the list of canonical sections cleared
  for code work.

**Authority.** The documentarian may mark a canonical section settled. The
documentarian may not unilaterally change the game's design intent; where the design
is unsettled, the documentarian escalates to the human or records an explicit
"interpretation pending" marker, which blocks code work on that section.

**Constraint.** The documentarian MUST NOT edit code. The reason is diagnostic, not
hierarchical: if a build breaks, you need to know whether the document or the code
moved. A role that touches both destroys that signal.

### 2.4 Worker

A worker takes a task from open to done without further coordination.

**Per-task obligations, in order.**

1. Complete the boot sequence (§4.1) and record the toolchain version.
2. Select the highest-priority open task whose scope has no live claim.
3. Claim it atomically (§5). If the claim file exists, the task is taken. Pick
   another. Never overwrite, edit, or delete another agent's claim file.
4. Work only inside the claimed scope. Small diffs. No drive-by refactors.
5. Re-read any shared anchor immediately before every write to it (§12.3).
6. Run the project gate. Record the command and its exit status verbatim.
7. On green: update the ledger row, append the decision log, write the report, file
   the reusable lesson, release the claim, refresh the heartbeat.
8. On red: mark the task `BLOCKED` with the failing output excerpt, write a blocker
   report, release the claim. Do not leave a claim live to "try again later".

**Prohibitions.**

- MUST NOT work outside the claimed scope, including to fix another agent's obvious
  bug. Blocking and reporting is the correct response (§12.7).
- MUST NOT mark a task done that was never open and claimed. Every done task has a
  lifecycle on the ledger (§5.2).
- MUST NOT mark its own candidate canonical.
- MUST NOT add another agent's name to a provenance assessor list. Assessors are
  self-added only.
- MUST NOT edit a claim or heartbeat file it does not own.

### 2.5 Verifier

Optional, and valuable specifically in documentation-heavy systems, where a wrong
merge is expensive to detect later. The verifier red-teams candidate drafts against
the canonical invariants and the decision log, and emits a contradiction report plus
a numbered list of questions for the human or documentarian.

The verifier authors nothing it assesses, and MUST NOT appear in the `assessor_llm`
list of a document whose author it is. This is the structural half of the
never-self-assess rule (§3.4).

---

## 3. Authority tiers and provenance

### 3.1 Tiers

Every file in the repository belongs to exactly one tier. Tier is determined by
**location**, and is corroborated by a `status` field in front matter.

| Tier | Location in target system | Status value | What it means | Can it become truth? |
|---|---|---|---|---|
| **Canonical** | `<RULES>/`, `<CODE>/`, `AGENTS.md`, `guidelines/` | `Governance`, `Specification` | Current truth. | Already is. |
| **Candidate** | `docs/proposals/` | `Proposal` | Under consideration. Never truth. | Yes, by merge plus gate. |
| **Observation** | `docs/reports/`, `.agent/REPORTS/`, `.agent/PATTERNS/`, `investigations/` | `Report`, `Pattern`, `Investigation` | Evidence and lessons. No authority. | Never. |
| **Record** | `docs/DECISIONS.md` | `Decision log` | Append-only history of promotions. | n/a |
| **Archive** | `docs/archive/` | `Archive` | Superseded, retained. | No. Never resurrected by reference. |

### 3.2 The authority rule

**Authority derives from location and status, never from recency, filename, length,
repetition, or confidence of phrasing.**

This rule is the load-bearing one. It is what allows a large number of cheap agents
to write freely into the observation tier without any risk of corrupting truth: the
worst outcome of a confused agent writing a very persuasive document is that a
persuasive document sits in a directory that means nothing.

Corollary, and it must be enforced in prose as well as in policy: **copying,
summarizing, or citing a document never confers authority on it.** An agent that
reads an investigation and restates it as a rule has not promoted the
investigation.

### 3.3 Front matter schema

Every generated Markdown file MUST open with front matter carrying provenance.
Minimal required set:

```yaml
---
document:
  title: "Human-readable title"
  status: "Governance | Specification | Proposal | Report | Pattern | Investigation | Archive"
provenance:
  author_llm: {name: "<agent name>", version: "<model version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<agent name>", version: "<model version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---
```

Notes on the shape, taken from the source system:

- The two-level `document` / `provenance` split is a convention, not a requirement.
  What is required is that provenance be machine-parseable at the top of the file
  and that author be distinguishable from assessor.
- `assessor_llm` is a **list, earliest first**, even when empty. It is a list from
  birth so that no structural rewrite is ever needed.
- Flat single-level front matter is acceptable where the toolchain prefers it:

```yaml
---
author_llm: {name: "<agent name>", version: "<model version>"}
assessor_llm: []
last_modified_by_llm: {name: "<agent name>", version: "<model version>"}
created_date: "<YYYY-MM-DD>"
last_modified_date: "<YYYY-MM-DD>"
---
```

  The gate (§6.3) MUST accept either shape. Detect provenance by looking for the
  keys `author_llm`, `assessor_llm`, and `last_modified_by_llm`, not by counting
  delimiters.

### 3.4 Provenance rules

1. **Author is immutable.** The original `author_llm` is never overwritten, never
   replaced, never re-ordered, and never removed — including by the author.
2. **Assessors append.** Any agent that assesses, reviews, edits, migrates, or
   otherwise changes a document MUST append an entry to `assessor_llm`, earliest
   first, and update `last_modified_by_llm` and `last_modified_date`.
3. **Never self-assess in the same pass.** An agent MUST NOT list itself as both
   author and assessor of the same document in the same revision. If the author
   edits their own document, they append a dated assessor entry describing the
   change. This is a bookkeeping requirement, not a hypocrisy detector: it forces
   the edit to be described, and the description is what a later reader needs.
4. **Unknown means unknown.** A file whose authorship cannot be established MUST be
   labelled `{name: "unknown", version: "unknown"}`. Authorship MUST NOT be inferred
   from writing style, file date, filename, or content.
5. **Assessors are self-added only.** An agent MUST NOT add another agent's name to
   an assessor list. Recording an assessment you did not perform is fabrication.
6. **Human edits do not require an LLM field** but MUST NOT remove LLM provenance.
7. **Supersede, never rewrite.** A corrected document is a NEW file that links to
   the one it supersedes. This applies to governance, proposals, patterns, and
   reports alike.

### 3.5 Agent identity

One stable `agent_id` per agent, across all sessions, for the life of the project.
Identity is the namespace for pattern records (§9) and the owner key for claims and
heartbeats (§5, §5.5).

**Rules.**

- `agent_id` MUST be filesystem-safe. Restrict to `[A-Za-z0-9._-]+`. The source
  system has agent namespaces containing spaces, parentheses, and a question mark;
  these create quoting hazards, ambiguous paths, and shell-globbing bugs.
- One agent running two concurrent sessions MUST use two distinct `agent_id` values
  (`<id>-a`, `<id>-b`). Sharing one identity across live sessions is a known
  pathology; see §12.2.
- The same agent MUST NOT drift between identities across sessions. Identity drift
  fragments the pattern store and makes claim ownership unauditable.
- A model upgrade is a new `version` under the same `agent_id`, not a new
  `agent_id`, unless the operator is deliberately running two agents.

---

## 4. Repository layout

### 4.1 Target layout

```
<PROJECT_ROOT>/
  AGENTS.md                      Governance. The constitution. Loaded automatically.
  WORKFLOW.yaml                  System parameters. Single place to substitute tokens.
  <RULES>/                       Canonical design reference. Read-only body.
  <CODE>/                        Canonical source. Languages and build per config.
  <TESTS>/                       Canonical test/probe suites.
  GUIDELINES.md or guidelines/   Build rules, provenance rules, constraints.
  docs/
    README.md                    Tier index and what each directory means.
    DECISIONS.md                 Append-only promotion log.
    proposals/                   Candidates.
    reports/                     Observations.
    archive/                     Superseded material.
  investigations/                Inbound and third-party material. Advisory.
  .agent/
    00_BOOT.md                   The boot contract.
    HANDOFF.md                   Condensed orientation for an incoming agent.
    TASK_LEDGER.md               All tasks, all states. One table.
    CLAIMS/                      Live locks. One JSON file per live claim.
    HEARTBEATS/                  Liveness. One JSON file per active agent.
    REPORTS/                     Close-out evidence.
    PATTERNS/                    Reusable lessons, namespaced by agent_id.
  <BUILD_GATE_COMMAND>           e.g. compile.sh, build.bat, scripts/docs_gate.py
```

**Placement rules (MUST).**

- No new Markdown at the repository root. The root holds governance, canonical
  reference, configuration, and code.
- Inbound or externally sourced material goes to `investigations/`. It is advisory
  and can never be promoted without passing through `docs/proposals/`.
- Agent-authored candidates go to `docs/proposals/`. Observations and test results
  go to `docs/reports/` or `.agent/REPORTS/`.
- Governance lives in `AGENTS.md` and `guidelines/`. Nothing else is governance.

### 4.2 Configuration file

Substitute every project-specific token in one place. Everything else in the system
references these keys rather than hardcoding values.

```yaml
# WORKFLOW.yaml — system parameters
project:
  name: "<PROJECT>"
  id_prefix: "<TASK_PREFIX>"        # e.g. GDD-, B5-, PJ-
  created_date: "<YYYY-MM-DD>"

paths:
  governance: "AGENTS.md"
  guidelines: "guidelines/Guidelines.md"
  rules: "<RULES>/"                 # canonical design reference (read-only body)
  code: "<CODE>/"                   # canonical source
  tests: "<TESTS>/"
  coordination: ".agent/"
  ledger: ".agent/TASK_LEDGER.md"
  claims: ".agent/CLAIMS/"
  heartbeats: ".agent/HEARTBEATS/"
  reports: ".agent/REPORTS/"
  patterns: ".agent/PATTERNS/"
  decisions: "docs/DECISIONS.md"
  proposals: "docs/proposals/"
  observations: "docs/reports/"
  archive: "docs/archive/"
  investigations: "investigations/"

mode:
  # doc_only       — no code. The gate is the documentation gate.
  # doc_first      — documentation is the sole authority; code follows settled docs.
  # hybrid         — read-only canonical reference + append-only decision log for
  #                  interpretations, code and reference evolve independently.
  workflow: "doc_first | doc_only | hybrid"

gate:
  # A project MUST declare exactly one verification command and its kind.
  kind: "build | doc | hybrid | none"
  command: "<the single command that returns 0 on success>"
  timeout_seconds: 900
  # What must be recorded in the close-out report as evidence.
  evidence: ["exit_status", "tool_version", "counts"]
  # Isolation: see §12.4. strongly recommended for concurrent workers.
  scratch_output_dir_per_claim: true

coordination:
  claim_ttl_minutes: 30
  heartbeat_interval_minutes: 5
  max_concurrent_workers: 8
  one_writer_per_scope: true
  scope_units: ["directory", "file", "doc_section"]

scopes:
  # Declared up front. Undeclared paths may not be claimed.
  - path: "<CODE>/engine/"
    owner_role: worker
  - path: "<CODE>/model/"
    owner_role: worker
  - path: "<CODE>/ui/"
    owner_role: worker
  - path: "<RULES>/"
    owner_role: documentarian
  - path: "docs/DECISIONS.md"
    owner_role: documentarian
```

The `scopes` list is the mechanism that makes "one writer per scope" enforceable
rather than aspirational: the gate can compare changed files against live claim
scopes without parsing prose.

### 4.3 Reading order

Fixed and mandatory. Document it once, in the boot contract, and require it.

1. `AGENTS.md`
2. `guidelines/Guidelines.md`
3. `WORKFLOW.yaml`
4. `docs/README.md` and `docs/DECISIONS.md`
5. `.agent/00_BOOT.md`
6. `.agent/TASK_LEDGER.md`, `.agent/CLAIMS/*.json`, `.agent/HEARTBEATS/*.json`
7. Newest records across `.agent/PATTERNS/` namespaces
8. The canonical reference for the specific area in scope, header and relevant
   sections only

---

## 5. Coordination substrate

### 5.1 The boot contract

A boot contract exists so that no agent's first action is an edit. It is short, it
is numbered, and every step is verifiable.

```
0.  Read governance: AGENTS.md, guidelines, WORKFLOW.yaml.
1.  Read the canonical reference index and the append-only decision log.
2.  Read this file, in full, before anything else.
3.  Read the task ledger. List live claims and heartbeats.
    A claim younger than the TTL is live and its task is taken.
4.  Read the newest pattern records across all namespaces.
5.  Record your toolchain version. Record it in your heartbeat and in every report.
6.  Select the highest-priority OPEN task with no live claim.
7.  Before claiming, check the task ID is not already present in the ledger.
8.  Claim atomically: create .agent/CLAIMS/<task-id>.json.
    If the file already exists, ABORT and select another task.
    Never overwrite, edit, or delete another agent's claim.
9.  Work only inside the claimed scope. Small diffs.
10. Verify with the project gate. Green to promote, red to block.
11. Close out: ledger row, decision log entry, report, pattern record,
    release claim, refresh heartbeat.
12. Stale claims past the TTL may be reaped ONLY after logging the reap in the
    ledger. Never touch a live claim or another agent's heartbeat.
```

Steps 7 and 4 are the two most commonly skipped and the two most consequential; see
§12.1 and §9.

### 5.2 Task lifecycle

```
                 seed
                  |
                  v
              [ OPEN ]  <-------- self-seed is allowed, but only into OPEN
                  |
            claim (atomic)
                  v
             [ CLAIMED ] --------- work in progress, claim live
                  |
        +---------+---------+
        |                   |
   gate green          gate red / blocked
        |                   |
        v                   v
     [ DONE ]           [ BLOCKED ] --> report, release claim, pick new task
        |                   |
        |                   +--> (human or documentarian unblocks)
        |                            |
        |                            v
        |                        [ OPEN ]  (reopened with a note)
        v
  claim released,
  report filed,
  lesson filed
```

**Transitions and their required side effects.**

| Transition | Required side effects |
|---|---|
| seed → OPEN | Row exists in the ledger with scope and acceptance criteria. No side effects. |
| OPEN → CLAIMED | Claim file created. Heartbeat updated with the current task. |
| CLAIMED → DONE | Gate green and recorded. Ledger row updated with verification. Decision log entry appended. Report written. Pattern record filed. Claim file deleted. |
| CLAIMED → BLOCKED | Failing output excerpt recorded. Blocker report written. Claim file deleted. |
| BLOCKED → OPEN | Reopen note in the ledger naming who unblocked it and why. |
| any → reaped | Ledger records the reap: which claim, the evidence of staleness, who reaped. |

A `DONE` row without a report path is malformed. The gate can detect this; see
§6.3 check 5.

### 5.3 Task ledger

One file. One table. One row per task. The ledger is the project's shared work
state and its only index of what is open.

```markdown
---
document:
  title: "Task ledger"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# TASK LEDGER

One writer per row. Claim via `.agent/CLAIMS/<task-id>.json` before editing.
Status: OPEN / CLAIMED / DONE / BLOCKED.

| ID | Status | Task | Scope | Claim | Verified |
|---|---|---|---|---|---|
| <PREFIX>-0001 | OPEN | <imperative task statement> | `<path/>` | — | — |
```

**Column semantics.**

| Column | Content |
|---|---|
| `ID` | `<PREFIX>-<zero-padded sequence>`. Never reused, never renumbered. |
| `Status` | One of the four states. This is the authoritative state. |
| `Task` | Imperative, specific, and scoped. Not a topic. |
| `Scope` | The exact paths this task may touch. Must be a subset of `WORKFLOW.yaml` scopes. |
| `Claim` | The `agent_id` holding the live claim, or the owning agent for closed rows. |
| `Verified` | The verification evidence: what was run, what the result was, when. |

**Ledger editing discipline (MUST).**

- One writer per row. A row is owned by the agent holding its claim, or by the agent
  that closed it. Nobody else edits it.
- Preserve the table pipes exactly. Never add, remove, or re-space a `|`. When
  appending a close-out cell, keep the trailing delimiter. When your content
  contains a pipe, escape it or write "pipe" — a miscounted column silently
  destroys the table for every reader.
- Never re-litigate another writer's close-out cell. If you believe it is wrong,
  verify the tree independently, record your finding in your own report, and open a
  new task. Two contradicting close-outs on one row is a known failure; the correct
  resolution is a new row, not an edit to theirs.
- Append-only in practice: corrections go in a new row that references the old ID.

**Priority.** Order the table by priority within status, or add a `Priority` column.
The boot contract says "highest priority with no live claim", so priority must be
readable without interpretation. The source system orders rows by ID and relies on
the ID sequence implying priority; that works only if ID assignment is disciplined.

### 5.4 Claims

A claim is a lock. **The existence of the claim file is the lock.** There is no
lock server and no compare-and-swap primitive; a filesystem create is atomic enough
for this purpose, and it works across machines on shared storage.

```
.agent/CLAIMS/<task-id>.json
```

```json
{
  "task": "<PREFIX>-0001",
  "agent_id": "<agent-id>",
  "started_utc": "<YYYY-MM-DDTHH:MM:SSZ>",
  "ttl_min": 30,
  "scope": ["<path/>", "<path/>"],
  "toolchain": "<toolchain version string>"
}
```

**Rules.**

- Creating the file IS the claim. Check existence first.
- If the file exists, the task is taken. Abort, pick another.
- Never overwrite, edit, or delete another agent's claim file. Not to release it,
  not to "fix" it, not to unblock yourself.
- Delete only your own claim, on close-out.
- Refresh the claim's `started_utc` if you intend to work longer than the TTL, and
  refresh your heartbeat when you do. An agent working for hours with an
  un-refreshed claim will be reaped out from under itself (§12.3).
- A claim MAY be scoped to a single file, a directory, or a named set of document
  sections. Narrower is better.

**Claim hygiene — delete on release.** A claim file left behind after close-out
breaks the existence-is-lock invariant: the task appears permanently taken. The
source system has both empty claim files and claim files rewritten into completion
records, which is exactly this failure. If you want the audit trail, move released
claims to `.agent/CLAIMS_HISTORY/`; do not leave them in `.agent/CLAIMS/`.

### 5.5 Heartbeats

A heartbeat is a liveness signal. It answers "is this agent still there", which is
what makes reaping safe and stall detection possible.

```
.agent/HEARTBEATS/<agent-id>.json
```

```json
{
  "agent_id": "<agent-id>",
  "model": "<model id>",
  "last_seen_utc": "<YYYY-MM-DDTHH:MM:SSZ>",
  "task": "<PREFIX>-0001",
  "toolchain": "<toolchain version>",
  "note": "<optional one-line status>"
}
```

**Rules.**

- Refresh every ~5 minutes while active, and immediately after any long operation.
- Never edit another agent's heartbeat.
- A heartbeat is evidence for a reap decision. Cross-check your clock against a
  heartbeat you did not write before concluding that anything is stale (§12.3).
- Stale heartbeats are not deleted. They are history, and the reaper is accountable
  for reading them.

### 5.6 Reports

One report per finished task, blocked or done.

```
.agent/REPORTS/<YYYY-MM-DD>-<agent-id>-<task-id>.md
```

A close-out report MUST record:

- Task ID, title, and status.
- Scope touched — the exact paths.
- Commands run, verbatim, with the toolchain version and the gate exit status.
- Verification result with counts where the gate produces counts.
- Files changed.
- What was deliberately not done, and why.
- The reusable lesson (§9).
- Open questions for the human or documentarian, if any.

**Rules.**

- Reports are observations. A report never confers authority (§3.2).
- A close-out with no report on disk is unverified by definition. The ledger's
  `Verified` cell MUST name the report path so the two can be cross-checked.
- If your work contradicts someone else's recorded result, keep both records and
  append an adjudication naming the methodological difference. Do not delete
  either.

### 5.7 Decision log

Append-only. One entry per promotion.

```markdown
## <YYYY-MM-DD> — <task-id> — <one-line title>

- **Agent:** <agent_id> (<model version>)
- **Scope:** `<paths>`
- **Gate:** `<command>` → exit <status> (<key counts>)
- **Change:** <what became true that was not true before>
- **Why:** <reason, and the interpretation if this resolves an ambiguity>
- **Report:** `.agent/REPORTS/<path>.md`
- **Supersedes:** <entry or task-id, or none>
```

**Rules.**

- Append at the end. Never edit or delete an existing entry.
- Where a canonical reference document's body must not be edited, record the
  interpretation here instead. This is the standard resolution for "the code and
  the specification disagree": the specification body is authoritative, the
  interpretation is logged, and the code is corrected.
- The log is the audit trail. If a decision cannot be reconstructed from it plus the
  reports, the system has a gap.

---

## 6. The verification gate

### 6.1 Principle

**No change becomes true without an objective, recorded, reproducible check.**

This is the system's main brake. Everything else — tiers, provenance, claims —
organizes work. The gate is what stops plausible output from becoming truth.

A gate is a single command that exits 0 when the tree is acceptable. One command.
Not a checklist a human remembers to run.

### 6.2 Gate kinds

| Kind | Command shape | Applies to |
|---|---|---|
| `build` | compile / test / probe suite, e.g. `./compile.sh && java -cp out <Suite>` | Mixed and code projects |
| `doc` | document validation, e.g. `python3 scripts/docs_gate.py` | Documentation-only projects |
| `hybrid` | doc gate then build gate, both must pass | Mature mixed projects |
| `none` | Not permitted for promotion. Permitted only for exploratory tasks, which MUST be marked `BLOCKED` until a gate exists. | Bootstrap only |

### 6.3 The documentation gate

A documentation-only project needs the same brake as a code project. The checks
below are the direct analogue of a compiler for text, and each corresponds to a
real defect class observed in the source system.

| # | Check | Defect class it catches |
|---|---|---|
| 1 | Every governed Markdown file opens with parseable front matter containing `author_llm`, `assessor_llm`, `last_modified_by_llm`, `created_date`, `last_modified_date` | Unattributed documents |
| 2 | `author_llm` name and version are non-empty and not the literal `TBD` | Fake provenance |
| 3 | If `last_modified_by_llm.name` equals `author_llm.name`, then `assessor_llm` is non-empty | Undeclared self-edit (§3.4 rule 3) |
| 4 | No `U+FFFD`; no `U+00C2`/`U+00C3` immediately followed by a byte in `U+00A0`–`U+00BF` | Encoding corruption of shared files (§12.5) |
| 5 | Every ledger row has the same field count as the header; IDs are unique; `Status` is in the allowed set; every `DONE` row names a report file that exists | Ledger corruption, ID collisions, unverified close-outs |
| 6 | Every claim file is valid JSON with the required keys; `agent_id` matches `[A-Za-z0-9._-]+`; `started_utc` parses; the count of claim files equals the count of `CLAIMED` rows | Orphaned, malformed, or un-released locks (§5.4) |
| 7 | Files changed since the last checkpoint are a subset of the union of live claim scopes | Scope violations (§12.7) |
| 8 | Every relative path mentioned in a governed document that looks like a repo path resolves | Dangling cross-references |
| 9 | Canonical paths have no modification later in the day than the newest decision-log entry naming them | Unlogged canonical edits |
| 10 | No file outside the allowed root directories | Placement violations (§4.1) |

The gate MUST print a per-check pass/fail readout, MUST exit non-zero if any check
fails, and MUST emit its failure count as a stable token so that a report can cite
it without re-deriving it. Emit the number the tool computed; do not re-count, and
do not paraphrase the failure.

**Reference implementation.** Python 3 standard library only, no dependencies. Adapt
paths from `WORKFLOW.yaml`.

```python
#!/usr/bin/env python3
"""Documentation gate. Exit 0 = promotable. Exit 1 = blocked."""
import json, os, re, sys, datetime, pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
LEDGER = ROOT / ".agent" / "TASK_LEDGER.md"
CLAIMS = ROOT / ".agent" / "CLAIMS"
GOVERNED = ["AGENTS.md", "guidelines", "docs", ".agent", "investigations"]
AGENT_ID = re.compile(r"^[A-Za-z0-9._-]+$")
ALLOWED_STATUS = {"OPEN", "CLAIMED", "DONE", "BLOCKED"}
MOJIBAKE = re.compile(r"[\u00C2\u00C3][\u00A0-\u00BF]")

results = []

def check(name, ok, detail=""):
    results.append((name, bool(ok), detail))

def front_matter(text):
    if not text.startswith("---"):
        return None
    end = text.find("\n---", 3)
    return text[3:end] if end != -1 else None

def field(block, key):
    m = re.search(rf"^\s*{key}\s*:\s*(.*)$", block, re.M)
    return m.group(1).strip() if m else None

# 1-4: per-file front matter and encoding
missing, selfedit, badenc = [], [], []
for rel in GOVERNED:
    base = ROOT / rel
    files = [base] if base.is_file() else (base.rglob("*.md") if base.exists() else [])
    for f in files:
        text = f.read_text(encoding="utf-8", errors="replace")
        if "\ufffd" in text or MOJIBAKE.search(text):
            badenc.append(str(f.relative_to(ROOT)))
        fm = front_matter(text)
        if fm is None:
            missing.append(str(f.relative_to(ROOT)))
            continue
        author, assessor = field(fm, "author_llm"), field(fm, "assessor_llm")
        lastmod = field(fm, "last_modified_by_llm")
        if not author or not assessor or not lastmod:
            missing.append(str(f.relative_to(ROOT)))
        elif "unknown" not in author and lastmod and lastmod == author and assessor.strip() in ("[]", ""):
            selfedit.append(str(f.relative_to(ROOT)))
check("front-matter-present", not missing, ", ".join(missing[:5]))
check("no-undeclared-self-edit", not selfedit, ", ".join(selfedit[:5]))
check("encoding-clean", not badenc, ", ".join(badenc[:5]))

# 5: ledger integrity
rows, uniq, claimed, badrows = [], set(), 0, []
if LEDGER.exists():
    for line in LEDGER.read_text(encoding="utf-8", errors="replace").splitlines():
        if re.match(r"^\|\s*" + re.escape("GDD") + r"-?\d", line) or re.match(r"^\|\s*[A-Z]+-\d", line):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            rows.append(cells)
            if len(cells) != 6:
                badrows.append(cells[0] if cells else "?")
            tid = cells[0]
            if tid in uniq:
                badrows.append(tid + "(dup)")
            uniq.add(tid)
            if len(cells) > 1 and cells[1] == "CLAIMED":
                claimed += 1
            if len(cells) > 5 and cells[1] == "DONE" and "REPORTS/" not in cells[5]:
                badrows.append(tid + "(no-report)")
check("ledger-well-formed", not badrows, ", ".join(badrows[:5]))

# 6: claim files
lockfiles, badclaims = 0, []
if CLAIMS.exists():
    for f in sorted(CLAIMS.glob("*.json")):
        lockfiles += 1
        try:
            c = json.loads(f.read_text(encoding="utf-8"))
        except Exception as e:
            badclaims.append(f"{f.name}(unparseable)")
            continue
        for k in ("task", "agent_id", "started_utc", "ttl_min", "scope"):
            if k not in c:
                badclaims.append(f"{f.name}(missing {k})")
        if not AGENT_ID.match(str(c.get("agent_id", ""))):
            badclaims.append(f"{f.name}(unsafe agent_id)")
        try:
            datetime.datetime.strptime(c["started_utc"], "%Y-%m-%dT%H:%M:%SZ")
        except Exception:
            badclaims.append(f"{f.name}(bad started_utc)")
check("claims-valid", not badclaims, ", ".join(badclaims[:5]))
check("claim-census-matches-ledger", lockfiles == claimed, f"files={lockfiles} rows={claimed}")

# 7: scope containment against the last checkpoint
try:
    out = os.popen(f'git -C "{ROOT}" diff --name-only HEAD').read().split()
    live = set()
    if CLAIMS.exists():
        for f in CLAIMS.glob("*.json"):
            try:
                live.update(json.loads(f.read_text(encoding="utf-8")).get("scope", []))
            except Exception:
                pass
    live = {s.rstrip("/") for s in live}
    outside = [p for p in out
               if not any(p == s or p.startswith(s + "/") or s == "*" for s in live)]
    check("scope-containment", not outside, ", ".join(outside[:5]))
except Exception:
    check("scope-containment", True, "skipped: no git")

for name, ok, detail in results:
    print(f"{'PASS' if ok else 'FAIL'}  {name}" + (f"  [{detail}]" if detail and not ok else ""))
fails = sum(1 for _, ok, _ in results if not ok)
print(f"DOC_GATE_RESULT failures={fails}")
sys.exit(1 if fails else 0)
```

Wire it as the project's `gate.command` for `kind: doc`. For a code project, make
the command a wrapper that runs this script first and then the build, and set
`kind: hybrid`.

### 6.4 Recording the gate

Every close-out MUST record: the exact command string, the toolchain version, the
exit status, and the gate's own count tokens. A close-out that says "tests pass"
without the command and the count is not evidence.

If the gate is nondeterministic, do not silently accept a green run. Re-run to a
stated number of attempts and report the distribution. A single green run of a
flaky harness is not verification.

---

## 7. Workflow modes

### 7.1 Mode A — documentation only

No code exists or code is out of scope. The loop is unchanged; the gate changes.

- `mode.workflow: doc_only`, `gate.kind: doc`.
- "Scope" for a task means documents, not directories. Claim document sections by
  heading anchor, e.g. `<RULES>/COMBAT.md#resolution-order`.
- Verification is: front matter valid, internal consistency, cross-references
  resolve, no contradiction with the decision log, terminology census clean.
- The documentarian is the only role that writes canonical text. Workers produce
  proposals, audits, and reports.
- Promotion of a proposal is a merge plus a doc-gate pass plus a decision entry.
- The self-seeding rule still applies: workers may propose new sections, but only
  through the normal claim cycle.

### 7.2 Mode B — documentation first, then code

Documentation is the sole authority for design. Code may only be written against
settled canonical text.

**The ordering rule (MUST).** A code task MUST cite the canonical document section
it implements. A code change that contradicts a canonical section is not an
improvement; it is a `BLOCKED` task plus a documentation-amendment request routed to
the documentarian. This is the single rule that makes "documentation first" mean
something operationally rather than aspirationally.

**The anti-pattern this prevents.** In a documentation-first project without this
rule, a worker reads an ambiguous spec, picks a reasonable reading, implements it,
and the implementation becomes the de facto specification. Within a few dozen tasks
the code and the documentation describe different games, and neither can be changed
without breaking the other. Detect this early: a code commit whose behavior cannot
be traced to a document ID is the fingerprint.

### 7.3 The readiness contract

"Complete enough to begin" needs a test, or it becomes a matter of opinion. A
canonical section is cleared for code work when all five hold:

| # | Condition | Test |
|---|---|---|
| R1 | Stable identity | The section has a permanent ID that code tasks can cite. |
| R2 | Stated invariant | The section says what must be true, not only what the system does. |
| R3 | Acceptance criteria | The section states observable conditions that a test can check. |
| R4 | No open adjudication | No pending ruling, no "interpretation pending" marker, no unresolved contradiction in the decision log. |
| R5 | No unresolved external dependency | Nothing in the section is blocked on a human decision the human has not made. |

The documentarian publishes the cleared set. Workers self-select from it. A section
failing any condition is code-blocked and its gap is a documentation task.

This is the mechanism that keeps a documentation-first project from stalling
indefinitely: it converts "the docs aren't done" from a global blocker into a
per-section list, so code work proceeds on the settled parts while documentation
work continues on the rest.

### 7.4 Mode C — hybrid with a read-only reference

The pattern the source system actually uses, and the right choice when an
authoritative specification already exists and must not be rewritten (a licensed
rules document, a published design bible, a standard).

- The reference body is **read-only**. Agents never edit it.
- Every question of "what does the reference mean here" is resolved by appending an
  interpretation to the decision log, and correcting the code to match.
- A conformance task is defined as: audit the implementation against the reference,
  report deviations with section citations, fix the smallest set.
- The reference is never paraphrased into a canonical document, because a paraphrase
  becomes a second source of truth and the two will diverge.

---

## 8. The document pipeline

How a piece of thinking becomes truth. Stages are separated so that no single agent
can promote its own work by writing it in the right place.

```
intake ──> candidate ──> adjudication ──> merge ──> gate ──> promote ──> lesson
             (proposal)   (documentarian/  (canonical)  (verify)  (decision log)
                            verifier)                            + report
```

| Stage | Owner | Output | Gate to the next stage |
|---|---|---|---|
| **Intake** | any | Material in `investigations/` or a new `docs/proposals/` file with front matter | Has an ID, a scope, an acceptance statement |
| **Candidate** | worker | `docs/proposals/<date>-<author>-<slug>.md` | States the change, the rationale, the affected canonical sections, the open questions |
| **Adjudication** | documentarian, verifier | Verdict recorded in the proposal or the decision log | Contradictions enumerated; ruling stated; interpretation logged if the reference is read-only |
| **Merge** | documentarian | Canonical text updated, candidate retained | Doc gate green; superseded material moved to `docs/archive/`, not deleted |
| **Gate** | gate command | Pass/fail with counts | Exit 0 |
| **Promote** | worker or documentarian | Decision log entry, ledger row closed, report written | All four promotion conditions (§9.1) |
| **Lesson** | worker | One-line reusable lesson plus a pattern record | Filed in the worker's own namespace |

### 8.1 Promotion conditions

A change becomes truth when **all four** hold.

1. **Claimed scope only.** Every modified path is inside the claim's scope.
2. **Gate green.** The project's verification command exits 0, and the exit status
   and counts are recorded.
3. **Logged.** A decision log entry exists naming the change, the reason, the gate
   result, and the report.
4. **Released.** The claim file is deleted and the ledger row is closed.

No human ruling is required for these. The only human gate is external dependency
approval, and in a design sense, settling intent.

### 8.2 Proposal template

```markdown
---
document:
  title: "<proposal title>"
  status: "Proposal"
provenance:
  author_llm: {name: "<agent-id>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<agent-id>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# <Proposal title>

**Proposal ID:** <PREFIX>-P<seq>
**Author:** <agent-id>
**Affects canonical sections:** <section IDs, or "new">

## Change
<What becomes different. One paragraph.>

## Rationale
<Why. Cite the decision log entry, the reference section, or the evidence.>

## Rulebook / reference conformance
<If a read-only reference applies: which sections, and whether this conforms,
extends, or contradicts.>

## Scope of impact
<What else must change if this is accepted.>

## Acceptance criteria
<Observable conditions. These become gate checks.>

## Open questions for the documentarian or human
<Numbered. Each blocks the merge if unanswered.>
```

---

## 9. Knowledge accumulation

### 9.1 The lesson rule

Every close-out report gains exactly one line: a **reusable lesson** — a specific,
testable, transferable statement about how work in this project goes wrong or goes
right. Not a platitude. "Be careful with concurrency" is not a lesson.
"Re-read the anchor row immediately before every write; a concurrent session may
have edited between your read and your write" is.

The report carries the lesson. The pattern record carries the reasoning.

### 9.2 The pattern store

```
.agent/PATTERNS/<agent-id>/<YYYY-MM-DD>-<slug>.md
```

- **Tier:** advisory. Same standing as `investigations/`. Copying or citing a
  pattern never confers authority.
- **Namespace:** your stable `agent_id`. Read all namespaces, write only your own.
- **Provenance:** front matter with `author_llm`, same rules as everything else.
- **Supersede, never rewrite:** a corrected pattern is a new file that links to the
  one it replaces.
- **Boot skim:** the boot contract includes reading the newest records across
  namespaces, so accumulated lessons shape work before a claim is taken.

```markdown
---
document:
  title: "<pattern title>"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "<agent-id>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
---

# Pattern: <short claim>

**When:** <the situation in which this bites>
**Rule:** <the imperative, testable statement>
**Why:** <the mechanism, not the platitude>
**Caveat:** <what this pattern does NOT authorize>
**Related:** <links to sibling patterns>
```

### 9.3 Why the store is advisory

Making lessons advisory is deliberate. A lesson store with authority becomes a
shadow specification that drifts and that agents cite instead of the canonical
text. Advisory records are cheap to write, cheap to be wrong, and still change
behavior — because they are read at boot, by every agent, before every claim.

---

## 10. Human interaction

| Situation | Human action |
|---|---|
| External dependency proposed | Approve or reject. Requires library, license, and a statement of why the standard library cannot suffice. |
| Design intent genuinely unsettled | Rule, or delegate to the documentarian with a deadline. |
| Repeated worker blockage on one item | Intervene. Three blocked attempts on one item is a decomposition failure, not a worker failure. |
| Scope change | Re-scope. Queued work invalidated by definition. |
| Everything else | Nothing. The system is autonomous by design. |

**Escalation discipline.** An agent that cannot resolve an ambiguity from files MUST
record the ambiguity in its report, mark the affected item `BLOCKED`, and continue
with unrelated work. It MUST NOT guess and proceed on a load-bearing ambiguity, and
it MUST NOT let one ambiguity stop unrelated items. Escalate narrowly, with a
one-paragraph statement of the question and the options you considered.

---

## 11. Scaling model

### 11.1 Where the system contends

| Resource | Contention | Mitigation |
|---|---|---|
| Ledger rows | Low per row, since one writer per row | Never edit another row; verify by re-read before write |
| Ledger file | Concurrent readers during a large append | Append-only discipline; readers tolerate a partial tail |
| Decision log tail | **High** — every promotion appends to the same file | One promotion at a time; re-read the tail immediately before appending; verify your entry landed |
| Claims directory | Low; file creation is atomic | Existence check before claim |
| Heartbeats directory | None; each agent owns its file | Never write another agent's heartbeat |
| **Gate** | **Highest** — every worker runs the same command | See §12.4. This is the system's real scaling limit. |
| Working tree | **High** — all workers share one checkout | Claims scope, plus per-claim scratch space |
| Canonical documents | Serialized by design | This is the documentarian's job; do not parallelize it |

### 11.2 Practical concurrency

The system is designed for a handful of concurrent workers — order 5 to 10 against
a single working tree. Beyond that, three things degrade in order:

1. **Gate throughput.** Every verification is serialized on shared build state.
   Workers spend more time queueing for the gate than working.
2. **Ledger and log contention.** Append conflicts and lost updates become common.
3. **Human attention.** The escalation queue becomes the bottleneck, because the
   design questions only the human can answer are the true scarce resource.

Past roughly 20 concurrent workers, the correct move is more trees, not more
workers: give each worker its own clone, and have the claim protocol serialize
merges. That is a larger change than this document's templates cover, and it should
be an explicit decision rather than an emergent crisis.

### 11.3 Self-seeding

Workers may create new tasks, but only as `OPEN` rows claimed through the normal
cycle. A worker MUST NOT mark work done that was never open and claimed, and MUST
NOT touch a shared code scope outside a claim — including work it seeded itself.
The rationale is specific: an agent that both invents a task and declares it
complete has removed every check that the system depends on.

Before seeding, check the ID is not already in the ledger. Two agents seeding the
same ID produces a duplicate row that is invisible until someone counts IDs.

---

## 12. Failure modes

Each entry: the mechanism, why the system's defenses are insufficient on their own,
and the specific control that catches it. These are drawn from the source system's
observed record.

### 12.1 Task ID collision

**Mechanism.** A worker self-seeds `<PREFIX>-0449` while the overseer seeds the same
ID for unrelated work. Two rows, one ID.

**Why defenses miss it.** The claim protocol is per-ID, so both agents can believe
they hold `<PREFIX>-0449`. The gate's column check passes. The corruption surfaces
later, as an audit that closes the wrong row.

**Control.** Before claiming, count duplicate IDs in the ledger:

```bash
awk -F'|' '/^\| <PREFIX>-/ {print $2}' .agent/TASK_LEDGER.md | sort | uniq -d
```

Non-empty output: stop, and reconcile before doing anything else. Better: allocate
IDs from a single monotonic sequence with a documented reservation step.

### 12.2 Two live sessions, one identity

**Mechanism.** The same agent runs two concurrent sessions under one `agent_id`.
One session finishes and releases the claim; the other is still working, now
holding no lock, and its tree edits are unguarded. Or one session writes a close-out
into a row the other is filling.

**Why defenses miss it.** Every per-agent rule in the system assumes one live
session per identity. The claim protocol is correct; the premise is false.

**Control.** Distinct `agent_id` per session (`-a`, `-b`). Re-read the exact anchor
immediately before every write to a shared file. If a row closes under someone
else's cell while your edits are live, verify the end state independently, record
the split authorship in your own report, and leave their cell alone. If your claim
file was emptied by someone else, treat that as a release signal: claim nothing
further under that ID and move on.

### 12.3 Clock skew and wrongful reaping

**Mechanism.** An agent's clock is shifted, or its notion of "now" is wrong. It
computes a live 12-minute-old claim as 12 hours stale, reaps it, and closes the
underlying row from its own re-run — with a different method than the row specified
and no report on disk. The original worker's verified result is already filed. The
result is two contradicting close-outs on one row and a phantom regression.

**Why defenses miss it.** The TTL rule is a comparison against a clock the system
does not own.

**Control.** Before reaping anything, cross-check the wall clock against at least
one timestamp you did not write: another agent's heartbeat, a report modification
time, the newest decision entry. If your own stamps are future-dated relative to
those, your staleness computation is wrong — do not reap. Reaping is a *reported*
action: file a report naming the claim, the evidence of staleness, and your
replacement work. A close-out cell with no on-disk report is unverified by
definition. When two datasets conflict on one row, keep both, append an
adjudication naming the methodological difference, and forbid follow-up work seeded
from the unconfirmed dataset until it is reproduced.

### 12.4 Shared build output

**Mechanism.** Every worker runs the same build command writing to the same output
directory. One worker compiles while another's half-written tree is on disk. The
gate then reports failures caused by another agent, or passes for the wrong reason.

**Why defenses miss it.** The gate is defined as a single command, and the obvious
implementation writes to shared state.

**Control.** Give each claim its own output directory and have the gate target it:

```
<scratch>/.agent/build/<task-id>/out
```

If your build tool cannot redirect output, then either run the gate only when no
other claim is live, or gate against a staged snapshot. **This is the most
underrated scaling defect in the source system** and the one most likely to produce
phantom regressions attributed to innocent agents.

### 12.5 Encoding corruption of shared files

**Mechanism.** A tool reads a shared UTF-8 file using a legacy code page, or writes
one through a shell redirection that defaults to a legacy encoding. The mangling is
written back and is now permanent in a governance file.

**Observed instance.** The source system's task ledger contains the byte sequence
`U+00C3 U+0192 U+00C2 U+00A2` where an arrow character belonged — the signature of a
UTF-8-to-CP1252-to-UTF-8 round trip. Several rows are affected. The file is valid
UTF-8 and contains no replacement characters, so casual inspection passes; the
damage is only visible as mojibake.

**Why defenses miss it.** Nothing in the system reads these files as text for
validation. A ledger is a table, not prose, and no check inspects cell contents.

**Control.** Mandate UTF-8 for all reads and writes of shared files. Forbid shell
redirection into shared files — the usual default writes UTF-16 or a console code
page. Use an editor or file-edit tool that declares its encoding. Add check 4 to
the doc gate: reject `U+FFFD`, and reject `U+00C2`/`U+00C3` followed by a character
in `U+00A0`–`U+00BF`, which is the double-encoding signature. Note that a console
may render correct text as mojibake, so **validate bytes, not terminal output**.

### 12.6 Table and delimiter corruption

**Mechanism.** An agent appends a close-out cell and drops the trailing delimiter, or
a task description contains an unescaped pipe. The table's column count diverges,
and every downstream reader — including the doc gate's field-count check — now
disagrees about which cell is which.

**Control.** Verify before you write: count the fields in the anchor row, keep the
trailing delimiter when appending, escape or avoid pipes in content, and re-read the
row after writing to confirm the count. A pipe-count check belongs in the doc gate.

### 12.7 Scope violation by a well-meaning fixer

**Mechanism.** A worker hits a compile error in a file another agent is actively
editing. The fix is obvious and small. The worker applies it, and now two agents
have written the same file.

**Observed instance.** A duplicate field declaration appeared in a file under another
agent's live claim — the fingerprint of a concurrent partial write, not a logic
error. The correct response was not a fix.

**Control.** A compile error in a file you do not hold a claim on is a **block**, not
a task. Mark `BLOCKED` with the exact error excerpt and the concurrent claim
information, release your claim, and file a blocker report. Then either wait for the
owning agent or, if their claim is stale with no heartbeat and no report, reap per
§12.3 and then fix. Touching a live writer's file is the exact violation the claim
contract exists to prevent, and "the fix was obvious" is not an exception to it.

### 12.8 Provenance fabrication and identity drift

**Mechanism.** An agent records an assessment it did not perform, or infers an
author from writing style, or continues under a slightly different identity string
than last session.

**Observed instance.** The source system's pattern store contains three namespaces
for what appears to be one or two agents — `Buffy (glm-5.3-flash)`,
`Buffy (unknown)`, and `Buffy-(glm-5.3-flash)` — plus a namespace containing a
question mark, which is illegal in most tooling and hostile to shell quoting. Agent
identity has also been recorded at version granularity (`solar-pro4:free` versus
`solar-pro4?free`) in different places.

**Control.** The gate's `agent_id` charset check. One declared `agent_id` per agent
in `WORKFLOW.yaml`, and all provenance fields derive from it rather than being
retyped. Self-added assessors only. `unknown` rather than inference.

### 12.9 Unverified close-out

**Mechanism.** A row is closed to `DONE` from a re-run or an inference, with no
report on disk, or with a verification cell that describes a method other than the
one the row specified.

**Control.** Every `DONE` row names its report file; the doc gate checks the file
exists. A close-out without a report is malformed by definition, not merely
weak.

### 12.10 Nondeterministic verification

**Mechanism.** A probe with wall-clock scheduling or unseeded randomness produces
different results on identical invocations. One failure is diagnosed as a
regression, and a phantom fix task is seeded.

**Control.** Before diagnosing a failure, re-run. A single failing run of a
nondeterministic harness is noise. Aggregate across runs, report the distribution,
and separate "the harness is flaky" from "the tree regressed". Capture per-run data
for regression comparison so a future run has something to compare against.

### 12.11 Stale documentation after a conformance fix

**Mechanism.** Code is corrected to match the reference. The documentation that
described the old, incorrect behavior is not updated, and now actively misleads.

**Control.** Treat a behavior-changing code task as carrying a documentation
refresh obligation for every document that described the old behavior. The
documentarian re-derives truth from the tree after a conformance sweep, and a task
is not closed while a document it invalidated is still stale.

### 12.12 Documentation debt

**Mechanism.** Workers find gaps faster than the documentarian can close them. Code
work starves, or proceeds on stale assumptions, or the readiness contract becomes
permissive enough to be meaningless.

**Control.** Make the gap visible and costed. Workers encountering a documentation
gap file it as a proposal with a specific section ID rather than working around it.
Track the count of sections failing each readiness condition; a rising count is a
human-visible signal to shift capacity toward the documentarian. The alternative —
loosening the readiness contract to unblock code — trades a visible queue for an
invisible divergence between documentation and implementation.

### 12.13 Scope creep in the name of helpfulness

**Mechanism.** An agent notices unrelated problems and fixes them in passing,
because the fix is small and obviously correct.

**Control.** One task, one scope, one report. Unrelated findings become new ledger
rows. This costs a little coordination and buys a clean audit trail; the reverse
trade is how a shared tree becomes unowned.

---

## 13. Annotated mapping

Generic element → the source system's concrete instance. The right-hand column is
an example, not a requirement.

| Generic element | Source system instance | Notes |
|---|---|---|
| `WORKFLOW.yaml` parameters | Scattered across `AGENTS.md`, `guidelines/Guidelines.md`, `00_BOOT.md`, `HANDOFF.md` | The main structural difference. The source system requires a reader to assemble parameters from four files; a target system should declare them once |
| Governance file | `AGENTS.md`, 6 sections, 2 assessors | Compact; the whole constitution fits in one file |
| Build rules | `guidelines/Guidelines.md` | Provenance and build rules in one file |
| Boot contract | `.agent/00_BOOT.md`, 10 steps | Step 10 (pattern skim) was added later; the sequence accretes. Write step 4 in from the start |
| Orientation for new agents | `.agent/HANDOFF.md` | A condensed restatement of governance. Useful, and a drift risk: it must defer to `AGENTS.md` explicitly |
| Task ledger | `.agent/TASK_LEDGER.md`, table, ~490 rows, IDs `B5-0001`… | Column format is the load-bearing part |
| Live claims | `.agent/CLAIMS/<id>.json`, TTL 30 min | See §12.4 and the claim-hygiene note in §5.4 |
| Heartbeats | `.agent/HEARTBEATS/<agent-id>.json`, ~5 min | Timestamp field is variously `utc` and `last_seen_utc`; fix the schema |
| Reports | `.agent/REPORTS/<date>-<agent-id>-<task-id>.md`, 200+ files | Filename convention is good; the volume is a signal that tasks were small |
| Pattern store | `.agent/PATTERNS/<agent-id>/`, ~60 records | Advisory, namespaced. The idea works; the identity hygiene does not |
| Decision log | `docs/DECISIONS.md` | Append-only. The provenance front matter has accumulated 30+ assessor entries, which is itself a scalability limit |
| Candidate tier | `docs/proposals/` | |
| Observation tier | `docs/reports/`, `.agent/REPORTS/`, `investigations/` | |
| Archive tier | `docs/archive/` | |
| Canonical reference (read-only) | `BABYLON5_CCG_RULEBOOK.md` | Body never edited; interpretations go to the decision log |
| Canonical code | `b5ccg/src/`, Java 6, stdlib only | |
| Frozen predecessor | `b5ccg/src-java8-archive/` | A migration archive. Never edited |
| Toolchain constraint | JDK 8, because it is the last release accepting `-source 6` | A general lesson: pin the gate toolchain and record its version in every heartbeat and report |
| Verification command | `b5ccg/compile.bat` / `compile.sh`, plus a headless probe suite and a conformance suite | Gate plus named probes, each with a source task row. A strong pattern: probes are individually addressable |
| Language gate | Grep for `->`, `::`, `stream()`, `computeIfAbsent`, `@FunctionalInterface`, `try (` in scope, must be empty | A cheap, high-yield static check. The doc-gate analogue is §6.3 |
| Human gate | External library approval only | Deliberately narrow. Keep it narrow |
| Task ID scheme | `B5-NNNN`, zero-padded, never reused | |
| Scope units | `b5ccg/src/b5ccg/engine/` and siblings | One writer per scope directory |
| Self-seeding | Allowed into `OPEN` only | |
| Placement rule | No new Markdown at repo root | |

---

## 14. Bootstrap checklist

Ordered. Do not skip forward — each step depends on the previous one being true.

**Day zero, before any agent runs.**

1. Create the directory skeleton (§4.1).
2. Write `WORKFLOW.yaml` with every token substituted. No angle brackets remain.
3. Write `AGENTS.md` from the template in §15.1. Include the authority rule, the
   four promotion conditions, the scope rule, and the placement rule.
4. Write `guidelines/Guidelines.md` from §15.2: provenance rules, toolchain rules,
   dependency gate.
5. Create `.agent/TASK_LEDGER.md` with the header row and no task rows.
6. Create `.agent/00_BOOT.md` from §15.3, with steps 4 and 7 (pattern skim, ID
   uniqueness) present from the start.
7. Create the empty directories `CLAIMS/`, `HEARTBEATS/`, `REPORTS/`, `PATTERNS/`
   with their READMEs, which are themselves provenance-bearing documents.
8. Create `docs/README.md`, an empty `docs/DECISIONS.md` with front matter, and the
   `proposals/`, `reports/`, `archive/` directories.
9. Write the gate. For a documentation-only project, adapt the script in §6.3 and
   confirm it exits 1 on a deliberately malformed fixture and 0 on a clean tree. A
   gate that has never been observed failing is not a gate.
10. Declare the first scopes in `WORKFLOW.yaml`. Fewer scopes, more serial. Start
    serial and widen only after you have seen the scope rule hold.
11. Seed the first five to ten tasks, each with a scope, an acceptance criterion,
    and a doc-section citation where one exists.
12. Run the gate yourself. It must be green before any worker starts.

**Day one, first worker session.**

13. Execute the boot contract verbatim, in order, with no edits.
14. Claim exactly one task. Do not seed on the first run.
15. Close out with a full report and one reusable lesson.
16. Confirm the pattern store now has one record and the decision log has one entry.
    The loop is closed when these exist.

**Week one, before scaling past a handful of workers.**

17. Audit: every `DONE` row names an existing report.
18. Audit: no duplicate IDs; claim-file count equals `CLAIMED`-row count.
19. Audit: every governed document has parseable front matter with an author.
20. Audit: no encoding corruption in any shared file.
21. Confirm the gate has been observed both green and red.
22. Then, and only then, widen `max_concurrent_workers`.

---

## 15. Templates

Substitute every `<ANGLE_BRACKET>`. Remove sections that genuinely do not apply to
your project rather than leaving placeholders in place.

### 15.1 `AGENTS.md`

```markdown
---
document:
  title: "AGENTS.md — project governance"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# AGENTS.md — <project name> governance

This file is the constitution. It wins over any other document on conflict,
including a condensed handoff file and any agent's summary of it.

## 1. Provenance (mandatory)

Every generated `.md` file MUST open with front matter carrying:
`author_llm: <name> (<version>)`.

- The author line records the original creator and is never overwritten or removed.
- A file whose authorship cannot be established SHALL be labelled
  `author_llm: {name: "unknown", version: "unknown"}`. Authorship MUST NOT be
  inferred from style, date, filename, or content.
- Any agent that assesses, reviews, edits, or migrates a document MUST append an
  `assessor_llm` entry, earliest first, and update `last_modified_by_llm` and
  `last_modified_date`. Assessors are self-added only; recording an assessment you
  did not perform is fabrication.
- Never list yourself as both author and assessor in the same revision. If the
  author edits their own document, append a dated assessor entry describing the
  change.
- Human edits do not require an LLM field but MUST NOT remove LLM provenance.
- Corrections are new documents that link to the document they supersede.

## 2. Authority tiers

| Tier | Location | Standing |
|---|---|---|
| Canonical | `<RULES>/`, `<CODE>/`, this file, `guidelines/` | Current truth |
| Candidate | `docs/proposals/` | Never truth until merged and gated |
| Observation | `docs/reports/`, `.agent/REPORTS/`, `.agent/PATTERNS/`, `investigations/` | Evidence only |
| Record | `docs/DECISIONS.md` | Append-only promotion history |
| Archive | `docs/archive/` | Superseded, retained for history |

**Authority derives from location and status, never from date, filename, length,
repetition, or phrasing.** Copying or summarising a document never confers
authority on it.

## 3. Roles

- **Human** — one. Project direction, design truth, external dependency approval,
  escalation. Not a per-task approval gate.
- **Documentarian** — one. Sole writer of canonical text. Adjudicates document
  conflicts. Owns the documentation gate. MUST NOT edit code.
- **Worker** — many. Claim, work, verify, report, close. MUST NOT touch unclaimed
  scope.
- **Verifier** — optional. Assesses others' candidates; authors nothing it assesses.

## 4. Workflow mode

`doc_only` | `doc_first` | `hybrid` — declared in `WORKFLOW.yaml`.

In `doc_first`, code MUST cite the canonical section it implements, and code that
contradicts canonical text is a `BLOCKED` task plus an amendment request, never a
silent correction. In `hybrid`, the reference body is read-only and every
interpretation is appended to `docs/DECISIONS.md`.

## 5. Build and toolchain rules

- Canonical source builds with: `<gate command>`.
- Toolchain: `<pinned version>`. Record its version in your heartbeat and every
  report.
- Language/feature restrictions: `<list>`, with the static check to run:
  `<grep or lint command>`.
- `<frozen paths>` are frozen. Never edit.
- No external dependency without explicit human approval. Propose the dependency,
  its license, and why the standard library cannot suffice. Do not vendor it until
  approved. This is the only human gate.
- Shared text files are UTF-8. Never write a shared file through shell
  redirection.

## 6. Coordination protocol

- Boot at `.agent/00_BOOT.md`, in order, every session, before any edit.
- Tasks: `.agent/TASK_LEDGER.md`. One writer per row. Preserve table delimiters
  exactly.
- Claim before edit: create `.agent/CLAIMS/<task-id>.json`. Its existence is the
  lock. If it exists, the task is taken — pick another. Never overwrite, edit, or
  delete another agent's claim. Delete only your own, on close-out.
- TTL `<N>` minutes. Refresh the claim and your heartbeat if you work longer.
- Stale claims may be reaped only after cross-checking the clock against a
  timestamp you did not write, and only with a reap note in the ledger.
- Heartbeat: `.agent/HEARTBEATS/<agent-id>.json`, refreshed every ~5 minutes while
  active. Never edit another agent's heartbeat.
- One stable `agent_id` per agent, filesystem-safe: `[A-Za-z0-9._-]+`. A second
  concurrent session gets a distinct id.
- One writer per scope. Work only inside your claim.
- Git is change tracking, not authority.

## 7. Promotion

A change becomes truth when all four hold:

1. Every modified path is inside the claimed scope.
2. `<gate command>` exits 0, and the command, toolchain version, and counts are
   recorded.
3. A `docs/DECISIONS.md` entry records the change, the reason, the gate result, and
   the report path.
4. The claim is released and the ledger row is closed.

No human ruling is needed. If an ambiguity cannot be resolved from files, record
it, mark the item `BLOCKED`, and continue with unrelated work.

## 8. Self-seeding and placement

- Agents may seed tasks only as `OPEN` rows claimed through the normal cycle.
  Never mark work done that was never open and claimed.
- No new `.md` files at the repository root. Root holds governance, the canonical
  reference, configuration, and code.
- Inbound or external material goes to `investigations/` (advisory, never
  canonical). Candidates go to `docs/proposals/`. Observations go to
  `docs/reports/` or `.agent/REPORTS/`.
- The pattern store is advisory, same tier as `investigations/`. Read all
  namespaces, write only your own. Supersede, never rewrite.

## 9. Close-out

Every finished task: update the ledger row, append the decision log, write
`.agent/REPORTS/<date>-<agent-id>-<task-id>.md` with full provenance, file one
reusable lesson as a pattern record, delete the claim, refresh the heartbeat.
A close-out with no report on disk is malformed.
```

### 15.2 `guidelines/Guidelines.md`

```markdown
---
document:
  title: "Project guidelines"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# Project guidelines

## Provenance

Formal rule, restated from `AGENTS.md` §1 because it applies to every artifact
this project produces: every LLM-generated `.md` carries `author_llm`; assessors
append; self-assessment in the same pass is forbidden; unknown authorship is
labelled `unknown` and never inferred.

## Build

- Canonical source builds with `<gate command>`, toolchain `<version>`.
- Feature restrictions: `<list>`. Verify with `<static check command>`; the result
  must be empty before you release a claim.
- Frozen paths: `<paths>`. Never edit.
- No external dependency without explicit human approval.

## Text handling

- All shared text files are UTF-8, LF line endings.
- Never write a shared file through shell redirection. Use a file-edit tool that
  declares UTF-8.
- Tables in shared files: preserve delimiters exactly; escape pipes in content.
- A near-miss finding you did not act on is a new task row, not a silent fix.

## Verification evidence

A close-out records the exact command, the toolchain version, the exit status, and
the gate's count tokens. "Tests pass" without the command and the counts is not
evidence. If a gate is nondeterministic, re-run to a stated number of attempts and
report the distribution.

## Add project-specific rules below

<project rules>
```

### 15.3 `.agent/00_BOOT.md`

```markdown
---
document:
  title: "Cold-boot sequence"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# 00_BOOT — read this first, every session

1. Read `AGENTS.md`, `guidelines/Guidelines.md`, `WORKFLOW.yaml`.
2. Read the canonical reference index and `docs/DECISIONS.md`.
3. Read the task ledger. List `.agent/CLAIMS/*.json` and `.agent/HEARTBEATS/*.json`.
   A claim younger than the TTL is live and its task is taken.
4. Read the newest records across every `.agent/PATTERNS/` namespace. Prior lessons
   shape your work before you claim.
5. Run the toolchain version command and record it. It goes in your heartbeat and
   in every report. The gate is `<gate command>`.
6. Select the highest-priority `OPEN` task with no live claim.
7. Check the task ID is not already in the ledger:
   `awk -F'|' '/^\| <PREFIX>-/ {print $2}' .agent/TASK_LEDGER.md | sort | uniq -d`
   Non-empty output: stop and reconcile.
8. Claim atomically: create `.agent/CLAIMS/<task-id>.json`. If the file exists,
   abort and pick another. Never overwrite, edit, or delete another agent's claim.
9. Work only inside the claimed scope. Small diffs. Re-read any shared anchor
   immediately before every write to it.
10. Verify: run the gate. Green: update the ledger row, append the decision log,
    write `.agent/REPORTS/<date>-<agent-id>-<task-id>.md` including one reusable
    lesson, file the lesson as a pattern record in your own namespace, delete your
    claim, refresh your heartbeat. Red: mark `BLOCKED` with the failing output
    excerpt, write a blocker report, release the claim.
11. Claims older than the TTL are stale. Reap one only after cross-checking the
    clock against a timestamp you did not write, and only after noting the reap in
    the ledger. Never touch live claims or another agent's heartbeat.
12. On ambiguity: record it, mark the item `BLOCKED`, continue with unrelated work.
    Never guess on a load-bearing ambiguity.
```

### 15.4 `.agent/CLAIMS/README.md`

```markdown
---
document:
  title: "Claims directory"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# CLAIMS

Live locks only. Creating the file IS the claim: check existence first, and if the
file is present the task is taken. Never overwrite, edit, or delete another agent's
file. Delete only your own, on close-out. Released claims belong in
`.agent/CLAIMS_HISTORY/`, never here — a leftover file makes a finished task look
permanently taken.

Format:

    {"task": "<PREFIX>-0001", "agent_id": "<agent-id>", "started_utc": "<ISO-8601Z>",
     "ttl_min": <N>, "scope": ["<path/>"], "toolchain": "<version>"}
```

### 15.5 `.agent/HEARTBEATS/README.md`

```markdown
---
document:
  title: "Heartbeats directory"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# HEARTBEATS

Liveness signals, one file per active agent, refreshed every ~5 minutes while
active and immediately after any long operation. Never edit another agent's file.
Never delete one — a stale heartbeat is the evidence a reaper needs.

Format:

    {"agent_id": "<agent-id>", "model": "<model id>", "last_seen_utc": "<ISO-8601Z>",
     "task": "<PREFIX>-0001", "toolchain": "<version>", "note": "<one line>"}
```

### 15.6 `.agent/REPORTS/README.md`

```markdown
---
document:
  title: "Reports directory"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# REPORTS

One file per finished task: `<YYYY-MM-DD>-<agent-id>-<task-id>.md`, covering done
and blocked alike. Each MUST open with full provenance front matter.

Record: task ID and status; scope touched; commands run verbatim; toolchain
version; gate exit status and counts; files changed; what was deliberately not done
and why; one reusable lesson; open questions.

Reports are observations. They carry no authority. The task ledger and
`docs/DECISIONS.md` carry the record. A close-out with no report on disk is
malformed and the documentation gate checks for it.
```

### 15.7 `.agent/REPORTS/` — report skeleton

```markdown
---
document:
  title: "<task-id> — <title> (close-out report)"
  status: "Report"
provenance:
  author_llm: {name: "<agent-id>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<agent-id>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# <task-id> — <title>

## Status
DONE | BLOCKED

## Scope touched
`<exact paths>`

## Commands run
| Command | Result | Exit |
|---|---|---|
| `<gate command>` | <key counts> | 0 |

- Toolchain: `<version>`

## What was done
<What became different.>

## What was deliberately not done
<Findings observed but left alone, and why — usually: out of scope, new task row
seeded as <id>.>

## Files changed
`<paths>`

## Reusable lesson
<One specific, testable, transferable statement.>

## Open questions
<Numbered, for the documentarian or human. Empty if none.>
```

### 15.8 `.agent/PATTERNS/README.md`

```markdown
---
document:
  title: "PATTERNS — shared lesson store"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# PATTERNS

Advisory self-improvement records, one namespace per agent. Same tier as
`investigations/`: advisory, never canonical. Copying or citing never confers
authority.

- Placement: `.agent/PATTERNS/<agent-id>/<YYYY-MM-DD>-<slug>.md`. Your one stable
  `agent_id` is your namespace.
- Writes: your own namespace only. Reads: all namespaces.
- `agent_id` must match `[A-Za-z0-9._-]+`.
- Provenance: front matter with `author_llm`; assessor entries self-added only.
- Supersede, never rewrite: a corrected pattern is a new file that links the one it
  replaces.
- Filing: every close-out report gains one reusable lesson, filed as a record here.
- Boot skim: read the newest records across namespaces before claiming.
```

### 15.9 `docs/README.md`

```markdown
---
document:
  title: "docs index"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# docs

- `DECISIONS.md` — append-only record of what changed and why. A green gate plus a
  ledger entry plus a decision entry is the whole promotion process.
- `proposals/` — candidate work. Never truth. Never edit canonical files from here.
- `reports/` — observations and test results. No authority.
- `archive/` — superseded material, retained for history.
- Canonical truth lives in `<RULES>/`, `<CODE>/`, `AGENTS.md`, `guidelines/`.
- Live coordination lives in `.agent/`: boot at `00_BOOT.md`, tasks at
  `TASK_LEDGER.md`, then `CLAIMS/`, `HEARTBEATS/`, `REPORTS/`, `PATTERNS/`.
```

### 15.10 `docs/DECISIONS.md`

```markdown
---
document:
  title: "Decision log (append-only)"
  status: "Governance"
provenance:
  author_llm: {name: "<author>", version: "<version>"}
  assessor_llm: []
  last_modified_by_llm: {name: "<author>", version: "<version>"}
  created_date: "<YYYY-MM-DD>"
  last_modified_date: "<YYYY-MM-DD>"
---

# Decision log

Append-only. One entry per promotion. Never edit or delete an existing entry. If a
canonical reference's body must not be edited, record the interpretation here
instead, and correct the implementation to match.

<!-- Append new entries below this line. -->
```

Entry format:

```markdown
## <YYYY-MM-DD> — <task-id> — <one-line title>

- **Agent:** <agent_id> (<model version>)
- **Scope:** `<paths>`
- **Gate:** `<command>` → exit <status> (<key counts>)
- **Change:** <what became true that was not true before>
- **Why:** <reason; state the interpretation if this resolves an ambiguity>
- **Report:** `.agent/REPORTS/<path>.md`
- **Supersedes:** <entry or task-id, or none>
```

---

## 16. Open questions this specification does not settle

Flagged rather than guessed, in the spirit of the provenance rule.

1. **Monotonic ID allocation.** The source system assigns IDs informally and has
   already produced a collision. A target system should reserve IDs from a single
   sequence with an explicit reservation step. The exact mechanism — a counter file,
   a ledger append, or human assignment — is a project decision.
2. **Multi-tree operation.** The claim protocol serializes merges; it does not
   define how merges are performed. A target system running more than roughly 20
   concurrent workers needs an explicit merge protocol. Out of scope here.
3. **Documentarian throughput.** One documentarian against many workers is the
   specified ratio. Whether that holds depends on how much of the corpus is
   settled. The readiness contract (§7.3) makes the backlog measurable; what to do
   when the backlog grows is a human decision about capacity.
4. **Gate cost.** A conformance suite that takes minutes serializes all workers
   through §12.4. Splitting the gate into a fast pre-gate and a full gate, or giving
   each claim a scratch tree, is a project decision.
5. **Pattern store growth.** No pruning policy is defined. At some scale the boot
   skim becomes expensive and stops being read, which silently disables §9. A target
   system should decide a cap or an index early.
6. **Verifier independence.** Whether a verifier drawn from the same model family as
   the workers counts as independent assessment is unresolved, and the same question
   applies to the `assessor_llm` rule generally.

---

## 17. Change log

| Date | Change | Agent |
|---|---|---|
| 2026-09-26 | Initial version. Architecture, replication templates, failure-mode catalogue, bootstrap checklist, annotated mapping to the source system. | opencode (big-pickle) |
