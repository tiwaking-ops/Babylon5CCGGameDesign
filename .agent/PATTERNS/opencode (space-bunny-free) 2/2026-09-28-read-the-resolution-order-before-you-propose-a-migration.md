---
document:
  title: "Read the resolution order before you propose a migration"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0785"
---

# Read the resolution order before you propose a migration

**Pattern.** When a defect looks like it needs new structure — a field, a schema
amendment, a registry entry, a migration of every existing record — find the consumer's
**resolution order** first and check whether its *first* branch already accepts the fix. A
first-match resolver is a compatibility layer that is already deployed and already paid
for; a migration is a second one you would be building.

**Where it applied.** An identity collision in a shared store (`agent_id` →
one heartbeat file per id) was diagnosed as a naming problem and proposed as one. The
validator resolved an id by **exact filename-stem match, then registry, then
sanitisation**. Exact stem match is first, so a new id made of characters the filesystem
accepts resolves with **no schema change, no registry row, no validator edit, and no
migration of the 32 existing files**. The whole promotion was two paragraphs of
governance text. The obvious alternative — add a `session_uuid` field, amend the schema,
migrate every file, keep the new invariant true forever — was strictly more work for the
same behaviour.

**The tell that you are about to propose the expensive fix.** You are about to add a
*new* identifier rather than a *new spelling* of an existing one. Ask why the old
identifier cannot carry the distinction. Usually the answer is that the old one names the
wrong thing — here a *model* where the store needed a *session* — and a spelling change
plus a naming rule is sufficient. A new field is only justified when the distinction is
genuinely orthogonal to what the old identifier names.

**Corollary: the diagnostic is the deliverable.** In the two rows that discovered this, the
expensive part was never the fix. It was measuring that (a) a lookalike code point
(U+F03A fullwidth colon) passes every filename constraint, (b) the store's normaliser
keeps only letters and digits, so two spellings collapse to one key, and (c) the index
holds one value per key, so the second instance is *absent* rather than ambiguous. Each
of those is a runnable measurement. None of them is a design decision, and all of them
changed what the fix had to be.

**How to apply.** On any "these records collide / these names clash" defect: find the
consumer, read the order in which it tries to resolve a name, and run the normaliser
against the candidate spellings *before* designing anything. If the first branch already
matches, the answer is a convention, not a migration — and the convention is worth
writing down, because the next agent will otherwise reach for the schema.

**Supersedes / relates.** Same family as `a-rule-written-down-and-implemented-once-is-a-rule-one-component-follows`
(this session's namespace) and as this repo's `claim-liveness-protocol-proposal.md`,
which specified a four-key conjunction and prefix matching that the *normalised-name*
join made unnecessary — rejected by human ruling 2026-09-28. Per `AGENTS.md` §6 this
record is advisory and confers no authority by being cited.
