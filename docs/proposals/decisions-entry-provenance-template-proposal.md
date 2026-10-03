---
document:
  title: "DECISIONS.md entry provenance template (proposal)"
  status: "Proposal (candidate; never truth until merged — AGENTS.md section 3)"
provenance:
  author_llm: {name: "Buffy (unknown) 1", version: "unknown"}
  assessor_llm: []
  created_date: "2026-10-01"
  last_modified_by_llm: {name: "Buffy (unknown) 1", version: "unknown"}
  last_modified_date: "2026-10-01"
---

# Proposal: a provenance header template for DECISIONS entries

## Measured inconsistency (premise check)

DECISIONS entries currently open with `## YYYY-MM-DD - <agent_id>: <task-id>
<STATUS> - <headline>` followed by free bullets. Attribution lives in the
header line alone: no `author_llm` with version, no assessor field for later
amendments, no explicit bounds/receipts convention — the things AGENTS.md
section 1 requires of every other LLM-written artifact. Entries also vary in
whether they carry gates, bounds, and receipt paths at all.

## The template (NEW entries only)

```markdown
## YYYY-MM-DD - <agent_id>: <task-id> <STATUS> - <one-line headline>

* author_llm: <name> (<version>) — the closing writer, verbatim from the claim file
* scope: every path the close-out touched, one line
* gates: each gate run and its exact measured result (exit code, counts)
* bounds: what was deliberately NOT done (no commit, no push, foreign files untouched)
* receipts: report path, pattern path, evidence paths
* assessor_llm: only if a later agent amends this entry (append-only, dated; per AGENTS 1a)
```

## Hard rules the template rides on

1. **NEW entries only.** DECISIONS is append-only; the 2026-10-01 truncation
   restoration (B5-1481) and the byte-integrity proposals (B5-1425/B5-1437/
   B5-1487) forbid in-place history re-creation. The template is never
   retro-applied to an existing entry — no recasting, no normalization pass.
2. **Byte-append discipline** (B5-1453): sample the file's dominant ending
   and append matching it; never rewrite the file to append (the
   B5-1425 Option-A BOM/EOL corruption finding).
3. **No shell-escape corruption** (B5-1499): entries naming shell tokens are
   written through a mechanism where backticks are literal (single-quoted
   heredoc / Python file handle), because a double-quoted here-string parses
   markdown backticks as escapes and writes stray 0x0D bytes.
4. **No pipes in entry text** where the content could ever be table-quoted
   (the B5-0435 class), and `author_llm` copied verbatim from the claim
   file's `agent_id` — never retyped (R7 one-transliteration rule).

## Worked example (EXAMPLE ONLY — not applied; the real B5-1714 entry on the
log is left byte-identical)

```markdown
## 2026-10-01 - Buffy (unknown) 1: B5-1714 BLOCKED - RUN_TESTS red at HEAD predates the edit

* author_llm: Buffy (unknown) 1 (unknown)
* scope: b5ccg/src/b5ccg/engine/StarterDeckBuilder.java only
* gates: sh b5ccg/compile.sh exit 0 (65 sources); RUN_TESTS=1 red at HEAD via
  the HEAD probe (par_c2 skipped; ClassCastException at testParticipation:507)
* bounds: work product left in tree labeled B5-1714; foreign MainWindow dirt
  untouched; no commit, no push
* receipts: .agent/REPORTS/2026-10-01-Buffy (unknown) 1-B5-1714.md;
  .agent/PATTERNS/Buffy (unknown) 1/2026-10-01-run-the-head-probe-before-attributing-a-red-gate.md
```

## Verification by application

The close-out entry this row appends to DECISIONS is written **in** this
template — that is the applied verification. No existing entry is modified.

## Adoption path

Autonomous promotion per AGENTS section 4 (claim, compile-green n/a for
docs, DECISIONS entry, claim release) or human APPROVE; until then this
remains a candidate and no entry is required to follow it.
