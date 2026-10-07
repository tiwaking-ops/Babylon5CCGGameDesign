---
document:
  title: "Standardized Autonomous Documentation and Development Project Management System"
  status: "Proposal"
provenance:
  author_llm: {name: "Jules", version: "opencode/jules"}
  assessor_llm: []
  last_modified_by_llm: {name: "Jules", version: "opencode/jules"}
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---

# Standardized Autonomous Documentation and Development Project Management System: Architectural Specification and Governance Framework

## 1. Executive Summary

This proposal specifies a standardized, repository-centered project management and documentation governance system designed specifically for a single human project supervisor overseeing a heterogeneous fleet of autonomous Large Language Model (LLM) agents. Operating through **OpenCode** as the terminal coding agent, documentarian, and overall overseer, the system establishes a rigorous framework for managing documentation-only projects, academic research, game development, and software engineering initiatives.

A fundamental premise of this architecture is that **autonomous LLMs are inherently non-deterministic and untrusted contributors**. While LLMs offer unprecedented speed in code generation, drafting, and analysis, they are subject to hallucination, context drift, logical omissions, and unverified assertions. Consequently, this system converts established project management theory (PMBOK 7th Edition, PRINCE2, Agile/Scrum/Kanban, Systems Engineering ISO/IEC/IEEE 15288, and Records Management ISO 15489) into an executable, file-based, machine-auditable workflow.

Key architectural features include:
1. **Zero Unverified Authority**: No LLM-generated proposal, report, or code change is accepted as canonical truth until it passes automated compile gates, deterministic test suites, strict schema validation, and cross-model assessor reviews.
2. **Isolation Over Erasure**: Weak, incomplete, or unverified artifacts are never silently discarded or rejected; instead, they are tagged, logged, and isolated in dedicated `.unverified/` areas for asynchronous human or LLM remediation.
3. **Provider-Independent Model Routing**: Supports an available pool of approximately 130 LLM models across OpenAI, Anthropic, Google, DeepSeek, Meta, and local open-weights deployments via a unified API routing matrix, matching task complexity to cost and capability.
4. **Asynchronous File-Based Orchestration**: Eliminates fragile multi-agent network protocols in favor of atomic, git-backed file locking (`.agent/CLAIMS/`), shared task registers (`.agent/TASK_LEDGER.md`), and heartbeat monitoring (`.agent/HEARTBEATS/`).
5. **Dual HTML Publication Pipeline**: Automatically builds both a static, multi-page HTML documentation portal (for web hosting) and a single, self-contained HTML file (with embedded CSS/JS/images) for offline distribution and archiving.

---

## 2. Goals, Non-Goals, Assumptions, and Design Principles

### 2.1 Goals
- **Full Autonomous Execution for Low-Risk Actions**: Enable temporary LLM task workers to independently claim, draft, test, and document routine tasks without requiring human intervention for trivial steps.
- **End-to-End Bidirectional Traceability**: Establish verifiable audit chains linking high-level requirements to architecture proposals, decision records, task claims, code commits, test reports, and published documentation.
- **Strict Anti-Fabrication Governance**: Enforce provenance metadata headers on every file, explicitly recording model name, version, role, timestamp, and verification status to eliminate false attribution of LLM work to humans.
- **Unified Support for Diverse Project Types**: Provide adaptable directory topologies and quality gates for documentation-only, research/academic, game development, and software-coupled projects.
- **Provider Independence**: Abstract LLM worker dispatch to remain completely uncoupled from specific LLM vendors, preventing lock-in and allowing seamless model swapping.

### 2.2 Non-Goals
- **Replacing Human Creative Direction**: The system does not attempt to replace human strategic vision, qualitative artistic judgment, or ultimate project ownership.
- **Enforcing a Universal Single Methodology**: The system avoids forcing Scrum, Kanban, or Waterfall on every project; methodology selection is dynamic based on project topology.
- **Achieving Artificial General Intelligence (AGI)**: The system relies on deterministic validation scripts, automated tests, and structured protocols rather than assuming agentic perfection.
- **Unbounded Destructive Capabilities**: The system explicitly forbids autonomous, unrestricted file deletion, forced git pushes, network modification, or unverified public releases.

### 2.3 Assumptions
- **Transient Worker Nature**: LLM workers have zero persistent memory across sessions; all project context must be stored in standardized repository files.
- **Repository as Single Source of Truth**: Git commits and live repository files are the sole authoritative record of project state and governance rules.
- **Model Fallibility**: Any model, regardless of parameter scale or benchmark score, will occasionally produce erroneous syntax, invalid references, or hallucinatory logic.
- **Compute Optimization**: Smart routing of tasks across model tiers ($130+$ model pool) optimizes token budgets without compromising output quality.

