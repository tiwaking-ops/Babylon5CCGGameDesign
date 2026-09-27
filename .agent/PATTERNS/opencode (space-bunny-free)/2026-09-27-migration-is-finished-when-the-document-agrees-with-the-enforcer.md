---
document:
  title: "A migration is finished when the document agrees with the enforcer, not when the data passes"
  status: "Advisory pattern record"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# A migration is finished when the document agrees with the enforcer, not when the data passes

Advisory only, same tier as `investigations/` and the rest of this store. Never canonical.
Cite freely; citing confers no authority.

**Fourth instance in this namespace.** Same family as:

* `a-self-certifying-gate-clause-is-not-a-precondition.md` — asserts its own satisfaction
* `a-self-check-must-not-share-its-subjects-pattern.md` — compares a set against itself
* `2026-09-27-a-duplicate-task-id-is-silently-invisible-to-a-keyed-status-map.md`
* `2026-09-27-a-proxy-check-passes-forever-when-it-shares-the-failure-mode.md`

All four are **one artefact disagreeing with another while every test is green.**

## The rule

> Data passing is not migration complete.
> **Migration is complete when the thing that EXPLAINS the data agrees with the
> thing that ENFORCES it.**

Those are separate artefacts with separate lifecycles, and only one of them is
usually under test. A store can be 100% migrated, its validator can exit 0, and the
specification a newcomer is told to read can still describe the *old* world — with
nothing red anywhere to say so.

## The worked instance

A heartbeat migration was declared "half-finished" and ordered finished. Measured
before repairing anything:

```
validate-heartbeats.ps1    exit 0   27 files, 27 conforming, 0 non-conforming
migrate-heartbeats.ps1     MIGRATE-JSON 28, nothing outstanding
git status                 10 paths deleted   <- looked like data loss
```

The data was finished. The 10 "deletions" were sanitised **renames**
(`solar-pro4:free` → `solar-pro4-free.json`, `:` being illegal in Windows
filenames), and six genuinely non-conforming files were correctly quarantined.

The real defect was that `00_BOOT.md` told every booting agent:

> Heartbeat format: `.agent/HEARTBEATS/README.md` is **binding** — strict JSON with
> `schema_version`, `agent_id`, `utc`, `state`, and `live_claims` …

and `HEARTBEATS/README.md` was still the pre-migration file — four fields, none of
the semantics. The enforced schema existed **only inside the validator**. So:

* an agent obeying the boot protocol wrote pre-schema files,
* the validator rejected them,
* and the validator's complaint pointed at a document that had never mentioned the
  requirement.

Nothing was red. The store was perfect and the instructions were a lie.

A second drift in the same place: the validator's enum was `active|idle|busy` while
its two *diagnostic messages* still said `active|idle`. It silently accepted a value
it told authors was invalid — so an author could not tell whether to fix the file or
fix their understanding.

## Why the "half-finished" framing nearly caused the wrong repair

The obvious move on "half-migrated" is another **data** pass. That pass would have
found nothing to do, reported success, and left the only real defect untouched —
while consuming the task and creating a false paper trail. "Half-finished" names a
**state**; it does not name a **cause**, and the cause determines the repair.

**Measure which half is missing before repairing either.** In this case the data half
was done and the document half was not, and only one of those is fixable by running
the migration tool again.

## The check that generalises

For any schema, protocol, standard or contract, ask of each pair:

| artefact | question |
|---|---|
| the **data** | does it validate? |
| the **enforcer** | what exactly does it require? |
| the **document** a newcomer is pointed at | does it state the same thing, field for field? |
| the **diagnostics** | do they agree with what the enforcer *accepts*? |

The last row is the one nobody checks, and it is the one that misleads: a tool that
accepts a value while its error text forbids it teaches the reader that the tool is
wrong, which is a worse outcome than the tool being simply absent.

Derive diagnostics from the source of truth (`$ValidState`), never retype the
literal beside it. Two copies of a list is one copy too many.

## The durable form

Documentation is not commentary on an implementation. Once a document is cited as
**binding**, it is an interface with an independent consumer, and it needs the same
drift protection as code: a test, or a generated-from-source arrangement. Otherwise
the failure is not that someone lied — it is that two true statements about
different artefacts quietly diverged while every signal stayed green.

**Applies to:** any migration, schema rollout, deprecation, or protocol adoption —
especially partial ones, where the data is usually migrated first and the prose
last, and the prose is the only part with no test.

**Read before:** declaring any migration complete, or pointing a reader at a document
as authoritative.
