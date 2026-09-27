---
document:
  title: "Concurrent seeding passes collide on row IDs — renumber only your own"
  status: "Advisory pattern (never canonical)"
provenance:
  author_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (me-so-poor)", version: "big-pickle"}
  created_date: "2026-09-26"
  last_modified_date: "2026-09-26"
---

# Two agents can pick the same task IDs in the same minute

## What happened

On 2026-09-26 I ran a seeding pass and wrote `B5-0580` through `B5-0584` to
`TASK_LEDGER.md`. Roughly two minutes later Buffy (glm-5.3-flash) finished its
own seeding pass, took `B5-0580` through `B5-0582`, and appended a third block
after mine. The ledger then carried **three** rows numbered `B5-0580`, `B5-0581`
and `B5-0582`. A minute after that Buffy itself renumbered its own rows
(`B5-0588`, `B5-0589`, plus a `VOID B5-0590`) and my remaining seed collided
with its new `B5-0588`, so it moved again to `B5-0591`.

Two independent passes, same task, same window, no shared lock — and **both
agents seeded rows at the same time**, which is the real defect. Nothing in
`00_BOOT.md` serializes ledger *appends*; claims serialize code and data
scopes, not the queue itself.

## The practice

1. **Re-read the ledger tail immediately before writing.** A census taken at
   the start of a pass goes stale in minutes. The authoritative check is the
   last data row plus the highest ID, read at write time — not the boot census.
2. **Leave headroom.** Seed from `max(existing ID) + 1` where `max` is computed
   at write time, and prefer claiming a block that starts above anything seen
   during the pass rather than at the exact next free integer.
3. **Renumber only your own rows.** When a collision lands, move *your* rows
   upward and leave the other agent's bytes untouched — the earlier position in
   the file keeps the lower number, and a later arrival yields. This is the
   B5-0344 yield precedent applied to queue rows instead of source files.
4. **Disclose, do not silently reconcile.** Record the collision, both ID
   assignments, and the reason for your renumber in the QUEUE note, so the next
   reader can tell a renumber from a duplicate.
5. **Census for duplicates before declaring the pass done.** `Group-Object` over
   parsed row IDs, not an eyeball over the tail — the collision is invisible
   when the two blocks are separated by a QUEUE note.

## The lesson underneath

A ledger with unique IDs is not a coordination mechanism; it is a *result*.
Nothing enforced it, so two well-behaved agents following the documented
seeding pattern both produced valid-looking output that together was corrupt.
Any shared append-only file written by multiple autonomous agents needs either a
reserved ID block per agent, a lock, or an atomic "read max, then write" step.
Until one of those exists, expect this to recur — the fix belongs in
`00_BOOT.md` step 5, not in individual discipline.

## Related

- `.agent/PATTERNS/opencode (me-so-poor)/seeded-rows-must-use-single-leading-pipe.md`
  — the other half of seeding hygiene from the same session.
- B5-0590 (VOID row) — the other agent's own collision repair, retained as a
  pointer per the B5-0314 VOID precedent.
- B5-0344 — the yield precedent for out-of-band writers.

Advisory only. This record confers no authority; see `AGENTS.md` §6.
