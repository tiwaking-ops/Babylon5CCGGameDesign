---
document:
  title: "A rule whose precondition cannot fire is a sizing task, not an implementation task"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A rule whose precondition cannot fire is a sizing task, not an implementation task

**Source task:** B5-0667 (Negative Power, B5-0639 remainder R15).
**Record:** 2026-09-27, Buffy (glm-5.3-flash).

## The lesson

Rulebook :1034 reads like a missing feature: "Any card that refers to only
counting influence as power cannot affect any player whose power is lower than
his influence." A triage row had already named it the largest blast radius of
any remainder, which invites an engine-wide Power implementation. But the
premise measured out differently: no Power field exists anywhere in the model,
every victory path reads `getInfluence()`, and zero of 829 card texts mention
power. Power == Influence engine-wide, so the rule's precondition — power
lower than influence — is *never satisfiable*. The rule is not unimplemented;
it is vacuous. Implementing it as seeded would have meant adding a whole
second stat architecture to serve a rule that cannot fire against a pool with
no card to trigger it.

## The transferable rule

When a triage or rulebook-derived task implies a missing subsystem, before
designing anything, prove the **precondition** — the state that would make the
rule's consequence observable. Three outcomes:

1. **Precondition reachable, state absent** → real implementation task.
2. **Precondition unreachable today** (this case) → vacuous rule: size it,
   record the equivalence it currently collapses as *known and correct*, file
   the seam for the day the precondition becomes reachable, and do not
   implement. Record it as NOT a bug.
3. **Precondition reachable but data-gated** → the R7 class (B5-0661 wave):
   seed a data-gate finding, not model work.

## Anti-patterns this heads off

- Implementing the architecture because a row called it "the largest blast
  radius" — blast radius without a trigger is zero blast.
- Recording a vacuous rule as a bug to fix: a half-applied split (Power read
  where the rulebook says influence) is worse than the honest equivalence.
- Trusting the triage's framing instead of re-measuring — the same wave that
  seeded R15 also seeded R7, whose grounding did not survive contact with the
  card data at all.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0667.md`
and `docs/proposals/negative-power-split-design-proposal.md`.
