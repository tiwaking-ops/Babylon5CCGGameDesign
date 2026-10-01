---
document:
  title: "Pin the attribute, don't silence the warning"
  status: "Pattern record (advisory only, never canonical)"
provenance:
  author_llm: {name: "Buffy (glm-5.3-flash) 2", version: "glm-5.3-flash"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0988"
---

# Pin the attribute, don't silence the warning

Traces to: B5-0988 (retiring the `compile.sh` CRLF conversion warning via a root
`.gitattributes` rule, per the B5-0974 recommendation).

A conversion warning like "LF will be replaced by CRLF the next time Git touches
it" is a *prediction of future damage* delivered at the moment of last safety.
Two wrong responses exist. Silencing the message (ignoring it, or reconfiguring
the tool so it stops talking) leaves the damage armed — the next checkout or
rebase still rewrites the file, just without telling anyone. Fixing only the
current bytes (renormalising the file once) leaves the next conversion equally
free to happen. The right response is the persistent one: an attribute that
constrains every future conversion of that path, chosen so the file's on-disk
form is pinned regardless of what `core.autocrlf` does on any host that clones
the repo.

The verification that makes the pin trustworthy is a four-probe battery plus one
negative control: the rule applies to the target (`check-attr`), the warning
disappears, the artifact still works (parse + build), and — the probe that
distinguishes a scoped fix from a lazy one — sibling files that legitimately want
the platform default still read `unspecified`.

**Rule:** when a tool warns it may damage a file later, add the persistent
constraint that makes the warning structurally impossible, then prove the
constraint is scoped (applies where needed, does not leak elsewhere) and that the
artifact survives it.

**Reusable lesson:** warnings are load-bearing predictions, and "make the warning
stop printing" is a different task from "make the predicted damage impossible" —
only the second one survives the next `git touch`.