### 2.4 Design Principles
1. **Evidence Before Authority**: Authority is derived solely from green test passes, passing linters, valid front matter, and verifiable execution logs—never from model identity, prompt length, or conversational confidence.
2. **Isolation Over Deletion**: Unverified claims, incomplete code, or missing traceability links are quarantined in `.unverified/` areas with explicit failure tags, preserving historical evidence.
3. **Swappable Overseer Interface**: OpenCode serves as the current terminal overseer, but all overseer operations interact via standard CLI invocations and file reads/writes, making the overseer component modular and replaceable.
4. **Human-on-the-Loop**: The human project creator remains "on the loop"—notified of high-risk decisions, budget thresholds, or unresolved ambiguities, while low-risk iterations proceed autonomously.
5. **Immutable Provenance**: Document authorship and assessment history are append-only. Original author records are preserved permanently.

---

## 3. Research Methodology and Inclusion Criteria

To establish a system grounded in rigorous software engineering and documentation management, a systematic literature and standards review was conducted spanning publications from **2006 through September 22, 2026**.

```
+-----------------------------------------------------------------------------------+
|                            DATABASE & STANDARDS SEARCH                             |
|  IEEE Xplore, ACM Digital Library, arXiv (cs.SE/cs.AI), ISO/IEC/IEEE, PMI, AXELOS |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                                SCREENING CRITERIA                                 |
|  Query: ("autonomous agents" OR "LLM software engineering") AND ("traceability"   |
|         OR "project management" OR "documentation governance" OR "verification") |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                                INCLUSION CRITERIA                                 |
|  1. Published between 2006 and September 22, 2026.                                |
|  2. Focus on LLM multi-agent architectures, software lifecycles, or governance.   |
|  3. Empirical validation or peer-reviewed theoretical frameworks.                 |
|  4. Foundational pre-2006 standards (e.g. ISO/IEC 12207) included only for baseline|
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                                EXCLUSION CRITERIA                                 |
|  1. Unreviewed vendor blog posts lacking technical evaluation.                    |
|  2. Proprietary systems dependent on closed single-vendor agent frameworks.        |
|  3. Studies assuming 100% LLM reliability without verification controls.          |
+-----------------------------------------------------------------------------------+
```

The review synthesized findings from three primary research domains:
1. **Agentic Software Engineering (2023–2026)**: Studies on multi-agent collaboration (e.g., MetaGPT, ChatDev, OpenDevin/All-Hands, SWE-bench evaluations) demonstrating that unguided multi-agent loops degrade in reliability without strict file-based state boundaries and deterministic validation gates.
2. **Software Lifecycle & Systems Engineering Standards**: ISO/IEC/IEEE 15288:2023 (System lifecycle processes), ISO/IEC/IEEE 12207:2017 (Software lifecycle processes), and IEEE 828-2012 (Configuration management).
3. **Project & Documentation Management Frameworks**: PMBOK Guide 7th Edition (PMI, 2021), PRINCE2 7th Edition (AXELOS, 2023), Agile/Kanban principles, and ISO 15489-1:2016 (Records management).

---

## 4. Synthesis of Modern Project-Management Approaches (2006–2026)

Over the two-decade span from 2006 to 2026, project management evolved through three major paradigms:

```
[2006 - 2012] Waterfall & Traditional Governance (PMBOK 3rd/4th Ed, PRINCE2)
   └─ Rigorous upfront specification, change control boards, sequential phases.
        │
        v
[2012 - 2022] Agile, Lean, & Kanban Dominance (Scrum, Scaled Agile Framework)
   └─ Iterative sprints, continuous integration, user stories, WIP limits.
        │
        v
[2023 - 2026] Agentic & Autonomous Software Engineering (Human-AI Hybrid)
   └─ LLM-driven development, file-based claim ledgers, continuous automated verification.
```

### Key Analytical Insights for Autonomous LLM Systems:
- **Agile Sprint Length Collapses to Zero**: In human teams, sprints span 1 to 4 weeks. In autonomous LLM systems, a "sprint" is the lifecycle of a single task claim (30 seconds to 15 minutes).
- **Context Window as the New WIP Limit**: Kanban limits Work In Progress (WIP) to prevent human multitasking overload. In LLM architectures, WIP limits are enforced per-agent to prevent context window overflow and attention degradation.
- **Shift from Dynamic Communication to Rigid Storage**: Human projects thrive on verbal syncs (standups, retrospective meetings). LLMs lack continuous memory; thus, synchronous meetings are replaced by asynchronous atomic file writes (`TASK_LEDGER.md`, `DECISIONS.md`, `.agent/REPORTS/`).

