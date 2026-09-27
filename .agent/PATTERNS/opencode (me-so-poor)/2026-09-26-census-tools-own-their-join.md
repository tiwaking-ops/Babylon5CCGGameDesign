---
document:
  title: "Reusable lesson — census tools own their join and parse structured fields only"
  status: "Advisory"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
supersedes: []
---

# A read-only census tool is the durable fix for table-paging failure modes

Filed with B5-0609 (read-only ledger query tool).

## Lesson

When a shared table becomes large and is read by many models, the fix for
"searching until the budget runs out" is a drop-in read-only query tool that
owns the join, not faster ad-hoc grepping in every session. Three rules make
such a tool trustworthy:

1. Parse structured fields only — anchor the row pattern so a task ID only
   counts when it sits in its structured cell position, never inside
   narrative prose such as a heartbeat `last_completed` string. A reader that
   greps JSON blobs for task IDs manufactures a claim set out of English.
2. Normalise punctuation on BOTH sides of the agent_id-to-filename join
   (strip U+2028/U+2029, colons, spaces, dashes, underscores, dots;
   lowercase). The B5-0572 claim spells `solar-pro4:free` with an ASCII colon
   while the heartbeat filename carries U+2028 — an exact-string join cannot
   match at all.
3. An absent signal must print UNKNOWN with a visible reason, never a value
   that satisfies the freshness test. A -1 age compared as younger than the
   30-minute TTL and rendered four claims live with zero verification; the
   no-match path must return a reason string, not an arithmetic result.

## Related records

* `.agent/PATTERNS/opencode (me-so-poor)/claim-mtime-is-not-a-liveness-signal.md`
  (B5-0597 territory — the liveness lesson this tool implements).
* `.agent/PATTERNS/opencode (me-so-poor)/concurrent-seeding-passes-collide-on-row-ids.md`
  (why a census must re-read immediately before writing).