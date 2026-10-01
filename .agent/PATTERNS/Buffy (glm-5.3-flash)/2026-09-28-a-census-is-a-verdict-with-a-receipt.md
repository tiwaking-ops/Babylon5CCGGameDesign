---
document:
  title: "A census is a verdict with a receipt"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash)", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# A census is a verdict with a receipt

**Task:** B5-0960 (Java 6 construct census, 2026-09-28).

**The trap.** "The build passes at `-source 6`, therefore the tree is Java 6" conflates
two different proofs. The gate proves *syntax* per compilation unit at compile time;
it proves nothing about API-level Java-8-isms (the classpath is still JDK 8, so
`Map.getOrDefault` would compile happily at `-source 6`), nothing per-file that a
later reader can cite, and nothing about the raw-vs-verified delta a token grep
carries inside it.

**What made this census more than a grep.** Three layers, each cheap:
1. **Mechanical masking** — blank the contents of `/*…*/`, `//`, `"…"`, `'…'`, then
   re-grep: whatever survives is code. This turns the comment/string classification
   from a per-hit judgement call into a repeatable transform.
2. **Hand-verification of every delta** — the mask said 47 arrow lines are prose;
   the report says so *and* shows the per-file distribution and spot-reads, so the
   next agent can re-verify a sample instead of re-deriving the whole claim. Raw
   count vs verified count, both printed, the difference named as the finding.
3. **The reverse-direction test** — the same token in the same codebase can be a
   project helper (`getOrDefault(map, key, def)` at DeckLoader.java:321), a real API
   call (`map.getOrDefault(key, def)`, 14 in the frozen archive), or prose. Only the
   qualified-call signature distinguishes them; the token alone files false positives
   in both directions.

**The cross-check that validated the instrument.** The frozen Java 8 archive ran
through the identical mask and produced the *opposite* verdict on the same constructs
(arrow 14 code lines, `::` 9, `.stream(` 8, qualified `getOrDefault` 14) — an
instrument that only ever says "clean" proves nothing about itself. Run the tool on
ground you already know is dirty before you trust its clean verdict.

**And when the census is clean, say so with the expectation, not the assumption:**
"expected clean" is only evidence once it is recorded per file with the method that
would have caught the dirt if it were there.
