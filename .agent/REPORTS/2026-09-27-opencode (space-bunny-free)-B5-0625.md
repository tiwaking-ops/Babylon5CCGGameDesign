---
document:
  title: "B5-0625 — filename conformance: four renames, one citation, and a loop procedure"
  status: "Report (no authority)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0625 — filename conformance

Human approvals: *"Want me to rename those 4 to the canonical spelling? APPROVED"* and
*"Save this prompt as AGENT_LOOP.md APPROVED"*. One row for both, because they are
the same subject.

## Part 1 — the fragmentation was real, and mine

The rule from B5-0623 is narrow: sanitise **only** `:` and `/` to `-`, and preserve
every other character. The evidence is not a matter of interpretation — it is in the
registry pairs themselves:

```
Kilo (kilo-auto/free)              ->  Kilo (kilo-auto-free).json
kilo (nvidia/nemotron-3-ultra-550b-a55b:free)
                                    ->  kilo (nvidia-nemotron-3-ultra-550b-a55b-free).json
solar-pro4:free                    ->  solar-pro4-free.json
```

Spaces and parentheses survive. Zero report files anywhere in the repo contain a
colon.

Against that rule, the repo held **eight reports for this one `agent_id`, split evenly
across two spellings**:

```
2026-09-27-opencode (space-bunny-free)-B5-0621.md          <- canonical
2026-09-27-opencode-(space-bunny-free)-seed-wave-9.md      <- not
```

The hyphenated form additionally converts **spaces**, which no rule authorises — and
that is what makes it harmful rather than merely inconsistent. Spaces→hyphens does not
produce a second filename for one agent; it produces what looks like a **second
agent**, which is the exact failure Amendment A1.1 exists to prevent, where two files
assert one identity and `live_claims` becomes ambiguous however well-formed either is.

The sharp part: this was produced by the very `agent_id` the rule was written about,
in the same session that wrote it. A rule is not evidence that it was followed.

### Rename with a citation census on both sides

Renaming a cited file is the operation that hid 14 rows once already (B5-0613). So the
census ran **before** the rename, not after:

| check | result |
|---|---|
| target names already exist? | 0 of 4 — no rename could overwrite anything |
| live references to the old names | **1** — `docs/DECISIONS.md:4286` |
| references to old names, after repair | **0** repo-wide |
| references to canonical names | 8 checked, **8 resolve**, 0 dangling |

Four files renamed (two via `git mv` where tracked, two by move where untracked), one
citation repaired. The agent now has eight reports under **one** spelling instead of
eight split across two.

## Part 2 — the prompt, checked against the repo rather than trusted

Filed as `.agent/AGENT_LOOP.md`, scoped deliberately as an **operating procedure, not
governance**, with an explicit precedence line that `AGENTS.md` and `00_BOOT.md` win on
conflict. Without that line a future agent could read a procedure as a source of
authority, which is the failure mode of every other document in `.agent/`.

Verifying the approved text against the repo found **two stale statements in it**:

1. *"incl. step 10 pattern skim"* — **step 11**. B5-0614 inserted the census step and
   renumbered 5–11 to 6–11. The prompt predated that repair.
2. *"STOP when: the ledger is empty"* — the ledger is never empty. It accumulates; 322
   closed rows is a healthy ledger, not an empty one. Now reads "no OPEN row remains".

Both were fixed before filing, and both are the same species of error this repo keeps
recording: **a pointer that is adjacent to a citation but was never checked against
it.** The prompt was written by a human from memory of a file that had since changed
underneath it, and it read as authoritative because it was a complete, confident
specification.

The file also carries a **clause-to-failure table** mapping thirteen rules to the
recorded incident that made each necessary (B5-0597, B5-0609, B5-0613, B5-0618,
B5-0622, B5-0568, B5-0611, A1.1, and others), because a procedure clause with no
failure behind it is decoration, and three ways this loop has lied to itself:

- a pre-write id check that races another reader, and is therefore insufficient;
- a fix that removes a symptom by introducing a silently degenerate value (B5-0624);
- a validator never observed red, which is not evidence of anything.

**Reusable lesson:** a rule you have just written is not evidence that you followed
it — the fragmentation here was created by the agent that authored the rule, four
files at a time. And a confident, complete specification is not a verified one: both
defects in the approved prompt were invisible precisely because the surrounding text
read as though it had been checked.
