---
document:
  title: "B5-1511 close-out — README agent-orientation proposal (proposal only, README not edited)"
  status: "Report — observation only, no authority"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  last_modified_by_llm: {name: "Kilo (kilo-auto/free)", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
task: B5-1511
---

# B5-1511 — Minimal agent-facing orientation block for the root README

**Scope honoured:** docs-only plus one proposal. **`README.md` was not edited** —
the row's scope is a proposal, and the edit needs its own claimed row. No gate
proposed. No commit, no push.

## The headline: the row's premise was false, and that reshaped the deliverable

B5-1511 grounds its work in the DONE B5-1429 census, which classified
`README.md` as pre-governance Figma-Make material first seen 2026-09-21T03:30
(before governance landed at `a9e7b1ff` 16:22) and concluded it **"orients
nobody"**.

**That conclusion has expired. Its evidence has not.** The README was reconciled
on 2026-09-28 under B5-0925 — its own front matter reads
`status: "Reconciled 2026-09-28 (B5-0925)"` with
`{name: "Cline (space-bunny) b5-0925"}` as the assessing LLM. It is now 143
lines and carries a complete agent-coordination section at **lines 102-116**.

Measured point by point against the five pointers the row names:

| Requested | Present | Evidence |
|---|---|---|
| `.agent/00_BOOT` as entry procedure | **YES** | L107 pointer; L116 imperative *"Read `.agent/00_BOOT.md` before touching anything here."* |
| `AGENTS.md` as governance | **YES** | L91 pointer; L109 gives its precedence over `AGENT_LOOP` |
| canonical rulebook | **YES** | L26-28, with the frozen-body rule and the DECISIONS-interpretations rule |
| queue runner command | **NO** | `run-queue` → **0** matches in the file |
| no-new-root-markdown rule | **NO** | only `root` hits are L118, L133 — both the Figma scaffold |

**Three of five pre-exist.** Adding a new block would have put two competing
entry points on one page.

## The residual gap, and the proposal

Two absences, three insertions — because the status-model sentence belongs with
the file-list that presupposes it, and the claim rule belongs with the boot
pointer.

1. **Queue runner + claim rule** — after L116. `run-queue.ps1 -DryRun` /
   `run-queue.sh -DryRun`, plus "creating the claim file *is* the claim". Placed
   at the end of the coordination list because it is the step between reading the
   boot file and doing the work.
2. **AGENTS §6 placement rule** — new bullet after L114, naming
   `docs/proposals/`, `docs/reports/`, `.agent/REPORTS/`, `investigations/`, and
   that the root holds governance, the rulebook and the README. AGENTS §6 is
   verified present at line 111.
3. **One-line status model** — after L100, above the coordination heading. L99-100
   already describe `docs/proposals/` and `docs/reports/` per file; this states
   the model those descriptions assume, and is the sentence that stops a reader
   treating a proposal as settled.

Proposal: `docs/proposals/readme-agent-orientation-block-proposal.md`, with the
exact `assessor_llm` line the edit would carry under AGENTS §1a.

## Authorship: an edit, not authorship

Per the row's explicit instruction, recorded in the proposal and repeated here:
a pre-existing file gaining content is an **edit**. `author_llm:
{name: "figma[bot]", version: "unknown"}` is not touched — AGENTS §1 forbids
overwriting original authorship — and this session enters as an **assessor**
(`passes: 1`), never as the author.

## Deliberate non-recommendations

- **No gate.** Per B5-1465's counter-argument: a rule that only ever produces a
  finding trains the fleet to ignore findings. Three bullets in a README are
  commentary; making them a check would be a different proposal with a different
  cost.
- **No re-opening of B5-1429's stray classification.** `QWEN.md`,
  `koda-memory.md` and `loop-prompt.md` are untouched by this row. Only the
  "orients nobody" *conclusion* is superseded; the census's first-seen git
  evidence is not contested.

## Gates

`b5ccg\compile.bat` green on JDK 1.8.0_292, `-source 1.6 -target 1.6`,
`Build successful`, exit 0, standing bootstrap warning only. No src touched.
Post-write `run-dup-census.ps1` **PASS, 0 duplicate IDs**; my row reads
`pipeCount 7` / `doubleLead no`.

## Reusable lesson

**A census's receipts do not carry its verdict.** B5-1429's evidence was correct
and current; its conclusion had been overtaken by a later merge. Citing "B5-1429
says" would have transferred authority the evidence supports and the conclusion
no longer does. The check that separated them was one grep — `run-queue` returns
0 while `00_BOOT` returns 2 — and it is cheaper than any amount of reading.