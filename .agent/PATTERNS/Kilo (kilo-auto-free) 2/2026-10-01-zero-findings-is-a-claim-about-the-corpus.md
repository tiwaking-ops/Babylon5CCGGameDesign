---
document:
  title: "Zero findings is a claim about the corpus — and re-run your own census under a looser anchor"
  status: "Pattern (advisory only, same tier as investigations/; never canonical)"
provenance:
  author_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  assessor_llm: []
  last_modified_by_llm: {name: "Kilo (kilo-auto/free) 2", version: "kilo-auto/free"}
  created_date: "2026-10-01"
  last_modified_date: "2026-10-01"
  task: "B5-1435"
---

# Zero findings is a claim about the corpus

**The lesson, part 1 — a census result is about the bytes you read.** B5-1435 was
asked to turn four incidental defect observations into a whole-file census of an
append-only log. The census came back **0, 0, 0, 0** on the working file. It was
not clean: **100% of the committed entry headers were absent** (290 distinct at
`HEAD`, 0 present in the worktree, 94% of committed bytes gone). Every class read
green because its evidence had been deleted, not repaired. Run against the
committed bytes the same instrument returned **23 ordering violations, 16
stragglers, 12 headers invisible to the anchor, 2 duplicate header pairs**.

So: **when a structural census returns zero, the first question is not "is the
file clean?" but "which corpus was that, and does it still contain the class?"**
Report per-class counts *with the corpus and its line total attached*, and when a
class has no surviving instance, state which of the two very different things
happened — **fixed** or **deleted**. Only one of them is a repair, and the
difference decides whether the class needs work at all.

**The lesson, part 2 — audit the anchor, not just the file.** Re-running the same
census under a deliberately *looser* header pattern (leading whitespace and a
stray U+FEFF tolerated, `#{2,}` instead of `## `) found **12 headers the strict
pattern could not see**: three with leading spaces, eight level-3 sub-entries,
and one complete, bodied entry header carrying a U+FEFF that is neither
whitespace nor ASCII — invisible to `^##` *and* to `TrimStart`. This is the same
defect `run-queue.ps1`'s `Get-LedgerRows` self-check was written for, where a
census compared only against its own regex reported `312==312` while 9 rows
carried a corrupt status: **a set compared against itself can never detect a
member the comparison cannot see.**

**The lesson, part 3 — a defect class can be a defect in your reader.** One of
the four inherited classes was "mojibake in the 09-28 headers". Dumping the
actual codepoints showed the only 09-28 header in the file is **pure ASCII** with
a plain hyphen. The mojibake was rendered by the reading path, not read off the
bytes. What the class was *describing* — a title that cannot be cited byte-exactly
— turned out to be real, two lines away from where anyone was looking, as two
U+FFFD replacement characters on a **09-24** header.

**Applies to.** Any structural census, integrity sweep, or "X is clean" claim over
a file that has been truncated, rewritten, restored, or heavily appended to — and
to any mojibake or encoding finding, which is a claim about a *read* until a
codepoint dump says otherwise.

**Supersedes nothing.** Second record in this namespace; the first
(`2026-10-01-a-dropped-heading-is-a-dropped-constraint.md`) is the sibling lesson
about auditing a merge rather than its result.
