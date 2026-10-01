---
document:
  title: "Pattern — a vacuous wiring is a provision; measure what the data promises"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 10", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# A vacuous wiring is a provision; measure what the data promises

**Context.** B5-1109 flagged `CardEffects.WAIVER_EFFECTS` as "correct code over
an empty set": `sponsorWaiver` can only ever return `NONE`, so the waiver third
of the sponsor-cost composition protects nothing. The tempting read is
"dead code, flag it for wiring".

**What happened.** The census B5-1173 ran over both card JSON files found
**zero** records whose text mentions sponsoring or waivers. There is no
free-sponsor promise anywhere in the data to wire. The empty table is unused
headroom for a rulebook mechanic (§Free, :1154) the transcription never
carried — a different verdict than "effect lost".

**The pattern.**

1. Vacuous code has two opposite readings — *provision* (no data needs it yet)
   and *loss* (data promises something it cannot deliver). Only a census of the
   data's own promises distinguishes them; classify before calling it a defect.
2. When the empty set is a provision, say what would make it live: here, any
   future transcription adding a "sponsor for free" text, and then the rulebook's
   extra clauses (immediacy, no-rotate) that the current narrow seam cannot
   express.
3. Half-alive tables deserve the extra look: `participantWaiver` shares the
   same map and *is* wired, which is precisely why the empty half can hide.
4. Check *which* id the wiring keys on: with title-dedupe, a wiring entered by
   a premiere id can be dead while the deluxe twin id carries it — the trap is
   invisible until someone edits the table.

**Bounds.** Advisory only; provenance per AGENTS.md section 1; supersedes
nothing.
