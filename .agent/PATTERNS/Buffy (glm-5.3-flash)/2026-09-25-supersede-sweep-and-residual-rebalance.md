---
document:
  title: "Pattern — supersede sweep + residual rebalance when a caveat's fix lands"
  status: "Pattern record (advisory, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: superseding a documented caveat is a sweep plus a rebalance

Filed under the standing "Reusable lesson" convention (00_BOOT step 10).
First applied in `.agent/REPORTS/2026-09-25-Buffy-(glm-5.3-flash)-B5-0434.md`.
Related: `2026-09-25-gated-docs-refresh-propagates-audits.md` (part 4:
propagate an audit's corrections). This record covers the *supersede*
direction: a previously documented caveat whose fix has since landed.

## The lessons

1. **Caveats propagate — hunt every copy.** The B5-0414 P0 caveat lived in a
   §4 blockquote AND resurfaced in a §7 gap bullet, each with different
   wording. Grep the whole document for several distinct phrasings of the old
   claim (here: `pending B5-0423`, `stay dark because their`,
   `unreachable in real play`) and require zero matches after the edit.
   Replacing only the source block leaves stale falsehoods in prose the
   reader trusts.

2. **A fix's report mints new residuals — reanchor them, don't drop them.**
   B5-0423 fixed the P0 but logged P3 residuals (attack window engine-gated;
   bid handler reads `offers.get(0)`). A supersede pass that only erases the
   old caveat would silently lose the fix's own honest leftovers. Read the
   fix's close-out report and (a) add its new residuals to the open-gaps
   inventory with their latent/live status, (b) rewrite the still-true parts
   of the old caveat (the AI-vs-AI engine-loop gap survived the fix and must
   remain).

3. **Say what landed, keep what stayed broken.** The replacement text should
   name the landed mechanism (shared selection handler, engine-predicate
   enablement, hit-test/paint geometry parity) so the reader knows why the
   caveat is gone, and carry forward the remaining gap with its task link so
   the guide stays the single honest status surface.

## Procedure

1. Read the fix task's close-out report; list its landed changes AND its
   logged residuals.
2. Draft the supersede text: landed mechanism + surviving gap with task refs.
3. Grep the target doc for at least three phrasings of the old claim; replace
   every occurrence; re-grep for zero stale matches.
4. Update the open-gaps inventory: new residuals in, obsolete ones out,
   still-true ones kept and re-anchored.
5. Append self `assessor_llm`; never overwrite `author_llm`.
