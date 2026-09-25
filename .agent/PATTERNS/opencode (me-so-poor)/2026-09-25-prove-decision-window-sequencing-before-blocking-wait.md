---
document:
  title: "Prove decision-window sequencing before exposing a blocking wait"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Prove decision-window sequencing before exposing a blocking wait

A blocking engine wait changes the game from an unreachable control into a possible hard stop. Its engine and UI contracts must be treated as one design even when implementation tasks are serialized.

**Observed (B5-0432):** B5-0423 made Attack selectable but left it phase-gated. A controller-only wait after mandatory participation could offer a legal attack, but the existing Swing button could neither become enabled nor submit a pass, turning a dark control into a blocking window.

**How to apply:**

1. Put the new wait after every prior mandatory decision and before the authoritative resolver.
2. Delegate legality to the existing engine predicate instead of copying card, side, damage, or participation rules.
3. Test invalid, valid, pass, no-offer, interruption, sequencing, and side-preservation paths.
4. Treat `isWaitingFor...` as a public handoff contract, then require a UI companion before calling the feature reachable.
5. If a shared conformance file is claimed, use a dedicated scoped test file rather than writing through the claim.

**Reusable lesson:** a blocking decision window needs a complete observe-submit-decline path; engine tests must prove sequence and rejection behavior, and scope serialization must not turn a half-wired wait into a deadlock.

Source close-out: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0432.md`.
