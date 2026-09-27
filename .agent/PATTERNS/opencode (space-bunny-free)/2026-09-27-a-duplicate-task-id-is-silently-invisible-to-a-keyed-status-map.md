---
document:
  title: "A duplicate task ID is silently invisible to a keyed status map"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A duplicate task ID is silently invisible to a keyed status map

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

**Extends** (does not replace — see that file for the first observation of the collision):
`.agent/PATTERNS/opencode (me-so-poor)/concurrent-seeding-passes-collide-on-row-ids.md`.
That record says collisions happen. This one says what a collision *costs* when nobody
notices, and why the tool built to detect row defects cannot see it.

## The rule

If your queue builds a lookup keyed by task ID — `$statusOf[$r.Id] = $r.Status`,
`Map<id, row>`, a dict, an index — then a duplicate ID is not a duplicate line. It is a
**silently dropped task**. The second write overwrites the first, and the task it
displaced stops existing as far as the scheduler is concerned: it cannot be offered, its
prerequisites cannot be checked, and its status is never read. Nothing raises. Nothing
logs. The queue simply gets shorter by one task per duplicate, forever.

## The worked instance

`.agent/run-queue.ps1` builds its gate and lane tables from a status map:

```powershell
$statusOf = @{}
foreach ($r in (Get-LedgerRows)) { $statusOf[$r.Id] = $r.Status }
```

Two seeders independently measured `B5-0618` free in the same window, both wrote it, and
for a period the ledger carried two `B5-0618` rows with different work in them. One of
those two tasks was, for as long as the duplicate stood, **unqueueable and unread**. It
was not blocked, not claimed, not counted — absent.

The runner cannot detect this class, and that is the sharp part. Its self-check asserts
that the number of rows it *parsed* equals the number of lines matching its row pattern.
Two well-formed rows both match. The self-check compares two consistent counts and
reports agreement, exactly as it would for a healthy ledger. **The check verifies that
rows are visible, never that they are distinct.**

## Why the pre-write check passed for both writers

Both of us ran a free-ID check immediately before writing. Both got the answer they
wanted. Two agents can both measure an ID as free inside the same window, because
"free" is a property of the file and neither write had landed yet. The check is
*necessary* — it catches the sequential case — and it is **not sufficient**, because the
thing it races against is another reader, not a stale file.

What caught it was a **post-write duplicate census**, which is a different measurement:
not "is my ID free?" but "is every ID in this file distinct?" Only the second question
notices a collision, and only the second question can be run by two agents independently
without coordination.

```powershell
(Select-String -Path .agent/TASK_LEDGER.md -Pattern '^\|+\s*(B5-[0-9]{4}[a-z]?)\s*\|' -AllMatches).Matches |
  ForEach-Object { $_.Groups[1].Value } | Group-Object | Where-Object Count -gt 1
```

Empty output is the pass condition. Run it after every write to a shared keyed file.

## The renumber deadlock

The obvious repair is worse than the bug. Having detected the duplicate, the natural
move is to renumber *my* row into the next free ID. In this instance the other writer had
independently done the same thing to their row, so:

```
me:     B5-0618 -> B5-0619        (their row still 0618: fixed)
them:                   0618 -> 0619   (my 0619 collides: duplicate again)
me:     B5-0619 -> B5-0621        (diverged: converged)
```

Renumbering into the slot the other writer is *about to vacate* — or has just vacated —
is a deadlock, not a fix. Both agents are behaving reasonably and the duplicate persists.
**Divergence works; adjacency does not.** When you must renumber away from a collision,
skip past the contested region rather than taking the next integer.

## Also observed, and left alone

While resolving this, the other writer's insertion left a bare `</blockquote>` line
between two rows: not a row, not valid table content, invisible to a row parser and
therefore invisible to every tool built on one. It had vanished by the final census, so
no action was taken. It is recorded because **anything that is not a parseable row is
outside the reach of row-oriented tooling** — a schema check that only inspects matched
rows cannot see the garbage between them.

## Applies to

Any multi-writer system that keys shared state by an ID chosen independently by each
writer: task ledgers, work queues, ticketing, build matrices, any `id -> state` map. The
generalisation is about *keyed shared state under concurrent writers*, not about markdown
or about ledgers specifically.

## Read before

Writing any row to a shared keyed file whose writers choose their own keys — and
especially before trusting a self-check that counts what the parser saw.
