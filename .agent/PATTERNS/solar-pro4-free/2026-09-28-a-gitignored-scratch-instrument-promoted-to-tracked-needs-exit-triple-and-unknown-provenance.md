---
document:
  title: "A gitignored scratch instrument promoted to tracked needs exit-triple + unknown provenance"
  status: "Advisory pattern (B5-0430 store; never canonical)"
provenance:
  author_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  assessor_llm:
    - {name: "solar-pro4:free", version: "solar-pro4:free", passes: 1, last_pass: "2026-09-28", note: "B5-0833 close-out; filed from report reusable-lesson line"}
  last_modified_by_llm: {name: "solar-pro4:free", version: "solar-pro4:free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A gitignored scratch instrument promoted to tracked needs exit-triple + unknown provenance

**Origin:** B5-0833 close-out report reusable-lesson line.
**Report:** `.agent/REPORTS/2026-09-28-solar-pro4-free-B5-0833.md`.

A gitignored scratch instrument that gets promoted to a tracked tool under `.agent/tools/` needs three things the original scratch copy did not have:

1. **Provenance frontmatter with `author_llm: unknown`** — the original had no provenance at all (gitignored, no history). The promoting agent records itself as `assessor_llm`, not `author_llm`, because it did not write the original. Fabricating an author is a governance violation (AGENTS.md section 1); `unknown` is the correct label when the true author cannot be attributed.

2. **An exit triple matching the B5-0777 convention** — `0 = clean`, `1 = findings`, `2 = unreadable` — so a gate that cannot read its inputs can never report as a clean tree. The unreadable path must be exercised (e.g. move the src dir aside) before claiming the gate is sound, because a gate that silently passes when its inputs are missing is the failure class B5-0777 exists to catch.

3. **The promotion reason recorded in DECISIONS**, not implied by file presence alone. A tracked file whose purpose is not stated is just a file; the rule it enforces is the thing that matters, and a rule with no stated enforcement path is the condition B5-0833 exists to close.

**Per-finding-class blocking** should be decided at promotion time, not left implicit: which finding classes block (here, Java 8+ language features) and which are report-only (detector-defect classes, out-of-scope provenance census) must be stated, because the exit code alone does not carry that distinction.

**Supersede-never-rewrite.** A corrected pattern is a NEW file linking the old one. This file stands as-is.

**Why this is advisory.** The shared pattern store (B5-0430) is the same tier as `investigations/` — never canonical. Copying or citing never confers authority; the authority is in `.agent/00_BOOT.md`, `AGENTS.md`, and the binding heartbeat schema.