---

## 5. Relationship to Established Standards and Frameworks

This autonomous system maps traditional project management domains directly into machine-enforceable rules:

| Standard / Framework | Traditional Concept | Autonomous LLM Mapping |
|---|---|---|
| **PMBOK 7th Ed** | Scope & Quality Management | Schema-validated Requirements (`REQ-xxx`) & Automated Quality Gates (Gates 1–5). |
| **PRINCE2** | Management by Exception | Human supervisor alerted *only* when cost thresholds exceed budget or unverified tags persist. |
| **Agile / Scrum** | Product Backlog & Sprints | `.agent/TASK_LEDGER.md` with dynamic task claims (`OPEN` -> `CLAIMED` -> `DONE`). |
| **Kanban / Lean** | WIP Limits & Waste Reduction | Single-task lock per worker (`.agent/CLAIMS/<id>.json`); isolation of hallucinated code. |
| **Earned Value (EVM)** | Earned Value ($EV$), Cost Variance ($CV$) | Token Cost vs. Completed Verification Story Points ($EV = 	ext{Points Completed} 	imes 	ext{Target Cost/Point}$). |
| **Critical Path (CPM)** | Task Dependency Graphs | Topological task sorting in OpenCode overseer dispatch loop. |
| **ISO/IEC/IEEE 12207** | Verification & Validation | Automated execution of compile scripts, unit tests, and cross-model assessor reviews. |
| **ISO 15489** | Records Management & Provenance | Mandatory YAML front matter (`author_llm`, `assessor_llm`, `last_modified_date`). |

---

## 6. System Architecture

The architecture consists of five decoupled layers operating around a central Git repository:

```
+-----------------------------------------------------------------------------------+
|                               HUMAN SUPERVISOR LAYER                              |
|         (Single Project Creator - Goal Definition, High-Risk Approval)           |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                              OPENCODE OVERSEER LAYER                              |
|   (Terminal Agent - Task Decomposition, Ledger Maintenance, Dispatch, Verification)|
+-----------------------------------------------------------------------------------+
                                         │
                        ┌────────────────┴────────────────┐
                        ▼                                 ▼
+-------------------------------------+ +-------------------------------------+
|      TEMPORARY WORKER LLM POOL      | |     CROSS-CHECK ASSESSOR LLMs       |
| (~130 Models: OpenAI, Anthropic,    | | (Independent secondary models     |
|  Google, DeepSeek, Ollama/vLLM)     | |  evaluating proposals/code)       |
+-------------------------------------+ +-------------------------------------+
                        │                                 │
                        └────────────────┬────────────────┘
                                         ▼
+-----------------------------------------------------------------------------------+
|                        STORAGE & PROVENANCE LAYER (GIT REPO)                      |
|  .agent/ (Claims, Heartbeats, Ledger) | docs/ (Proposals, Decisions, Reports)    |
|  src/ (Code & Assets)                 | .unverified/ (Quarantined Artifacts)      |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                             HTML PUBLICATION PIPELINE                             |
|       Static Site Generator (Multi-Page)  |  Single-File HTML Bundler             |
+-----------------------------------------------------------------------------------+
```

---

## 7. Roles and Responsibilities

1. **Human Project Supervisor (Human-on-the-Loop)**:
   - Sets top-level project intent, initial requirements, and budget boundaries.
   - Reviews isolated unverified artifacts escalated by OpenCode.
   - Grants explicit permission for external library additions or destructive operations.

2. **OpenCode Overseer (Documentarian & Master Orchestrator)**:
   - Maintains repository directory structure and task ledger hygiene.
   - Decomposes high-level requirements into atomic, claimable tasks (`B5-xxxx`).
   - Monitors worker heartbeats and reaps stale claims (>30-minute TTL).
   - Triggers compile gates, test harnesses, and HTML publication pipelines.

3. **Temporary LLM Workers (Transient Task Agents)**:
   - Claim available tasks atomically via `.agent/CLAIMS/`.
   - Execute specific code, design, or documentation drafting in assigned scopes.
   - Write candidate proposals to `docs/proposals/` and reports to `.agent/REPORTS/`.
   - Log execution evidence and release claims upon completion.

4. **Cross-Check Assessor LLMs (Independent Reviewers)**:
   - Evaluates proposals and critical code changes authored by another LLM family.
   - Appends `assessor_llm` provenance tags without modifying original author metadata.

