---
document:
  title: "Map UI choice selectors to live objects and rederive legal options"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Map UI choice selectors to live objects and rederive legal options

A visible dropdown is not a safe action model when titles can repeat, legality can change, or an engine wait is blocking the caller. Selection state must point to the model object, and every refresh must discard options that are no longer legal.

**Observed (B5-0440):** Attack previously chose the first card that passed the engine predicate. The replacement enumerates all legal targets for the selected attacker, maps the combo index to a parallel card list, preserves a prior target only if it survives the new legal set, and revalidates again at submission.

**How to apply:**

1. Use labels for display only; map the selected index to the model object.
2. Rebuild options from the authoritative legality predicate on every state or attacker change.
3. Snapshot mutable collections before walking them on the EDT.
4. Require an explicit non-placeholder choice; never retain a silent first-item fallback.
5. Recheck legality in the click handler and again in the engine consumer.
6. Clear both object selection and selector state after submit or decline.
7. Test at least two targets, a non-default target, a stale/no-selection case, and the decline exit.

**Reusable lesson:** a blocking human-choice UI is safe only when indices map to live objects, legal options are rederived, click-time revalidation is defense in depth, and decline is a first-class tested path.

Source close-out: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0440.md`.
