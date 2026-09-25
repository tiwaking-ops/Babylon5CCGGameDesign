---
document:
  title: "Pattern — honesty notes need a decision rule, not just history"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: reader-facing caveats must survive the defect's fix

**Lesson:** The guide's agendas note said "the aggregate is a parser
artifact" — correct until B5-0464 fixed the emitter, after which the note
would wrongly teach readers to distrust a now-honest number. Documentation
of a known-wrong output needs a dated boundary, not an eternal warning.

**Rule of thumb:**
1. When documenting a known-bad output, state the fix condition: values
   before change X are artifacts, values after are real.
2. When that fix lands, update the note in the same docs pass that
   updates dependent numbers (counts, baselines).
3. Guide refreshes should end by grepping their own stale numbers (old
   suite totals, old percentages) — every stale site found is a site
   the next reader would have trusted.
