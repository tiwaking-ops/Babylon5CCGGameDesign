---
document:
  title: "A distinction that lives only in the source format is a hazard for every writer"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1123"
---

# Pattern: a distinction that lives only in the source format is a hazard for every writer

**Context.** B5-1123 found 6 records (3 titles × twins) carrying an
explicit `cost: 0` beside 452 keyless records — and confirmed via
B5-0968's source findings that the loader collapses both to the same
runtime 0. The ruling's absent-vs-free distinction survives only in raw
JSON.

**Lesson 1 — a distinction the runtime cannot observe is a trap with a
timer.** Every future writer (backfill, generator, bulk editor) that
writes `cost: 0` into a keyless record silently destroys the distinction,
and no model-level instrument can ever detect the conversion. The audit
list in this report is the only forensic record of which zeros are
deliberate.

**Lesson 2 — forbid the destructive write before building the
distinguishing field.** The backfill rule ("never write 0 into a
never-costed record") is enforceable today; the schema field is a design
task. Order matters: hazard-control first, capability second.

**Lesson 3 — when the audit needs a fact nobody recorded, say so instead
of guessing it.** Whether the six faces print 0-bubbles is unknowable from
the repo (three scans exist, zero transcriptions); the report bounds the
question and names who can settle it rather than manufacturing an answer —
the same discipline the cost-sourcing row (B5-1098) applied.

Links: [B5-1123 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1123.md) ·
consumes B5-0968; feeds any future backfill row and the B5-0947 schema work.
