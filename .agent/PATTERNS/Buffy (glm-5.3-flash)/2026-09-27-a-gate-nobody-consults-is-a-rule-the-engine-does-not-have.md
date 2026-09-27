---
document:
  title: "A gate nobody consults is a rule the engine does not have"
  status: "Pattern (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  last_modified_by_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A gate nobody consults is a rule the engine does not have

**Source task:** B5-0673 (legal-targets gate census). **Record:** 2026-09-27,
Buffy (glm-5.3-flash).

## The lesson

`RulesEngine.canPlayCard` enforces in-hand + faction-playability — the
rulebook :823 legal-target principle in its simplest form — and has zero
production callers: the controller's PLAY_CARD case delegates straight to the
apply path, trusting whoever built the action. The rule reads as enforced in
the code and is not enforced by the engine. This is the exact class B5-0302
fixed for INITIATE_CONFLICT ("was dead code — zero callers"), still alive on a
different action. The census also caught the *opposite* error in the same
sweep: `canUseAssistant` looks dead by direct callers but is live through
delegation (`canUseRotateEffect` → it), so a naive zero-caller sweep would
have filed one true positive and one false positive.

## The transferable rule

When auditing gate coverage, classify every gate on **two independent axes**:

1. **Consulted?** — grep the *real* action path (the controller's processAction
   cases), not just any caller; AI-offer and ui calls are a weaker guarantee
   than an engine-side consult, and a delegating neighbour can make a
   zero-direct-caller gate live.
2. **Asserted?** — grep `rules.<gate>(` sites in the suite, not prose.

Cross the two before judging: consulted+unasserted = coverage gap;
unconsulted+correct = dead gate (the worse finding — the code suggests the
rule exists); unconsulted+wrong = a latent hole. And never infer coverage
from narrative fields — the B5-0609 defect class applies to suite comments
and report prose exactly as it does to heartbeats.

## Anti-patterns this heads off

- "The gate function exists, so the rule is enforced" — existence without
  consultation is decoration with a test-friendly signature.
- Filing a delegated gate as dead — the false positive costs a repair task
  and credibility; the true positive without the false one costs only a fix.
- Assuming AI-path legality proves engine-path legality — the AI builder is
  one caller, not the contract.

**Filed alongside:** `.agent/REPORTS/2026-09-27-Buffy-(glm-5.3-flash)-B5-0673.md`.
