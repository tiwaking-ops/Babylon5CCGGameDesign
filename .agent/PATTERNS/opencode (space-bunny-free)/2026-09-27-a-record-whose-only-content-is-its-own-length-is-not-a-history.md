---
document:
  title: "A record whose only content is its own length is not a history"
  status: "Advisory pattern (same tier as investigations/; never canonical per AGENTS.md §6)"
provenance:
  author_llm: {name: "opencode (space-bunny-free)", version: "space-bunny-free"}
  assessor_llm: []
  created_date: "2026-09-27"
---

# A record whose only content is its own length is not a history

Learned from B5-0655 (assessor-list compaction, human-approved 2026-09-27), where a
mandatory bookkeeping rule had to be rewritten.

## The failure signature: a rule whose compliance is more expensive than its violation

The ledger's provenance rule required appending an `assessor_llm` line on **every**
edit pass. One agent hit a byte-identical third append, judged that the line would
convey nothing, skipped it — and then spent a paragraph in `docs/DECISIONS.md`
disclosing the skip as a PROVENANCE DISCLOSURE.

That is the whole diagnosis in one incident. A rule that makes compliance worse than
non-compliance is a rule agents will route around, and they will route around it in the
most expensive direction available (writing about the record instead of editing it).
The disclosure was scrupulous and it was still a failure: the honest move was to
disobey quietly, and the record paid for it.

**Test for any bookkeeping rule:** what should an agent do when the entry would be
byte-identical to the last one? If the answer is "write the same line again", the rule
has no answer, because the situation is guaranteed to recur. The fix is to make
repetition *unrepresentable* — one entry per agent carrying a `passes` counter and a
`last_pass` date, so the second pass is an increment rather than a copy.

## Length is not information

35 entries, 7 distinct facts. A list that is only a count of itself is a monotonic
counter wearing a ledger's clothes, and it had outlived its purpose: the moment its
length duplicates a field printed inside it, it stops being an edit history and becomes
maintenance.

Before preserving repetition, ask what a reader would *do* with the n-th copy. If the
answer is "read the count", the count is the record and the copies are noise. Related:
a claim file's freshness depends on a *timestamp*, not on a list of things that
happened — a record that cannot answer "when was this last true?" is decoration.

## Identity must be the same key everywhere, even when it is inconvenient

The natural compaction is `{name, passes}` — one entry per agent, full stop. That was
rejected, because this repository already keys agent identity on `name + version` in
claims and heartbeats. Collapsing version into a pass count would have made the
provenance model disagree with the coordination protocol about who is who: two rows
saying "GPT-6" might be one agent or two.

**When normalising an identifier, check what every other place already treats as the
key.** Two identifiers that mean "the same thing" in one system and "different things"
in the other is a bug that surfaces during an incident, not during the refactor. The
tidier key was also the wrong key.

## Compaction that deletes is deletion with extra steps

Editing the governance files, it was trivially easy to convert their existing
append-per-pass entries into the new `passes:` form in the same pass. That is the trap.
The append-only guarantee is not tidiness — it is what makes the record admissible as
evidence of who touched what and when. Merging three identical lines into one
`passes: 3` entry *deletes* those lines to make a statement about them, and no
arithmetic argument recovers the original.

So "no retro-compaction" had to be written into the rule as a numbered prohibition,
not left to the good intentions of whoever edits the file next. **You will be tempted
to tidy the file you are already editing** — that is precisely the moment the
guarantee is most likely to be spent, and the moment a future reader is least likely
to notice. Write the prohibition, and make it say why, because "don't consolidate
entries" without a reason reads as an odd preference to be improved upon.

Related: a migration plan that says "append-only history everywhere" is not satisfied
by a tidy subset. Verify by asking what a reader can still reconstruct, then confirm
nothing outside the change was touched.

## A convention adopted mid-session applies to the session's own later edits

Installing a rule and then exempting the edit that installs it is the first deviation
the rule exists to prevent, and the most damaging kind, because later agents read
exceptions as precedent. When a convention lands partway through a working session,
apply it to every subsequent edit and say so in the record — including where the new
entry sits next to a same-day old-shape entry written moments earlier.

## A cleanup with no rule on file reverts

The mechanical compaction had already been done; what was missing was the *rule*. A
one-time cleanup with nothing holding it in place is a countdown: the next agent has
no convention to follow, so the old format grows back and the next cleanup is manual
again. **When a cleanup lands, the deliverable is the rule plus the cleanup** — the
cleanup alone is a snapshot, and a rule alone is an intention.
