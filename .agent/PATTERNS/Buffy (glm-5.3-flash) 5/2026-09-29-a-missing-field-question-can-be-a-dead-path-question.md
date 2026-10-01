---
document:
  title: "A missing-field question can be a dead-path question"
  status: "Pattern"
  task: "B5-1036"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# A missing-field question can be a dead-path question

**Measured 2026-09-29, Babylon 5 CCG, while tracing the cost-free ENHANCEMENT
class (B5-1036).**

The row asked: what does an enhancement with **no cost** mean mechanically? The
trace found a sharper answer: cost means **nothing mechanically for any
enhancement** — the PLAY_CARD path never reads it. Two costed enhancements play
exactly as free as the one cost-free record; `canPlayCard` checks faction and
hand only; the influence charge lives solely in the character recruit/promote
branches. The two judgment_by_success records looked like a schema hole; they
are two innocent records sitting on a path where the whole field is cargo.

## The general diagnostic order

When a record lacks a field the schema "should" have:

1. Find every consumer of that field (loader, gates, executors — not just the
   loader).
2. If a consumer exists: the record's absence is the question (crash? default?
   exploit?) — answer at that read site.
3. If no consumer exists on the relevant path: the *present* values are cargo
   too, and the defect is the dead path, not the record. The smallest
   well-defined fix moves from data backfill to model gate.

## The trap this avoids

Backfilling the "missing" values (write cost=3 on two records) feels like
fixing the schema and changes nothing observable — the records stay inert, and
the field stays meaningless, with 72 more records now carrying numbers that
still do nothing. A data fix on a dead path is a placebo with a diff.

## Reusable lesson

Before calling a record a schema hole, enumerate the field's consumers: when
present values are consumed nowhere on the path that matters, the well-defined
fix is a model gate at the read site, not data backfill — a data fix on a dead
path is a placebo with a diff.