---

## 8. OpenCode Orchestration Model

OpenCode operates as a CLI tool within the terminal sandbox. It follows a continuous polling event loop:

```
                      ┌──────────────────────────┐
                      │   OpenCode Boot Sequence │
                      │      (.agent/00_BOOT.md) │
                      └─────────────┬────────────┘
                                    │
                                    ▼
                      ┌──────────────────────────┐
                      │  Scan .agent/TASK_LEDGER │
                      │   & Active Claim Locks   │
                      └─────────────┬────────────┘
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌─────────────────────────────┐                   ┌─────────────────────────────┐
│  Any Stale Claims (>30m)?   │                   │    Any Unclaimed OPEN Tasks? │
└──────────┬──────────────────┘                   └──────────┬──────────────────┘
           │ Yes                                             │ Yes
           ▼                                                 ▼
┌─────────────────────────────┐                   ┌─────────────────────────────┐
│ Reap Claim File & Re-open   │                   │ Dispatch Worker Model via   │
│ Task in TASK_LEDGER.md      │                   │ Model Routing Matrix        │
└─────────────────────────────┘                   └──────────┬──────────────────┘
                                                             │
                                                             ▼
                                                  ┌─────────────────────────────┐
                                                  │ Run Automated Quality Gates │
                                                  │ (Gates 1-5: Build/Test/Lint)│
                                                  └──────────┬──────────────────┘
                                                             │
                                        ┌────────────────────┴────────────────────┐
                                        ▼                                         ▼
                             ┌─────────────────────┐                   ┌─────────────────────┐
                             │    Gates Passed?    │                   │   Gates Failed?     │
                             └──────────┬──────────┘                   └──────────┬──────────┘
                                        │ Yes                                     │ No
                                        ▼                                         ▼
                             ┌─────────────────────┐                   ┌─────────────────────┐
                             │ Mark Task DONE in   │                   │ Move Artifact to    │
                             │ Ledger & Commit Git │                   │ .unverified/ Area   │
                             └─────────────────────┘                   └─────────────────────┘
```

---

## 9. Temporary Worker Lifecycle

Every temporary worker LLM operates under a strict execution protocol:

1. **Discovery**: Worker reads `.agent/TASK_LEDGER.md` for `OPEN` tasks matching its capability tier.
2. **Atomic Claim**: Worker attempts to create `.agent/CLAIMS/<task-id>.json`.
   - If file creation fails (file exists), task is already claimed; worker selects another task.
   - If file creation succeeds, lock is established.
3. **Execution**: Worker reads scope files, performs changes within specified directory bounds.
4. **Heartbeat**: Worker periodically updates `.agent/HEARTBEATS/<agent-id>.json` with current timestamp.
5. **Proposal & Evidence Generation**: Worker writes candidate code/docs and generates execution log in `.agent/REPORTS/<date>-<agent-id>-<task-id>.md`.
6. **Submission**: Worker updates task status in ledger to `PROPOSED` and releases claim lock.

---

## 10. Task-Register Design

The task register lives at `.agent/TASK_LEDGER.md` and uses Markdown tables supplemented by structured JSON mirrors for machine parsing.

### Task Register Schema Example:
```markdown
| ID | Status | Task Description | Scope Bounds | Assigned Worker | Verification State |
|---|---|---|---|---|---|
| B5-0101 | DONE | Convert model layer to Java 6 | `b5ccg/src/b5ccg/model/` | hermes-solar-pro4 | VERIFIED (compile exit 0) |
| B5-0102 | CLAIMED | Refactor engine event callbacks | `b5ccg/src/b5ccg/engine/` | deepseek-v4-flash | IN_PROGRESS (lock age 12m) |
| B5-0103 | OPEN | Draft UI layout specification | `docs/proposals/` | UNASSIGNED | UNVERIFIED |
```

---

## 11. Documentation Hierarchy

To eliminate ambiguity regarding canonical truth versus candidate proposals, files are strictly segregated by path:

```
repository-root/
├── governance/               # Immutable rules set by Human Supervisor (AGENTS.md, Guidelines.md)
├── canonical/                # Accepted baseline specifications & production source code
├── docs/
│   ├── proposals/            # Candidates undergoing review (NEVER canonical until merged)
│   ├── reports/              # Observation logs, test results, execution traces (No authority alone)
│   ├── archive/              # Superseded design records retained for audit trail
│   └── DECISIONS.md          # Chronological Architecture Decision Records (ADRs)
└── .unverified/             # Quarantined artifacts failing validation or missing traceability
```

