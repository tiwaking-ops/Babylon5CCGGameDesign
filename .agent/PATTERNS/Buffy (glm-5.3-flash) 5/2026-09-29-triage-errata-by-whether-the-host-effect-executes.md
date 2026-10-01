---
document:
  title: "Triage errata by whether the host effect executes"
  status: "Pattern"
  task: "B5-1032"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# Triage errata by whether the host effect executes

**Measured 2026-09-29, Babylon 5 CCG, while triaging 150 annotated deluxe
errata (B5-1032).**

The row's premise described a 124-card wall of prose rules changes that
contradict the engine. The measured reality: 148 of 150 deluxe texts are
core-identical to premiere once the annotation is stripped, 10 cards are
engine-dispatched at all, and the real contradiction list is **7 cards sharing
one root cause** — their deluxe deltas are *additions* to dispatched effects,
and the dispatch tables were built from the core text. A "138-card problem"
became a 7-line work order by asking one question per card: **does anything
execute for this card's effect family?**

1. Executes + delta missing → WRONG (the deliverable; silent nerfs).
2. Executes + delta present (in code or structured fields) → RIGHT.
3. Silent for both editions → the pre-existing prose-effect gap, not a
   per-erratum defect; group it, don't multiply it.

The inverse lesson sits inside the same measurement: 11 annotations assert
bonus numbers their structured fields do not carry. Those cannot contradict the
engine *today* — but they are landmines under the effect families that would be
wired next, and any family implementation must re-check them first.

## Reusable lesson

Before triaging a pile of textual rules changes, intersect them with what the
engine actually executes: the contradictions live only on the dispatched
subset, the rest is documentation debt, and one shared root cause usually
explains the whole WRONG list.
