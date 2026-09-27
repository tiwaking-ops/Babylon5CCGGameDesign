---
document:
  title: "Agent_id spelling is a namespace identity"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# The colon in "solar-pro4:free" is why everything fragments

## What happened

B5-0584 censused every agent_id occurrence across CLAIMS, HEARTBEATS, REPORTS,
PATTERNS and ledger claim cells. The single most fragmented agent is also the
single most active one: `solar-pro4:free` appears as at least six raw spellings
across the six locations, and the **same agent's own seeds** disagreed about
the mangled codepoint (seed premise: U+2028; evidence on disk: U+F03A).

Root cause is mechanical, not stylistic: **Windows forbids `:` in a filename
segment.** The logical agent_id works in JSON and ledger cells, but every
filesystem namespace — heartbeat filename, report filename, pattern namespace —
must transliterate it, and no one ever defined how. The result:

- `solar-pro4:free` (claims, ledger cells, some frontmatter)
- `solar-pro4-free` (51 report filenames — a hand-made transliteration)
- `solar-pro4` (27 report filenames, 1 heartbeat filename)
- `solar-pro4<U+F03A>free` (13 report filenames, 1 heartbeat filename, the
  PATTERNS namespace — the mangle a tool produced when told to write `:`)

A liveness scan that joins `agent_id` to heartbeat filenames with an exact
string sees **none** of the agent's own heartbeats. A pattern-store reader
looking up `.agent/PATTERNS/solar-pro4:free/` finds nothing, because that
directory cannot exist.

Buffy fragments differently (four version strings across loops), and the
`opencode (me-so-poor)` / `me-so-poor` / `big-pickle` / `poolside-s-01` family
shows that even a single agent can drift across sessions. Every location has a
different spelling for nearly every agent.

## The practice

1. **Separate the logical agent_id from its filesystem transliteration.** JSON
   `agent_id` and ledger claim cells keep the canonical id verbatim; filename
   and directory namespaces use one defined transliteration (`:` -> `-`, no PUA
   codepoints). Name both in the canonical-spelling record.
2. **Never write a raw PUA/managed character.** It is the signature of a tool
   that tried to create an illegal filename and silently mangled it. If a
   namespace creation would produce one, transliterate explicitly instead.
3. **Normalise both sides before any join, or report UNKNOWN.** A join keyed on
   exact casing/punctuation is a liveness blind spot, and per B5-0597 an absent
   signal must be UNKNOWN, not "live".
4. **Verify codepoints, don't trust seed prose.** Two seeded rows asserted
   U+2028; the bytes were U+F03A. When a report cites an exotic character,
   print the codepoints before repeating the claim.

## Related

- `.agent/PATTERNS/opencode (me-so-poor)/claim-mtime-is-not-a-liveness-signal.md`
- `.agent/PATTERNS/opencode (me-so-poor)/prose-in-a-json-field-is-not-a-field.md`
- B5-0584 report; B5-0597 (liveness protocol, must normalise the join);
  B5-0598 (cross-reference audit, second independent measurement);
  B5-0609 (read-only query tool).

Advisory only. This record confers no authority; see `AGENTS.md` §6.