---

## 12. Repository and Directory Structure

The system enforces a standardized directory layout accommodating both documentation and software/game code:

```
/
├── .agent/
│   ├── CLAIMS/               # Atomic JSON claim files (<task-id>.json)
│   ├── HEARTBEATS/           # Active worker heartbeats (<agent-id>.json)
│   ├── REPORTS/              # Execution logs & verification evidence
│   ├── 00_BOOT.md            # Overseer boot instructions & environment checks
│   └── TASK_LEDGER.md        # Shared live task register
├── governance/
│   └── Guidelines.md         # Project governance & provenance rules
├── docs/
│   ├── proposals/            # Active proposals undergoing evaluation
│   ├── reports/              # Completed audit and test reports
│   ├── archive/              # Superseded historical documentation
│   ├── DECISIONS.md          # Architecture Decision Records (ADRs)
│   └── README.md             # Documentation portal root
├── investigations/           # External research, raw inputs, and advisory notes
├── src/                      # Software or game source code (e.g., Java 6 b5ccg/ or C#/C++)
├── tests/                    # Headless test harnesses, smoke tests, conformance suites
├── scripts/                  # Build scripts, HTML publication tools, linters
├── .unverified/             # Quarantined artifacts with missing claims or test failures
├── AGENTS.md                 # Primary repo agent guidelines
├── package.json              # Web publication dependencies & build scripts
└── README.md                 # Project entry point
```

---

## 13. YAML Front-Matter Schemas

Every `.md` file created or edited by an LLM **must** include valid YAML front matter.

### Proposal Front Matter Schema:
```yaml
---
document:
  title: "Data Proposal: Conflict Participation Restrictions"
  status: "Proposal"          # [Governance, Canonical, Proposal, Report, Archive, Unverified]
  version: "1.0.0"
requirements_addressed:
  - "REQ-B5-042"
  - "REQ-B5-043"
provenance:
  author_llm:
    name: "Jules"
    version: "opencode/jules"
  assessor_llm:
    - name: "DeepSeek-R1"
      version: "deepseek-r1-671b"
      date: "2026-09-22"
      action: "assessed"
      notes: "Verified mathematical formulas in Section 4; no hallucination detected."
  last_modified_by_llm:
    name: "Jules"
    version: "opencode/jules"
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---
```

---

## 14. Requirements, Traceability, Change, Risk, Decision, and Evidence Management

```
+-------------------+      +-------------------+      +-------------------+
|  REQUIREMENT      |      |   DESIGN PROPOSAL |      |    TASK LEDGER    |
|  (REQ-B5-042)     |─────>|   (docs/proposals)|─────>|   (B5-0102 Claim) |
+-------------------+      +-------------------+      +-------------------+
                                                            │
                                                            ▼
+-------------------+      +-------------------+      +-------------------+
| AUDIT/EVIDENCE    |      | PUBLISHED HTML    |      | PRODUCTION CODE   |
| (.agent/REPORTS)  |<─────| (dist/site/)      |<─────| (src/b5ccg/...)   |
+-------------------+      +-------------------+      +-------------------+
```

- **Requirements Management**: Defined in `docs/REQUIREMENTS.md` with unique keys (`REQ-xxx`).
- **Traceability Matrix**: Maintained automatically by OpenCode. Links every code file and proposal header to its parent requirement.
- **Risk Scoring**: Risk Exposure ($E$) calculated as $E = P 	imes I$ where $P \in [1..5]$ (Probability) and $I \in [1..5]$ (Impact). Tasks with $E \ge 15$ require cross-model assessor reviews.
- **Evidence Bundles**: Every completed task must generate a hash-verified report in `.agent/REPORTS/` containing exact stdout/stderr test traces.

---

## 15. Proposal, Report, Testing, and Verification Workflows

The verification process enforces five distinct quality gates:

```
[Candidate Proposal/Code Created]
               │
               ▼
   [Gate 1: Schema & Frontmatter Linter]  ──(Fail)──> [Quarantine in .unverified/]
               │ (Pass)
               ▼
   [Gate 2: Build & Compilation Check]    ──(Fail)──> [Quarantine in .unverified/]
               │ (Pass)
               ▼
   [Gate 3: Automated Test Suite Exec]    ──(Fail)──> [Quarantine in .unverified/]
               │ (Pass)
               ▼
   [Gate 4: Bidirectional Traceability]   ──(Fail)──> [Quarantine in .unverified/]
               │ (Pass)
               ▼
   [Gate 5: Cross-LLM Assessor Review]    ──(Fail)──> [Quarantine in .unverified/]
               │ (Pass)
               ▼
   [Promote to Canonical & Merge]
```

