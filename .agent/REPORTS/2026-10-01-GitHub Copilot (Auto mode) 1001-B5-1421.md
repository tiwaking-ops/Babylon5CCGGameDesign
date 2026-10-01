---
author_llm: {name: "GitHub Copilot", version: "Auto mode"}
assessor_llm: []
created_date: "2026-10-01"
last_modified_by_llm: {name: "GitHub Copilot", version: "Auto mode"}
last_modified_date: "2026-10-01"
---

# B5-1421 — Java 6 forbidden-construct sweep

## Scope

Read-only census of the seven dirty source files named by the task:

* `b5ccg/src/b5ccg/ai/AIPlayer.java`
* `b5ccg/src/b5ccg/engine/DeckLoader.java`
* `b5ccg/src/b5ccg/engine/GameController.java`
* `b5ccg/src/b5ccg/engine/RulesEngine.java`
* `b5ccg/src/b5ccg/engine/CardEffects.java`
* `b5ccg/src/b5ccg/ui/MainWindow.java`
* `b5ccg/src/b5ccg/ui/GameBoardPanel.java`

## Result

The standing construct census checked `->`, `::`, standalone `stream()`,
`computeIfAbsent`, `@FunctionalInterface`, and `try (`. No executable
forbidden constructs were found.

The raw token matches were non-code text only:

* `AIPlayer.java:550` and `AIPlayer.java:571`: `->` in block-comment prose.
* `CardEffects.java:298`: the `try (` pattern matched the substring in
  `registry (` inside an explanatory comment; it is not a construct.
* `MainWindow.java:1772`: `->` in a user-facing string.

The initial broad search also matched `openStream()` and
`ByteArrayOutputStream`; those are ordinary Java 6 APIs, not `stream()`.
The boundary-aware standing census excluded those false positives, while the
`try (` hit was likewise classified by its surrounding comment text.

`b5ccg/compile.bat` passed with the expected Java 6 bootstrap warning and no
source edits were made.

Reusable lesson: a Java 6 token census must distinguish standalone `stream()`
from legal method names such as `openStream()` before turning grep output into
a defect.
