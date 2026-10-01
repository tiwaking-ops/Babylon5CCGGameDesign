---
document:
  title: "Run the loader before fixing it; records are not titles"
  status: "Pattern (advisory; B5-0430 store, same tier as investigations/)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  task: "B5-1043"
---

# Pattern: run the loader before fixing it

**Context.** B5-1043 demanded a loader fix so "58 deluxe-only records never
enter play" would load. The runtime probe through `DeckLoader` itself
measured deluxe-only = 0: every deluxe title has a premiere twin, the pool
of 446 equals the title union with zero duplicates. The premise was a
records-vs-titles miscount (829 records → 446 titles) wearing a fix-me
sign — the same de_-stripped artifact B5-1024 had already adjudicated.

**Lesson 1 — the loader is the authority; a static JSON regex is an
estimate.** My own first static census undercounted (441/381) because a
`{[^{}]*}` object split misses nested structures. The disagreement between
static estimate and runtime loader was itself the finding. When a row
claims the loader drops data, the probe goes through the loader.

**Lesson 2 — records ≠ titles.** A twin census over records counts pairs
twice; a pool census over titles counts cards once. Any "N records
unreachable" claim must first state which key (id? title? de_-stripped
title?) makes the class a class. Unreachable-by-title and
unreachable-by-record are different defects; only one of them was real
here, and it was already governed by B5-0320's pool rule with errata homes
filed by B5-1032.

**Lesson 3 — a gated chain on a refuted premise is five rows waiting on a
no-op.** B5-1043 gated B5-1045/1047/1051/1057/1059. Closing the no-op
promptly (verdict close, zero edits, the B5-0991/B5-0992 precedent) is not
skipping work — it is unblocking the chain with the measurement attached.

**Lesson 4 — a noise finding is a finding.** The loader's validation log
printed `UNKNOWN FIELD` for every standard field of every record during
every load — loud enough to drown any real warning. Flagged to its owners
(B5-1055/B5-1047), not fixed out of scope; a defect report that cannot be
acted on trains readers to ignore reports, and so does log spam.

Links: [B5-1043 report](../../REPORTS/2026-09-30-Freebuff%20(Buffy%20glm-5.3-flash)%201-B5-1043.md) ·
same artifact family as B5-1024's refutation; probe pattern per B5-1039-class scratch discipline.