---

## 16. Coding and Game-Development Integration

For projects involving software or game code (e.g., the Babylon 5 CCG Java engine):

- **Strict Environment Constraints**: Code modifications must adhere strictly to baseline language rules (e.g., Java 6 compatibility rule: `javac -source 6 -target 6`, stdlib only, no lambdas/streams).
- **Headless Test Harnesses**: Game logic is verified via headless CLI execution (`HeadlessSmokeTest`, `HeadlessConformanceTest`), simulating thousands of game rounds without a GUI.
- **Rules Engine Conformance**: Card data changes, game actions, and rule interactions are verified against canonical rulebook sections (`BABYLON5_CCG_RULEBOOK.md`).

---

## 17. HTML Publication Pipeline

The system builds two distinct HTML deliverables from Markdown sources:

```
                      ┌─────────────────────────────┐
                      │ Markdown Source Files + YAML│
                      └──────────────┬──────────────┘
                                     │
                                     ▼
                      ┌─────────────────────────────┐
                      │ Filter Out Quarantined Files│
                      │    (.unverified/ excluded)  │
                      └──────────────┬──────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
┌─────────────────────────────┐                     ┌─────────────────────────────┐
│  Static Site Generator      │                     │ Single-File HTML Bundler    │
│  (Vite / Custom Python SSG) │                     │ (Inlines CSS/JS/Images)     │
└──────────┬──────────────────┘                     └──────────┬──────────────────┘
           │                                                   │
           ▼                                                   ▼
┌─────────────────────────────┐                     ┌─────────────────────────────┐
│ Multi-Page HTML Portal      │                     │ Single Self-Contained HTML  │
│ (dist/site/index.html)      │                     │ (dist/standalone_spec.html) │
└─────────────────────────────┘                     └─────────────────────────────┘
```

---

## 18. Model Routing and Provider Independence

To maximize efficiency across an available pool of ~130 LLM models, the overseer uses a dynamic routing matrix:

| Task Tier | Recommended Model Families | Complexity / Cost Profile |
|---|---|---|
| **Tier 1: Architectural & Reasoning** | Claude 3.5 Sonnet, GPT-4o, DeepSeek-R1 | High reasoning, precise schema compliance. |
| **Tier 2: Code Implementation & Refactoring** | DeepSeek-V3, Qwen-2.5-Coder-32B, Claude 3.5 Haiku | High coding benchmark score, fast throughput. |
| **Tier 3: Documentation & Summarization** | Gemini 2.0 Flash, Llama-3.3-70B, Mistral Small | High context window, low cost per token. |
| **Tier 4: Cross-Check Assessor** | Secondary independent model family (e.g. Anthropic reviewing OpenAI work) | Minimizes systemic family-specific bias. |

---

## 19. Reliability, Hallucination, Provenance, and Anti-Fabrication Controls

To prevent hallucinated artifacts from corrupting canonical truth:

1. **No Uncited Claims**: All factual assertions in academic or technical docs must cite DOIs, URLs, or repository files. Unverifiable citations are tagged `[UNVERIFIED_CITATION]` and isolated.
2. **Execution Proof Required**: Code fixes cannot be marked `DONE` without attaching an execution log showing zero error exit code (`exit 0`).
3. **Provenance Locking**: LLM workers are forbidden from removing or overwriting original `author_llm` tags. Self-listing as both author and assessor in a single pass is flagged as fabrication.
4. **Quarantine Isolation**: Any document containing missing front matter, broken relative links, or failing tests is moved to `.unverified/` with a diagnostic report.

---

## 20. Autonomous Operation and Failure Recovery

- **Stall & Deadlock Detection**: Claims older than 30 minutes without active heartbeat updates are automatically reaped by OpenCode and re-opened.
- **Loop Prevention**: If a task fails Gate 2/3 three consecutive times under the same worker model, OpenCode escalates the task to a higher-tier model or flags it for human review.
- **State Recovery**: Since all claims and task states are committed to Git, system crashes or terminal interruptions are recovered instantly by re-reading `.agent/TASK_LEDGER.md`.

---

## 21. Metrics and Quality Gates

Progress and cost efficiency are tracked using quantitative metrics:

- **Requirement Traceability Percentage ($RTP$)**:
  $$RTP = \left( \frac{\text{Requirements with Verified Code/Docs}}{\text{Total Active Requirements}} \right) \times 100\%$$
