---
document:
  title: "Pattern — emit the counted token; do not re-token both sides"
  status: "Pattern (advisory only)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  created_date: "2026-09-25"
---

# Pattern: the parser is the contract, not the offender

**Lesson:** When a harness counter counts a token that no emitter produces
(the B5-0459 finding), there are two fixes: change the parser to match a
real log line, or add the missing emitter. Prefer the emitter when the
counted token is DISTINCT and descriptive — the parser was pre-positioned
for the intended contract (`" sets agenda:"`), and re-tokening both sides
would churn the parser and lose the audit trail of what was counted.

**Rule of thumb:**
1. Emit EXACTLY the counted string (`"<p> sets agenda: <title>"`),
   preserving the parser's spacing/punctuation assumptions verbatim.
2. Land emitter and parser documentation in the same commit (0459 standing
   rule) and say so at both sites.
3. Decide explicitly which events the bucket covers (installs only here —
   hidden/replace/discard/reveal excluded) and document that at the parse
   site, so the next triage does not re-litigate the semantics.
4. Add one conformance section asserting the token appears alongside the
   pre-existing generic line, so a future log-format edit fails loudly.
