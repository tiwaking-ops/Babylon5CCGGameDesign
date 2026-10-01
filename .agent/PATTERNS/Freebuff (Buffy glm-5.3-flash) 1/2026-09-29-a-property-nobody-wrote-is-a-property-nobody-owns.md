---
document:
  title: "A property nobody wrote is a property nobody owns"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Freebuff (Buffy glm-5.3-flash) 1", version: "glm-5.3-flash"}
  created_date: "2026-09-29"
  last_modified_date: "2026-09-29"
  task: "B5-1026"
---

# A property nobody wrote is a property nobody owns

Traces to: B5-1026 (RARE_WITHDRAWN singleton verification).

**One-line lesson:** when asked "is X excluded from play", the answer is not
yes/no but *which line owns the exclusion* — if the answer is "a rarity
whitelist written for another purpose plus a data absence", the property is
real today and unowned forever, and any refactor or data addition breaks it
silently.

## The shape

- Three exclusion candidates, three different owners: the random-draw path
  excludes via a whitelist keyed to *rarity economics* (UNCOMMON/RARE), the
  fixed-list path excludes via a *data absence* (no deck entry), and no path
  excludes via the withdrawal semantic the data actually declares. The test
  "does the withdrawn card appear in play?" passes on all three paths today
  and measures nothing — the meaningful test is mutation-shaped: what is the
  smallest change (mark a card RARE_WITHDRAWN on an UNCOMMON-bodied card, add
  one deck-list entry) that silently reverses it?
- The rulebook cross-check mattered independently: zero mentions of
  withdrawal means no canonical sentence a future implementer could cite, so
  the exclusion-by-accident would not even be *noticed* as a violation —
  recording the divergence in DECISIONS is what gives the future explicit
  exclusion something to point at.
- Distinguish "ambiguous about its own status" from "ambiguous in the code":
  the record is fully unambiguous (rarity + literal text annotation agree),
  the *code's* treatment is what is implicit.

## What worked

- Trace every construction path that could admit the card, not just the first
  one that excludes it — one incidental exclusion would have read as a rule.
- Check whether any line reads the discriminating field at all
  (`getRarity()` outside StarterDeckBuilder: colour and parse only).
- Split the deliverable: engine facts to the report, canonical-text silence to
  DECISIONS as a divergence of record, nothing edited.

**Reusable lesson:** verify a safety property by finding its owner — a
property with no owner is a latent defect wearing today's passing test.
