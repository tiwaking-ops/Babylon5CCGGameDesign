# .agent/PATTERNS/solar-pro4:free/ — reusable-lesson pattern store

*`author_llm: Solar Pro4 (solar-pro4:free)`*

## What

This directory holds reusable-lesson pattern records for the solar-pro4:free agent. Each close-out report gains a one-line "Reusable lesson" item, and the author files it here as a Markdown record.

## Convention

- **One file per lesson**, named `<date>-<agent-id>-<task-id>-<short-desc>.md`.
- **One line per lesson** (the "Reusable lesson" item from the close-out report).
- **Advisory tier only** — never canonical. Copying or citing a pattern never confers authority (same tier as `docs/proposals/`, `docs/reports/`, `investigations/`; see AGENTS.md section 3).
- **Per-agent write namespace** — solar-pro4:free writes only to this directory. Other agents write to their own `<agent-id>/` subdirectories.
- **Cross-agent read** — any agent may read any namespace.
- **Supersede-never-rewrite** — a corrected pattern is a NEW file that links the old one, not an overwrite.
- **Provenance** — every `.md` here MUST open with `author_llm: <name> (<version>)` frontmatter; assessor_llm entries are self-added only (never list another LLM as assessor unless it actually assessed that exact file).

## Boot-skim

Before claiming a task, glance at the newest records across all namespaces (one line each, advisory only). See `.agent/00_BOOT.md` step 10.

## Related

- AGENTS.md section 3 (statuses) — `.agent/PATTERNS/` listed as advisory tier.
- `.agent/00_BOOT.md` step 5 — shared-files protocol includes `.agent/PATTERNS/<agent-id>/` filing.
- `.agent/00_BOOT.md` step 10 — boot-skim of pattern store before claiming.
