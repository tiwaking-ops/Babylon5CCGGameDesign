---
document:
  title: "Reusable lesson — rank a coverage backlog by play-reach, and let the seam decide"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-1125", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-30"
  last_modified_date: "2026-09-30"
---

# Rank a coverage backlog by play-reach, and let the seam decide

**Reusable lesson:** rank a coverage backlog by the copies that actually reach play,
not by the size of the corpus — and let "is there already a dispatch seam to extend"
outrank reach, because a gap with no seam is a missing feature, not a missing row.

B5-1125 ranked the 829-card effect-coverage backlog. The two obvious denominators
both produce the wrong order:

* **Corpus size** puts CHARACTER first (161 unregistered) — its printed abilities
  ("While Adira Tyree is in your Inner Circle, you may look at one opponent's hand")
  are persistent triggered abilities with no id-keyed dispatch surface at all.
* **In-play copies** fixes the denominator but not the verdict: CHARACTER still leads
  at 45 copies against CONFLICT's 29.

The factor that settles it is a third one nobody counts by default: does an id-keyed
table and a per-type dispatch site already exist? It is binary, and it split the
table exactly — the top four types all had a seam to extend, the bottom five had
none. A gap with no seam cannot be closed by adding a row; sending an agent there
means asking for a subsystem, whatever its play-frequency.

**The corollary the census hides.** A card being "unregistered" is not the same as
its effect being unlanded. All 13 unregistered in-play CONFLICT cards carry an
`influenceReward` field that `RulesEngine.resolveConflict` already pays generically,
so the "Winner gains N Influence" clause lands on every one of them. Stripping the
clauses that already fire left a residual clause on 12 of 13 — so the real slice was
12, not 13, and it was not the loser-penalty table the card names suggested.
**Count what is missing, never what is merely unregistered.**

**How to apply it.** Before ranking a coverage backlog, ask two questions per
candidate: *how many copies of this reach a player*, and *is there a seam whose next
entry is one line*. Then subtract the clauses that already fire by a generic path
before believing the residual count.

Related: `.agent/REPORTS/2026-09-30-Cline (space-bunny) b5-1125-B5-1125.md` and
`.agent/PATTERNS/Cline (space-bunny) b5-1109/2026-09-30-enumerate-the-call-sites-before-comparing-two-prices.md`
(the same instinct applied to a single comparison: find the path before comparing
the prices).
