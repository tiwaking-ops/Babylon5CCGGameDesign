---
document:
  title: "File-level Java 6 grep must distinguish comment/string arrow literals from code constructs"
  status: "Advisory"
provenance:
  author_llm: {name: "solar-pro4 (solar-pro4:free)", version: "solar-pro4:free"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# File-level Java 6 grep must distinguish comment/string arrow literals from code constructs

**Context:** A file-level Java 6 construct grep over uncommitted source files
flags any `->` occurrence as a candidate forbidden construct. Two of the three
hits in B5-0789's three-file scope were not code constructs.

**Observation:** AIPlayer.java lines 550 and 571 each contain `->` inside a
Javadoc/plain-text comment describing score bands (`1..2 -> 3`, `3..5 -> 2`,
`1..2 -> 1.5, 3..4 -> 1.0, 5..7 -> 0.5, 8..10 -> 0.25, met -> 0`). MainWindow.java
line 1745 contains `->` inside a tooltip string literal describing a surrender
influence range. None of these is a lambda/arrow expression in code. A grep that
stops at the character match without classifying the match context would flag
these as false positives and could trigger an unjustified BLOCKED verdict.

**Lesson:** Before filing a `->` (or `::`, or `<>`, or any other construct-pattern)
hit as a forbidden-construct finding, read the matched line and classify whether
the token is code syntax or comment/tooltip prose. If it is comment or string
literal content, it is noise, not a violation, and the file-level Java-6-clean
verdict holds. Only code-level occurrences justify a defect report or a BLOCKED
mark. B5-0755 attributed those three files to DONE rows; B5-0789's file-level
grep confirms they remain Java-6-clean at the construct level, with the only
arrow occurrences being comment/string literals.

**Supersede-never-rewrite:** this is the first record of this lesson; no prior
pattern is being corrected.
