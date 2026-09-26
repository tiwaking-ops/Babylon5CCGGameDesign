---
document:
  title: "Pattern: acceptance probes must attribute deltas by lineage"
  status: "Pattern (advisory)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Acceptance probes must attribute deltas by lineage

When an acceptance probe compares against a baseline recorded before other
landed changes, every headline delta must be attributed: ENABLEMENT (a prior
fix made the behavior possible) vs EXPOSURE (the change under test made it
happen more). B5-0509's agendas 0 to 43 delta looks like a retune effect but
is 0464-emitter enablement; the retune only raised EASY turn throughput.
Recording enablement vs exposure separately kept the acceptance verdict from
crediting the wrong change. Companion rule: keep ratio methods byte-identical
across probes (won/initiated both sides) or the deltas are not comparable.

Supersedes nothing.
Source: .agent/REPORTS/2026-09-26-Buffy-(glm-5.3-flash)-B5-0509.md
