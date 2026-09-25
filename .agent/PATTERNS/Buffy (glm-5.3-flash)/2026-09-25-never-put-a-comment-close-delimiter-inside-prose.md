---
document:
  title: "Pattern — never write a comment-close delimiter inside comment prose"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: shorthand like can*/execute terminates javadoc early

**Lesson:** Writing `can*/execute` inside a `/** ... */` javadoc embeds a
literal `*/`, which closes the comment at that point. Everything after it
compiles as raw Java — here producing five cascading `';' expected` /
`<identifier> expected` errors at the following method's modifiers, twice
in one task (B5-0460) because the same shorthand appeared in two comments.

**Rule of thumb:**
1. In comment prose, spell out wildcards: "legality predicate plus execute
   pair", never "can*/execute".
2. After ANY comment edit, compile immediately — delimiter bugs surface as
   nonsense errors several lines past the real cause (the error line is
   the code AFTER the swallowed comment tail, not the comment itself).
3. When two identical compile failures recur, grep the edited file for
   `*/` inside comment text before re-reading logic.
