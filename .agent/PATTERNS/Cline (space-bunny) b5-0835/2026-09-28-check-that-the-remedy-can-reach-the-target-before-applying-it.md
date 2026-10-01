---
document:
  title: "Check that the remedy can reach the target before applying it"
  status: "Pattern (advisory, never canonical)"
provenance:
  author_llm: {name: "Cline (space-bunny) b5-0835", version: "space-bunny"}
  assessor_llm: []
  created_date: "2026-09-28"
  last_modified_date: "2026-09-28"
task: "B5-0835"
---

# Check that the remedy can reach the target before applying it

A task row can carry its own *measured* premise — "measured in this pass that X is
VISIBLE, therefore eligible" — and the measurement can be wrong, or right about the
thing it measured and wrong about the thing that follows. B5-0835 named six root
artefacts a literal commit would sweep in. Four of them never were, and two of the
remaining could not be fixed by the prescribed remedy at all.

**The rule.** Before applying a fix, establish two things separately: (1) does the
problem actually exist on the target, and (2) can this remedy reach it? A row
answering only (1) hands you a plan whose second half is a no-op.

**Two failures, one pass.** `git status --untracked-files=all` returned *zero* entries
for `.mimocode`, `.qwen`, `.freebuff` and `Pene` — all four already invisible, for
three different reasons: a nested self-ignore, an empty directory git cannot track,
and two paths already *tracked*. Meanwhile the remedy — "gitignore them" — cannot
reach a tracked path, because gitignore governs untracked paths only. So the fix
would have written six entries and achieved two, while the file on disk read like
six.

**The tell.** A remedy that leaves no trace of its own failure is the dangerous kind.
`check-ignore` exiting 1 on `.qwen/settings.json` *after* the entry was written was
the honest reading, and the only reason it was available was that the file comment
said the entry was inert by construction. Record the inert entries, with the reason
and the command that would actually complete them, rather than letting the entry
count imply coverage.

**Corollary — a byte diff is not a containment test.** `ledger.bak` differs from the
live ledger by 17 lines, which reads alarming. The question was never "do they
differ" but "does the backup hold anything the live file lacks", and the answer was
a set containment: 388 backup task ids, 432 live, difference *empty*. It was a trap,
not a recovery asset. Ask the set question, not the diff question — a large diff can
be entirely old news, and a small one can hide a missing row.

**Corollary — a build that reports 1 on stderr is not a red gate.** `compile.bat`
printed the expected `-source 1.6` bootstrap-classpath warning and a console capture
reported failure. Redirecting output recovered a true exit 0. Capture the exit code;
do not let a warning on stderr adjudicate a gate that never went red.

Related: `Cline (space-bunny) b5-0807/2026-09-28-a-status-cell-is-the-last-byte-a-close-out-writes.md`
(complementary: that one is about finishing the close-out correctly, this one about
whether the close-out was worth doing). Does not supersede it; both stand.