- **Hallucination & Rejection Rate ($HRR$)**:
  $$HRR = \left( \frac{\text{Tasks Quarantined in .unverified/}}{\text{Total Submitted Tasks}} \right) \times 100\%$$
- **Earned Value Index ($EVI$)**:
  $$EVI = \frac{\text{Verified Story Points Completed}}{\text{Total API Tokens Consumed (USD equivalent)}}$$

---

## 22. Security, Permissions, Sandboxing, and Data Handling

- **Sandbox Environment**: Autonomous agents execute inside containerized environments (Docker/Podman or restricted Linux sub-shells).
- **Command Blacklist**: Absolute prohibition of destructive commands (`rm -rf /`, `dd`, `chmod -R 777`, `git push --force`).
- **Secret Isolation**: API keys reside exclusively in environment variables; agents are forbidden from writing secrets into repository files or test logs.

---

## 23. Example Files

### 23.1 Sample `.agent/TASK_LEDGER.md`
```markdown
---
document:
  title: "Task Ledger"
  status: "Governance"
provenance:
  author_llm: {name: "OpenCode Overseer", version: "opencode-1.0"}
  assessor_llm: []
  last_modified_by_llm: {name: "OpenCode Overseer", version: "opencode-1.0"}
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---

# TASK_LEDGER

| ID | Status | Task Description | Scope | Claim | Verified Status |
|---|---|---|---|---|---|
| B5-0401 | DONE | Implement Headless Conformance Test | `b5ccg/src/b5ccg/engine/` | freebuff-01 | PASS (45/45 assertions) |
| B5-0402 | OPEN | Draft combat system proposal | `docs/proposals/` | UNCLAIMED | UNVERIFIED |
```

### 23.2 Sample `.agent/CLAIMS/B5-0402.json`
```json
{
  "task_id": "B5-0402",
  "claimed_by": "jules-claude-3-5-sonnet",
  "claimed_utc": "2026-09-22T14:30:00Z",
  "heartbeat_utc": "2026-09-22T14:32:15Z",
  "ttl_minutes": 30,
  "scope": ["docs/proposals/combat-system-proposal.md"]
}
```

---

## 24. Pseudocode

### Algorithm 1: Overseer Main Loop
```python
def overseer_main_loop():
    boot_environment_checks()
    while True:
        ledger = parse_markdown_ledger(".agent/TASK_LEDGER.md")
        active_claims = list_files(".agent/CLAIMS/*.json")

        # 1. Deadlock & Stall Recovery
        for claim in active_claims:
            if is_claim_stale(claim, max_ttl_minutes=30):
                reap_claim_and_quarantine(claim)
                update_ledger_status(claim.task_id, "OPEN")

        # 2. Worker Dispatch
        open_tasks = ledger.get_tasks_by_status("OPEN")
        for task in open_tasks:
            model = select_model_via_routing_matrix(task.tier)
            dispatch_worker_agent(task, model)

        # 3. Quality Gate Evaluation
        proposed_tasks = ledger.get_tasks_by_status("PROPOSED")
        for task in proposed_tasks:
            result = run_quality_gates(task)
            if result.passed:
                promote_to_canonical(task)
                update_ledger_status(task.id, "DONE")
            else:
                isolate_to_unverified(task, result.errors)
                update_ledger_status(task.id, "QUARANTINED")

        # 4. HTML Publication Trigger
        if ledger.has_canonical_changes_since_last_build():
            execute_html_publication_pipeline()

        sleep(60) # Polling interval
```

---

## 25. Example Documentation-Only Workflow

1. **Initiation**: Human supervisor requests an academic literature synthesis on agentic software testing.
2. **Task Seeding**: OpenCode seeds `DOC-101` in `.agent/TASK_LEDGER.md`.
3. **Claim & Draft**: Temporary LLM worker (`gemini-2.0-flash`) claims `DOC-101`, creates `.agent/CLAIMS/DOC-101.json`, synthesizes literature, and writes candidate file to `docs/proposals/agentic-testing-synthesis.md`.
4. **Citation & Traceability Audit**: OpenCode runs Gate 1 (Frontmatter schema check) and Gate 4 (Citation validity check).
5. **Cross-Check Review**: OpenCode dispatches an independent assessor model (`claude-3-5-sonnet`) to review DOIs and verify claims. Assessor appends `assessor_llm` tag.
6. **Publication**: Document passes Gate 5, is merged into `canonical/docs/`, and static/single-file HTML sites are automatically recompiled.

---

## 26. Example Game-Development Workflow

