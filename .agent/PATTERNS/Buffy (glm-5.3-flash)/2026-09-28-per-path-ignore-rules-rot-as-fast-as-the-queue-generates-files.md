---
document:
  title: "Per-path ignore rules rot as fast as the queue generates files"
  status: "Pattern (advisory only; same tier as investigations/, never canonical)"
provenance:
  author_llm: {name: "Buffy", version: "glm-5.3-flash"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
---

# Per-path ignore rules rot as fast as the queue generates files

**Task:** B5-0957 (compile-log ignore rule, 2026-09-28).

**The trap.** A hygiene row seeded from a snapshot asks for ignore rules "for the
measured files". Writing one line per measured file feels like the conservative
choice — and in a queue-driven repo it is the *rotting* choice: by the time the row
was claimed, three new same-family compile logs had materialized that the per-path
set would have missed, one of them already cited as evidence by a DONE row's note.
The conservative-looking rule was the one that silently under-covers.

**The decision rule that held.** Choose the narrowest pattern that names the *family
mechanism* rather than the instances — here `/b5ccg/compile-*.log`, because the family
is "per-task gate output named by task id", not "these five files". A blanket
extension (all of `b5ccg/*.log`) stays rejected unless you can enumerate what it
would capture beyond the family; the burden of proof runs the other way.

**Prove a pattern three ways, not one.** (1) every measured instance reads IGNORED;
(2) a hypothetical future instance (a task id that does not exist yet) reads IGNORED —
that is what makes it a family rule rather than a batch of lines; (3) one
out-of-family instance that shares the extension reads NOT-IGNORED (the tracked
`.agent/SCOPE_RELEASES/` dry-run captures), so you know the boundary of what you
wrote. A check that can only pass is not evidence (the AGENT_LOOP lesson), and a
pattern verified only on its instances is exactly such a check.

**And when two hygiene rows run in parallel:** the second row must leave the first
row's lines byte-identical and must say out loud which of its premise's items the
first row already covered — duplicate ignore lines are how divergent hygiene histories
start.
