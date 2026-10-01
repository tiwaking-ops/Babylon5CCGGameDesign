---
document:
  title: "Check the twin's structured fields before calling text a rules change"
  status: "Pattern"
  task: "B5-1021"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 5", version: "glm-5.3-flash"}
  last_modified_date: "2026-09-29"
---

# Check the twin's structured fields before calling text a rules change

**Measured 2026-09-29, Babylon 5 CCG, while adjudicating the four unannotated
deluxe text divergences (B5-1021).**

`de_enh_prophecy`'s deluxe text adds "That character also gains +1 Psi." against
its premiere twin — a silent, unannotated bonus sentence, prima facie the worst
class of divergence (a power creep with no provenance). One field lookup kills
the alarm: `psiBonus` reads **1 in both editions**. The deluxe prose is a
clarification that catches up with data that never changed; the engine has
implemented the +1 identically in both editions all along. Of the four
suspicious cards, one resolved to *deliberate and mechanically neutral* for the
cost of one diff.

## The general rule

Card text is the rules surface, but it is not the only surface. When a text
divergence asserts a quantity, look for a structured field carrying that
quantity and diff *that* too:

1. Text says a bonus/malus changed → diff the bonus fields.
2. Text says timing changed → look for a structured timing/participation key.
3. Structured fields identical → the divergence is presentational (annotation
   debt, clarification, transcription drift), not a rules change.
4. Structured fields differ too → the divergence is real twice over, and the
   stat row (B5-1022's territory) carries it.

## The remaining asymmetry

The reverse case is more dangerous: text *identical* while structured fields
differ (the latent-telepath class — annotated text change, psiBonus 2→3, where
the engine reading structured fields implements premiere while the player holds
deluxe). Text diffs are visible to every census; stat diffs need the field join.
Any twin census that reads only one surface is blind by construction.

## Reusable lesson

Before adjudicating a prose divergence as a design change, diff the twin's
structured fields: when the data already encodes the "new" behaviour in both
editions, the text difference is clarification, not rules — and when the data
diverges silently, the census reading only text is the blind instrument.
