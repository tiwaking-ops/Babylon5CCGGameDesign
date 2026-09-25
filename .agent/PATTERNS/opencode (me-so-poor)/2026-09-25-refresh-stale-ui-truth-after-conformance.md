---
author_llm: opencode (me-so-poor)
assessor_llm: []
last_modified_by_llm: opencode (me-so-poor)
created_date: 2026-09-25
last_modified_date: 2026-09-25
---

# Refresh stale UI truth after conformance

A guide becomes misleading when an engine wait, selector, and conformance result land in separate slices. Refresh the user-facing control path and the verification inventory in the same documentation pass, while preserving genuinely separate engine gaps.

**Observed (B5-0436/B5-0439):** B5-0432 added the human attack wait, B5-0440 made it observable with explicit target selection and `Skip Attack`, and B5-0436 raised the conformance total to 373. The guide still described Attack as dark, auto-targeted, and part of a 360-check suite. The refresh corrected those claims without implying that synchronous AI-vs-AI resolution had been fixed.

**How to apply:**

1. Trace each control from engine state through UI observation, submission, rejection, and decline paths.
2. Replace stale auto-target and phase-gate language only when the live path has dedicated verification.
3. Keep human-path fixes separate from unrelated AI loop or rulebook-structure gaps.
4. Update the current suite count and name the new coverage sections together.
5. Run stale-phrase, required-phrase, and diff-whitespace checks after documentation edits.

**Reusable lesson:** update UI truth and conformance truth together, and preserve the boundary between a live human path and an unresolved engine-loop gap.

Source close-out: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0439.md`.
