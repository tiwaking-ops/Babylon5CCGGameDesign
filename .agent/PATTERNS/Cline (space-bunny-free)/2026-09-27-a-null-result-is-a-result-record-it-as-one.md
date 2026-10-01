---
document:
  title: "A null result is a result — record it as one"
  status: "Pattern"
  provenance_note: "Advisory only, per AGENTS.md section 6. Never canonical; citing confers no authority."
provenance:
  author_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Cline (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A null result is a result — record it as one

**Reusable lesson (B5-0705, 2026-09-27).** When measurement shows a landed
feature *cannot* affect the thing it was expected to affect, say so plainly and
explain the mechanism. A hedged "may help" seeds a follow-up row that
re-derives the same null.

## The measurement that produced one

The question was whether the newly-landed computed Power seam could change the
20–20 endgame tie stall. The audit: `getPower()` call sites over **production**
`engine/` and `ai/`, tests excluded.

**One hit** — and it was the wrong one. `RulesEngine.java:922` is the B5-0667
*protection gate* (`getPower() >= getInfluence()`), not a victory path. Every
victory predicate reads `getInfluence()`.

So: no, it cannot. With zero POWER sources in either card set, Power and
Influence are equal by construction, and the seam is inert with respect to
victory.

## Why the null is more useful than a hedge

The null **ruled something out**. "Switch the victory predicates to
`getPower()`" was a plausible follow-up someone would otherwise have seeded —
and measurement showed it would be a **no-op refactor**, i.e. exactly the
"half-applied split" the B5-0667 design ruling warns against. Writing "cannot,
here is why" closed that row before it was written.

## The method worth keeping

1. **Exclude tests and probes.** The single hit was in production code, but
   scanning `Headless*` would have returned dozens of conformance hits and
   buried the one that mattered. A call-site audit that does not filter by file
   role answers a different question.
2. **Name the mechanism, not just the verdict.** "Power is inert for victory"
   is a conclusion. "`getPower()` is called once, at :922, in the protection
   gate, and no victory predicate calls it" is a conclusion someone can re-check
   in thirty seconds. The second one ages; the first one gets re-litigated.
3. **Report the conditional half too.** Surrender *does* break ties — but only
   when reachable, and at 20–20 it usually is not. A finding with a condition
   attached is more useful than either a bare yes or a bare no.

## Supersedes

Nothing. Fourth record in this namespace; see also
`2026-09-27-an-empty-census-is-not-evidence-of-an-empty-queue.md`,
`2026-09-27-the-code-outranks-the-summary-line.md`, and
`2026-09-27-measure-the-gap-by-searching-the-layer-you-are-allowed-to-touch.md`.
