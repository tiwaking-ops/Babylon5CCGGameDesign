---
document:
  title: "B5-1135 pattern — DECISIONS.md hygiene audit (B5-0966 criteria)"
  status: "Pattern"
provenance:
  author_llm: {name: "Solar Pro4", version: "solar-pro4"}
  assessor_llm: []
  last_modified_by_llm: {name: "Solar Pro4", version: "solar-pro4"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
references:
  - .agent/REPORTS/2026-09-30-solar-pro4-B5-1135.md
---

# Pattern — DECISIONS.md hygiene audit (B5-0966 criteria)

## When to apply

For any future DECISIONS.md audit task in the spirit of B5-1135 — i.e., sample
audit of recent entries against B5-0966 criteria:

1. **author_llm present**
2. **Append-only with no body edit**
3. **Interpretations recorded as interpretations, no rulebook-body change**

## The fast path

1. **Find the candidate entries.** `awk` over `docs/DECISIONS.md` filtering by
   `^## YYYY-MM-DD` heading and matching the target task-id pattern; report the
   line pointer of each match. A heading-style grep
   (`grep -nE "^## YYYY-MM-DD.*B5-TASK"`) is fast but misses bullet-style entries
   under multi-task sections (e.g., GitHub Copilot's `## 2026-09-30 — Copilot...`
   with `* B5-1087 DONE:` sub-bullets) — for those, search the bullet pattern
   too (`grep -nE "^\* B5-TASK\b"`).
2. **Diff evidence for append-only.** `git diff docs/DECISIONS.md` shows insertions
   vs deletions at line-grain. Pure-insert entries (`+`-only lines) are by
   construction append-only. Bulk deletions in the diff that look scary are
   almost always one of: (a) provenance-compaction of `assessor_llm` entries per
   the §1a rule, or (b) cp1252-as-UTF8 mojibake restoration noted by B5-0966.
   Neither is a body edit.
3. **Rulebook-body check.** `git status --short BABYLON5_CCG_RULEBOOK.md` —
   should be absent from the list. Any presence is an automatic FAIL for any
   entry whose scope could plausibly touch the rulebook.
4. **Author identification.** Heading agents supply author_llm in the `## YYYY-MM-DD
   - <Agent>: <title>` line. Bullet-style entries inherit author_llm from the
   parent `##` heading (Copilot convention). Inline `Agent:` lines reinforce
   provenance but are not required when the parent heading carries it.

## Common pitfalls

- **Missed bullet entries.** Searching only for `^##` headings misses bullet
  entries under multi-task sections. Always do a second pass with `^\* B5-TASK\b`.
- **Diff noise.** A 1100-line deletion count is alarming until you realise it's
  provenance/metadata repair. Read the diff sample, don't just trust the
  stat line.
- **Mistaking a heading agent for the bullet author.** The author is the
  parent `##` heading, not the bullet text. Don't credit the bullet for
  authorship.
- **Rulebook check only at the file level.** `git status --short` lists the
  rulebook if any working-tree change touches it; if it's missing, the audit
  is clean by construction.

## Reusable lesson (one-line, per B5-0430)

B5-0966 hygiene audits are fastest with three signals stacked:
`grep -nE "^##|^\\*"` for entries, `git diff --stat docs/DECISIONS.md` for
append-only vs body-edit, and `git status --short BABYLON5_CCG_RULEBOOK.md`
for the rulebook invariant — any reading time > 5 minutes indicates the
agent skipped the heading-vs-bullet pass.
