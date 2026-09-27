---
document:
  title: "B5-0655 — assessor_llm compaction convention adopted"
  status: "Report (observation and test results; no authority per AGENTS.md §3)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  created_date: "2026-09-27"
  last_modified_date: "2026-09-27"
---

# B5-0655 — assessor_llm compaction convention adopted

Human ruling 2026-09-27: **YES** — adopt Buffy's assessor-list compaction proposal
(B5-0586) and write the convention into `AGENTS.md` section 1 and the provenance
section of `guidelines/Guidelines.md`.

Governance docs only. No frontmatter anywhere in the repository was migrated.

## Why this was a real gap and not a cosmetic one

B5-0604 already compacted the ledger's assessor list mechanically, and the DECISIONS
entry for that work explicitly recorded the rule change as "remains pending". So the
repository was in a strange state: the ledger had been compacted, but the convention
that was supposed to govern the format had never been written down anywhere.

That matters because the rule is what the *next* pass follows. With the ledger
compacted and no rule on file, the next edit has nothing to follow and the
append-per-pass form grows straight back — the compaction is a one-time cleanup with
nothing holding it in place. Writing the rule down is what makes the cleanup durable.

## The old rule had already forced a deviation to escape the trap

The strongest evidence that the old rule was wrong is that it had already been broken
under pressure, and broken in the most expensive available direction. One agent faced
a byte-identical third append and, rather than write a line conveying zero
information, skipped the append and recorded the skip as a **PROVENANCE DISCLOSURE**
in `docs/DECISIONS.md`.

That is the tell. A working rule should not make compliance-with-it *worse* than
non-compliance, because the moment it does, agents start choosing which violation
costs less. Under the adopted convention that same situation is simply compliant:
increment `passes`, refresh `last_pass`, move on, no disclosure needed. A convention
whose compliance requires a disclosure ceremony will get disclosures, and the record
fills with text about the record.

## The form, and why version is part of identity

One entry per agent per file:

```yaml
assessor_llm:
  - {name: "GPT-6 Codex", version: "GPT-6", passes: 14, last_pass: "2026-09-26"}
```

Repeat pass increments `passes` in place and sets `last_pass`. A first pass, or a
same-agent **different version**, appends a new entry with `passes: 1`.

Version is part of identity because that is already how this repository identifies
agents everywhere else — claim files and heartbeats both key on
`name + version`, and a version bump is a different agent as far as coordination is
concerned. Letting version collapse into a pass count on disk but not in the ledger
would have produced a provenance model that disagrees with the claims protocol about
who is who. Consistency with the existing identity rule beat maximal tidiness.

`note` is optional and only for substantive passes, so the signal survives: a list of
`passes: 14` entries with no notes still tells you 14 passes happened, while notes on
the few that mattered tell you what they were.

## No retro-compaction, and why I did not "tidy up" while I was in there

I was editing both files and could have converted their existing entries to the new
form in the same pass. That would have been rule 4's deletion case wearing a tidier
hat. Rule 4 says entries are never deleted, renamed, or rewritten beyond
`passes`/`last_pass`/`note`, and the append-only guarantee is the property that makes
the ledger trustworthy for forensics — precisely the forensics this repository has
relied on. Merging three identical appends into one `passes: 3` entry destroys the
entries to express a fact about them.

So both files were edited by **appending one new entry in the compacted shape**,
leaving every pre-existing entry byte-identical, and the rule says so explicitly
("lists already on disk keep their full contents forever"). The migration plan's
append-only-history-everywhere constraint holds: no document's frontmatter was
touched anywhere in the repository.

## Attribution density is the constraint that made this safe

The proposal's own test was whether compaction destroys information. It does not, and
the reason is visible in how this repository actually reconstructs coordination
events: the B5-0433 claim destructions, the index absorption, the B5-0618 seeding
collision — all recovered from **pass counts plus report files and note text**, not
from how many times a name is typed in a row. Repetition was carrying no information
the count did not already carry, which is why 35 entries held 7 facts.

## The rule applied to the edit that installed it

This session's own B5-0657 entry appended to `.agent/00_BOOT.md` used the compacted
shape on the same day the rule was adopted, because a convention adopted mid-session
applies to edits made after adoption. Inventing an exception for the very edit that
installs the rule would be the first deviation the rule was written to prevent — and
it would have been the kind of deviation that later agents would read as precedent.

## Verification

`b5ccg/compile.bat` green, `Build successful`, JDK 1.8.0_292 — tree-health reading
only, no code in scope. Both governance files re-read after writing: section 1a and
the Guidelines paragraph carry the same five numbered rules with the same wording,
`passes: 1` entries appended to each file's `assessor_llm`, `last_modified_by_llm`
updated to this agent, original `author_llm` values untouched (`Muse Spark` in
`AGENTS.md`, `unknown` in `Guidelines.md` — left unknown rather than inferred).
Ledger: duplicate-ID census empty, `7` pipes, `doubleLead no`.

## Reusable lesson

A record whose only content is its own length is not a history — it is a copy counter
that has stopped earning its bytes. The tell that a bookkeeping rule has failed is not
that it is verbose, but that complying with it has become *more* expensive than
breaking it: when agents start filing disclosures about skipping a line, the rule is
the problem. And compaction is only safe while the append-only guarantee survives it;
merging existing entries is deletion with extra steps, so "no retro-compaction" has to
be written as a rule rather than trusted to judgement, because tidying the file you
are already editing is exactly when you will decide it would be a favour.
