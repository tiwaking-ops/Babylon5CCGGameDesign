---
document:
  title: "An orphaned refactor row is a census row"
  task: "B5-0969"
  date: "2026-09-29"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 4", version: "glm-5.3-flash"}
---

# An orphaned refactor row is a census row

**One-line lesson:** when a row describes work whose target cannot exist — a
named file that is not on disk, a symbol that was never written — the row is
not a repair row; it is a census row, and its deliverable is the classified
proof of absence.

## Shape of the case

B5-0969 asked engine/ to be censused for every `ResolveConflict` and
`ConflictResolver` symbol to settle whether the B5-0953 described refactor had
a real target. It did not: no `ConflictResolution.java`, no `ConflictResolver`
type. The census classified all 22 hits (1 definition at RulesEngine.java:309,
1 production call at GameController.java:566, 17 harness call sites, 3 prose
mentions) plus 7 bare-`Resolve` aftermath-state hits, so the verdict is a
table, not an adjective — and the B5-0963 premise finding ("description does
not match the row") is now corroborated by evidence any later agent can
re-derive in one grep.

## What worked

- Run the census the row specified exactly: one grep per symbol family, every
  hit recorded with file, line, and definition-vs-call-site-vs-prose class.
- State the verdict as an answer to the row's own question ("does the refactor
  have a real target?"), not as a commentary on the dead claim.
- Keep the row read-only end to end; zero edits under `b5ccg/src` so no
  compile gate can be attributed to this task.

## Related records

- Companion to `B5-0963` (the B5-0953 claim-premise audit) and the B5-0999
  gate-release re-census this session: the audit doubted the premise, the
  re-census opened the gate, this census settled the question.
