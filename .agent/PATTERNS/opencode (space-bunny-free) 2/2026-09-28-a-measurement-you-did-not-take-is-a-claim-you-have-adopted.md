---
document:
  title: "A measurement you did not take is a claim you have adopted"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  assessor_llm: []
  last_modified_by_llm: {name: "opencode (space-bunny-free) 2", version: "space-bunny-free"}
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
  task: "B5-0801"
---

# A measurement you did not take is a claim you have adopted

**Pattern.** Reporting a number you obtained from a document, rather than from a query, is
a **claim** — you have adopted someone else's measurement and will answer for it. It fails
the same way an unchecked assertion fails, and it fails *worse*, because it arrives with
the borrowed authority of a document you did not write and were not asked to defend.

**Three instances in one session, all mine, all the same shape.**

1. "0 of 829 cards carry a Power stat" — relayed from a proposal's §4, never queried. The
   proposal's own method (grep for fields *opening with* `power`) could not have detected
   the true case. Correct figure: 13 records, none granting a Power stat. The conclusion
   survived; the number and the reasoning did not.
2. "B5-0789, seeded and unclaimed" — an ID named in conversation, never checked against the
   ledger. It was a real row, already closed DONE by another agent, for unrelated work.
3. A report paragraph asserting a near-miss write defect that never happened — drafted as a
   narrative beat before re-reading my own command.

**The tell.** Each one *felt* like reporting rather than deciding, which is exactly why
none of them triggered caution. Facts acquired by reading feel inert; they arrive already
shaped, and the reader's eye skips them precisely because they look like context rather
than a finding.

**The fix, and it is one command.** Before stating a number to a human, run the query that
produces it — in the same breath, not later. If the query is expensive, say "I have not
verified this" instead of stating the figure. **"Unverified" is a complete sentence.** The
asymmetry is brutal: an unverified number costs one sentence to retract and can cost a
human decision made on it; the query costs seconds.

**The deeper form.** A *baseline* converts this class from a reporting risk into a
mechanical one. Frozen fingerprints plus a by-`id` diff mean "has this changed, what
changed, what vanished" is answered by running something rather than by remembering
accurately — and a silently dropped record becomes a `REMOVED: 1` line instead of an
absence nobody notices. Proven in B5-0801: 0/0/0/446 on an identical pool, 0/1/2/443 on a
mutant, naming `char_jeffrey_sinclair.text` and `char_gkar.rarity`.

**How to apply.** Two habits. Before quoting any figure: run the query, or mark it
unverified. Before accepting data from anywhere — a human, a scrape, a generator — freeze
a baseline and diff against it, so that correctness is a report rather than an opinion.

**Supersedes / relates.** Generalises `a-proxy-check-passes-forever-when-it-shares-its-failure-mode`,
`a-rule-you-just-wrote-is-not-evidence-that-you-followed-it` and
`a-census-warning-is-a-defect-report` (this session's namespace) from the
implementation and gate cases to the **reporting** case, which is the one a human acts on.
Per `AGENTS.md` §6 this record is advisory and confers no authority by being cited.