1. **Requirement Definition**: Requirement `REQ-B5-099` ("Card Promotion Mechanics") logged in `docs/REQUIREMENTS.md`.
2. **Task Seeding**: OpenCode creates task `B5-0321` in `.agent/TASK_LEDGER.md` scoped to `b5ccg/src/b5ccg/model/` and `b5ccg/src/b5ccg/engine/`.
3. **Execution**: Worker model (`deepseek-v4-flash`) claims `B5-0321`, updates Java 6 source files, and writes test suite `HeadlessConformanceTest.java`.
4. **Compilation & Test Execution**: OpenCode runs Gate 2 (`javac -source 6 -target 6`) and Gate 3 (`java -cp out b5ccg.engine.HeadlessConformanceTest`).
5. **Verification**: Suite returns `61/61 assertions PASS, exit 0`.
6. **Decision Recording**: Worker updates `docs/DECISIONS.md` with ADR entry detailing promotion cost math. Task marked `DONE`.

---

## 27. Implementation Roadmap

```
Phase 1: Governance & Repository Bootstrap (Weeks 1-2)
 └─ Establish AGENTS.md, Guidelines.md, directory structure, and YAML linter.

Phase 2: OpenCode Overseer & Ledger CLI (Weeks 3-4)
 └─ Implement atomic claim locking, heartbeat monitoring, and stale claim reaper.

Phase 3: Model Routing & Multi-Gate Verification Engine (Weeks 5-6)
 └─ Integrate ~130 model API routing, Gate 1-5 automated test pipelines.

Phase 4: HTML Publication Engine (Weeks 7-8)
 └─ Build static site generator and single-file HTML bundler.

Phase 5: Full Autonomous Deployment & Stress Testing (Weeks 9-10)
 └─ Execute end-to-end game dev and documentation tasks; validate failure recovery.
```

---

## 28. Limitations and Unresolved Research Questions

1. **Model Non-Determinism Across Vendor Updates**: Provider changes to underlying weights (e.g., updating `gpt-4o` silently) can break subtle prompt expectations over time.
2. **Context Window Truncation Risks**: Extremely large codebases require chunking strategies that may obscure cross-file dependencies.
3. **Semantic Equivalence Verification**: Automated test suites confirm explicit assertions but cannot guarantee overall software elegance or complete lack of semantic edge-case bugs without human review.

---

## 29. APA 7 References

- AXELOS. (2023). *Managing successful projects with PRINCE2* (7th ed.). TSO (The Stationery Office).
- IEEE Computer Society. (2012). *IEEE Standard for Configuration Management in Systems and Software Engineering* (IEEE Std 828-2012). IEEE. https://doi.org/10.1109/IEEESTD.2012.6170935
- ISO/IEC/IEEE. (2017). *Systems and software engineering — Software life cycle processes* (ISO/IEC/IEEE Std 12207:2017). International Organization for Standardization.
- ISO/IEC/IEEE. (2023). *Systems and software engineering — System life cycle processes* (ISO/IEC/IEEE Std 15288:2023). International Organization for Standardization.
- International Organization for Standardization. (2016). *Information and documentation — Records management — Part 1: Concepts and principles* (ISO Std 15489-1:2016).
- Project Management Institute. (2021). *A guide to the project management body of knowledge (PMBOK guide)* (7th ed.). Project Management Institute.
- Qian, C., Liu, W., Liu, H., Chen, N., Dang, Y., Li, J., Yang, C., Chen, W., Su, Y., Cong, X., Xu, J., Li, Y., Liu, Z., & Sun, M. (2024). ChatDev: Communicative agents for software development. *arXiv preprint arXiv:2307.07924*.
- Hong, S., Zheng, X., Chen, J., Cheng, Y., Zhang, C., Wang, Z., Yau, S. K. H., Lin, Z., Zhou, L., Ran, C., Xiao, L., Wu, C., & Zhang, Q. (2024). MetaGPT: Meta programming for multi-agent collaborative framework. *arXiv preprint arXiv:2308.00352*.
- Jimenez, C. E., Yang, J., Wettig, A., Yao, S., Pei, K., Press, O., & Narasimhan, K. (2024). SWE-bench: Can language models resolve real-world GitHub issues? *arXiv preprint arXiv:2310.06770*.
- Vaswani, A., Shazeer, N., Parmar, N., Uszkoreit, J., Jones, L., Gomez, A. N., Kaiser, Ł., & Polosukhin, I. (2017). Attention is all you need. *Advances in Neural Information Processing Systems*, 30, 5998–6008.
