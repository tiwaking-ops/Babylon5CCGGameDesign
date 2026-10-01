---
document:
  title: "Pattern — a gate that exists and is silent is a name for somebody else's silence"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 8", version: "glm-5.3-flash"}
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash) 8", version: "glm-5.3-flash"}
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# A gate that exists and is silent is a name for somebody else's silence

**Context.** B5-1121 asked whether a player shown a legal sponsor move can be
refused at the moment they commit — specifically whether the sponsor discount,
which the row feared a gate "exists and is silent about", could strand a
character. B5-1109 had already established the call-site fact: `canPlayCard`
(the gate the row worried about) has zero production callers.

**What happened.** The tempting close was to inherit the predecessor verdict
and write one paragraph. But the row's question is *player-facing*, and a
player-facing answer has more seams than a call-site census: the card's
highlight in hand (`HandPanel.cardPlayable`), the button that fires the action
(Sponsor, re-checking `canRecruit` at click), the engine re-check before the
charge, and the other button that could fire instead ("Play Card", whose gate
reads no cost at all). Each was verified against the current tree, not the
report.

**The finding.** All of those seams read one cost notion — `sponsorCost` — at
both moments, except "Play Card", which is silent in *both* directions and so
cannot strand anything. The feared stranding class does not exist on this path.

**The pattern.**

1. When a row asks about a *shown* move, enumerate the affordances that show
   and fire it — not just the engine predicate. A gate that "exists" may be on
   a path the player is never shown (B5-1109's dead `canPlayCard`), and the
   affordance the player is shown may have no gate at all.
2. Verify predecessor verdicts at the moment of action: re-read each cited seam
   in the current tree before consuming a DONE row's conclusion.
3. A button whose gate omits cost entirely is *not* a stranding hazard — silence
   in both directions cannot contradict the charge. The hazard is a *cost read
   that disagrees*, or an *engine path that bypasses the gate the UI used*.

**Bounds.** Advisory only; provenance per AGENTS.md section 1; supersedes
nothing.
