---
document:
  title: "Census executable defaults before changing a configuration recommendation"
  status: "Pattern (advisory; never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-25"
  last_modified_date: "2026-09-25"
---

# Census executable defaults before changing a configuration recommendation

A recommendation can correctly identify a cheap configuration edit while assuming a default that does not exist. The cheapest edit is still the wrong edit when the current tree already satisfies its stated goal.

**Observed (B5-0431):** B5-0422 Option B proposed replacing an assumed all-EASY default. Current production and all three full-game harnesses already use mixed tiers with exactly one EASY seat. Isolated contract fixtures also instantiate EASY, but they are tier subjects rather than default seats.

**How to apply:**

1. Enumerate every executable default and its real call site before estimating the change.
2. Distinguish production defaults, full-game harnesses, and isolated tier-under-test fixtures; a repository-wide search without that classification creates false candidates.
3. Check whether a claimed user-selectable override has an actual argument parser or constructor path.
4. Treat changes that remove the last representative of a behavior as a new metrics or coverage policy, even when the literal edit is small.
5. Restate the recommendation as actionable or dead from current evidence. A historically filed proposal is not authority over the present tree.

**Reusable lesson:** census executable defaults and explicit-constructor call sites before acting on a seat-mix recommendation; valid mixed baselines can coexist with proposals that describe a configuration that never existed.

Source close-out: `.agent/REPORTS/2026-09-25-opencode (me-so-poor)-B5-0431.md`.
