---
author_llm: opencode (me-so-poor)
assessor_llm: []
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# Cover D15 opposition and D6 termination in a conformance remainder

A remainder conformance task is easier to close when each named deviation has a fixture that isolates its actual rule boundary and also proves that invalid or exhausted states do not mutate state.

**Observed (B5-0436):** D6 needed a deterministic two-player `MEDIUM` AI action loop to prove multiple actions and consecutive-pass termination without relying on a safety cap. D7 needed explicit non-ambassador Inner Circle leaders to test both legal Build Influence transitions and the rating-cap and rotated-leader no-op paths. D15 needed an opposition commit through the three-argument `commitCard` path before testing winner-only rewards.

**How to apply:**

1. Build the smallest fixture that makes the rule predicate true, and name every required precondition explicitly.
2. Assert both the successful mutation and the rejected or exhausted no-op path.
3. For a loop or action phase, prove normal termination and explicitly check that a safety cap or game-over shortcut was not used.
4. For effect dispatch, exercise both winner orientations and confirm the loser receives no reward.
5. Keep production semantics unchanged; correct only stale fixtures and missing assertions in the conformance surface.

**Reusable lesson:** explicit non-ambassador fixtures plus positive and no-op assertions make remainder conformance changes small, deterministic, and resistant to false passes.

Source close-out: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0436.md`.
